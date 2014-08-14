package org.testobject.kernel.imgproc.plot;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;

/**
 *
 * @author enijkamp
 *
 */
public interface Visualizer {

	public interface HasCustomScaling {

		void setScale(float scale);

	}

	public interface Graphics {

		void setColor(Color color);

		void setStrokeWidth(int width);

		void setFont(Font font);
		
		void setAlpha(float alpha);
		
		void fillRect(int x, int y, int w, int h);

		void drawLine(int x1, int y1, int x2, int y2);

		void drawRect(int x, int y, int w, int h);

		void drawOval(int x, int y, int w, int h);

		void drawString(String text, int x, int y);

		void drawImage(BufferedImage image, int x, int y, int w, int h);
	}

	public interface Renderable {

		void render(Graphics graphics);

	}

	public interface Figure {

		void setScale(float scale);

		void addLayer(Renderable renderable);
	}

	public interface MouseListener {
		void move(int x, int y);
	}
	
	public interface Window {

		Figure getFigure();

		void setTitle(String title);
		
		void setMouseListener(MouseListener listener);
		
		void repaint();

		void close();
	}
	
	Figure figure(Renderable... renderable);

	Window show(String title, Figure figure);

	BufferedImage draw(Figure figure);
}