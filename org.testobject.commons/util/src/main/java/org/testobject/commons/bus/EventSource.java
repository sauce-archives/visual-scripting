package org.testobject.commons.bus;

/**
 * 
 * @author enijkamp
 *
 */
public interface EventSource {
	
	<T extends Event.Handler> Registration register(Class<? extends Event<T>> event, T handler);
	
	<T extends Event<?>> Registration register(Class<T> event, Dispatcher<T> dispatcher);
}