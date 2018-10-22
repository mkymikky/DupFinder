package de.b0n.dir.processor;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Sucht von gegeben Dateigruppen die inhaltlichen Duplikate.
 */
public class DuplicateContentFinder implements Runnable {

	private static final Function<FileReader, Integer> fileReaderToInt = fileReader -> fileReader.read();
	private static final Function<FileReader, File> fileReaderToFile = fileReader -> fileReader.clear();
	private static final Function<Collection<FileReader>, Stream<FileReader>> collection = collection -> collection
			.parallelStream();
	private static final Function<Entry<Integer, List<FileReader>>, List<FileReader>> entryToValue = entry -> entry
			.getValue();
	private static final Predicate<Entry<Integer, List<FileReader>>> hasSingleItemInEntry = entry -> entry.getValue()
			.size() < 2;

	private Collection<FileReader> currentCandidates;

	private final DuplicateContentFinderCallback callback;
	private final Executor executor;

	public DuplicateContentFinder(final Collection<FileReader> files, final DuplicateContentFinderCallback callback,
			Executor executor) {
		this.currentCandidates = files;
		this.callback = callback;
		this.executor = executor;
	}

	@Override
	public void run() {
		while (currentCandidates != null) {
			Map<Integer, List<FileReader>> sortedFiles = currentCandidates.parallelStream()
					.collect(Collectors.groupingByConcurrent(fileReaderToInt));
			currentCandidates = null;

			// Failed Files
			List<FileReader> failingFiles = sortedFiles.remove(FileReader.FAILING);
			if (failingFiles != null) {
				failingFiles.parallelStream().map(fileReaderToFile).forEach(file -> callback.failedFile(file));
				failingFiles = null;
			}

			// Unique Files
			sortedFiles.entrySet().parallelStream().filter(hasSingleItemInEntry)
					.peek(entry -> sortedFiles.remove(entry.getKey())).map(entryToValue).flatMap(collection)
					.map(fileReaderToFile).forEach(file -> callback.uniqueFile(file));

			// Duplicate Files
			List<FileReader> duplicateFiles = sortedFiles.remove(FileReader.FINISHED);
			if (duplicateFiles != null) {
				callback.duplicateGroup(
						duplicateFiles.parallelStream().map(fileReaderToFile).collect(Collectors.toList()));
				duplicateFiles = null;

			}

			// Prepare first group for next iteration
			sortedFiles.values().stream().limit(1).forEach(list -> currentCandidates = list);

			// Outsource other groups
			sortedFiles.values().stream().skip(1).forEach(outsourcedCandidates -> executor
					.submit(new DuplicateContentFinder(outsourcedCandidates, callback, executor)));
		}
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
	public static Queue<List<File>> getResult(final Collection<File> input) {
		ConcurrentLinkedQueue<List<File>> result = new ConcurrentLinkedQueue<>();
		DuplicateContentFinderCallback callback = new DuplicateContentFinderCallback() {

			@Override
			public void duplicateGroup(List<File> duplicateGroup) {
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
	 *            Callback, um über die Ergebnisse der Dublettensuche informiert zu
	 *            werden
	 */
	public static void getResult(final Collection<File> input, final DuplicateContentFinderCallback callback) {
		if (input == null) {
			throw new IllegalArgumentException("input may not be null.");
		}
		if (callback == null) {
			throw new IllegalArgumentException("callback may not be null.");
		}

		Executor executor = new Executor();
		executor.submit(new DuplicateContentFinder(FileReader.pack(input), callback, executor));
		executor.consolidate();
	}
}