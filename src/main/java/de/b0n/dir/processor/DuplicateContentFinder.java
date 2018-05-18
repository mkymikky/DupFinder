package de.b0n.dir.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

/**
 * Sucht von gegeben Dateigruppen die inhaltlichen Duplikate.
 */
public class DuplicateContentFinder extends AbstractProcessor implements Runnable {

	private Collection<FileReader> inputFileReaders;

	private final DuplicateContentFinderCallback callback;
	private final List<Future<?>> futures = new ArrayList<>();

	public DuplicateContentFinder(final Collection<FileReader> files, final DuplicateContentFinderCallback callback) {
		this.inputFileReaders = files;
		this.callback = callback;
	}
	
	@Override
	public void run() {
		Cluster<Integer, FileReader> sortedFiles = null;
		
		try {
			while (inputFileReaders != null && !inputFileReaders.isEmpty()) {
				sortedFiles = sortFilesByByte(inputFileReaders);
				
				// Failing Streams
				if (sortedFiles.containsGroup(FileReader.FAILING)) {
					callback.failedFiles(sortedFiles.getGroup(FileReader.FAILING).size());
					FileReader.closeAll(sortedFiles.removeGroup(FileReader.FAILING));
				}
				
				// Unique Streams
				int uniqueFiles = sortedFiles.removeUniques().size();
				if (uniqueFiles > 0) {
					callback.uniqueFiles(uniqueFiles);
				}

				// Finished Streams
				Queue<FileReader> finishedFiles = null;
				if (sortedFiles.containsGroup(FileReader.FINISHED)) {
					finishedFiles = sortedFiles.removeGroup(FileReader.FINISHED);
					FileReader.closeAll(finishedFiles);
					callback.duplicateGroup(FileReader.extract(finishedFiles));
				}
				
				// Prepare first group for next iteration
				inputFileReaders = sortedFiles.popGroup();
				
				// Outsource other groups
				for (Queue<FileReader> FileReaders : sortedFiles.values()) {
					futures.add(submit(new DuplicateContentFinder(FileReaders, callback)));
				}
			}
		} catch(Exception e) {
			FileReader.closeAll(inputFileReaders);
			if (sortedFiles != null) {
				for (Collection<FileReader> FileReaders : sortedFiles.values()) {
					FileReader.closeAll(FileReaders);
				}
			}
			throw e;
		} finally {
			consolidate(futures);
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
	 * @param input Dateigruppen, welche auf Inhaltliche gleichheit geprüft werden sollen
	 * @return Nach inhaltlichen Dubletten gruppierte File-Listen
	 */
	public static Queue<Queue<File>> getResult(final Queue<File> input) {
		ConcurrentLinkedQueue<Queue<File>> result = new ConcurrentLinkedQueue<Queue<File>>();
		DuplicateContentFinderCallback callback = new DuplicateContentFinderCallback() {
			
			@Override
			public void uniqueFiles(int uniqueFileCount) {
				return;
			}
			
			@Override
			public void failedFiles(int size) {
				return;
			}
			
			@Override
			public void duplicateGroup(Queue<File> duplicateGroup) {
				result.add(duplicateGroup);
			}
		};
		
		getResult(input, callback);
		
		return result;
	}

	/**
	 * Ermittelt anhand der optional nach Dateigröße vorgruppierten Files inhaltliche Dubletten.
	 * @param input Dateigruppen, welche auf Inhaltliche gleichheit geprüft werden sollen
	 * @param callback Callback, um über die Ergebnisse der Dublettensuche informiert zu werden
	 */
	public static void getResult(final Queue<File> input, final DuplicateContentFinderCallback callback) {
		if (input == null) {
			throw new IllegalArgumentException("input may not be null.");
		}
		if (callback == null) {
			throw new IllegalArgumentException("callback may not be null.");
		}

		consolidate(submit(new DuplicateContentFinder(FileReader.pack(input), callback)));
	}
}