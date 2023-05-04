package de.b0n.dir;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class DupFinderConsoleTest extends de.b0n.dir.Test {
	private static final String PATH_FILE = "src/test/resources/Test1.txt";
	private static final String PATH_EMPTY_FOLDER = "src/test/resources/emptyFolder";
    private static final String PATH_SAME_SIZE_FILES_IN_TREE_FOLDER = "src/test/resources/duplicateTree";

	private PrintStream printStream;
	private ByteArrayOutputStream byteArrayOutputStream;

	@Before
	public void setUp() {
		byteArrayOutputStream = new ByteArrayOutputStream();
		printStream = new PrintStream(byteArrayOutputStream);
	}

	@Test
	public void testNoArgument() {
		System.setErr(printStream);
		DupFinderConsole.main(new String[] {});
		assertEquals(
				"FEHLER: Parameter <Verzeichnis> fehlt.\r\n Benutzung: DupFinder <Verzeichnis>\r\n<Verzeichnis> = Verzeichnis in dem rekursiv nach Duplikaten gesucht wird",
				byteArrayOutputStream.toString(StandardCharsets.UTF_8).trim());
	}

	@Test
	public void testArgumentIsFile() {
		System.setErr(printStream);
		DupFinderConsole.main(new String[] {PATH_FILE});
		assertEquals(
				"FEHLER: Parameter <Verzeichnis> ist kein Verzeichnis.\r\n Benutzung: DupFinder <Verzeichnis>\r\n<Verzeichnis> = Verzeichnis in dem rekursiv nach Duplikaten gesucht wird",
				byteArrayOutputStream.toString(StandardCharsets.UTF_8).trim());
	}

	@Test
	public void testPathIsEmpty() {
		System.setErr(printStream);
        final File folder = new File(PATH_EMPTY_FOLDER);
        assumeTrue(folder.mkdir());
    	DupFinderConsole.main(new String[] {PATH_EMPTY_FOLDER});
    	assertTrue(byteArrayOutputStream.toString(StandardCharsets.UTF_8).isEmpty());
    	folder.delete();
	}

	@Test
	public void testDuplicates() {
		System.setOut(printStream);
       	DupFinderConsole.main(new String[] {PATH_SAME_SIZE_FILES_IN_TREE_FOLDER});
       	String output = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
       	List<String> lines = Arrays.asList(output.split("\\r\\n|\\n|\\r"));
       	assertListContainsLineEndingWith(lines, "Test1.txt");
       	assertListContainsLineEndingWith(lines, "Test2.txt");
	}
}
