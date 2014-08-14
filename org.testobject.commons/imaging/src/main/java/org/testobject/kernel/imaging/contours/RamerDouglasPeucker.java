package org.testobject.kernel.imaging.contours;

import java.util.Arrays;
import java.util.List;

import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.math.algebra.Point;

/**
 * 
 * @author enijkamp
 *
 */
public class RamerDouglasPeucker {

	public static List<Point.Double> simplifyPolyline(List<Point.Double> points, double epsilon) {
		return simplifyPolyline(points, 0, points.size() - 1, epsilon);
	}

	private static List<Point.Double> simplifyPolyline(List<Point.Double> points, int start, int end, double epsilon) {
		double maxDistance = 0;
		int maxIndex = 0;
		for (int i = start + 1; i < end; i++) {
			double distance = distancePointLine(points.get(i), points.get(start), points.get(end));
			if (distance > maxDistance) {
				maxDistance = distance;
				maxIndex = i;
			}
		}

		if (maxDistance >= epsilon) {
			List<Point.Double> result1 = simplifyPolyline(points, start, maxIndex, epsilon);
			List<Point.Double> result2 = simplifyPolyline(points, maxIndex, end, epsilon);
			return Lists.concat(sublist(result1, 0, result1.size() - 1), result2);
		} else {
			return Arrays.asList(new Point.Double[] { points.get(start), points.get(end) });
		}
	}

	private static List<Point.Double> sublist(List<Point.Double> points, int from, int to) {
		return points.subList(from, to);
	}

	private static double distancePointPoint(Point.Double p1, Point.Double p2)
	{
		double dx = p1.x - p2.x;
		double dy = p1.y - p2.x;
		return Math.sqrt(dx * dx + dy * dy);
	}

	private static double distancePointLine(Point.Double p, Point.Double l1, Point.Double l2)
	{
		if (l1.x == l2.x && l1.y == l2.y)
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

		if (((l2.x - l1.y) * (l2.x - l1.x) + (l2.y - l1.y) * (l2.y - l1.y)) == 0) {
			return 0;
		}

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
