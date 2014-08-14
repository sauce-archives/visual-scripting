package org.testobject.kernel.imgproc.blob;

import org.testobject.kernel.imgproc.blob.ArrayRaster;

import java.awt.Dimension;

public final class TestUtils
{
	private TestUtils() {
	}

	public static ArrayRaster init(int[][] pixels)
	{
		if (pixels.length == 0 || pixels[0].length == 0)
		{
			return new ArrayRaster(new boolean[][] {}, new Dimension(0, 0));
		}

		Dimension size = new Dimension(pixels[0].length, pixels.length);

		boolean[][] raster = new boolean[size.height][size.width];
		for (int y = 0; y < size.height; y++)
		{
			for (int x = 0; x < size.width; x++)
			{
				raster[y][x] = pixels[y][x] != 0;
			}
		}

		return new ArrayRaster(raster, size);
	}
}
