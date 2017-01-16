package de.b0n.dir;

import java.io.File;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import de.b0n.dir.processor.DuplicateContentFinder;
import de.b0n.dir.processor.DuplicateLengthFinder;

/**
 * Einfache Konsolenanwendung zur Ausgabe der gefundenen Dubletten in einem übergebenen Verzeichnis.
 * @author Claus
 *
 */
public class DupFinder {

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
		
		ExecutorService threadPool = Executors.newWorkStealingPool(2);
		Map<Long, Queue<File>> duplicateLengthFilesQueuesMap = DuplicateLengthFinder.getResult(threadPool, directory);
		Queue<Queue<File>> duplicateLengthFilesQueues = unmap(duplicateLengthFilesQueuesMap);
		Queue<Queue<File>> duplicateContentFilesQueues = DuplicateContentFinder.getResult(threadPool, duplicateLengthFilesQueues);
		printQueues(duplicateContentFilesQueues);
	}

	private static Queue<Queue<File>> unmap(Map<Long, Queue<File>> input) {
		Queue<Queue<File>> result = new ConcurrentLinkedQueue<Queue<File>>();
		for (Long key : input.keySet()) {
			result.add(input.get(key));
		}
		return result;
	}
    
	private static void printQueues(Queue<Queue<File>> queues) {
		for (Queue<File> files : queues) {
			printFiles(files);
		}
	}

	private static void printFiles(Queue<File> files) {
		for (File file : files) {
			printFile(file);
			System.out.println();
		}
	}

	private static void printFile(File file) {
		System.out.println(file.getAbsolutePath());
	}
}
