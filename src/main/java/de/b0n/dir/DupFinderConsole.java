package de.b0n.dir;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

import de.b0n.dir.processor.Cluster;
import de.b0n.dir.processor.DuplicateContentFinder;
import de.b0n.dir.processor.DuplicateLengthFinder;

/**
 * Einfache Konsolenanwendung zur Ausgabe der gefundenen Dubletten in einem übergebenen Verzeichnis.
 * @author Claus
 *
 */
public class DupFinderConsole {

	private static final String ERROR = "FEHLER: ";
	private static final String USAGE = "\r\n Benutzung: DupFinder <Verzeichnis>\r\n<Verzeichnis> = Verzeichnis in dem rekursiv nach Duplikaten gesucht wird";
	private static final String NO_PARAM = "Parameter <Verzeichnis> fehlt.";
	private static final String INVALID_DIRECTORY = "Parameter <Verzeichnis> ist kein Verzeichnis.";
	private static final String UREADABLE_DIRECTORY = "Parameter <Verzeichnis> kann nicht gelesen werden.";

	/**
	 * Sucht im übergebenen Verzeichnis nach Dubletten.
	 * @param args Erster Parameter muss ein gültiges Verzeichnis sein
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println(ERROR + NO_PARAM + USAGE);
			return;
		}
		
		File directory = new File(args[0] + File.separator);
		
		if (!directory.isDirectory()) {
			System.err.println(ERROR + INVALID_DIRECTORY + USAGE);
			return;
		}
		
		if (!directory.canRead()) {
			System.err.println(ERROR + UREADABLE_DIRECTORY + USAGE);
			return;
		}

		Cluster<Long, File> cluster = DuplicateLengthFinder.getResult(directory);
		
		Queue<Queue<File>> duplicateContentFilesQueues = new LinkedList<>();
		for (Queue<File> fileQueue : cluster.values()) {
			duplicateContentFilesQueues.addAll(DuplicateContentFinder.getResult(fileQueue));
		}
		printQueues(duplicateContentFilesQueues);
	}
    
	private static void printQueues(Queue<Queue<File>> queues) {
		for (Queue<File> files : queues) {
			printFiles(files);
			System.out.println();
		}
	}

	private static void printFiles(Queue<File> files) {
		for (File file : files) {
			printFile(file);
		}
	}

	private static void printFile(File file) {
		System.out.println(file.getAbsolutePath());
	}
}
