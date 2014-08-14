package org.testobject.kernel.imgproc.diff;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.image.Image;

/**
 * Occlusion tracker keeps a stack of occlusion levels to determine the actual occupied region.
 * Each level is associated with an image depicting the occlusion and a boolean array tracking
 * occlusions on a per pixel basis.
 * 
 * @author enijkamp
 *
 */
public class StackOcclusionTracker implements OcclusionTracker
{
	private static final Rectangle.Int EMPTY = new Rectangle.Int(0, 0, 0, 0);

	private static class Level
	{
		final Image.Int image;
		final Rectangle.Int region;
		final boolean[][] occlusion;

		public Level(Image.Int image, Rectangle.Int region, boolean[][] occlusion)
		{
			this.image = image;
			this.region = region;
			this.occlusion = occlusion;
		}
	}

	private List<Level> levels = new LinkedList<Level>();

	public Rectangle.Int process(Image.Int before, Image.Int after, Rectangle.Int region)
	{
		/* 0. root level */

		// root level
		if (levels.isEmpty())
		{
			boolean[][] occlusion = new boolean[before.h][before.w];
			for (int y = 0; y < before.h; y++)
			{
				for (int x = 0; x < before.w; x++)
				{
					occlusion[y][x] = true;
				}
			}
			Level root = new Level(before, new Rectangle.Int(0, 0, before.w, before.h), occlusion);
			levels.add(root);
		}

		/* 1. identify affected occlusions in stack */

		// determine intersecting occlusions
		List<Level> intersections = new ArrayList<>();
		for (Level level : levels)
		{
			// occlusion can only disappear if it's fully contained in region
			if (region.contains(level.region))
			{
				intersections.add(level);
			}
		}

		// is occlusion still there?
		List<Level> disappeared = new ArrayList<>();
		for (Level level : intersections)
		{
			int total = 0, restored = 0;

			// parts which are not covered should be gone
			for (int y = 0; y < level.region.h; y++)
			{
				for (int x = 0; x < level.region.w; x++)
				{
					// transform into global w system
					int globalY = level.region.y + y;
					int globalX = level.region.x + x;

					// pixel is covered by occlusion
					if (level.occlusion[globalY][globalX])
					{
						// count restored pixels
						int pixelBefore = getPixelBefore(levels, level, globalX, globalY);
						int pixelCurrent = getPixel(after, globalX, globalY);
						restored += (pixelBefore == pixelCurrent ? 1 : 0);
						total++;
					}
				}
			}

			// still visible?            
			float reconstruction = restored / (float) total;
			if (reconstruction > .1f)
			{
				disappeared.add(level);
			}
		}

		// not occupied anymore
		levels.removeAll(disappeared);

		/* 2. determine occupied area on a per-pixel basis */

		// determine real occlusion
		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

		// for each pixel ...
		boolean[][] occupiedPixels = new boolean[before.h][before.w];
		int total = 0, restored = 0;
		for (int y = 0; y < region.h; y++)
		{
			for (int x = 0; x < region.w; x++)
			{
				int globalY = region.y + y;
				int globalX = region.x + x;

				boolean isCovered = getPixel(before, globalX, globalY) != getPixel(after, globalX, globalY);
				boolean isRestored = false;

				for (Level level : disappeared)
				{
					if (level.region.contains(globalX, globalY))
					{
						int pixelCurrent = getPixel(after, globalX, globalY);
						if (matchesPixelBefore(levels, globalX, globalY, pixelCurrent))
						{
							isRestored = true;
							break;
						}
					}
				}

				if (!isRestored && isCovered)
				{
					// set
					occupiedPixels[globalY][globalX] = true;

					// min
					minX = (globalX < minX ? globalX : minX);
					minY = (globalY < minY ? globalY : minY);

					// max
					maxX = (globalX > maxX ? globalX : maxX);
					maxY = (globalY > maxY ? globalY : maxY);
				}

				if (isCovered)
				{
					total++;
					restored += (isRestored ? 1 : 0);
				}
			}
		}

		/* 3. update stack */

		// an occlusion only disappeared
		float reconstruction = restored / (float) total;

		// return empty or computed occlusion
		if (reconstruction > .95f)
		{
			// no more occlusions?
			if (levels.size() == 1)
			{
				levels.clear();
			}

			// nothing to cover
			return EMPTY;
		}
		else
		{
			// computed occlusion
			Rectangle.Int occupiedRegion = new Rectangle.Int(minX, minY, maxX - minX + 1, maxY - minY + 1);
			levels.add(new Level(after, occupiedRegion, occupiedPixels));
			return occupiedRegion;
		}

	}

	private boolean matchesPixelBefore(List<Level> levels, int x, int y, int pixel)
	{
		// traverse from "newest" to "oldest" occlusion
		for (int i = levels.size() - 1; i >= 0; i--)
		{
			if (levels.get(i).occlusion[y][x])
			{
				if (getPixel(levels.get(i).image, x, y) == pixel)
				{
					return true;
				}
			}
		}
		return false;
	}

	private int getPixelBefore(List<Level> levels, Level level, int x, int y)
	{
		int pos = getPosition(levels, level);
		if (pos == 0)
		{
			return getPixel(levels.get(0).image, x, y);
		}
		else
		{
			for (int i = pos - 1; i >= 0; i--)
			{
				if (levels.get(i).occlusion[y][x])
				{
					return getPixel(levels.get(i).image, x, y);
				}
			}
			throw new IllegalStateException();
		}
	}

	private int getPosition(List<Level> levels, Level level)
	{
		for (int i = 0; i < levels.size(); i++)
		{
			if (levels.get(i) == level)
			{
				return i;
			}
		}
		throw new IllegalStateException();
	}

	private final static int getPixel(Image.Int image, int x, int y) {
		return image.pixels[y * image.w + x];
	}
}
