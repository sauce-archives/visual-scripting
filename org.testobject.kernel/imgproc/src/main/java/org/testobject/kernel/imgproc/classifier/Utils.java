package org.testobject.kernel.imgproc.classifier;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testobject.kernel.imgproc.blob.BlobUtils;
import org.testobject.kernel.imgproc.blob.BoundingBox;
import org.testobject.kernel.imgproc.blob.Blob;

/**
 * 
 * @author nijkamp
 * 
 */
public final class Utils
{
	public static interface UnaryFilter<T>
	{
		boolean pass(T t);
	}

	public static interface BinaryFilter<T>
	{
		boolean pass(T t1, T t2);
	}

	public static final List<Blob> findRecursive(Blob blob, UnaryFilter<Blob> filter)
	{
		List<Blob> passed = new ArrayList<Blob>();
		findRecursive(blob, passed, filter);
		return passed;
	}

	public static final void findRecursive(final Blob blob, final List<Blob> passed, UnaryFilter<Blob> filter)
	{
		for (Blob child : blob.children)
		{
			findRecursive(child, passed, filter);
			if (!filter.pass(child))
			{
				continue;
			}
			else
			{
				passed.add(child);
			}
		}
	}

	public static final <T> List<List<T>> findPairwise(final List<T> objects, List<BinaryFilter<T>> filters)
	{
		List<List<T>> passed = new ArrayList<List<T>>();
		// cartesian product
		for (int i = 0; i < objects.size(); i++)
		{
			final T o1 = objects.get(i);
			List<T> matches = new ArrayList<T>();
			candidate: for (int j = i; j < objects.size(); j++)
			{
				final T o2 = objects.get(j);
				for (BinaryFilter<T> filter : filters)
				{
					if (!filter.pass(o1, o2))
					{
						continue candidate;
					}
				}
				matches.add(o2);
			}
			if (!matches.isEmpty())
			{
				matches.add(o1);
				passed.add(matches);
			}
		}
		return passed;
	}

	public static final <T> List<List<T>> findPairwise(final List<T> objects, BinaryFilter<T> filter1)
	{
		List<BinaryFilter<T>> filters = new ArrayList<BinaryFilter<T>>();
		filters.add(filter1);
		return findPairwise(objects, filters);
	}

	public static final <T> List<List<T>> findPairwise(final List<T> objects, BinaryFilter<T> filter1, BinaryFilter<T> filter2)
	{
		List<BinaryFilter<T>> filters = new ArrayList<BinaryFilter<T>>();
		filters.add(filter1);
		filters.add(filter2);
		return findPairwise(objects, filters);
	}

	public static final <T> List<T> find(final List<T> objects, UnaryFilter<T>[] filters)
	{
		List<T> passed = new ArrayList<T>();
		candidate: for (T t : objects)
		{
			for (UnaryFilter<T> filter : filters)
			{
				if (!filter.pass(t))
				{
					continue candidate;
				}
			}
			passed.add(t);
		}
		return passed;
	}

	public static final <T> List<T> find(final List<T> objects, UnaryFilter<T> filter1)
	{
		List<T> passed = new ArrayList<T>();
		candidate: for (T t : objects)
		{
			if (!filter1.pass(t))
			{
				continue candidate;
			}
			passed.add(t);
		}
		return passed;
	}

	public static final <T> List<T> find(final List<T> objects, UnaryFilter<T> filter1, UnaryFilter<T> filter2)
	{
		List<T> passed = new ArrayList<T>();
		candidate: for (T t : objects)
		{
			if (!filter1.pass(t) || !filter2.pass(t))
			{
				continue candidate;
			}
			passed.add(t);
		}
		return passed;
	}

	public static final <T> List<T> find(final List<T> objects, UnaryFilter<T> filter1, UnaryFilter<T> filter2, UnaryFilter<T> filter3)
	{
		List<T> passed = new ArrayList<T>();
		candidate: for (T t : objects)
		{
			if (!filter1.pass(t) || !filter2.pass(t) || !filter3.pass(t))
			{
				continue candidate;
			}
			passed.add(t);
		}
		return passed;
	}

	public static final <T extends BoundingBox> BinaryFilter<T> alignedAxis()
	{
		return new BinaryFilter<T>()
		{
			@Override
			public boolean pass(T t1, T t2)
			{
				return t1.getBoundingBox().x == t2.getBoundingBox().x || t1.getBoundingBox().y == t2.getBoundingBox().y;
			}
		};
	}

	public static final <T extends BoundingBox> BinaryFilter<T> sameSize()
	{
		return new BinaryFilter<T>()
		{
			@Override
			public boolean pass(T t1, T t2)
			{
				return t1.getBoundingBox().height == t2.getBoundingBox().height && t1.getBoundingBox().width == t2.getBoundingBox().width;
			}
		};
	}

	public static final UnaryFilter<Blob> roundRects(final int min, final int max)
	{
		return new UnaryFilter<Blob>()
		{
			@Override
			public boolean pass(Blob blob)
			{
				float ratio = Math.abs(1 - (float) blob.bbox.width / (float) blob.bbox.height);
				return (ratio < .1f && blob.bbox.width > min && blob.bbox.width < max && isRoundRect(blob));
			}
		};
	}

	public static final boolean isRoundRect(final Blob blob)
	{
		final Rectangle box = blob.bbox;
		for (int y = 0; y < box.height; y++)
		{
			int left = 0;
			for (int x = 0; x < box.width; x++)
			{
				if (blob.get(x, y))
				{
					left = x;
					break;
				}
			}

			int right = 0;
			for (int x = box.width - 1; x > left; x--)
			{
				if (blob.get(x, y))
				{
					right = x;
					break;
				}
			}

			int width = right - left;
			int diff = Math.abs(box.width - width);
			if (width > 0 && diff > 5)
			{
				return false;
			}
		}

		return true;
	}

	public static final List<Blob> within(final Blob reference, final Blob parent, int maxDistance)
	{
		List<Blob> blobs = new ArrayList<Blob>();
		for (Blob blob : parent.children)
		{
			int distance = rectDistance(reference.bbox, blob.bbox);
			if (distance <= maxDistance)
			{
				blobs.add(blob);
			}
		}
		return blobs;
	}

	public static final boolean larger(Rectangle candidate, Rectangle reference)
	{
		float relativeDiff = candidate.height / (float) reference.height;
		return relativeDiff < .1f;
	}

	public static final boolean above(Rectangle candidate, Rectangle reference, int threshold)
	{
		int horizontal = Math.abs(reference.y - candidate.y);
		if (horizontal > threshold)
		{
			return false;
		}

		int vertical = candidate.x - reference.x;
		if (vertical < 0 || vertical > reference.width)
		{
			return false;
		}
		return true;
	}

	public static final boolean below(Rectangle candidate, Rectangle reference, int threshold)
	{
		int horizontal = Math.abs(reference.y - candidate.y);
		if (horizontal > threshold)
		{
			return false;
		}

		int vertical = candidate.x - reference.x;
		if (vertical < 0 || vertical > reference.width)
		{
			return false;
		}
		return true;
	}

	public static final <T extends BoundingBox> List<T> getHorizontalFuzzyGroup(final List<T> blobs, int width)
	{
		// area -> determine min/max values for y
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (T blob : blobs)
		{
			min = blob.getBoundingBox().y < min ? blob.getBoundingBox().y : min;
			max = blob.getBoundingBox().y > max ? blob.getBoundingBox().y : max;
		}

		// 1) all blobs within window w
		if ((max - min) < width)
		{
			return new ArrayList<T>(blobs);
		}
		// 2) determine window with maximal blob density
		else
		{
			// histogram -> count bounding boxes on specific y value
			final int length = max - min + 1;
			int[] histogram = new int[length];
			for (T blob : blobs)
			{
				histogram[blob.getBoundingBox().y - min]++;
			}

			// window -> determine initial density
			int window_count = 0, window_start = 0;
			for (int y = 0; y < width; y++)
			{
				window_count += histogram[y];
			}
			int window_max = window_count;

			// sliding window -> find range with highest density
			for (int start = 0; start < length - width; start++)
			{
				int end = start + width;
				window_count += (histogram[end] - histogram[start]);
				if (window_count > window_max)
				{
					window_max = window_count;
					window_start = start;
				}
			}
			final int window_end = window_start + width;

			// group -> get all blobs within the window
			List<T> group = new ArrayList<T>(window_max);
			for (T blob : blobs)
			{
				if (blob.getBoundingBox().y >= (min + window_start) && blob.getBoundingBox().y <= (min + window_end))
				{
					group.add(blob);
				}
			}

			return group;
		}
	}

	public static final Rectangle union(final List<? extends BoundingBox> boxes)
	{
		final Rectangle init = boxes.get(0).getBoundingBox();
		Rectangle box = new Rectangle(init.x, init.y, init.width, init.height);
		for (int i = 1; i < boxes.size(); i++)
		{
			box = box.union(boxes.get(i).getBoundingBox());
		}
		return box;
	}

	public static final <T extends BoundingBox> T getBoxMaxWidth(final List<T> blobs)
	{
		T maxBlob = blobs.get(0);
		int maxArea = maxBlob.getBoundingBox().width;
		for (T blob : blobs)
		{
			int area = blob.getBoundingBox().width;
			if (area > maxArea)
			{
				maxBlob = blob;
				maxArea = area;
			}
		}
		return maxBlob;
	}

	public static final <T extends BoundingBox> T getBoxMinWidth(final List<T> blobs)
	{
		T minBlob = blobs.get(0);
		int minArea = minBlob.getBoundingBox().width;
		for (T blob : blobs)
		{
			int area = blob.getBoundingBox().width;
			if (area < minArea)
			{
				minBlob = blob;
				minArea = area;
			}
		}
		return minBlob;
	}

	public static final <T extends BoundingBox> T getBoxMaxSize(final List<T> blobs)
	{
		T maxBlob = blobs.get(0);
		int maxArea = toArea(maxBlob);
		for (T blob : blobs)
		{
			int area = toArea(blob);
			if (area > maxArea)
			{
				maxBlob = blob;
				maxArea = area;
			}
		}
		return maxBlob;
	}

	public static final <T extends BoundingBox> T getBoxMinSize(final List<T> blobs)
	{
		T minBlob = blobs.get(0);
		int minArea = toArea(minBlob);
		for (T blob : blobs)
		{
			int area = toArea(blob);
			if (area > minArea)
			{
				minBlob = blob;
				minArea = area;
			}
		}
		return minBlob;
	}

	public static final <T extends BoundingBox> int toArea(final T blob)
	{
		return blob.getBoundingBox().width * blob.getBoundingBox().height;
	}

	public static boolean rectOverlaps(final Rectangle r, final Rectangle q)
	{
		int x0 = r.x;
		int x1 = r.x + r.width;
		int y0 = r.y;
		int y1 = r.y + r.height;

		int q_x0 = q.x;
		int q_x1 = q.x + q.width;
		int q_y0 = q.y;
		int q_y1 = q.y + q.height;

		return x0 <= q_x1 && y0 <= q_y1 && q_x0 <= x1 && q_y0 <= y1;
	}

	public static int rectDistance(final Rectangle r, final Rectangle q)
	{
		if (rectOverlaps(r, q))
		{
			return 0;
		}

		int r_x0 = r.x;
		int r_x1 = r.x + r.width;
		int r_y0 = r.y;
		int r_y1 = r.y + r.height;

		int q_x0 = q.x;
		int q_x1 = q.x + q.width;
		int q_y0 = q.y;
		int q_y1 = q.y + q.height;

		int d = 0;
		if (r_x0 > q_x1)
		{
			d += (r_x0 - q_x1) * (r_x0 - q_x1);
		} else if (q_x0 > r_x1)
		{
			d += (q_x0 - r_x1) * (q_x0 - r_x1);
		}
		if (r_y0 > q_y1)
		{
			d += (r_y0 - q_y1) * (r_y0 - q_y1);
		} else if (q_y0 > r_y1)
		{
			d += (q_y0 - r_y1) * (q_y0 - r_y1);
		}
		return (int) Math.sqrt((double) d);
	}

	public static <T> List<T> toList(@SuppressWarnings("unchecked") T... ts)
	{
		return Arrays.asList(ts);
	}

	public static <T> List<T> toList(List<T> list, @SuppressWarnings("unchecked") T... ts)
	{
		List<T> result = new ArrayList<T>(list);
		result.addAll(Arrays.asList(ts));
		return result;
	}

	public static <T> List<T> toList(List<T> list, List<T> list2, @SuppressWarnings("unchecked") T... ts)
	{
		List<T> result = new ArrayList<T>(list);
		result.addAll(list2);
		result.addAll(Arrays.asList(ts));
		return result;
	}

	public static final void renderMeta(Blob root, BufferedImage buffer)
	{
		for (Blob blob : root.children)
		{
			renderMeta(blob, buffer);
		}
	}

	public static final void renderMeta(BufferedImage buffer, Blob blob)
	{
		if (blob.meta instanceof Classes.TabPanel)
		{
			Classes.TabPanel panel = (Classes.TabPanel) blob.meta;
			String title = blob.meta.getClass().getSimpleName() + " " + panel.contour.id;
			Rectangle contour = panel.contour.bbox;
			BlobUtils.toBufferedImage(buffer, panel.contour, contour.x, contour.y, Color.red);
			Graphics graphics = buffer.getGraphics();
			{
				graphics.setColor(Color.red);
				graphics.drawString(title, contour.x, contour.y + contour.height + 12);
			}
			graphics.dispose();
		}
		else if (blob.meta instanceof Classes.Tab)
		{
			Classes.Tab tab = (Classes.Tab) blob.meta;
			String title = blob.meta.getClass().getSimpleName() + " " + tab.body.id;
			Rectangle contour = tab.body.bbox;
			BlobUtils.toBufferedImage(buffer, tab.body, contour.x, contour.y, toAlpha(Color.blue, 50));
			Graphics graphics = buffer.getGraphics();
			{
				graphics.setColor(Color.blue);
				graphics.drawString(title, contour.x, contour.y - 4);
			}
			graphics.dispose();
		}
		else if (blob.meta instanceof Classes.TextChar)
		{
			String title = "text";
			Rectangle chars = union(blob.children);
			Graphics graphics = buffer.getGraphics();
			{
				graphics.setColor(Color.green);
				graphics.drawRect(chars.x, chars.y, chars.width, chars.height);
				graphics.setColor(Color.red);
				graphics.drawString(title, chars.x, chars.y - 4);
			}
			graphics.dispose();
		}
	}

	public static Color toAlpha(Color color, int alpha)
	{
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}
}