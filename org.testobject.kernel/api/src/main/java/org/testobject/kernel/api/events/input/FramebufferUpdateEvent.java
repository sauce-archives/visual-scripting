package org.testobject.kernel.api.events.input;

import java.util.List;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.Image.Int;
import org.testobject.kernel.api.events.Timestamp;

public class FramebufferUpdateEvent implements Event, Timestamp.Event {
	
	private final Timestamp timestamp;
	private final Image.Int framebuffer;
	private final List<Rectangle.Int> updates;

	public FramebufferUpdateEvent(Timestamp timestamp, Int framebuffer, List<Rectangle.Int> updates) {
		this.timestamp = timestamp;
		this.framebuffer = framebuffer;
		this.updates = updates;
	}

	@Override
	public Timestamp getTimeStamp() {
		return timestamp;
	}
	
	public Image.Int getFramebuffer() {
		return framebuffer;
	}
	
	public List<Rectangle.Int> getUpdates() {
		return updates;
	}
	
	@Override
	public String toString() {
		return "frame(time = " + timestamp + ", updates = " + updates.size() + ")";
	}

}
