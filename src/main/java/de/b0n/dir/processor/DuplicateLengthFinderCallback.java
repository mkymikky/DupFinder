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
	 * @param folder Nicht lesbares Verzeichnis
	 */
	void unreadableFolder(File folder);

	/**
	 * Wird aufgerufen, wenn die Dateilänge einer Datei ermittelt wurde
	 * @param size Länge der Datei
	 * @param file Datei mit der ermittelten Länge
	 */
	void addGroupedElement(Long size, File file);

}
