package org.testobject.kernel.inference.input;

import org.testobject.commons.util.image.Image;

/**
 * 
 * @author enijkamp
 *
 */
public class Framebuffer {
	
	public static class Factory {
		
		public static Framebuffer empty(int w, int h) {
			return new Framebuffer(System.currentTimeMillis(), new Image.Int(w, h));
		}
		
	}
	
	public final long timestamp;
	public final Image.Int framebuffer;

	public Framebuffer(long timestamp, Image.Int framebuffer) {
		this.timestamp = timestamp;
		this.framebuffer = framebuffer;
	}

	@Override
	public Framebuffer clone() {
		return new Framebuffer(timestamp, framebuffer);
	}
}