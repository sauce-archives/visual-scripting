package org.testobject.commons.math.algebra;

import java.util.Comparator;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * 
 * @author enijkamp
 *
 */
public interface Point {

	class Int {

		public int x, y;

		public static final Point.Int ZERO = new Point.Int(0, 0);
		
		public static Int zero() {
			return ZERO;
		}
		
		public static Int from(int x, int y) {
			return new Int(x, y);
		}

        @JsonCreator
		public Int(@JsonProperty("x") int x, @JsonProperty("y") int y) {
			this.x = x;
			this.y = y;
		}

        @JsonProperty("x")
		public int getX() {
			return x;
		}

        @JsonProperty("y")
		public int getY() {
			return y;
		}

		public String toString() {
			return "(" + x + ", " + y + ")";
		}

		public Point.Int plus(Point.Int point) {
			return new Point.Int(x + point.x, y + point.y);
		}

		public Point.Int minus(int x, int y) {
			return new Point.Int(this.x - x, this.y - y);
		}

		public Point.Int minus(Point.Int point) {
			return new Point.Int(x - point.x, y - point.y);
		}

		public Point.Int neg() {
			return new Point.Int(-x, -y);
		}

		public Point.Int plus(int x, int y) {
			return new Point.Int(this.x + x, this.y + y);
		}

		public Point.Int scale(double scale) {
			return new Point.Int((int)(x * scale), (int)(y * scale));
		}

		public Point.Int scale(double scaleX, double scaleY) {
			return new Point.Int((int)(x * scaleX), (int)(y * scaleY));
		}

		public Point.Int mirrorY() {
			return new Point.Int(x, -y);
		}

		public Point.Int mirrorX() {
			return new Point.Int(-x, y);
		}

		public boolean equals(Object obj) {
			if (!(obj instanceof Point.Int))
				return false;
			Point.Int p = (Point.Int) obj;
			return x == p.x && y == p.y;
		}

		public int hashCode() {
			return hashCode(x) + 31 * hashCode(y);
		}

		private int hashCode(double value) {
			long bits = java.lang.Double.doubleToLongBits(value);
			return (int) (bits ^ (bits >>> 32));
		}

		public static class CartesianComparator implements Comparator<Point.Int> {

			public int compare(Point.Int p1, Point.Int p2) {
				if (p1.y < p2.y)
					return -1;
				if (p1.y > p2.y)
					return 1;
				if (p1.x == p2.x)
					return 0;
				return p1.x < p2.x ? -1 : 1;
			}
		}

		public static double distance(Point.Int a, Point.Int b) {
	        double px = a.x - b.x;
	        double py = a.y - b.y;

	        return Math.sqrt(px * px + py * py);
		}
	}

	class Double {

		public double x, y;

		public static final Point.Double ZERO = new Point.Double(0, 0);
		
		public static Double zero() {
			return ZERO;
		}
		
		public static Double from(int x, int y) {
			return new Double(x, y);
		}

		public Double(double x, double y) {
			this.x = x;
			this.y = y;
		}

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}

		public String toString() {
			return "(" + x + ", " + y + ")";
		}

		public Point.Double plus(Point.Double point) {
			return new Point.Double(x + point.x, y + point.y);
		}

		public Point.Double minus(double x, double y) {
			return new Point.Double(this.x - x, this.y - y);
		}

		public Point.Double minus(Point.Double point) {
			return new Point.Double(x - point.x, y - point.y);
		}

		public Point.Double neg() {
			return new Point.Double(-x, -y);
		}

		public Point.Double plus(double x, double y) {
			return new Point.Double(this.x + x, this.y + y);
		}

		public Point.Double scale(double scale) {
			return new Point.Double(x * scale, y * scale);
		}

		public Point.Double scale(double scaleX, double scaleY) {
			return new Point.Double(x * scaleX, y * scaleY);
		}

		public Point.Double mirrorY() {
			return new Point.Double(x, -y);
		}

		public Point.Double mirrorX() {
			return new Point.Double(-x, y);
		}

		public boolean equals(Object obj) {
			if (!(obj instanceof Point.Double))
				return false;
			Point.Double p = (Point.Double) obj;
			return x == p.x && y == p.y;
		}

		public int hashCode() {
			return hashCode(x) + 31 * hashCode(y);
		}

		private int hashCode(double value) {
			long bits = java.lang.Double.doubleToLongBits(value);
			return (int) (bits ^ (bits >>> 32));
		}

		public static class CartesianComparator implements Comparator<Point.Double> {

			public int compare(Point.Double p1, Point.Double p2) {
				if (p1.y < p2.y)
					return -1;
				if (p1.y > p2.y)
					return 1;
				if (p1.x == p2.x)
					return 0;
				return p1.x < p2.x ? -1 : 1;
			}

		}
	}
}