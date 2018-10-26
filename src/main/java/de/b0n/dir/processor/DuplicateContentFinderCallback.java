package de.b0n.dir.processor;

import java.io.File;
import java.util.List;

public interface DuplicateContentFinderCallback {

	default void failedFile(File failedFile) {}

	default void uniqueFile(File uniqueFile) {}

	default void duplicateGroup(List<File> duplicateFiles) {}

}
