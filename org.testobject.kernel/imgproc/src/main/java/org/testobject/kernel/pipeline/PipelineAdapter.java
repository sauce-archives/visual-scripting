package org.testobject.kernel.pipeline;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.bus.EventBus;
import org.testobject.commons.bus.Register;
import org.testobject.commons.bus.Registration;
import org.testobject.kernel.inputs.InputStateMachine;
import org.testobject.kernel.inputs.InputStateChangeEvent;
import org.testobject.kernel.pipeline.event.LocatorUpdateEvent;
import org.testobject.kernel.pipeline.event.PipelineResultEvent;
import org.testobject.kernel.script.api.Events;

/**
 * 
 * @author enijkamp
 * 
 */
public class PipelineAdapter implements InputStateChangeEvent.Handler {

	public static final Log log = LogFactory.getLog(PipelineAdapter.class);

	private final Pipeline pipeline;
	private final EventBus bus;

	public PipelineAdapter(EventBus bus, Pipeline pipeline) {
		this.pipeline = pipeline;
		this.bus = bus;
	}

	@Override
	public void stateChanged(InputStateMachine.State state) {
		Events.Event event = toEvent(state);

		Pipeline.Result result = pipeline.process(event, state.before.framebuffer, state.after.framebuffer);

		// log
		{
			log.trace(result.action.request);
			for (int i = 0; i < result.action.response.size(); i++) {
				log.trace("   " + i + ". " + result.action.response.get(i));
			}
		}

		// events
		{
			bus.fireEvent(PipelineResultEvent.class, new PipelineResultEvent(state.before.timestamp, state.after.timestamp, result));
		}
	}

	private Events.Event toEvent(InputStateMachine.State state) {
		switch (state.type) {
		case Click:
			return state.clickEvent;
		case Key:
			return state.typeEvent;
		default:
			throw new UnsupportedOperationException("Unknown Event: " + state.type);
		}
	}

	public Pipeline getPipeline() {
		return pipeline;
	}

	// FIXME use new event system (en)
	public Registration register(Class<LocatorUpdateEvent> clazz, LocatorUpdateEvent.Handler handler) {
		return Register.registerHandler(bus, clazz, handler);
	}

	public Registration register(Class<PipelineResultEvent> clazz, PipelineResultEvent.Handler handler) {
		return Register.registerHandler(bus, clazz, handler);
	}

}
