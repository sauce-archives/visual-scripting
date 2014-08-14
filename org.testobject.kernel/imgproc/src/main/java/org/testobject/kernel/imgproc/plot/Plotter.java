package org.testobject.kernel.imgproc.plot;

import java.awt.Color;

public interface Plotter {

	interface Plot extends Visualizer.Renderable {

		void setTitle(String title);

		void addLine(String name, Color color, double... xy);

		void addLine(String name, Color color, double[] x, double[] y);

		void addBars(String name, Color color, double[][] bins);
	}

	Plot plot(int width, int height);

	Plot plot(int width, int height, String title);
}