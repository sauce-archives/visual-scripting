package org.testobject.kernel.imgproc.plot;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

public class JFreeChartPlotter implements Plotter {

	private class JFreeChartPlot implements Plot, Visualizer.HasCustomScaling {

		private XYSeriesCollection dataLines;
		private CategoryDataset dataBars;

		private final String title;
		private JFreeChart chart;
		private final int width, height;
		private float scale = 1f;

		public JFreeChartPlot(int width, int height, String title) {
			this.title = title;
			this.width = width;
			this.height = height;
		}

		private JFreeChart createLineChart(String title, XYSeriesCollection data) {
			// plot
			final NumberAxis xAxis = new NumberAxis("X");
			xAxis.setAutoRangeIncludesZero(false);
			final NumberAxis yAxis = new NumberAxis("Y");
			yAxis.setAutoRangeIncludesZero(false);

			final XYSplineRenderer renderer = new XYSplineRenderer();
			final XYPlot plot = new XYPlot(data, xAxis, yAxis, renderer);
			{
				plot.setBackgroundPaint(Color.lightGray);
				plot.setDomainGridlinePaint(Color.white);
				plot.setRangeGridlinePaint(Color.white);
				plot.setAxisOffset(new RectangleInsets(4, 4, 4, 4));
			}

			// chart
			return new JFreeChart(title, plot);
		}

		private JFreeChart createBarChart(String title, CategoryDataset data) {
			return ChartFactory.createBarChart(
					title, "X", "Y", data,
					PlotOrientation.VERTICAL, true, true, false);
		}

		@Override
		public void setTitle(String title) {
			this.chart.setTitle(title);
		}

		@Override
		public void addLine(String name, Color color, double... xy) {
			// TODO color
			final XYSeries series = new XYSeries(name);
			for (int i = 0; i < xy.length; i += 2) {
				series.add(xy[i], xy[i + 1]);
			}
			this.dataLines.addSeries(series);

			if (this.chart == null) {
				this.chart = createLineChart(this.title, this.dataLines);
			}
		}

		@Override
		public void addLine(String name, Color color, double[] x, double[] y) {
			// TODO color
			assert x.length == y.length;

			final XYSeries series = new XYSeries(name);
			for (int i = 0; i < x.length; i++) {
				series.add(x[i], y[i]);
			}
			this.dataLines.addSeries(series);

			if (this.chart == null) {
				this.chart = createLineChart(this.title, this.dataLines);
			}
		}

		@Override
		public void addBars(String name, Color color, double[][] bins) {
			this.dataBars = DatasetUtilities.createCategoryDataset("", "", bins);
			this.chart = createBarChart(this.title, this.dataBars);
		}

		@Override
		public void setScale(float scale) {
			this.scale = scale;
		}

		@Override
		public void render(Visualizer.Graphics graphics) {
			graphics.drawImage(this.chart.createBufferedImage(scale(this.width), scale(this.height)), 0, 0, scale(this.width), scale(this.height));
		}

		private int scale(int value) {
			return (int) (value * this.scale);
		}
	}
	

	@Override
	public Plot plot(int width, int height) {
		return new JFreeChartPlot(width, height, "");
	}

	@Override
	public Plot plot(int width, int height, String title) {
		return new JFreeChartPlot(width, height, title);
	}
}