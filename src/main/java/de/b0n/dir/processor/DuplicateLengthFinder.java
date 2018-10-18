package de.b0n.dir.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Sucht in einem gegebenen Verzeichnis und dessen Unterverzeichnissen nach
 * Dateien und sortiert diese nach Dateigröße.
 */
public class DuplicateLengthFinder extends AbstractProcessor implements Runnable {

	private final File folder;
	private final DuplicateLengthFinderCallback callback;
	private final List<Future<?>> futures = new ArrayList<Future<?>>();

	private DuplicateLengthFinder(final File folder, DuplicateLengthFinderCallback callback) {
		this.folder = folder;
		this.callback = callback;
	}

	/**
	 * Iteriert durch die Elemente im Verzeichnis und legt neue Suchen für
	 * Verzeichnisse an. Dateien werden sofort der Größe nach abgelegt. Wartet
	 * die Unterverzeichnis-Suchen ab und merged deren Ergebnisdateien. Liefert
	 * das Gesamtergebnis zurück.
	 */
	@Override
	public void run() {
		callback.enteredNewFolder(folder);

		for (File file : readContent(folder)) {
			if (file.isDirectory()) {
				futures.add(submit(new DuplicateLengthFinder(file, callback)));
			}

			if (file.isFile()) {
				callback.addGroupedElement(Long.valueOf(file.length()), file);
			}
		}

		consolidate(futures);
	}

	private List<File> readContent(File folder) {
		List<File> contents = new ArrayList<>();
		String[] folderContents = folder.list();
		if (folderContents == null) {
			callback.unreadableFolder(folder);
		} else {
			for (String fileName : folderContents) {
				contents.add(new File(folder.getAbsolutePath() + System.getProperty("file.separator") + fileName));
			}
		}
		return contents;
	}

	/**
	 * Einstiegstmethode zum Durchsuchen eines Verzeichnisses nach Dateien
	 * gleicher Größe.
	 * 
	 * @param folder
	 *            Zu durchsuchendes Verzeichnis
	 * @return Liefert ein Cluster nach Dateigröße strukturierten Queues zurück, in
	 *         denen die gefundenen Dateien abgelegt sind
	 */
	public static Cluster<Long, File> getResult(final File folder) {
		Cluster<Long, File> cluster = new Cluster<>();
		DuplicateLengthFinderCallback callback = new DuplicateLengthFinderCallback() {

			@Override
			public void unreadableFolder(File folder) {
				return;
			}

			@Override
			public void enteredNewFolder(File folder) {
				return;
			}

			@Override
			public void addGroupedElement(Long size, File file) {
				cluster.addGroupedElement(size, file);
			}
		};

		getResult(folder, callback);

		return cluster;
	}

	/**
	 * Einstiegstmethode zum Durchsuchen eines Verzeichnisses nach Dateien
	 * gleicher Größe.
	 * 
	 * @param folder
	 *            Zu durchsuchendes Verzeichnis
	 * @param threadPool
	 *            Pool zur Ausführung der Suchen
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

		consolidate(submit(new DuplicateLengthFinder(folder, callback)));
	}
}
