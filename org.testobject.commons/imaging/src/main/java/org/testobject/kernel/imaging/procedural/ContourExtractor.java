package org.testobject.kernel.imaging.procedural;

import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.geom.RoundRectangle2D;
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
	
	public static List<Point.Double> getContour(Element element) {
		
		if (element instanceof Element.Rect) {
			Element.Rect rect = (Element.Rect) element;
			PathIterator path = new RoundRectangle2D.Double(0, 0, rect.getSize().w, rect.getSize().h, rect.getRoundX(), rect.getRoundY()).getPathIterator(null);
			return Path.extractContour(path);
		} 
		
		if (element instanceof Element.Polyline) {
			Element.Polyline polyline = (Element.Polyline) element;
			return polyline.getPath();
		}
		
		if(element instanceof Element.Circle) {
			Element.Circle circle = (Element.Circle) element;
			PathIterator path = new Ellipse2D.Double(0, 0, circle.getSize().w, circle.getSize().h).getPathIterator(null);
			return Path.extractContour(path);
		}
		
		throw new IllegalArgumentException(element.getClass().getSimpleName());
	}
	
	public static class Path {

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
	
		public static List<Point.Double> extractContour(PathIterator path) {
			List<Segment> segments = extractSegments(path);
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
	
		private static void toPoints(List<Point.Double> points, Line line) {
			points.add(line.p1);
			points.add(line.p2);
		}
	
		private static void toPoints(List<Point.Double> points, Bezier bezier) {
			for (double t = 0d; t < 1d; t += 0.2d) {
				points.add(getPointOnBezier(bezier, t));
			}
		}
	
		private static Point.Double getPointOnBezier(Bezier bezier, double t) {
			return bezier.p1.scale(MathUtils.cub(1 - t)).plus(
			        bezier.c1.scale(3 * MathUtils.sqr(1 - t) * t)).plus(
			        bezier.c2.scale(3 * (1 - t) * t * t)).plus(bezier.p2.scale(MathUtils.cub(t)));
		}
	
		private static List<Segment> extractSegments(PathIterator path) {
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
}
