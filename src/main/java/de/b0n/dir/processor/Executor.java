package de.b0n.dir.processor;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Diese Klasse dient zum vereinfachten Starten von Runnables, welche vor der Weiterverarbeitung abgeschlossen sein sollen.
 * @author Claus
 *
 */
class Executor {
	private final ExecutorService THREAD_POOL = Executors.newWorkStealingPool();
	private final Queue<Future<?>> futures = new ConcurrentLinkedQueue<>();

	/**
	 * Fügt ein Runnable zur Ausführungsqueue hinzu
	 * @param runnable zu Startende Nebenläufige Klasse
	 */
	public void submit(Runnable runnable) {
		futures.add(THREAD_POOL.submit(runnable));
	}

	/**
	 * Schließt alle Threads im Ausführungsqueue ab, bevor diese Methode zurückkehrt
	 */
	public void consolidate() {
		while (!futures.isEmpty()) {
			try {
				futures.remove().get();
			} catch (InterruptedException e) {
				throw new IllegalStateException("Thread was already stopped", e);
			} catch (ExecutionException e) {
				throw new IllegalStateException("Thread could not be consolidated", e);
			}
		}
	}
}
