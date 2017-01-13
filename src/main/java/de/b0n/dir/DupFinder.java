package de.b0n.dir;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JFrame;
import javax.swing.UIManager;

import de.b0n.dir.processor.DuplicateContentFinder;
import de.b0n.dir.processor.DuplicateLengthFinder;

public class DupFinder {

	private static final String MESSAGE_NO_PARAM = "FEHLER: Parameter <Verzeichnis> fehlt\r\n usage: DupFinder <Verzeichnis>\r\n<Verzeichnis> = Verzeichnis in dem rekursiv nach Duplikaten gesucht wird";

	private static TreeView treeView = new TreeView();

	public static void main(String[] args) {
		// Lese Root-Verzeichnis aus Argumenten
		if (args.length < 1 || args[0] == null) {
			// exit(1): Kein Parameter übergeben
			System.err.println(MESSAGE_NO_PARAM);
			System.exit(1);
		}
		
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
		
		long startTime = System.nanoTime();
		ExecutorService threadPool = Executors.newWorkStealingPool();
		Queue<Queue<File>> duplicatesByLength = DuplicateLengthFinder.getResult(threadPool, new File(args[0]));
		Queue<Queue<File>> duplicatesByContent = new ConcurrentLinkedQueue<Queue<File>>();
		Future<?> updater = threadPool.submit(treeView.new Updater(duplicatesByContent));
		DuplicateContentFinder.getResult(threadPool, duplicatesByLength, duplicatesByContent);
		updater.cancel(true);
		long duplicateTime = System.nanoTime();
		System.out.println("Zeit in Sekunden zum Finden der Duplikate: " + ((duplicateTime - startTime)/1000000000));
	}
    
	/**
	* Create the GUI and show it.  For thread safety,
	* this method should be invoked from the
	* event dispatch thread.
	*/
	private static void createAndShowGUI() {
	   try {
	       UIManager.setLookAndFeel(
	           UIManager.getSystemLookAndFeelClassName());
	   } catch (Exception e) {
	       System.err.println("Couldn't use system look and feel.");
	   }
	
	   //Create and set up the window.
	   JFrame frame = new JFrame("Duplikat-Finder");
	   frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
	   //Add content to the window.
	   frame.add(treeView );
	
	   //Display the window.
	   frame.pack();
	   frame.setVisible(true);
	}
}
