package de.b0n.dir.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    	DuplicateContentFinder.getResult(null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void noArguments2() {
    	DuplicateContentFinder.getResult(null, null);
    }

    @Test
    public void scanEmptyInputWithoutCallback() {
        final Queue<File> input = new ConcurrentLinkedQueue<File>();
        final Queue<Queue<File>> output = DuplicateContentFinder.getResult(input);
        assertNotNull("Es muss ein Ergebnis zurück gegeben werden", output);
        assertTrue("Es muss ein leeres Ergebnis zurück gegeben werden", output.isEmpty());
    }

    @Test
    public void scanEmptyInputWithCallback() {
        final Queue<File> input = new ConcurrentLinkedQueue<File>();
        final DuplicateContentFinderCallback callback = new DuplicateContentFinderCallback() {
			
			@Override
			public void uniqueFiles(int removeUniques) {
				fail();
			}
			
			@Override
			public void failedFiles(int size) {
				fail();
			}
			
			@Override
			public void duplicateGroup(Queue<File> queue) {
				fail();
			}
		};
		DuplicateContentFinder.getResult(input, callback);
    }

    @Test
    public void scanSingleInput() {
        final File file = new File(PATH_FILE_1);
        final Queue<File> input = new ConcurrentLinkedQueue<File>();
        input.add(file);
        final Queue<Queue<File>> output = DuplicateContentFinder.getResult(input);
        assertNotNull("Es muss ein Ergebnis zurück gegeben werden", output);
        assertTrue("Es muss ein leeres Ergebnis zurück gegeben werden", output.isEmpty());
    }

    @Test
    public void scanDuplicateInput() {
        final File file1 = new File(PATH_FILE_1);
        final File file2 = new File(PATH_FILE_2);
        final Queue<File> input = new ConcurrentLinkedQueue<File>();
        input.add(file1);
        input.add(file2);
        final Queue<Queue<File>> output = DuplicateContentFinder.getResult(input);
        assertNotNull("Es muss ein Ergebnis zurück gegeben werden", output);
        assertEquals("Es muss ein Ergebnis mit zwei Dubletten zurück gegeben werden", 1, output.size());
        assertEquals("Es muss ein Ergebnis mit zwei Dubletten zurück gegeben werden", 2, output.peek().size());
        assertTrue("Es muss ein Ergebnis Test1.txt zurück gegeben werden", output.peek().peek().getAbsolutePath().endsWith("Test1.txt"));
    }

    @Test
    public void scanDuplicateInputWithCallback() {
    	final List<Queue<File>> duplicateList = new ArrayList<Queue<File>>();
        final File file1 = new File(PATH_FILE_1);
        final File file2 = new File(PATH_FILE_2);
        final Queue<File> input = new ConcurrentLinkedQueue<File>();
        input.add(file1);
        input.add(file2);
        DuplicateContentFinder.getResult(input, new DuplicateContentFinderCallback() {
			
			@Override
			public void uniqueFiles(int uniques) {
				fail();
			}
			
			@Override
			public void failedFiles(int size) {
				fail();
			}
			
			@Override
			public void duplicateGroup(Queue<File> queue) {
				duplicateList.add(queue);
			}
		});
        assertEquals(1, duplicateList.size());
        assertEquals(2, duplicateList.get(0).size());
        assertTrue("Es muss ein Ergebnis Test1.txt zurück gegeben werden", duplicateList.get(0).peek().getAbsolutePath().endsWith("Test1.txt"));
        assertTrue(duplicateList.get(0).contains(file1));
        assertTrue(duplicateList.get(0).contains(file2));
    }
}
