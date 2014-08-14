package org.testobject.kernel.imaging.segmentation;

import org.testobject.commons.math.algebra.Size;

import java.awt.Point;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author mike
 *
 */
public class FillBlobBuilder
{
	public enum Direction
	{

	    E(1, 0), W(-1, 0), S(0, 1), N(0, -1), NW(-1, -1), NE(1, -1), SW(-1, 1), SE(1, 1);

	    private final int deltax;
	    private final int deltay;

	    private Direction(int deltax, int deltay)
	    {
	        this.deltax = deltax;
	        this.deltay = deltay;
	    }

	    public Point move(Point base)
	    {
	        return new Point(base.x + this.deltax, base.y + this.deltay);
	    }

	    public void move(Point base, Point out)
	    {
	        out.x = base.x + deltax;
	        out.y = base.y + deltay;
	    }

	    public static final EnumSet<Direction> NSWE = EnumSet.of(E, W, N, S);
	    public static final EnumSet<Direction> ALL = EnumSet.allOf(Direction.class);
	}
	
    private final BooleanRaster raster;
    private final int[][] ids;
    private int blobIdSource = 1;

    private final PointStack bg = new PointStack();
    private final PointStack fg = new PointStack();
    
    private FillBlobBuilder(BooleanRaster raster)
    {
        this.raster = raster;
        this.ids = new int[raster.getSize().h][raster.getSize().w];
    }

    /*
     * We start with the raster. Conceptually, we'll consider pixels outside of the raster as background pixels (though algorithm is guaranteed not to
     * query raster outside of it dimensions.
     * 
     * For every pixel in the raster we store its "id" - an integer that will uniquely identify which blob contains this pixel
     * (it is blob's id). We use rectangular integer array to store this information.
     * 
     * Initially, all raster pixels have id '0', which is a special value, telling us that pixel was not yet
     * classified. 
     * 
     * The process of locating connected blobs starts with a collection of "seeds" - pixels of one color (foreground or background). All seed
     * pixels are initially uncategorized. What is important is that they all belong to one or more blobs **at the same level of hierarchy**.
     * 
     * Algorithm starts with one unclassified seed point and assigns new blob id to it. Then it does bucket-fill search, assigning same id to all directly
     * connected pixels. When bucket-fill completes, we check the next seed point. We remove all seeds that were classified, and find the next 
     * unclassified one. If found, we generate new blob id and assign to this pixel, then, again, run a bucket-fill algorithm looking for connected
     * uncategorized pixels of the same color. And so on, until we exhausted all seed pixels. This will find us all blobs at this hierarchy level.
     * 
     * Important: when bucket-fill meets uncategorized pixel of the wrong color, it pushes it to the "hole" stack for this blob. When bucket-fill search completes,
     * this hole stack may contain some points. If it does, then they are used to locate set of unconnected blobs of the complimentary color - using the same 
     * exact algorithm recursively.
     * 
     * Putting it all together: we start with a single "seed" pixel on the outside border. This pixel has background color. We run blob extraction, expecting that it 
     * will return a list containing single blob (as border is fully connected). It also returns set of foreground seeds (could be empty). All seeds in the set are
     * "inside" the blob. Then, for every uncategorized point in the seed collection we run blob extraction (this time we'll be looking for connected pixels of
     * foreground color). For every detected blob we will get set of "inner" seed points, that are used to find contained blobs. And so on, and so forth.
     * Its a recursive procedure with recursion level equal to the h of containment hierarchy.
     * 
     * Important note on bucket-fill. We assume that diagonally-positioned foreground pixels represent a line, and are connected. And, complimentary, we want
     * the background areas on each side of this diagonal to be separate. Therefore, we use slightly different bucket-fill algorithm depending on which color 
     * we fill: if its background, we check only North, South, West, and East directions; for foreground we also check North-East, North-West, South-east, 
     * and South-West.
     */
    private List<Blob> process(int count, boolean color)
    {
        List<Blob> blobs = new LinkedList<Blob>();

        final PointStack seeds = color == true ? fg : bg;
        final PointStack holes = color == false ? fg : bg;

        // step 1: bucket-fill using all the seeds, finding potential inner blob seeds
        Point point = new Point();

        int visitedArea = 0;
        while (count > 0)
        {
            seeds.pop(point);
            count--;

            // discard all "seen" seeds
            if (ids[point.y][point.x] != 0)
            {
                // already classified
                continue;
            }

            // unseen seed (new blob!)
            int id = blobIdSource++;

            org.testobject.commons.math.algebra.Rectangle.Int minmax = new org.testobject.commons.math.algebra.Rectangle.Int(point.x, point.y, 1, 1);

            seeds.push(point);

            int holeCount = holes.size();
            int area = floodFill(1, minmax, color, id);
            holeCount = holes.size() - holeCount;
            
            visitedArea += area;
            if(visitedArea > 1024)
            {
                // do not call progress meter too often
                visitedArea = 0;
            }

            // here we may have non-empty hole seeds. recursively process them
            List<Blob> children = process(holeCount, !color);

            // blobs.add(new Blob(id, color ? Blob.Color.FG : Blob.Color.BG, minmax, area, children, ids));
            blobs.add(new Blob(id, minmax, area, children, ids));
        }
        
        return blobs;
    }

    private int floodFill(int count, org.testobject.commons.math.algebra.Rectangle.Int limits, boolean color, int id)
    {
        final Point me = new Point();
        final Point probe = new Point();
        final Size.Int size = raster.getSize();
        final EnumSet<Direction> directions = color ? Direction.ALL : Direction.NSWE;

        final PointStack seeds = color == true ? fg : bg;
        final PointStack holes = color == false ? fg : bg;

        int area = 0;
        int minx = Integer.MAX_VALUE;
        int maxx = -1;
        int miny = Integer.MAX_VALUE;
        int maxy = -1;

        while (count > 0)
        {
            seeds.pop(me);
            count--;

            if (ids[me.y][me.x] != 0)
            {
                continue;
            }

            ids[me.y][me.x] = id;
            area++; // statistics

            // update bounding box
            minx = Math.min(minx, me.x);
            maxx = Math.max(maxx, me.x);
            miny = Math.min(miny, me.y);
            maxy = Math.max(maxy, me.y);

            for (Direction dir : directions)
            {
                dir.move(me, probe); // in-place

                if (probe.x >= 0 && probe.x < size.w && probe.y >= 0 && probe.y < size.h && ids[probe.y][probe.x] == 0)
                {

                    if (raster.get(probe.x, probe.y) == color)
                    {
                        // foreground! move there
                        seeds.push(probe);
                        count++;
                    } else
                    {
                        // background, unseen - hole in this blob
                        holes.push(probe);
                    }
                }
            }
        }

        limits.x = minx;
        limits.y = miny;
        limits.w = maxx - minx + 1;
        limits.h = maxy - miny + 1;

        return area;
    }

    private int bootstrap()
    {
        final Size.Int size = raster.getSize();
        final Point p = new Point();

        // top band
        p.y = 0;
        for (int x = 0; x < size.w; x++)
        {
            p.x = x;
            if (raster.get(p.x, p.y) == true)
            {
                fg.push(p);
            } 
            else
            {
                bg.push(p);
            }
        }

        // bottom band
        if (size.h > 1)
        {
            p.y = size.h - 1;
            for (int x = 0; x < size.w; x++)
            {
                p.x = x;
                if (raster.get(p.x, p.y) == true)
                {
                    fg.push(p);
                } 
                else
                {
                    bg.push(p);
                }
            }
        }

        // left band
        p.x = 0;
        for (int y = 0; y < size.h; y++)
        {
            p.y = y;
            if (raster.get(p.x, p.y) == true)
            {
                fg.push(p);
            } 
            else
            {
                bg.push(p);
            }
        }

        // right band
        if (size.w > 1)
        {
            p.x = size.w - 1;
            for (int y = 0; y < size.h; y++)
            {
                p.y = y;
                if (raster.get(p.x, p.y) == true)
                {
                    fg.push(p);
                } 
                else
                {
                    bg.push(p);
                }
            }
        }

        return bg.size();
    }

    private Blob detectAllBlobs()
    {
        // int totalArea = raster.getSize().w * raster.getSize().h;
        
        int count = bootstrap();

        int area = floodFill(count, new org.testobject.commons.math.algebra.Rectangle.Int(), false, blobIdSource++); // note blob id '1'

        List<Blob> blobs = process(fg.size(), true); // note that first blob id is '2'. 

        Size.Int size = raster.getSize();

        // return new Blob(1, Blob.Color.BG, new Rectangle(0, 0, size.w, size.h), area, blobs, ids);
        return new Blob(1, new org.testobject.commons.math.algebra.Rectangle.Int(0, 0, size.w, size.h), area, blobs, ids);
    }

    /**
     * Same as {@link #detectAllBlobs(BooleanRaster, ProgressMeter)}, but does not 
     * provide any ability to track progrees of operation (for upward-compatibility).
     * 
     * @param raster read-only (black-and-white) raster
     * @return Blob hierarchy.
     */
    public static Blob detectAllBlobs(BooleanRaster raster)
    {
        return new FillBlobBuilder(raster).detectAllBlobs();
    }

}
