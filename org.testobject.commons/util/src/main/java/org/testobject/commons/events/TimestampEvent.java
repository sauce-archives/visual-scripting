package org.testobject.commons.events;

import org.testobject.commons.bus.Event;

/**
 *
 * @author enijkamp
 *
 */
// FIXME replace these events by kernel.api.input events (en)
public interface TimestampEvent<H extends Event.Handler> extends Event<H> {

	public class Timestamp implements Comparable<Timestamp> {

		private final long serverTimestamp;
		private final long framebufferTimestamp;

		private final long messageCounter;

		public Timestamp(long serverTimestamp, long framebufferTimestamp, long messageCounter) {
			this.serverTimestamp = serverTimestamp;
			this.framebufferTimestamp = framebufferTimestamp;

			this.messageCounter = messageCounter;
		}

		public long getServerTimestamp() {
			return serverTimestamp;
		}

		public long getFramebufferTimestamp() {
			return framebufferTimestamp;
		}

		public long getMessageCounter() {
			return messageCounter;
		}

		@Override
		public int compareTo(Timestamp other) {
			int order1 = Long.compare(this.getFramebufferTimestamp(), other.getFramebufferTimestamp());
			if (order1 == 0) {
				int order2 = Long.compare(this.getServerTimestamp(), other.getServerTimestamp());
				if (order2 == 0) {
					int order3 = Long.compare(this.getMessageCounter(), other.getMessageCounter());
					return order3;
				} else {
					return order2;
				}
			} else {
				return order1;
			}
		}
		
		@Override
		public String toString() {
			return serverTimestamp + " " + framebufferTimestamp + " " + messageCounter;
		}
	}

	Timestamp getTimestamp();

}