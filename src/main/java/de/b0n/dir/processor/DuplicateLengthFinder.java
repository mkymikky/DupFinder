package de.b0n.dir.processor;

import de.b0n.dir.DupFinderCallback;

import java.io.File;
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

    protected final Cluster<Long, File> model;
    protected final ExecutorService threadPool;

    protected static final Queue<Future<?>> futures = new ConcurrentLinkedQueue<Future<?>>();
    protected static final DupFinderCallback DUMMY_CALLBACK = new DummyCallback();


    /**
     * Bereitet für das gegebene Verzeichnis die Suche nach gleich großen
     * Dateien vor.
     *
     * @param threadPool Pool zur Ausführung der Suchen
     * @param callback   Ruft den Callback bei jedem neu betretenen Verzeichnis auf
     *                   (darf null sein)
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
        this.execute(folder);
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

        private String checkFolder(final File folder) {
            String exceptionMessage = null;
            if (folder.list() == null) {
                exceptionMessage = "FEHLER: Parameter <Verzeichnis> kann nicht aufgelistet werden: ";
            }
            if (!folder.canRead()) {
                exceptionMessage = "FEHLER: Parameter <Verzeichnis> ist nicht lesbar: ";
            }
            if (!folder.isDirectory()) {
                exceptionMessage = "FEHLER: Parameter <Verzeichnis> ist kein Verzeichnis: ";
            }
            if (!folder.exists()) {
                exceptionMessage = "FEHLER: Parameter <Verzeichnis> existiert nicht: ";
            }
            return exceptionMessage;
        }

        /**
         * Iteriert durch die Elemente im Verzeichnis und legt neue Suchen für
         * Verzeichnisse an. Dateien werden sofort der Größe nach abgelegt.
         * Wartet die Unterverzeichnis-Suchen ab und merged deren
         * Ergebnisdateien. Liefert das Gesamtergebnis zurück.
         */
        @Override
        public void run() {
            String[] contents = folder.list();
            if (contents == null) {
                if (callback != null) {
                    callback.unreadableFolder(folder);
                }
                return;
            }

            for (String fileName : contents) {
                File file = new File(folder.getAbsolutePath(), fileName);

                if (file.isDirectory()) {
                    String exceptionMessage = checkFolder(folder);
                    if (exceptionMessage == null) {
                        Thread.yield();
                        System.gc();
                        futures.add(threadPool.submit(new DuplicateLengthRunner(model, threadPool, file, callback)));
                        if (callback != null) {
                            callback.enteredNewFolder(file);
                        }
                    } else {
                        if (callback != null) {
                            callback.unreadableFolder(file);
                        }
                    }
                }

                if (file.isFile()) {
                    model.addGroupedElement(Long.valueOf(file.length()), file);
                }
            }
            return;
        }
    }

}
