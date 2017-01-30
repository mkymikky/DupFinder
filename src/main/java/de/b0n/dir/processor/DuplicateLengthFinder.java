package de.b0n.dir.processor;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
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
public class DuplicateLengthFinder implements Runnable {

	private final ExecutorService threadPool;
	private final File folder;
	Map<Long, Queue<File>> result;
	private final DuplicateLengthFinderCallback callback;

	/**
	 * Bereitet für das gegebene Verzeichnis die Suche nach gleich großen Dateien vor. 
	 * @param threadPool Pool zur Ausführung der Suchen
	 * @param folder zu durchsuchendes Verzeichnis, muss existieren und lesbar sein
	 */
	private DuplicateLengthFinder(final File folder, final ExecutorService threadPool, Map<Long, Queue<File>> result, DuplicateLengthFinderCallback callback) {
		if (!folder.exists()) {
			throw new IllegalArgumentException("FEHLER: Parameter <Verzeichnis> existiert nicht: " + folder.getAbsolutePath());
		}
		if (!folder.isDirectory()) {
			throw new IllegalArgumentException("FEHLER: Parameter <Verzeichnis> ist kein Verzeichnis: " + folder.getAbsolutePath());
		}
		if (!folder.canRead()) {
			throw new IllegalArgumentException("FEHLER: Parameter <Verzeichnis> ist nicht lesbar: " + folder.getAbsolutePath());
		}

		this.threadPool = threadPool;
		this.folder = folder;
		this.result = result;
		this.callback = callback;
	}

	/**
	 * Iteriert durch die Elemente im Verzeichnis und legt neue Suchen für Verzeichnisse an.
	 * Dateien werden sofort der Größe nach abgelegt.
	 * Wartet die Unterverzeichnis-Suchen ab und merged deren Ergebnisdateien.
	 * Liefert das Gesamtergebnis zurück.
	 */
	@Override
	public void run() {
		Queue<Future<?>> futures = new ConcurrentLinkedQueue<Future<?>>();

		if (folder.list() == null) {
			System.out.println("Could not read content of folder: " + folder.getAbsolutePath());
			return;
		}

		for (String fileName : folder.list()) {
			File file = new File(folder.getAbsolutePath() + System.getProperty("file.separator") + fileName);

			if (file.isDirectory()) {
				try {
					futures.add(threadPool.submit(new DuplicateLengthFinder(file, threadPool, result, callback)));
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
				addFileBySize(result, file);
			}
		}

		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				// This is a major problem, notify user and try to recover
				e.printStackTrace();
			}
		}

		return;
	}

	/**
	 * Fügt eine Datei ihrer Größe entsprechend in einer passenden Queue der Map ein.
	 * @param filesizeMap Map mit allen Dateigrößen als Queue welche die Dateien enthalten
	 * @param file Datei zum hinzufügen zu einer passenden Queue
	 * @return Liefert die eingelieferte Map zurück
	 */
	private Map<Long, Queue<File>> addFileBySize(Map<Long, Queue<File>> filesizeMap, File file) {
		if (filesizeMap == null) {
			throw new IllegalArgumentException("filesizeMap darf nicht null sein.");
		}
		if (file == null) {
			throw new IllegalArgumentException("file darf nicht null sein.");
		}
		
		long fileSize = file.length();
		Queue<File> filesOneSize = filesizeMap.get(fileSize);
		if (filesOneSize == null) {
			filesOneSize = insertQueueToMap(filesizeMap, fileSize);
		}
		filesOneSize.add(file);

		return filesizeMap;
	}

	/**
	 * Erstellt bei Bedarf synchronisiert eine neue Queue für den fehlenden Schlüssel der Map.
	 * @param map Die Map, welche um einen evtl. fehlenden Schlüssel ergänzt wird
	 * @param key Schlüssel, für den die Queue erstellt wird
	 * @return Queue, welche in der Map zu dem Schlüssel existiert oder erstellt wurde
	 */
	private Queue<File> insertQueueToMap(Map<Long, Queue<File>> map, long key) {
		Queue<File> queueOfKey;
		synchronized (map) {
			queueOfKey = map.get(key);
			if (queueOfKey == null) {
				queueOfKey = new ConcurrentLinkedQueue<File>();
				map.put(key, queueOfKey);
			}
		}
		return queueOfKey;
	}

	/**
	 * Filtert Queues mit nur einer Datei heraus. Diese können keine Dubletten sein.
	 * @param filesizeMap Map mit allen Dateigrößen als Queue welche die Dateien enthalten
	 * @return Liefert die eingelieferte bestehende Map ohne Einzelelemente zurück
	 */
	private static Map<Long, Queue<File>> filterUniqueSizes(Map<Long, Queue<File>> filesizeMap) {
		for (Long size : filesizeMap.keySet()) {
			Queue<File> files = filesizeMap.get(size);
			if (files.size() <= 1) {
				filesizeMap.remove(size);
			}
		}
		return filesizeMap;
	}

	/**
	 * Einstiegstmethode zum Durchsuchen eines Verzeichnisses nach Dateien gleicher Größe.
	 * Verwendet einen Executors.newWorkStealingPool() als ThreadPool.
	 * @param folder Zu durchsuchendes Verzeichnis
	 * @return Liefert eine Map nach Dateigröße strukturierten Queues zurück, in denen die gefundenen Dateien abgelegt sind 
	 */
	public static Map<Long, Queue<File>> getResult(final File folder) {
		return getResult(folder, Executors.newWorkStealingPool(), null);
	}

	/**
	 * Einstiegstmethode zum Durchsuchen eines Verzeichnisses nach Dateien gleicher Größe.
	 * @param folder Zu durchsuchendes Verzeichnis
	 * @param threadPool Pool zur Ausführung der Suchen
	 * @return Liefert eine Map nach Dateigröße strukturierten Queues zurück, in denen die gefundenen Dateien abgelegt sind 
	 */
	public static Map<Long, Queue<File>> getResult(final File folder, final ExecutorService threadPool) {
		return getResult(folder, threadPool, null);
	}

	/**
	 * Einstiegstmethode zum Durchsuchen eines Verzeichnisses nach Dateien gleicher Größe.
	 * Verwendet einen Executors.newWorkStealingPool() als ThreadPool.
	 * @param folder Zu durchsuchendes Verzeichnis
	 * @param callback Ruft den Callback bei jedem neu betretenen Verzeichnis auf
	 * @return Liefert eine Map nach Dateigröße strukturierten Queues zurück, in denen die gefundenen Dateien abgelegt sind 
	 */
	public static Map<Long, Queue<File>> getResult(final File folder, DuplicateLengthFinderCallback callback) {
		return getResult(folder, Executors.newWorkStealingPool(), null);
	}

	/**
	 * Einstiegstmethode zum Durchsuchen eines Verzeichnisses nach Dateien gleicher Größe.
	 * @param folder Zu durchsuchendes Verzeichnis
	 * @param threadPool Pool zur Ausführung der Suchen
	 * @param callback Ruft den Callback bei jedem neu betretenen Verzeichnis auf
	 * @return Liefert eine Map nach Dateigröße strukturierten Queues zurück, in denen die gefundenen Dateien abgelegt sind 
	 */
	public static Map<Long, Queue<File>> getResult(final File folder, final ExecutorService threadPool, DuplicateLengthFinderCallback callback) {
		if (folder == null) {
			throw new IllegalArgumentException("folder may not be null.");
		}

		if (threadPool == null) {
			throw new IllegalArgumentException("threadPool may not be null.");
		}
		ConcurrentHashMap<Long, Queue<File>> result = new ConcurrentHashMap<Long, Queue<File>>();

		Future<?> future = threadPool.submit(new DuplicateLengthFinder(folder, threadPool, result, callback));

		try {
			future.get();
		} catch (InterruptedException | ExecutionException e) {
			// This is a critical problem, nothing to recover, abort
			throw new IllegalStateException("Unrecoverable problem, aborting file search", e);
		}

		return filterUniqueSizes(result);
	}
}
