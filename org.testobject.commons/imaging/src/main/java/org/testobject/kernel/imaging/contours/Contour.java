package org.testobject.kernel.imaging.contours;

import java.util.List;

import org.testobject.commons.math.algebra.Point;

/**
 * 
 * @author enijkamp
 *
 */
public class Contour {
	
	public static boolean contains(List<Point.Double> points, double x, double y) {
		int hits = 0;

		double lastx = points.get(points.size() - 1).x;
		double lasty = points.get(points.size() - 1).y;
		double curx, cury;

		// walk the edges of the polygon
		for (int i = 0; i < points.size(); lastx = curx, lasty = cury, i++) {
			curx = points.get(i).x;
			cury = points.get(i).y;

			if (cury == lasty) {
				continue;
			}

			double leftx;
			if (curx < lastx) {
				if (x >= lastx) {
					continue;
				}
				leftx = curx;
			} else {
				if (x >= curx) {
					continue;
				}
				leftx = lastx;
			}

			double test1, test2;
			if (cury < lasty) {
				if (y < cury || y >= lasty) {
					continue;
				}
				if (x < leftx) {
					hits++;
					continue;
				}
				test1 = x - curx;
				test2 = y - cury;
			} else {
				if (y < lasty || y >= cury) {
					continue;
				}
				if (x < leftx) {
					hits++;
					continue;
				}
				test1 = x - lastx;
				test2 = y - lasty;
			}

			if (test1 < (test2 / (lasty - cury) * (lastx - curx))) {
				hits++;
			}
		}

		return ((hits & 1) != 0);
	}
}