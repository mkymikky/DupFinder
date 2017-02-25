package de.b0n.dir.processor;

import java.io.File;

/**
 * Ermöglicht das Tracking des aktuellen Fortschritts auf Verzeichnisebene.
 * Sobald ein neues Verzeichnis vom DuplicateLengthFinder zum Durchsuchen erkannt wurde, wird dieser Callback mit dessen kanonischen Pfad aufgerufen.
 *
 * @author Claus
 */
public interface DuplicateLengthFinderCallback extends SearchProcessorCallback {

    /**
     * Wird aufgerufen, sobald ein neues zu durchsuchendes Verzeichnis erkannt wurde
     *
     * @param folder File zum zu durchsuchenden Verzeichnis
     */
    default void enteredNewFolder(File folder) {
        System.out.println("Scanne " + folder.getAbsolutePath());
    }

    /**
     * Wird aufgerufen, wenn ein Verzeichnis nicht gelesen werden kann.
     *
     * @param folder
     */
    default void unreadableFolder(File folder) {
        System.err.println("Warning: Folder nicht lesbar: " + folder.getAbsolutePath());
    }

    /**
     * Wird aufgerufen wenn ein Folder nicht analysiert wird z.B. weil er ein Sym-Link ist.
     *
     * @param folder
     */
    default void skipFolder(File folder) {
        System.out.println("Warning: Folder wird übersprungen: " + folder.getAbsolutePath());
    }

}
