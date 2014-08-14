package org.testobject.commons.util.image;

/**
 * 
 * @author enijkamp
 * 
 */
public class Image {

	public static final class Bool {

		public final boolean[] pixels;
		public final int x, y, w, h;
		public final int scanline;

		public Bool(int width, int height) {
			this(new boolean[height * width], width, height);
		}

		public Bool(boolean[] pixels, int width, int height) {
			this(pixels, 0, 0, width, height, width);
		}

		public Bool(boolean[] pixels, int x, int y, int w, int h, int scanline) {
			this.pixels = pixels;
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.scanline = scanline;
		}

		public boolean get(int x, int y) {
			return pixels[y * w + x];
		}

		public void set(int x, int y, boolean value) {
			pixels[y * w + x] = value;
		}
	}

	public static final class Int {

		public enum Type {
			RGB, ARGB
		}

		public final int[] pixels;
		public final int x, y, w, h;
		public final int scanline;
		public final Type type;

		public Int(int width, int height) {
			this(new int[height * width], width, height);
		}
		
		public Int(int width, int height, Type type) {
			this(new int[height * width], 0, 0, width, height, width, type);
		}

		public Int(int[] pixels, int width, int height) {
			this(pixels, 0, 0, width, height, width, Type.RGB);
		}

		public Int(int[] pixels, int x, int y, int w, int h, int scanline, Type type) {
			this.pixels = pixels;
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.scanline = scanline;
			this.type = type;
		}

		public int get(int x, int y) {
			return pixels[y * w + x];
		}

		public void set(int x, int y, int value) {
			pixels[y * w + x] = value;
		}
	}

	public static final class Byte {

		public final byte[] pixels;
		public final int x, y, w, h;
		public final int scanline;

		public Byte(int width, int height) {
			this(new byte[height * width], width, height);
		}

		public Byte(byte[] pixels, int width, int height) {
			this(pixels, 0, 0, width, height, width);
		}

		public Byte(byte[] pixels, int x, int y, int w, int h, int scanline) {
			this.pixels = pixels;
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.scanline = scanline;
		}

		public byte get(int x, int y) {
			return pixels[y * w + x];
		}

		public void set(int x, int y, byte value) {
			pixels[y * w + x] = value;
		}
	}

	public static final class Double {

		public final double[] pixels;
		public final int x, y, w, h;
		public final int scanline;

		public Double(int width, int height) {
			this(new double[height * width], width, height);
		}

		public Double(double[] pixels, int width, int height) {
			this(pixels, 0, 0, width, height, width);
		}

		public Double(double[] pixels, int x, int y, int w, int h, int scanline) {
			this.pixels = pixels;
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.scanline = scanline;
		}

		public double get(int x, int y) {
			return pixels[y * w + x];
		}

		public void set(int x, int y, double value) {
			pixels[y * w + x] = value;
		}
	}

	public static final class Float {

		public final float[] pixels;
		public final int x, y, w, h;
		public final int scanline;

		public Float(int width, int height) {
			this(new float[height * width], width, height);
		}

		public Float(float[] pixels, int width, int height) {
			this(pixels, 0, 0, width, height, width);
		}

		public Float(float[] pixels, int x, int y, int w, int h, int scanline) {
			this.pixels = pixels;
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.scanline = scanline;
		}

		public float get(int x, int y) {
			return pixels[y * w + x];
		}

		public void set(int x, int y, float value) {
			pixels[y * w + x] = value;
		}
	}

}
