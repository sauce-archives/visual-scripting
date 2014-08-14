package org.testobject.kernel.inference.input;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.commons.util.io.Closable;
import org.testobject.kernel.api.events.Timestamp;
import org.testobject.kernel.api.events.Timestamp.Event;
import org.testobject.kernel.api.events.input.FramebufferUpdateEvent;

/**
 * 
 * @author enijkamp
 * 
 */
public class DelayingAndOrderingQueue implements Closable {
	
	public interface Factory {
		
		DelayingAndOrderingQueue create(Callback callback);
		
	}
	
	public interface Callback {
		
		void onEvent(Timestamp.Event event);
		
	}

	private class SortingWorker implements Runnable, Closable {
		private final int SLEEP_TIME = 50;
		private final AtomicBoolean closed = new AtomicBoolean(false);
		private final long delay;

		public SortingWorker(long delay) {
			this.delay = delay;
		}

		@Override
		public void run() {
			try {
				while (closed.get() == false) {
					final Timestamp.Event peek = toSort.peek();
					if (peek != null) {
						final long diff = System.currentTimeMillis() - peek.getTimeStamp().getServerTimestamp();
						if (diff > delay) {
							Event event = toSort.poll();
							callback.onEvent(event);
						}
					} else {
						Thread.sleep(SLEEP_TIME);
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

	private class CopyWorker implements Runnable, Closable {

		private final AtomicBoolean closed = new AtomicBoolean(false);

		@Override
		public void run() {
			try {
				while (closed.get() == false) {
					FramebufferUpdateEvent event = toCopy.take();
					Image.Int deepCopy = ImageUtil.deepCopy(event.getFramebuffer());
					List<Rectangle.Int> updates = event.getUpdates();
					
					toSort.add(new FramebufferUpdateEvent(event.getTimeStamp(), deepCopy, updates));
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

	private final CopyWorker copyWorker;
	private final SortingWorker sortWorker;
	private final Thread copyThread, sortThread;
	private final PriorityBlockingQueue<Timestamp.Event> toSort;
	private final BlockingQueue<FramebufferUpdateEvent> toCopy = new LinkedBlockingQueue<>();
	private final Callback callback;

	public DelayingAndOrderingQueue(ThreadFactory threadFactory, final Comparator<Timestamp.Event> comparator, long delay, Callback callback) {
		this.callback = callback;
		this.copyWorker =  new CopyWorker();
		this.sortWorker = new SortingWorker(delay);
		this.toSort = new PriorityBlockingQueue<Timestamp.Event>(100, new Comparator<Timestamp.Event>() {
			@Override
			public int compare(Timestamp.Event t1, Timestamp.Event t2) {
				return comparator.compare(t1, t2);
			}
		});
		this.copyThread = threadFactory.newThread(copyWorker);
		this.sortThread = threadFactory.newThread(sortWorker);
	}

	public void open() {
		sortThread.start();
		copyThread.start();
	}

	public int getCongestion() {
		return toSort.size();
	}

	@Override
	public void close() {
		copyWorker.close();
		sortWorker.close();
	}

	public void putEvent(Timestamp.Event event) {
		if(event instanceof FramebufferUpdateEvent) {
			FramebufferUpdateEvent fbuEvent = (FramebufferUpdateEvent) event;
			toCopy.add(fbuEvent);
		} else {
			toSort.add(event);
		}
	}
}