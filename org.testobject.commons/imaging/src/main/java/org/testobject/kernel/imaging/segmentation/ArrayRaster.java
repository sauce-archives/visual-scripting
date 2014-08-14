package org.testobject.kernel.imaging.segmentation;

import org.testobject.commons.math.algebra.Size;

/**
 * 
 * @author enijkamp
 *
 */
public class ArrayRaster implements BooleanRaster
{
    public final boolean[][] fg;
    public final Size.Int size;

    public ArrayRaster(final boolean[][] fg, Size.Int size)
    {
        this.fg = fg;
        this.size = size;
    }

    @Override
    public void set(int x, int y, boolean what)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Size.Int getSize()
    {
        return size;
    }

    @Override
    public boolean get(int x, int y)
    {
        return fg[y][x];
    }
}

