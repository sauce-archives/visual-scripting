package org.testobject.kernel.pipeline.event;

import java.util.Map;

import org.testobject.commons.bus.Event;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.locator.api.Locator;
import org.testobject.kernel.locator.api.Root;

public class LocatorUpdateEvent implements Event<LocatorUpdateEvent.Handler> {

	public interface Handler extends Event.Handler {
		void update(Root root, Map<Locator, Blob> locatorToBlob);
	}

	private final Root root;
	private final Map<Locator, Blob> locatorToBlob;

	public LocatorUpdateEvent(Root root, Map<Locator, Blob> locatorToBlob) {
		this.root = root;
		this.locatorToBlob = locatorToBlob;
	}

	@Override
	public void dispatch(Handler handler) {
		handler.update(root, locatorToBlob);
	}
}
