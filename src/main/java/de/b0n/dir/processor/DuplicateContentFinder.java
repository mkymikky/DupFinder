package de.b0n.dir.processor;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.*;

public class DuplicateContentFinder extends AbstractSearchProcessor {
	private static final Integer FINISHED = Integer.valueOf(-1);
	private static final Integer FAILING = Integer.valueOf(-2);

//	private final Collection<Queue<File>> input;
	protected Cluster<Long,File> model;
	private final ExecutorService threadPool;
	private final DuplicateContentFinderCallback callback;

	private final Queue<Queue<File>> result =new ConcurrentLinkedQueue<Queue<File>>();
	private final Queue<Future<?>> futures=new ConcurrentLinkedQueue<Future<?>>();

	protected DuplicateContentFinder(final Cluster<Long,File> model, final ExecutorService threadPool, final DuplicateContentFinderCallback callback) {
		this.model = model;
		this.threadPool = threadPool;
		this.callback = callback;
	}

	protected DuplicateContentFinder(final Cluster<Long,File> model, final DuplicateContentFinderCallback callback){
		this(model, Executors.newWorkStealingPool(),callback);
	}


	/**
	 * Ermittelt anhand der optional nach Dateigröße vorgruppierten Files inhaltliche Dubletten.
	 * Der ThreadPool, wenn noch nicht im Programm verwendet, kann mit Executors.newWorkStealingPool(); instantiiert werden.
	 * @param input Dateigruppen, welche auf Inhaltliche gleichheit geprüft werden sollen
	 * @param threadPool ExecutorService zur Steuerung der Parallelisierung
	 * @param callback Optionaler Callback, um über den Fortschritt der Dublettensuche informiert zu werden
	 * @return Nach inhaltlichen Dubletten gruppierte File-Listen
	 */
	public Queue<Queue<File>> determineDuplicates() {
		if (threadPool == null) {
			throw new IllegalArgumentException("threadPool may not be null.");
		}
		if (model == null) {
			throw new IllegalArgumentException("input may not be null.");
		}

		callback.processorStartAt(ID,new Date());
		final Queue<Queue<File>> duplicates=this.execute();
		callback.processorEndsAt(ID,new Date());
		return duplicates;
	}


	private Queue<Queue<File>> execute() {

		final Collection<Queue<File>> input = this.model.values();

		for (Queue<File> files : input) {
			futures.add(threadPool.submit(new DuplicateContentRunner(FileStream.pack(files))));
		}

		while (!futures.isEmpty()) {
			try {
				futures.remove().get();
			} catch (InterruptedException | ExecutionException e) {
				throw new IllegalStateException("Threading has failes: " + e.getMessage(), e);
			}
		}

		return this.result;
	}
	
	private class DuplicateContentRunner implements Runnable {
		private Collection<FileStream> inputFileStreams;

		private DuplicateContentRunner(Collection<FileStream> inputFileStreams) {
			this.inputFileStreams = inputFileStreams;
		}
	
		@Override
		public void run() {
			Cluster<Integer, FileStream> sortedFiles = null;
			
			try {
				while (inputFileStreams != null && !inputFileStreams.isEmpty()) {
					sortedFiles = sortFilesByByte(inputFileStreams);
					
					// Failing Streams
					if (sortedFiles.containsGroup(FAILING)) {
						if (callback != null) {
							callback.failedFiles(sortedFiles.getGroup(FAILING).size());
						}
						FileStream.closeAll(sortedFiles.removeGroup(FAILING));
					}
					
					// Unique Streams
					int uniqueFiles = sortedFiles.removeUniques().size();
					if (callback != null && uniqueFiles > 0) {
						callback.uniqueFiles(uniqueFiles);
					}
	
					// Finished Streams
					Queue<FileStream> finishedFiles = null;
					if (sortedFiles.containsGroup(FINISHED)) {
						finishedFiles = sortedFiles.removeGroup(FINISHED);
						result.add(FileStream.extract(finishedFiles));
						FileStream.closeAll(finishedFiles);
						if (callback != null) {
							callback.duplicateGroup(FileStream.extract(finishedFiles));
						}
					}
					
					// Prepare for next iteration
					inputFileStreams = sortedFiles.popGroup();
					
					// Outsource other groups
					for (Queue<FileStream> fileStreams : sortedFiles.values()) {
						futures.add(threadPool.submit(new DuplicateContentRunner(fileStreams)));
					}
				}
			} catch(Exception e) {
				FileStream.closeAll(inputFileStreams);
				if (sortedFiles != null) {
					for (Collection<FileStream> fileStreams : sortedFiles.values()) {
						FileStream.closeAll(fileStreams);
					}
				}
				throw e;
			}
		}		
	}

	/**
	 * Liest aus allen gegebenen FileStreams ein Byte und sortiert die FileStreams nach Ergebnis.
	 * Dadurch werden alle nicht-Dubletten bezüglich dieses Bytes in unterschiedliche Gruppen sortiert.
	 * Ebenso werden alle vollständig gelesenen Dateien in eine eigene Gruppe sortiert INPUT(-1).
	 * Dateien, welche nicht (mehr) gelesen werden können, fallen in die Kategrorie FAILED(-2).
	 * Die restlichen FileStreams landen in den Gruppen des jeweils gelesenen Bytes.
	 * @param inputFileStreams zu sortierende FileStreams
	 * @return Cluster mit den nach Ergebnis sortierten FileStreams
	 */
	private Cluster<Integer, FileStream> sortFilesByByte(Collection<FileStream> inputFileStreams) {
		Cluster<Integer, FileStream> sortedFiles = new Cluster<Integer, FileStream>();
		for (FileStream sortFile : inputFileStreams) {
			try {
				sortedFiles.addGroupedElement(sortFile.read(), sortFile);
			} catch (IllegalStateException e) {
				System.out.println(e.getMessage());
				sortedFiles.addGroupedElement(FAILING, sortFile);
			}								
		}
		return sortedFiles;
	}

}