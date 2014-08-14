package org.testobject.kernel.ocr;

import java.util.List;

/**
 * 
 * @author enijkamp
 * 
 */
public class AdditiveMask {

	public final char chr;
	public final CharacterMask mask;
	public final List<CharacterMask> masks;
	public final int height, width;
	public final int fontSize;

	public AdditiveMask(char chr, int fontSize, List<CharacterMask> masks) {
		this.chr = chr;
		this.fontSize = fontSize;
		this.mask = toCharacterMask(masks);
		this.masks = masks;
		this.height = maxHeight(masks);
		this.width = maxWidth(masks);
	}

	private int maxHeight(Iterable<CharacterMask> masks) {
		int max = Integer.MIN_VALUE;
		for (CharacterMask mask : masks) {
			max = Math.max(max, mask.height);
		}
		return max;
	}

	private int maxWidth(Iterable<CharacterMask> masks) {
		int max = Integer.MIN_VALUE;
		for (CharacterMask mask : masks) {
			max = Math.max(max, mask.width);
		}
		return max;
	}

	public boolean get(int x, int y) {
		return mask.get(x, y);
	}

	CharacterMask toCharacterMask(List<CharacterMask> masks) {
		// size
		int maxHeight = 0, maxWidth = 0;
		for (CharacterMask mask : masks) {
			maxHeight = Math.max(mask.height, maxHeight);
			maxWidth = Math.max(mask.width, maxWidth);
		}

		// intervals
		int[][] intervals = new int[maxHeight][2];
		for (int y = 0; y < maxHeight; y++) {
			int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
			for (CharacterMask mask : masks) {
				if (y < mask.height) {
					min = Math.min(mask.mask[y][0], min);
					max = Math.max(mask.mask[y][1], max);
				}
			}
			intervals[y][0] = min;
			intervals[y][1] = max;
		}

		return new CharacterMask(intervals, masks.get(0).fontSize, masks.get(0).chr, "");
	}
}