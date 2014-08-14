package org.testobject.kernel.pipeline.event;

import org.testobject.commons.bus.Event;
import org.testobject.kernel.pipeline.Pipeline;

public class PipelineResultEvent implements Event<PipelineResultEvent.Handler> {

	public interface Handler extends Event.Handler {
		void handlePipelineResultEvent(PipelineResultEvent event);
	}

	public final long timeAfter;
	public final long timeBefore;
	public final Pipeline.Result result;

	public PipelineResultEvent(long timeBefore, long timeAfter, Pipeline.Result result) {
		this.result = result;
		this.timeAfter = timeAfter;
		this.timeBefore = timeBefore;
	}

	@Override
	public void dispatch(Handler handler) {
		handler.handlePipelineResultEvent(this);
	}
}
