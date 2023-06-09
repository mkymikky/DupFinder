package de.b0n.dir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DupFinderConsoleTest extends de.b0n.dir.Test {
	private static final String PATH_FILE = "src/test/resources/Test1.txt";
	private static final String PATH_EMPTY_FOLDER = "src/test/resources/emptyDirectory";

	private PrintStream printStream;
	private ByteArrayOutputStream byteArrayOutputStream;

	@BeforeEach
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
        final File directory = new File(PATH_EMPTY_FOLDER);
        assumeTrue(directory.mkdir());
    	DupFinderConsole.main(new String[] {PATH_EMPTY_FOLDER});
    	assertTrue(byteArrayOutputStream.toString(StandardCharsets.UTF_8).isEmpty());
    	directory.delete();
	}
}
