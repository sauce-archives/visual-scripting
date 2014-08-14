package org.testobject.kernel.inference.recording;

import org.testobject.commons.bus.Event;

/**
 * 
 * @author enijkamp
 *
 */
public class InterpreterEvent implements Event<InterpreterEvent.Handler> {

	public interface Handler extends Event.Handler {
		void handle(TransitionResult result);
	}
	
	public final TransitionResult result;

	public InterpreterEvent(TransitionResult result) {
		this.result = result;
	}

	@Override
	public void dispatch(Handler handler) {
		handler.handle(result);
	}
}
