package de.b0n.dir.processor;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

public class FailingDuplicateContentFinderCallback implements DuplicateContentFinderCallback {

	@Override
	public void uniqueFile(File uniqueFile) { fail(); }

	@Override
	public void failedFile(File failedFile) {
		fail();
	}
}
