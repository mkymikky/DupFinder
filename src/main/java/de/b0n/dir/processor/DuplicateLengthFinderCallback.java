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
	default void unreadableDirectory(String directory) {}

	/**
	 * Wird aufgerufen, wenn ein File im Verzeichnis weder Datei noch Verzeichnis ist
	 * @param file Unklassifiziertes Dateiobjekt
	 */
	default void unidentifiedFileObject(String file) {}
}
