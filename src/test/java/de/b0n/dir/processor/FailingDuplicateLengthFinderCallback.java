package de.b0n.dir.processor;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

public class FailingDuplicateLengthFinderCallback implements DuplicateLengthFinderCallback {

	@Override
	public void enteredNewDirectory(File file) {
		fail();
	}

	@Override
	public void unreadableDirectory(String directory) {
		fail();
	}
}
