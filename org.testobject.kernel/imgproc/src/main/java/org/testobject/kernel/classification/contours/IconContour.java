package org.testobject.kernel.classification.contours;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.blob.Blob;

/**
 * 
 * @author enijkamp
 *
 */
public class IconContour {

	public static Color extractColor(Image.Int input, List<Contour> contours, Contour contour) {

		List<Contour> inner = getInnerContours(contours, contour);

		int sum_r = 0, sum_g = 0, sum_b = 0, sum_a = 0, sum = 0;
		for (int x = 0; x < input.w; x++) {
			for (int y = 0; y < input.h; y++) {
				int rgba = input.pixels[y * input.w + x];
				if (isOnlyContainedIn(inner, contour, x, y)) {
					int a = (rgba >> 24) & 0xff;
					int r = (rgba >> 16) & 0xff;
					int g = (rgba >> 8) & 0xff;
					int b = (rgba >> 0) & 0xff;

					sum_a += a;
					sum_r += r;
					sum_g += g;
					sum_b += b;
					sum++;
				}
			}
		}
		return new Color((sum_r / sum), (sum_g / sum), (sum_b / sum), (sum_a / sum));
	}

	public static Color extractColor(BufferedImage input, List<Contour> contours, Contour contour) {

		List<Contour> inner = getInnerContours(contours, contour);

		int sum_r = 0, sum_g = 0, sum_b = 0, sum_a = 0, sum = 0;
		for (int x = 0; x < input.getWidth(); x++) {
			for (int y = 0; y < input.getHeight(); y++) {
				int rgba = input.getRGB(x, y);
				if (isOnlyContainedIn(inner, contour, x, y)) {
					int a = (rgba >> 24) & 0xff;
					int r = (rgba >> 16) & 0xff;
					int g = (rgba >> 8) & 0xff;
					int b = (rgba >> 0) & 0xff;

					sum_a += a;
					sum_r += r;
					sum_g += g;
					sum_b += b;
					sum++;
				}
			}
		}
		return new Color((sum_r / sum), (sum_g / sum), (sum_b / sum), (sum_a / sum));
	}

	private static List<Contour> getInnerContours(List<Contour> contours, Contour contour) {
		List<Contour> inner = new LinkedList<Contour>();
		for (Contour other : contours) {
			if (contour != other) {
				if (isContained(other, contour)) {
					inner.add(other);
				}
			}
		}
		return inner;
	}

	private static boolean isContained(Contour other, Contour contour) {
		for (int i = 0; i < other.npoints; i++) {
			if (contour.contains(other.xpoints[i], other.ypoints[i]) == false) {
				return false;
			}
		}
		return true;
	}

	private static boolean isOnlyContainedIn(List<Contour> contours, Contour contour, int x, int y) {
		if (contour.contains(x, y)) {
			for (Contour other : contours) {
				if (other != contour) {
					if (other.contains(x, y)) {
						return false;
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}

	public static Color extractColor(Image.Int image, Blob blob) {
		int sum_r = 0, sum_g = 0, sum_b = 0, sum_a = 0, sum = 0;
		for (int x = 0; x < blob.bbox.width; x++) {
			for (int y = 0; y < blob.bbox.height; y++) {
				if (blob.get(x, y)) {
					int rgba = image.pixels[(blob.bbox.y + y) * image.w + blob.bbox.x + x];
					{
						int a = (rgba >> 24) & 0xff;
						int r = (rgba >> 16) & 0xff;
						int g = (rgba >> 8) & 0xff;
						int b = (rgba >> 0) & 0xff;

						sum_a += a;
						sum_r += r;
						sum_g += g;
						sum_b += b;
						sum++;
					}
				}
			}
		}
		return new Color((sum_r / sum), (sum_g / sum), (sum_b / sum), (sum_a / sum));
	}
}
