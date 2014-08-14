package org.testobject.kernel.inference.recording;

import java.util.List;

import org.testobject.commons.util.image.Image;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.events.output.Responses;

public class ResponseResult {
	
	public final List<Responses.Response> responses;
	
	public final Image.Int rawAfter;
	
	public final Locator.Node locatorAfter;

	public ResponseResult(List<Responses.Response> responses, Image.Int rawAfter, Locator.Node locatorAfter) {
		this.responses = responses;
		this.rawAfter = rawAfter;
		this.locatorAfter = locatorAfter;
	}

}
