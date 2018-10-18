package de.b0n.dir.processor;

import java.io.File;
import java.util.Queue;

public interface DuplicateContentFinderCallback {

	void failedFiles(Queue<File> failedFiles);

	void duplicateGroup(Queue<File> duplicateFiles);

	void uniqueFiles(Queue<File> uniqueFiles);
}
