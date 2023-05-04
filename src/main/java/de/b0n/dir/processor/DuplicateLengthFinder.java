package de.b0n.dir.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sucht in einem gegebenen Verzeichnis und dessen Unterverzeichnissen nach
 * Dateien und sortiert diese nach Dateigröße.
 */
public class DuplicateLengthFinder implements Runnable {

	private final File folder;
	private final DuplicateLengthFinderCallback callback;
	private final Executor executor;

	private DuplicateLengthFinder(final File folder, DuplicateLengthFinderCallback callback, Executor executor) {
		this.folder = folder;
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
		callback.enteredNewFolder(folder);

		List<File> folderContent = readContent(folder);
		folderContent.parallelStream().filter(File::isDirectory)
				.forEach(file -> executor.submit(new DuplicateLengthFinder(file, callback, executor)));
		folderContent.parallelStream().filter(File::isFile)
				.forEach(file -> callback.addGroupedElement(file.length(), file));
	}

	private List<File> readContent(File folder) {
		String[] folderContents = folder.list();
		if (folderContents == null) {
			callback.unreadableFolder(folder);
			return Collections.emptyList();
		}

		return Arrays.stream(folderContents).parallel()
				.map(fileName -> new File(folder.getAbsolutePath() + System.getProperty("file.separator") + fileName))
				.collect(Collectors.toList());
	}

	/**
	 * Einstiegsmethode zum Durchsuchen eines Verzeichnisses nach Dateien gleicher
	 * Größe.
	 * 
	 * @param folder
	 *            Zu durchsuchendes Verzeichnis
	 * @return Liefert ein Cluster nach Dateigröße strukturierten Queues zurück, in
	 *         denen die gefundenen Dateien abgelegt sind
	 */
	public static Map<Long, List<File>> getResult(final File folder) {
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

		getResult(folder, callback);

		return result;
	}

	/**
	 * Einstiegsmethode zum Durchsuchen eines Verzeichnisses nach Dateien gleicher
	 * Größe.
	 * 
	 * @param folder
	 *            Zu durchsuchendes Verzeichnis
	 * @param callback
	 *            Ruft den Callback bei jedem neu betretenen Verzeichnis auf
	 */
	public static void getResult(final File folder, DuplicateLengthFinderCallback callback) {
		if (folder == null) {
			throw new IllegalArgumentException("folder may not be null.");
		}
		if (callback == null) {
			throw new IllegalArgumentException("callback may not be null.");
		}
		if (!folder.exists()) {
			throw new IllegalArgumentException("folder must exist.");
		}
		if (!folder.isDirectory()) {
			throw new IllegalArgumentException("folder must be a valid folder.");
		}

		Executor executor = new Executor();
		executor.submit(new DuplicateLengthFinder(folder, callback, executor));
		executor.consolidate();
	}
}
