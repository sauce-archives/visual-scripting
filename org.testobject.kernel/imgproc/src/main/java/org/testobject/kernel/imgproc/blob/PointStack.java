package org.testobject.kernel.imgproc.blob;

import java.awt.Point;
import java.util.LinkedList;

/**
 * Super fast stack implementation geared for storing points. Avoids small memory allocations.
 * 
 * @author enijkamp
 */
public class PointStack
{
    private final LinkedList<Object> free = new LinkedList<Object>();
    private final LinkedList<Object> stack = new LinkedList<Object>();

    private static final int INITIAL_CAPACITY = 128;

    private int[] data;
    private int top;
    private int size;

    public PointStack()
    {
        this(INITIAL_CAPACITY);
    }

    public PointStack(int cap)
    {
        data = new int[cap * 2];
    }

    public void push(Point p)
    {
        if (top >= data.length)
        {

            int[] d;
            if (free.isEmpty())
            {
                d = new int[data.length];
            } else
            {
                d = (int[]) free.removeLast();
            }

            stack.addLast(data);
            data = d;
            top = 0;
        }

        data[top++] = p.x;
        data[top++] = p.y;
        size++;
    }

    public boolean pop(Point out)
    {
        if (top <= 0)
        {
            if (stack.isEmpty())
            {
                return false;
            }

            free.addLast(data);
            data = (int[]) stack.removeLast();
            top = data.length;
        }

        out.y = data[--top];
        out.x = data[--top];
        size--;

        return true;
    }

    public int size()
    {
        return size;
    }
}
