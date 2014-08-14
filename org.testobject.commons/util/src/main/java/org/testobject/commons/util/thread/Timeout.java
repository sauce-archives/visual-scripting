package org.testobject.commons.util.thread;

import java.util.concurrent.TimeUnit;

public class Timeout {

	private final long timeout;
	private String errorMessage;

	public Timeout(int value, TimeUnit unit) {
		this.timeout = now() + unit.toMillis(value);
	}

	public Timeout(int value, TimeUnit unit, String errorMessage) {
		this(value, unit);
		this.errorMessage = errorMessage;
	}

	public void check() {
		if (now() > timeout) {
			if (errorMessage == null) {
				throw new TimeoutException();
			} else {
				throw new TimeoutException(errorMessage);
			}
		}
	}

	private static long now() {
		return System.currentTimeMillis();
	}

}
