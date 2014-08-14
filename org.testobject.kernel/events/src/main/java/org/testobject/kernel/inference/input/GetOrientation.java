package org.testobject.kernel.inference.input;

import org.testobject.commons.events.Orientation;
import org.testobject.commons.util.concurrency.Get;

/**
 * 
 * @author enijkamp
 *
 */
public interface GetOrientation extends Get<Orientation> {
	
	abstract class Factory {
		public static GetOrientation stub() {
			return new GetOrientation() {
				@Override
				public Orientation get() {
					return Orientation.PORTRAIT;
				}
			};
		}
	}
	
}