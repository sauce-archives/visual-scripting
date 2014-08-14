package org.testobject.kernel.pipeline;

import java.util.List;
import java.util.Map;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.locator.api.Locator;
import org.testobject.kernel.locator.api.Root;
import org.testobject.kernel.script.api.Events;
import org.testobject.kernel.script.api.Script.Requests.Request;
import org.testobject.kernel.script.api.Script.Responses.Response;

/**
 * 
 * @author enijkamp
 *
 */
public interface Pipeline
{
	interface Intermediate
	{

	}

	class Action implements Intermediate
	{
		public final Request request;
		public final List<Response> response;

		public Action(Request request, List<Response> response)
		{
			this.request = request;
			this.response = response;
		}
	}

	class Target implements Intermediate
	{
		public final Blob blob;

		public Target(Blob blob)
		{
			this.blob = blob;
		}
	}

	class Images implements Intermediate
	{
		public final Image.Int before, after;

		public Images(Image.Int before, Image.Int after)
		{
			this.before = before;
			this.after = after;
		}
	}

	class Diffs implements Intermediate
	{
		public final List<Rectangle.Int> diffs;

		public Diffs(List<Rectangle.Int> diffs)
		{
			this.diffs = diffs;
		}
	}

	class Metas implements Intermediate
	{
		public final Blob before, after;

		public Metas(Blob before, Blob after)
		{
			this.before = before;
			this.after = after;
		}
	}

	class Locators implements Intermediate
	{
		public final Root before, after;
		public final Map<Locator, Blob> beforeLocatorToBlob, afterLocatorToBlob;

		public Locators(Root before, Root after, Map<Locator, Blob> beforeLocatorToBlob,
		        Map<Locator, Blob> afterLocatorToBlob)
		{
			this.before = before;
			this.after = after;
			this.beforeLocatorToBlob = beforeLocatorToBlob;
			this.afterLocatorToBlob = afterLocatorToBlob;
		}
	}

	class Result
	{
		public final Images images;
		public final Action action;
		public final Metas meta;
		public final Locators locator;
		public final Diffs diff;
		public final Target target;

		public Result(Images images, Action action, Metas meta, Locators locator, Diffs diff, Target target)
		{
			this.images = images;
			this.action = action;
			this.meta = meta;
			this.locator = locator;
			this.diff = diff;
			this.target = target;
		}
	}

	Result process(Events.Event event, Image.Int before, Image.Int after);
}
