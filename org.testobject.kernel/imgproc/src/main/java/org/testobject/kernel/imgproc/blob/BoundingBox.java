package org.testobject.kernel.imgproc.blob;

import java.awt.Rectangle;

/**
 * Interface for the objects that have bounding box (Blobs, Blob Groups, and others).
 *
 * @author enijkamp
 */
public interface BoundingBox
{
    Rectangle getBoundingBox();
}
