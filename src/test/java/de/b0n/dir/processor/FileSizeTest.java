package de.b0n.dir.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class FileSizeTest {
	FileSize fileSize;

	@Before
	public void setUp() {
		fileSize = new FileSize(new File("."), 1);
	}

	@Test
	public void testFileSize() {
		assertNotNull(fileSize);
	}

	@Test
	public void testGetSize() {
		assertEquals(1, fileSize.getSize());
	}

	@Test
	public void testGetFile() {
		assertEquals(".", fileSize.getFile().getPath());
	}

}
