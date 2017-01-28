package de.b0n.dir.processor;

/**
 * Ermöglicht das Tracking des aktuellen Fortschritts auf Verzeichnisebene.
 * Sobald ein neues Verzeichnis vom DuplicateLengthFinder zum Durchsuchen erkannt wurde, wird dieser Callback mit dessen kanonischen Pfad aufgerufen.
 * @author Claus
 *
 */
public interface DuplicateLengthFinderCallback {

	/**
	 * Wird aufgerufen, sobald ein neues zu durchsuchendes Verzeichnis erkannt wurde
	 * @param canonicalPath Eindeutiger Pfad zum zu durchsuchenden Verzeichnis
	 */
	void enteredNewFolder(String canonicalPath);

}
