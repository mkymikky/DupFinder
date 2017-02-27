package de.b0n.dir.processor;

import com.github.funthomas424242.unmodifiable.UnmodifiableQueue;
import com.github.funthomas424242.unmodifiable.UnmodifiableQueueLIFO;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.*;

public class DuplicateContentFinder extends AbstractSearchProcessor {
    private static final Integer FINISHED = Integer.valueOf(-1);
    private static final Integer FAILING = Integer.valueOf(-2);

    //	private final Collection<Queue<File>> input;
    protected Cluster<Long, File> model;
    private final ExecutorService threadPool;
    private final DuplicateContentFinderCallback callback;

    private final Queue<UnmodifiableQueue<File>> result = new ConcurrentLinkedQueue<>();
    private final Queue<Future<?>> futures = new ConcurrentLinkedQueue<>();

    protected DuplicateContentFinder(final Cluster<Long, File> model, final ExecutorService threadPool, final DuplicateContentFinderCallback callback) {
        this.model = model;
        this.threadPool = threadPool;
        this.callback = callback;
    }

    protected DuplicateContentFinder(final Cluster<Long, File> model, final DuplicateContentFinderCallback callback) {
        this(model, Executors.newWorkStealingPool(), callback);
    }


    /**
     * Ermittelt anhand der optional nach Dateigröße vorgruppierten Files inhaltliche Dubletten.
     * Der ThreadPool, wenn noch nicht im Programm verwendet, kann mit Executors.newWorkStealingPool(); instantiiert werden.
     *
     * @param input      Dateigruppen, welche auf Inhaltliche gleichheit geprüft werden sollen
     * @param threadPool ExecutorService zur Steuerung der Parallelisierung
     * @param callback   Optionaler Callback, um über den Fortschritt der Dublettensuche informiert zu werden
     * @return Nach inhaltlichen Dubletten gruppierte File-Listen
     */
    public Queue<UnmodifiableQueue<File>> determineDuplicates() {
        if (threadPool == null) {
            throw new IllegalArgumentException("threadPool may not be null.");
        }
        if (model == null) {
            throw new IllegalArgumentException("input may not be null.");
        }

        final Date startDate =  new Date();
        callback.processorStartAt(ID,startDate);
        final Queue<UnmodifiableQueue<File>> duplicates = this.execute();
        callback.processorEndsAt(ID, startDate, new Date());
        return duplicates;
    }


    private Queue<UnmodifiableQueue<File>> execute() {

        final Collection<UnmodifiableQueue<File>> input = this.model.values();

        for (UnmodifiableQueue<File> files : input) {
            futures.add(threadPool.submit(new DuplicateContentRunner(FileStream.pack(files.iterator()))));
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
        private UnmodifiableQueue<FileStream> inputFileStreams;

        private DuplicateContentRunner(UnmodifiableQueue<FileStream> inputFileStreams) {
            this.inputFileStreams = inputFileStreams;
        }

        @Override
        public void run() {
            Cluster<Integer, FileStream> sortedFiles = null;

            try {
                while (inputFileStreams != null && !inputFileStreams.isEmpty()) {
                    sortedFiles = sortFilesByByte(inputFileStreams.iterator());

                    // Failing Streams
                    if (sortedFiles.containsGroup(FAILING)) {
                        if (callback != null) {
                            callback.failedFiles(sortedFiles.getGroup(FAILING).size());
                        }
                        //FileStream.closeAll(sortedFiles.removeGroup(FAILING));
                        FileStream.closeAll(sortedFiles.removeGroup(FAILING).iterator());
                    }

                    // Unique Streams
                    int uniqueFiles = sortedFiles.removeUniques().size();
                    if (callback != null && uniqueFiles > 0) {
                        callback.uniqueFiles(uniqueFiles);
                    }

                    // Finished Streams
                    UnmodifiableQueue<FileStream> finishedFiles = null;
                    if (sortedFiles.containsGroup(FINISHED)) {
                        finishedFiles = sortedFiles.removeGroup(FINISHED);
                        result.add(FileStream.convertListOfFileStreamToListOfFiles(finishedFiles.iterator()));
                        FileStream.closeAll(finishedFiles.iterator());
                        if (callback != null) {
                            callback.duplicateGroup(FileStream.convertListOfFileStreamToListOfFiles(finishedFiles.iterator()));
                        }
                    }

                    // Prepare for next iteration
                    inputFileStreams = sortedFiles.popGroup();

                    // Outsource other groups
                    for (UnmodifiableQueue<FileStream> fileStreams : sortedFiles.values()) {
                        futures.add(threadPool.submit(new DuplicateContentRunner(fileStreams)));
                    }
                }
            } catch (Exception e) {
                FileStream.closeAll(inputFileStreams.iterator());
                if (sortedFiles != null) {
                    for (UnmodifiableQueue<FileStream> fileStreams : sortedFiles.values()) {
                        FileStream.closeAll(fileStreams.iterator());
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
     *
     * @param inputFileStreams zu sortierende FileStreams
     * @return Cluster mit den nach Ergebnis sortierten FileStreams
     */
    private Cluster<Integer, FileStream> sortFilesByByte(Iterator<FileStream> inputFileStreams) {
        Cluster<Integer, FileStream> sortedFiles = new Cluster<Integer, FileStream>();
        while (inputFileStreams.hasNext()) {
            final FileStream sortFile = inputFileStreams.next();
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