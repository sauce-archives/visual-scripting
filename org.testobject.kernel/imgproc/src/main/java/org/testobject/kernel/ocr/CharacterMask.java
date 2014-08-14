package org.testobject.kernel.ocr;

/**
 * 
 * @author enijkamp
 * 
 */
public class CharacterMask {

	public final int[][] mask;
	public final char chr;
	public final int fontSize;
	public final String fontName;
	public final int width, height;

	public double[][] histogram;

	public CharacterMask(int[][] mask, int fontSize, char chr, String fontName) {
		super();
		this.mask = mask;
		this.chr = chr;
		this.fontSize = fontSize;
		this.fontName = fontName;
		this.width = getWidth();
		this.height = mask.length;
	}

	public boolean get(int x, int y) {
		return mask[y][0] <= x && mask[y][1] >= x;
	}

	private int getWidth() {
		int maxWidth = 0;
		for (int i = 0; i < mask.length; i++) {
			maxWidth = Math.max(maxWidth, mask[i][1]);
		}
		return maxWidth + 1;
	}

	@Override
	public String toString() {
		return String.format("character: %s, font: %s, w: %d, h: %d", chr, fontName, width, height);
	}
}
