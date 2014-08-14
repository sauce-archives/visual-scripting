package org.testobject.commons.bus;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.testobject.commons.util.exceptions.Exceptions;

/**
 * Simple (and fast) event bus implementation. In most cases this is the only event bus implementation that one would ever need for
 * in-process message delivery.
 * 
 * This class is not thread-safe!
 * 
 */
public class SimpleEventBus extends AbstractEventBus {

	@Override
	public <T extends Event<?>> void fireEvent(T event) {
		@SuppressWarnings("unchecked")
		final Class<T> type = (Class<T>) getEventClass(event.getClass());
		fireEvent(type, event);
	}

	// FIXME messy (en)
	private Class<?> getEventClass(Class<?> cls) {
		for(Class<?> parent : cls.getInterfaces()) {
			if(parent == Event.class) return cls;
			if(isEvent(parent)) {
				return parent;
			}
		}
		
		throw Exceptions.newUnsupportedTypeException("event", cls);
	}

	private boolean isEvent(Class<?> cls) {
		if(Event.class.equals(cls)) {
			return true;
		}
		for(Class<?> parent : cls.getInterfaces()) {
			if(isEvent(parent)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public <T extends Event<?>> void fireEvent(final Class<T> type, final T event) {
		List<Throwable> exlist = null;

		final Map<Integer, Dispatcher<?>> hh = dispatcherByTypeById.get(type);
		if (hh != null) {
			for (Dispatcher<?> target : hh.values()) {
				try {
					@SuppressWarnings("unchecked")
					Dispatcher<T> typeSafeTarget = (Dispatcher<T>) target;
					typeSafeTarget.dispatch(type, event);
				} catch (Exception ex) {
					if (exlist == null) {
						exlist = new LinkedList<Throwable>();
					}
					exlist.add(ex);
				}
			}
		}

		if (exlist != null) {
			throw new UmbrellaException(exlist);
		}
	}
}