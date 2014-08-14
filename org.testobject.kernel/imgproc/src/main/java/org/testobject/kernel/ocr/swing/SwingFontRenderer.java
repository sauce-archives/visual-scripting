package org.testobject.kernel.ocr.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JComponent;

import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.kernel.ocr.FontRenderer;

/**
 * 
 * @author enijkamp
 * 
 */
public class SwingFontRenderer implements FontRenderer {

	@Override
	public BufferedImage drawChar(File font, float size, char chr) {
		return renderString(chr + "", new Font(font.getName(), Font.PLAIN, (int) size), Color.white, Color.black);
	}

	public static BufferedImage renderString(String value, Font font) {
		return renderString(value, font, Color.white, Color.black);
	}

	private static BufferedImage renderString(String string, Font font, Color bg, Color fg) {
		
		Rectangle2D stringBounds = getStringBounds(font, string);
		BufferedImage image = new BufferedImage((int)(stringBounds.getWidth()), (int)(stringBounds.getHeight()),
				BufferedImage.TYPE_INT_RGB);

		{
			Graphics graphics = image.createGraphics();
			graphics.setColor(bg);
			graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
			enableAntiAliasing(graphics);
			graphics.setFont(font);
			graphics.setColor(fg);
			graphics.drawString(string, 1, font.getSize());
		}

		return ImageUtil.trim(image, Color.white.getRGB());
	}
	
	private static Rectangle2D getStringBounds(Font font, String string) {
		BufferedImage dummy = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = dummy.createGraphics();
		enableAntiAliasing(graphics);
		Rectangle2D stringBounds = font.getStringBounds(string, graphics.getFontRenderContext());
		graphics.dispose();
		return stringBounds;
	}

	private static final void enableAntiAliasing(Graphics g) {
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
	}

	private static BufferedImage renderComponentToImage(JComponent component) {
		// Create the image
		BufferedImage image = createCompatibleImage(component.getWidth(),
				component.getHeight());

		// Render the component onto the image
		Graphics graphics = image.createGraphics();
		// component.update(graphics);
		component.paint(graphics);
		graphics.dispose();
		return image;
	}

	private static BufferedImage createCompatibleImage(int width, int height) {
		GraphicsConfiguration configuration = GraphicsEnvironment
				.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();
		return configuration.createCompatibleImage(width, height,
				Transparency.TRANSLUCENT);
	}
}
