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

	private final ExecutorService threadPool;
	Map<Integer, List<FileStream>> groupedListOfFileStreams = new HashMap<Integer, List<FileStream>>();
	private final Queue<Queue<File>> result;

	private DuplicateContentFinder(ExecutorService threadPool, Queue<File> files, Queue<Queue<File>> result) {
		this(threadPool, repack(files), result);
	}
	
	private DuplicateContentFinder(ExecutorService threadPool, List<FileStream> fileStreams, Queue<Queue<File>> result) {
		this.threadPool = threadPool;
		groupedListOfFileStreams.put(Integer.valueOf(0), fileStreams);
		this.result = result;
	}

	@Override
	public void run() {
		Queue<Future<?>> futures = new ConcurrentLinkedQueue<Future<?>>();
		// Open Streams & wrap in BufferedInputStream
		
		try {
			while (groupedListOfFileStreams.size() == 1) {
				// Read bytewise and compare
				List<FileStream> originalFileStreams = getFirstElement(groupedListOfFileStreams);
				groupedListOfFileStreams.clear();
				for (FileStream currentFileStream : originalFileStreams) {
					int read;
					try {
						read = currentFileStream.read();
					} catch (IllegalStateException e) {
						System.out.println(e.getMessage());
						continue;
					}
										
					List<FileStream> currentFileStreams = groupedListOfFileStreams.get(read);
					if (currentFileStreams == null) {
						currentFileStreams = new ArrayList<FileStream>();
						groupedListOfFileStreams.put(read, currentFileStreams);
					}
					currentFileStreams.add(currentFileStream);
				}

				// if group consists of only one file, discard as unique
				List<Integer> uniqueIds = new ArrayList<Integer>();
				for (Integer groupedQueueOfFileStreamsKey : groupedListOfFileStreams.keySet()) {
					List<FileStream> fileStreams = groupedListOfFileStreams.get(groupedQueueOfFileStreamsKey);
					if (fileStreams.size() == 1) {
						closeAll(fileStreams);
						uniqueIds.add(groupedQueueOfFileStreamsKey);
					}
				}
				for (Integer uniqueId : uniqueIds) {
					groupedListOfFileStreams.remove(uniqueId);
				}

				// Finished Streams
				if (groupedListOfFileStreams.containsKey(-1)) {
					List<FileStream> fileStreams = groupedListOfFileStreams.get(-1);
						//System.out.println("Duplikate gefunden zu " + fileStreams.iterator().next().getFile().getAbsolutePath());
						result.add(extractFiles(fileStreams));
					closeAll(fileStreams);
					groupedListOfFileStreams.remove(-1);						
				}
				
				// differences: take all files with same input like first to continue
				//		continue until none rest: take group of all files like first of rest to continue in new Thread
				if (groupedListOfFileStreams.size() > 1) {
					Iterator<Integer> iteratorOfFileStreamKeys = groupedListOfFileStreams.keySet().iterator();
					iteratorOfFileStreamKeys.next();
					List<Integer> extractedIds = new ArrayList<Integer>();
					while (iteratorOfFileStreamKeys.hasNext()) {
						Integer currentId = iteratorOfFileStreamKeys.next();
						extractedIds.add(currentId);
						futures.add(threadPool.submit(new DuplicateContentFinder(threadPool, groupedListOfFileStreams.get(currentId), result)));
					}
					
					for (Integer extractedId : extractedIds) {
						groupedListOfFileStreams.remove(extractedId);
					}
				}
			}

			if (!groupedListOfFileStreams.isEmpty()) {
				throw new IllegalStateException("Es wurden potentielle Dubletten unbearbeitet gelassen!");
			}
		} finally {
			for (Integer groupedQueueOfFileStreamsKey : groupedListOfFileStreams.keySet()) {
				for (FileStream fileStream : groupedListOfFileStreams.get(groupedQueueOfFileStreamsKey)) {
					fileStream.close();
				}
			}
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

	private List<FileStream> getFirstElement(Map<Integer, List<FileStream>> map) {
		return map.get(map.keySet().iterator().next());
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