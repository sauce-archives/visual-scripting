package org.testobject.kernel.inference.recording;

import java.util.List;

import org.testobject.commons.util.image.Image;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.events.output.Requests;
import org.testobject.kernel.api.events.output.Responses;
import org.testobject.kernel.api.events.output.Requests.Request;
import org.testobject.kernel.api.events.output.Responses.Response;

/**
 * 
 * @author enijkamp
 *
 */
public class TransitionResult {

	public final Requests.Request request;
	public final List<Responses.Response> responses;

	public final long requestTimeout, responsesTimeout;

	public final Image.Int rawBefore;
	public final Image.Int rawAfter;

	public final Locator.Node locatorBefore;
	public final Locator.Node locatorAfter;

	public TransitionResult(Request request, List<Response> responses, long requestTimeout, long responsesTimeout, Image.Int rawBefore,
			Image.Int rawAfter, Locator.Node locatorBefore, Locator.Node locatorAfter) {
		this.request = request;
		this.responses = responses;
		this.requestTimeout = requestTimeout;
		this.responsesTimeout = responsesTimeout;
		this.rawBefore = rawBefore;
		this.rawAfter = rawAfter;
		this.locatorBefore = locatorBefore;
		this.locatorAfter = locatorAfter;
	}
}
