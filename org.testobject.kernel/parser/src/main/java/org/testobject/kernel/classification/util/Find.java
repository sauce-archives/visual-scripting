package org.testobject.kernel.classification.util;

import java.util.LinkedList;
import java.util.List;

import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.collections.Lists;
import org.testobject.kernel.imaging.segmentation.Mask;

/**
 * 
 * @author enijkamp
 *
 */
public class Find<T> {
	
	public static interface Adapter<T> {
		
		LinkedList<T> at(Point.Int location);
		
		List<T> childs(T parent);
		
		boolean isGroup(T t);
		
		Rectangle.Double toBox(T t);
		
		Mask toMask(T t);
	}
	
	public static class Result<T> {
		
		public final boolean assigned;
		public final Point.Int location;
		public final Mask overMask;
		public final Mask touchMask;
		public final Rectangle.Double[] boxes;
		public final Rectangle.Double[] scaled;
		public final int index;
		public final LinkedList<T> path;
		
		public Result(boolean assigned, Point.Int location, Mask overMask, Mask touchMask, Rectangle.Double[] boxes, Rectangle.Double[] scaled, int index, LinkedList<T> path) {
			this.assigned = assigned;
			this.location = location;
			this.overMask = overMask;
			this.touchMask = touchMask;
			this.boxes = boxes;
			this.scaled = scaled;
			this.index = index;
			this.path = path;
		}
	}
	
	public static final int unassigned = -1;
	
	private final Adapter<T> adapter;
	
	public Find(Adapter<T> adapter) {
		this.adapter = adapter;
	}

	public Result<T> at(double x, double y) {

		// find
		Point.Int location = point((int) x, (int) y);
		LinkedList<T> parentPath = adapter.at(location);
		if(parentPath.isEmpty()) {
			return new Result<T>(false, location, null, null, null, null, unassigned, parentPath);
		}
		T parent = parentPath.getLast();
		List<T> childs = adapter.childs(parent);
		
		// childs
		if(isRoot(parentPath) == false && (childs.isEmpty() || hasFlatChilds(parent) || isFlatGroup(parent))) {
			
			// fit
			Rectangle.Double[] boxes = { adapter.toBox(parent) };
			double[] scales = fit(boxes, location);
			int index = boxes.length > 0 ? best(scales) : unassigned;
			Rectangle.Double[] scaled = scale(boxes, scales);
			
			// masks
			Mask overMask = adapter.toMask(parent);
			Mask touchMask = adapter.toMask(parent);
			
			return new Result<T>(index != unassigned, location, overMask, touchMask, boxes, scaled, index, parentPath);
			
		} else {
			
			// fit
			Rectangle.Double[] boxes = toBoxes(childs);
			double[] scales = fit(boxes, location);
			Rectangle.Double[] scaled = scale(boxes, scales);
			Mask overMask = adapter.toMask(parent);
			int index = boxes.length > 0 ? best(scales) : unassigned;
			
			if(index != unassigned) {
				T object = childs.get(index);
				Mask touchMask = adapter.toMask(object);
				
				return new Result<T>(true, location, overMask, touchMask, boxes, scaled, index, Lists.concat(parentPath, object));
			} else {
				return new Result<T>(false, location, overMask, null, boxes, scaled, index, parentPath);
			}
		}
	}

	private boolean isRoot(LinkedList<T> parentPath) {
		return parentPath.size() == 1;
	}

	private boolean hasFlatChilds(T parent) {
		for(T child : adapter.childs(parent)) {
			if(adapter.childs(child).size() > 0) {
				return false;
			}
		}
		
		return true;
	}

	private boolean isFlatGroup(T parent) {
		if(adapter.isGroup(parent) == false) {
			return false;
		}
		
		for(T child : adapter.childs(parent)) {
			if(adapter.childs(child).size() > 0) {
				return false;
			}
		}
		
		return true;
	}

	private Rectangle.Double[] toBoxes(List<T> childs) {
		Rectangle.Double[] boxes = new Rectangle.Double[childs.size()];
		for(int i = 0; i < childs.size(); i++) {
			boxes[i] = adapter.toBox(childs.get(i));
		}
		
		return boxes;
	}

	private static int best(double[] values) {
		double min = Double.MAX_VALUE;
		int index = 0;
		for (int i = 0; i < values.length; i++) {
			if (values[i] < min) {
				index = i;
				min = values[i];
			}
		}
		return index;
	}

	private static double[] fit(Rectangle.Double[] boxes, Point.Int click) {
		double[] scale = new double[boxes.length];
		for (int i = 0; i < boxes.length; i++) {
			Rectangle.Double box = boxes[i];

			double cx = box.getCenterX();
			double w = box.getWidth();
			double dx = Math.abs(click.x - cx) - (w / 2);
			double sx = 1 + (dx * 2 / w);

			double cy = box.getCenterY();
			double h = box.getHeight();
			double dy = Math.abs(click.y - cy) - (h / 2);
			double sy = 1 + (dy * 2 / h);

			scale[i] = Math.max(sx, sy);
		}
		return scale;
	}
	
	private static Point.Int point(int x, int y) {
		return new Point.Int(x, y);
	}
	
	private static Rectangle.Double[] scale(Rectangle.Double[] boxes, double[] scales) {
		Rectangle.Double[] scaled = new Rectangle.Double[boxes.length];
		for(int i = 0; i < boxes.length; i++) {
			double s = scales[i];
			Rectangle.Double box = boxes[i];
			
			double x = box.x;
			double y = box.y;
			double w = box.w;
			double h = box.h;
			
			double nw = w * s;
			double nh = h * s;
			double nx = x - (nw - w) / 2;
			double ny = y - (nh - h) / 2;
			
			scaled[i] = new Rectangle.Double(nx, ny, nw, nh);
		}
		return scaled;	
	}
}