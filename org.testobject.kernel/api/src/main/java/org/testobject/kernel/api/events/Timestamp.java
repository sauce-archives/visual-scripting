package org.testobject.kernel.api.events;


public class Timestamp implements Comparable<Timestamp> {

	public interface Event extends org.testobject.kernel.api.events.input.Event {

		Timestamp getTimeStamp();

	}

	private final long framebufferTimestamp;
	private final long serverTimestamp;
	private final long messageCounter;

	public Timestamp(long framebufferTimestamp, long serverTimestamp, long messageCounter) {
		this.framebufferTimestamp = framebufferTimestamp;
		this.serverTimestamp = serverTimestamp;
		this.messageCounter = messageCounter;
	}

	public long getFramebufferTimestamp() {
		return framebufferTimestamp;
	}

	public long getServerTimestamp() {
		return serverTimestamp;
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
		return framebufferTimestamp + " " + serverTimestamp +  " " + messageCounter;
	}

}