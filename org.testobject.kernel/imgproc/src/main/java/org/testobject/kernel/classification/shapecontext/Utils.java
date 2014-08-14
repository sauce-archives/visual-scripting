package org.testobject.kernel.classification.shapecontext;

import java.awt.Color;

import org.testobject.kernel.imgproc.plot.Visualizer;
import org.testobject.kernel.imgproc.plot.VisualizerUtil;
import org.testobject.commons.math.algebra.Point;

public class Utils {

	private static class ContourRenderer implements Visualizer.Renderable {

		private static final int SCALE = 32;

		private final String description;
		private final Point.Double[] contour1;
		private final Point.Double[] contour2;
		private final int[][] assignments;

		public ContourRenderer(final String description, final Point.Double[] contour1, final Point.Double[] contour2,
		        final int[][] assignments) {
			this.description = description;
			this.contour1 = contour1;
			this.contour2 = contour2;
			this.assignments = assignments;
		}

		public void render(Visualizer.Graphics g) {
			if (description != null)
				g.drawString(description, 0, 10);
			if (contour1 != null)
				renderContour(g, contour1, 0, 12);
			if (contour2 != null)
				renderContour(g, contour2, 16 * SCALE, 12);
			if (assignments != null)
				renderAssignments(g, assignments, contour1, contour2, 0, 12);
		}

		private static void renderContour(Visualizer.Graphics g, Point.Double[] contour, int xOffset,
		        int yOffset) {
			for (Point.Double point : contour) {
				renderPoint(g, point, xOffset, yOffset);
			}
		}

		private static void renderPoint(Visualizer.Graphics g, Point.Double p, int xOffset, int yOffset) {
			g.setColor(Color.green);
			g.fillRect(toInt(p.x * SCALE + xOffset), toInt(p.y * SCALE + yOffset), SCALE, SCALE);
		}

		private static void renderAssignments(Visualizer.Graphics g, int[][] assignments,
		        Point.Double[] contour1, Point.Double[] contour2, int xOffset, int yOffset) {
			g.setColor(Color.blue);
			for (int[] assignment : assignments) {
				if (assignment != null) {
					renderAssignment(g, contour1[assignment[0]], contour2[assignment[1]], xOffset, yOffset);
				}
			}
		}

		private static void renderAssignment(Visualizer.Graphics g, Point.Double point1,
		        Point.Double point2,
		        int xOffset, int yOffset) {
			g.drawLine(toInt(point1.x * SCALE + SCALE / 2),
			        toInt(point1.y * SCALE + yOffset + SCALE / 2),
			        toInt(point2.x * SCALE + 16 * SCALE + SCALE / 2),
			        toInt(point2.y * SCALE + yOffset + SCALE / 2));
		}
	}

	public static void show(String description, Point.Double[] contour1, Point.Double[] contour2, int[][] assignments) {
		VisualizerUtil.show(description, new ContourRenderer(description, contour1, contour2, assignments));
	}

	private static final int toInt(double value) {
		return (int) value;
	}

}
