package de.b0n.dir.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.toList;

/**
 * Sucht in einem gegebenen Verzeichnis und dessen Unterverzeichnissen nach
 * Dateien und sortiert diese nach Dateigröße.
 */
public class DuplicateLengthFinder implements Runnable {

	private final File directory;
	private final DuplicateLengthFinderCallback callback;
	private final Executor executor;

	private DuplicateLengthFinder(final File directory, DuplicateLengthFinderCallback callback, Executor executor) {
		this.directory = directory;
		this.callback = callback;
		this.executor = executor;
	}

	/**
	 * Iteriert durch die Elemente im Verzeichnis und legt neue Suchen für
	 * Verzeichnisse an. Dateien werden sofort der Größe nach abgelegt. Wartet die
	 * Unterverzeichnis-Suchen ab und merged deren Ergebnisdateien. Liefert das
	 * Gesamtergebnis zurück.
	 */
	@Override
	public void run() {
		callback.enteredNewDirectory(directory);

		List<File> directoryContent = readContent(directory);
		directoryContent.parallelStream().filter(File::isDirectory)
				.forEach(file -> executor.submit(new DuplicateLengthFinder(file, callback, executor)));
		directoryContent.parallelStream().filter(File::isFile)
				.forEach(file -> callback.addGroupedElement(file.length(), file));
	}

	private List<File> readContent(File directory) {
		String[] directoryContents = directory.list();
		if (directoryContents == null) {
			callback.unreadableDirectory(directory);
			return Collections.emptyList();
		}

		return Arrays.stream(directoryContents).parallel()
				.map(fileName -> new File(directory.getAbsolutePath() + System.getProperty("file.separator") + fileName))
				.collect(toList());
	}

	/**
	 * Einstiegsmethode zum Durchsuchen eines Verzeichnisses nach Dateien gleicher
	 * Größe.
	 * 
	 * @param directory
	 *            Zu durchsuchendes Verzeichnis
	 * @return Liefert ein Cluster nach Dateigröße strukturierten Queues zurück, in
	 *         denen die gefundenen Dateien abgelegt sind
	 */
	public static Map<Long, List<File>> getResult(final File directory) {
		Map<Long, List<File>> result = new HashMap<>();
		DuplicateLengthFinderCallback callback = new DuplicateLengthFinderCallback() {

			@Override
			public void addGroupedElement(Long size, File file) {
				synchronized (this) {
					List<File> group = result.computeIfAbsent(size, k -> new ArrayList<>());
					group.add(file);
				}
			}
		};

		getResult(directory, callback);

		return result;
	}

	/**
	 * Einstiegsmethode zum Durchsuchen eines Verzeichnisses nach Dateien gleicher
	 * Größe.
	 * 
	 * @param directory
	 *            Zu durchsuchendes Verzeichnis
	 * @param callback
	 *            Ruft den Callback bei jedem neu betretenen Verzeichnis auf
	 */
	public static void getResult(final File directory, DuplicateLengthFinderCallback callback) {
		if (directory == null) {
			throw new IllegalArgumentException("directory may not be null.");
		}
		if (callback == null) {
			throw new IllegalArgumentException("callback may not be null.");
		}
		if (!directory.exists()) {
			throw new IllegalArgumentException("directory must exist.");
		}
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException("directory must be a valid directory.");
		}

		Executor executor = new Executor();
		executor.submit(new DuplicateLengthFinder(directory, callback, executor));
		executor.consolidate();
	}
}
