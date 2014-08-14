package org.testobject.commons.util.concurrency;



/**
 * 
 * @author enijkamp
 *
 */
public interface Sequence<T> {
	
	boolean hasNext();
	
	T next();

}
