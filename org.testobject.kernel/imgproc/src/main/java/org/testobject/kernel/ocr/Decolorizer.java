package org.testobject.kernel.ocr;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

import org.testobject.kernel.imgproc.blob.BoundingBox;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.Image.Int;
import org.testobject.commons.util.tuple.Pair;
import org.testobject.kernel.imgproc.blob.BooleanRaster;

// TODO reuse color arrays
public class Decolorizer {

	public static class ColorRange {

		public static final ColorRange WHITE = new ColorRange(new int[] { 255, 255, 255 }, new int[] { 0, 0, 0 });

		public final int red;
		public final int green;
		public final int blue;
		public final int tolerance;

		public ColorRange(int[] colors, int[] tolerances) {
			this(colors[0], colors[1], colors[2], tolerances);
		}

		public ColorRange(int r, int g, int b, int[] tolerances) {
			super();
			this.red = r;
			this.green = g;
			this.blue = g;
			this.tolerance = Math.max(Math.max(tolerances[0], tolerances[1]), tolerances[2]);
		}

	}

	public <T extends BooleanRaster & BoundingBox> Image.Int decolorize(Image.Int image, T raster) {
		Pair<ColorRange, ColorRange> foregroundBackground = calculateForeAndBackgroundColor(image, raster);

		return decorolize(image, raster, foregroundBackground);
	}

	public static void drawHistorgram(BufferedImage image, int[][] data, int max) {
		Color[] c = new Color[] { Color.RED, Color.GREEN, Color.BLUE };
		Graphics2D g2D = image.createGraphics();
		int width = image.getWidth();
		int height = image.getHeight();

		Polygon[] poly = new Polygon[3];
		for (int j = 0; j < 3; j++) {
			poly[j] = new Polygon();
			g2D.setColor(c[j]);

			float stepX = (float) width / (float) data[j].length;
			float stepY = (float) height / (float) max;

			for (int i = 0; i < data[j].length; i++) {
				int x = (int) ((float) i * stepX);
				int y = (int) ((float) data[j][i] * stepY);

				poly[j].addPoint(x, height - y);
			}
			poly[j].addPoint(width, height);
			g2D.fill(poly[j]);
		}

		Area red = new Area(poly[0]);
		Area green = new Area(poly[1]);
		Area blue = new Area(poly[2]);

		red.intersect(green);
		green.intersect(blue);
		blue.intersect(new Area(poly[0]));

		g2D.setColor(new Color(255, 255, 0));
		g2D.fill(red);

		g2D.setColor(new Color(0, 255, 255));
		g2D.fill(green);

		g2D.setColor(new Color(255, 0, 255));
		g2D.fill(blue);

		g2D.setColor(Color.white);
		blue.intersect(new Area(poly[2]));
		g2D.fill(blue);

	}

	public static BufferedImage drawHistogram(int[] data) {
		BufferedImage image = new BufferedImage(256, 700, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2D = image.createGraphics();
		g2D.setColor(Color.WHITE);
		g2D.fillRect(0, 0, image.getWidth(), image.getHeight());

		int max = 0;
		for (int i = 0; i < data.length; i++) {
			max = Math.max(max, data[i]);
		}

		int width = image.getWidth();
		int height = image.getHeight();

		float stepX = (float) width / (float) data.length;
		float stepY = (float) height / (float) max;

		g2D.setColor(new Color(255, 0, 255));
		for (int i = 0; i < data.length; i++) {
			int x = (int) ((float) i * stepX);
			int y = (int) ((float) data[i] * stepY);

			g2D.drawLine(x, height, x + 1, height - y);
		}

		g2D.dispose();
		return image;
	}

	private <T extends BooleanRaster & BoundingBox> Pair<ColorRange, ColorRange> calculateForeAndBackgroundColor(Int image, T raster) {
		int fgCounter = 0;
		int bgCounter = 0;
		int[] fg = new int[3];
		int[] bg = new int[3];

		int[] rgb = new int[3];
		Rectangle bbox = raster.getBoundingBox();
		for (int y = 0; y < bbox.height; y++) {
			for (int x = 0; x < bbox.width; x++) {
				ImageUtil.toRGB(image.pixels[(y + bbox.y) * image.w + (x + bbox.x)], rgb);
				if (raster.get(x, y)) {
					fgCounter++;
					fg[0] += rgb[0];
					fg[1] += rgb[1];
					fg[2] += rgb[2];
				} else {
					bgCounter++;
					bg[0] += rgb[0];
					bg[1] += rgb[1];
					bg[2] += rgb[2];
				}
			}
		}
		int[] tolerances = new int[3];
		tolerances[0] = tolerances[1] = tolerances[2] = 0;

		ColorRange fgColor = fgCounter != 0 ? new ColorRange(fg[0] / fgCounter, fg[1] / fgCounter, fg[2] / fgCounter, tolerances)
				: ColorRange.WHITE;
		ColorRange bgColor = bgCounter != 0 ? new ColorRange(bg[0] / bgCounter, bg[1] / bgCounter, bg[2] / bgCounter, tolerances)
				: ColorRange.WHITE;
		return new Pair<ColorRange, ColorRange>(fgColor, bgColor);
	}

	private static <T extends BooleanRaster & BoundingBox> Image.Int decorolize(Image.Int image, T raster,
			Pair<ColorRange, ColorRange> foregroundBackgroundColor) {
		Rectangle bbox = raster.getBoundingBox();
		Image.Int result = new Image.Int(bbox.width, bbox.height);

		for (int x = 0; x < result.w; x++) {
			int[] before = new int[3];
			int[] after = new int[3];
			for (int y = 0; y < result.h; y++) {
				int color = image.pixels[(bbox.y + y) * image.w + (bbox.x + x)];
				ImageUtil.toRGB(color, before);

				after[0] = getInversAAColor(foregroundBackgroundColor.second.red, foregroundBackgroundColor.first.red, before[0]);
				after[1] = getInversAAColor(foregroundBackgroundColor.second.green, foregroundBackgroundColor.first.green, before[1]);
				after[2] = getInversAAColor(foregroundBackgroundColor.second.blue, foregroundBackgroundColor.first.blue, before[2]);

				result.pixels[y * result.w + x] = ImageUtil.toInt(after);
			}
		}
		return result;
	}

	private static int getInversAAColor(int bgColor, int txtColor, int pixelColor) {
		int inverse = (int) ((255f / (bgColor - txtColor)) * (pixelColor - txtColor));
		if (inverse > 255) {
			inverse = 255;
		} else if (inverse < 0) {
			inverse = 0;
		}
		return inverse;
	}

}
