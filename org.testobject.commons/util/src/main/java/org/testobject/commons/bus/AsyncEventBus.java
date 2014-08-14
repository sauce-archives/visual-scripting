package org.testobject.commons.bus;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.util.io.Closable;
import org.testobject.commons.util.thread.ClosableThread;

/**
 * 
 * @author enijkamp
 * 
 */
public class AsyncEventBus extends SimpleEventBus implements Closable {
	private static final Log log = LogFactory.getLog(AsyncEventBus.class);

	// FIXME WebsocketScopes.continueSession shouldn't be in here
	protected final ClosableThread thread = new ClosableThread(new Worker());
	protected final BlockingQueue<Event<?>> queue = new LinkedBlockingQueue<Event<?>>();

	private class Worker implements Runnable, Closable {
		private final AtomicBoolean closed = new AtomicBoolean(false);

		@Override
		public void run() {
			try {
				while (closed.get() == false) {
					Event<?> event = queue.poll(200, TimeUnit.MILLISECONDS);
					if (event != null) {
						AsyncEventBus.super.fireEvent(event);
					}
				}
			} catch (InterruptedException ignored) {
				// ignored
			}
		}

		@Override
		public void close() {
			closed.set(true);
		}
	}

	public AsyncEventBus(UncaughtExceptionHandler handler) {
		thread.setUncaughtExceptionHandler(handler);
	}

	public void open() {
		thread.start();
	}

	@Override
	public <T extends Event<?>> void fireEvent(T event) {
		if (log.isDebugEnabled() && queue.isEmpty() == false) {
			log.debug("congestion size " + queue.size());
		}

		queue.add(event);
	}

	public int getCongestion() {
		return queue.size();
	}

	@Override
	public void close() {
		thread.close();
	}
}
