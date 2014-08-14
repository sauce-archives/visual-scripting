package org.testobject.commons.events;

import org.testobject.commons.bus.Event;

/**
 *
 * @author enijkamp
 *
 */
public interface KeyEvent extends TimestampEvent<KeyEvent.Handler> {
	
	interface Handler extends Event.Handler {
		void keyEvent(final Timestamp timestamp, int key, boolean controlKey, boolean downFlag);
	}
	
	class Factory {
		public static KeyEvent create(final Timestamp timestamp, final int key, final boolean controlKey, final boolean downFlag) {
			return new KeyEvent() {
				@Override
				public TimestampEvent.Timestamp getTimestamp() {
					return timestamp;
				}

				@Override
				public void dispatch(Handler handler) {
					handler.keyEvent(timestamp, key, controlKey, downFlag);
				}
			};
		}
	}
}
