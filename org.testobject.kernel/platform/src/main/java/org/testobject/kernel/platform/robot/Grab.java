package org.testobject.kernel.platform.robot;

import org.testobject.commons.util.concurrency.Get;
import org.testobject.commons.util.io.Closable;
import org.testobject.kernel.inference.input.Framebuffer;

/**
 * 
 * @author enijkamp
 * 
 */
public interface Grab extends Get<Framebuffer>, Closable {
	
	boolean grab();

	void reset();
}
