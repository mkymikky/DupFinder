package de.b0n.dir.processor;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class DuplicateLengthFinderTest extends de.b0n.dir.Test {
	private static final String PATH_SAME_SIZE_FILES_IN_TREE_FOLDER = "src/test/resources/duplicateTree";
	private static final String PATH_FILE = "src/test/resources/Test1.txt";
	private static final String PATH_INVALID_FOLDER = "src/test/resourcesInvalid/";
	private static final String PATH_NO_SAME_SIZE_FOLDER = "src/test/resources/duplicateTree/subdirectory";
	private static final String PATH_SAME_SIZE_FOLDER = "src/test/resources/noDuplicates";
	private static final String PATH_EMPTY_FOLDER = "src/test/resources/emptyDirectory";
	private static final String PATH_PLENTY_SAME_SIZE_FOLDER = "src/test/resources/";
	private static final String PATH_SAME_SIZE_IN_FLAT_FOLDER = "src/test/resources/directoryOnlyDirectory/flatDuplicateTree";
	private static final String PATH_FOLDER_ONLY_FOLDER = "src/test/resources/directoryOnlyDirectory";
	private static final String PATH_UNREADABLE_WINDOWS = "C:\\SYSTEM VOLUME INFORMATION";
	private static final String PATH_UNREADABLE_LINUX = "/root";
	private static final DuplicateLengthFinderCallback FAILING_DLF_CALLBACK = new FailingDuplicateLengthFinderCallback();

	@Test
	public void noArgumentDirectory() {
		try {
			DuplicateLengthFinder.getResult(null);
			fail("Missing Parameter must be notified");
		} catch (IllegalArgumentException e){
			// Expected Exception
		} catch (Exception e) {
			fail("Exception should not occur " + e.getLocalizedMessage());
		}
	}

	@Test
	public void noArgumentCallback() {
		try {
			DuplicateLengthFinder.getResult(new File("."), null);
			fail("Missing Parameter must be notified");
		} catch (IllegalArgumentException e){
			// Expected Exception
		} catch (Exception e) {
			fail("Exception should not occur " + e.getLocalizedMessage());
		}
	}

	@Test
	public void noArgumentDirectoryButCallback() {
		try {
			DuplicateLengthFinder.getResult(null, FAILING_DLF_CALLBACK);
			fail("Missing Parameter must be notified");
		} catch (IllegalArgumentException e){
			// Expected Exception
		} catch (Exception e) {
			fail("Exception should not occur " + e.getLocalizedMessage());
		}
	}

	@Test
	public void scanInvalidDirectory() {
		try {
			final File directory = new File(PATH_INVALID_FOLDER);
			DuplicateLengthFinder.getResult(directory);
			fail("Missing Parameter must be notified");
		} catch (IllegalArgumentException e){
			// Expected Exception
		} catch (Exception e) {
			fail("Exception should not occur " + e.getLocalizedMessage());
		}
	}

	@Test
	public void scanFile() {
		try {
			final File directory = new File(PATH_FILE);
			DuplicateLengthFinder.getResult(directory);
			fail("Missing Parameter must be notified");
		} catch (IllegalArgumentException e){
			// Expected Exception
		} catch (Exception e) {
			fail("Exception should not occur " + e.getLocalizedMessage());
		}
	}

	@Test
	public void scanFlatDirectory() {
		final File directory = new File(PATH_SAME_SIZE_IN_FLAT_FOLDER);
		final Map<Long, List<File>> result = DuplicateLengthFinder.getResult(directory);
		assertNotNull(result);
		assertEquals(1, result.values().size());
		assertEquals(2, result.values().iterator().next().size());
	}

	@Test
	public void scanDirectoryOnlyDirectory() {
		final File directory = new File(PATH_FOLDER_ONLY_FOLDER);
		final Map<Long, List<File>> result = DuplicateLengthFinder.getResult(directory);
		assertNotNull(result);
		assertEquals(1, result.values().size());
		assertEquals(2, result.values().iterator().next().size());
	}

	@Test
	public void scanEmptyDirectory() {
		final File directory = new File(PATH_EMPTY_FOLDER);
		if (directory.mkdir()) {
			final Map<Long, List<File>> result = DuplicateLengthFinder.getResult(directory);
			assertNotNull(result);
			assertTrue(result.values().isEmpty());
			directory.delete();
		}
	}

	@Test
	public void scanUnreadableDirectory() {
		File directory = null;
		if (System.getProperty("os.name").contains("Linux")) {
			directory = new File(PATH_UNREADABLE_LINUX);
		} else if (System.getProperty("os.name").contains("Windows")) {
			directory = new File(PATH_UNREADABLE_WINDOWS);
		} else {
			System.err.println("OS calls itself: " + System.getProperty("os.name"));
			assumeTrue(false);
		}
		List<String> unreadables = new ArrayList<>();
		DuplicateLengthFinder.getResult(directory, new FailingDuplicateLengthFinderCallback() {
			
			@Override
			public void unreadableDirectory(String directory) {
				unreadables.add(directory);
			}
			
			@Override
			public void enteredNewDirectory(File directory) {
			}
		});
		
		assertEquals(1, unreadables.size());
		assertEquals(directory.getAbsolutePath(), unreadables.get(0));

	}

	@Test
	public void scanNoDuplicates() {
		final File directory = new File(PATH_NO_SAME_SIZE_FOLDER);
		final Map<Long, List<File>> result = DuplicateLengthFinder.getResult(directory);
		assertNotNull(result);
		assertEquals(1, result.values().size());
		assertEquals(1, result.values().iterator().next().size());
	}
	
	@Test
	public void scanDuplicates() {
		final File directory = new File(PATH_SAME_SIZE_FOLDER);
		final Map<Long, List<File>> result = DuplicateLengthFinder.getResult(directory);
		assertNotNull(result);
		assertEquals(1, result.values().size());
		assertEquals(2, result.values().iterator().next().size());
	}

	@Test
	public void scanDuplicatesInTree() {
		final File directory = new File(PATH_SAME_SIZE_FILES_IN_TREE_FOLDER);
		final Map<Long, List<File>> result = DuplicateLengthFinder.getResult(directory);
		assertNotNull(result);
		Iterator<List<File>> elementsIterator = result.values().iterator();
		assertEquals(1, result.values().size());
		assertEquals(2, elementsIterator.next().size());
	}

	@Test
	public void scanDuplicatesInBiggerTreeWithCallback() {
		final File directory = new File(PATH_PLENTY_SAME_SIZE_FOLDER);
		List<String> directoriesEntered = new ArrayList<>();
		DuplicateLengthFinderCallback callback = new FailingDuplicateLengthFinderCallback() {

			@Override
			public void enteredNewDirectory(File directory) {
				try {
					directoriesEntered.add(directory.getCanonicalPath());
				} catch (IOException e) {
					fail(e.getLocalizedMessage());
				}
			}
		};

		Map<Long, List<File>> result = DuplicateLengthFinder.getResult(directory, callback);

		assertEquals(3, result.values().size());
		Iterator<List<File>> elementsIterator = result.values().iterator();
		assertEquals(2, elementsIterator.next().size());
		assertEquals(6, elementsIterator.next().size());
		assertEquals(1, elementsIterator.next().size());
		assertEquals(6, directoriesEntered.size());
		assertListContainsLineEndingWith(directoriesEntered, "resources");
		assertListContainsLineEndingWith(directoriesEntered, "duplicateTree");
		assertListContainsLineEndingWith(directoriesEntered, "subdirectory");
		assertListContainsLineEndingWith(directoriesEntered, "directoryOnlyDirectory");
		assertListContainsLineEndingWith(directoriesEntered, "flatDuplicateTree");
		assertListContainsLineEndingWith(directoriesEntered, "noDuplicates");

	}

	@Test
	public void scanDuplicatesInBiggerTreeWithDirectory() {
		final File directory = new File(PATH_PLENTY_SAME_SIZE_FOLDER);
		final Map<Long, List<File>> result = DuplicateLengthFinder.getResult(directory);
		assertNotNull(result);
		assertEquals(3, result.values().size());
		Iterator<List<File>> elementsIterator = result.values().iterator();
		assertEquals(2, elementsIterator.next().size());
		assertEquals(6, elementsIterator.next().size());
		assertEquals(1, elementsIterator.next().size());
	}
}
