package org.testobject.kernel.replay.impl;

import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.kernel.inference.input.Framebuffer;
import org.testobject.kernel.platform.robot.Grab;

/**
 * 
 * @author enijkamp
 *
 */
public class FramebufferGrab implements Grab {

	private final Image.Int framebuffer;

	public FramebufferGrab(Image.Int framebuffer) {
		this.framebuffer = framebuffer;
	}

	@Override
	public boolean grab() {
		return true;
	}

	@Override
	public Framebuffer get() {
		return new Framebuffer(System.currentTimeMillis(), ImageUtil.deepCopy(framebuffer));
	}

	@Override
	public void reset() {
		
	}

	@Override
	public void close() {
		
	}

}
