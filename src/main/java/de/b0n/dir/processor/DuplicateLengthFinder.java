package de.b0n.dir.processor;

import java.io.File;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Sucht in einem gegebenen Verzeichnis und dessen Unterverzeichnissen nach
 * Dateien und sortiert diese nach Dateigröße.
 * 
 * @author Claus
 *
 */
public class DuplicateLengthFinder implements Callable<Map<Long, Queue<File>>> {

	private final ExecutorService threadPool;
	private final File folder;
	Map<Long, Queue<File>> result;

	/**
	 * Bereitet für das gegebene Verzeichnis die Suche nach gleich großen Dateien vor. 
	 * @param threadPool Pool zur Ausführung der Suchen
	 * @param folder zu durchsuchendes Verzeichnis, muss existieren und lesbar sein
	 */
	private DuplicateLengthFinder(final ExecutorService threadPool, final File folder, Map<Long, Queue<File>> result) {
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
	}

	/**
	 * Iteriert durch die Elemente im Verzeichnis und legt neue Suchen für Verzeichnisse an.
	 * Dateien werden sofort der Größe nach abgelegt.
	 * Wartet die Unterverzeichnis-Suchen ab und merged deren Ergebnisdateien.
	 * Liefert das Gesamtergebnis zurück.
	 */
	@Override
	public Map<Long, Queue<File>> call() throws Exception {
		Queue<Future<Map<Long, Queue<File>>>> futures = new ConcurrentLinkedQueue<Future<Map<Long, Queue<File>>>>();

		if (folder.list() == null) {
			System.out.println("Could not read content of folder: " + folder.getAbsolutePath());
			return result;
		}

		for (String fileName : folder.list()) {
			File file = new File(folder.getAbsolutePath() + System.getProperty("file.separator") + fileName);

			if (file.isDirectory()) {
				try {
					futures.add(threadPool.submit(new DuplicateLengthFinder(threadPool, file, result)));
				} catch (IllegalArgumentException e) {
					System.err.println("Given Folder is invalid, continue with next: " + file.getAbsolutePath());
					continue;
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

		return result;
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
	 * @param threadPool Pool zur Ausführung der Suchen
	 * @param folder Zu durchsuchendes Verzeichnis
	 * @return Liefert eine Map nach Dateigröße strukturierten Queues zurück, in denen die gefundenen Dateien abgelegt sind 
	 */
	public static Map<Long, Queue<File>> getResult(final ExecutorService threadPool, final File folder) {
		return getResult(threadPool, folder, null);
	}

	/**
	 * Einstiegstmethode zum Durchsuchen eines Verzeichnisses nach Dateien gleicher Größe.
	 * @param threadPool Pool zur Ausführung der Suchen
	 * @param folder Zu durchsuchendes Verzeichnis
	 * @return Liefert eine Map nach Dateigröße strukturierten Queues zurück, in denen die gefundenen Dateien abgelegt sind 
	 */
	public static Map<Long, Queue<File>> getResult(final ExecutorService threadPool, final File folder, Map<Long, Queue<File>> result) {
		if (threadPool == null) {
			throw new IllegalArgumentException("threadPool may not be null.");
		}
		if (folder == null) {
			throw new IllegalArgumentException("folder may not be null.");
		}

		if (result == null) {
			result = new ConcurrentHashMap<Long, Queue<File>>();
		}

		Future<Map<Long, Queue<File>>> future = threadPool.submit(new DuplicateLengthFinder(threadPool, folder, result));

		Map<Long, Queue<File>> filesizeMap = null;
		try {
			filesizeMap = future.get();
		} catch (InterruptedException | ExecutionException e) {
			// This is a critical problem, nothing to recover, abort
			throw new IllegalStateException("Unrecoverable problem, aborting file search", e);
		}

		return filterUniqueSizes(filesizeMap);
	}
}
