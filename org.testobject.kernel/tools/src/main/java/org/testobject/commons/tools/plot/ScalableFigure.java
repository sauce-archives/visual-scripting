package org.testobject.commons.tools.plot;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

/**
 * 
 * @author enijkamp
 *
 */
@SuppressWarnings("serial")
public class ScalableFigure extends JPanel implements Visualizer.Figure {
	
	// state
	public List<Visualizer.Renderable> layers = new LinkedList<>();
	public float scale = 1f;
	
	public ScalableFigure(Visualizer.Renderable... renderable) {
		this.layers.addAll(Arrays.asList(renderable));
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
		this.repaint(50);
	}
	
	@Override
	public void paintComponent(java.awt.Graphics graphics) {
		drawToAwt((java.awt.Graphics2D) graphics);
	}

	@Override
	public void addLayer(Visualizer.Renderable renderable) {
		layers.add(renderable);	
	}
	
	@Override
	public void removeLayers() {
		layers.clear();
	}
	
	public void drawToAwt(java.awt.Graphics2D graphicsAwt) {
		for (Visualizer.Renderable renderable : this.layers) {
			renderable.render(new SwingGraphics(graphicsAwt, this.scale));
		}
	}
	
	public BufferedImage drawToBuffer() {
		// determine size
		final Rectangle size = getSizeOfFigure();

		// buffer
		final BufferedImage rgb = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
		final java.awt.Graphics2D graphicsImage = (Graphics2D) rgb.getGraphics();

		try {
			drawToAwt(graphicsImage);
			return rgb;
		} finally {
			graphicsImage.dispose();
		}
	}
	
	public Rectangle getSizeOfFigure() {
		// mock for graphics
		final BufferedImage mockBuffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

		// bounding box
		final Rectangle box = new Rectangle(0, 0, 0, 0);

		// draw
		for (Visualizer.Renderable renderable : this.layers) {
			final GetSizeOfFigure mockGraphics = new GetSizeOfFigure(mockBuffer.getGraphics(), this.scale);
			renderable.render(mockGraphics);
			box.width = mockGraphics.getWidth() > box.getWidth() ? mockGraphics.getWidth() : (int) box.getWidth();
			box.height = mockGraphics.getHeight() > box.getHeight() ? mockGraphics.getHeight() : (int) box.getHeight();
		}

		return box;
	}
	
	public void resize() {
		final Rectangle size = getSizeOfFigure();
		this.setPreferredSize(new Dimension((int) size.getX() + (int) size.getWidth(), (int) size.getY() + (int) size.getHeight()));
	}
}