package org.testobject.kernel.imgproc.blob;

import java.awt.Point;
import java.util.EnumSet;

/**
 * 
 * @author enijkamp
 *
 */
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
