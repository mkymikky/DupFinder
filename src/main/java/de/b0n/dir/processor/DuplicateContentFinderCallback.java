package de.b0n.dir.processor;

import com.github.funthomas424242.unmodifiable.UnmodifiableQueue;

import java.io.File;
import java.util.Queue;

public interface DuplicateContentFinderCallback extends SearchProcessorCallback {

    default void failedFiles(int size) {
        System.out.println("Es wurden " + size + " Dateien nicht berücksichtigt (failed files).");
    }

    default void duplicateGroup(UnmodifiableQueue<File> duplicateGroup){

    }

    default void uniqueFiles(int uniqueFileCount) {
        System.out.println("Es wurden " + uniqueFileCount + " einzigartige Dateien (besitzen keine Duplikate) gefunden.");
    }

}
