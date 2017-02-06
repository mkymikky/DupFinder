package de.b0n.dir.processor;

import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Sucht in einem gegebenen Verzeichnis und dessen Unterverzeichnissen nach
 * Dateien und sortiert diese nach Dateigröße.
 * 
 * @author Claus
 *
 */
public class DuplicateLengthFinder {

	private final File folder;
	private final ExecutorService threadPool;
	private final DuplicateLengthFinderCallback callback;

	private final Queue<Future<?>> futures = new ConcurrentLinkedQueue<Future<?>>();
	private final Cluster<Long, File> result = new Cluster<Long, File>();

	/**
	 * Bereitet für das gegebene Verzeichnis die Suche nach gleich großen
	 * Dateien vor.
	 * 
	 * @param threadPool
	 *            Pool zur Ausführung der Suchen
	 * @param folder
	 *            zu durchsuchendes Verzeichnis, muss existieren und lesbar sein
	 */
	private DuplicateLengthFinder(final File folder, final ExecutorService threadPool,
			DuplicateLengthFinderCallback callback) {
		if (!folder.exists()) {
			throw new IllegalArgumentException(
					"FEHLER: Parameter <Verzeichnis> existiert nicht: " + folder.getAbsolutePath());
		}
		if (!folder.isDirectory()) {
			throw new IllegalArgumentException(
					"FEHLER: Parameter <Verzeichnis> ist kein Verzeichnis: " + folder.getAbsolutePath());
		}
		if (!folder.canRead()) {
			throw new IllegalArgumentException(
					"FEHLER: Parameter <Verzeichnis> ist nicht lesbar: " + folder.getAbsolutePath());
		}

		this.threadPool = threadPool;
		this.folder = folder;
		this.callback = callback;
	}

	private Cluster<Long, File> execute() {
		futures.add(threadPool.submit(new DuplicateLengthRunner(folder)));

		while (!futures.isEmpty()) {
			try {
				futures.remove().get();
			} catch (InterruptedException | ExecutionException e) {
				throw new IllegalStateException("Threading has failed: " + e.getMessage(), e);
			}
		}
		return result;
	}

	private class DuplicateLengthRunner implements Runnable {
		private final File folder;

		public DuplicateLengthRunner(File folder) {
			this.folder = folder;
		}

		/**
		 * Iteriert durch die Elemente im Verzeichnis und legt neue Suchen für
		 * Verzeichnisse an. Dateien werden sofort der Größe nach abgelegt.
		 * Wartet die Unterverzeichnis-Suchen ab und merged deren
		 * Ergebnisdateien. Liefert das Gesamtergebnis zurück.
		 */
		@Override
		public void run() {
			String[] contents = folder.list();
			if (contents == null) {
				try {
					System.err.println(folder.getCanonicalPath() + " cannot list its content.");
				} catch (IOException e) {
				} finally {
					return;
				}
			}

			for (String fileName : contents) {
				File file = new File(folder.getAbsolutePath() + System.getProperty("file.separator") + fileName);
				if (!file.canRead()) {
					continue;
				}

				if (file.isDirectory()) {
					try {
						futures.add(threadPool.submit(new DuplicateLengthRunner(file)));
						if (callback != null) {
							callback.enteredNewFolder(file.getCanonicalPath());
						}
					} catch (IllegalArgumentException e) {
						System.err.println("Given Folder is invalid, continue with next: " + file.getAbsolutePath());
						continue;
					} catch (IOException e) {
						System.err.println("Callback ");
					}
				}

				if (file.isFile()) {
					result.addGroupedElement(Long.valueOf(file.length()), file);
				}
			}
			return;
		}
	}

	/**
	 * Einstiegstmethode zum Durchsuchen eines Verzeichnisses nach Dateien
	 * gleicher Größe. Verwendet einen Executors.newWorkStealingPool() als
	 * ThreadPool.
	 * 
	 * @param folder
	 *            Zu durchsuchendes Verzeichnis
	 * @return Liefert eine Map nach Dateigröße strukturierten Queues zurück, in
	 *         denen die gefundenen Dateien abgelegt sind
	 */
	public static Cluster<Long, File> getResult(final File folder) {
		return getResult(folder, Executors.newWorkStealingPool(), null);
	}

	/**
	 * Einstiegstmethode zum Durchsuchen eines Verzeichnisses nach Dateien
	 * gleicher Größe.
	 * 
	 * @param folder
	 *            Zu durchsuchendes Verzeichnis
	 * @param threadPool
	 *            Pool zur Ausführung der Suchen
	 * @return Liefert eine Map nach Dateigröße strukturierten Queues zurück, in
	 *         denen die gefundenen Dateien abgelegt sind
	 */
	public static Cluster<Long, File> getResult(final File folder, final ExecutorService threadPool) {
		return getResult(folder, threadPool, null);
	}

	/**
	 * Einstiegstmethode zum Durchsuchen eines Verzeichnisses nach Dateien
	 * gleicher Größe. Verwendet einen Executors.newWorkStealingPool() als
	 * ThreadPool.
	 * 
	 * @param folder
	 *            Zu durchsuchendes Verzeichnis
	 * @param callback
	 *            Ruft den Callback bei jedem neu betretenen Verzeichnis auf
	 *            (darf null sein)
	 * @return Liefert eine Map nach Dateigröße strukturierten Queues zurück, in
	 *         denen die gefundenen Dateien abgelegt sind
	 */
	public static Cluster<Long, File> getResult(final File folder, DuplicateLengthFinderCallback callback) {
		return getResult(folder, Executors.newWorkStealingPool(), callback);
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
	 *            (darf null sein)
	 * @return Liefert eine Map nach Dateigröße strukturierten Queues zurück, in
	 *         denen die gefundenen Dateien abgelegt sind
	 */
	public static Cluster<Long, File> getResult(final File folder, final ExecutorService threadPool,
			DuplicateLengthFinderCallback callback) {
		if (folder == null) {
			throw new IllegalArgumentException("folder may not be null.");
		}

		if (threadPool == null) {
			throw new IllegalArgumentException("threadPool may not be null.");
		}

		return new DuplicateLengthFinder(folder, threadPool, callback).execute();
	}
}
