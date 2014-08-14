package org.testobject.kernel.imaging.contours;

import java.util.LinkedList;
import java.util.List;

import org.testobject.kernel.imaging.segmentation.Mask;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imaging.procedural.Color;
import org.testobject.commons.math.algebra.Point;

/**
 * 
 * @author enijkamp
 *
 */
public class ColorExtractor {
	
	public static Color extractColor(Image.Int image) {

		int sum_r = 0, sum_g = 0, sum_b = 0, sum_a = 0, sum = 0;
		
		for (int x = 0; x < image.w; x++) {
			for (int y = 0; y < image.h; y++) {
				int rgba = image.pixels[y * image.w + x];
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
		
		return sum == 0 ? new Color(0, 0, 0, 0) : new Color((sum_r / sum), (sum_g / sum), (sum_b / sum), (sum_a / sum));
	}
	
	public static Color extractColor(Image.Int input, Mask mask) {

		int sum_r = 0, sum_g = 0, sum_b = 0, sum_a = 0, sum = 0;
		
		for (int x = 0; x < mask.getBoundingBox().w; x++) {
			for (int y = 0; y < mask.getBoundingBox().h; y++) {
				int rgba = input.pixels[y * input.w + x];
				if (mask.get(x, y)) {
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
		
		return sum == 0 ? new Color(0, 0, 0, 0) : new Color((sum_r / sum), (sum_g / sum), (sum_b / sum), (sum_a / sum));
	}

	public static Color extractColor(Image.Int input, List<List<Point.Double>> contours, List<Point.Double> contour) {

		List<List<Point.Double>> inner = getInnerContours(contours, contour);

		int sum_r = 0, sum_g = 0, sum_b = 0, sum_a = 0, sum = 0;
		for (int x = 0; x < input.w; x++) {
			for (int y = 0; y < input.h; y++) {
				int rgba = input.pixels[y * input.w + x];
				if (isOnlyContainedIn(inner, contour, new Point.Double(x, y))) {
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
	
	private static List<List<Point.Double>> getInnerContours(List<List<Point.Double>> contours, List<Point.Double> contour) {
		List<List<Point.Double>> inner = new LinkedList<List<Point.Double>>();
		for (List<Point.Double> other : contours) {
			if (contour != other) {
				if (isContained(other, contour)) {
					inner.add(other);
				}
			}
		}
		return inner;
	}

	private static boolean isContained(List<Point.Double> other, List<Point.Double> contour) {
		for (int i = 0; i < other.size(); i++) {
			if (contour.contains(other.get(i)) == false) {
				return false;
			}
		}
		return true;
	}

	private static boolean isOnlyContainedIn(List<List<Point.Double>> contours, List<Point.Double> contour, Point.Double point) {
		if (contour.contains(point)) {
			for (List<Point.Double> other : contours) {
				if (other != contour) {
					if (other.contains(point)) {
						return false;
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}
}
