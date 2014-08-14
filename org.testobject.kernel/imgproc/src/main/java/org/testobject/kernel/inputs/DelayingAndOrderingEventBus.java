package org.testobject.kernel.inputs;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.testobject.commons.bus.AbstractEventBus;
import org.testobject.commons.bus.Dispatcher;
import org.testobject.commons.bus.Event;
import org.testobject.commons.bus.Event.Handler;
import org.testobject.commons.bus.Register;
import org.testobject.commons.bus.Registration;
import org.testobject.commons.bus.UmbrellaException;
import org.testobject.commons.events.ButtonMask;
import org.testobject.commons.events.FrameBufferUpdateEvent;
import org.testobject.commons.events.KeyEvent;
import org.testobject.commons.events.PointerClickEvent;
import org.testobject.commons.events.PointerClickEvent.ClickType;
import org.testobject.commons.events.TimestampEvent;
import org.testobject.commons.guice.WebsocketScopes;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.Image.Int;
import org.testobject.commons.util.lifecycle.Closable;
import org.testobject.commons.util.thread.ClosableThread;
import org.testobject.kernel.imgproc.diff.ImageComparator;
import org.testobject.kernel.imgproc.util.ImageUtil;

/**
 * 
 * @author enijkamp
 * 
 */
public class DelayingAndOrderingEventBus extends AbstractEventBus implements KeyEvent.Handler, PointerClickEvent.Handler,
		FrameBufferUpdateEvent.Handler, Closable {

	private static class Timestamp<T extends Event<?>> {
		public final long time;

		public final T event;
		public final Class<T> type;

		public Timestamp(Class<T> type, T event, long timestamp) {
			this.type = type;
			this.event = event;
			this.time = timestamp;
		}
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
					final Timestamp<?> peek = queue.peek();
					if (peek != null) {
						final long diff = System.currentTimeMillis() - peek.time;
						if (diff > delay) {
							@SuppressWarnings("unchecked")
							final Timestamp<TimestampEvent<Event.Handler>> timestamp = (Timestamp<TimestampEvent<Event.Handler>>) queue
									.poll();
							final Class<TimestampEvent<Event.Handler>> type = timestamp.type;
							final TimestampEvent<Event.Handler> event = timestamp.event;
							final Map<Integer, Dispatcher<?>> hh = dispatcherByTypeById.get(type);
							if (hh == null) {
								continue; // nothing registered
							}

							List<Throwable> exlist = null;

							for (final Dispatcher<?> h : hh.values()) {
								try {
									@SuppressWarnings("unchecked")
									Dispatcher<TimestampEvent<Handler>> d = (Dispatcher<TimestampEvent<Event.Handler>>) h;
									d.dispatch(type, event);
								} catch (final Exception ex) {
									if (exlist == null) {
										exlist = new LinkedList<Throwable>();
									}
									exlist.add(ex);
								}
							}

							if (exlist != null) {
								throw new UmbrellaException(exlist);
							}
						}
					} else {
						Thread.sleep(SLEEP_TIME);
					}
				}
			} catch (InterruptedException ignored) {

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
					FrameBufferUpdateEventImpl event = events.take();
					long timestamp = event.getTimestamp();
					Image.Int deepCopy = ImageUtil.deepCopy(event.buffer);
					if (lastImage == null) {
						List<Rectangle.Int> updates = event.updates;
						fireEvent(FrameBufferUpdateEvent.class, new FrameBufferUpdateEventImpl(timestamp, deepCopy, updates));
					} else if (event.updates.size() == 1
							&& event.updates.get(0).equals(new Rectangle.Int(deepCopy.x, deepCopy.y, deepCopy.w, deepCopy.h))) {
						// full frame buffer update
						List<Rectangle.Int> updates = ImageComparator.compare(lastImage, deepCopy, 0);
						if (updates.isEmpty() == false) {
							fireEvent(FrameBufferUpdateEvent.class, new FrameBufferUpdateEventImpl(timestamp, deepCopy, updates));
						}
					} else {
						List<Rectangle.Int> updates = event.updates;
						fireEvent(FrameBufferUpdateEvent.class, new FrameBufferUpdateEventImpl(timestamp, deepCopy, updates));
					}
					lastImage = deepCopy;
				}
			} catch (final InterruptedException e) {

			}
		}

		@Override
		public void close() {
			closed.set(true);
		}
	}

	private static class FrameBufferUpdateEventImpl implements FrameBufferUpdateEvent {

		final long timestamp;
		Image.Int buffer;
		final List<Rectangle.Int> updates;

		public FrameBufferUpdateEventImpl(long timestamp, Image.Int buffer, List<Rectangle.Int> updates) {
			this.timestamp = timestamp;
			this.buffer = buffer;
			this.updates = updates;
		}

		@Override
		public void dispatch(Handler handler) {
		    handler.updateFrameBuffer(timestamp, buffer, updates);
		}

		@Override
		public long getTimestamp() {
			return timestamp;
		}

		@Override
		public Int getFramebuffer() {
			return buffer;
		}

		@Override
		public List<Rectangle.Int> getUpdates() {
			return updates;
		}
	}

	private final ClosableThread copyThread;
	private final ClosableThread sortingThread;
	private final PriorityBlockingQueue<Timestamp<?>> queue;
	private final BlockingQueue<FrameBufferUpdateEventImpl> events = new LinkedBlockingQueue<FrameBufferUpdateEventImpl>();

	private Image.Int lastImage;

	public DelayingAndOrderingEventBus(final Comparator<Event<?>> comparator, long delay) {
		copyThread = new ClosableThread(new CopyWorker());
		// FIXME WebsocketScopes.continueSession shouldn't be in here
		sortingThread = new ClosableThread(WebsocketScopes.continueSession(new SortingWorker(delay)));
		queue = new PriorityBlockingQueue<Timestamp<?>>(100, new Comparator<Timestamp<?>>() {
			@Override
			public int compare(Timestamp<?> t1, Timestamp<?> t2) {
				return comparator.compare(t1.event, t2.event);
			}
		});
	}

	public void open() {
		sortingThread.start();
		copyThread.start();
	}

	@Override
	public <T extends Event<?>> void fireEvent(T event) {
		@SuppressWarnings("unchecked")
		Class<T> typeSafeType = (Class<T>) event.getClass();
		fireEvent(typeSafeType, event);
	}

	@Override
	public <T extends Event<?>> void fireEvent(Class<T> type, T event) {
		long timestamp = TimestampEvent.class.cast(event).getTimestamp();
		queue.add(new Timestamp<T>(type, event, timestamp));
	}

	public int getCongestion() {
		return queue.size();
	}

	@Override
	public void close() {
		sortingThread.close();
		copyThread.close();
	}

	@Override
	public void updateFrameBuffer(final long timestamp, Image.Int buffer, final List<Rectangle.Int> updates) {
		events.add(new FrameBufferUpdateEventImpl(timestamp, buffer, updates));
	}

	@Override
	public void pointerEvent(final long timestamp, final ClickType clickType, final ButtonMask buttonMask, final int x, final int y) {
		fireEvent(PointerClickEvent.class, PointerClickEvent.Builder.create(timestamp, clickType, buttonMask, x, y));
	}

	@Override
	public void keyEvent(final long timestamp, final int key, final boolean hardwareButton, final boolean downFlag) {
		fireEvent(KeyEvent.class, KeyEvent.Builder.create(timestamp, key, hardwareButton, downFlag));
	}

	// FIXME use new event system (en)
	public Registration register(Class<PointerClickEvent> clazz, PointerClickEvent.Handler handler) {
		return Register.registerHandler(this, clazz, handler);
	}

	public Registration register(Class<KeyEvent> clazz, KeyEvent.Handler handler) {
		return Register.registerHandler(this, clazz, handler);
	}

	public Registration register(Class<FrameBufferUpdateEvent> clazz, FrameBufferUpdateEvent.Handler handler) {
		return Register.registerHandler(this, clazz, handler);
	}

}