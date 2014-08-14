package org.testobject.kernel.imgproc.segmentation;

public class Convolve {

	public static void convolve_even(float[][] src, float[][] dst, int width, int height, float[] mask) {
		int len = mask.length;
		for (int y = 0; y < height; y++) {
			float[] y_row = src[y];
			for (int x = 0; x < width; x++) {
				float sum = mask[0] * y_row[x];
				for (int i = 1; i < len; i++) {
					sum += mask[i] * (y_row[Math.max(x - i, 0)] + y_row[Math.min(x + i, width - 1)]);
				}
				dst[x][y] = sum;
			}
		}
	}
}
