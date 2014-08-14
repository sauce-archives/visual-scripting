package org.testobject.kernel.classification.contours;

import java.awt.Dimension;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.testobject.kernel.imgproc.blob.BooleanRaster;
import org.testobject.commons.math.algebra.Point;

/**
 * http://www.iis.sinica.edu.tw/~fchang/paper/component_labeling_cviu.pdf
 * 
 * http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.95.6330
 * 
 * http://blog.gregerstoltnilsen.net/tag/blob/
 * 
 * https://github.com/diwi/diewald_CV_kit
 * 
 * 
 * @author enijkamp
 *
 */
public class Contours {

	private final static int[] cd = { 1, 0, 1, 1, 0, 1, -1, 1, -1, 0, -1, -1, 0, -1, 1, -1 };

	public static List<Point.Double> contourTrace(BooleanRaster raster) {
		BooleanRaster largeRaster = growRaster(raster);
		for (int y = 0; y < largeRaster.getSize().height; y++) {
			for (int x = 0; x < largeRaster.getSize().width; x++) {
				if (largeRaster.get(x, y)) {
					return contourTrace(largeRaster, new Point.Double(x, y));
				}
			}
		}
		return Collections.emptyList();
	}

	private static BooleanRaster growRaster(final BooleanRaster raster) {
		final Dimension size = new Dimension(raster.getSize().width + 2, raster.getSize().height + 2);
		return new BooleanRaster() {
			public void set(int x, int y, boolean what) {
				throw new IllegalStateException();
			}

			public Dimension getSize() {
				return size;
			}

			public boolean get(int x, int y) {
				if (x == 0 || y == 0 || x == size.width - 1 || y == size.height - 1) {
					return false;
				} else {
					return raster.get(x - 1, y - 1);
				}
			}
		};
	}

	public static List<Point.Double> contourTrace(BooleanRaster raster, Point.Double start) {

		int[] tracking = new int[raster.getSize().height * raster.getSize().width];

		LinkedList<Point.Double> contour = new LinkedList<>();
		contour.add(start);

		int next = trace(raster, tracking, start, 7);

		if (next == 8)
			return contour;

		Point.Double point = new Point.Double(start.x + cd[next << 1], start.y + cd[(next << 1) + 1]);
		contour.add(point);

		next = trace(raster, tracking, point, (next + 6) % 8);

		if (next == 8)
			return contour;

		while (true) {
			double x = contour.getLast().x + cd[(next << 1) % 16];
			double y = contour.getLast().y + cd[((next << 1) + 1) % 16];
			next = trace(raster, tracking, new Point.Double(x, y), (next + 6) % 8);

			// check if we are back at start position
			if ((x == start.x) && (y == start.y) && (x + cd[next << 1] == point.x) && (y + cd[(next << 1) + 1] == point.y))
			{
				return contour;
			}
			else
			{
				contour.add(new Point.Double(x, y));
			}
		}
	}

	private static int trace(BooleanRaster raster, int[] tracking, Point.Double p, int n) {
		for (int i = 0; i < 8; i++) {

			double x = p.x + cd[((i + n) % 8) * 2];
			double y = p.y + cd[((i + n) % 8) * 2 + 1];

			if (raster.get((int) x, (int) y)) {
				tracking[toInt(y * raster.getSize().width + x)] = +1;
				return (i + n) % 8;
			} else {
				if (tracking[toInt(y * raster.getSize().width + x)] == 0)
					tracking[toInt(y * raster.getSize().width + x)] = -1;
			}

		}

		return 8; // should only happen for isolated pixels.
	}

	private static final int toInt(double value) {
		return (int) value;
	}

}
