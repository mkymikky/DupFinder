package de.b0n.dir;

import java.io.File;
import java.util.Iterator;
import java.util.Queue;

import com.github.funthomas424242.unmodifiable.UnmodifiableQueue;
import de.b0n.dir.model.DupFinderModel;
import de.b0n.dir.processor.SearchProcessorModel;
import de.b0n.dir.processor.*;

/**
 * Einfache Konsolenanwendung zur Ausgabe der gefundenen Dubletten in einem übergebenen Verzeichnis.
 *
 * @author Claus
 */
public class Launcher {

    protected static final String ERROR = "FEHLER: ";
    protected static final String USAGE = "\r\n Benutzung: SearchChainBuilder <Verzeichnis>\r\n<Verzeichnis> = Verzeichnis in dem rekursiv nach Duplikaten gesucht wird";
    protected static final String NO_PARAM = "Parameter <Verzeichnis> fehlt.";
    protected static final String INVALID_DIRECTORY = "Parameter <Verzeichnis> ist kein Verzeichnis.";
    protected static final String UREADABLE_DIRECTORY = "Parameter <Verzeichnis> kann nicht gelesen werden.";
    protected static final String NO_EXIST_DIRECTORY = "Parameter <Verzeichnis> existiert nicht.";


    protected final SearchChainBuilder searchChainBuilder;

    public Launcher(final SearchProcessorModel<Long, File> model) {
        this.searchChainBuilder = new SearchChainBuilder(model);
    }

    public Queue<UnmodifiableQueue<File>> searchDuplicatesIn(final File folder, final DupFinderCallback callback) {
        return this.searchChainBuilder.searchDuplicatesIn(folder, callback);
    }


    /**
     * Sucht im übergebenen Verzeichnis nach Dubletten.
     *
     * @param args Erster Parameter muss ein gültiges Verzeichnis sein
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println(ERROR + NO_PARAM + USAGE);
            return;
        }

        final File folder = new File(args[0] + File.separator);

        if (!folder.exists()) {
            System.err.println(ERROR + NO_EXIST_DIRECTORY + USAGE);
            return;
        }

        if (!folder.isDirectory()) {
            System.err.println(ERROR + INVALID_DIRECTORY + USAGE);
            return;
        }

        if (!folder.canRead()) {
            System.err.println(ERROR + UREADABLE_DIRECTORY + USAGE);
            return;
        }

        final SearchProcessorModel<Long, File> model = new DupFinderModel<>();
        final Launcher launcher = new Launcher(model);
        final Queue<UnmodifiableQueue<File>> duplicates = launcher.searchDuplicatesIn(folder, DupFinderCallback.DUMMY_CALLBACK);
        launcher.printQueues(duplicates);

    }


    protected void printQueues(Queue<UnmodifiableQueue<File>> queues) {
        System.out.println("\n\n### Liste gefundener Duplikate ###\n");
        for (UnmodifiableQueue<File> files : queues) {
            printFiles(files);
            System.out.println();
        }
    }

    protected void printFiles(UnmodifiableQueue<File> files) {
        if( !files.isEmpty()){
            final File firstFile=files.peek();
            System.out.println("( Orte mit "+ firstFile.length()+" Bytes Länge )");
        }

        final Iterator<File> filesIterator=files.iterator();
        while (filesIterator.hasNext()) {
            printFile(filesIterator.next());
        }
    }

    protected void printFile(File file) {
        System.out.println( file.getAbsolutePath());
    }

}
