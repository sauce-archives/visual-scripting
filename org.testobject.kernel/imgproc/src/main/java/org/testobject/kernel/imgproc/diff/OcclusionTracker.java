package org.testobject.kernel.imgproc.diff;


import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.image.Image;

/**
 * 
 * @author enijkamp
 *
 */
public interface OcclusionTracker
{
	Rectangle.Int process(Image.Int before, Image.Int after, Rectangle.Int region);
}
