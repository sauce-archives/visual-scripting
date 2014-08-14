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
 * Captures a framebuffer after a user-defined delay.
 * 
 * @author enijkamp
 *
 */
public class FramebufferSequenceDelay implements FramebufferSequence {
	
	public static class Factory implements FramebufferSequence.Factory {
		
		public FramebufferSequence create(Get<Framebuffer> framebuffer, long delayMs) {
			return new FramebufferSequenceDelay(framebuffer, delayMs);
		}
	}
	
	private static final Log log = LogFactory.getLog(FramebufferSequenceDelay.class);
	
	private static final Framebuffer sentinal = new Framebuffer(0, new Image.Int(0, 0));
	
	private final long delayMs;
	private final Get<Framebuffer> framebuffer;
	
	private final ReentrantLock lock = new ReentrantLock();
	private final Timer timer = new Timer();
	
	private final BlockingQueue<Framebuffer> queue = Queues.newLinkedBlockingQueue();
	
	public FramebufferSequenceDelay(Get<Framebuffer> framebuffer, long delayMs) {
		this.framebuffer = framebuffer;
		this.delayMs = delayMs;
	}

	@Override
	public void open() {
		lock.lock();
		try {
			scheduleDelayTimeout();
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
	
	private void handleDelayTimeout() {
		lock.lock();
		try {
			log.info("handleDelayTimeout()");
			queue.add(framebuffer.get());
			queue.add(sentinal);
		} finally {
			lock.unlock();
		}
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

