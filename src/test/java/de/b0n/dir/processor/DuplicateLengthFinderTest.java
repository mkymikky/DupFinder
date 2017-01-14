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

/**
 * Created by huluvu424242 on 07.01.17.
 */
public class DuplicateLengthFinderTest {

    private static final String PATH_SAME_SIZE_FILES_IN_TREE_FOLDER = "src/test/resources/duplicateTree";
	private static final String PATH_FILE = "src/test/resources/Test1.txt";
	private static final String PATH_INVALID_FOLDER = "src/test/resourcesInvalid/";
	private static final String PATH_NO_SAME_SIZE_FOLDER = "src/test/resources/noDuplicates";
	private static final String PATH_EMPTY_FOLDER = "src/test/resources/emptyFolder";
    private static final String PATH_PLENTY_SAME_SIZE_FOLDER = "src/test/resources/";
	private static final String PATH_SAME_SIZE_IN_FLAT_FOLDER = "src/test/resources/folderOnlyFolder/flatDuplicateTree";
	private static final String PATH_FOLDER_ONLY_FOLDER = "src/test/resources/folderOnlyFolder";

	@Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test(expected=IllegalArgumentException.class)
    public void scanInvalidFolder() {
        final ExecutorService threadPool = Executors.newWorkStealingPool();
        final File folder = new File(PATH_INVALID_FOLDER);
        DuplicateLengthFinder.getResult(threadPool, folder);
    }

    @Test(expected=IllegalArgumentException.class)
    public void scanFile() {
        final ExecutorService threadPool = Executors.newWorkStealingPool();
        final File folder = new File(PATH_FILE);
        DuplicateLengthFinder.getResult(threadPool, folder);
    }

    @Test
    public void scanFlatFolder() {
        final ExecutorService threadPool = Executors.newWorkStealingPool();
        final File folder = new File(PATH_SAME_SIZE_IN_FLAT_FOLDER);
        final Queue<Queue<File>> result = DuplicateLengthFinder.getResult(threadPool, folder);
        assertNotNull(result);
        assertEquals("falsche Anzahl an Dateien gleicher Größe bestimmt", 1, result.size());
        assertEquals("falsche Anzahl von 26 Byte-Datei Vorkommen bestimmt", 2, result.element().size());
    }

    @Test
    public void scanFolderOnlyFolder() {
        final ExecutorService threadPool = Executors.newWorkStealingPool();
        final File folder = new File(PATH_FOLDER_ONLY_FOLDER);
        final Queue<Queue<File>> result = DuplicateLengthFinder.getResult(threadPool, folder);
        assertNotNull(result);
        assertEquals("falsche Anzahl an Dateien gleicher Größe bestimmt", 1, result.size());
        assertEquals("falsche Anzahl von 26 Byte-Datei Vorkommen bestimmt", 2, result.element().size());
    }

    @Test
    public void scanEmptyFolder() {
        final ExecutorService threadPool = Executors.newWorkStealingPool();
        final File folder = new File(PATH_EMPTY_FOLDER);
        if (folder.mkdir()) {
        	final Queue<Queue<File>> result = DuplicateLengthFinder.getResult(threadPool, folder);
        	assertNotNull(result);
        	assertEquals(0, result.size());
        	folder.delete();
        }
    }

    @Test
    public void scanNoDuplicates() {
        final ExecutorService threadPool = Executors.newWorkStealingPool();
        final File folder = new File(PATH_NO_SAME_SIZE_FOLDER);
        final Queue<Queue<File>> result = DuplicateLengthFinder.getResult(threadPool, folder);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void scanDuplicatesInTree() {
        final ExecutorService threadPool = Executors.newWorkStealingPool();
        final File folder = new File(PATH_SAME_SIZE_FILES_IN_TREE_FOLDER);
        final Queue<Queue<File>> result = DuplicateLengthFinder.getResult(threadPool, folder);
        assertNotNull(result);
        assertEquals(1,result.size());
        assertEquals(2,result.element().size());
    }

    @Test
    public void scanDuplicatesInBiggerTree() {
        final ExecutorService threadPool = Executors.newWorkStealingPool();
        final File folder = new File(PATH_PLENTY_SAME_SIZE_FOLDER);
        final Queue<Queue<File>> result = DuplicateLengthFinder.getResult(threadPool, folder);
        assertNotNull(result);
        assertEquals("falsche Anzahl an Dateien gleicher Größe bestimmt", 2, result.size());
        assertEquals("falsche Anzahl von 26 Byte-Datei Vorkommen bestimmt", 2, result.poll().size());
        assertEquals("falsche Anzahl von 91 Byte-Datei Vorkommen bestimmt", 4, result.poll().size());
    }
}
