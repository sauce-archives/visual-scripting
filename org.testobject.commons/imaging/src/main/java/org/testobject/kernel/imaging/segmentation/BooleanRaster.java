package org.testobject.kernel.imaging.segmentation;

import org.testobject.commons.math.algebra.Size;

/**
 *
 * @author enijkamp
 *
 */
public interface BooleanRaster
{

    /**
     *  returns rectangle describing valid ranges for (x, y)
     */
    Size.Int getSize();

    /**
     * Returns pixel value.
     *
     * @param x x-position of the pixel. Queries outside of bounding box should return background color (FALSE).
     * @param y y-position of the pixel. Queries outside of bounding box should return background color (FALSE).
     *
     * @return pixel color (true is foreground, false is background).
     */
    boolean get(int x, int y);

    /**
     * Sets pixel value. Its illegal to set a pixel outside of bounding box to anything but background.
     *
     * @param x
     * @param y
     * @param what
     */
    void set(int x, int y, boolean what);
}
