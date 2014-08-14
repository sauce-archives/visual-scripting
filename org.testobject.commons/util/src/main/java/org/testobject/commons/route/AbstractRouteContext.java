package org.testobject.commons.route;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.testobject.commons.bus.Event;
import org.testobject.commons.bus.Registration;
import org.testobject.commons.util.io.Closable;

import com.google.common.base.Preconditions;

public class AbstractRouteContext {

	private final AtomicBoolean open = new AtomicBoolean(false);
	private final Set<RouteBuilder> routes = new HashSet<>();
	private final Set<Closable> activeRoutes = new HashSet<>();
	private final Map<Class<? extends Event<?>>, Object[]> eventProducer = new HashMap<Class<? extends Event<?>>, Object[]>();

	protected class Expose {

		private Class<? extends Event<?>> eventType;
		private List<Object> producers = new LinkedList<>();

		public Expose event(Class<? extends Event<?>> eventType) {
			this.eventType = eventType;
			return this;
		}

		public Expose from(Object producer) {
			producers.add(producer);
			return this;
		}

		public void build() {
			eventProducer.put(eventType, producers.toArray());
		}
	}

	protected void add(RouteBuilder routeBuilder) {
		routes.add(routeBuilder);
	}

	protected Expose emits() {
		return new Expose();
	}

	protected <H extends Event.Handler> Registration register(Class<? extends Event<H>> eventType, H handler) {
		Object[] producers = eventProducer.get(eventType);
		Preconditions.checkNotNull(producers, "no event producers registered for event type '" + eventType + "' requesting handler is '" + handler + "'");

		final List<Registration> registrations = new LinkedList<>();
		for (Object producer : producers) {
			registrations.add(RouteUtils.register(eventType, RouteUtils.getHandlerClass(eventType), producer, handler));
		}

		return new Registration() {
			public void unregister() {
				for (Registration registration : registrations) {
					registration.unregister();
				}
			}
		};
	}

	public void open() {
		if (open.getAndSet(true)) {
			throw new IllegalStateException("route context is open");
		}
		for (RouteBuilder routeBuilder : routes) {
			activeRoutes.add(routeBuilder.build());
		}
	}

	public void close() {
		for (Closable activeRoute : activeRoutes) {
			activeRoute.close();
		}
		this.open.set(false);
	}
}
