package org.testobject.commons.util.io;

import java.io.Closeable;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * 
 * @author enijkamp
 *
 */
public class SafeClose {
	
	private static class Nothing {}
	
	private static final Log log = LogFactory.getLog(SafeClose.class);
	
	private final ThreadFactory threadFactory;
	private final long timeoutMs;
	
	public SafeClose(ThreadFactory threadFactory, long timeoutMs) {
		this.threadFactory = threadFactory;
		this.timeoutMs = timeoutMs;
	}
	
	public void close(String timeoutMessage, final Closeable closeable) {
		
		ExecutorService executor = Executors.newSingleThreadExecutor(threadFactory);
		try {
			
			Callable<Nothing> callable = new Callable<Nothing>() {
				@Override
				public Nothing call() throws Exception {
					closeable.close();
					return new Nothing();
				}
			};
			
			Future<Nothing> future = executor.submit(callable);
			
			try {
				future.get(timeoutMs, TimeUnit.MILLISECONDS);
			} catch(TimeoutException e) {
				log.warn(timeoutMessage, e);
			} catch (InterruptedException e) {
				log.debug(e.getMessage(), e);
			} catch (Throwable e) {
				log.warn(e.getMessage(), e);
			}
			
		} finally {
			executor.shutdownNow();
		}
	}

	public void close(final Closeable closeable) {
		close("Safe-close timed out", closeable);
	}
	
	public static SafeClose singleThread(long timeoutMs) {
		UncaughtExceptionHandler handler = new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				log.warn(e.getMessage(), e);
			}
		};
		
		ThreadFactory threadFactory = new ThreadFactoryBuilder()
			.setNameFormat("Device-pool close thread")
			.setUncaughtExceptionHandler(handler).build();
		
		return new SafeClose(threadFactory, timeoutMs);
	}

}
