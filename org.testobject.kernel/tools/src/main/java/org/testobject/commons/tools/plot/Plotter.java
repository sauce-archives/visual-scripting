package org.testobject.commons.tools.plot;

import java.awt.Color;

import org.testobject.commons.tools.plot.Visualizer.Renderable;

public interface Plotter {

	interface Plot extends Renderable {

		void setTitle(String title);

		void addLine(String name, Color color, double... xy);

		void addLine(String name, Color color, double[] x, double[] y);

		void addBars(String name, Color color, double[][] bins);
	}

	Plot plot(int width, int height);

	Plot plot(int width, int height, String title);
}