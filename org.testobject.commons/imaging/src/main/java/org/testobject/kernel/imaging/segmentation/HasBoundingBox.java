package org.testobject.kernel.imaging.segmentation;

import org.testobject.commons.math.algebra.Rectangle;

/**
 * Interface for the objects that have bounding box (Blobs, Blob Groups, and others).
 *
 * @author enijkamp
 */
public interface HasBoundingBox {
	
    Rectangle.Int getBoundingBox();
    
}
