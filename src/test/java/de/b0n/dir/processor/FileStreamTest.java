package de.b0n.dir.processor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import static org.junit.Assert.*;

/**
 * Created by huluvu424242 on 06.01.17.
 */
public class FileStreamTest {

	private static final String PATH_VALID_FILE = "src/test/resources/Test1.txt";
	private static final String PATH_INVALID_FILE = "src/test/resources/Testxxx1.txt";

	private File textFile = null;
	private FileStream fileStream = null;

    @Before
    public void setUp() {
        textFile = new File(PATH_VALID_FILE);
        assertTrue(textFile.exists());

    }

    @After
    public void tearDown() {
        textFile = null;
        if (fileStream != null) {
        	fileStream.close();
        	fileStream = null;
        }
    }

    @Test
    public void createInstanceFromValidFile() {
        final FileStream fileStream = new FileStream(textFile);
        assertNotNull(fileStream);
    }

    @Test
    public void createInstanceFromInvalidFile() {
        final File invalidFile = new File(PATH_INVALID_FILE);
        assertFalse(invalidFile.exists());
        try {
            final FileStream fileStream = new FileStream(invalidFile);
            fileStream.read();
            fail();
        } catch (IllegalStateException ex) {
            assertNotNull(ex);
        }
    }

    @Test
    public void createInstanceFromNull() {
        try {
        	new FileStream(null);
            fail();
        } catch (IllegalArgumentException ex) {
            assertNotNull(ex);
        }
    }

    @Test
    public void getValidStreamWithValidFile() throws IOException {
        final FileStream fileStream = new FileStream(this.textFile);
        assertNotNull(fileStream);
        
        for (int i = 0; i<91; i++) {
        	assertTrue(fileStream.read() > 0);
        }
        assertEquals(-1, fileStream.read());
    }

    @Test
    public void getValidFileWithValidFile() throws IOException {
        final FileStream stream = new FileStream(textFile);
        assertNotNull(stream);
        assertSame(textFile, stream.getFile());
        assertEquals("wrong file size", 91, stream.getFile().length());
    }

    @Test
    public void beNiceIfDoubleClose() {
        final FileStream fileStream = new FileStream(textFile);
        assertNotNull(fileStream);
        assertEquals('U', fileStream.read());
        fileStream.close();
        assertEquals('U', fileStream.read());
        fileStream.close();
        fileStream.close();
    }
}
