package org.testobject.commons.util.tree.r;

import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Rectangle;

/**
 *
 * @author enijkamp
 */
public interface SpatialIndex<T> {

    interface Adapter<T> {

        Rectangle.Double getBoundingBox(T payload);
    }
    
    interface Factory<T> {

        SpatialIndex<T> create(Adapter<T> adapter);
    }

    interface Visitor<T> {

        boolean visit(T payload);
    }


    enum Ordering {
        Sorted,
        Unsorted;
    }

    int size();

    boolean isEmpty();

    Rectangle.Double getBounds();

    void put(T element);

    boolean remove(T element);

    void nearest(Point.Double point, double furthestDistance, Ordering ordering, Visitor<T> visitor);

    void nearest(Point.Double point, double furthestDistance, int n, Ordering ordering, Visitor<T> visitor);

    void intersects(Rectangle.Double rect, Visitor<T> visitor);

    void contains(Rectangle.Double rect, Visitor<T> visitor);

    Iterable<T> entries();

}