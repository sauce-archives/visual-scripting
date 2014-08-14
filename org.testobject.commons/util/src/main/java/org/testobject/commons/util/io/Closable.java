package org.testobject.commons.util.io;

/**
 * To be implemented by objects that have <code>close()</code> method.
 * 
 * @author enijkamp
 *
 */
public interface Closable {

	void close();
	
	class Factory {
		public static Closable stub() {
			return new Closable() {
				@Override
				public void close() {
					
				}
			};
		}
	}
	
}
    
 