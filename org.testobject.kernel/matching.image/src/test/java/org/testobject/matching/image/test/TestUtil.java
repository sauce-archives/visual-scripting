package org.testobject.matching.image.test;

import org.testobject.commons.math.algebra.Rectangle;

public class TestUtil {

	public static boolean areIntersecting(Rectangle.Int template, Rectangle.Int match) {

		boolean xOverlap = inRange(template.x, match.x, match.x + match.w)
				|| inRange(match.x, template.x, template.x + template.w);

		boolean yOverlap = inRange(template.y, match.y, match.y + match.h)
				|| inRange(match.y, template.y, template.y + template.h);

		if (xOverlap || yOverlap) {
			return true;
		}

		return false;
	}

	public static boolean inRange(int value, int min, int max) {
		return value >= min && value <= max;
	}
	
	public static boolean areClose(Rectangle.Int template, Rectangle.Int match, int threshold) {
		
		return areClose(template.x, match.x, threshold) && areClose(template.y, match.y, threshold)
				&& areClose(template.x + template.w, match.x + match.w, threshold) 
				&& areClose(template.y + template.h, match.y + match.h, threshold) ? true : false;
	}
	
	private static boolean areClose(int first, int second, int threshold) {		
		return Math.abs(first - second) > threshold ? false : true; 
	}
}
