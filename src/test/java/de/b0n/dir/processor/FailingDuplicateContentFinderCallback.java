package de.b0n.dir.processor;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

public class FailingDuplicateContentFinderCallback implements DuplicateContentFinderCallback {

	@Override
	public void uniqueFile(File uniqueFile) {
		fail();
	}

	@Override
	public void failedFile(File failedFile) {
		fail();
	}

	@Override
	public void duplicateGroup(List<File> queue) {
		fail();
	}
};
