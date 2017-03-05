//package de.b0n.dir.processor;
//
//import static org.junit.Assert.*;
//import static org.junit.Assume.*;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Queue;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//import de.b0n.dir.processor.DuplicateLengthFinderCallback;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
///**
// * Created by huluvu424242 on 07.01.17.
// */
//public class DuplicateLengthFinderTest extends de.b0n.dir.Test {
//
//	private static final String PATH_SAME_SIZE_FILES_IN_TREE_FOLDER = "src/test/resources/duplicateTree";
//	private static final String PATH_FILE = "src/test/resources/Test1.txt";
//	private static final String PATH_INVALID_FOLDER = "src/test/resourcesInvalid/";
//	private static final String PATH_NO_SAME_SIZE_FOLDER = "src/test/resources/noDuplicates";
//	private static final String PATH_EMPTY_FOLDER = "src/test/resources/emptyFolder";
//	private static final String PATH_PLENTY_SAME_SIZE_FOLDER = "src/test/resources/";
//	private static final String PATH_SAME_SIZE_IN_FLAT_FOLDER = "src/test/resources/folderOnlyFolder/flatDuplicateTree";
//	private static final String PATH_FOLDER_ONLY_FOLDER = "src/test/resources/folderOnlyFolder";
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
//	@Test(expected = IllegalArgumentException.class)
//	public void noArgumentsFolder() {
//		DuplicateLengthFinder.readFilesRecursiveOf(null);
//	}
//
//	@Test(expected = IllegalArgumentException.class)
//	public void noThreadPool() {
//		ExecutorService executorService = null;
//		DuplicateLengthFinder.readFilesRecursiveOf(new File("."), executorService);
//	}
//
//	@Test
//	public void noCallback() {
//		DuplicateLengthFinderCallback duplicateLengthFinderCallback = null;
//		DuplicateLengthFinder.readFilesRecursiveOf(new File("."), duplicateLengthFinderCallback);
//	}
//
//	@Test(expected = IllegalArgumentException.class)
//	public void noFolderButThreadPool() {
//		final ExecutorService threadPool = Executors.newWorkStealingPool();
//		DuplicateLengthFinder.readFilesRecursiveOf(null, threadPool);
//	}
//
//	@Test(expected = IllegalArgumentException.class)
//	public void noFolderButCallback() {
//		DuplicateLengthFinder.readFilesRecursiveOf(null, new DuplicateLengthFinderCallback() {
//			@Override
//			public void enteredNewFolder(File file) {
//				fail();
//			}
//
//			@Override
//			public void unreadableFolder(File folder) {
//				fail();
//			}
//		});
//	}
//
//	@Test(expected = IllegalArgumentException.class)
//	public void scanInvalidFolder() {
//		final File folder = new File(PATH_INVALID_FOLDER);
//		DuplicateLengthFinder.readFilesRecursiveOf(folder);
//	}
//
//	@Test(expected = IllegalArgumentException.class)
//	public void scanFile() {
//		final File folder = new File(PATH_FILE);
//		DuplicateLengthFinder.readFilesRecursiveOf(folder);
//	}
//
//	@Test
//	public void scanFlatFolder() {
//		final File folder = new File(PATH_SAME_SIZE_IN_FLAT_FOLDER);
//		final AbstractModel<Long, File> result = DuplicateLengthFinder.readFilesRecursiveOf(folder);
//		assertNotNull(result);
//		assertEquals("falsche Anzahl an Dateien gleicher Größe bestimmt", 1, result.values().size());
//		assertEquals("falsche Anzahl von 26 Byte-Datei Vorkommen bestimmt", 2,
//				result.values().iterator().next().size());
//	}
//
//	@Test
//	public void scanFolderOnlyFolder() {
//		final File folder = new File(PATH_FOLDER_ONLY_FOLDER);
//		final AbstractModel<Long, File> result = DuplicateLengthFinder.readFilesRecursiveOf(folder);
//		assertNotNull(result);
//		assertEquals("falsche Anzahl an Dateien gleicher Größe bestimmt", 1, result.values().size());
//		assertEquals("falsche Anzahl von 26 Byte-Datei Vorkommen bestimmt", 2,
//				result.values().iterator().next().size());
//	}
//
//	@Test
//	public void scanEmptyFolder() {
//		final File folder = new File(PATH_EMPTY_FOLDER);
//		if (folder.mkdir()) {
//			final AbstractModel<Long, File> result = DuplicateLengthFinder.readFilesRecursiveOf(folder);
//			assertNotNull(result);
//			assertEquals(0, result.values().size());
//			folder.delete();
//		}
//	}
//
//	@Test(expected = IllegalArgumentException.class)
//	public void scanUnreadableFolder() {
//		System.err.println("OS calls itself: " + System.getProperty("os.name"));
//		assumeTrue(System.getProperty("os.name").contains("Linux"));
//		File folder = new File("/root");
//		DuplicateLengthFinder.readFilesRecursiveOf(folder);
//	}
//
//	@Test
//	public void scanNoDuplicates() {
//		final File folder = new File(PATH_NO_SAME_SIZE_FOLDER);
//		final AbstractModel<Long, File> result = DuplicateLengthFinder.readFilesRecursiveOf(folder);
//		assertNotNull(result);
//		assertEquals("Es darf nur eine Gruppe gefunden werden", 1, result.values().size());
//		assertEquals("In der gefundenen Gruppe darf nur ein Element sein", 1, result.values().iterator().next().size());
//
//	}
//
//	@Test
//	public void scanDuplicatesInTree() {
//		final File folder = new File(PATH_SAME_SIZE_FILES_IN_TREE_FOLDER);
//		final AbstractModel<Long, File> result = DuplicateLengthFinder.readFilesRecursiveOf(folder);
//		assertNotNull(result);
//		Iterator<Queue<File>> elementsIterator = result.values().iterator();
//		assertEquals(1, result.values().size());
//		assertEquals(2, elementsIterator.next().size());
//	}
//
//	@Test
//	public void scanDuplicatesInBiggerTreeWithCallback() {
//		final File folder = new File(PATH_PLENTY_SAME_SIZE_FOLDER);
//		List<String> foldersEntered = new ArrayList<String>();
//		final AbstractModel<Long, File> result = DuplicateLengthFinder.readFilesRecursiveOf(folder, new DuplicateLengthFinderCallback() {
//
//			@Override
//			public void enteredNewFolder(File folder) {
//				try {
//					foldersEntered.add(folder.getCanonicalPath());
//				} catch (IOException e) {
//					fail();
//				}
//			}
//
//			@Override
//			public void unreadableFolder(File folder) {
//				fail();
//			}
//		});
//		assertNotNull(result);
//		assertEquals("falsche Anzahl an Dateien gleicher Größe bestimmt", 2, result.values().size());
//		Iterator<Queue<File>> elementsIterator = result.values().iterator();
//		assertEquals("falsche Anzahl von 26 Byte-Datei Vorkommen bestimmt", 2, elementsIterator.next().size());
//		assertEquals("falsche Anzahl von 91 Byte-Datei Vorkommen bestimmt", 4, elementsIterator.next().size());
//		assertEquals(5, foldersEntered.size());
//		assertListContainsLineEndingWith(foldersEntered, "duplicateTree");
//		assertListContainsLineEndingWith(foldersEntered, "subfolder");
//		assertListContainsLineEndingWith(foldersEntered, "folderOnlyFolder");
//		assertListContainsLineEndingWith(foldersEntered, "flatDuplicateTree");
//		assertListContainsLineEndingWith(foldersEntered, "noDuplicates");
//
//	}
//
//	@Test
//	public void scanDuplicatesInBiggerTreeWithFolder() {
//		final File folder = new File(PATH_PLENTY_SAME_SIZE_FOLDER);
//		final AbstractModel<Long, File> result = DuplicateLengthFinder.readFilesRecursiveOf(folder);
//		assertNotNull(result);
//		assertEquals("falsche Anzahl an Dateien gleicher Größe bestimmt", 2, result.values().size());
//		Iterator<Queue<File>> elementsIterator = result.values().iterator();
//		assertEquals("falsche Anzahl von 26 Byte-Datei Vorkommen bestimmt", 2, elementsIterator.next().size());
//		assertEquals("falsche Anzahl von 91 Byte-Datei Vorkommen bestimmt", 4, elementsIterator.next().size());
//	}
//
//	@Test
//	public void scanDuplicatesInBiggerTreeWithThreadPool() {
//		final File folder = new File(PATH_PLENTY_SAME_SIZE_FOLDER);
//		final ExecutorService threadPool = Executors.newWorkStealingPool();
//		final AbstractModel<Long, File> result = DuplicateLengthFinder.readFilesRecursiveOf(folder, threadPool);
//		assertNotNull(result);
//		assertEquals("falsche Anzahl an Dateien gleicher Größe bestimmt", 2, result.values().size());
//		Iterator<Queue<File>> elementsIterator = result.values().iterator();
//		assertEquals("falsche Anzahl von 26 Byte-Datei Vorkommen bestimmt", 2, elementsIterator.next().size());
//		assertEquals("falsche Anzahl von 91 Byte-Datei Vorkommen bestimmt", 4, elementsIterator.next().size());
//	}
//}
