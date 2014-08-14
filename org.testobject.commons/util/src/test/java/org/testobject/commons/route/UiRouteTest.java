package org.testobject.commons.route;

import static org.testobject.commons.bus.Register.registerHandler;

import org.testobject.commons.bus.Event;
import org.testobject.commons.bus.EventBus;
import org.testobject.commons.bus.SimpleEventBus;
import org.testobject.commons.route.RouteBuilder;
import org.testobject.commons.util.io.Closable;

public class UiRouteTest {

	public static class ImageEvent implements Event<ImageEvent.Handler> {

		private final String image = "test";

		public interface Handler extends Event.Handler {
			void repaint(String image);
		}

		@Override
		public void dispatch(Handler handler) {
			handler.repaint(this.image);
		}
	}

	public static class ImageRouteEvent implements Event<ImageRouteEvent.Handler> {

		private final boolean blobHierarchy = true;

		public interface Handler extends Event.Handler {
			void isOn(boolean on);
		}

		@Override
		public void dispatch(Handler handler) {
			handler.isOn(this.blobHierarchy);
		}
	}

	public interface ImageSource {

	}

	public class SourceImpl implements ImageSource {

	}

	public interface UiSink {
		// select a route
		ImageRouteEvent getImageRouteEvent();

		// end-point of route
		ImageEvent.Handler getPingRouteHandler();
	}

	public class SinkImpl implements UiSink {

		private final ImageRouteEvent sw = new ImageRouteEvent();

		@Override
		public ImageRouteEvent getImageRouteEvent() {
			return this.sw;
		}

		public void setView(String view) {
			if (view.equals("blob")) {
				// this.sw.fire(blob);
			}
		}

		@Override
		public ImageEvent.Handler getPingRouteHandler() {
			return new ImageEvent.Handler() {
				@Override
				public void repaint(String image) {
					System.out.println(image);
				}
			};
		}
	}

	public void testRouting() {
		// components
		final ImageSource source = new SourceImpl();
		final UiSink sink = new SinkImpl();
		guice(new SimpleEventBus(), source, sink);
	}

	private void guice(EventBus bus, ImageSource source, UiSink sink) {
		// routes
		final Closable raw = new RouteBuilder().event(ImageEvent.class).from(source).to(sink.getPingRouteHandler()).build();
		final Closable blobs = new RouteBuilder().event(ImageEvent.class).from(source).to(sink.getPingRouteHandler()).build();
		// wiring
		registerHandler(bus, ImageRouteEvent.class, new ImageRouteEvent.Handler() {
			@Override
			public void isOn(boolean on) {
				if (on) {
					// bus.register(raw);
				} else {
					// bus.register(blobs);
				}
			}
		});
		System.out.println(raw);
		System.out.println(blobs);
	}

}
