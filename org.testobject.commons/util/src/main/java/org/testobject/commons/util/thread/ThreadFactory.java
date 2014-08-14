package org.testobject.commons.util.thread;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadFactory implements java.util.concurrent.ThreadFactory {

	private final String prefix;

	private final AtomicInteger counter = new AtomicInteger(0);
	private final UncaughtExceptionHandler exceptionHandler;

	public ThreadFactory(String prefix, UncaughtExceptionHandler exceptionHandler) {
		this.prefix = prefix;
		this.exceptionHandler = exceptionHandler;
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread thread = new Thread(r, prefix + "-" + counter.incrementAndGet());
		thread.setUncaughtExceptionHandler(exceptionHandler);

		return thread;
	}

}
