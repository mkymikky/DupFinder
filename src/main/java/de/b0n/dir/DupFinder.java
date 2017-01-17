package de.b0n.dir;

import java.io.File;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.b0n.dir.processor.DuplicateContentFinder;
import de.b0n.dir.processor.DuplicateLengthFinder;
import de.b0n.dir.view.AbstractView;

public class DupFinder {

	private static final String MESSAGE_NO_PARAM = "FEHLER: Parameter <Verzeichnis> fehlt\r\n usage: DupFinder <Verzeichnis>\r\n<Verzeichnis> = Verzeichnis in dem rekursiv nach Duplikaten gesucht wird";
	private static final String MESSAGE_NO_INSTANCE_PARAM = "FEHLER: Parameter <TreeView> fehlt\r\n usage: new DupFinder(treeView);";

	protected AbstractView view;

	public DupFinder(final AbstractView view){
		if( view == null ){
			throw new IllegalArgumentException(MESSAGE_NO_INSTANCE_PARAM);
		}

		this.view=view;
	}

	public static void main(String[] args) throws InterruptedException {
		// Lese Root-Verzeichnis aus Argumenten
		if (args.length < 1 || args[0] == null) {
			// exit(1): Kein Parameter übergeben
			System.err.println(MESSAGE_NO_PARAM);
			throw new IllegalArgumentException(MESSAGE_NO_PARAM);
		}
		final File folder = new File(args[0] + File.separator);

		final DupFinderGUI gui = new DupFinderGUI();
		gui.showView();
		final DupFinder dupFinder = new DupFinder(gui.getTreeView());
		try {
			dupFinder.startSearching(folder);
		}catch (Throwable ex){
			gui.forceClose();
			throw ex;
		}
	}

	public void startSearching(final File folder){

		if( folder == null ){
			throw new IllegalArgumentException(MESSAGE_NO_PARAM);
		}

		long startTime = System.nanoTime();
		ExecutorService threadPool = Executors.newWorkStealingPool();

		Queue<Queue<File>> duplicatesByLength=null;
		try {

			duplicatesByLength = unmap(DuplicateLengthFinder.getResult(threadPool, folder));
		}catch(IllegalArgumentException ex) {
			System.err.println(ex.getMessage());
			throw ex;
		}
		Queue<Queue<File>> duplicatesByContent = new ConcurrentLinkedQueue<Queue<File>>();
		Future<?> updater = threadPool.submit(view.createViewUpdater(duplicatesByContent));
		DuplicateContentFinder.getResult(threadPool, duplicatesByLength, duplicatesByContent);
		updater.cancel(true);
		long duplicateTime = System.nanoTime();
		System.out.println("Zeit in Sekunden zum Finden der Duplikate: " + ((duplicateTime - startTime)/1000000000));

	}

	private Queue<Queue<File>> unmap(Map<Long, Queue<File>> input) {
		Queue<Queue<File>> result = new ConcurrentLinkedQueue<Queue<File>>();
		for (Long key : input.keySet()) {
			result.add(input.get(key));
		}
		return result;
	}


}
