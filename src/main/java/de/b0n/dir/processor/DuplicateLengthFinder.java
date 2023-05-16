package de.b0n.dir.processor;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * Sucht in einem gegebenen Verzeichnis und dessen Unterverzeichnissen nach
 * Dateien und sortiert diese nach Dateigröße.
 */
public class DuplicateLengthFinder {
	/**
	 * Iteriert durch die Elemente im Verzeichnis und legt neue Suchen für
	 * Verzeichnisse an. Dateien werden sofort der Größe nach abgelegt. Wartet die
	 * Unterverzeichnis-Suchen ab und merged deren Ergebnisdateien. Liefert das
	 * Gesamtergebnis zurück.
	 */
	public static Stream<File> handleDirectory(File directory, DuplicateLengthFinderCallback callback) {
		callback.enteredNewDirectory(directory);
		try {
			return readContent(directory).flatMap(file -> handleFile(file, callback));
		} catch (IllegalStateException ise) {
			callback.unreadableDirectory(ise.getMessage());
		}
		return Stream.empty();
	}

	private static Stream<File> handleFile(File file, DuplicateLengthFinderCallback callback) {
		if (file.isDirectory()) {
			return handleDirectory(file, callback);
		} else if (file.isFile()) {
			return Stream.of(file);
		}
		callback.unidentifiedFileObject(file.getAbsolutePath());
		return Stream.empty();
	}

	private static Stream<File> readContent(File directory) {
		return Arrays.stream(Optional.ofNullable(directory.list())
				.orElseThrow(() -> new IllegalStateException(directory.getAbsolutePath())))
				.parallel()
				.map(fileName -> new File(directory.getAbsolutePath() + System.getProperty("file.separator") + fileName));
	}

	/**
	 * Einstiegsmethode zum Durchsuchen eines Verzeichnisses nach Dateien gleicher
	 * Größe.
	 * 
	 * @param directory
	 *            Zu durchsuchendes Verzeichnis
	 * @return Liefert ein Cluster nach Dateigröße strukturierten Queues zurück, in
	 *         denen die gefundenen Dateien abgelegt sind
	 */
	public static Map<Long, List<File>> getResult(final File directory) {
		return getResult(directory, new DuplicateLengthFinderCallback() {});
	}

	/**
	 * Einstiegsmethode zum Durchsuchen eines Verzeichnisses nach Dateien gleicher
	 * Größe.
	 * 
	 * @param directory
	 *            Zu durchsuchendes Verzeichnis
	 * @param callback
	 *            Ruft den Callback bei jedem neu betretenen Verzeichnis auf
	 */
	public static Map<Long, List<File>> getResult(final File directory, DuplicateLengthFinderCallback callback) {
		if (directory == null) {
			throw new IllegalArgumentException("directory may not be null.");
		}
		if (callback == null) {
			throw new IllegalArgumentException("callback may not be null.");
		}
		if (!directory.exists()) {
			throw new IllegalArgumentException("directory must exist.");
		}
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException("directory must be a valid directory.");
		}

		return handleDirectory(directory, callback).collect(groupingBy(File::length, toList()));
	}
}
