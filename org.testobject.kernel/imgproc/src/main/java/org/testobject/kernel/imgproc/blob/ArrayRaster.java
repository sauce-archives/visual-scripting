package org.testobject.kernel.imgproc.blob;

import java.awt.Dimension;

/**
 * 
 * @author nijkamp
 *
 */
public class ArrayRaster implements BooleanRaster
{
    public final boolean[][] fg;
    public final Dimension size;

    public ArrayRaster(final boolean[][] fg, Dimension size)
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
    public Dimension getSize()
    {
        return size;
    }

    @Override
    public boolean get(int x, int y)
    {
        return fg[y][x];
    }
}

