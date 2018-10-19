package de.b0n.dir.processor;

import java.io.File;

public class AbstractDuplicateLengthFinderCallback implements DuplicateLengthFinderCallback {

	@Override
	public void enteredNewFolder(File folder) {}

	@Override
	public void unreadableFolder(File folder) {}

	@Override
	public void addGroupedElement(Long size, File file) {}

}
