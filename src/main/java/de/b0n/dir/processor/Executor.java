package de.b0n.dir.processor;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Executor {
	private final ExecutorService THREAD_POOL = Executors.newWorkStealingPool();
	private final Queue<Future<?>> futures = new ConcurrentLinkedQueue<>();

	public void submit(Runnable runnable) {
		futures.add(THREAD_POOL.submit(runnable));
	}

	public void consolidate() {
		while (!futures.isEmpty()) {
			try {
				futures.remove().get();
			} catch (InterruptedException e) {
				// May never occur, Thread is already stopped
				throw new IllegalStateException("Thread was already stopped", e);
			} catch (ExecutionException e) {
				throw new IllegalStateException("Thread could not be consolidated", e);
			}
		}
	}
}
