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

    protected File simpleTextfile=null;

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
    }

    @Test
    public void getValidStreamWithValidFile() throws IOException {
        final FileStream fileStream = new FileStream(this.simpleTextfile);
        assertNotNull(fileStream);
        assertEquals("wrong file size",91,fileStream.getStream().available());
    }

}
