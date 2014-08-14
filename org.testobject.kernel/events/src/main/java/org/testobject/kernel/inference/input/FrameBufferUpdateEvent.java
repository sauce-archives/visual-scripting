package org.testobject.kernel.inference.input;

import java.util.List;

import org.testobject.commons.bus.Event;
import org.testobject.commons.events.TimestampEvent;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.Image.Int;

/**
 *
 * @author enijkamp
 *
 */
//TODO delete me (en)
public interface FrameBufferUpdateEvent extends TimestampEvent<FrameBufferUpdateEvent.Handler> {
	
	Image.Int getFramebuffer();
	
	List<Rectangle.Int> getUpdates();
	
	interface Handler extends Event.Handler {
        void updateFrameBuffer(final Timestamp time, Image.Int buffer, List<Rectangle.Int> updates);
	}
	
	class Factory {
		public static FrameBufferUpdateEvent create(final Timestamp timestamp, final Image.Int buffer, final List<Rectangle.Int> updates) {
			return new FrameBufferUpdateEvent() {
				@Override
				public void dispatch(Handler handler) {
					handler.updateFrameBuffer(timestamp, buffer, updates);
				}

				@Override
				public TimestampEvent.Timestamp getTimestamp() {
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
			};
		}
	}
}