package org.testobject.kernel.inference.recording;

import org.testobject.commons.util.image.Image;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.events.output.Requests;
import org.testobject.kernel.api.events.output.Requests.Request;

public class RequestResult {

	public final Requests.Request request;

	public final Image.Int rawBefore;
	
	public final Locator.Node locatorBefore;

	public RequestResult(Request request, Image.Int rawBefore, Locator.Node locatorBefore) {
		this.request = request;
		this.rawBefore = rawBefore;
		this.locatorBefore = locatorBefore;
	}
}
