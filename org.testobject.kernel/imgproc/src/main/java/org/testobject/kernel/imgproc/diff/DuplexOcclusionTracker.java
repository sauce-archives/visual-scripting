package org.testobject.kernel.imgproc.diff;

import java.util.ArrayList;
import java.util.List;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.image.Image;

/**
 * Occlusion tracker comparing the before and after state of occlusions to determine the actual occlusion.
 * Tracker keeps a list of occlusions and identifies disappeared occlusions which are part of the given
 * delta region, but should not by processed by classification. Since each occlusion is only defined by two images
 * and a region and the interplay between occlusions is omitted, the tracker cannot correctly identify multiple
 * stacking occlusions (disappeared occlusion is still part of the before image of another occlusion).
 * 
 * @author nijkamp
 *
 */
public class DuplexOcclusionTracker implements OcclusionTracker
{
	private static final Rectangle.Int EMPTY = new Rectangle.Int(0, 0, 0, 0);

	private static class Occlusion
	{
		final Image.Int before, after;
		final Rectangle.Int region;

		public Occlusion(Image.Int before, Image.Int after, Rectangle.Int region)
		{
			this.before = before;
			this.after = after;
			this.region = region;
		}
	}

	private List<Occlusion> occlusions = new ArrayList<Occlusion>();

	public Rectangle.Int process(Image.Int before, Image.Int after, Rectangle.Int region)
	{
		// determine intersecting occlusions
		List<Occlusion> intersections = new ArrayList<Occlusion>();
		for (Occlusion occlusion : occlusions)
		{
			// occlusion can only disappear if it's fully contained in region
			if (region.contains(occlusion.region))
			{
				intersections.add(occlusion);
			}
		}

		// is occlusion still there?
		List<Occlusion> disappeared = new ArrayList<Occlusion>();
		for (Occlusion occlusion : intersections)
		{
			int total = 0, restored = 0;

			// parts which are not covered should be gone
			for (int y = 0; y < occlusion.region.h; y++)
			{
				for (int x = 0; x < occlusion.region.w; x++)
				{
					// transform into global coordinate system
					int globalY = occlusion.region.y + y;
					int globalX = occlusion.region.x + x;

					// occlusion covered pixel
					boolean wasCoveredByOldOcclusion = getPixel(occlusion.before, globalX, globalY) != getPixel(occlusion.after, globalX,
					        globalY);

					// count restored pixels
					if (wasCoveredByOldOcclusion)
					{
						boolean isRestored = getPixel(occlusion.before, globalX, globalY) == getPixel(after, globalX, globalY);
						restored += (isRestored ? 1 : 0);
						total++;
					}
				}
			}

			// still visible?            
			float similarity = restored / (float) total;
			if (similarity > .1f)
			{
				disappeared.add(occlusion);
			}
		}

		// not occupied anymore
		occlusions.removeAll(disappeared);

		// determine real occlusion
		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

		// for each pixel ...
		int total = 0, restored = 0;
		for (int y = 0; y < region.h; y++)
		{
			for (int x = 0; x < region.w; x++)
			{
				int globalY = region.y + y;
				int globalX = region.x + x;

				boolean isCovered = getPixel(before, globalX, globalY) != getPixel(after, globalX, globalY);
				boolean isRestored = false;

				for (Occlusion occlusion : disappeared)
				{
					if (occlusion.region.contains(globalX, globalY))
					{
						if (getPixel(occlusion.before, globalX, globalY) == getPixel(after, globalX, globalY))
						{
							isRestored = true;
							break;
						}
					}
				}

				if (!isRestored && isCovered)
				{
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

		// an occlusion only disappeared
		float similarity = restored / (float) total;

		// return empty or computed occlusion
		if (similarity > .95f)
		{
			return EMPTY;
		}
		else
		{
			Rectangle.Int newOcclusion = new Rectangle.Int(minX, minY, maxX - minX + 1, maxY - minY + 1);
			occlusions.add(new Occlusion(before, after, newOcclusion));
			return newOcclusion;
		}

	}

	private final static int getPixel(Image.Int image, int x, int y) {
		return image.pixels[y * image.w + x];
	}

}
