package org.testobject.commons.tools.plot;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.testobject.commons.util.image.ImageUtil;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.tools.plot.Visualizer.Figure;
import org.testobject.commons.tools.plot.Visualizer.Graphics;
import org.testobject.commons.tools.plot.Visualizer.Renderable;
import org.testobject.commons.tools.plot.Visualizer.Window;

/**
 * 
 * @author enijkamp
 * 
 */
public class VisualizerUtil {

	public static void show(String title, final BufferedImage image, float scale) {
		show(title, scale, toRenderable(image));
	}

	public static void show(String title, final BufferedImage image) {
		show(title, 1f, toRenderable(image));
	}

	public static Renderable toRenderable(final BufferedImage image) {
		return new Renderable() {
			@Override
			public void render(Graphics g) {
				g.drawImage(image, 0, 0, image.getWidth(), image.getHeight());
			}
		};
	}

	public static Window show(Image.Int image) {
		return show("image", image);
	}

	public static Window show(String title, Image.Int image) {
		return show(title, image, 1f);
	}

	public static Window show(String title, Image.Int image, float scale) {
		final Visualizer vis = new SwingVisualizer();
		final Figure fig = vis.figure(toRenderable(ImageUtil.Convert.toBufferedImage(image)));
		fig.setScale(scale);
		return vis.show(title, fig);
	}

	public static Window show(String title, Renderable... renderable) {
		final Visualizer vis = new SwingVisualizer();
		final Figure fig = vis.figure(renderable);
		fig.setScale(1f);
		return vis.show(title, fig);
	}

	public static Window show(String title, float scale, Renderable... renderable) {
		final Visualizer vis = new SwingVisualizer();
		final Figure fig = vis.figure(renderable);
		fig.setScale(scale);
		return vis.show(title, fig);
	}

	public static Window plotHistogram(String title, int[] histogram) {
		final Visualizer vis = new SwingVisualizer();
		final Plotter.Plot plot = new JFreeChartPlotter().plot(800, 500);
		final double[][] bins = new double[1][histogram.length];
		for (int i = 0; i < histogram.length; i++) {
			bins[0][i] = histogram[i];
		}
		plot.addBars(title, Color.RED, bins);
		final Visualizer.Figure figure = vis.figure(plot);
		return vis.show(title, figure);
	}

}
