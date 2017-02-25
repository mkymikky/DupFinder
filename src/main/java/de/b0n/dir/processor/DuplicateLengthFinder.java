package de.b0n.dir.processor;

import de.b0n.dir.DupFinderCallback;

import java.io.File;
import java.nio.file.Files;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Sucht in einem gegebenen Verzeichnis und dessen Unterverzeichnissen nach
 * Dateien und sortiert diese nach Dateigröße.
 *
 * @author Claus
 */
public class DuplicateLengthFinder {

    protected final ProcessorID ID=new ProcessorID(this.getClass().getName());

    protected final Cluster<Long, File> model;
    protected final ExecutorService threadPool;

    protected static final Queue<Future<?>> futures = new ConcurrentLinkedQueue<Future<?>>();
    protected static final DupFinderCallback DUMMY_CALLBACK = new DummyCallback();


    /**
     * Bereitet für das gegebene Verzeichnis die Suche nach gleich großen
     * Dateien vor.
     *
     * @param threadPool Pool zur Ausführung der Suchen
     *
     */
    protected DuplicateLengthFinder(final Cluster<Long, File> model, final ExecutorService threadPool) {
        if (model == null) {
            throw new IllegalArgumentException("model may not be null.");
        }
        if (threadPool == null) {
            throw new IllegalArgumentException("threadPool may not be null.");
        }

        this.model = model;
        this.threadPool = threadPool;
    }

    protected DuplicateLengthFinder(final Cluster<Long, File> model) {
        this(model, Executors.newWorkStealingPool());
    }

    /**
     * Einstiegstmethode zum Durchsuchen eines Verzeichnisses nach Dateien
     * gleicher Größe.
     *
     * @param folder zu durchsuchendes Verzeichnis, muss existieren und lesbar sein
     * @return Liefert eine Map nach Dateigröße strukturierten Queues zurück, in
     * denen die gefundenen Dateien abgelegt sind
     */
    public void readFilesRecursiveOf(final File folder, final DuplicateLengthFinderCallback callback) {

        if (folder == null) {
            throw new IllegalArgumentException("folder may not be null.");
        }

        if (threadPool == null) {
            futures.add(threadPool.submit(new DuplicateLengthRunner(this.model,this.threadPool, folder, DUMMY_CALLBACK)));
        } else {
            futures.add(threadPool.submit(new DuplicateLengthRunner(this.model,this.threadPool, folder, callback)));
        }
        callback.processorStartAt(ID,new Date());
        this.execute(folder);
        callback.processorEndsAt(ID,new Date());
    }


    protected Cluster<Long, File> execute(final File folder) {


        while (!futures.isEmpty()) {
            try {
                futures.remove().get();
            } catch (InterruptedException | ExecutionException e) {
                throw new IllegalStateException("Threading has failed: " + e.getMessage(), e);
            }
        }
        return model;
    }

    private static class DuplicateLengthRunner implements Runnable {

        protected Cluster<Long, File> model;
        protected final ExecutorService threadPool;
        protected final File folder;
        protected final DuplicateLengthFinderCallback callback;

        public DuplicateLengthRunner(final Cluster<Long, File> model, final ExecutorService threadPool, final File folder, final DuplicateLengthFinderCallback callback) {
            this.model = model;
            this.threadPool = threadPool;
            this.folder = folder;
            this.callback = callback;
        }

        private boolean isValidFolder(final File folder) {
            boolean isValidFolder=true;

            if (!folder.exists()) {
                isValidFolder=false;
            }
            if (!folder.isDirectory()) {
                isValidFolder=false;
            }
            if( Files.isSymbolicLink( folder.toPath()) ){
                isValidFolder=false;
            }
            if (!folder.canRead()) {
                isValidFolder=false;
            }
            if (folder.list() == null) {
                isValidFolder=false;
            }
            return isValidFolder;
        }

        /**
         * Iteriert durch die Elemente im Verzeichnis und legt neue Suchen für
         * Verzeichnisse an. Dateien werden sofort der Größe nach abgelegt.
         * Wartet die Unterverzeichnis-Suchen ab und merged deren
         * Ergebnisdateien. Liefert das Gesamtergebnis zurück.
         */
        @Override
        public void run() {
            final String[] contents = folder.list();
            if (contents == null) {
                if (callback != null) {
                    callback.unreadableFolder(folder);
                }
                return;
            }

            for (String fileName : contents) {
                final File file = new File(folder, fileName);

                if (file.isFile()) {
                    model.addGroupedElement(Long.valueOf(file.length()), file);
                }else{
                    final boolean isValidFolder = isValidFolder(folder);
                    if (isValidFolder) {
                        Thread.yield();
                        System.gc();
                        futures.add(threadPool.submit(new DuplicateLengthRunner(model, threadPool, file, callback)));
                        if (callback != null) {
                            callback.enteredNewFolder(file);
                        }
                    } else {
                        if (callback != null) {
                            callback.skipFolder(file);
                        }
                    }
                }
            }
            return;
        }
    }

}
