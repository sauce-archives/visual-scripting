package org.testobject.commons.bus;

/**
 * 
 * @author enijkamp
 *
 */
public interface Dispatcher<T extends Event<?>> {

	void dispatch(Class<T> type, T event);

}