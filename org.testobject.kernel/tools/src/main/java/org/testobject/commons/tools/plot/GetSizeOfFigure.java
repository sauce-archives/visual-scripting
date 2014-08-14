package org.testobject.commons.tools.plot;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;

import org.testobject.commons.tools.plot.Visualizer.Graphics;

/**
 * 
 * @author enijkamp
 *
 */
public class GetSizeOfFigure implements Graphics {

	private int height = 0, width = 0;

	private final java.awt.Graphics graphics;
	private final float scale;

	public GetSizeOfFigure(java.awt.Graphics graphics, float scale) {
		this.graphics = graphics;
		this.scale = scale;
	}

	public int getHeight() {
		return this.height;
	}

	public int getWidth() {
		return this.width;
	}

	private int scale(int value) {
		return (int) (value * this.scale);
	}

	private void checkBounds(int x, int y, int w, int h) {
		if ((scale(x) + scale(w)) > this.width) {
			this.width = (scale(x) + scale(w));
		}
		if ((scale(y) + scale(h)) > this.height) {
			this.height = (scale(y) + scale(h));
		}
	}

	@Override
	public void setColor(Color color) {

	}

	@Override
	public void setFont(Font font) {
		this.graphics.setFont(font);
	}

	@Override
	public void fillRect(int x, int y, int w, int h) {
		checkBounds(x, y, w, h);
	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		if (Math.max(x1, x2) > this.width) {
			this.width = Math.max(x1, x2);
		}
		if (Math.max(y1, y2) > this.height) {
			this.height = Math.max(y1, y2);
		}
	}

	@Override
	public void drawRect(int x, int y, int w, int h) {
		checkBounds(x, y, w, h);
	}

	@Override
	public void drawOval(int x, int y, int w, int h) {
		checkBounds(x, y, w, h);
	}

	@Override
	public void drawString(String text, int x, int y) {
		if ((scale(x) + this.graphics.getFontMetrics().getStringBounds(text, this.graphics).getWidth()) > this.width) {
			this.width = (int) (scale(x) + this.graphics.getFontMetrics().getStringBounds(text, this.graphics).getWidth());
		}
		if ((scale(y) + this.graphics.getFont().getSize()) > this.height) {
			this.height = scale(y);
		}
	}

	@Override
	public void drawImage(BufferedImage image, int x, int y, int w, int h) {
		checkBounds(x, y, w, h);
	}

	@Override
	public void setAlpha(float alpha) {
		
	}
}