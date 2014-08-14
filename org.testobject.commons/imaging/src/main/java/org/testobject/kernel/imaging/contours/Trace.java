package org.testobject.kernel.imaging.contours;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.testobject.commons.math.algebra.Size;
import org.testobject.commons.util.collections.Lists;
import org.testobject.kernel.imaging.segmentation.BooleanRaster;
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
public class Trace {

	private final static int[] cd = { 1, 0, 1, 1, 0, 1, -1, 1, -1, 0, -1, -1, 0, -1, 1, -1 };

	public static List<Point.Double> contourTrace(BooleanRaster raster) {
		final int translateX = 1;
		final int translateY = 1;
		BooleanRaster largeRaster = growRaster(raster, translateX, translateY);
		for (int y = 0; y < largeRaster.getSize().h; y++) {
			for (int x = 0; x < largeRaster.getSize().w; x++) {
				if (largeRaster.get(x, y)) {
					return translate(contourTrace(largeRaster, new Point.Double(x, y)), -translateX, -translateY);
				}
			}
		}
		return Collections.emptyList();
	}

	private static List<Point.Double> translate(List<Point.Double> source, int translateX, int translateY) {
		List<Point.Double> target = Lists.newArrayList(source.size());
		for(Point.Double point : source) {
			target.add(point.plus(translateX, translateY));
		}
		return target;
	}

	private static BooleanRaster growRaster(final BooleanRaster raster, final int translateX, final int translateY) {
		final Size.Int size = new Size.Int(raster.getSize().w + translateX*2, raster.getSize().h + translateY*2);
		return new BooleanRaster() {
			public void set(int x, int y, boolean what) {
				throw new IllegalStateException();
			}

			public Size.Int getSize() {
				return size;
			}

			public boolean get(int x, int y) {
				if (x == 0 || y == 0 || x == size.w - translateX || y == size.h - translateY) {
					return false;
				} else {
					return raster.get(x - translateX, y - translateY);
				}
			}
		};
	}

	public static List<Point.Double> contourTrace(BooleanRaster raster, Point.Double start) {

		int[] tracking = new int[raster.getSize().h * raster.getSize().w];

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
				tracking[toInt(y * raster.getSize().w + x)] = +1;
				return (i + n) % 8;
			} else {
				if (tracking[toInt(y * raster.getSize().w + x)] == 0)
					tracking[toInt(y * raster.getSize().w + x)] = -1;
			}

		}

		return 8; // should only happen for isolated pixels.
	}

	private static final int toInt(double value) {
		return (int) value;
	}

}
