package org.testobject.kernel.replay.impl;

import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.Image.Int;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.kernel.platform.Grab;

public class FramebufferGrab implements Grab {

	private final Int framebuffer;

	public FramebufferGrab(Image.Int framebuffer) {
		this.framebuffer = framebuffer;
	}

	@Override
	public boolean grab() {
		return true;
	}

	@Override
	public Image.Int getImage() {
		return ImageUtil.deepCopy(framebuffer);
	}

	@Override
	public void reset() {
	}

	@Override
	public void close() {
	}

}
