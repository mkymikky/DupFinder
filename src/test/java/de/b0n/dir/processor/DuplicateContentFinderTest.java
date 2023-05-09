package de.b0n.dir.processor;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
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
	public void scanFailingInputWithCallback() {
		List<File> failfiles = new ArrayList<>();
		final File file = new File(PATH_FILE_1A) {
			@Serial
			private static final long serialVersionUID = 1L;

			public String getPath() {
				throw new IllegalStateException();
			}
		};
		
		DuplicateContentFinderCallback callback = new FailingDuplicateContentFinderCallback() {

			@Override
			public void failedFile(File failedFile) {
				failfiles.add(failedFile);
			}
		};
		
		final List<File> input = new ArrayList<>();
		input.add(file);
		DuplicateContentFinder.getResult(input, callback);
		assertEquals(1, failfiles.size());
	}

	@Test
	public void scanEmptyInput() {
		final Queue<File> input = new ConcurrentLinkedQueue<>();
		final Queue<List<File>> output = DuplicateContentFinder.getResult(input);
		assertNotNull(output);
		assertTrue(output.isEmpty());
	}

	@Test
	public void scanEmptyInputWithCallback() {
		final Queue<File> input = new ConcurrentLinkedQueue<>();
		DuplicateContentFinder.getResult(input, FAILING_DCF_CALLBACK);
	}

	@Test
	public void scanSingleInput() {
		final File file = new File(PATH_FILE_1A);
		final List<File> input = new ArrayList<>();
		input.add(file);
		final Queue<List<File>> output = DuplicateContentFinder.getResult(input);
		assertNotNull(output);
		assertTrue(output.isEmpty());
	}

	@Test
	public void scanSingleDuplicateInput() {
		final File file1 = new File(PATH_FILE_1A);
		final File file2 = new File(PATH_FILE_1B);
		final Queue<File> input = new ConcurrentLinkedQueue<>();
		input.add(file1);
		input.add(file2);
		final Queue<List<File>> output = DuplicateContentFinder.getResult(input);
		assertNotNull(output);
		assertEquals(1, output.size());
		assertEquals(2, output.peek().size());
		assertTrue(output.peek().get(0).getAbsolutePath().endsWith("Test1.txt"));
	}

	@Test
	public void scanDoubleDuplicateInput() {
		final File file1 = new File(PATH_FILE_1A);
		final File file2 = new File(PATH_FILE_1B);
		final File file3 = new File(PATH_FILE_2A);
		final File file4 = new File(PATH_FILE_2B);
		final Queue<File> input = new ConcurrentLinkedQueue<>();
		input.add(file1);
		input.add(file3);
		input.add(file4);
		input.add(file2);
		final Queue<List<File>> output = DuplicateContentFinder.getResult(input);
		assertNotNull(output);
		assertEquals(2, output.size());
		List<File> group1 = output.remove();
		List<File> group2 = output.remove();
		assertEquals(2, group1.size());
		assertEquals(2, group2.size());
		assertTrue(group1.get(0).getAbsolutePath().endsWith("Test1.txt") || group2.get(0).getAbsolutePath().endsWith("Test1.txt"));
		assertTrue(group1.get(0).getAbsolutePath().endsWith("Test2.txt") || group2.get(0).getAbsolutePath().endsWith("Test2.txt"));
	}

	@Test
	public void scanDuplicateInputWithCallback() {
		final List<List<File>> duplicateList = new ArrayList<>();
		final File file1 = new File(PATH_FILE_1A);
		final File file2 = new File(PATH_FILE_1B);
		final Queue<File> input = new ConcurrentLinkedQueue<>();
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
		assertTrue(duplicateList.get(0).get(0).getAbsolutePath().endsWith("Test1.txt"));
		assertTrue(duplicateList.get(0).contains(file1));
		assertTrue(duplicateList.get(0).contains(file2));
	}
}
