package org.testobject.kernel.inference.recording;

import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.Image.Int;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.classification.graph.Locator.Node;
import org.testobject.kernel.api.events.output.Events;

/**
 * 
 * @author enijkamp
 *
 */
public class ParserResult {

	public static class Partial {

		public final long time;
		public final Locator.Node locator;
		public final Image.Int raw;

		public Partial(long time, Node locator, Int raw) {
			this.time = time;
			this.locator = locator;
			this.raw = raw;
		}
	}

	public final Events.Event input;
	public final Partial before, after;

	public ParserResult(Events.Event input, long timeBefore, long timeAfter, Image.Int rawBefore, Image.Int rawAfter,
			Locator.Node locatorBefore, Locator.Node locatorAfter) {
		this.input = input;
		this.before = new Partial(timeBefore, locatorBefore, rawBefore);
		this.after = new Partial(timeAfter, locatorAfter, rawAfter);
	}

}
