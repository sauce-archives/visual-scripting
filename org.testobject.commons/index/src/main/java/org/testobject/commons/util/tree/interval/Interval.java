package org.testobject.commons.util.tree.interval;

/**
 * Represents closes integer interval [start, end].
 * 
 * @author mike
 */
public final class Interval
{
    public final int start;
    public final int end;

    public Interval(int start, int end)
    {
        this.start = start;
        this.end = end;
    }

    public boolean overlaps(Interval other)
    {
        return overlaps(other.start, other.end);
    }

    public boolean overlaps(int start, int end)
    {
        return start <= this.end && this.start <= end;
    }
    
    public static boolean overlap(Interval a, Interval b)
    {
        return a.overlaps(b);
    }
    
    public static boolean equal(Interval a, Interval b)
    {
        return a.start == b.start && a.end == b.end;
    }
    
    @Override
    public boolean equals(Object other)
    {
        return equal(this, (Interval) other);
    }
    
    @Override
    public int hashCode()
    {
        return start ^ end;
    }
    
    @Override
    public String toString()
    {
        return "[" + start  + ", " + end + "]";
    }
}
