package org.testobject.kernel.imaging.segmentation;

import org.testobject.commons.math.algebra.Size;

/**
 * 
 * @author enijkamp
 *
 */
public final class TestUtils
{
	private TestUtils() {
	}

	public static ArrayRaster init(int[][] pixels)
	{
		if (pixels.length == 0 || pixels[0].length == 0)
		{
			return new ArrayRaster(new boolean[][] {}, new Size.Int(0, 0));
		}

		Size.Int size = new Size.Int(pixels[0].length, pixels.length);

		boolean[][] raster = new boolean[size.h][size.w];
		for (int y = 0; y < size.h; y++)
		{
			for (int x = 0; x < size.w; x++)
			{
				raster[y][x] = pixels[y][x] != 0;
			}
		}

		return new ArrayRaster(raster, size);
	}
}
