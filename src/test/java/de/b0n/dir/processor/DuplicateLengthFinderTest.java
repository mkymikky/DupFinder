package de.b0n.dir.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.junit.Test;

public class DuplicateLengthFinderTest extends de.b0n.dir.Test {
	
	private static final Predicate<Entry<Long, List<File>>> hasSingleItemInEntry = entry -> entry.getValue()
			.size() < 2;

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
		DuplicateLengthFinder.getResult(null, FAILING_DLF_CALLBACK);
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
		final Map<Long, List<File>> result = DuplicateLengthFinder.getResult(folder);
		assertNotNull(result);
		assertEquals("falsche Anzahl an Dateien gleicher Größe bestimmt", 1, result.values().size());
		assertEquals("falsche Anzahl von 26 Byte-Datei Vorkommen bestimmt", 2,
				result.values().iterator().next().size());
	}

	@Test
	public void scanFolderOnlyFolder() {
		final File folder = new File(PATH_FOLDER_ONLY_FOLDER);
		final Map<Long, List<File>> result = DuplicateLengthFinder.getResult(folder);
		assertNotNull(result);
		assertEquals("falsche Anzahl an Dateien gleicher Größe bestimmt", 1, result.values().size());
		assertEquals("falsche Anzahl von 26 Byte-Datei Vorkommen bestimmt", 2,
				result.values().iterator().next().size());
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
		assertEquals("Es darf nur eine Gruppe gefunden werden", 1, result.values().size());
		assertEquals("In der gefundenen Gruppe darf nur ein Element sein", 1, result.values().iterator().next().size());
	}
	
	@Test
	public void scanDuplicates() {
		final File folder = new File(PATH_SAME_SIZE_FOLDER);
		final Map<Long, List<File>> result = DuplicateLengthFinder.getResult(folder);
		assertNotNull(result);
		assertEquals("Es darf nur eine Gruppe gefunden werden", 1, result.values().size());
		assertEquals("In der gefundenen Gruppe dürfen nur zwei Element sein", 2, result.values().iterator().next().size());
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
		List<String> foldersEntered = new ArrayList<String>();
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
					List<File> group = result.computeIfAbsent(size, k -> new ArrayList<File>());
					group.add(file);
				}
			}
		};

		DuplicateLengthFinder.getResult(folder, callback);
		result.entrySet().parallelStream().filter(hasSingleItemInEntry).forEach(entry -> result.remove(entry.getKey()));

		assertEquals("falsche Anzahl an Dateien gleicher Größe bestimmt", 2, result.values().size());
		Iterator<List<File>> elementsIterator = result.values().iterator();
		assertEquals("falsche Anzahl von 26 Byte-Datei Vorkommen bestimmt", 2, elementsIterator.next().size());
		assertEquals("falsche Anzahl von 91 Byte-Datei Vorkommen bestimmt", 6, elementsIterator.next().size());
		StringBuilder enteredFolders = new StringBuilder();
		for (String string : foldersEntered) {
			enteredFolders.append(string).append("\n");
		}
		assertEquals("Following Folders have been entered:\n" + enteredFolders + "6 should have been entered", 6,
				foldersEntered.size());
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
		assertEquals("falsche Anzahl an Dateien gleicher Größe bestimmt", 2, result.values().size());
		Iterator<List<File>> elementsIterator = result.values().iterator();
		assertEquals("falsche Anzahl von 26 Byte-Datei Vorkommen bestimmt", 2, elementsIterator.next().size());
		assertEquals("falsche Anzahl von 91 Byte-Datei Vorkommen bestimmt", 6, elementsIterator.next().size());
	}
}
