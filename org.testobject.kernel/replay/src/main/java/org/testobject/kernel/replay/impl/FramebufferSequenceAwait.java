package org.testobject.kernel.replay.impl;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.util.concurrency.Get;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.inference.input.Framebuffer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;

/**
 * Captures framebuffers after a user-defined delay, waits a certain time until no more FBUs are
 * incoming or a maximum amout of time has passed.
 * 
 * @author enijkamp
 *
 */
public class FramebufferSequenceAwait implements FramebufferSequence {
	
	public static class Factory implements FramebufferSequence.Factory {
		
		public static final long WAIT_FOR_DAMAGES_FBU_MS = 5 * 1000;
		public static final long WAIT_FOR_DAMAGES_MAX_MS = 10 * 1000;

		@Override
		public FramebufferSequence create(Get<Framebuffer> framebuffer, long delayMs) {
			return new FramebufferSequenceAwait(framebuffer, WAIT_FOR_DAMAGES_FBU_MS, WAIT_FOR_DAMAGES_MAX_MS, delayMs);
		}
	}
	
	private static final Log log = LogFactory.getLog(FramebufferSequenceAwait.class);
	
	private static final Framebuffer sentinal = new Framebuffer(0, new Image.Int(0, 0));
	
	private final long waitForDamagesFbuMs;
	private final long waitForDamagesMaxMs;
	private final long delayMs;
	private final Get<Framebuffer> framebuffer;
	
	private final ReentrantLock lock = new ReentrantLock();
	private final Timer timer = new Timer();
	
	private final BlockingQueue<Framebuffer> queue = Queues.newLinkedBlockingQueue();
	
	private long lastFbuTimestamp = now();
	
	public FramebufferSequenceAwait(Get<Framebuffer> framebuffer, long waitForDamagesFbuMs, long waitForDamagesMaxMs, long delayMs) {
		this.framebuffer = framebuffer;
		this.waitForDamagesFbuMs = waitForDamagesFbuMs;
		this.waitForDamagesMaxMs = waitForDamagesMaxMs;
		this.delayMs = delayMs;
	}

	@Override
	public void open() {
		lock.lock();
		try {
			scheduleDelayTimeout();
			scheduleMaxTimeout();
		} finally {
			lock.unlock();
		}
	}

	// TODO this has to be called by device context (en)
	public void repaint() {
		lock.lock();
		try {
			scheduleFbuTimeout();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void close() {
		timer.purge();
	}
	
	private void scheduleDelayTimeout() {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				handleDelayTimeout();
			}
		};
		timer.schedule(task, delayMs);
	}
	
	private void scheduleMaxTimeout() {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				handleMaxTimeout();
			}
		};
		timer.schedule(task, waitForDamagesMaxMs);
	}
	
	private void handleDelayTimeout() {
		lock.lock();
		try {
			log.info("handleDelayTimeout()");
			queue.add(framebuffer.get());
		} finally {
			lock.unlock();
		}
	}
	
	private void handleMaxTimeout() {
		lock.lock();
		try {
			log.info("handleMaxTimeout()");
			queue.add(framebuffer.get());
			queue.add(sentinal);
		} finally {
			lock.unlock();
		}
	}
	
	private void scheduleFbuTimeout() {
		lastFbuTimestamp = now();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				handleFbuTimeout(lastFbuTimestamp);
			}
		};
		timer.schedule(task, waitForDamagesFbuMs);
	}

	private void handleFbuTimeout(final long fbuTimestamp) {
		lock.lock();
		try {
			final boolean isLatestFbu = this.lastFbuTimestamp <= fbuTimestamp;

			if (isLatestFbu) {
				log.info("handleFbuTimeout(" + fbuTimestamp + ")");
				queue.add(framebuffer.get());
			}
		} finally {
			lock.unlock();
		}
	}
	
	private static long now() {
		return System.currentTimeMillis();
	}
	
	@Override
	public boolean hasNext() {
		return queue.isEmpty() || queue.peek() != sentinal;
	}

	@Override
	public Framebuffer next() {
		try {
			Framebuffer next = queue.take();
			Preconditions.checkState(next != sentinal);
			return next;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}

