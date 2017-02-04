package de.b0n.dir.processor;

import java.io.File;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class DuplicateContentFinder implements Runnable {
	private static final Integer FINISHED = Integer.valueOf(-1);
	private static final Integer FAILING = Integer.valueOf(-3);

	private final ExecutorService threadPool;
	private final Queue<Queue<File>> result;
	private Collection<FileStream> inputFileStreams;

	public DuplicateContentFinder(ExecutorService threadPool, Queue<File> files, Queue<Queue<File>> result) {
		this(threadPool, repack(files), result);
	}

	public DuplicateContentFinder(ExecutorService threadPool, Collection<FileStream> fileStreams, Queue<Queue<File>> result) {
		this.threadPool = threadPool;
		this.inputFileStreams = fileStreams;
		this.result = result;
	}

	@Override
	public void run() {
		Queue<Future<?>> futures = new ConcurrentLinkedQueue<Future<?>>();
		Cluster<Integer, FileStream> sortedFiles = null;
		// Open Streams & wrap in BufferedInputStream
		
		try {
			while (inputFileStreams != null && !inputFileStreams.isEmpty()) {
				sortedFiles = sortFilesByByte(inputFileStreams);
				
				// Failing Streams
				if (sortedFiles.containsGroup(FAILING)) {
					closeAll(sortedFiles.removeGroup(FAILING));
				}
				
				// Unique Streams
				sortedFiles.removeUniques();

				// Finished Streams
				if (sortedFiles.containsGroup(FINISHED)) {
					result.add(extractFiles(sortedFiles.getGroup(FINISHED)));
					closeAll(sortedFiles.removeGroup(FINISHED));
				}
				
				// Prepare for next iteration
				inputFileStreams = sortedFiles.removeGroup(FINISHED);
				
				// Outsource other groups
				for (Queue<FileStream> fileStreams : sortedFiles.values()) {
					futures.add(threadPool.submit(new DuplicateContentFinder(threadPool, fileStreams, result)));
				}
			}
		} catch(Exception e) {
			closeAll(inputFileStreams);
			if (sortedFiles != null) {
				for (Collection<FileStream> fileStreams : sortedFiles.values()) {
					closeAll(fileStreams);
				}
			}
			throw e;
		} finally {
			
		}

		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				// This is a major problem, notify user and try to recover
				e.printStackTrace();
			}
		}
	}

	private Cluster<Integer, FileStream> sortFilesByByte(Collection<FileStream> inputFileStreams2) {
		Cluster<Integer, FileStream> sortedFiles = new Cluster<Integer, FileStream>();
		for (FileStream sortFile : inputFileStreams2) {
			try {
				sortedFiles.addGroupedElement(sortFile.read(), sortFile);
			} catch (IllegalStateException e) {
				System.out.println(e.getMessage());
				sortedFiles.addGroupedElement(FAILING, sortFile);

			}								
		}
		return sortedFiles;
	}

	public static Queue<Queue<File>> getResult(final ExecutorService threadPool, final Collection<Queue<File>> input) {
		return getResult(threadPool, input, null);
	}

	public static Queue<Queue<File>> getResult(final ExecutorService threadPool, final Collection<Queue<File>> input, Queue<Queue<File>> result) {
		if (threadPool == null) {
			throw new IllegalArgumentException("threadPool may not be null.");
		}
		if (input == null) {
			throw new IllegalArgumentException("input may not be null.");
		}

		if (result == null) {
			result = new ConcurrentLinkedQueue<Queue<File>>();
		}
	
		Queue<Future<?>> futures = new ConcurrentLinkedQueue<Future<?>>();
		for (Queue<File> files : input) {
			if (files.size() > 1) {
				futures.add(threadPool.submit(new DuplicateContentFinder(threadPool, files, result)));
			}
		}
		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				// This is a critical problem, nothing to recover, abort
				throw new IllegalStateException("Unrecoverable problem, aborting file search", e);
			}
		}
		return result;
	}

	private static Collection<FileStream> repack(Collection<File> files) {
		Queue<FileStream> fileStreams = new ConcurrentLinkedQueue<FileStream>();
		for (File file : files) {
			fileStreams.add(new FileStream(file));
		}
		return fileStreams;
	}

	private Queue<File> extractFiles(Collection<FileStream> fileStreams) {
		Queue<File> filesQueue = new ConcurrentLinkedQueue<File>();
		for (FileStream fileStream : fileStreams) {
			filesQueue.add(fileStream.getFile());
		}
		return filesQueue;
	}

	private void closeAll(Collection<FileStream> fileStreams) {
		for (FileStream fileStream : fileStreams) {
			fileStream.close();
		}
	}
}