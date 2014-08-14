package org.testobject.kernel.ocr;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.kernel.ocr.cairo.CairoFontRenderer;
import org.testobject.kernel.ocr.freetype.FT2Library;
import org.testobject.kernel.ocr.freetype.FreeTypeColorFontRenderer;
import org.testobject.commons.util.file.FileUtil;
import org.testobject.kernel.imgproc.plot.VisualizerUtil;
import org.testobject.kernel.ocr.freetype.FreeTypeGrayscaleFontRenderer;
import org.testobject.kernel.ocr.FontRenderer;

public class FontRendererMain {

	private static final File FONT = FileUtil.toFileFromSystem("android/4_0_3/fonts/sans-serif/DroidSans.ttf");

	public static void main(String... args) throws Exception {
		// testRendering();
		// testCairoGray();
		testFreetypeGray();

		VisualizerUtil.show(ImageUtil.read("android/4_0_3/classifier/text/positives/twitter/Cancel.png"));
	}

	public static void testRendering() throws Exception {
		final int fontSize = 10;

		// training (cairo)
		{
			CairoFontRenderer renderer = new CairoFontRenderer(FONT, fontSize, 96, 72);
			BufferedImage image = ImageUtil.trim(renderer.drawString(String.valueOf("a")), Color.WHITE.getRGB());
			VisualizerUtil.show("a (cairo)", image, 20f);
		}

		// training (freetype)
		{
			final int filter = FT2Library.FT_LCD_FILTER_LIGHT;
			FontRenderer renderer = new FreeTypeColorFontRenderer(96, 72, filter);
			BufferedImage image = ImageUtil.trim(renderer.drawChar(FONT, fontSize, 'a'), Color.WHITE.getRGB());
			VisualizerUtil.show("a (freetype)", image, 20f);
		}
	}

	public static void testCairoGray() throws IOException {
		{
			CairoFontRenderer renderer = new CairoFontRenderer(FONT, 24, 96, 72, CairoFontRenderer.Settings.GRAY);
			BufferedImage image = ImageUtil.trim(renderer.drawString(String.valueOf("s")), Color.WHITE.getRGB());
			VisualizerUtil.show("gray (cairo)", image, 20f);
		}
	}

	public static void testFreetype() throws IOException {
		{
			FreeTypeColorFontRenderer renderer = new FreeTypeColorFontRenderer(160, 240, FT2Library.FT_LCD_FILTER_LIGHT);
			BufferedImage image = renderer.drawChar(getFont("Roboto-Bold.ttf"), 12, 'n');
			VisualizerUtil.show("gray (freetype)", image, 20f);
		}
	}

	public static void testFreetypeGray() throws IOException {
		{
			FreeTypeGrayscaleFontRenderer renderer = new FreeTypeGrayscaleFontRenderer(160, 240, FT2Library.FT_LCD_FILTER_LIGHT);
			BufferedImage image = renderer.drawChar(getFont("Roboto-Bold.ttf"), 12, 'n');
			VisualizerUtil.show("gray (freetype)", image, 20f);
		}
	}

	private static final File getFont(String font) {
		return FileUtil.toFileFromSystem("android/4_0_3/fonts/sans-serif/" + font);
	}

}