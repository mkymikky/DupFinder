package de.b0n.dir.processor;

import java.io.File;
import java.util.List;

/**
 * Ermöglicht das Tracking des aktuellen Fortschritts auf Dateiebene.
 * Sobald ein Ergebnis bezüglich einer oder mehrerer Dateien vom DuplicateContentFinder erkannt wurde, wird dieser Callback mit dem oder den Files aufgerufen.
 * @author Claus
 *
 */
public interface DuplicateContentFinderCallback {

	/**
	 * Wird aufgerufen, sobald ein Fehler beim Lesen der Datei erkannt wurde
	 * @param failedFile File mit Zugriffsfehler
	 */
	default void failedFile(File failedFile) {}

	/**
	 * Wird aufgerufen, sobald eine einzigartige Datei erkannt wurde
	 * @param uniqueFile File ohne Duplikat
	 */
	default void uniqueFile(File uniqueFile) {}

	/**
	 * Wird aufgerufen, sobald eine Gruppe gleicher Dateien erkannt wurde
	 * @param duplicateFiles Files mit demselben Inhalt
	 */
	default void duplicateGroup(List<File> duplicateFiles) {}

}
