package org.testobject.kernel.replay.impl;

import org.testobject.commons.util.concurrency.Get;
import org.testobject.commons.util.concurrency.Sequence;
import org.testobject.kernel.inference.input.Framebuffer;

/**
 * 
 * @author enijkamp
 *
 */
public interface FramebufferSequence extends Sequence<Framebuffer> {
	
	interface Factory {
		
		FramebufferSequence create(Get<Framebuffer> framebuffer, long delayMs);
		
	}
	
	void open();
	
	void close();
}
