package de.b0n.dir.processor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Created by huluvu424242 on 06.01.17.
 */
public class FileStreamTest {

    protected File simpleTextfile = null;

    @Before
    public void setUp() {
        simpleTextfile = new File("src/test/resources/Test1.txt");
        assertTrue(simpleTextfile.exists());

    }

    @After
    public void tearDown() {
        simpleTextfile = null;
    }

    @Test
    public void createInstanceFromValidFile() {
        final File validFile = new File("src/test/resources/Test1.txt");
        assertTrue(validFile.exists());
        final FileStream stream = new FileStream(validFile);
        assertNotNull(stream);
        assertSame(validFile, stream.getFile());
        stream.close();
    }

    @Test
    public void createInstanceFromInvalidFile() {
        final File invalidFile = new File("src/test/resources/Testxxx1.txt");
        assertFalse(invalidFile.exists());
        try {
            final FileStream stream = new FileStream(invalidFile);
            stream.getStream();
            fail();
        } catch (IllegalStateException ex) {
            assertNotNull(ex);
        }
    }

    @Test
    public void createInstanceFromNull() {
        try {
            final FileStream stream = new FileStream(null);
            stream.getStream();
            fail();
        } catch (NullPointerException ex) {
            assertNotNull(ex);
        }
    }


    @Test
    public void getValidStreamWithValidFile() throws IOException {
        final FileStream fileStream = new FileStream(this.simpleTextfile);
        assertNotNull(fileStream);
        final InputStream stream = fileStream.getStream();
        assertEquals("wrong file size", 91, stream.available());
        // duplicate call for else branch
        assertSame(stream, fileStream.getStream());
        fileStream.close();
    }

    @Test
    public void getValidFileWithValidFile() throws IOException {
        final FileStream fileStream = new FileStream(this.simpleTextfile);
        assertNotNull(fileStream);
        assertEquals("wrong file size", 91, fileStream.getFile().length());
        fileStream.close();
    }

    @Test
    public void handleExceptionIfDoubleClose() {
        final FileStream fileStream = new FileStream(this.simpleTextfile);
        assertNotNull(fileStream);
        final InputStream stream = fileStream.getStream();
        assertNotNull(stream);
        try {
            stream.close();
        } catch (IOException e) {
            fail();
        }
        assertNotNull(fileStream.getStream());
        fileStream.close();
        assertNotNull(fileStream.getStream());
        //TODO ewarten würde ich eigentlich null
        //assertNull(fileStream.getStream());
    }

}
