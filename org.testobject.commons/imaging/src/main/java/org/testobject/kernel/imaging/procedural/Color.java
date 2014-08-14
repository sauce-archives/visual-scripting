package org.testobject.kernel.imaging.procedural;

import java.util.Random;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * 
 * @author enijkamp
 *
 */
public class Color {
	
	public static final Color blue = Builder.rgb(0, 0, 255);
	public static final Color white = Builder.rgb(255, 255, 255);
	public static final Color black = Builder.rgb(0, 0, 0);
	
	public final int r, g, b, a;
	
	@JsonCreator
	public Color(@JsonProperty("r") int r, @JsonProperty("g") int g, @JsonProperty("b") int b, @JsonProperty("a") int a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	public int getR() {
		return r;
	}
	
	public int getG() {
		return g;
	}
	
	public int getB() {
		return b;
	}
	
	public int getA() {
		return a;
	}

	public int toRgb() {
        return ((a & 0xff) << 24) |
               ((r & 0xff) << 16) |
               ((g & 0xff) << 8)  |
               ((b & 0xff) << 0);
	}
	
	@Override
	public String toString() {
		return "argb("+ a + "," + r + "," + g + "," + b + ")";
	}
	
	public static double l2(Color a, Color b) {
		double l2 = 0d;
		l2 += (a.r - b.r) * (a.r - b.r);
		l2 += (a.g - b.g) * (a.g - b.g);
		l2 += (a.b - b.b) * (a.b - b.b);
		return Math.sqrt(l2);
	}
	
	public static class Builder {
		public static Color random() {
			Random rnd = new Random();
			int r = Math.abs(rnd.nextInt()) % 255;
			int g = Math.abs(rnd.nextInt()) % 255;
			int b = Math.abs(rnd.nextInt()) % 255;
			return new Color(r, g, b, 255);
		}
		
		public static Color argb(int argb) {
			int a = (argb >> 24) & 0xff;
			int r = (argb >> 16) & 0xff;
			int g = (argb >> 8) & 0xff;
			int b = (argb >> 0) & 0xff;
			
			return new Color(r, g, b, a);
		}
		
		public static Color rgb(int r, int g, int b) {
			return new Color(r, g, b, 255);
		}
		
		public static Color transparent() {
			return new Color(0, 0, 0, 0);
		}
		
		public static Color argb(int a, int r, int g, int b) {
			return new Color(r, g, b, a);
		}
	}
}
