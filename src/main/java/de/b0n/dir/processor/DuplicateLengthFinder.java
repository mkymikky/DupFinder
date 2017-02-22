package de.b0n.dir.processor;

import java.io.File;
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

	private final Cluster<Long, File> model;
	private final ExecutorService threadPool;
	private final DuplicateLengthFinderCallback callback;

	private final Queue<Future<?>> futures = new ConcurrentLinkedQueue<Future<?>>();


	/**
	 * Bereitet für das gegebene Verzeichnis die Suche nach gleich großen
	 * Dateien vor.
	 * 
	 * @param threadPool
	 *            Pool zur Ausführung der Suchen
	 *
	 * @param callback
	 *            Ruft den Callback bei jedem neu betretenen Verzeichnis auf
	 *            (darf null sein)
	 */
	protected DuplicateLengthFinder(final Cluster<Long,File> model,final ExecutorService threadPool,
			DuplicateLengthFinderCallback callback) {
		this.model =model;
		this.threadPool = threadPool;
		this.callback = callback;
	}

	protected DuplicateLengthFinder(final Cluster<Long,File> model, DuplicateLengthFinderCallback callback){
		this.model = model;
		this.threadPool = Executors.newWorkStealingPool();
		this.callback = callback;
	}

	/**
	 * Einstiegstmethode zum Durchsuchen eines Verzeichnisses nach Dateien
	 * gleicher Größe.
	 *
	 * @param folder
	 *            zu durchsuchendes Verzeichnis, muss existieren und lesbar sein
	 *
	 * @return Liefert eine Map nach Dateigröße strukturierten Queues zurück, in
	 *         denen die gefundenen Dateien abgelegt sind
	 */
	public void readFilesRecursiveOf(final File folder) {
		if (folder == null) {
			throw new IllegalArgumentException("folder may not be null.");
		}

		if (threadPool == null) {
			throw new IllegalArgumentException("threadPool may not be null.");
		}

		this.execute(folder);
	}


	private String checkFolder(final File folder) {
		String exceptionMessage = null;
		if (folder.list() == null) {
			exceptionMessage = "FEHLER: Parameter <Verzeichnis> kann nicht aufgelistet werden: ";
		}
		if (!folder.canRead()) {
			exceptionMessage = "FEHLER: Parameter <Verzeichnis> ist nicht lesbar: ";
		}
		if (!folder.isDirectory()) {
			exceptionMessage = "FEHLER: Parameter <Verzeichnis> ist kein Verzeichnis: ";
		}
		if (!folder.exists()) {
			exceptionMessage = "FEHLER: Parameter <Verzeichnis> existiert nicht: ";
		}
		return exceptionMessage;
	}

	protected Cluster<Long, File> execute( final File folder) {
		futures.add(threadPool.submit(new DuplicateLengthRunner(folder)));

		while (!futures.isEmpty()) {
			try {
				futures.remove().get();
			} catch (InterruptedException | ExecutionException e) {
				throw new IllegalStateException("Threading has failed: " + e.getMessage(), e);
			}
		}
		return model;
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
				if (callback != null) {
					callback.unreadableFolder(folder);
				}
				return;
			}

			for (String fileName : contents) {
				File file = new File(folder.getAbsolutePath() + System.getProperty("file.separator") + fileName);

				if (file.isDirectory()) {
					String exceptionMessage = checkFolder(folder);
					if (exceptionMessage == null) {
						futures.add(threadPool.submit(new DuplicateLengthRunner(file)));
						if (callback != null) {
							callback.enteredNewFolder(file);
						}
					} else {
						if (callback != null) {
							callback.unreadableFolder(file);
						}
					}
				}

				if (file.isFile()) {
					model.addGroupedElement(Long.valueOf(file.length()), file);
				}
			}
			return;
		}
	}

}
