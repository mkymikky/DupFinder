package de.b0n.dir.processor;

import java.io.File;
import java.util.Queue;

public interface DuplicateContentFinderCallback extends SuchProcessorCallback{

	void failedFiles(int size);

	void duplicateGroup(Queue<File> duplicateGroup);

	void uniqueFiles(int uniqueFileCount);

}
