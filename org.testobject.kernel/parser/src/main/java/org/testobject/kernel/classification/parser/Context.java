package org.testobject.kernel.classification.parser;

import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.Image.Int;

/**
 * 
 * @author enijkamp
 *
 */
public interface Context {
	
	Image.Int raw();
	
	class Factory {
		public static Context none() {
			return new Context() {
				@Override
				public Int raw() {
					throw new UnsupportedOperationException();
				}
			};
		}
		
		public static Context create(final Image.Int raw) {
			return new Context() {
				@Override
				public Image.Int raw() {
					return raw;
				}
			};
		}
	}
}