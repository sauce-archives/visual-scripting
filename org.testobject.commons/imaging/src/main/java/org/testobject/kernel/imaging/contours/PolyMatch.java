package org.testobject.kernel.imaging.contours;

import java.awt.Rectangle;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.testobject.commons.util.config.Debug;
import org.testobject.commons.math.algebra.Point;

/**
 * 
 * @author enijkamp
 *
 */
//TODO polymatch does not take relative size of sub-contours into account (e.g. camera lense is smaller than frame) (en)
public class PolyMatch {
	
	public static final boolean DEBUG = Debug.toDebugMode(false);
	
	// TODO distance is not symmetric -> match(contours_1, contours_2) != match(contours_2, contours_1)
	public static double matchPairwise(List<List<Point.Double>> contours_1, List<List<Point.Double>> contours_2) {

		double sum = 0d;

		for (List<Point.Double> contour_2 : contours_2) {
			double min = Double.MAX_VALUE;
			for (List<Point.Double> contour_1 : contours_1) {
				min = Math.min(min, match(contour_1, contour_2));
			}
			sum += min;
		}

		return sum;
	}

	/*
	private static void show(LinkedList<Point.Double> points) {
		BufferedImage bi = new BufferedImage(105, 105, BufferedImage.TYPE_INT_RGB);
		Graphics g = bi.getGraphics();

		for (int i = 1; i < points.size(); i++) {
			g.drawLine((int) (points.get(i - 1).x * 100f) + 2, (int) (points.get(i - 1).y * 100f) + 2, (int) (points.get(i).x * 100f) + 2,
			        (int) (points.get(i).y * 100f) + 2);
		}
		g.drawLine((int) (points.getLast().x * 100f) + 2, (int) (points.getLast().y * 100f) + 2, (int) (points.getFirst().x * 100f) + 2,
		        (int) (points.getFirst().y * 100f) + 2);
		VisualizerUtil.show("points", bi);
	}
	
	public static void showRelative(String title, List<Point.Double> points) {
		
		Rectangle.Double bounds = bounds(points);
		
		BufferedImage bi = new BufferedImage((int)(bounds.w * 100f + 5), (int)(bounds.h * 100f + 5), BufferedImage.TYPE_INT_RGB);
		Graphics g = bi.getGraphics();

		for (int i = 1; i < points.size(); i++) {
			g.drawLine((int) (points.get(i - 1).x * 100f) + 2, (int) (points.get(i - 1).y * 100f) + 2, (int) (points.get(i).x * 100f) + 2,
			        (int) (points.get(i).y * 100f) + 2);
		}
		g.drawLine((int) (points.get(points.size()-1).x * 100f) + 2, (int) (points.get(points.size()-1).y * 100f) + 2, (int) (points.get(0).x * 100f) + 2,
		        (int) (points.get(0).y * 100f) + 2);
		VisualizerUtil.show(title, bi);
	}
	*/
	
	private static Rectangle.Double bounds(LinkedList<Point.Double> points) {
		double w = 0, h = 0;
		for(Point.Double point : points) {
			w = Math.max(w, point.x);
			h = Math.max(h, point.y);
		}
		return new Rectangle.Double(0, 0, w, h);
	}
	
	public static double match(List<Point.Double> points_1, List<Point.Double> points_2) {

		if (points_1.isEmpty() || points_2.isEmpty()) {
			throw new IllegalStateException();
		}

		LinkedList<Point.Double> norm_points_1 = resizeRelative(points_1);
		LinkedList<Point.Double> norm_points_2 = resizeRelative(points_2);

		double sum = 0d;

		for (Point.Double p1 : norm_points_1) {
			double min = Double.MAX_VALUE;
			Iterator<Point.Double> iter = norm_points_2.listIterator();
			Point.Double l1 = iter.next();
			while (iter.hasNext()) {
				Point.Double l2 = iter.next();
				min = Math.min(min, distancePointLine(p1, l1, l2));
				l1 = l2;
			}
			min = Math.min(min, distancePointLine(p1, norm_points_2.getLast(), norm_points_2.getFirst()));
			sum += min;
		}

		for (Point.Double p2 : norm_points_2) {
			double min = Double.MAX_VALUE;
			Iterator<Point.Double> iter = norm_points_1.listIterator();
			Point.Double l1 = iter.next();
			while (iter.hasNext()) {
				Point.Double l2 = iter.next();
				min = Math.min(min, distancePointLine(p2, l1, l2));
				l1 = l2;
			}
			min = Math.min(min, distancePointLine(p2, norm_points_1.getLast(), norm_points_1.getFirst()));
			sum += min;
		}
		
		/*
		if(DEBUG) {
			showRelative("points 1 ("+sum+")", norm_points_1);
			showRelative("points 2 ("+sum+")", norm_points_2);
		}
		*/

		return sum;
	}

	private static LinkedList<Point.Double> resizeRelative(List<Point.Double> points) {

		LinkedList<Point.Double> result = new LinkedList<>();

		Rectangle.Double bounds = bounds(points);
		
		if(bounds.width == 0 || bounds.height == 0) {
			
			result.addAll(points);
			
		} else {
			double scale = 1d / Math.max(bounds.width, bounds.height);
			
			for (Point.Double point : points) {
				double x = point.x;
				double y = point.y;
				x -= bounds.x;
				y -= bounds.y;
				x *= scale;
				y *= scale;
				
				result.add(new Point.Double(x, y));
			}
		}
		
		return result;
	}

	private static LinkedList<Point.Double> resizeQuadratic(List<Point.Double> points) {

		LinkedList<Point.Double> result = new LinkedList<Point.Double>();

		Rectangle.Double bounds = bounds(points);
		for (int i = 0; i < points.size(); i++) {
			double x = points.get(i).x;
			double y = points.get(i).y;
			x -= bounds.x;
			y -= bounds.y;
			x /= bounds.width;
			y /= bounds.height;
			result.add(new Point.Double(x, y));
		}

		return result;
	}

	private static Rectangle.Double bounds(List<Point.Double> points) {
		double min_x = Integer.MAX_VALUE, min_y = Integer.MAX_VALUE;
		double max_x = Integer.MIN_VALUE, max_y = Integer.MIN_VALUE;
		for (int i = 0; i < points.size(); i++) {
			min_x = Math.min(min_x, points.get(i).x);
			min_y = Math.min(min_y, points.get(i).y);
			max_x = Math.max(max_x, points.get(i).x);
			max_y = Math.max(max_y, points.get(i).y);
		}
		return new Rectangle.Double(min_x, min_y, max_x - min_x, max_y - min_y);
	}

	private static double distancePointPoint(Point.Double p1, Point.Double p2) {
		double dx = p1.x - p2.x;
		double dy = p1.y - p2.x;
		return Math.sqrt(dx * dx + dy * dy);
	}

	private static double distancePointLine(Point.Double p, Point.Double l1, Point.Double l2)
	{
		if (l1.x == l2.x && l1.y == l2.y)
			return 0d;

		if (l1.x == p.x && l1.y == p.y)
			return 0d;

		if (l2.x == p.x && l2.y == p.y)
			return 0d;

		/*(1)     	      AC dot AB
		            r =   ---------
		                  ||AB||^2
		 
		            r has the following meaning:
		            r=0 Point = A
		            r=1 Point = B
		            r<0 Point is on the backward extension of AB
		            r>1 Point is on the forward extension of AB
		            0<r<1 Point is interior to AB
		*/

		double r = ((p.x - l1.x) * (l2.x - l1.x) + (p.y - l1.y) * (l2.y - l1.y))
		        /
		        ((l2.x - l1.y) * (l2.x - l1.x) + (l2.y - l1.y) * (l2.y - l1.y));

		if (r <= 0.0)
			return distancePointPoint(p, l1);
		if (r >= 1.0)
			return distancePointPoint(p, l2);

		/*(2)
		                (Ay-Cy)(Bx-Ax)-(Ax-Cx)(By-Ay)
		            s = -----------------------------
		         	                Curve^2

		            Then the distance from C to Point = |s|*Curve.
		*/

		double s = ((l1.y - p.y) * (l2.x - l1.x) - (l1.x - p.x) * (l2.y - l1.y))
		        /
		        ((l2.x - l1.x) * (l2.x - l1.x) + (l2.y - l1.y) * (l2.y - l1.y));

		return Math.abs(s) * Math.sqrt(((l2.x - l1.x) * (l2.x - l1.x) + (l2.y - l1.y) * (l2.y - l1.y)));
	}
}
