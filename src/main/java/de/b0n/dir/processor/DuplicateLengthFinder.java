package de.b0n.dir.processor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
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

	/**
	 * Bereitet für das gegebene Verzeichnis die Suche nach gleich großen Dateien vor. 
	 * @param threadPool Pool zur Ausführung der Suchen
	 * @param folder zu durchsuchendes Verzeichnis, muss existieren und lesbar sein
	 */
	private DuplicateLengthFinder(final ExecutorService threadPool, final File folder) {
		if (!folder.exists()) {
			throw new IllegalArgumentException("Not existing path: " + folder.getAbsolutePath());
		}
		if (!folder.isDirectory()) {
			throw new IllegalArgumentException("Path is not a directory: " + folder.getAbsolutePath());
		}
		if (!folder.canRead()) {
			throw new IllegalArgumentException("Cannot read path: " + folder.getAbsolutePath());
		}

		this.threadPool = threadPool;
		this.folder = folder;
	}

	/**
	 * Iteriert durch die Elemente im Verzeichnis und legt neue Suchen für Verzeichnisse an.
	 * Dateien werden sofort der Größe nach abgelegt.
	 * Wartet die Unterverzeichnis-Suchen ab und merged deren Ergebnisdateien.
	 * Liefert das Gesamtergebnis zurück.
	 */
	@Override
	public Map<Long, Queue<File>> call() throws Exception {
		Map<Long, Queue<File>> filesizeMap = new HashMap<Long, Queue<File>>();
		Queue<Future<Map<Long, Queue<File>>>> futures = new ConcurrentLinkedQueue<Future<Map<Long, Queue<File>>>>();

		for (String fileName : folder.list()) {
			File file = new File(folder.getAbsolutePath() + System.getProperty("file.separator") + fileName);
			if (file.isDirectory()) {
				try {
					futures.add(threadPool.submit(new DuplicateLengthFinder(threadPool, file)));
				} catch (IllegalArgumentException e) {
					// Given Folder is invalid, continue with next
				}
			}

			if (file.isFile()) {
				addFileBySize(filesizeMap, file);
			}
		}

		for (Future<Map<Long, Queue<File>>> futureFilesizes : futures) {
			try {
				mergeFilesBySize(filesizeMap, futureFilesizes.get());
			} catch (InterruptedException | ExecutionException e) {
				// This is a major problem, notify user and try to recover
				e.printStackTrace();
			}
		}
		return filesizeMap;
	}

	/**
	 * Fügt eine Datei ihrer Größe entsprechend in einer passenden Queue der Map ein.
	 * @param filesizeMap Map mit allen Dateigrößen als Queue welche die Dateien enthalten
	 * @param file Datei zum hinzufügen zu einer passenden Queue
	 * @return Liefert die eingelieferte Map zurück
	 */
	private Map<Long, Queue<File>> addFileBySize(Map<Long, Queue<File>> filesizeMap, File file) {
		long fileSize = file.length();
		Queue<File> filesOneSize = filesizeMap.get(fileSize);
		if (filesOneSize == null) {
			filesOneSize = new ConcurrentLinkedQueue<File>();
			filesizeMap.put(fileSize, filesOneSize);
		}
		filesOneSize.add(file);

		return filesizeMap;
	}

	/**
	 * Merged die Dateien in den Queues der beiden übergebenen Maps nach Größe.
	 * @param filesizeMap Bestehende Map, zu der die Elemente hinzugefügt werden
	 * @param additionalFilesizeMap Zusätzliche Map, deren Elemente zur bestehenden Map hinzugefügt werden
	 * @return Liefert die eingelieferte bestehende Map mit den neuen Elementen zurück
	 */
	private Map<Long, Queue<File>> mergeFilesBySize(Map<Long, Queue<File>> filesizeMap,
			Map<Long, Queue<File>> additionalFilesizeMap) {
		for (Long filesize : additionalFilesizeMap.keySet()) {
			Queue<File> additionalFiles = additionalFilesizeMap.get(filesize);
			Queue<File> localFiles = filesizeMap.get(filesize);
			if (localFiles == null) {
				filesizeMap.put(filesize, additionalFiles);
			} else {
				localFiles.addAll(additionalFiles);
			}
		}
		return filesizeMap;
	}

	/**
	 * Filtert Queues mit nur einer Datei heraus. Diese können keine Dubletten sein.
	 * @param filesizeMap Map mit allen Dateigrößen als Queue welche die Dateien enthalten
	 * @return Liefert die eingelieferte bestehende Map ohne Einzelelemente zurück
	 */
	private static Queue<Queue<File>> filterUniqueSizes(Map<Long, Queue<File>> filesizeMap) {
		Queue<Queue<File>> filesQueues = new ConcurrentLinkedQueue<Queue<File>>();
		for (Long size : filesizeMap.keySet()) {
			Queue<File> files = filesizeMap.get(size);
			if (files.size() > 1) {
				filesQueues.add(files);
			}
		}
		return filesQueues;
	}

	/**
	 * Einstiegstmethode zum Durchsuchen eines Verzeichnisses nach Dateien gleicher Größe.
	 * @param threadPool Pool zur Ausführung der Suchen
	 * @param folder Zu durchsuchendes Verzeichnis
	 * @return Liefert eine Map nach Dateigröße strukturierten Queues zurück, in denen die gefundenen Dateien abgelegt sind 
	 */
	public static Queue<Queue<File>> getResult(final ExecutorService threadPool, final File folder) {
		Future<Map<Long, Queue<File>>> future = threadPool.submit(new DuplicateLengthFinder(threadPool, folder));

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
