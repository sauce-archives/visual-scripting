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
 * Captures framebuffers in a certain interval and times out after a user-defined amount of time.
 * 
 * @author enijkamp
 *
 */
public class FramebufferSequenceInterval implements FramebufferSequence {
	
	public static class Factory implements FramebufferSequence.Factory {
		
		@Override
		public FramebufferSequence create(Get<Framebuffer> framebuffer, long timeoutMs) {
			return new FramebufferSequenceInterval(framebuffer, timeoutMs);
		}
	}
	
	private static final Log log = LogFactory.getLog(FramebufferSequenceInterval.class);
	
	private static final long INTERVAL_MS = 1000;
	
	private static final Framebuffer sentinal = new Framebuffer(0, new Image.Int(0, 0));
	
	private final long timeoutMs;
	private final Get<Framebuffer> framebuffer;
	
	private final ReentrantLock lock = new ReentrantLock();
	private final Timer timer = new Timer();
	
	private final BlockingQueue<Framebuffer> queue = Queues.newLinkedBlockingQueue();
	
	private long timeoutTimestamp = now();
	
	public FramebufferSequenceInterval(Get<Framebuffer> framebuffer, long timeoutMs) {
		this.framebuffer = framebuffer;
		this.timeoutMs = timeoutMs;
	}

	@Override
	public void open() {
		lock.lock();
		try {
			scheduleInterval();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void close() {
		timer.cancel();
		timer.purge();
	}
	
	private void scheduleInterval() {
		this.timeoutTimestamp = now() + timeoutMs;
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				handleInterval();
			}
		};
		timer.scheduleAtFixedRate(task, 0, INTERVAL_MS);
	}
	
	private void handleInterval() {
		lock.lock();
		try {
			log.info("handleInterval()");
			queue.clear();
			queue.add(framebuffer.get());
			
			if(now() > timeoutTimestamp) {
				queue.add(sentinal);
				timer.cancel();
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
		lock.lock();
		try {
			return queue.isEmpty() || queue.peek() != sentinal;
		} finally {
			lock.unlock();
		}
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

