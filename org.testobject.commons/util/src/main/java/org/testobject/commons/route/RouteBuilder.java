package org.testobject.commons.route;

import static org.testobject.commons.route.RouteUtils.getHandlerClass;
import static org.testobject.commons.route.RouteUtils.register;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.testobject.commons.bus.Dispatcher;
import org.testobject.commons.bus.Event;
import org.testobject.commons.bus.Event.Handler;
import org.testobject.commons.bus.MultiRegistration;
import org.testobject.commons.bus.Registration;
import org.testobject.commons.util.io.Closable;

/**
 * Closable route = new RouteBuilder()
 *      .event(ClickEvent.Handler.class).from(sourceComponent).to(targetComponent)
 *      .event(RepaintEvent.Handler.class).from(sourceComponent).to(someOtherTarget)
 *      .events(ClickEvent.Handler.class, RepaintEvent.Handler.class).from(otherSource).to(target1, target2)
 *      .with(sourceComponent) // optional, tells builder to call sourceComponent.close() when route is closed.
 *      .build();
 * ...
 * route.close(); // this detaches all handlers and also calls sourceComponent.close(), the latter is courtesy of {@link #with(Closable)} method.
 */
public class RouteBuilder
{

	private final List<Route> routes = new ArrayList<Route>();
	private final List<Closable> closables = new ArrayList<Closable>();

	public interface To
	{
		<H extends Event.Handler> RouteBuilder to(H handler);

		RouteBuilder to(Dispatcher<?> handler);

		RouteBuilder to(Event.Handler handler, Event.Handler[] handlers);
	}

	public interface From
	{
		To from(Object obj);

		To from(Object obj, Object[] objs);
	}

	private static class Route
	{
		public final List<Class<? extends Event<?>>> eclass = new LinkedList<Class<? extends Event<?>>>();
		public final List<Object> source = new LinkedList<Object>();
		public final List<Object> target = new LinkedList<Object>();

		public final MultiRegistration mreg = new MultiRegistration();
	}

	private class FromImpl extends Route implements From, To
	{

		@Override
		public To from(Object obj)
		{
			source.add(obj);

			return this;
		}

		@Override
		public To from(Object obj, Object[] objs)
		{
			source.add(obj);
			for (Object o : objs)
			{
				source.add(o);
			}

			return this;
		}

		@Override
		public <H extends Handler> RouteBuilder to(H handler)
		{
			target.add(handler);

			routes.add(this);

			return RouteBuilder.this;
		}

		@Override
		public RouteBuilder to(Handler handler, Handler[] handlers)
		{
			target.add(handler);

			for (Handler h : handlers)
			{
				target.add(h);
			}

			routes.add(this);

			return RouteBuilder.this;
		}

		@Override
		public RouteBuilder to(Dispatcher<?> handler)
		{
			target.add(handler);

			routes.add(this);

			return RouteBuilder.this;
		}
	}

	public <E extends Event<H>, H extends Handler> From event(Class<E> event)
	{
		final FromImpl from = new FromImpl();

		from.eclass.add(event);

		return from;
	}

	public Closable build()
	{
		for (Route r : routes)
		{
			for (Class<? extends Event<?>> e : r.eclass)
			{
				Class<?> h = getHandlerClass(e);
				for (Object source : r.source)
				{
					for (Object target : r.target)
					{
						@SuppressWarnings("unchecked")
						final Registration reg = register((Class<Event<?>>) e, (Class<Event.Handler>) h, source, target);
						r.mreg.add(reg);
					}
				}
			}
		}

		return new Closable()
		{
			@Override
			public void close()
			{
				for (Route r : routes)
				{
					r.mreg.unregister();
				}

				for (Closable c : closables)
				{
					c.close();
				}
			}
		};
	}

	public RouteBuilder with(Closable cl)
	{
		closables.add(cl);

		return this;
	}

	public static RouteBuilder route() {
		return new RouteBuilder();
	}
}
