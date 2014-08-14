package org.testobject.commons.route;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.testobject.commons.bus.Dispatcher;
import org.testobject.commons.bus.Emits;
import org.testobject.commons.bus.Event;
import org.testobject.commons.bus.HasEvents;
import org.testobject.commons.bus.Registration;

/**
 * 
 * @author enijkamp
 * 
 */
public class RouteUtils {

	public static <E extends Event<? extends Event.Handler>> Class<? extends Event.Handler> getHandlerClass(Class<E> event) {
		Class<? extends Event.Handler> cls = getHandlerClassRecursive(event);
		if (cls != null) {
			return cls;
		}
		throw new IllegalArgumentException(event.getSimpleName() + " does not implement " + Event.class.getSimpleName());
	}

	private static <E extends Event<? extends Event.Handler>> Class<? extends Event.Handler> getHandlerClassRecursive(Class<E> event) {
		for (Type type : event.getGenericInterfaces()) {

			if (type instanceof ParameterizedType) {
				ParameterizedType eventType = (ParameterizedType) type;
				@SuppressWarnings("unchecked")
				Class<? extends Event.Handler> handlerClass = (Class<? extends Event.Handler>) eventType.getActualTypeArguments()[0];
				if (Event.Handler.class.isAssignableFrom(handlerClass)) {
					return handlerClass;
				} else {
					Type raw = eventType.getRawType();
					if (raw instanceof Class<?>) {
						@SuppressWarnings("unchecked")
						Class<? extends Event.Handler> cls = getHandlerClassRecursive((Class<E>) raw);
						if (cls != null) {
							return cls;
						}
					}
				}
			}

			if (type.equals(Class.class)) {
				@SuppressWarnings("unchecked")
				Class<? extends Event.Handler> cls = getHandlerClassRecursive((Class<E>) type);
				if (cls != null) {
					return cls;
				}
			}
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Registration register(Class<? extends Event<?>> eclass, Class<? extends Event.Handler> hclass, Object source,
			Object target)
	{
		final boolean isDispatcher = Dispatcher.class.isAssignableFrom(target.getClass());
		final boolean isHandler = hclass.isAssignableFrom(target.getClass());
		
		
		if(isDispatcher) {
			
			final Class<Event> event = (Class<Event>)(Object) eclass;
			final Dispatcher dispatcher = (Dispatcher)(Object) target;
			
			if(source instanceof HasEvents) {
				HasEvents hasEvents = (HasEvents) source;
				
				Set<Class<? extends Event<?>>> emittedEvents = extractSupportedEvents(source);
				if(emittedEvents.contains(eclass) == false) {
					throw new IllegalArgumentException("source '" + source.getClass().getName() + "' does not emit event '" + eclass.getName() + "'");
				}
				
				return hasEvents.getEventSource().register(event, dispatcher);
			} else {
				
				// FIXME somehow consolidate handlers and dispatcher ... (en)
				Method handlerRegistrationMethod = extractRegisterDispatcherMethod(source);
				return invokeRegisterMethod(eclass, source, dispatcher, handlerRegistrationMethod);
			}
		}
		
		if(isHandler) {
			if(source instanceof HasEvents) {
				HasEvents emitter = (HasEvents) source;
				
				Set<Class<? extends Event<?>>> emittedEvents = extractSupportedEvents(source);
				if(emittedEvents.contains(eclass) == false) {
					throw new IllegalArgumentException("source '" + source.getClass().getName() + "' does not emit event '" + eclass.getName() + "'");
				}
				
				return emitter.getEventSource().register((Class<Event<Event.Handler>>) eclass, (Event.Handler) target);
			} else {
				Method handlerRegistrationMethod = extractRegisterMethod(eclass, hclass, source);
				return invokeRegisterMethod(eclass, source, target, handlerRegistrationMethod);
			}
		}
		
		throw new IllegalArgumentException("target class " + target.getClass().getName() + " does not implement handler class "
				+ hclass.getName() + " nor dispatcher interface");
	}
	
	private static Set<Class<? extends Event<?>>> extractSupportedEvents(Object object) {
		Emits emits = object.getClass().getAnnotation(Emits.class);
        if(emits == null){
            throw new IllegalArgumentException("class '" + object.getClass() + "' must be annotated with @Emits");
        }
		Class<? extends Event<?>>[] events = emits.value();
		return fromArray(events);
	}
	
	@SafeVarargs
	public static <T> Set<T> fromArray(T ... objects) {
		Set<T> result = new HashSet<>();
		for (T object : objects) {
			result.add(object);
		}

		return Collections.unmodifiableSet(result);
	}

	private static Registration invokeRegisterMethod(Class<? extends Event<?>> eclass, Object source, Object target,
			Method handlerRegistrationMethod) {
		try
		{
			return (Registration) handlerRegistrationMethod.invoke(source, new Object[] { eclass, target });
		} catch (final IllegalArgumentException e)
		{
			throw e;
		} catch (final Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private static Method extractRegisterDispatcherMethod(Object source) {
		Method handlerRegistrationMethod = null;
		for (final Method m : source.getClass().getMethods())
		{
			if (m.getReturnType().equals(Registration.class))
			{
				if (m.getParameterTypes().length == 2)
				{
					final Class<?> eventClass = m.getParameterTypes()[0];
					if (eventClass instanceof Class == false)
					{
						continue;
					}

					final Class<?> handlerClass = m.getParameterTypes()[1];
					if (Dispatcher.class.isAssignableFrom(handlerClass))
					{
						if (handlerRegistrationMethod == null)
						{
							handlerRegistrationMethod = m;
						} else {
							throw new IllegalArgumentException("ambiguity: both " + m.getName() + " and "
									+ handlerRegistrationMethod.getName() + " methods of class " + source.getClass().getName()
									+ " expose the event dispatcher ");
						}
					}
				}

			}
		}
		if (handlerRegistrationMethod == null)
		{
			throw new IllegalArgumentException("event not found: class " + source.getClass().getName() + " does not expose the dispatcher");
		}
		return handlerRegistrationMethod;
	}
	
	private static Method extractRegisterMethod(Class<? extends Event<?>> eclass, Class<? extends Event.Handler> hclass, Object source) {
		Method handlerRegistrationMethod = null;
		for (final Method m : source.getClass().getMethods())
		{
			if (m.getReturnType().equals(Registration.class))
			{
				if (m.getParameterTypes().length == 2)
				{
					final Class<?> eventClass = m.getParameterTypes()[0];
					if (eventClass instanceof Class == false)
					{
						continue;
					}

					final Class<?> handlerClass = m.getParameterTypes()[1];
					if (handlerClass.isAssignableFrom(hclass))
					{
						if (handlerRegistrationMethod == null)
						{
							handlerRegistrationMethod = m;
						}
						else
						{
							throw new IllegalArgumentException("ambiguity: both " + m.getName() + " and "
									+ handlerRegistrationMethod.getName() + " methods of class " + source.getClass().getName()
									+ " expose the event handler " + hclass.getName());
						}
					}
				}

			}
		}
		if (handlerRegistrationMethod == null)
		{
			throw new IllegalArgumentException("event not found: class " + source.getClass().getName()
					+ " does not expose the event handler " + hclass.getName());
		}
		return handlerRegistrationMethod;
	}
}
