package org.testobject.kernel.imgproc.blob;

import java.awt.Rectangle;

/**
 * Histogram is a projection of all blob pixels onto X or Y axis. May help in finding aligned rows/columns.
 * 
 * @author enijkamp
 *
 */
public class Histogram
{
    public static int [] computeHistogramX(Iterable<? extends BoundingBox> boxes) 
    {
        int minx = Integer.MAX_VALUE;
        int maxx = Integer.MIN_VALUE;
        
        for(BoundingBox b : boxes)
        {
            Rectangle r = b.getBoundingBox();
            
            minx = Math.min(minx, r.x);
            maxx = Math.max(maxx, r.x + r.width - 1);
        }
        
        if(minx > maxx)
        {
            return new int[] {};
        }
        
        System.err.println("" + maxx + " " + minx);
        int [] hist = new int[maxx - minx + 1];

        for(BoundingBox b : boxes)
        {
            Rectangle r = b.getBoundingBox();
            
            for(int i = 0; i < r.width; i++)
            {
                hist[r.x - minx + i] += r.height;
            }
        }
        
        return hist;
    }

    public static int [] computeHistogramY(Iterable<? extends BoundingBox> boxes) 
    {
        int miny = Integer.MAX_VALUE;
        int maxy = Integer.MIN_VALUE;
        
        for(BoundingBox b : boxes)
        {
            Rectangle r = b.getBoundingBox();
            
            miny = Math.min(miny, r.y);
            maxy = Math.max(maxy, r.y + r.height - 1);
        }
        
        if(miny > maxy)
        {
            return new int[] {};
        }
        
        int [] hist = new int[maxy - miny + 1];

        for(BoundingBox b : boxes)
        {
            Rectangle r = b.getBoundingBox();
            
            for(int i = 0; i < r.height; i++)
            {
                hist[r.y - miny + i] += r.width;
            }
        }
        
        return hist;
    }
}
