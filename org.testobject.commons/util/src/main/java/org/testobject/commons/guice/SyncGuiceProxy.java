package org.testobject.commons.guice;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.testobject.commons.bus.Event;
import org.testobject.commons.bus.EventBus;
import org.testobject.commons.bus.Register;
import org.testobject.commons.bus.Registration;
import org.testobject.commons.bus.SimpleEventBus;

import com.google.inject.Injector;

public class SyncGuiceProxy<E extends Event<H>, H extends Event.Handler> implements InvocationHandler {

	private interface RegisterAware {
		Registration register(Class<? extends Event<?>> event, Event.Handler handler);
	}

	public static class Factory {
		@SuppressWarnings("unchecked")
		public static <H extends Event.Handler> H sync(Injector injector, Class<H> handler, Class<? extends Event<?>> returnEventClass) {
			return (H) Proxy.newProxyInstance(SyncGuiceProxy.class.getClassLoader(),
					new Class<?>[] { handler, SyncGuiceProxy.RegisterAware.class },
					new SyncGuiceProxy<>(injector, handler, returnEventClass));
		}
	}

	private final EventBus bus = new SimpleEventBus();

	private final Class<H> handlerClass;
	private final Injector injector;
	private final Class<? extends Event<?>> returnEventClass;

	private SyncGuiceProxy(Injector injector, Class<H> handlerClass, Class<? extends Event<?>> returnEventClass) {
		this.handlerClass = handlerClass;
		this.injector = injector;
		this.returnEventClass = returnEventClass;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getName().equals("register")) {
			return register((org.testobject.commons.bus.Event.Handler) args[1]);
		} else {
			H instance = injector.getInstance(handlerClass);
			Object result = method.invoke(instance, args);

			Event<?> returnEvent = createReturnEvent(result, method.getReturnType());
			bus.fireEvent(returnEvent);

			return result;
		}
	}

	private Event<?> createReturnEvent(Object arg, Class<?> returnValueClass) {
		try {
			if(void.class.equals(returnValueClass)) {
				Class<?>[] argTypes = new Class<?>[]{};
				Constructor<? extends Event<?>> constructor = returnEventClass.getConstructor(argTypes);
				return constructor.newInstance();
			} else {
				Class<?>[] argTypes = new Class<?>[] { returnValueClass };
				Constructor<? extends Event<?>> constructor = returnEventClass.getConstructor(argTypes);
				return constructor.newInstance(arg);	
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException | InstantiationException e) {
			throw new RuntimeException(e);
		}
	}

	private Registration register(Event.Handler handler) {
		return Register.registerUnsafeHandler(bus, returnEventClass, handler);
	}

}