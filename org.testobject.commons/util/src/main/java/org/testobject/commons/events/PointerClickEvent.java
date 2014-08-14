package org.testobject.commons.events;

import org.testobject.commons.bus.Event;

/**
 *
 * @author enijkamp
 *
 */
public interface PointerClickEvent extends TimestampEvent<PointerClickEvent.Handler> {

	enum ClickType {
		DOWN, MOVE, UP
	}

	interface Handler extends Event.Handler {
		void pointerEvent(Timestamp timestamp, ClickType clickType, int x, int y);
	}

	class Factory {
		public static PointerClickEvent create(final Timestamp timestamp, final ClickType clickType, final int x, final int y) {
			return new PointerClickEvent() {
				@Override
				public TimestampEvent.Timestamp getTimestamp() {
					return timestamp;
				}

				@Override
				public void dispatch(Handler handler) {
					handler.pointerEvent(timestamp, clickType, x, y);
				}
			};
		}
	}
}
