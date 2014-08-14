package org.testobject.kernel.inference.occlusion;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.image.Image;

/**
 * 
 * @author enijkamp
 *
 */
public interface OcclusionTracker {
	
	Rectangle.Int process(Image.Int before, Image.Int after, Rectangle.Int damage);
	
	class Factory {
		
		public static OcclusionTracker identity() {
			return new IdentityOcclusionTracker();
		}
		
		public static OcclusionTracker stack() {
			return new StackOcclusionTracker();
		}
		
		public static OcclusionTracker duplex() {
			return new DuplexOcclusionTracker();
		}
		
	}
}
