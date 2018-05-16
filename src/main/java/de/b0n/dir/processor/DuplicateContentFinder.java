package de.b0n.dir.processor;

import java.io.File;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class DuplicateContentFinder {
	private final Collection<Queue<File>> input;
	private final ExecutorService threadPool;
	private final DuplicateContentFinderCallback callback;

	private final Queue<Queue<File>> result;
	private final Queue<Future<?>> futures;

	public DuplicateContentFinder(final Collection<Queue<File>> input, final ExecutorService threadPool, final DuplicateContentFinderCallback callback) {
		this.input = input;
		this.threadPool = threadPool;
		this.callback = callback;

		result = new ConcurrentLinkedQueue<Queue<File>>();
		futures = new ConcurrentLinkedQueue<Future<?>>();

	}
	
	private Queue<Queue<File>> execute() {
		for (Queue<File> files : input) {
			futures.add(threadPool.submit(new DuplicateContentRunner(FileReader.pack(files))));
		}

		while (!futures.isEmpty()) {
			try {
				futures.remove().get();
			} catch (InterruptedException | ExecutionException e) {
				throw new IllegalStateException("Threading has failes: " + e.getMessage(), e);
			}
		}
		return result;
	}
	
	private class DuplicateContentRunner implements Runnable {
		private Collection<FileReader> inputFileReader;

		private DuplicateContentRunner(Collection<FileReader> inputFileReaders) {
			this.inputFileReader = inputFileReaders;
		}
	
		@Override
		public void run() {
			Cluster<Integer, FileReader> sortedFiles = null;
			
			try {
				while (inputFileReader != null && !inputFileReader.isEmpty()) {
					sortedFiles = sortFilesByByte(inputFileReader);
					
					// Failing Streams
					if (sortedFiles.containsGroup(FileReader.FAILING)) {
						if (callback != null) {
							callback.failedFiles(sortedFiles.getGroup(FileReader.FAILING).size());
						}
						FileReader.closeAll(sortedFiles.removeGroup(FileReader.FAILING));
					}
					
					// Unique Streams
					int uniqueFiles = sortedFiles.removeUniques().size();
					if (callback != null && uniqueFiles > 0) {
						callback.uniqueFiles(uniqueFiles);
					}
	
					// Finished Streams
					Queue<FileReader> finishedFiles = null;
					if (sortedFiles.containsGroup(FileReader.FINISHED)) {
						finishedFiles = sortedFiles.removeGroup(FileReader.FINISHED);
						result.add(FileReader.extract(finishedFiles));
						FileReader.closeAll(finishedFiles);
						if (callback != null) {
							callback.duplicateGroup(FileReader.extract(finishedFiles));
						}
					}
					
					// Prepare for next iteration
					inputFileReader = sortedFiles.popGroup();
					
					// Outsource other groups
					for (Queue<FileReader> FileReaders : sortedFiles.values()) {
						futures.add(threadPool.submit(new DuplicateContentRunner(FileReaders)));
					}
				}
			} catch(Exception e) {
				FileReader.closeAll(inputFileReader);
				if (sortedFiles != null) {
					for (Collection<FileReader> FileReaders : sortedFiles.values()) {
						FileReader.closeAll(FileReaders);
					}
				}
				throw e;
			}
		}		
	}

	/**
	 * Liest aus allen gegebenen FileReaders ein Byte und sortiert die FileReaders nach Ergebnis.
	 * Dadurch werden alle nicht-Dubletten bezüglich dieses Bytes in unterschiedliche Gruppen sortiert.
	 * Ebenso werden alle vollständig gelesenen Dateien in eine eigene Gruppe sortiert INPUT(-1).
	 * Dateien, welche nicht (mehr) gelesen werden können, fallen in die Kategrorie FAILED(-2).
	 * Die restlichen FileReaders landen in den Gruppen des jeweils gelesenen Bytes.
	 * @param inputFileReaders zu sortierende FileReaders
	 * @return Cluster mit den nach Ergebnis sortierten FileReaders
	 */
	private Cluster<Integer, FileReader> sortFilesByByte(Collection<FileReader> inputFileReaders) {
		Cluster<Integer, FileReader> sortedFiles = new Cluster<Integer, FileReader>();
		for (FileReader sortFile : inputFileReaders) {
			try {
				sortedFiles.addGroupedElement(sortFile.read(), sortFile);
			} catch (IllegalStateException e) {
				System.out.println(e.getMessage());
				sortedFiles.addGroupedElement(FileReader.FAILING, sortFile);
			}								
		}
		return sortedFiles;
	}

	/**
	 * Ermittelt anhand der optional nach Dateigröße vorgruppierten Files inhaltliche Dubletten.
	 * Der ThreadPool, wenn noch nicht im Programm verwendet, kann mit Executors.newWorkStealingPool(); instantiiert werden.
	 * @param input Dateigruppen, welche auf Inhaltliche gleichheit geprüft werden sollen
	 * @param threadPool ExecutorService zur Steuerung der Parallelisierung
	 * @return Nach inhaltlichen Dubletten gruppierte File-Listen
	 */
	public static Queue<Queue<File>> getResult(final Collection<Queue<File>> input, final ExecutorService threadPool) {
		return getResult(input, threadPool, null);
	}

	/**
	 * Ermittelt anhand der optional nach Dateigröße vorgruppierten Files inhaltliche Dubletten.
	 * Der ThreadPool, wenn noch nicht im Programm verwendet, kann mit Executors.newWorkStealingPool(); instantiiert werden.
	 * @param input Dateigruppen, welche auf Inhaltliche gleichheit geprüft werden sollen
	 * @param threadPool ExecutorService zur Steuerung der Parallelisierung
	 * @param callback Optionaler Callback, um über den Fortschritt der Dublettensuche informiert zu werden
	 * @return Nach inhaltlichen Dubletten gruppierte File-Listen
	 */
	public static Queue<Queue<File>> getResult(final Collection<Queue<File>> input, final ExecutorService threadPool, final DuplicateContentFinderCallback callback) {
		if (threadPool == null) {
			throw new IllegalArgumentException("threadPool may not be null.");
		}
		if (input == null) {
			throw new IllegalArgumentException("input may not be null.");
		}
	
		return new DuplicateContentFinder(input, threadPool, callback).execute();
	}
}