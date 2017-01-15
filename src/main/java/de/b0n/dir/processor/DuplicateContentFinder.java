package de.b0n.dir.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class DuplicateContentFinder implements Runnable {
	private static final int FINISHED = Integer.valueOf(-1);
	private static final int INPUT = Integer.valueOf(-2);
	private static final int FAILING = Integer.valueOf(-3);

	private final ExecutorService threadPool;
	Map<Integer, List<FileStream>> groupedListOfFileStreams = new HashMap<Integer, List<FileStream>>();
	private final Queue<Queue<File>> result;

	private DuplicateContentFinder(ExecutorService threadPool, Queue<File> files, Queue<Queue<File>> result) {
		this(threadPool, repack(files), result);
	}
	
	private DuplicateContentFinder(ExecutorService threadPool, List<FileStream> fileStreams, Queue<Queue<File>> result) {
		this.threadPool = threadPool;
		groupedListOfFileStreams.put(INPUT, fileStreams);
		this.result = result;
	}

	@Override
	public void run() {
		Queue<Future<?>> futures = new ConcurrentLinkedQueue<Future<?>>();
		// Open Streams & wrap in BufferedInputStream
		
		try {
			while (groupedListOfFileStreams.containsKey(INPUT)) {
				sortFilesByByte(groupedListOfFileStreams, groupedListOfFileStreams.get(INPUT));
				
				// Failing Streams
				if (groupedListOfFileStreams.containsKey(FAILING)) {
					closeAll(groupedListOfFileStreams.get(FAILING));
					groupedListOfFileStreams.remove(FAILING);
				}
				
				// Unique Streams
				discardSingleFileGroups(groupedListOfFileStreams);

				// Finished Streams
				if (groupedListOfFileStreams.containsKey(FINISHED)) {
					result.add(extractFiles(groupedListOfFileStreams.get(FINISHED)));
					closeAll(groupedListOfFileStreams.get(FINISHED));
					groupedListOfFileStreams.remove(FINISHED);						
				}
				
				// Prepare for next iteration
				Iterator<List<FileStream>> iterator = groupedListOfFileStreams.values().iterator();
				List<FileStream> nextInput = null;
				if (iterator.hasNext()) {
					nextInput = iterator.next();
				}
				
				// Outsource other groups
				while (iterator.hasNext()) {
					futures.add(threadPool.submit(new DuplicateContentFinder(threadPool, iterator.next(), result)));
				}
				
				// Continue preparation for next iteration
				groupedListOfFileStreams.clear();
				if (nextInput != null) {
					groupedListOfFileStreams.put(INPUT, nextInput);
				}
			}
		} catch(Exception e) {
			for (List<FileStream> fileStreams : groupedListOfFileStreams.values()) {
				closeAll(fileStreams);
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

	private void discardSingleFileGroups(Map<Integer, List<FileStream>> groups) {
		List<Integer> uniqueIds = new ArrayList<Integer>();
		for (Integer fileStreamsKey : groups.keySet()) {
			List<FileStream> fileStreams = groups.get(fileStreamsKey);
			if (fileStreams.size() == 1) {
				closeAll(fileStreams);
				uniqueIds.add(fileStreamsKey);
			}
		}
		for (Integer uniqueId : uniqueIds) {
			groups.remove(uniqueId);
		}
	}

	private void sortFilesByByte(Map<Integer, List<FileStream>> sortFilesMap, List<FileStream> sortFiles) {
		for (FileStream sortFile : sortFiles) {
			try {
				insertFileToKey(sortFilesMap, sortFile, sortFile.read());
			} catch (IllegalStateException e) {
				System.out.println(e.getMessage());
				insertFileToKey(sortFilesMap, sortFile, FAILING);
			}								
		}
		sortFilesMap.remove(INPUT);
	}

	private List<FileStream> insertFileToKey(Map<Integer, List<FileStream>> sortFilesMap, FileStream sortFile,
			int key) {
		List<FileStream> currentFileStreams = sortFilesMap.get(key);
		if (currentFileStreams == null) {
			currentFileStreams = new ArrayList<FileStream>();
			sortFilesMap.put(key, currentFileStreams);
		}
		currentFileStreams.add(sortFile);
		return currentFileStreams;
	}

	public static Queue<Queue<File>> getResult(final ExecutorService threadPool, final Queue<Queue<File>> input) {
		return getResult(threadPool, input, null);
	}

	public static Queue<Queue<File>> getResult(final ExecutorService threadPool, final Queue<Queue<File>> input, Queue<Queue<File>> result) {
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

	private static List<FileStream> repack(Collection<File> files) {
		List<FileStream> fileStreams = new ArrayList<FileStream>();
		for (File file : files) {
			fileStreams.add(new FileStream(file));
		}
		return fileStreams;
	}

	private Queue<File> extractFiles(
			Collection<FileStream> queueOfFileStreams) {
		Queue<File> filesQueue = new ConcurrentLinkedQueue<File>();
		for (FileStream fileStream : queueOfFileStreams) {
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