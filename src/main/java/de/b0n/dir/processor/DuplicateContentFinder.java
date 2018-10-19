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

	private Collection<FileReader> currentCandidates;

	private final DuplicateContentFinderCallback callback;
	private final List<Future<?>> futures = new ArrayList<>();

	public DuplicateContentFinder(final Collection<FileReader> files, final DuplicateContentFinderCallback callback) {
		this.currentCandidates = files;
		this.callback = callback;
	}

	@Override
	public void run() {
		Cluster<Integer, FileReader> sortedFiles = null;

		while (currentCandidates != null) {
			sortedFiles = sortFilesByByte(currentCandidates);

			// Failing Streams
			Queue<FileReader> failedFiles = sortedFiles.removeGroup(FileReader.FAILING);
			if (failedFiles != null) {
				if (failedFiles.isEmpty()) {
					throw new IllegalStateException("Empty group of failed files occurred");
				}
				// Already closed when error has been recognized
				callback.failedFiles(FileReader.extract(failedFiles));
			}

			// Unique Files
			Queue<FileReader> uniques = sortedFiles.removeUniques();
			if (uniques != null) {
				if (uniques.isEmpty()) {
					throw new IllegalStateException("Empty group of uniques occurred");
				}
				FileReader.closeAll(uniques);
				callback.uniqueFiles(FileReader.extract(uniques));
			}

			// Finished Streams
			Queue<FileReader> duplicates = sortedFiles.removeGroup(FileReader.FINISHED);
			if (duplicates != null) {
				if (duplicates.size() < 2) {
					throw new IllegalStateException("Single or empty duplicates occurred: " + FileReader.getContentDescription(duplicates));
				}
				FileReader.closeAll(duplicates);
				callback.duplicateGroup(FileReader.extract(duplicates));
			}

			// Prepare first group for next iteration
			currentCandidates = sortedFiles.popGroup();
			if (currentCandidates != null && currentCandidates.size() < 2) {
				throw new IllegalStateException("Single or empty candidates occurred: " + FileReader.getContentDescription(currentCandidates));
			}

			// Outsource other groups
			for (Queue<FileReader> outsourcedCandidates : sortedFiles.values()) {
				if (outsourcedCandidates.size() < 2) {
					throw new IllegalStateException("Single or empty candidates occurred: " + FileReader.getContentDescription(outsourcedCandidates));
				}
				futures.add(submit(new DuplicateContentFinder(outsourcedCandidates, callback)));
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