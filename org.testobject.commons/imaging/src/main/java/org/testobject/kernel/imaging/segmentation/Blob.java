package org.testobject.kernel.imaging.segmentation;

import java.util.List;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.math.algebra.Size;

/**
 * Blob hierarchy element.
 *
 * Blob is a set of connected pixels with the same color.
 *
 * @author enijkamp
 */
public class Blob implements Mask {

	/**
	 * This blob unique id.
	 */
	public final int id;

	/**
	 * This blob bounding box. Do not mutate this, PLEASE!
	 */
	public final Rectangle.Int bbox;

	/**
	 * This blob area, in pixels.
	 */
	public final int area;

	/**
	 * Inner blobs (if any).
	 */
	public final List<Blob> children;

	/**
	 * Reference to an array that maps every pixel of the original image to the blob id.
	 * Bounding box (see {@link #bbox} helps to narrow the area where this blob can be found.
	 */
	public final int[][] ids;

	public Blob(int id, Rectangle.Int bbox, int area, List<Blob> children, int[][] ids) {
		this.id = id;
		this.bbox = bbox;
		this.area = area;
		this.children = children;
		this.ids = ids;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Blob(id=").append(this.id).append(", bbox=").append(toString(this.bbox)).append(", area=").append(this.area);
		if (this.children.size() > 0) {
			sb.append(", childs=").append(this.children.size());
		}
		sb.append(")");

		return sb.toString();
	}

	private final static String toString(Rectangle.Int rect) {
		return "[x=" + rect.x + ",y=" + rect.y + ",w=" + rect.w + ",h=" + rect.h + "]";
	}

	@Override
	public Size.Int getSize() {
		return new Size.Int(this.bbox.w, this.bbox.h);
	}

	@Override
	public boolean get(int x, int y) {
		if (x < 0 || x >= this.bbox.w || y < 0 || y >= this.bbox.h)	{
			throw new ArrayIndexOutOfBoundsException("x=" + x + ", y=" + y + ", size=" + getSize());
		}

		return this.id == this.ids[y + this.bbox.y][x + this.bbox.x];
	}

	@Override
	public void set(int x, int y, boolean what) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Rectangle.Int getBoundingBox() {
		return this.bbox;
	}
}