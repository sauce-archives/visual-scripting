package org.testobject.kernel.inputs;

import org.testobject.commons.bus.Event;

//FIXME this is not specific to playback, find better package location (en)
// FIXME rename to ISM-transistion event (find proper name) (en)
public class InputStateChangeEvent implements Event<InputStateChangeEvent.Handler>
{
	public interface Handler extends Event.Handler
	{
		void stateChanged(InputStateMachine.State state);
	}

	private final InputStateMachine.State state;

	public InputStateChangeEvent(InputStateMachine.State state)
	{
		this.state = state;
	}

	@Override
	public void dispatch(Handler handler)
	{
		handler.stateChanged(state);
	}
}