package org.testobject.kernel.imaging.segmentation;

import java.awt.Point;
import java.util.LinkedList;

/**
 * Super fast stack implementation geared for storing points. Avoids small memory allocations.
 * 
 * @author mike
 */
public class PointStack {
	
	private static final int INITIAL_CAPACITY = 128;

	private final LinkedList<int[]> free = new LinkedList<>();
    private final LinkedList<int[]> stack = new LinkedList<>();

    private int[] data;
    private int top;
    private int size;

    public PointStack() {
        this(INITIAL_CAPACITY);
    }

    public PointStack(int cap) {
        data = new int[cap * 2];
    }

    public void push(Point p)
    {
        if (top >= data.length) {

            int[] d;
            if (free.isEmpty()) {
                d = new int[data.length];
            } else {
                d = free.removeLast();
            }

            stack.addLast(data);
            data = d;
            top = 0;
        }

        data[top++] = p.x;
        data[top++] = p.y;
        size++;
    }

    public boolean pop(Point out) {
        if (top <= 0) {
            if (stack.isEmpty()) {
                return false;
            }

            free.addLast(data);
            data = stack.removeLast();
            top = data.length;
        }

        out.y = data[--top];
        out.x = data[--top];
        size--;

        return true;
    }

    public int size() {
        return size;
    }
}