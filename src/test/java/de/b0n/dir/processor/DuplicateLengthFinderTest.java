package de.b0n.dir.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DuplicateLengthFinderTest extends de.b0n.dir.Test {

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

	@Test(expected = IllegalArgumentException.class)
	public void noArgumentFolder() {
		DuplicateLengthFinder.getResult(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void noArgumentCallback() {
		DuplicateLengthFinder.getResult(new File("."), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void noArgumentFolderButCallback() {
		DuplicateLengthFinder.getResult(null, new DuplicateLengthFinderCallback() {
			@Override
			public void enteredNewFolder(File file) {
				fail();
			}

			@Override
			public void unreadableFolder(File folder) {
				fail();
			}

			@Override
			public void addGroupedElement(Long size, File file) {
				fail();
			}
		});
	}

	@Test(expected = IllegalArgumentException.class)
	public void scanInvalidFolder() {
		final File folder = new File(PATH_INVALID_FOLDER);
		DuplicateLengthFinder.getResult(folder);
	}

	@Test(expected = IllegalArgumentException.class)
	public void scanFile() {
		final File folder = new File(PATH_FILE);
		DuplicateLengthFinder.getResult(folder);
	}

	@Test
	public void scanFlatFolder() {
		final File folder = new File(PATH_SAME_SIZE_IN_FLAT_FOLDER);
		final Cluster<Long, File> result = DuplicateLengthFinder.getResult(folder);
		assertNotNull(result);
		assertEquals("falsche Anzahl an Dateien gleicher Größe bestimmt", 1, result.values().size());
		assertEquals("falsche Anzahl von 26 Byte-Datei Vorkommen bestimmt", 2,
				result.values().iterator().next().size());
	}

	@Test
	public void scanFolderOnlyFolder() {
		final File folder = new File(PATH_FOLDER_ONLY_FOLDER);
		final Cluster<Long, File> result = DuplicateLengthFinder.getResult(folder);
		assertNotNull(result);
		assertEquals("falsche Anzahl an Dateien gleicher Größe bestimmt", 1, result.values().size());
		assertEquals("falsche Anzahl von 26 Byte-Datei Vorkommen bestimmt", 2,
				result.values().iterator().next().size());
	}

	@Test
	public void scanEmptyFolder() {
		final File folder = new File(PATH_EMPTY_FOLDER);
		if (folder.mkdir()) {
			final Cluster<Long, File> result = DuplicateLengthFinder.getResult(folder);
			assertNotNull(result);
			assertTrue(result.values().isEmpty());
			folder.delete();
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void scanUnreadableFolder() {
		System.err.println("OS calls itself: " + System.getProperty("os.name"));
		assumeTrue(System.getProperty("os.name").contains("Linux"));
		File folder = new File("/root");
		DuplicateLengthFinder.getResult(folder);
	}

	@Test
	public void scanNoDuplicates() {
		final File folder = new File(PATH_NO_SAME_SIZE_FOLDER);
		final Cluster<Long, File> result = DuplicateLengthFinder.getResult(folder);
		assertNotNull(result);
		assertEquals("Es darf nur eine Gruppe gefunden werden", 1, result.values().size());
		assertEquals("In der gefundenen Gruppe darf nur ein Element sein", 1, result.values().iterator().next().size());
		
	}

	@Test
	public void scanDuplicatesInTree() {
		final File folder = new File(PATH_SAME_SIZE_FILES_IN_TREE_FOLDER);
		final Cluster<Long, File> result = DuplicateLengthFinder.getResult(folder);
		assertNotNull(result);
		Iterator<Queue<File>> elementsIterator = result.values().iterator(); 
		assertEquals(1, result.values().size());
		assertEquals(2, elementsIterator.next().size());
	}

	@Test
	public void scanDuplicatesInBiggerTreeWithCallback() {
		final File folder = new File(PATH_PLENTY_SAME_SIZE_FOLDER);
		List<String> foldersEntered = new ArrayList<String>();
		Cluster<Long, File> result = new Cluster<>();
		DuplicateLengthFinder.getResult(folder, new DuplicateLengthFinderCallback() {
			
			@Override
			public void enteredNewFolder(File folder) {
				try {
					foldersEntered.add(folder.getCanonicalPath());
				} catch (IOException e) {
					fail();
				}
			}

			@Override
			public void unreadableFolder(File folder) {
				fail();
			}

			@Override
			public void addGroupedElement(Long size, File file) {
				result.addGroupedElement(size, file);
			}
		});
		assertEquals("falsche Anzahl an Dateien gleicher Größe bestimmt", 2, result.values().size());
		Iterator<Queue<File>> elementsIterator = result.values().iterator(); 
		assertEquals("falsche Anzahl von 26 Byte-Datei Vorkommen bestimmt", 2, elementsIterator.next().size());
		assertEquals("falsche Anzahl von 91 Byte-Datei Vorkommen bestimmt", 4, elementsIterator.next().size());
		assertEquals(5, foldersEntered.size());
		assertListContainsLineEndingWith(foldersEntered, "duplicateTree");
		assertListContainsLineEndingWith(foldersEntered, "subfolder");
		assertListContainsLineEndingWith(foldersEntered, "folderOnlyFolder");
		assertListContainsLineEndingWith(foldersEntered, "flatDuplicateTree");
		assertListContainsLineEndingWith(foldersEntered, "noDuplicates");
		
	}

	@Test
	public void scanDuplicatesInBiggerTreeWithFolder() {
		final File folder = new File(PATH_PLENTY_SAME_SIZE_FOLDER);
		final Cluster<Long, File> result = DuplicateLengthFinder.getResult(folder);
		assertNotNull(result);
		assertEquals("falsche Anzahl an Dateien gleicher Größe bestimmt", 2, result.values().size());
		Iterator<Queue<File>> elementsIterator = result.values().iterator(); 
		assertEquals("falsche Anzahl von 26 Byte-Datei Vorkommen bestimmt", 2, elementsIterator.next().size());
		assertEquals("falsche Anzahl von 91 Byte-Datei Vorkommen bestimmt", 4, elementsIterator.next().size());
	}
}
