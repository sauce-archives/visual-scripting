package org.testobject.kernel.imaging.filters;

public class GaussianFilter {

	private final int width, height;
	private final double[][] buffer;

	public GaussianFilter(int width, int height) {
		this.buffer = new double[height][width];
		this.width = width;
		this.height = height;
	}

	/* convolve image with gaussian filter */
	public void smooth(double[][] img, float sigma) {
		float[] mask = make_fgauss(sigma);
		normalize(mask);

		convolve_even(img, buffer, mask);
		convolve_even(buffer, img, mask);
	}

	/* normalize mask so it integrates to one */
	private void normalize(float[] mask) {
		int len = mask.length;
		float sum = 0;
		for (int i = 1; i < len; i++) {
			sum += Math.abs(mask[i]);
		}
		sum = 2 * sum + Math.abs(mask[0]);
		for (int i = 0; i < len; i++) {
			mask[i] /= sum;
		}
	}

	private float[] make_fgauss(float sigma) {
		sigma = Math.max(sigma, 0.01f);
		int len = (int) Math.ceil(sigma * 4.0) + 1;
		float[] mask = new float[len];
		for (int i = 0; i < len; i++) {
			mask[i] = (float) Math.exp(-0.5 * Math.pow(i / sigma, 2));
		}
		return mask;
	}

	private void convolve_even(double[][] source, double[][] destination, float[] mask) {
		int len = mask.length;

		for (int y = 0; y < height; y++) {
			double[] y_row = source[y];
			for (int x = 0; x < width; x++) {
				double sum = mask[0] * y_row[x];
				for (int i = 1; i < len; i++) {
					sum += mask[i] * (y_row[Math.max(x - i, 0)] + y_row[Math.min(x + i, width - 1)]);
				}
				destination[y][x] = sum;
			}
		}
	}
}
