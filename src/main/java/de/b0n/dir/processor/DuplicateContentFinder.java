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

	private Collection<FileStream> inputFileStreams;
	private final DuplicateContentFinderCallback callback;
	private final List<Future<?>> futures = new ArrayList<>();

	private static final Integer FINISHED = Integer.valueOf(-1);
	private static final Integer FAILING = Integer.valueOf(-2);

	public DuplicateContentFinder(final Collection<FileStream> files, final DuplicateContentFinderCallback callback) {
		this.inputFileStreams = files;
		this.callback = callback;
	}
	
	@Override
	public void run() {
		Cluster<Integer, FileStream> sortedFiles = null;
		
		try {
			while (inputFileStreams != null && !inputFileStreams.isEmpty()) {
				sortedFiles = sortFilesByByte(inputFileStreams);
				
				// Failing Streams
				if (sortedFiles.containsGroup(FAILING)) {
					callback.failedFiles(sortedFiles.getGroup(FAILING).size());
					FileStream.closeAll(sortedFiles.removeGroup(FAILING));
				}
				
				// Unique Streams
				int uniqueFiles = sortedFiles.removeUniques().size();
				if (uniqueFiles > 0) {
					callback.uniqueFiles(uniqueFiles);
				}

				// Finished Streams
				Queue<FileStream> finishedFiles = null;
				if (sortedFiles.containsGroup(FINISHED)) {
					finishedFiles = sortedFiles.removeGroup(FINISHED);
					FileStream.closeAll(finishedFiles);
					callback.duplicateGroup(FileStream.extract(finishedFiles));
				}
				
				// Prepare first group for next iteration
				inputFileStreams = sortedFiles.popGroup();
				
				// Outsource other groups
				for (Queue<FileStream> fileStreams : sortedFiles.values()) {
					futures.add(submit(new DuplicateContentFinder(fileStreams, callback)));
				}
			}
		} catch(Exception e) {
			FileStream.closeAll(inputFileStreams);
			if (sortedFiles != null) {
				for (Collection<FileStream> fileStreams : sortedFiles.values()) {
					FileStream.closeAll(fileStreams);
				}
			}
			throw e;
		} finally {
			consolidate(futures);
		}
	}		

	/**
	 * Liest aus allen gegebenen FileStreams ein Byte und sortiert die FileStreams nach Ergebnis.
	 * Dadurch werden alle nicht-Dubletten bezüglich dieses Bytes in unterschiedliche Gruppen sortiert.
	 * Ebenso werden alle vollständig gelesenen Dateien in eine eigene Gruppe sortiert INPUT(-1).
	 * Dateien, welche nicht (mehr) gelesen werden können, fallen in die Kategrorie FAILED(-2).
	 * Die restlichen FileStreams landen in den Gruppen des jeweils gelesenen Bytes.
	 * @param inputFileStreams zu sortierende FileStreams
	 * @return Cluster mit den nach Ergebnis sortierten FileStreams
	 */
	private Cluster<Integer, FileStream> sortFilesByByte(Collection<FileStream> inputFileStreams) {
		Cluster<Integer, FileStream> sortedFiles = new Cluster<Integer, FileStream>();
		for (FileStream sortFile : inputFileStreams) {
			try {
				sortedFiles.addGroupedElement(sortFile.read(), sortFile);
			} catch (IllegalStateException e) {
				System.out.println(e.getMessage());
				sortedFiles.addGroupedElement(FAILING, sortFile);
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

		consolidate(submit(new DuplicateContentFinder(FileStream.pack(input), callback)));
	}
}