package de.b0n.dir.processor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class DuplicateLengthFinder implements Callable<Queue<FileSize>>  {

	private final ExecutorService threadPool;
	private final File folder;
	
	private DuplicateLengthFinder(final ExecutorService threadPool, final File folder) {
		this.threadPool = threadPool;
		this.folder = folder;
	}

	@Override
	public Queue<FileSize> call() throws Exception {
		try {
			directoryCheck();
			readabilityCheck();
			existanceCheck();
			containsElementsCheck();
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
			return new ConcurrentLinkedQueue<FileSize>();
		}
		
		return retrieveFilesizes();
	}

	private void containsElementsCheck() {
		if (folder.list() == null) {
			throw new IllegalArgumentException("Pfad enthält keine Elemente: " + folder.getAbsolutePath());
		}
	}

	private void existanceCheck() {
		if (!folder.exists()) {
			throw new IllegalArgumentException("Pfad existiert nicht: " + folder.getAbsolutePath());
		}
	}

	private void readabilityCheck() {
		if (!folder.canRead()) {
			throw new IllegalArgumentException("Kann Pfad nicht lesen: " + folder.getAbsolutePath());
		}
	}

	private void directoryCheck() {
		if (!folder.isDirectory()) {
			throw new IllegalArgumentException("Pfad gibt kein Verzeichnis an: " + folder.getAbsolutePath());
		}
	}

	private Queue<FileSize> retrieveFilesizes() {
		Queue<FileSize> filesizes = new ConcurrentLinkedQueue<FileSize>();
		Queue<Future<Queue<FileSize>>> futures = new ConcurrentLinkedQueue<Future<Queue<FileSize>>>();

		for (String fileName : folder.list()) {
			File file = new File(folder.getAbsolutePath() + System.getProperty("file.separator") + fileName);
			if (file.isDirectory()) {
				futures.add(threadPool.submit(new DuplicateLengthFinder(threadPool, file)));
			}
			
			if (file.isFile()) {
				filesizes.add(new FileSize(file, file.length()));	
			}
		}
		
		for (Future<Queue<FileSize>> futureFilesizes : futures) {
			try {
				filesizes.addAll(futureFilesizes.get());
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return filesizes;
	}

	public static Queue<Queue<File>> getResult(final ExecutorService threadPool, final File folder) {
		Future<Queue<FileSize>> future = threadPool.submit(new DuplicateLengthFinder(threadPool, folder));
		
		Queue<FileSize> filesizes = null;
		try {
			filesizes = future.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Map<Long, Queue<File>> filesizeMap = new HashMap<Long, Queue<File>>();
		for (FileSize filesize : filesizes) {
			Long size = filesize.getSize();
			File file = filesize.getFile();
			Queue<File> files = filesizeMap.get(size);
			
			if (files == null) {
				files = new ConcurrentLinkedQueue<File>();
				filesizeMap.put(size, files);
			}
			files.add(file);
		}
		
		Queue<Queue<File>> filesQueues =  new ConcurrentLinkedQueue<Queue<File>>();
		for (Long size : filesizeMap.keySet()) {
			Queue<File> files = filesizeMap.get(size);
			if (files.size() > 1) {
				filesQueues.add(files);
			}
		}
		return filesQueues;
	}
}
