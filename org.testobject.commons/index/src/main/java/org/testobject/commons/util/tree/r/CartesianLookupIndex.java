package org.testobject.commons.util.tree.r;

import java.util.LinkedList;
import java.util.List;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.math.algebra.Rectangle.Double;

/**
 * 
 * @author enijkamp
 *
 */
public class CartesianLookupIndex<T> implements SpatialIndex<T> {
	
    public static class Factory<T> implements SpatialIndex.Factory<T> {
        @Override
        public SpatialIndex<T> create(Adapter<T> adapter) {
            return new CartesianLookupIndex<>(adapter);
        }
    }
	
	private final List<T> entries = new LinkedList<T>();
	private final Adapter<T> adapter;
	
	public CartesianLookupIndex(Adapter<T> adapter) {
		this.adapter = adapter;
	}

	@Override
	public int size() {
		return entries.size();
	}

	@Override
	public boolean isEmpty() {
		return entries.size() == 0;
	}

	@Override
	public Rectangle.Double getBounds() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void put(T element) {
		entries.add(element);
	}

	@Override
	public boolean remove(T element) {
		return entries.remove(element);
	}

	@Override
	public void nearest(org.testobject.commons.math.algebra.Point.Double point, double furthestDistance,
			org.testobject.commons.util.tree.r.SpatialIndex.Ordering ordering,
			org.testobject.commons.util.tree.r.SpatialIndex.Visitor<T> visitor) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void nearest(org.testobject.commons.math.algebra.Point.Double point, double furthestDistance, int n,
			org.testobject.commons.util.tree.r.SpatialIndex.Ordering ordering,
			org.testobject.commons.util.tree.r.SpatialIndex.Visitor<T> visitor) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void intersects(Double rect, org.testobject.commons.util.tree.r.SpatialIndex.Visitor<T> visitor) {
		for(T entry : entries) {
			Rectangle.Double box = adapter.getBoundingBox(entry);
			if(rect.intersects(box)) {
				visitor.visit(entry);
			}
		}
	}

	@Override
	public void contains(Double rect, org.testobject.commons.util.tree.r.SpatialIndex.Visitor<T> visitor) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<T> entries() {
		return entries;
	}

}
