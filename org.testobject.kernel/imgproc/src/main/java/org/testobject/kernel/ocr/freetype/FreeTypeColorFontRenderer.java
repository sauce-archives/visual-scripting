package org.testobject.kernel.ocr.freetype;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.kernel.ocr.FontRenderer;

/**
 * 
 * @author enijkamp
 * 
 */
public class FreeTypeColorFontRenderer implements FontRenderer {

	private FT2Library freetype;
	private final int baselineDpi;
	private final int targetDpi;
	private final int filter;
	
	public FreeTypeColorFontRenderer(int baselineDpi, int targetDpi, final int filter) {
		this(FreeTypeLoader.loadRewrite().freetype, baselineDpi, targetDpi, filter);
	}

	public FreeTypeColorFontRenderer(FT2Library freetype, int baselineDpi, int targetDpi, final int filter) {
		this.freetype = freetype;
		this.baselineDpi = baselineDpi;
		this.targetDpi = targetDpi;
		this.filter = filter;
	}

	// FIXME replace BufferedImage by Image.Int (en)
	public BufferedImage drawChar(File font, float size, char chr) {

		try (FreeTypeFont freeFont = FreeTypeFont.create(freetype, font)) {
			
			// set rendering flags
			freeFont.setCharSize(0, toScaledFontSize(size), baselineDpi, baselineDpi);
			freeFont.setLCDFilter(filter);

			final int loadFlags = filter;
			final int renderFlags = FT2Library.FT_RENDER_MODE_LCD;
			FreeTypeGlyphInfo info = freeFont.loadCodePoint(chr, loadFlags, renderFlags);

			// draw char
			BufferedImage buffer = new BufferedImage(info.getWidth(), info.getHeight(), BufferedImage.TYPE_INT_ARGB);

			// draw background
			Graphics2D g = buffer.createGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
			g.dispose();

			// glyph
			freeFont.copyGlpyhToBufferedImageColor(buffer, 0, 0);

			// resize
			return ImageUtil.crop(buffer, new Rectangle(0, 0, info.getWidth() / 3, info.getHeight()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private float toScaledFontSize(float fontSize) {
		return fontSize / ((float) targetDpi / (float) baselineDpi);
	}
}
