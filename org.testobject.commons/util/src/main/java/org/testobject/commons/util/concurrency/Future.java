package org.testobject.commons.util.concurrency;

/**
 * A Future represents the result of an asynchronous computation.
 * 
 * @author enijkamp
 *
 */
public interface Future<T> {
	
	T get();

}
