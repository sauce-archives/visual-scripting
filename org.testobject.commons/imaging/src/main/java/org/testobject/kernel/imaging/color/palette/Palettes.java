package org.testobject.kernel.imaging.color.palette;

import java.awt.Color;

/**
 * 
 * @author enijkamp
 * 
 */
public interface Palettes {

	public static final int[] PALETTE_6 = Builder.colors(Color.red, Color.blue,
			Color.green, Color.cyan, Color.magenta, Color.yellow);
	
	public static final int[] PALETTE_3 = Builder.colors(Color.black, Color.gray, Color.white);
	
	public static final int[] PALETTE_BW = Builder.colors(Color.white, Color.black);

	public static class Builder {

		private static final int PRECESION = 4;

		public static int[] colors(Color ... colors) {
			int[] palette = new int[colors.length];
			for (int i = 0; i < colors.length; i++) {
				palette[i] = colors[i].getRGB();
			}
			return palette;
		}

		public static int[] spectrum(int n) {

			int[] palette = new int[n];
			for (int i = 0; i < palette.length; i++) {
				float fraction = round(((float) i) / (palette.length / 6), PRECESION);
				float h = round(0.95f * fraction, PRECESION);
				Color color = Color.getHSBColor(h, 1, 1);
				palette[i] = color.getRGB();
			}

			return palette;
		}
		
		private static float round(double valueToRound, int numberOfDecimalPlaces) {
			double multipicationFactor = Math.pow(10, numberOfDecimalPlaces);
			double interestedInZeroDPs = valueToRound * multipicationFactor;
		    return (float) (Math.round(interestedInZeroDPs) / multipicationFactor);
		}

		public static int[] grayscale(int n) {

			int[] palette = new int[n];
			for (int i = 0; i < palette.length; i++) {
				float fraction = ((float) i) / (palette.length - 1);
				Color color = new Color(0.9f * fraction, 0.9f * fraction,
						0.9f * fraction);
				palette[i] = color.getRGB();
			}

			return palette;
		}

		public static int[] gradient(int n, Color start, Color end) {

			int[] palette = new int[n];
			for (int i = 0; i < palette.length; i++) {
				float fraction = ((float) i) / (palette.length - 1);

				float r1 = start.getRed() / 255.0F;
				float r2 = end.getRed() / 255.0F;
				float r = Math.max(0,
						Math.min(1, r2 * fraction + r1 * (1 - fraction)));
				float g1 = start.getGreen() / 255.0F;
				float g2 = end.getGreen() / 255.0F;
				float g = Math.max(0,
						Math.min(1, g2 * fraction + g1 * (1 - fraction)));
				float b1 = start.getBlue() / 255.0F;
				float b2 = end.getBlue() / 255.0F;
				float b = Math.max(0,
						Math.min(1, b2 * fraction + b1 * (1 - fraction)));
				Color color = new Color(r, g, b);
				palette[i] = color.getRGB();
			}

			return palette;
		}

	}
	
}