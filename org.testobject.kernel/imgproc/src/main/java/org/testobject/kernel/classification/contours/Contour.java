package org.testobject.kernel.classification.contours;

import java.util.List;

import org.testobject.commons.math.algebra.Point;

/**
 * 
 * @author enijkamp
 *
 */
public class Contour {
	
	public final double[] xpoints, ypoints;
	public final int npoints;

	public Contour(List<Point.Double> points) {
		this.npoints = points.size();
		this.xpoints = new double[npoints];
		this.ypoints = new double[npoints];
		int i = 0;
		for (Point.Double point : points) {
			xpoints[i] = point.x;
			ypoints[i] = point.y;
			i++;
		}
	}

	public boolean isEmpty() {
		return npoints == 0;
	}

	public int size() {
		return npoints;
	}

	public boolean contains(double x, double y) {
		int hits = 0;

		double lastx = xpoints[npoints - 1];
		double lasty = ypoints[npoints - 1];
		double curx, cury;

		// walk the edges of the polygon
		for (int i = 0; i < npoints; lastx = curx, lasty = cury, i++) {
			curx = xpoints[i];
			cury = ypoints[i];

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