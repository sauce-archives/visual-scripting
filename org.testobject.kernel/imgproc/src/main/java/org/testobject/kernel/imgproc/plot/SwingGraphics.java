package org.testobject.kernel.imgproc.plot;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;

/**
 * 
 * @author enijkamp
 *
 */
public class SwingGraphics implements Visualizer.Graphics {

	private final java.awt.Graphics2D graphics;
	private final float scale;

	public SwingGraphics(java.awt.Graphics2D graphics, float scale) {
		this.graphics = graphics;
		this.scale = scale;
	}

	private int scale(int value) {
		return (int) (value * this.scale);
	}

	@Override
	public void setColor(Color color) {
		this.graphics.setColor(color);
	}

	@Override
	public void setStrokeWidth(int width) {
		// TODO
	}

	@Override
	public void setFont(Font font) {
		this.graphics.setFont(font);
	}

	@Override
	public void fillRect(int x, int y, int w, int h) {
		this.graphics.fillRect(scale(x), scale(y), scale(w), scale(h));
	}

	@Override
	public void drawRect(int x, int y, int w, int h) {
		this.graphics.drawRect(scale(x), scale(y), scale(w), scale(h));
	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		this.graphics.drawLine(scale(x1), scale(y1), scale(x2), scale(y2));
	}

	@Override
	public void drawString(String text, int x, int y) {
		this.graphics.drawString(text, scale(x), scale(y));

	}

	@Override
	public void drawImage(BufferedImage image, int x, int y, int w, int h) {
		this.graphics.drawImage(image, scale(x), scale(y), scale(w), scale(h), null);
	}

	@Override
	public void drawOval(int x, int y, int w, int h) {
		this.graphics.drawOval(scale(x), scale(y), scale(w), scale(h));
	}

	@Override
	public void setAlpha(float alpha) {
		this.graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));		
	}
}