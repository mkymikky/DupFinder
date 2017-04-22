//package de.b0n.dir.processor;
//
//import org.junit.*;
//import org.junit.runner.RunWith;
//
//import static org.junit.Assert.*;
//import static org.mockito.Mockito.*;
//import org.mockito.*;
//import org.mockito.junit.MockitoJUnitRunner;
//
//import java.io.BufferedInputStream;
//import java.io.File;
//import java.io.IOException;
//
//@RunWith(MockitoJUnitRunner.class)
//public class FileStreamErrorTest {
//	File file = new File("");
//
//	@Mock
//	private BufferedInputStream stream;
//
//	@InjectMocks
//	private FileStream fileStream = new FileStream(file);
//
//	@Test
//	public void validRead() throws IOException {
//		assertNotNull(fileStream);
//		when(stream.read()).thenReturn(66);
//
//		assertEquals(66, fileStream.read());
//
//		verify(stream).read();
//		verifyNoMoreInteractions(stream);
//	}
//
//	@Test
//	public void failedRead() throws IOException {
//		when(stream.read()).thenThrow(new IOException("No Message"));
//
//		try {
//			fileStream.read();
//		} catch (IllegalStateException e) {
//			assertEquals("Stream of " + file.getAbsolutePath() + " could not be read: No Message", e.getMessage());
//		}
//
//		verify(stream).read();
//		verify(stream).close();
//		verifyNoMoreInteractions(stream);
//	}
//
//	@Test
//	public void validClose() throws IOException {
//		assertNotNull(fileStream);
//
//		fileStream.close();
//
//		verify(stream).close();
//		verifyNoMoreInteractions(stream);
//	}
//
//	@Test
//	public void failedClose() throws IOException {
//		doThrow(new IOException("No Message")).when(stream).close();
//
//		try {
//			fileStream.close();
//		} catch (IllegalStateException e) {
//			assertEquals("Could not close Stream. Nothing to do about that, resetting FileStream.", e.getMessage());
//		}
//
//		verify(stream).close();
//		verifyNoMoreInteractions(stream);
//	}
//}
