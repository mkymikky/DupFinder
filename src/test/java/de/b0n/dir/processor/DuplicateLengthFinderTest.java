package de.b0n.dir.processor;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class DuplicateLengthFinderTest extends de.b0n.dir.Test {
	private static final String PATH_SAME_SIZE_FILES_IN_TREE_FOLDER = "src/test/resources/duplicateTree";
	private static final String PATH_FILE = "src/test/resources/Test1.txt";
	private static final String PATH_INVALID_FOLDER = "src/test/resourcesInvalid/";
	private static final String PATH_NO_SAME_SIZE_FOLDER = "src/test/resources/duplicateTree/subfolder";
	private static final String PATH_SAME_SIZE_FOLDER = "src/test/resources/noDuplicates";
	private static final String PATH_EMPTY_FOLDER = "src/test/resources/emptyFolder";
	private static final String PATH_PLENTY_SAME_SIZE_FOLDER = "src/test/resources/";
	private static final String PATH_SAME_SIZE_IN_FLAT_FOLDER = "src/test/resources/folderOnlyFolder/flatDuplicateTree";
	private static final String PATH_FOLDER_ONLY_FOLDER = "src/test/resources/folderOnlyFolder";
	private static final DuplicateLengthFinderCallback FAILING_DLF_CALLBACK = new FailingDuplicateLengthFinderCallback();

	@Test
	public void noArgumentFolder() {
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
	public void noArgumentFolderButCallback() {
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
	public void scanInvalidFolder() {
		try {
			final File folder = new File(PATH_INVALID_FOLDER);
			DuplicateLengthFinder.getResult(folder);
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
			final File folder = new File(PATH_FILE);
			DuplicateLengthFinder.getResult(folder);
			fail("Missing Parameter must be notified");
		} catch (IllegalArgumentException e){
			// Expected Exception
		} catch (Exception e) {
			fail("Exception should not occur " + e.getLocalizedMessage());
		}
	}

	@Test
	public void scanFlatFolder() {
		final File folder = new File(PATH_SAME_SIZE_IN_FLAT_FOLDER);
		final Map<Long, List<File>> result = DuplicateLengthFinder.getResult(folder);
		assertNotNull(result);
		assertEquals(1, result.values().size());
		assertEquals(2, result.values().iterator().next().size());
	}

	@Test
	public void scanFolderOnlyFolder() {
		final File folder = new File(PATH_FOLDER_ONLY_FOLDER);
		final Map<Long, List<File>> result = DuplicateLengthFinder.getResult(folder);
		assertNotNull(result);
		assertEquals(1, result.values().size());
		assertEquals(2, result.values().iterator().next().size());
	}

	@Test
	public void scanEmptyFolder() {
		final File folder = new File(PATH_EMPTY_FOLDER);
		if (folder.mkdir()) {
			final Map<Long, List<File>> result = DuplicateLengthFinder.getResult(folder);
			assertNotNull(result);
			assertTrue(result.values().isEmpty());
			folder.delete();
		}
	}

	@Test
	public void scanUnreadableFolder() {
		System.err.println("OS calls itself: " + System.getProperty("os.name"));
		assumeTrue(System.getProperty("os.name").contains("Linux"));
		File folder = new File("/root");
		List<File> unreadables = new ArrayList<>();
		DuplicateLengthFinder.getResult(folder, new FailingDuplicateLengthFinderCallback() {
			
			@Override
			public void unreadableFolder(File folder) {
				unreadables.add(folder);
			}
			
			@Override
			public void enteredNewFolder(File folder) {
			}
		});
		
		assertEquals(1, unreadables.size());
		assertEquals(folder.getAbsolutePath(), unreadables.get(0).getAbsolutePath());

	}

	@Test
	public void scanNoDuplicates() {
		final File folder = new File(PATH_NO_SAME_SIZE_FOLDER);
		final Map<Long, List<File>> result = DuplicateLengthFinder.getResult(folder);
		assertNotNull(result);
		assertEquals(1, result.values().size());
		assertEquals(1, result.values().iterator().next().size());
	}
	
	@Test
	public void scanDuplicates() {
		final File folder = new File(PATH_SAME_SIZE_FOLDER);
		final Map<Long, List<File>> result = DuplicateLengthFinder.getResult(folder);
		assertNotNull(result);
		assertEquals(1, result.values().size());
		assertEquals(2, result.values().iterator().next().size());
	}

	@Test
	public void scanDuplicatesInTree() {
		final File folder = new File(PATH_SAME_SIZE_FILES_IN_TREE_FOLDER);
		final Map<Long, List<File>> result = DuplicateLengthFinder.getResult(folder);
		assertNotNull(result);
		Iterator<List<File>> elementsIterator = result.values().iterator();
		assertEquals(1, result.values().size());
		assertEquals(2, elementsIterator.next().size());
	}

	@Test
	public void scanDuplicatesInBiggerTreeWithCallback() {
		final File folder = new File(PATH_PLENTY_SAME_SIZE_FOLDER);
		List<String> foldersEntered = new ArrayList<>();
		Map<Long, List<File>> result = new HashMap<>();
		DuplicateLengthFinderCallback callback = new FailingDuplicateLengthFinderCallback() {

			@Override
			public void enteredNewFolder(File folder) {
				try {
					foldersEntered.add(folder.getCanonicalPath());
				} catch (IOException e) {
					fail(e.getLocalizedMessage());
				}
			}

			@Override
			public void addGroupedElement(Long size, File file) {
				synchronized (this) {
					result.computeIfAbsent(size, list -> new ArrayList<>()).add(file);
				}
			}
		};

		DuplicateLengthFinder.getResult(folder, callback);

		assertEquals(3, result.values().size());
		Iterator<List<File>> elementsIterator = result.values().iterator();
		assertEquals(2, elementsIterator.next().size());
		assertEquals(6, elementsIterator.next().size());
		assertEquals(1, elementsIterator.next().size());
		assertEquals(6, foldersEntered.size());
		assertListContainsLineEndingWith(foldersEntered, "resources");
		assertListContainsLineEndingWith(foldersEntered, "duplicateTree");
		assertListContainsLineEndingWith(foldersEntered, "subfolder");
		assertListContainsLineEndingWith(foldersEntered, "folderOnlyFolder");
		assertListContainsLineEndingWith(foldersEntered, "flatDuplicateTree");
		assertListContainsLineEndingWith(foldersEntered, "noDuplicates");

	}

	@Test
	public void scanDuplicatesInBiggerTreeWithFolder() {
		final File folder = new File(PATH_PLENTY_SAME_SIZE_FOLDER);
		final Map<Long, List<File>> result = DuplicateLengthFinder.getResult(folder);
		assertNotNull(result);
		assertEquals(3, result.values().size());
		Iterator<List<File>> elementsIterator = result.values().iterator();
		assertEquals(2, elementsIterator.next().size());
		assertEquals(6, elementsIterator.next().size());
		assertEquals(1, elementsIterator.next().size());
	}
}
