package org.testobject.kernel.imgproc.plot;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.Image.Int;

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

	public static Visualizer.Renderable toRenderable(final BufferedImage image) {
		return new Visualizer.Renderable() {
			@Override
			public void render(Visualizer.Graphics g) {
				g.drawImage(image, 0, 0, image.getWidth(), image.getHeight());
			}
		};
	}

	public static Visualizer.Window show(Image.Int image) {
		return show("Image.Int", image);
	}

	public static Visualizer.Window show(String title, Image.Int image) {
		return show(title, image, 1f);
	}

	public static Visualizer.Window show(String title, Int image, float scale) {
		final Visualizer vis = new SwingVisualizer();
		final Visualizer.Figure fig = vis.figure(toRenderable(ImageUtil.toBufferedImage(image)));
		fig.setScale(scale);
		return vis.show(title, fig);
	}

	public static Visualizer.Window show(String title, Visualizer.Renderable... renderable) {
		final Visualizer vis = new SwingVisualizer();
		final Visualizer.Figure fig = vis.figure(renderable);
		fig.setScale(1f);
		return vis.show(title, fig);
	}

	public static Visualizer.Window show(String title, float scale, Visualizer.Renderable... renderable) {
		final Visualizer vis = new SwingVisualizer();
		final Visualizer.Figure fig = vis.figure(renderable);
		fig.setScale(scale);
		return vis.show(title, fig);
	}

	public static Visualizer.Window plotHistogram(String title, int[] histogram) {
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
