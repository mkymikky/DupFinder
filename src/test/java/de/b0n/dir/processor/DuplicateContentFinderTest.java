package de.b0n.dir.processor;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static java.util.List.of;

import java.io.File;
import java.io.Serial;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.jupiter.api.Test;

public class DuplicateContentFinderTest {

	private static final String PATH_FILE_1A = "src/test/resources/Test1.txt";
	private static final String PATH_FILE_1B = "src/test/resources/noDuplicates/Test1.txt";
	private static final String PATH_FILE_2A = "src/test/resources/Test2.txt";
	private static final String PATH_FILE_2B = "src/test/resources/noDuplicates/Test2.txt";

	private static final DuplicateContentFinderCallback FAILING_DCF_CALLBACK = new FailingDuplicateContentFinderCallback();

	@Test
	public void noArguments() {
		try {
			DuplicateContentFinder.getResult(null);
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
			final Queue<File> input = new ConcurrentLinkedQueue<>();
			DuplicateContentFinder.getResult(input, null);
			fail("Missing Parameter must be notified");
		} catch (IllegalArgumentException e){
			// Expected Exception
		} catch (Exception e) {
			fail("Exception should not occur " + e.getLocalizedMessage());
		}
	}

	@Test
	public void noArgumentInput() {
		try {
			DuplicateContentFinder.getResult(null, FAILING_DCF_CALLBACK);
			fail("Missing Parameter must be notified");
		} catch (IllegalArgumentException e){
			// Expected Exception
		} catch (Exception e) {
			fail("Exception should not occur " + e.getLocalizedMessage());
		}
	}

	@Test
	public void scanEmptyInput() {
		try {
			DuplicateContentFinder.getResult(emptyList());
			fail("Missing Parameter must be notified");
		} catch (IllegalArgumentException e){
			// Expected Exception
		} catch (Exception e) {
			fail("Exception should not occur " + e.getLocalizedMessage());
		}
	}

	@Test
	public void scanEmptyInputWithCallback() {
		try {
			DuplicateContentFinder.getResult(emptyList(), FAILING_DCF_CALLBACK);
			fail("Missing Parameter must be notified");
		} catch (IllegalArgumentException e){
			// Expected Exception
		} catch (Exception e) {
			fail("Exception should not occur " + e.getLocalizedMessage());
		}
	}

	@Test
	public void scanFailingInputWithCallback() {
		List<File> failFiles = new ArrayList<>();
		DuplicateContentFinderCallback callback = new FailingDuplicateContentFinderCallback() {
			@Override
			public void failedFile(File failedFile) {
				failFiles.add(failedFile);
			}
		};

		final File file = new File(PATH_FILE_1A) {
			@Serial
			private static final long serialVersionUID = 1L;
			public String getPath() {
				throw new IllegalStateException("Du kannst nix lesen!");
			}
		};

		final List<List<File>> duplicateFilesLists = DuplicateContentFinder.getResult(of(
					file,
					new File(PATH_FILE_1B),
					new File(PATH_FILE_1B)),
				callback).toList();
		assertEquals(1, failFiles.size());
		assertEquals(1, duplicateFilesLists.size());
		assertEquals(2, duplicateFilesLists.get(0).size());
	}

	@Test
	public void scanSingleInput() {
		final List<List<File>> output = DuplicateContentFinder.getResult(of(
				new File(PATH_FILE_1A))).toList();
		assertNotNull(output);
		assertTrue(output.isEmpty());
	}

	@Test
	public void scanSingleDuplicateInput() {
		final List<List<File>> output = DuplicateContentFinder.getResult(of(
				new File(PATH_FILE_1A),
				new File(PATH_FILE_1B))).toList();
		assertEquals(1, output.size());
		assertEquals(2, output.get(0).size());
		assertTrue(output.get(0).get(0).getAbsolutePath().endsWith("Test1.txt"));
	}

	@Test
	public void scanDoubleDuplicateInput() {
		final List<List<File>> output = DuplicateContentFinder.getResult(of(
				new File(PATH_FILE_1A),
				new File(PATH_FILE_2A),
				new File(PATH_FILE_2B),
				new File(PATH_FILE_1B))).toList();
		assertNotNull(output);
		assertEquals(2, output.size());
		Iterator<List<File>> iter = output.iterator();
		List<File> group1 = iter.next();
		List<File> group2 = iter.next();
		assertEquals(2, group1.size());
		assertEquals(2, group2.size());
		assertTrue(group1.get(0).getAbsolutePath().endsWith("Test1.txt") || group2.get(0).getAbsolutePath().endsWith("Test1.txt"));
		assertTrue(group1.get(0).getAbsolutePath().endsWith("Test2.txt") || group2.get(0).getAbsolutePath().endsWith("Test2.txt"));
	}
}
