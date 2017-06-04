package de.b0n.dir.processor;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class AbstractProcessor {
	private static final ExecutorService THREAD_POOL = Executors.newWorkStealingPool();
	
	static protected Future<?> submit(Runnable runnable) {
		return THREAD_POOL.submit(runnable);
	}
	
	static void consolidate(Collection<Future<?>> futures) {
		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
	}
	
	static void consolidate(Future<?>... futures) {
		consolidate(Arrays.asList(futures));
	}
}