package org.testobject.commons.util.image;

/**
 * 
 * @author enijkamp
 *
 */
public class PixelFormat {

	public int bitsPerPixel; // 8, 16 or 32
	public int depth; // 8 to 32
	public boolean bigEndianFlag;
	public boolean trueColourFlag; // False -> requires colour map
	public int redMax; // Ignored if trueColor = false
	public int greenMax; // Ignored if trueColor = false
	public int blueMax; // Ignored if trueColor = false
	public int redShift; // Ignored if trueColor = false
	public int greenShift; // Ignored if trueColor = false
	public int blueShift; // Ignored if trueColor = false

	public PixelFormat() {

	}

	public PixelFormat(int bitsPerPixel, int depth, boolean bigEndianFlag, boolean trueColourFlag, int redMax, int greenMax, int blueMax,
	        int redShift, int greenShift, int blueShift) {
		this.bitsPerPixel = bitsPerPixel;
		this.depth = depth;
		this.bigEndianFlag = bigEndianFlag;
		this.trueColourFlag = trueColourFlag;
		this.redMax = redMax;
		this.greenMax = greenMax;
		this.blueMax = blueMax;
		this.redShift = redShift;
		this.greenShift = greenShift;
		this.blueShift = blueShift;
	}

	public static PixelFormat create32bppPixelFormat(boolean bigEndianFlag) {
		final PixelFormat pixelFormat = new PixelFormat();
		pixelFormat.bigEndianFlag = bigEndianFlag;
		pixelFormat.bitsPerPixel = 32;
		pixelFormat.blueMax = 255;
		pixelFormat.blueShift = 0;
		pixelFormat.greenMax = 255;
		pixelFormat.greenShift = 8;
		pixelFormat.redMax = 255;
		pixelFormat.redShift = 16;
		pixelFormat.depth = 24;
		pixelFormat.trueColourFlag = true;
		return pixelFormat;
	}

	/**
	 * specifies 65536 colors, 5bit per Red, 6bit per Green, 5bit per Blue
	 */
	public static PixelFormat create16bppPixelFormat(boolean bigEndianFlag) {
		final PixelFormat pixelFormat = new PixelFormat();
		pixelFormat.bigEndianFlag = bigEndianFlag;
		pixelFormat.bitsPerPixel = 16;
		pixelFormat.blueMax = 31;
		pixelFormat.blueShift = 0;
		pixelFormat.greenMax = 63;
		pixelFormat.greenShift = 5;
		pixelFormat.redMax = 31;
		pixelFormat.redShift = 11;
		pixelFormat.depth = 16;
		pixelFormat.trueColourFlag = true;
		return pixelFormat;
	}

	/**
	 * specifies 256 colors, 2bit per Blue, 3bit per Green & Red
	 */
	public static PixelFormat create8bppPixelFormat(boolean bigEndianFlag) {
		final PixelFormat pixelFormat = new PixelFormat();
		pixelFormat.bigEndianFlag = bigEndianFlag;
		pixelFormat.bitsPerPixel = 8;
		pixelFormat.redMax = 7;
		pixelFormat.redShift = 0;
		pixelFormat.greenMax = 7;
		pixelFormat.greenShift = 3;
		pixelFormat.blueMax = 3;
		pixelFormat.blueShift = 6;
		pixelFormat.depth = 8;
		pixelFormat.trueColourFlag = true;
		return pixelFormat;
	}

	/**
	 * specifies 64 colors, 2bit per Red, Green & Blue
	 */
	public static PixelFormat create6bppPixelFormat(boolean bigEndianFlag) {
		final PixelFormat pixelFormat = new PixelFormat();
		pixelFormat.bigEndianFlag = bigEndianFlag;
		pixelFormat.bitsPerPixel = 8;
		pixelFormat.blueMax = 3;
		pixelFormat.blueShift = 0;
		pixelFormat.greenMax = 3;
		pixelFormat.greenShift = 2;
		pixelFormat.redMax = 3;
		pixelFormat.redShift = 4;
		pixelFormat.depth = 6;
		pixelFormat.trueColourFlag = true;
		return pixelFormat;
	}

	/**
	 * specifies 8 colors, 1bit per Red, Green & Blue
	 */
	public static PixelFormat create3bppPixelFormat(boolean bigEndianFlag) {
		final PixelFormat pixelFormat = new PixelFormat();
		pixelFormat.bigEndianFlag = bigEndianFlag;
		pixelFormat.bitsPerPixel = 8;
		pixelFormat.blueMax = 1;
		pixelFormat.blueShift = 0;
		pixelFormat.greenMax = 1;
		pixelFormat.greenShift = 1;
		pixelFormat.redMax = 1;
		pixelFormat.redShift = 2;
		pixelFormat.depth = 3;
		pixelFormat.trueColourFlag = true;
		return pixelFormat;
	}

	@Override
	public String toString() {
		return "PixelFormat: [bits-per-pixel: " + String.valueOf(0xff & bitsPerPixel) +
		        ", depth: " + String.valueOf(0xff & depth) +
		        ", big-endian-flag: " + String.valueOf(bigEndianFlag) +
		        ", true-color-flag: " + String.valueOf(trueColourFlag) +
		        ", red-max: " + String.valueOf(0xffff & redMax) +
		        ", green-max: " + String.valueOf(0xffff & greenMax) +
		        ", blue-max: " + String.valueOf(0xffff & blueMax) +
		        ", red-shift: " + String.valueOf(0xff & redShift) +
		        ", green-shift: " + String.valueOf(0xff & greenShift) +
		        ", blue-shift: " + String.valueOf(0xff & blueShift) +
		        "]";
	}
}
