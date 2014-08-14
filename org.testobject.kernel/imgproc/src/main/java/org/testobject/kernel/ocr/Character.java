package org.testobject.kernel.ocr;

import java.awt.Font;

import org.testobject.kernel.imgproc.blob.BoundingBox;
import org.testobject.kernel.imgproc.blob.BooleanRaster;

/**
 * 
 * @author enijkamp
 * 
 * @param <T>
 */
public class Character<T extends BooleanRaster & BoundingBox> {
	public final T raster;
	public final Font font;
	public final char character;
	public final int topLine, baseLine;

	public Character(T raster, Font font, char character, int topLine, int baseLine) {
		this.raster = raster;
		this.character = character;
		this.font = font;
		this.topLine = topLine;
		this.baseLine = baseLine;
	}

	public static char[] getAllChars() {
		String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz" + "0123456789" + "@";
		return alphabet.toCharArray();
	}
}