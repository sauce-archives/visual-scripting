package org.testobject.kernel.imgproc.segmentation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import org.testobject.commons.util.image.Image;

/*
 *  - TODO may use 256 color images instead of real color (creating indexed images seems to be quite expensive)
 *  - TODO transitive thresholds: may merge pixels with diffs > 0
 *  - TODO check if all screenshots could be handled with default settings (0.6,10000,20)
 *
 */
public class Segmentation {

	private static int[] segment_image(BufferedImage im, float sigma, double c, int min_size) throws IOException {
		int width = im.getWidth();
		int height = im.getHeight();

		float[][] r_im = new float[height][width];
		float[][] g_im = new float[height][width];
		float[][] b_im = new float[height][width];

		// smooth each color channel
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int rgb = im.getRGB(x, y);
				r_im[y][x] = ((rgb >> 16) & 0xff);
				g_im[y][x] = ((rgb >> 8) & 0xff);
				b_im[y][x] = ((rgb >> 0) & 0xff);
			}
		}

		long startSmoothing = System.currentTimeMillis();

		Filter.smooth(r_im, width, height, sigma);
		Filter.smooth(g_im, width, height, sigma);
		Filter.smooth(b_im, width, height, sigma);

		System.out.println("Smoothing: " + (System.currentTimeMillis() - startSmoothing));

		int[] a = new int[width * height * 4];
		int[] b = new int[width * height * 4];
		double[] w = new double[width * height * 4];

		int _a;
		int _b;
		double _w;

		int num = 0;
		int numEnd = (width * height * 4) - 1;
		for (int y = 0; y < height; y++) {
			float[] r_row_0 = y > 0 ? r_im[y - 1] : null;
			float[] g_row_0 = y > 0 ? g_im[y - 1] : null;
			float[] b_row_0 = y > 0 ? b_im[y - 1] : null;
			float[] r_row_1 = r_im[y];
			float[] g_row_1 = g_im[y];
			float[] b_row_1 = b_im[y];
			float[] r_row_2 = y < height - 1 ? r_im[y + 1] : null;
			float[] g_row_2 = y < height - 1 ? g_im[y + 1] : null;
			float[] b_row_2 = y < height - 1 ? b_im[y + 1] : null;
			for (int x = 0; x < width; x++) {
				float r0 = r_row_1[x];
				float g0 = g_row_1[x];
				float b0 = b_row_1[x];
				if (x < width - 1) {
					_a = y * width + x;
					_b = y * width + (x + 1);
					_w = diff(r0, r_row_1[x + 1], g0, g_row_1[x + 1], b0, b_row_1[x + 1]);
					if (_w != 0) {
						a[num] = _a;
						b[num] = _b;
						w[num] = _w;
						num++;
					} else {
						a[numEnd] = _a;
						b[numEnd] = _b;
						w[numEnd] = _w;
						numEnd--;
					}
				}

				if (y < height - 1) {
					_a = y * width + x;
					_b = (y + 1) * width + x;
					_w = diff(r0, r_row_2[x], g0, g_row_2[x], b0, b_row_2[x]);
					if (_w != 0) {
						a[num] = _a;
						b[num] = _b;
						w[num] = _w;
						num++;
					} else {
						a[numEnd] = _a;
						b[numEnd] = _b;
						w[numEnd] = _w;
						numEnd--;
					}
				}

				if ((x < width - 1) && (y < height - 1)) {
					_a = y * width + x;
					_b = (y + 1) * width + (x + 1);
					_w = diff(r0, r_row_2[x + 1], g0, g_row_2[x + 1], b0, b_row_2[x + 1]);
					if (_w != 0) {
						a[num] = _a;
						b[num] = _b;
						w[num] = _w;
						num++;
					} else {
						a[numEnd] = _a;
						b[numEnd] = _b;
						w[numEnd] = _w;
						numEnd--;
					}
				}

				if ((x < width - 1) && (y > 0)) {
					_a = y * width + x;
					_b = (y - 1) * width + (x + 1);
					_w = diff(r0, r_row_0[x + 1], g0, g_row_0[x + 1], b0, b_row_0[x + 1]);
					if (_w != 0) {
						a[num] = _a;
						b[num] = _b;
						w[num] = _w;
						num++;
					} else {
						a[numEnd] = _a;
						b[numEnd] = _b;
						w[numEnd] = _w;
						numEnd--;
					}
				}
			}
		}

		IntroSort.sort(a, b, w, 0, num - 1);

		// init thresholds
		double[] threshold = new double[height * width];
		Arrays.fill(threshold, c);

		// segment
		// make a disjoint-set forest
		int[] p = new int[height * width];
		int[] rank = new int[height * width];
		int[] size = new int[height * width];
		for (int i = 0; i < p.length; i++) {
			p[i] = i;
			size[i] = 1;
		}

		for (int i = (width * height * 4) - 1; i >= numEnd; i--) {
			int a_tmp = find(p, a[i]);
			int b_tmp = find(p, b[i]);
			double w_tmp = w[i];
			if (a_tmp != b_tmp) {
				join(rank, size, p, a_tmp, b_tmp);
				a_tmp = find(p, a_tmp);
				threshold[a_tmp] = w_tmp + threshold(size[a_tmp], c);
			}
		}

		// for each edge, in non-decreasing weight order...
		for (int i = 0; i < num; i++) {
			// components conected by this edge
			int a_tmp = find(p, a[i]);
			int b_tmp = find(p, b[i]);
			double w_tmp = w[i];
			if (a_tmp != b_tmp) {
				if ((w_tmp <= threshold[a_tmp]) && (w_tmp <= threshold[b_tmp])) {
					join(rank, size, p, a_tmp, b_tmp);
					a_tmp = find(p, a_tmp);
					threshold[a_tmp] = w_tmp + threshold(size[a_tmp], c);
				}
			}
		}

		// post process small components
		for (int i = 0; i < num; i++) {
			int a_tmp = find(p, a[i]);
			int b_tmp = find(p, b[i]);
			if ((a_tmp != b_tmp) && ((size[a_tmp] < min_size) || (size[b_tmp] < min_size))) {
				join(rank, size, p, a_tmp, b_tmp);
			}
		}

		return p;
	}

	// random color
	public static Image.Int visualizeSegmentation(BufferedImage input) throws IOException {
		float sigma = 0.6f;
		double k = 10000.0f;
		int min_size = 20;

		int[] p = segment_image(input, sigma, k, min_size);

		int height = input.getHeight();
		int width = input.getWidth();
		Image.Int output = new Image.Int(width, height);

		// pick random colors for each component
		int[] colors = new int[width * height];
		for (int i = 0; i < width * height; i++) {
			colors[i] = random_rgb();
		}

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int comp = find(p, y * width + x);
				output.pixels[y * width + x] = colors[comp];
			}
		}
		return output;
	}

	private static int random_rgb() {
		Random random = new Random();

		int r = random.nextInt(256);
		int g = random.nextInt(256);
		int b = random.nextInt(256);

		return ((255 & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
	}

	public static double threshold(int size, double c) {
		return c / size;
	}

	private static final double diff(double r0, double r1, double g0, double g1, double b0, double b1) {
		return Math.sqrt((r0 - r1) * (r0 - r1) + (g0 - g1) * (g0 - g1) + (b0 - b1) * (b0 - b1));
	}

	private static final void join(int[] rank, int[] size, int[] p, int x, int y) {
		if (rank[x] > rank[y]) {
			mergeYintoX(size, p, x, y);
		} else {
			mergeXintoY(rank, size, p, x, y);
		}
	}

	private static final void mergeYintoX(int[] size, int[] p, int x, int y) {
		p[y] = x;
		size[x] += size[y];
	}

	private static final void mergeXintoY(int[] rank, int[] size, int[] p, int x, int y) {
		p[x] = y;
		size[y] += size[x];
		if (rank[x] == rank[y]) {
			rank[y]++;
		}
	}

	private static final int find(int[] p, int x) {
		int y = x;
		while (y != p[y]) {
			y = p[y];
		}
		p[x] = y;
		return y;
	}

}
