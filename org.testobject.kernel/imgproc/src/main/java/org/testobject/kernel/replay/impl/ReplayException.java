package org.testobject.kernel.replay.impl;

@SuppressWarnings("serial")
public class ReplayException extends Exception{

	public ReplayException() {
		super();
	}
	
	public ReplayException(String message) {
		super(message);
	}

	public ReplayException(Throwable cause) {
		super(cause);
	}
	
	public ReplayException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReplayException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
