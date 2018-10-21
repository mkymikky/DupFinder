package de.b0n.dir.processor;

import static org.junit.Assert.fail;

import java.io.File;

public class FailingDuplicateLengthFinderCallback implements DuplicateLengthFinderCallback {

	@Override
	public void enteredNewFolder(File file) {
		fail();
	}

	@Override
	public void unreadableFolder(File folder) {
		fail();
	}

	@Override
	public void addGroupedElement(Long size, File file) {
		fail();
	}
}
