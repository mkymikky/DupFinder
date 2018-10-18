package de.b0n.dir.processor;

import java.io.File;
import java.util.Queue;

public class AbstractDuplicateContentFinderCallback implements DuplicateContentFinderCallback {

	@Override
	public void failedFiles(Queue<File> failedFiles) {}

	@Override
	public void duplicateGroup(Queue<File> duplicateGroup) {}

	@Override
	public void uniqueFiles(Queue<File> uniqueFiles) {}
}
