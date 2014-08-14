package org.testobject.commons.events;

import org.testobject.commons.bus.Event;

/**
 * 
 * @author enijkamp
 *
 */
public class ExceptionEvent implements Event<ExceptionEvent.Handler> {
	
	private final Throwable exception;

	public interface Handler extends Event.Handler {
		void handleEvent(ExceptionEvent event);
	}

	public ExceptionEvent(Throwable exception) {
		this.exception = exception;
	}
	
	public Throwable getException() {
		return exception;
	}

	@Override
	public void dispatch(Handler handler) {
		handler.handleEvent(this);
	}
}
