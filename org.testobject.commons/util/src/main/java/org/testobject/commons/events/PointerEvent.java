package org.testobject.commons.events;

import org.testobject.commons.bus.Event;
import org.testobject.commons.events.PointerClickEvent.ClickType;

/**
 *
 * @author enijkamp
 *
 */
//FIXME replace these events by kernel.api.input events (en)
public interface PointerEvent extends TimestampEvent<PointerEvent.Handler> {

	interface Handler extends Event.Handler {
		void pointerEvent(Timestamp timestamp, ClickType clickType, int x, int y);
	}

	class Factory {
		public static PointerEvent create(final Timestamp timestamp, final ClickType clickType, final int x,
				final int y) {
			return new PointerEvent() {
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
