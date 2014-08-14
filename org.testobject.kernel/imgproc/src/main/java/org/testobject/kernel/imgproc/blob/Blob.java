package org.testobject.kernel.imgproc.blob;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;

/**
 * Blob hierarchy element.
 *
 * Blob is a set of connected pixels with the same color.
 *
 * @author enijkamp
 */
public class Blob implements BooleanRaster, BoundingBox
{

	/**
	 * This blob unique id.
	 */
	public final int id;

	/**
	 * This blob bounding box. Do not mutate this, PLEASE!
	 */
	public final Rectangle bbox;

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

	// FIXME factor out to second hierarchy (blob->classes->locators) to enable proposals and clean separation of mutability (en)
	/**
	 * Type of blob.
	 */
	public Meta meta = Meta.blob;

	/**
	 * If blob is part of a complex composition, then back pointers point to parent of this composition.
	 */
	public final List<Blob> backpointers = new LinkedList<Blob>();

	public Blob(int id, Rectangle bbox, int area, List<Blob> children, int[][] ids)
	{
		this.id = id;
		this.bbox = bbox;
		this.area = area;
		this.children = children;
		this.ids = ids;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("Blob(id=").append(this.id).append(", bbox=").append(toString(this.bbox)).append(", area=").append(this.area);
		if (this.children.size() > 0)
		{
			sb.append(", childs=").append(this.children.size());
		}
		sb.append(")");

		return sb.toString();
	}

	private final static String toString(Rectangle rect) {
		return "[x=" + rect.x + ",y=" + rect.y + ",w=" + rect.width + ",h=" + rect.height + "]";
	}

	@Override
	public Dimension getSize()
	{
		return new Dimension(this.bbox.width, this.bbox.height);
	}

	@Override
	public boolean get(int x, int y)
	{
		if (x < 0 || x >= this.bbox.width || y < 0 || y >= this.bbox.height)
		{
			throw new ArrayIndexOutOfBoundsException("x=" + x + ", y=" + y + ", size=" + getSize());
		}

		return this.id == this.ids[y + this.bbox.y][x + this.bbox.x];
	}

	@Override
	public void set(int x, int y, boolean what)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Rectangle getBoundingBox()
	{
		return this.bbox;
	}
}
