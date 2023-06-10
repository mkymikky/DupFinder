package de.b0n.dir.processor;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingByConcurrent;

/**
 * Sucht von gegeben Dateigruppen inhaltliche Duplikate.
 */
public class DuplicateContentFinder {

	private static Stream<List<File>> streamDuplicateFilesList(List<FileReader> pack, DuplicateContentFinderCallback callback) {
		if (pack.size() == 1) {
			return Stream.of(
					pack.stream()
						.map(FileReader::clear)
						.toList());
		}

		Map<Integer, List<FileReader>> dubletteCandidates = Map.of(0, pack);
		while (dubletteCandidates.size() == 1
				&& !dubletteCandidates.containsKey(FileReader.FINISHED)
				&& !dubletteCandidates.containsKey(FileReader.FAILING)) {
			dubletteCandidates = dubletteCandidates.values().parallelStream()
					.flatMap(List::parallelStream)
					.collect(groupingByConcurrent(FileReader::read));
		}

		return dubletteCandidates.entrySet().parallelStream()
						.filter(failedFiles(callback))
						.filter(uniqueFiles(callback))
						.flatMap(entry -> {
							if (entry.getKey() == FileReader.FINISHED) {
								return Stream.of(
										entry.getValue().stream()
											.map(FileReader::clear)
											.toList());
							} else {
								return streamDuplicateFilesList(entry.getValue(), callback);
							}
						});
	}

	private static Predicate<Map.Entry<Integer, List<FileReader>>> failedFiles(DuplicateContentFinderCallback callback) {
		return entry -> {
			if (entry.getKey() == FileReader.FAILING) {
				entry.getValue().stream()
						.map(FileReader::clear)
						.forEach(callback::failedFile);
				return false;
			}
			return true;
		};
	}

	private static Predicate<Map.Entry<Integer, List<FileReader>>> uniqueFiles(DuplicateContentFinderCallback callback) {
		return entry -> {
			if (entry.getValue().size() == 1) {
				entry.getValue().stream()
						.map(FileReader::clear)
						.forEach(callback::uniqueFile);
				return false;
			}
			return true;
		};
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
	public static Stream<List<File>> getResult(final Collection<File> input) {
		return getResult(input, new DuplicateContentFinderCallback() {});
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
	public static Stream<List<File>> getResult(final Collection<File> input, final DuplicateContentFinderCallback callback) {
		if (input == null) {
			throw new IllegalArgumentException("input may not be null.");
		}
		if (input.isEmpty()) {
			throw new IllegalArgumentException("input may not be empty.");
		}
		if (callback == null) {
			throw new IllegalArgumentException("callback may not be null.");
		}

		return streamDuplicateFilesList(FileReader.pack(input), callback);
	}
}