package org.testobject.kernel.ocr;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

import org.testobject.kernel.imgproc.plot.Visualizer;
import org.testobject.kernel.imgproc.plot.VisualizerUtil;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.commons.util.image.Image;

/**
 * 
 * @author enijkamp
 * 
 */
public class HistogramFeature {

	private static boolean DEBUG = false;

	public static void partitionChar(BufferedImage image, int n) {

		// statistics
		Image.Byte gray = ImageUtil.toImageByte(image);
		float[][] stats = computeStatistics(gray);
		float[] cols = stats[0];
		float[] rows = stats[1];

		// quantiles
		float[] quantilesCols = computeQuantiles(cols, n);
		float[] quantilesRows = computeQuantiles(rows, n);

		// plot
		// plotChar(gray, n, quantilesCols, quantilesRows);

		// compute histogram
		double[][] histogram = computeHistogram(n, quantilesCols,
				quantilesRows, gray);

		// plot histogram
		plotHistograms("histogram", histogram);
	}

	// TODO may divide distance by mask size (al)
	public static double classify(List<double[][]> training,
			double[][] testHistogram) {
		double min = Double.MAX_VALUE;
		double[][] minHistogramm = null;
		for (double[][] trainingSample : training) {
			double distance = distance(testHistogram, trainingSample);
			if (!Double.isNaN(distance) && distance < min) {
				min = distance;
				minHistogramm = trainingSample;
			}
		}

		if (DEBUG) {
			plotHistograms("test histogram" + min, testHistogram);
			plotHistograms("best fit histogram" + min, minHistogramm);
		}

		// labels
//		System.out.println();
//		for (int i = 0; i < 10; i++) {
//			Triple<Double, String, BufferedImage> candidate = labels.get(i);
//			System.out.println(candidate.first + " -> " + candidate.second);
//		}

		// top 1
		return min;
	}

	public static void plotHistograms(String title, final double[][] histogram) {
		final int n = histogram.length;
		final int scale = 50;

		// plot
		VisualizerUtil.show(title, new Visualizer.Renderable() {
            public void render(Visualizer.Graphics g) {
                for (int x = 0; x < n; x++) {
                    for (int y = 0; y < n; y++) {
                        double intensity = histogram[x][y];
                        g.setColor(new Color((float) intensity,
                                (float) intensity, (float) intensity));
                        g.fillRect(x * scale, y * scale, scale, scale);
                    }
                }
            }
        });
	}

	public static double distance(double[][] v1, double[][] v2) {
		double diff = 0d;
		assert (v1.length == v2.length);
		for (int i = 0; i < v1.length; i++) {
			double[] row1 = v1[i];
			double[] row2 = v2[i];
			assert (row1.length == row2.length);
			for (int j = 0; j < row1.length; j++) {
				diff += Math.pow(row1[j] - row2[j], 2);
			}
		}
		return Math.sqrt(diff);
	}

	public static double[][] computeHistogram(Image.Byte gray, int n) {
		// statistics
		float[][] stats = computeStatistics(gray);
		float[] cols = stats[0];
		float[] rows = stats[1];

		// quantiles
		float[] quantilesCols = computeQuantiles(cols, n);
		float[] quantilesRows = computeQuantiles(rows, n);

		// compute histogram
		return computeHistogram(n, quantilesCols, quantilesRows, gray);
	}

	private static int invert(byte intensity) {
		return 255 - (intensity & 0xff);
	}

	private static float[][] computeStatistics(Image.Byte gray) {
		float[] cols = new float[gray.w];
		float[] rows = new float[gray.h];
		for (int x = 0; x < gray.w; x++) {
			for (int y = 0; y < gray.h; y++) {
				cols[x] += invert(gray.pixels[y * gray.w + x]);
				rows[y] += invert(gray.pixels[y * gray.w + x]);
			}
		}
		return new float[][] { cols, rows };
	}

	private static float[] computeQuantiles(float[] intensites, int n) {

		// partitions
		float sum = 0f;
		{
			for (int i = 0; i < intensites.length; i++) {
				sum += intensites[i];
			}
		}

		// cumulated intensities
		float[] sums = new float[intensites.length + 1];
		{
			for (int i = 1; i < sums.length - 1; i++) {
				sums[i] = sums[i - 1] + (intensites[i - 1] / sum);
			}
			sums[sums.length - 1] = 1f;
		}

		// compute quantiles
		float[] quantiles = new float[n + 1];
		for (int i = 1; i < n; i++) {
			final float y = i * (1f / n); // quantile limit (.2, .4, .6, ...)
			int partition = 0;
			while (sums[partition] < y) {
				partition++;
			}
			// translated line equation for the interval of this quantile
			float b = sums[partition - 1]; // left limit of interval
			float m = sums[partition] - sums[partition - 1];
			float x_offset = (partition - 1);
			float x = x_offset + (y - b) / m;
			quantiles[i] = x;
		}
		quantiles[n] = intensites.length;

		return quantiles;
	}

	private static double[][] computeHistogram(int n, float[] quantilesCols,
			float[] quantilesRows, Image.Byte gray) {
		double[][] histogram = new double[n][n];
		for (int i = 0; i < n; i++) {
			// partition rect -> (l)eft, (r)right, ...
			double l = quantilesCols[i];
			double r = quantilesCols[i + 1];
			double[] cols = histogram[i];
			for (int j = 0; j < n; j++) {
				// weighted average partition intensity
				double partition_sum_int = 0f;
				// partition rect -> (l)eft, (r)right, ...
				double t = quantilesRows[j];
				double b = quantilesRows[j + 1];
				// neighboring pixels
				for (double x = Math.floor(l); x < Math.ceil(r); x++) {
					// x
					double begin_x = Math.max(x, l);
					double end_x = Math.min(x + 1, r);
					double portion_x = end_x - begin_x;
					for (double y = Math.floor(t); y < Math.ceil(b); y++) {
						// y
						double begin_y = Math.max(y, t);
						double end_y = Math.min(y + 1, b);
						double portion_y = end_y - begin_y;
						// intensity of sub-pixel
						partition_sum_int += portion_x
								* portion_y
								* invert(gray.pixels[(int) y * gray.w + (int) x]);
					}
				}
				// average
				cols[j] = partition_sum_int / ((r - l) * (b - t)) / 255f;
			}
		}
		return histogram;
	}
}
