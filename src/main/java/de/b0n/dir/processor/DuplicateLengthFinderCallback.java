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
	 * @param folder File zum zu durchsuchenden Verzeichnis
	 */
	void enteredNewFolder(File folder);

	/**
	 * Wird aufgerufen, wenn ein Verzeichnis nicht gelesen werden kann.
	 * @param folder
	 */
	void unreadableFolder(File folder);

}
