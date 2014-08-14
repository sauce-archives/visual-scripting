package org.testobject.kernel.inference.input;

import org.testobject.commons.bus.Event;
import org.testobject.commons.events.TimestampEvent;
import org.testobject.commons.util.image.PixelFormat;

/**
 *
 * @author enijkamp
 *
 */
// TODO delete me (en)
public interface FrameBufferRotationEvent extends TimestampEvent<FrameBufferRotationEvent.Handler> {
	
	interface Handler extends Event.Handler {
		void resizeFrameBuffer(final Timestamp timestamp, PixelFormat pixelformat, GetFramebuffer buffer);
	}
	
	class Factory {
		public static FrameBufferRotationEvent create(final Timestamp timestamp, final PixelFormat pixelformat, final GetFramebuffer buffer) {
			return new FrameBufferRotationEvent() {
				@Override
				public void dispatch(Handler handler) {
					handler.resizeFrameBuffer(timestamp, pixelformat, buffer);
				}

				@Override
				public org.testobject.commons.events.TimestampEvent.Timestamp getTimestamp() {
					return timestamp;
				}
			};
		}
	}
}