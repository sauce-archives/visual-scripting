package org.testobject.kernel.imgproc.segmentation;

public class Filter {

	/* convolve image with gaussian filter */
	public static void smooth(float[][] img, int width, int height, float sigma) {
		float[] mask = make_fgauss(sigma);
		normalize(mask);

		float[][] tmp = new float[height][width];
		Convolve.convolve_even(img, tmp, width, height, mask);
		Convolve.convolve_even(tmp, img, width, height, mask);
	}

	/* normalize mask so it integrates to one */
	public static void normalize(float[] mask) {
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

	public static float[] make_fgauss(float sigma) {
		sigma = Math.max(sigma, 0.01f);
		int len = (int) Math.ceil(sigma * 4.0) + 1;
		float[] mask = new float[len];
		for (int i = 0; i < len; i++) {
			mask[i] = (float) Math.exp(-0.5 * Math.pow(i / sigma, 2));
		}
		return mask;
	}

}
