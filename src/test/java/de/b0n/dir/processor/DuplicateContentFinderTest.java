//package de.b0n.dir.processor;
//
//import static org.junit.Assert.*;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Queue;
//import java.util.concurrent.ConcurrentLinkedQueue;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//import de.b0n.dir.processor.DuplicateContentFinderCallback;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//public class DuplicateContentFinderTest {
//
//	private static final String PATH_FILE_1 = "src/test/resources/Test1.txt";
//	private static final String PATH_FILE_2 = "src/test/resources/noDuplicates/Test1.txt";
//
//	@Before
//	public void setUp() {
//
//	}
//
//	@After
//	public void tearDown() {
//
//	}
//
//    @Test(expected=IllegalArgumentException.class)
//    public void noArguments1() {
//    	DuplicateContentFinder.determineDuplicates(null, null);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void noArguments2() {
//    	DuplicateContentFinder.determineDuplicates(null, null, null);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void noThreadPool1() {
//        final Queue<Queue<File>> input = new ConcurrentLinkedQueue<Queue<File>>();
//        DuplicateContentFinder.determineDuplicates(input, null);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void noThreadPool2() {
//        final Queue<Queue<File>> input = new ConcurrentLinkedQueue<Queue<File>>();
//        DuplicateContentFinder.determineDuplicates(input, null, null);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void noFolder1() {
//        final ExecutorService threadPool = Executors.newWorkStealingPool();
//        DuplicateContentFinder.determineDuplicates(null, threadPool);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void noFolder2() {
//        final ExecutorService threadPool = Executors.newWorkStealingPool();
//        DuplicateContentFinder.determineDuplicates(null, threadPool, null);
//    }
//
//    @Test
//    public void scanEmptyInputWithoutCallback() {
//        final ExecutorService threadPool = Executors.newWorkStealingPool();
//        final Queue<Queue<File>> input = new ConcurrentLinkedQueue<Queue<File>>();
//        final Queue<Queue<File>> output = DuplicateContentFinder.determineDuplicates(input, threadPool);
//        assertNotNull("Es muss ein Ergebnis zurück gegeben werden", output);
//        assertTrue("Es muss ein leeres Ergebnis zurück gegeben werden", output.isEmpty());
//    }
//
//    @Test
//    public void scanEmptyInputWithCallback() {
//        final ExecutorService threadPool = Executors.newWorkStealingPool();
//        final Queue<Queue<File>> input = new ConcurrentLinkedQueue<Queue<File>>();
//        final DuplicateContentFinderCallback callback = new DuplicateContentFinderCallback() {
//
//			@Override
//			public void uniqueFiles(int removeUniques) {
//				fail();
//			}
//
//			@Override
//			public void failedFiles(int size) {
//				fail();
//			}
//
//			@Override
//			public void duplicateGroup(Queue<File> queue) {
//				fail();
//			}
//		};
//        final Queue<Queue<File>> output = DuplicateContentFinder.determineDuplicates(input, threadPool, callback);
//        assertNotNull("Es muss ein Ergebnis zurück gegeben werden", output);
//        assertTrue("Es muss ein leeres Ergebnis zurück gegeben werden", output.isEmpty());
//    }
//
//    @Test
//    public void scanSingleInput() {
//        final ExecutorService threadPool = Executors.newWorkStealingPool();
//        final File file = new File(PATH_FILE_1);
//        final Queue<File> files = new ConcurrentLinkedQueue<File>();
//        files.add(file);
//        final Queue<Queue<File>> input = new ConcurrentLinkedQueue<Queue<File>>();
//        input.add(files);
//        final Queue<Queue<File>> output = DuplicateContentFinder.determineDuplicates(input, threadPool);
//        assertNotNull("Es muss ein Ergebnis zurück gegeben werden", output);
//        assertTrue("Es muss ein leeres Ergebnis zurück gegeben werden", output.isEmpty());
//    }
//
//    @Test
//    public void scanDuplicateInput() {
//        final ExecutorService threadPool = Executors.newWorkStealingPool();
//        final File file1 = new File(PATH_FILE_1);
//        final File file2 = new File(PATH_FILE_2);
//        final Queue<File> files = new ConcurrentLinkedQueue<File>();
//        files.add(file1);
//        files.add(file2);
//        final Queue<Queue<File>> input = new ConcurrentLinkedQueue<Queue<File>>();
//        input.add(files);
//        final Queue<Queue<File>> output = DuplicateContentFinder.determineDuplicates(input, threadPool);
//        assertNotNull("Es muss ein Ergebnis zurück gegeben werden", output);
//        assertEquals("Es muss ein Ergebnis mit zwei Dubletten zurück gegeben werden", 1, output.size());
//        assertEquals("Es muss ein Ergebnis mit zwei Dubletten zurück gegeben werden", 2, output.peek().size());
//        assertTrue("Es muss ein Ergebnis Test1.txt zurück gegeben werden", output.peek().peek().getAbsolutePath().endsWith("Test1.txt"));
//    }
//
//    @Test
//    public void scanDuplicateInputWithCallback() {
//    	final List<Queue<File>> duplicateList = new ArrayList<Queue<File>>();
//        final ExecutorService threadPool = Executors.newWorkStealingPool();
//        final File file1 = new File(PATH_FILE_1);
//        final File file2 = new File(PATH_FILE_2);
//        final Queue<File> files = new ConcurrentLinkedQueue<File>();
//        files.add(file1);
//        files.add(file2);
//        final Queue<Queue<File>> input = new ConcurrentLinkedQueue<Queue<File>>();
//        input.add(files);
//        final Queue<Queue<File>> output = DuplicateContentFinder.determineDuplicates(input, threadPool, new DuplicateContentFinderCallback() {
//
//			@Override
//			public void uniqueFiles(int uniques) {
//				fail();
//			}
//
//			@Override
//			public void failedFiles(int size) {
//				fail();
//			}
//
//			@Override
//			public void duplicateGroup(Queue<File> queue) {
//				duplicateList.add(queue);
//			}
//		});
//        assertNotNull("Es muss ein Ergebnis zurück gegeben werden", output);
//        assertEquals("Es muss ein Ergebnis mit zwei Dubletten zurück gegeben werden", 1, output.size());
//        assertEquals("Es muss ein Ergebnis mit zwei Dubletten zurück gegeben werden", 2, output.peek().size());
//        assertTrue("Es muss ein Ergebnis Test1.txt zurück gegeben werden", output.peek().peek().getAbsolutePath().endsWith("Test1.txt"));
//        assertEquals(1, duplicateList.size());
//        assertEquals(2, duplicateList.get(0).size());
//        assertTrue(duplicateList.get(0).contains(file1));
//        assertTrue(duplicateList.get(0).contains(file2));
//    }
//}
