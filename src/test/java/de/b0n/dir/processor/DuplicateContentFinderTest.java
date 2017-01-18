package de.b0n.dir.processor;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DuplicateContentFinderTest {

	private static final String PATH_FILE_1 = "src/test/resources/Test1.txt";
	private static final String PATH_FILE_2 = "src/test/resources/noDuplicates/Test1.txt";
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
    
    @Test(expected=IllegalArgumentException.class)
    public void noArguments1() {
    	DuplicateContentFinder.getResult(null, null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void noArguments2() {
    	DuplicateContentFinder.getResult(null, null, null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void noThreadPool1() {
        final Queue<Queue<File>> input = new ConcurrentLinkedQueue<Queue<File>>();
        DuplicateContentFinder.getResult(null, input);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void noThreadPool2() {
        final Queue<Queue<File>> input = new ConcurrentLinkedQueue<Queue<File>>();
        DuplicateContentFinder.getResult(null, input, null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void noFolder1() {
        final ExecutorService threadPool = Executors.newWorkStealingPool();
        DuplicateContentFinder.getResult(threadPool, null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void noFolder2() {
        final ExecutorService threadPool = Executors.newWorkStealingPool();
        DuplicateContentFinder.getResult(threadPool, null, null);
    }

    @Test
    public void scanEmptyInput1() {
        final ExecutorService threadPool = Executors.newWorkStealingPool();
        final Queue<Queue<File>> input = new ConcurrentLinkedQueue<Queue<File>>();
        final Queue<Queue<File>> output = DuplicateContentFinder.getResult(threadPool, input);
        assertNotNull("Es muss ein Ergebnis zurück gegeben werden", output);
        assertTrue("Es muss ein leeres Ergebnis zurück gegeben werden", output.isEmpty());
    }

    @Test
    public void scanEmptyInput2() {
        final ExecutorService threadPool = Executors.newWorkStealingPool();
        final Queue<Queue<File>> input = new ConcurrentLinkedQueue<Queue<File>>();
        final Queue<Queue<File>> output = new ConcurrentLinkedQueue<Queue<File>>();
        final Queue<Queue<File>> otherOutput = DuplicateContentFinder.getResult(threadPool, input, output);
        assertNotNull("Es muss ein Ergebnis zurück gegeben werden", output);
        assertSame("Ausgabe und Arbeitsqueue müssen identisch sein.", output, otherOutput);
        assertTrue("Es muss ein leeres Ergebnis zurück gegeben werden", output.isEmpty());
    }

    @Test
    public void scanSingleInput() {
        final ExecutorService threadPool = Executors.newWorkStealingPool();
        final File file = new File(PATH_FILE_1);
        final Queue<File> files = new ConcurrentLinkedQueue<File>();
        files.add(file);
        final Queue<Queue<File>> input = new ConcurrentLinkedQueue<Queue<File>>();
        input.add(files);
        final Queue<Queue<File>> output = DuplicateContentFinder.getResult(threadPool, input);
        assertNotNull("Es muss ein Ergebnis zurück gegeben werden", output);
        assertTrue("Es muss ein leeres Ergebnis zurück gegeben werden", output.isEmpty());
    }

    @Test
    public void scanDuplicateInput() {
        final ExecutorService threadPool = Executors.newWorkStealingPool();
        final File file1 = new File(PATH_FILE_1);
        final File file2 = new File(PATH_FILE_2);
        final Queue<File> files = new ConcurrentLinkedQueue<File>();
        files.add(file1);
        files.add(file2);
        final Queue<Queue<File>> input = new ConcurrentLinkedQueue<Queue<File>>();
        input.add(files);
        final Queue<Queue<File>> output = DuplicateContentFinder.getResult(threadPool, input);
        assertNotNull("Es muss ein Ergebnis zurück gegeben werden", output);
        assertEquals("Es muss ein Ergebnis mit zwei Dubletten zurück gegeben werden", 1, output.size());
        assertEquals("Es muss ein Ergebnis mit zwei Dubletten zurück gegeben werden", 2, output.peek().size());
        assertTrue("Es muss ein Ergebnis Test1.txt zurück gegeben werden", output.peek().peek().getAbsolutePath().endsWith("Test1.txt"));
    }
}
