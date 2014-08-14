package org.testobject.kernel.replay;

import org.testobject.kernel.pipeline.event.PipelineResultEvent;

/**
 * 
 * @author nijkamp
 * 
 */
public interface AwaitPipelineEvent extends PipelineResultEvent.Handler
{
	PipelineResultEvent await();
}