package org.testobject.kernel.replay.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.testobject.kernel.pipeline.event.PipelineResultEvent;
import org.testobject.kernel.replay.AwaitPipelineEvent;

/**
 * 
 * @author enijkamp
 * 
 */
public class BusAwaitPipelineEventAdapter implements AwaitPipelineEvent
{
	private final static int RESPONSE_TIMEOUT = 80000;

	private final BlockingQueue<PipelineResultEvent> queue = new LinkedBlockingQueue<>();

	private final int timeout;

	public BusAwaitPipelineEventAdapter(int timeout)
	{
		this.timeout = timeout;
	}

	public BusAwaitPipelineEventAdapter()
	{
		this(RESPONSE_TIMEOUT);
	}

	@Override
	public void handlePipelineResultEvent(PipelineResultEvent event)
	{
		queue.add(event);
	}

	@Override
	public PipelineResultEvent await()
	{
		try
		{
			// FIXME we most likely need the pipelineevent data elsewhere (locators etc.), should that code extend itself to the bus or
			// return event here?
			PipelineResultEvent event = queue.poll(timeout, TimeUnit.MILLISECONDS);
			if (event == null)
			{
				throw new IllegalStateException("no response within " + timeout + " ms");
			}
			return event;
		} catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}
}