package org.testobject.kernel.inference.occlusion;

import org.testobject.commons.math.algebra.Rectangle;

/**
 * 
 * @author enijkamp
 *
 */
public class IdentityOcclusionTracker implements OcclusionTracker {

	@Override
	public Rectangle.Int process(org.testobject.commons.util.image.Image.Int before, org.testobject.commons.util.image.Image.Int after, Rectangle.Int damage) {
		return damage;
	}

}
