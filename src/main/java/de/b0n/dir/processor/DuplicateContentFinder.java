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

			while (inputFileReaders != null && !inputFileReaders.isEmpty()) {
				sortedFiles = sortFilesByByte(inputFileReaders);

			// Failing Streams
			Queue<FileReader> failingFiles = sortedFiles.removeGroup(FileReader.FAILING);
			if (failingFiles != null) {
				// Already closed when error has been recognized
				callback.failedFiles(FileReader.extract(failingFiles));
			}

			// Unique Files
			Queue<FileReader> uniqueFiles = sortedFiles.removeUniques();
			if (uniqueFiles != null) {
				FileReader.closeAll(uniqueFiles);
				callback.uniqueFiles(FileReader.extract(uniqueFiles));
			}

			// Finished Streams
			Queue<FileReader> finishedFiles = sortedFiles.removeGroup(FileReader.FINISHED);
			if (finishedFiles != null) {
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
		consolidate(futures);
	}

	/**
	 * Liest aus allen gegebenen FileReaders ein Byte und sortiert die
	 * FileReader nach Ergebnis. Dadurch werden alle nicht-Dubletten bezüglich
	 * dieses Bytes in unterschiedliche Gruppen sortiert. Ebenso werden alle
	 * vollständig gelesenen Dateien in eine eigene Gruppe sortiert
	 * FINISHED(-1). Dateien, welche nicht (mehr) gelesen werden können, fallen
	 * in die Kategrorie FAILED(-2). Die restlichen FileReader landen in den
	 * Gruppen des jeweils gelesenen Bytes.
	 * 
	 * @param inputFileStreams
	 *            zu sortierende FileStreams
	 * @return Cluster mit den nach Ergebnis sortierten FileStreams
	 */
	private Cluster<Integer, FileReader> sortFilesByByte(Collection<FileReader> inputFileReaders) {
		Cluster<Integer, FileReader> sortedFiles = new Cluster<Integer, FileReader>();
		for (FileReader sortFile : inputFileReaders) {
			sortedFiles.addGroupedElement(sortFile.read(), sortFile);
		}
		return sortedFiles;
	}

	/**
	 * Ermittelt anhand der optional nach Dateigröße vorgruppierten Files
	 * inhaltliche Dubletten.
	 * 
	 * @param input
	 *            Dateigruppen, welche auf inhaltliche Gleichheit geprüft werden
	 *            sollen
	 * @return Nach inhaltlichen Dubletten gruppierte File-Listen
	 */
	public static Queue<Queue<File>> getResult(final Queue<File> input) {
		ConcurrentLinkedQueue<Queue<File>> result = new ConcurrentLinkedQueue<Queue<File>>();
		DuplicateContentFinderCallback callback = new AbstractDuplicateContentFinderCallback() {

			@Override
			public void duplicateGroup(Queue<File> duplicateGroup) {
				result.add(duplicateGroup);
			}
		};

		getResult(input, callback);

		return result;
	}

	/**
	 * Ermittelt anhand der optional nach Dateigröße vorgruppierten Files
	 * inhaltliche Dubletten.
	 * 
	 * @param input
	 *            Dateigruppen, welche auf inhaltliche Gleichheit geprüft werden
	 *            sollen
	 * @param callback
	 *            Callback, um über die Ergebnisse der Dublettensuche informiert
	 *            zu werden
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