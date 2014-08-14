package org.testobject.commons.bus;

import org.testobject.commons.bus.Event.Handler;

/**
 * 
 * @author enijkamp
 * 
 */
public class Register {

	@SuppressWarnings("unchecked")
	public static <H extends Event.Handler> Registration registerUnsafeHandler(EventBus bus, Class<? extends Event<?>> returnEventClass,
			final Handler handler) {
		return bus.registerDispatcher(returnEventClass, toUnsafeDispatcher((Class<? extends Event<Handler>>) returnEventClass, handler));
	}

	public static Dispatcher<Event<Event.Handler>> toUnsafeDispatcher(
			Class<? extends Event<Event.Handler>> type, final Event.Handler handler) {
		return new Dispatcher<Event<Event.Handler>>() {
			@Override
			public void dispatch(Class<Event<Event.Handler>> type, Event<Event.Handler> event) {
				event.dispatch(handler);
			}
		};
	}

	public static <H extends Event.Handler> Registration registerHandler(
			EventBus bus, Class<? extends Event<H>> type, final H handler) {
		return bus.registerDispatcher(type, toDispatcher(type, handler));
	}

	public static <H extends Event.Handler> Dispatcher<Event<H>> toDispatcher(
			Class<? extends Event<H>> type, final H handler) {
		return new Dispatcher<Event<H>>() {
			@Override
			public void dispatch(Class<Event<H>> type, Event<H> event) {
				event.dispatch(handler);
			}
		};
	}

}
