package org.testobject.commons.guice;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.testobject.commons.bus.Event;
import org.testobject.commons.bus.Event.Handler;
import org.testobject.commons.bus.Registration;
import org.testobject.commons.route.RouteUtils;

import com.google.inject.Injector;

public class AsyncGuiceProxy<E extends Event<H>, H extends Event.Handler> implements InvocationHandler {

	private interface RegisterAware {
		Registration register(Class<? extends Event<?>> clazz, Event.Handler handler);
	}

	public static class Factory {
		@SuppressWarnings("unchecked")
		public static <H extends Event.Handler> H async(Injector injector, Class<H> handler) {
			return (H) Proxy.newProxyInstance(AsyncGuiceProxy.class.getClassLoader(),
					new Class<?>[] { handler, AsyncGuiceProxy.RegisterAware.class },
					new AsyncGuiceProxy<>(injector, handler));
		}
	}

	private final Class<H> handlerClass;
	private final Injector injector;

	private final Map<Event.Handler, Class<? extends Event<?>>> handlers = new HashMap<>();

	public AsyncGuiceProxy(Injector injector, Class<H> handlerClass) {
		this.handlerClass = handlerClass;
		this.injector = injector;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getName().equals("register")) {
			@SuppressWarnings("unchecked")
			Class<? extends Event<?>> eventClass = (Class<? extends Event<?>>) args[0];
			return register(eventClass, (org.testobject.commons.bus.Event.Handler) args[1]);
		} else {
			H instance = withHandlers(injector.getInstance(handlerClass));
			method.invoke(instance, args);

			return null;
		}
	}

	public Registration register(Class<? extends Event<?>> eventClass, Event.Handler handler) {
		handlers.put(handler, eventClass);
		return new Registration() {
			public void unregister() {
				throw new UnsupportedOperationException();
			}
		};
	}

	private H withHandlers(H instance) {
		for (Entry<Handler, Class<? extends Event<?>>> entry : handlers.entrySet()) {
			try {
				Class<? extends Event<?>> eventClass = entry.getValue();
				Class<? extends Event.Handler> handlerClass = RouteUtils.getHandlerClass(eventClass);

				instance.getClass().getMethod("register", Class.class, handlerClass).invoke(instance, eventClass, entry.getKey());
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
					| SecurityException e) {
				throw new RuntimeException(e);
			}
		}
		return instance;
	}
}