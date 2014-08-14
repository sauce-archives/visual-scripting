package org.testobject.kernel.pipeline;

import java.awt.Rectangle;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.testobject.commons.bus.EventBus;
import org.testobject.commons.events.FrameBufferSetEvent;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.GraphBlobBuilder;
import org.testobject.kernel.inputs.InputStateMachine;
import org.testobject.kernel.locator.api.Button;
import org.testobject.kernel.locator.api.Locator;
import org.testobject.kernel.locator.api.Root;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.Image.Int;
import org.testobject.commons.util.image.PixelFormat;
import org.testobject.kernel.inputs.InputStateChangeEvent;
import org.testobject.kernel.script.api.Events;
import org.testobject.kernel.script.api.Events.Click;
import org.testobject.kernel.script.api.Events.Event;
import org.testobject.kernel.script.api.Script;
import org.testobject.kernel.script.api.Script.Requests.ClickOn;
import org.testobject.kernel.script.api.Script.Requests.Request;
import org.testobject.kernel.script.api.Script.Responses.Appears;
import org.testobject.kernel.script.api.Script.Responses.Response;

/**
 * 
 * @author hlenke
 * 
 */

public class MockPipeline implements Pipeline, InputStateChangeEvent.Handler, FrameBufferSetEvent.Handler {

	private final EventBus bus;

	public MockPipeline(EventBus bus) {
		this.bus = bus;
	}

	@Override
	public Result process(Event event, Int before, Int after) {
		Locator mockButton = new Button("Label");
		GraphBlobBuilder blobBuilder = new GraphBlobBuilder(before.w, before.h);
		Blob[] blobs = blobBuilder.build(before);

		Locator[] childs = new Locator[1];
		childs[0] = mockButton;
		// create pipeline result
		Root root = new Root(childs);

		if (event instanceof Click) {
			Click click = (Click) event;
			Blob target = blobs[blobs[0].ids[click.y][click.x]];
			Blob blob = new Blob(0, new Rectangle(target.bbox.x + 2, target.bbox.y + 2, target.bbox.width - 2, target.bbox.height - 2), 0,
					null, null);

			Pipeline.Result result = new Result(new Images(before, after), getAction(mockButton, click), getMetas(), getLocator(root, blob,
					mockButton), getDiffs(), getTarget());
			return result;
		} else if (event instanceof Events.Type) {
			Events.Type type = (Events.Type) event;

			Blob target = blobs[0];
			Blob blob = new Blob(0, new Rectangle(target.bbox.x + 2, target.bbox.y + 2, target.bbox.width - 2, target.bbox.height - 2), 0,
					null, null);

			Pipeline.Result result = new Result(new Images(before, after), getTypeAction(mockButton, type), getMetas(), getLocator(root,
					blob, mockButton), getDiffs(), getTarget());
			return result;
		}
		return null;
	}

	@Override
	public void setFrameBuffer(long timestamp, PixelFormat pixelformat, Image.Int buffer) {
	}

	@Override
	public void stateChanged(InputStateMachine.State state) {
	}

	// mock result

	private Action getAction(Locator mockButton, Click click) {
		LinkedList<Locator> path = new LinkedList<Locator>();
		path.add(mockButton);

		Request request = new ClickOn(click, path);
		List<Response> response = new LinkedList<Response>();

		// FIXME add more
		Appears appears = new Appears(path);

		response.add(appears);

		Pipeline.Action action = new Action(request, response);
		return action;
	}

	private Action getTypeAction(Locator mockButton, Events.Type type) {
		LinkedList<Locator> path = new LinkedList<Locator>();
		path.add(mockButton);

		Request request = new Script.Requests.Type(type);
		List<Response> response = new LinkedList<Response>();

		// FIXME add more
		Appears appears = new Appears(path);

		response.add(appears);

		Pipeline.Action action = new Action(request, response);
		return action;
	}

	private Metas getMetas() {
		return null;
	}

	private Locators getLocator(Root root, Blob mockBlob, Locator mockButton) {
		Map<Locator, Blob> beforeLocatorToBlob = new IdentityHashMap<>();
		Map<Locator, Blob> afterLocatorToBlob = new IdentityHashMap<>();

		beforeLocatorToBlob.put(mockButton, mockBlob);
		afterLocatorToBlob.put(mockButton, mockBlob);
		return new Locators(root, root, beforeLocatorToBlob, afterLocatorToBlob);
	}

	private Diffs getDiffs() {
		return null;
	}

	private Target getTarget() {
		return null;
	}

}
