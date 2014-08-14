package org.testobject.kernel.classification.procedural;

import java.awt.geom.PathIterator;
import java.util.LinkedList;
import java.util.List;

import org.testobject.commons.math.algebra.MathUtils;
import org.testobject.commons.math.algebra.Point;

/**
 * 
 * @author enijkamp
 *
 */
public class ContourExtractor {

	private static interface Segment {
	}

	private static class Line implements Segment {
		public final Point.Double p1, p2;

		public Line(Point.Double p1, Point.Double p2) {
			this.p1 = p1;
			this.p2 = p2;
		}
	}

	private static class Bezier implements Segment {
		public final Point.Double p1, p2;
		public final Point.Double c1, c2;

		public Bezier(Point.Double p1, Point.Double p2, Point.Double c1, Point.Double c2) {
			this.p1 = p1;
			this.p2 = p2;
			this.c1 = c1;
			this.c2 = c2;
		}
	}

	private final PathIterator path;

	public ContourExtractor(PathIterator path) {
		this.path = path;
	}

	public List<Point.Double> extractContour() {
		List<Segment> segments = extractSegments();
		List<Point.Double> points = new LinkedList<Point.Double>();
		for (Segment segment : segments) {
			if (segment instanceof Line) {
				Line line = (Line) segment;
				toPoints(points, line);
			}

			if (segment instanceof Bezier) {
				Bezier bezier = (Bezier) segment;
				toPoints(points, bezier);
			}
		}
		return points;
	}

	private void toPoints(List<Point.Double> points, Line line) {
		points.add(line.p1);
		points.add(line.p2);
	}

	private void toPoints(List<Point.Double> points, Bezier bezier) {
		for (double t = 0d; t < 1d; t += 0.2d) {
			points.add(getPointOnBezier(bezier, t));
		}
	}

	private Point.Double getPointOnBezier(Bezier bezier, double t) {
		return bezier.p1.scale(MathUtils.cub(1 - t)).plus(
		        bezier.c1.scale(3 * MathUtils.sqr(1 - t) * t)).plus(
		        bezier.c2.scale(3 * (1 - t) * t * t)).plus(bezier.p2.scale(MathUtils.cub(t)));
	}

	private List<Segment> extractSegments() {
		List<Segment> segments = new LinkedList<Segment>();
		Point.Double current = new Point.Double(0, 0);
		while (path.isDone() == false) {
			double[] coords = new double[6];
			int seg = path.currentSegment(coords);
			switch (seg) {
			case PathIterator.SEG_MOVETO: {
				current.x = coords[0];
				current.y = coords[1];
				break;
			}
			case PathIterator.SEG_LINETO: {
				Point.Double to = new Point.Double(coords[0], coords[1]);
				segments.add(new Line(current, to));
				current = to;
				break;
			}
			case PathIterator.SEG_CUBICTO: {
				Point.Double to = new Point.Double(coords[4], coords[5]);
				segments.add(new Bezier(current, to, new Point.Double(coords[0], coords[1]), new Point.Double(coords[2], coords[3])));
				current = to;
				break;
			}
			case PathIterator.SEG_QUADTO:
				throw new IllegalStateException();
			case PathIterator.SEG_CLOSE:
				break;
			}
			path.next();
		}
		return segments;
	}
}
