package org.testobject.commons.bus;

/**
 * 
 * @author enijkamp
 *
 */
public interface Event<H extends Event.Handler> {
	
	interface Handler {}

	void dispatch(H handler);

}
