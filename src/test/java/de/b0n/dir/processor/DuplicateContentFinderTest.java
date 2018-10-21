package de.b0n.dir.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DuplicateContentFinderTest {

	private static final String PATH_FILE_1 = "src/test/resources/Test1.txt";
	private static final String PATH_FILE_2 = "src/test/resources/noDuplicates/Test1.txt";

	private static DuplicateContentFinderCallback FAILING_DCF_CALLBACK = new FailingDuplicateContentFinderCallback();

	@Before
	public void setUp() {

	}

	@After
	public void tearDown() {

	}

	@Test(expected = IllegalArgumentException.class)
	public void noArguments() {
		DuplicateContentFinder.getResult(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void noArgumentCallback() {
		final Queue<File> input = new ConcurrentLinkedQueue<File>();
		DuplicateContentFinder.getResult(input, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void noArgumentInput() {
		DuplicateContentFinder.getResult(null, FAILING_DCF_CALLBACK);
	}

	@Test
	public void scanEmptyInputWithoutCallback() {
		final Queue<File> input = new ConcurrentLinkedQueue<File>();
		final Queue<List<File>> output = DuplicateContentFinder.getResult(input);
		assertNotNull("Es muss ein Ergebnis zurück gegeben werden", output);
		assertTrue("Es muss ein leeres Ergebnis zurück gegeben werden: " + output.size(), output.isEmpty());
	}

	@Test
	public void scanEmptyInputWithCallback() {
		final Queue<File> input = new ConcurrentLinkedQueue<File>();
		DuplicateContentFinder.getResult(input, FAILING_DCF_CALLBACK);
	}

	@Test
	public void scanSingleInput() {
		final File file = new File(PATH_FILE_1);
		final Queue<File> input = new ConcurrentLinkedQueue<File>();
		input.add(file);
		final Queue<List<File>> output = DuplicateContentFinder.getResult(input);
		assertNotNull("Es muss ein Ergebnis zurück gegeben werden", output);
		assertTrue("Es muss ein leeres Ergebnis zurück gegeben werden", output.isEmpty());
	}

	@Test
	public void scanDuplicateInput() {
		final File file1 = new File(PATH_FILE_1);
		final File file2 = new File(PATH_FILE_2);
		final Queue<File> input = new ConcurrentLinkedQueue<File>();
		input.add(file1);
		input.add(file2);
		final Queue<List<File>> output = DuplicateContentFinder.getResult(input);
		assertNotNull("Es muss ein Ergebnis zurück gegeben werden", output);
		assertEquals("Es muss ein Ergebnis mit zwei Dubletten zurück gegeben werden", 1, output.size());
		assertEquals("Es muss ein Ergebnis mit zwei Dubletten zurück gegeben werden", 2, output.peek().size());
		assertTrue("Es muss ein Ergebnis Test1.txt zurück gegeben werden",
				output.peek().get(0).getAbsolutePath().endsWith("Test1.txt"));
	}

	@Test
	public void scanDuplicateInputWithCallback() {
		final List<List<File>> duplicateList = new ArrayList<>();
		final File file1 = new File(PATH_FILE_1);
		final File file2 = new File(PATH_FILE_2);
		final Queue<File> input = new ConcurrentLinkedQueue<File>();
		input.add(file1);
		input.add(file2);
		DuplicateContentFinderCallback callback = new FailingDuplicateContentFinderCallback() {

			@Override
			public void duplicateGroup(List<File> duplicateFiles) {
				duplicateList.add(duplicateFiles);
			}
		};

		DuplicateContentFinder.getResult(input, callback);

		assertEquals(1, duplicateList.size());
		assertEquals(2, duplicateList.get(0).size());
		assertTrue("Es muss ein Ergebnis Test1.txt zurück gegeben werden",
				duplicateList.get(0).get(0).getAbsolutePath().endsWith("Test1.txt"));
		assertTrue(duplicateList.get(0).contains(file1));
		assertTrue(duplicateList.get(0).contains(file2));
	}
}
