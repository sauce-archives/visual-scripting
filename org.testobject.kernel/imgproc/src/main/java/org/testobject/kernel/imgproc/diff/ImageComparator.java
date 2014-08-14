package org.testobject.kernel.imgproc.diff;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.collections.Sets;
import org.testobject.commons.util.image.Image;

/**
 * Compares two given images and returns a set of bounding boxes which identify the delta regions.
 * 
 * @author enijkamp
 *
 */
public class ImageComparator {
	
	public static List<Rectangle.Int> compare(Image.Int image1, Image.Int image2, int tolerance) {
		// identity hash-set
		final Set<Rectangle.Int> boxes = Sets.newIdentitySet();

		final int height = image1.h;
		final int width = image1.w;

		for (int y = 0; y < height; y++) {
			int start = -1, end = -1;

			for (int x = 0; x < width; x++)	{
				if (getPixel(image1, x, y) != getPixel(image2, x, y)) {
					// interval start
					if (start == -1) {
						start = x;
					} else {
						end = x;
					}
				} else {
					// interval end
					boolean isOutOfTolerance = (x - end) > tolerance;
					boolean isNearBorder = (width - x) <= tolerance;
					if (end != -1 && (isOutOfTolerance || isNearBorder))
					{
						// bounding box
						final List<Rectangle.Int> candidates = new ArrayList<>();
						for (Rectangle.Int box : boxes)	{
							boolean isVerticalInTol = (box.y + box.h + tolerance) > y;
							boolean isStartInTol = start > (box.x - tolerance) && start < (box.x + box.w + tolerance);
							boolean isEndInTol = end > (box.x - tolerance) && end < (box.x + box.w + tolerance);
							boolean isWider = start < box.x && end > (box.x + box.w);

							if (isVerticalInTol && (isStartInTol || isEndInTol || isWider))	{
								candidates.add(box);
							}
						}

						// new box
						if (candidates.isEmpty()) {
							boxes.add(new Rectangle.Int(start, y, end - start + 1, 1));
							// System.out.println("new box x=" + start + " y=" + y + " w=" + (end-start) + " h=1");
						}
						// modify box
						else
						{
							Rectangle.Int box = candidates.remove(0);

							// merge
							if (!candidates.isEmpty())
							{
								for (Rectangle.Int candidate : candidates)
								{
									box.setRect(box.union(candidate));
								}
								// int count = boxes.size();
								boxes.removeAll(candidates);
								// System.out.println("merge " + count + " -> " + boxes.size());
							}

							// extend
							if (box.x > start) {
								box.x = start;
							}
							if ((box.x + box.w) < end) {
								box.w = (end - box.x) + 1;
							}
							box.h = y - box.y + 1;

							// System.out.println("update box x=" + box.x + " y=" + box.y + " w=" + box.w + " h=" + box.h);
						}

						// reset
						start = end = -1;
					}
				}
			}
		}
		return new ArrayList<Rectangle.Int>(boxes);
	}

	private final static int getPixel(Image.Int image, int x, int y) {
		return image.pixels[y * image.w + x];
	}
}
