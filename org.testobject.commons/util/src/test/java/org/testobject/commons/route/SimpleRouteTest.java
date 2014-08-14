package org.testobject.commons.route;

import static org.testobject.commons.bus.Register.registerHandler;

import org.testobject.commons.bus.Event;
import org.testobject.commons.bus.EventBus;
import org.testobject.commons.bus.SimpleEventBus;
import org.testobject.commons.route.RouteBuilder;
import org.testobject.commons.util.io.Closable;

public class SimpleRouteTest {

	public static class Ping implements Event<Ping.Handler> {

		private final String text = "test";

		public interface Handler extends Event.Handler {
			void hello(String text);
		}

		@Override
		public void dispatch(Handler handler) {
			handler.hello(this.text);
		}
	}

	public static class PingRouteEvent implements Event<PingRouteEvent.Handler> {

		private final boolean on = true;

		public interface Handler extends Event.Handler {
			void isOn(boolean on);
		}

		@Override
		public void dispatch(Handler handler) {
			handler.isOn(this.on);
		}
	}

	public interface Source {

	}

	public class SourceImpl implements Source {

	}

	public interface Sink {
		// select a route
		PingRouteEvent getPingRouteEvent();

		// end-point of route
		Ping.Handler getPingRouteHandler();
	}

	public class SinkImpl implements Sink {

		private final PingRouteEvent sw = new PingRouteEvent();

		@Override
		public PingRouteEvent getPingRouteEvent() {
			return this.sw;
		}

		@Override
		public Ping.Handler getPingRouteHandler() {
			return new Ping.Handler() {
				@Override
				public void hello(String text) {
					System.out.println(text);
				}
			};
		}
	}

	public void testRouting() {
		// components
		final Source source = new SourceImpl();
		final Sink sink = new SinkImpl();
		guice(source, sink);
	}

	private void guice(Source source, Sink sink) {
		final EventBus bus = new SimpleEventBus();
		// routes
		final Closable ping = new RouteBuilder().event(Ping.class).from(source).to(sink.getPingRouteHandler()).build();
		// wiring
		registerHandler(bus, PingRouteEvent.class, new PingRouteEvent.Handler() {
			@Override
			public void isOn(boolean on) {
				if (on) {
					// bus.register(ping);
				} else {
					// bus.unregister(ping);
				}
			}
		});
		System.out.println(ping);
	}

}
