package de.b0n.dir.processor;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;

public class FileStreamErrorTest {
	private final File file = new File("");

	private final BufferedInputStream stream = mock(BufferedInputStream.class);

	private FileReader fileStream = null;

	@BeforeEach
	void setUp() {
		fileStream = new FileReader(file, stream);
	}
	
	@Test
	public void validRead() throws IOException {
		assertNotNull(fileStream);
		when(stream.read()).thenReturn(66);
		
		assertEquals(66, fileStream.read());
		
		verify(stream).read();
		verifyNoMoreInteractions(stream);
	}

	@Test
	public void failedRead() throws IOException {
		when(stream.read()).thenThrow(new IOException("No Message"));
		
		try {
			fileStream.read();
		} catch (IllegalStateException e) {
			assertEquals("Stream of " + file.getAbsolutePath() + " could not be read: No Message", e.getMessage());
		}

		verify(stream).read();
		verify(stream).close();
		verifyNoMoreInteractions(stream);
	}
	
	@Test
	public void validClose() throws IOException {
		assertNotNull(fileStream);
		
		fileStream.clear();
		
		verify(stream).close();
		verifyNoMoreInteractions(stream);
	}

	@Test
	public void failedClose() throws IOException {
		doThrow(new IOException("No Message")).when(stream).close();
		
		try {
			fileStream.clear();
		} catch (IllegalStateException e) {
			assertEquals("Could not close Stream. Nothing to do about that, resetting FileStream.", e.getMessage());
		}

		verify(stream).close();
		verifyNoMoreInteractions(stream);
	}
}
