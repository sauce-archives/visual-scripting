package org.testobject.kernel.ocr;

import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Ignore;
import org.junit.Test;
import org.testobject.kernel.imgproc.blob.BoundingBox;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.kernel.ocr.freetype.FT2Library;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.file.FileUtil;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.Image.Int;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.BooleanRaster;
import org.testobject.kernel.imgproc.blob.GraphBlobBuilder;
import org.testobject.kernel.imgproc.plot.VisualizerUtil;
import org.testobject.kernel.ocr.Decolorizer;
import org.testobject.kernel.ocr.HistogramFeature;
import org.testobject.kernel.ocr.freetype.FreeTypeGrayscaleFontRenderer;
import org.testobject.kernel.ocr.MaskClusterer;

public class DecolorizerTest {

	private static class SimpleRaster implements BooleanRaster, BoundingBox {

		private final int width;
		private final int height;
		private final Rectangle foreground;

		SimpleRaster(int width, int height, Rectangle foreground) {
			this.width = width;
			this.height = height;
			this.foreground = foreground;
		}

		@Override
		public void set(int x, int y, boolean what) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean get(int x, int y) {
			return foreground.contains(x, y);
		}

		@Override
		public Rectangle getBoundingBox() {
			return new Rectangle(width, height);
		}

		@Override
		public Dimension getSize() {
			return new Dimension(width, height);
		}

	}

	private static final boolean DEBUG = Debug.toDebugMode(true);

	@Test
	public void decolorizeRedAndGreen() throws IOException {
		SimpleRaster raster = new SimpleRaster(100, 100, new Rectangle(20, 20, 60, 60));
		BufferedImage image = draw2Rects(new Color(255, 0, 0), new Color(150, 150, 255), raster);

		// VisualizerUtil.show("colored image", image);

		Image.Int result = new Decolorizer().decolorize(ImageUtil.toImage(image), raster);

		// VisualizerUtil.show("decolored image", result);

		for (int y = 0; y < result.h; y++) {
			for (int x = 0; x < result.w; x++) {
				assertTrue(result.get(x, y) == Color.WHITE.getRGB() || result.get(x, y) == Color.BLACK.getRGB());
			}
		}

	}

	private static BufferedImage draw2Rects(Color bgColor, Color fgColor, SimpleRaster fg) {
		BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
		{
			Graphics g = image.getGraphics();
			g.setColor(bgColor);
			g.fillRect(0, 0, fg.width, fg.height);
			g.setColor(fgColor);
			g.fillRect(fg.foreground.x, fg.foreground.y, fg.foreground.width, fg.foreground.height);
			g.dispose();
		}
		return image;
	}

	@Test
	public void decolorizeAndMeasureDistance1233() throws IOException {
		final File font = FileUtil.toFileFromSystem("android/4_0_3/fonts/sans-serif/Roboto-Regular.ttf");
		Image.Int trainImage = ImageUtil.toImage(new FreeTypeGrayscaleFontRenderer(160, 240, FT2Library.FT_LCD_FILTER_LIGHT).drawChar(font,
				16f, '3'));
		Image.Int testImage = ImageUtil.read("android/4_0_3/classifier/text/positives/system/1233.png");

		Blob[] blobs = new GraphBlobBuilder(testImage.w, testImage.h).build(testImage);

		Blob blob = blobs[3];

		Decolorizer decolorizer = new Decolorizer();

		Int trainDecolorized = decolorizer.decolorize(trainImage, MaskClusterer.toRaster(trainImage));
		Int testDecolorized = decolorizer.decolorize(testImage, blob);

		double[][] train1Histogram = HistogramFeature.computeHistogram(ImageUtil.toImageByte(trainDecolorized), 7);
		double[][] trainHistogram = HistogramFeature.computeHistogram(ImageUtil.toImageByte(testDecolorized), 7);

		// HistogramFeature.plotHistograms("test", trainHistogram);
		// HistogramFeature.plotHistograms("train", train1Histogram);

		double distance = HistogramFeature.distance(train1Histogram, trainHistogram);

		assertTrue(distance < 0.8);
	}

	@Test
	public void decolorize1233() throws IOException {
		Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/system/1233.png");
		Blob blob = new GraphBlobBuilder(image.w, image.h).build(image)[3];
		Int decolorized = new Decolorizer().decolorize(image, blob);

		if (DEBUG) {
			VisualizerUtil.show(decolorized);
		}

		double white = countColor(decolorized, new Color(254, 255, 255).getRGB());
		double black = countColor(decolorized, Color.BLACK.getRGB());

		if (DEBUG) {
			System.out.println(white / (blob.bbox.width * blob.bbox.height));
			System.out.println(black / (blob.bbox.width * blob.bbox.height));
		}

		assertTrue(white / (blob.bbox.width * blob.bbox.height) > 0.4d);
		assertTrue(black / (blob.bbox.width * blob.bbox.height) > 0.2d);
	}

	@Test
	public void decolorizeTweet() throws IOException {
		Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/twitter/Tweet.png");
		Blob blob = new GraphBlobBuilder(image.w, image.h).build(image)[3];
		Int decolorized = new Decolorizer().decolorize(image, blob);

		if (DEBUG) {
			VisualizerUtil.show(decolorized);
		}

		Color color = new Color(-65537);
		System.out.println(color.getAlpha());
		System.out.println(color.getRed());
		System.out.println(color.getGreen());
		System.out.println(color.getBlue());

		Color color1 = new Color(-866304);
		System.out.println(color1.getAlpha());
		System.out.println(color1.getRed());
		System.out.println(color1.getGreen());
		System.out.println(color1.getBlue());

		double black = countColor(decolorized, Color.BLACK.getRGB());

		assertTrue(black / (blob.bbox.width * blob.bbox.height) > 0.37d);
	}

	private int countColor(Int image, int color) {
		int counter = 0;
		for (int y = 0; y < image.h; y++) {
			for (int x = 0; x < image.w; x++) {
				if (color == image.get(x, y)) {
					counter++;
				}
			}
		}
		return counter;
	}

	@Test
	@Ignore
	public void test() throws IOException {
		BufferedImage bufferedImage = ImageIO.read(FileUtil
				.readFileFromSystem("android/4_0_3/classifier/text/positives/twitter/Tweet.png"));
		Int image = ImageUtil.toImage(bufferedImage);

		Blob[] blobs = new GraphBlobBuilder(image.w, image.h).build(image);

		Blob blob = blobs[3];

		VisualizerUtil.show(ImageUtil.crop(image, blob.bbox));

		Int decolorizedImage = new Decolorizer().decolorize(image, blob);

		VisualizerUtil.show(ImageUtil.toGrayscaleImage(decolorizedImage));

		System.in.read();
	}
}
