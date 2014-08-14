package org.testobject.kernel.ocr.freetype;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.kernel.ocr.FontRenderer;

/**
 * 
 * @author enijkamp
 * 
 */
public class FreeTypeGrayscaleFontRenderer implements FontRenderer {
	
	public static final Log log = LogFactory.getLog(FreeTypeGrayscaleFontRenderer.class);
	
	private final FT2Library freetype;

	private final int baselineDpi;
	private final int targetDpi;
	private final int filter;
	
	public FreeTypeGrayscaleFontRenderer(int baselineDpi, int targetDpi, final int filter) {
		this(FreeTypeLoader.loadRewrite().freetype, baselineDpi, targetDpi, filter);
	}

	public FreeTypeGrayscaleFontRenderer(FT2Library freetype, int baselineDpi, int targetDpi, int filter) {
		check(filter);
		this.freetype = freetype;
		this.baselineDpi = baselineDpi;
		this.targetDpi = targetDpi;
		this.filter = filter;
	}

	private void check(int filter) {
		if(filter != FT2Library.FT_LCD_FILTER_LIGHT) {
			// 'FT_LCD_FILTER_DEFAULT' renders chars with have much larger size (en)
			log.warn("freetype lcd filter is not set to 'FT_LCD_FILTER_LIGHT' - is this correct?");
			throw new IllegalStateException();
		}
	}
	
	// FIXME replace BufferedImage by Image.Int (en)
	@Override
	public BufferedImage drawChar(File font, float size, char chr) {
		try (FreeTypeFont freeFont = FreeTypeFont.create(freetype, font)) {
			
			// set rendering flags
			freeFont.setCharSize(0, toScaledFontSize(size), baselineDpi, baselineDpi);
			freeFont.setLCDFilter(filter);

			final int loadFlags = filter;
			final int renderFlags = FT2Library.FT_RENDER_MODE_LIGHT;
			FreeTypeGlyphInfo info = freeFont.loadCodePoint(chr, loadFlags, renderFlags);

			// draw char
			BufferedImage buffer = new BufferedImage(info.getWidth(), info.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

			// draw background
			{
				Graphics2D g = buffer.createGraphics();
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
				g.dispose();
			}

			// glyph
			freeFont.copyGlpyhToBufferedImageGray(buffer, 0, 0);

			// FIXME hacky (en)
			BufferedImage rgb = new BufferedImage(buffer.getWidth(), buffer.getHeight(), BufferedImage.TYPE_INT_RGB);
			{
				Graphics g = rgb.getGraphics();
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
				g.drawImage(buffer, 0, 0, null);
				g.dispose();
			}

			// resize
			return ImageUtil.crop(rgb, new Rectangle(0, 0, info.getWidth(), info.getHeight()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private float toScaledFontSize(float fontSize) {
		return fontSize / ((float) targetDpi / (float) baselineDpi);
	}
}
