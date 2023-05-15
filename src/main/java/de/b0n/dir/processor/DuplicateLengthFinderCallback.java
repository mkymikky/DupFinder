package de.b0n.dir.processor;

import java.io.File;

/**
 * Ermöglicht das Tracking des aktuellen Fortschritts auf Verzeichnisebene.
 * Sobald ein neues Verzeichnis vom DuplicateLengthFinder zum Durchsuchen erkannt wurde, wird dieser Callback mit dessen kanonischen Pfad aufgerufen.
 * @author Claus
 *
 */
public interface DuplicateLengthFinderCallback {

	/**
	 * Wird aufgerufen, sobald ein neues zu durchsuchendes Verzeichnis erkannt wurde
	 * @param directory File zum zu durchsuchenden Verzeichnis
	 */
	default void enteredNewDirectory(File directory) {}

	/**
	 * Wird aufgerufen, wenn ein Verzeichnis nicht gelesen werden kann.
	 * @param directory Nicht lesbares Verzeichnis
	 */
	default void unreadableDirectory(File directory) {}

	/**
	 * Wird aufgerufen, wenn die Dateilänge einer Datei ermittelt wurde
	 * @param size Länge der Datei
	 * @param file Datei mit der ermittelten Länge
	 */
	default void addGroupedElement(Long size, File file) {}

}
