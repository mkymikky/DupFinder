package de.b0n.dir.processor;

import com.github.funthomas424242.unmodifiable.UnmodifiableQueueLIFO;

import java.io.File;

public interface DuplicateContentFinderCallback extends SearchProcessorCallback {

    default void failedFiles(int size) {
        System.out.println("Es wurden " + size + " Dateien nicht berücksichtigt (failed files).");
    }

    default void duplicateGroup(UnmodifiableQueueLIFO<File> duplicateGroup){

    }

    default void uniqueFiles(int uniqueFileCount) {
        System.out.println("Es wurden " + uniqueFileCount + " einzigartige Dateien (besitzen keine Duplikate) gefunden.");
    }

}
