package org.testobject.kernel.ocr;

import java.awt.Color;

import org.testobject.commons.util.image.Image;

/**
 * 
 * @author enijkamp
 * 
 */
public class MaskExtractor {

	public static CharacterMask getMask(Image.Int image, char chr, int fontSize, String fontName) {
		int[][] mask = new int[image.h][2];
		for (int y = 0; y < image.h; y++) {
			int min = Integer.MAX_VALUE;
			int max = 0;
			for (int x = 0; x < image.w; x++) {
				if (image.get(x, y) != Color.WHITE.getRGB()) {
					min = Math.min(min, x);
					max = Math.max(max, x);
				}
			}
			mask[y] = new int[] { min, max };
		}
		return new CharacterMask(mask, fontSize, chr, fontName);
	}

}
