package org.testobject.kernel.pipeline.event;

import org.testobject.commons.bus.Event;
import org.testobject.kernel.inputs.InputStateMachine;

/**
 * 
 * @author hlenke
 * 
 */
public class NewInputStateEvent implements Event<NewInputStateEvent.Handler>
{
	public interface Handler extends Event.Handler
	{
		void newState(InputStateMachine.State state);
	}

	private final InputStateMachine.State state;

	public NewInputStateEvent(InputStateMachine.State state)
	{
		this.state = state;
	}

	@Override
	public void dispatch(Handler handler)
	{
		handler.newState(state);
	}
}