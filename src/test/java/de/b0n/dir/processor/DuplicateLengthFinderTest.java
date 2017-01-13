package de.b0n.dir.processor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by huluvu424242 on 07.01.17.
 */
public class DuplicateLengthFinderTest {

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void createValidInstanceWithInvalidFolderParameter() {
        final ExecutorService threadPool = Executors.newWorkStealingPool();
        final File folder = new File("src/test/resourcesInvalid/");
        final Queue<Queue<File>> result = DuplicateLengthFinder.getResult(threadPool, folder);
        assertNotNull(result);
        assertEquals("falsche Anzahl an Duplikaten bestimmt",0,result.size());
    }

    @Test
    public void createValidInstanceWithAFileAnstaedOfFolderParameter() {
        final ExecutorService threadPool = Executors.newWorkStealingPool();
        final File folder = new File("src/test/resources/Test1.txt");
        final Queue<Queue<File>> result = DuplicateLengthFinder.getResult(threadPool, folder);
        assertNotNull(result);
        assertEquals("falsche Anzahl an Duplikaten bestimmt",0,result.size());
    }

    @Test
    public void createValidInstanceWithValidParameters() {
        final ExecutorService threadPool = Executors.newWorkStealingPool();
        final File folder = new File("src/test/resources/");
        final Queue<Queue<File>> result = DuplicateLengthFinder.getResult(threadPool, folder);
        assertNotNull(result);
        assertEquals("falsche Anzahl an Duplikaten bestimmt",1,result.size());
        assertEquals("falsche Anzahl von Test1.txt Vorkommen bestimmt",4,result.element().size());
    }

    @Test
    public void scanForDuplicateGivenEmptyFolder() {
        final ExecutorService threadPool = Executors.newWorkStealingPool();
        final File folder = new File("src/test/resources/emptyFolder");
        final Queue<Queue<File>> result = DuplicateLengthFinder.getResult(threadPool, folder);
        assertNotNull(result);
        assertEquals(0,result.size());
    }

    @Test
    public void scanForDuplicateGivenDuplicate0() {
        final ExecutorService threadPool = Executors.newWorkStealingPool();
        final File folder = new File("src/test/resources/duplicate0");
        final Queue<Queue<File>> result = DuplicateLengthFinder.getResult(threadPool, folder);
        assertNotNull(result);
        assertEquals(0,result.size());
    }

    @Test
    public void scanForDuplicateGivenDuplicate1() {
        final ExecutorService threadPool = Executors.newWorkStealingPool();
        final File folder = new File("src/test/resources/duplicate1");
        final Queue<Queue<File>> result = DuplicateLengthFinder.getResult(threadPool, folder);
        assertNotNull(result);
        assertEquals(1,result.size());
        assertEquals(2,result.element().size());
    }

}
