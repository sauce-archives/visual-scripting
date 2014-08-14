package org.testobject.commons.math.algebra;

import java.util.List;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * 
 * @author enijkamp
 * 
 */
// TODO align Int, Double class methods (en)
public interface Rectangle {

	class Int {

		public int x;
		public int y;
		public int w;
		public int h;

		public static final Rectangle.Int ZERO = new Rectangle.Int(0, 0, 0, 0);

		public Int() {
			this.x = 0;
			this.y = 0;
			this.w = 0;
			this.h = 0;
		}

		public Int(int w, int h) {
			this.x = 0;
			this.y = 0;
			this.w = w;
			this.h = h;
		}

		@JsonCreator
		public Int(@JsonProperty("x") int x, @JsonProperty("y") int y, @JsonProperty("w") int w, @JsonProperty("h") int h) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}

		public Int(Point.Int position, Size.Int size) {
			this.x = position.x;
			this.y = position.y;
			this.w = size.w;
			this.h = size.h;
		}

		public Int(Rectangle.Int other) {
			this.x = other.x;
			this.y = other.y;
			this.w = other.w;
			this.h = other.h;
		}

		public Point.Int getLocation() {
			return new Point.Int(x, y);
		}

		@JsonProperty("x")
		public int getX() {
			return x;
		}

		@JsonProperty("y")
		public int getY() {
			return y;
		}

		@JsonProperty("w")
		public int getWidth() {
			return w;
		}

		@JsonProperty("h")
		public int getHeight() {
			return h;
		}

		public int getMinX() {
			return getX();
		}

		public int getMinY() {
			return getY();
		}

		public int getMaxX() {
			return getX() + getWidth();
		}

		public int getMaxY() {
			return getY() + getHeight();
		}

		public int getCenterX() {
			return getX() + getWidth() / 2;
		}

		public int getCenterY() {
			return getY() + getHeight() / 2;
		}

		public Size.Int getSize() {
			return new Size.Int(w, h);
		}

		public boolean isEmpty() {
			return (w <= 0) || (h <= 0);
		}

		public String toString() {
			return "[" + x + "," + y + "," + w + "," + h + "]";
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Rectangle.Int))
				return false;
			Rectangle.Int p = (Rectangle.Int) obj;
			return x == p.x && y == p.y && w == p.w && h == p.h;
		}

		@Override
		public int hashCode() {
			long bits = java.lang.Double.doubleToLongBits(getX());
			bits += java.lang.Double.doubleToLongBits(getY()) * 37;
			bits += java.lang.Double.doubleToLongBits(getWidth()) * 43;
			bits += java.lang.Double.doubleToLongBits(getHeight()) * 47;
			return (((int) bits) ^ ((int) (bits >> 32)));
		}

		public void setRect(Rectangle.Int rect) {
			reshape(rect.x, rect.y, rect.w, rect.h);
		}

		public void add(Rectangle.Int r) {
			int tx2 = this.w;
			int ty2 = this.h;
			if ((tx2 | ty2) < 0) {
				reshape(r.x, r.y, r.w, r.h);
			}
			int rx2 = r.w;
			int ry2 = r.h;
			if ((rx2 | ry2) < 0) {
				return;
			}
			int tx1 = this.x;
			int ty1 = this.y;
			tx2 += tx1;
			ty2 += ty1;
			int rx1 = r.x;
			int ry1 = r.y;
			rx2 += rx1;
			ry2 += ry1;
			if (tx1 > rx1)
				tx1 = rx1;
			if (ty1 > ry1)
				ty1 = ry1;
			if (tx2 < rx2)
				tx2 = rx2;
			if (ty2 < ry2)
				ty2 = ry2;
			tx2 -= tx1;
			ty2 -= ty1;
			// tx2,ty2 will never underflow since both original
			// rectangles were non-empty
			// they might overflow, though...
			if (tx2 > java.lang.Integer.MAX_VALUE)
				tx2 = java.lang.Integer.MAX_VALUE;
			if (ty2 > java.lang.Integer.MAX_VALUE)
				ty2 = java.lang.Integer.MAX_VALUE;
			reshape(tx1, ty1, tx2, ty2);
		}

		private void reshape(int x, int y, int w, int h) {
			this.x = x;
			this.y = y;
			this.h = h;
			this.w = w;
		}

		public boolean contains(int X, int Y) {
			int w = this.w;
			int h = this.h;
			if ((w | h) < 0) {
				// At least one of the dimensions is negative...
				return false;
			}
			// Note: if either dimension is zero, tests below must return false...
			int x = this.x;
			int y = this.y;
			if (X < x || Y < y) {
				return false;
			}
			w += x;
			h += y;
			// overflow || intersect
			return ((w < x || w > X) && (h < y || h > Y));
		}

		public boolean contains(Rectangle.Int r) {
			return contains(r.x, r.y, r.w, r.h);
		}

		public boolean contains(int X, int Y, int W, int H) {
			int w = this.w;
			int h = this.h;
			if ((w | h | W | H) < 0) {
				// At least one of the dimensions is negative...
				return false;
			}
			// Note: if any dimension is zero, tests below must return false...
			int x = this.x;
			int y = this.y;
			if (X < x || Y < y) {
				return false;
			}
			w += x;
			W += X;
			if (W <= X) {
				// X+W overflowed or W was zero, return false if...
				// either original w or W was zero or
				// x+w did not overflow or
				// the overflowed x+w is smaller than the overflowed X+W
				if (w >= x || W > w)
					return false;
			} else {
				// X+W did not overflow and W was not zero, return false if...
				// original w was zero or
				// x+w did not overflow and x+w is smaller than X+W
				if (w >= x && W > w)
					return false;
			}
			h += y;
			H += Y;
			if (H <= Y) {
				if (h >= y || H > h)
					return false;
			} else {
				if (h >= y && H > h)
					return false;
			}
			return true;
		}

		public static void union(Rectangle.Int src1, Rectangle.Int src2, Rectangle.Int dest) {
			int x1 = Math.min(src1.getMinX(), src2.getMinX());
			int y1 = Math.min(src1.getMinY(), src2.getMinY());
			int x2 = Math.max(src1.getMaxX(), src2.getMaxX());
			int y2 = Math.max(src1.getMaxY(), src2.getMaxY());
			dest.setFrameFromDiagonal(x1, y1, x2, y2);
		}
		
		public static Rectangle.Int union(List<Rectangle.Int> rectangles) {
			if(rectangles.isEmpty()) {
				throw new IllegalArgumentException("empty set of rectangles");
			}
			
			Rectangle.Int union = new Rectangle.Int(rectangles.get(0));
			for(int i = 1; i < rectangles.size(); i++) {
				union = union.union(rectangles.get(i));
			}
			return union;
		}

		public void setFrameFromDiagonal(int x1, int y1, int x2, int y2) {
			if (x2 < x1) {
				int t = x1;
				x1 = x2;
				x2 = t;
			}
			if (y2 < y1) {
				int t = y1;
				y1 = y2;
				y2 = t;
			}
			reshape(x1, y1, x2 - x1, y2 - y1);
		}

		public Rectangle.Int union(Rectangle.Int r) {
			long tx2 = this.w;
			long ty2 = this.h;
			if ((tx2 | ty2) < 0) {
				// This rectangle has negative dimensions...
				// If r has non-negative dimensions then it is the answer.
				// If r is non-existant (has a negative dimension), then both
				// are non-existant and we can return any non-existant rectangle
				// as an answer. Thus, returning r meets that criterion.
				// Either way, r is our answer.
				return new Rectangle.Int(r);
			}
			long rx2 = r.w;
			long ry2 = r.h;
			if ((rx2 | ry2) < 0) {
				return new Rectangle.Int(this);
			}
			int tx1 = this.x;
			int ty1 = this.y;
			tx2 += tx1;
			ty2 += ty1;
			int rx1 = r.x;
			int ry1 = r.y;
			rx2 += rx1;
			ry2 += ry1;
			if (tx1 > rx1)
				tx1 = rx1;
			if (ty1 > ry1)
				ty1 = ry1;
			if (tx2 < rx2)
				tx2 = rx2;
			if (ty2 < ry2)
				ty2 = ry2;
			tx2 -= tx1;
			ty2 -= ty1;
			// tx2,ty2 will never underflow since both original rectangles
			// were already proven to be non-empty
			// they might overflow, though...
			if (tx2 > Integer.MAX_VALUE)
				tx2 = Integer.MAX_VALUE;
			if (ty2 > Integer.MAX_VALUE)
				ty2 = Integer.MAX_VALUE;
			return new Rectangle.Int(tx1, ty1, (int) tx2, (int) ty2);
		}

		public Rectangle.Int intersection(Rectangle.Int r) {
			int tx1 = this.x;
			int ty1 = this.y;
			int rx1 = r.x;
			int ry1 = r.y;
			long tx2 = tx1;
			tx2 += this.w;
			long ty2 = ty1;
			ty2 += this.h;
			long rx2 = rx1;
			rx2 += r.w;
			long ry2 = ry1;
			ry2 += r.h;
			if (tx1 < rx1)
				tx1 = rx1;
			if (ty1 < ry1)
				ty1 = ry1;
			if (tx2 > rx2)
				tx2 = rx2;
			if (ty2 > ry2)
				ty2 = ry2;
			tx2 -= tx1;
			ty2 -= ty1;
			// tx2,ty2 will never overflow (they will never be
			// larger than the smallest of the two source w,h)
			// they might underflow, though...
			if (tx2 < Integer.MIN_VALUE)
				tx2 = Integer.MIN_VALUE;
			if (ty2 < Integer.MIN_VALUE)
				ty2 = Integer.MIN_VALUE;
			return new Rectangle.Int(tx1, ty1, (int) tx2, (int) ty2);
		}
		
		public boolean bordering(Rectangle.Int r) {
			int tw = this.w;
			int th = this.h;
			int rw = r.w;
			int rh = r.h;
			if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
				return false;
			}
			int tx = this.x;
			int ty = this.y;
			int rx = r.x;
			int ry = r.y;
			rw += rx;
			rh += ry;
			tw += tx;
			th += ty;
			// overflow || intersect
			return ((rw <= rx || rw >= tx) &&
					(rh <= ry || rh >= ty) &&
					(tw <= tx || tw >= rx) && (th <= ty || th >= ry));
		}

		public boolean intersects(Rectangle.Int r) {
			int tw = this.w;
			int th = this.h;
			int rw = r.w;
			int rh = r.h;
			if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
				return false;
			}
			int tx = this.x;
			int ty = this.y;
			int rx = r.x;
			int ry = r.y;
			rw += rx;
			rh += ry;
			tw += tx;
			th += ty;
			// overflow || intersect
			return ((rw < rx || rw > tx) &&
					(rh < ry || rh > ty) &&
					(tw < tx || tw > rx) && (th < ty || th > ry));
		}
	}

	class Double {

		public double x;
		public double y;
		public double w;
		public double h;

		public static final Rectangle.Double ZERO = new Rectangle.Double(0, 0, 0, 0);

		public Double() {
			this.x = 0;
			this.y = 0;
			this.w = 0;
			this.h = 0;
		}

		public Double(double x, double y, double w, double h) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}

		public double getWidth() {
			return w;
		}

		public double getHeight() {
			return h;
		}

		public double getMinX() {
			return getX();
		}

		public double getMinY() {
			return getY();
		}

		public double getMaxX() {
			return getX() + getWidth();
		}

		public double getMaxY() {
			return getY() + getHeight();
		}

		public double getCenterX() {
			return getX() + getWidth() / 2.0;
		}

		public double getCenterY() {
			return getY() + getHeight() / 2.0;
		}

		public String toString() {
			return "[" + x + "," + y + "," + w + "," + h + "]";
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Rectangle.Double))
				return false;
			Rectangle.Double p = (Rectangle.Double) obj;
			return x == p.x && y == p.y && w == p.w && h == p.h;
		}

		@Override
		public int hashCode() {
			long bits = java.lang.Double.doubleToLongBits(getX());
			bits += java.lang.Double.doubleToLongBits(getY()) * 37;
			bits += java.lang.Double.doubleToLongBits(getWidth()) * 43;
			bits += java.lang.Double.doubleToLongBits(getHeight()) * 47;
			return (((int) bits) ^ ((int) (bits >> 32)));
		}

		public double area() {
			return w * h;
		}

		public boolean contains(Rectangle.Double r) {
			return contains(r.x, r.y, r.w, r.h);
		}

		public boolean contains(double x, double y, double w, double h) {
			if (w <= 0 || h <= 0) {
				return false;
			}
			double x0 = getX();
			double y0 = getY();
			return (x >= x0 &&
					y >= y0 &&
					(x + w) <= x0 + getWidth() && (y + h) <= y0 + getHeight());
		}

		public Rectangle.Double union(Rectangle.Double r) {
			Rectangle.Double dest = new Rectangle.Double();
			Rectangle.Double.union(this, r, dest);
			return dest;
		}

		public static void union(Rectangle.Double src1,
				Rectangle.Double src2,
				Rectangle.Double dest) {
			double x1 = Math.min(src1.getMinX(), src2.getMinX());
			double y1 = Math.min(src1.getMinY(), src2.getMinY());
			double x2 = Math.max(src1.getMaxX(), src2.getMaxX());
			double y2 = Math.max(src1.getMaxY(), src2.getMaxY());
			dest.setFrameFromDiagonal(x1, y1, x2, y2);
		}

		public void setFrameFromDiagonal(double x1, double y1,
				double x2, double y2) {
			if (x2 < x1) {
				double t = x1;
				x1 = x2;
				x2 = t;
			}
			if (y2 < y1) {
				double t = y1;
				y1 = y2;
				y2 = t;
			}
			setFrame(x1, y1, x2 - x1, y2 - y1);
		}

		public Rectangle.Double intersect(Rectangle.Double r) {
			Rectangle.Double dest = new Rectangle.Double();
			Rectangle.Double.intersect(this, r, dest);
			return dest;
		}

		public static void intersect(Rectangle.Double src1,
				Rectangle.Double src2,
				Rectangle.Double dest) {
			double x1 = Math.max(src1.getMinX(), src2.getMinX());
			double y1 = Math.max(src1.getMinY(), src2.getMinY());
			double x2 = Math.min(src1.getMaxX(), src2.getMaxX());
			double y2 = Math.min(src1.getMaxY(), src2.getMaxY());

			dest.setFrame(x1, y1, x2 - x1, y2 - y1);
		}

		public boolean intersects(Rectangle.Double rect) {
			if (rect.w <= 0 || rect.h <= 0) {
				return false;
			}
			double x0 = x;
			double y0 = y;
			return (rect.x + rect.w > x0 &&
					rect.y + rect.h > y0 &&
					rect.x < x0 + w && rect.y < y0 + h);
		}

		public void add(Rectangle.Double r) {
			double tx2 = this.w;
			double ty2 = this.h;
			if ((tx2) < 0 || (ty2) < 0) {
				setFrame(r.x, r.y, r.w, r.h);
			}
			double rx2 = r.w;
			double ry2 = r.h;
			if ((rx2) < 0 || (ry2) < 0) {
				return;
			}
			double tx1 = this.x;
			double ty1 = this.y;
			tx2 += tx1;
			ty2 += ty1;
			double rx1 = r.x;
			double ry1 = r.y;
			rx2 += rx1;
			ry2 += ry1;
			if (tx1 > rx1)
				tx1 = rx1;
			if (ty1 > ry1)
				ty1 = ry1;
			if (tx2 < rx2)
				tx2 = rx2;
			if (ty2 < ry2)
				ty2 = ry2;
			tx2 -= tx1;
			ty2 -= ty1;
			// tx2,ty2 will never underflow since both original
			// rectangles were non-empty
			// they might overflow, though...
			if (tx2 > java.lang.Double.MAX_VALUE)
				tx2 = java.lang.Double.MAX_VALUE;
			if (ty2 > java.lang.Double.MAX_VALUE)
				ty2 = java.lang.Double.MAX_VALUE;
			setFrame(tx1, ty1, tx2, ty2);
		}

		private void setFrame(double x, double y, double w, double h) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}

		// FIXME use (x,y,w,h) notation, currently used by r-tree (en)
		public static boolean contains(double r1MinX, double r1MinY, double r1MaxX, double r1MaxY,
				double r2MinX, double r2MinY, double r2MaxX, double r2MaxY) {
			return r1MaxX >= r2MaxX && r1MinX <= r2MinX && r1MaxY >= r2MaxY && r1MinY <= r2MinY;
		}

		public static double distanceSq(double minX, double minY, double maxX, double maxY, double pX, double pY) {
			double distanceSqX = 0;
			double distanceSqY = 0;

			if (minX > pX) {
				distanceSqX = minX - pX;
				distanceSqX *= distanceSqX;
			} else if (pX > maxX) {
				distanceSqX = pX - maxX;
				distanceSqX *= distanceSqX;
			}

			if (minY > pY) {
				distanceSqY = minY - pY;
				distanceSqY *= distanceSqY;
			} else if (pY > maxY) {
				distanceSqY = pY - maxY;
				distanceSqY *= distanceSqY;
			}

			return distanceSqX + distanceSqY;
		}

		public static boolean intersects(double r1MinX, double r1MinY, double r1MaxX, double r1MaxY,
				double r2MinX, double r2MinY, double r2MaxX, double r2MaxY) {
			return r1MaxX >= r2MinX && r1MinX <= r2MaxX && r1MaxY >= r2MinY && r1MinY <= r2MaxY;
		}

		public static double area(double minX, double minY, double maxX, double maxY) {
			return (maxX - minX) * (maxY - minY);
		}

		public static double enlargement(double r1MinX, double r1MinY, double r1MaxX, double r1MaxY,
				double r2MinX, double r2MinY, double r2MaxX, double r2MaxY) {
			double r1Area = (r1MaxX - r1MinX) * (r1MaxY - r1MinY);

			if (r1Area == java.lang.Double.POSITIVE_INFINITY) {
				return 0; // cannot enlarge an infinite rectangle...
			}

			if (r2MinX < r1MinX)
				r1MinX = r2MinX;
			if (r2MinY < r1MinY)
				r1MinY = r2MinY;
			if (r2MaxX > r1MaxX)
				r1MaxX = r2MaxX;
			if (r2MaxY > r1MaxY)
				r1MaxY = r2MaxY;

			double r1r2UnionArea = (r1MaxX - r1MinX) * (r1MaxY - r1MinY);

			if (r1r2UnionArea == Float.POSITIVE_INFINITY) {
				// if a finite rectangle is enlarged and becomes infinite,
				// then the enlargement must be infinite.
				return Float.POSITIVE_INFINITY;
			}
			return r1r2UnionArea - r1Area;
		}
	}
}