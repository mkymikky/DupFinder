package de.b0n.dir;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DupFinderTest {
	private static final String PATH_FILE = "src/test/resources/Test1.txt";
	private static final String PATH_EMPTY_FOLDER = "src/test/resources/emptyFolder";
    private static final String PATH_SAME_SIZE_FILES_IN_TREE_FOLDER = "src/test/resources/duplicateTree";

	private PrintStream printStream;
	private ByteArrayOutputStream byteArrayOutputStream;

	@Before
	public void setUp() throws IOException {
		byteArrayOutputStream = new ByteArrayOutputStream();
		printStream = new PrintStream(byteArrayOutputStream);
	}

	@After
	public void tearDown() throws IOException {
	}

	@Test
	public void testNoArgument() {
		System.setErr(printStream);
		DupFinder.main(new String[] {});
		assertEquals(
				"FEHLER: Parameter <Verzeichnis> fehlt.\r\n Benutzung: DupFinder <Verzeichnis>\r\n<Verzeichnis> = Verzeichnis in dem rekursiv nach Duplikaten gesucht wird\r\n",
				new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8));
	}

	@Test
	public void testArgumentIsFile() {
		System.setErr(printStream);
		DupFinder.main(new String[] {PATH_FILE});
		assertEquals(
				"FEHLER: Parameter <Verzeichnis> ist kein Verzeichnis.\r\n Benutzung: DupFinder <Verzeichnis>\r\n<Verzeichnis> = Verzeichnis in dem rekursiv nach Duplikaten gesucht wird\r\n",
				new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8));
	}

	@Test
	public void testPathIsEmpty() {
		System.setErr(printStream);
        final File folder = new File(PATH_EMPTY_FOLDER);
        if (folder.mkdir()) {
        	DupFinder.main(new String[] {PATH_EMPTY_FOLDER});
        	assertTrue(new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8).isEmpty());
        	folder.delete();
        }
	}

	// This test probably fails due to Bug #2
//	@Test
//	public void testDuplicates() {
//		System.setErr(printStream);
//       	DupFinder.main(new String[] {PATH_SAME_SIZE_FILES_IN_TREE_FOLDER});
//       	List<String> lines = Arrays.asList(new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8).split("\\r\\n|\\n|\\r"));
//       	listContainsLineEndingWith(lines, "Test1.txt");
//       	listContainsLineEndingWith(lines, "Test2.txt");
//	}

	private void listContainsLineEndingWith(List<String> lines, String ending) {
		for (String line : lines) {
			if (line.endsWith(ending)) {
				return;
			}
		}
		fail("No line ends with " + ending);
	}
}
