package org.testobject.kernel.classification.procedural;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.classification.contours.Contour;
import org.testobject.commons.math.algebra.Point;

/**
 * 
 * @author enijkamp
 * 
 */
public class ProceduralRenderer {

	public static interface Shape {

		double getHeight();

		double getWidth();

		double getX();

		double getY();

		Fill getFill();
	}

	public static interface Fill {

	}

	public static interface Stroke {

	}

	public static class LinearGradient implements Fill {
		public final Color from, to;

		public LinearGradient(Color from, Color to) {
			this.from = from;
			this.to = to;
		}
	}

	public static class BasicFill implements Fill {
		public final Color color;

		public BasicFill(Color color) {
			this.color = color;
		}
	}

	public static class BasicStroke implements Stroke {
		public final Color color;

		public BasicStroke(Color color) {
			this.color = color;
		}
	}

	public static class Polygon implements Shape {

		public final double x, y, w, h;
		public final Contour path;
		public final Stroke stroke;
		public final Fill fill;

		Polygon(Contour path, Stroke stroke, Fill fill) {
			this.path = path;
			this.stroke = stroke;
			this.fill = fill;

			double x = Integer.MAX_VALUE, y = Integer.MAX_VALUE;
			double w = 0, h = 0;
			for (int i = 0; i < path.npoints; i++) {
				x = x > path.xpoints[i] ? path.xpoints[i] : x;
				y = y > path.ypoints[i] ? path.ypoints[i] : y;
				w = w < path.xpoints[i] ? path.xpoints[i] : w;
				h = h < path.ypoints[i] ? path.ypoints[i] : h;
			}

			this.x = x;
			this.y = y;
			this.w = w + 1;
			this.h = h + 1;
		}

		public double getHeight() {
			return h;
		}

		public double getWidth() {
			return w;
		}

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}

		public Fill getFill() {
			return fill;
		}
	}

	public static class Rect implements Shape {

		public final double x, y;
		public final double w, h;
		public final double rx, ry;
		public final Stroke stroke;
		public final Fill fill;
		public final String text;

		public Rect(double x, double y, double w, double h, double rx, double ry, Stroke stroke, Fill fill, String text) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.rx = rx;
			this.ry = ry;
			this.stroke = stroke;
			this.fill = fill;
			this.text = text;
		}

		public double getHeight() {
			return h;
		}

		public double getWidth() {
			return w;
		}

		public double getX() {
			return 0;
		}

		public double getY() {
			return 0;
		}

		public Fill getFill() {
			return fill;
		}
	}

	public static class Procedure {

		public double w, h;
		public List<Shape> shapes = new LinkedList<Shape>();

	}

	public static List<Contour> getContours(List<Shape> shapes) {
		List<Contour> contours = new LinkedList<Contour>();
		for (Shape shape : shapes) {
			contours.add(getContour(shape));
		}
		return contours;
	}

	public static Contour getContour(Shape shape) {
		if (shape instanceof Rect) {
			Rect rect = (Rect) shape;
			PathIterator path = new RoundRectangle2D.Double(0, 0, rect.w, rect.h, rect.rx, rect.ry).getPathIterator(null);
			List<org.testobject.commons.math.algebra.Point.Double> contour = new ContourExtractor(path).extractContour();
			return new Contour(contour);
		} else if (shape instanceof Polygon) {
			Polygon polygon = (Polygon) shape;
			return polygon.path;
		}
		throw new IllegalStateException();
	}

	public BufferedImage render(Procedure proc) {
		BufferedImage image = new BufferedImage((int) proc.w, (int) proc.h, BufferedImage.TYPE_4BYTE_ABGR);
		for (Shape shape : proc.shapes) {
			if (shape instanceof Rect) {
				Rect rect = (Rect) shape;
				render(image, rect);
			} else if (shape instanceof Polygon) {
				Polygon polygon = (Polygon) shape;
				render(image, polygon);
			}
		}
		return image;
	}

	public Image.Int render(int w, int h, List<Procedure> procs) {
		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		for (Procedure proc : procs) {
			for (Shape shape : proc.shapes) {
				if (shape instanceof Rect) {
					Rect rect = (Rect) shape;
					render(image, rect);
				} else if (shape instanceof Polygon) {
					Polygon polygon = (Polygon) shape;
					render(image, polygon);
				}
			}
		}
		return ImageUtil.toImage(image);
	}

	public void render(BufferedImage image, Rect rect) {
		Graphics2D g = (Graphics2D) image.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		{
			RoundRectangle2D r = new RoundRectangle2D.Double(rect.x, rect.y, rect.w, rect.h, rect.rx, rect.ry);
			// fill
			{
				setFill(g, rect);
				g.fill(r);
			}
			// stroke
			if (rect.stroke != null) {
				BasicStroke stroke = (BasicStroke) rect.stroke;
				g.setColor(stroke.color);
				g.setStroke(new java.awt.BasicStroke(1.0f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
				g.draw(r);
			}
			// FIXME this is another nested "shape" (en)
			// text
			if (rect.text != null) {
				Font font = new Font("Arial", Font.PLAIN, 18);
				Rectangle2D bounds = font.getStringBounds(rect.text, g.getFontRenderContext());

				g.setColor(Color.black);
				g.setFont(font);
				int textX = toInt(rect.x + (rect.w - bounds.getWidth()) / 2);
				int textY = toInt(rect.y + rect.h / 2 + bounds.getHeight() / 2);
				g.drawString(rect.text, textX, textY);
			}
		}
		g.dispose();
	}

	private static final int toInt(double value) {
		return (int) value;
	}

	// FIXME replace Polygon with Path2d.Double (en)
	public void render(BufferedImage image, Polygon polygon) {
		Graphics2D g = (Graphics2D) image.getGraphics();
		// g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		{
			// path
			java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
			{
				p.moveTo(polygon.path.xpoints[0], polygon.path.ypoints[0]);
				for (int i = 1; i < polygon.path.npoints; i++) {
					p.lineTo(polygon.path.xpoints[i], polygon.path.ypoints[i]);
				}
				p.closePath();
			}

			// fill
			{
				setFill(g, polygon);
				g.fill(p);
			}
			// stroke
			{
				BasicStroke stroke = (BasicStroke) polygon.stroke;
				g.setColor(stroke.color);
				g.setStroke(new java.awt.BasicStroke(1.0f));
				g.draw(p);
			}
		}
		g.dispose();
	}

	private void setFill(Graphics2D g, Shape shape) {
		if (shape.getFill() instanceof LinearGradient) {
			LinearGradient fill = (LinearGradient) shape.getFill();
			GradientPaint gp = new GradientPaint((int) shape.getX(), (int) shape.getY(), fill.from, (int) shape.getX(), (int) shape.getY()
					+ (int) shape.getHeight(), fill.to, true);
			g.setPaint(gp);
		} else if (shape.getFill() instanceof BasicFill) {
			BasicFill fill = (BasicFill) shape.getFill();
			g.setPaint(fill.color);
		} else {
			throw new IllegalStateException();
		}
	}

	public static class Builder {

		private Procedure proc = new Procedure();

		public static class Type {
			public Type text() {
				return this;
			}

			public Type icon() {
				return this;
			}

			public Type or() {
				return this;
			}
		}

		public static class Composite {
			public Composite center() {
				return this;
			}

			public Composite type(Type type) {
				return this;
			}
		}

		public interface Shape {
			ProceduralRenderer.Shape build();
		}

		public static class Color {

			public static java.awt.Color white() {
				return java.awt.Color.white;
			}

			public static java.awt.Color darkGray() {
				return java.awt.Color.darkGray;
			}

			public static java.awt.Color lightGray() {
				return java.awt.Color.lightGray;
			}

			public static java.awt.Color black() {
				return java.awt.Color.black;
			}

			public static java.awt.Color blue() {
				return java.awt.Color.blue;
			}
		}

		public static class Dimension {

			public enum Orientiation {
				width, height
			}

			public final Orientiation orientiation;
			public final int size;

			public Dimension(Orientiation orientiation, int size) {
				this.orientiation = orientiation;
				this.size = size;
			}

			public static Dimension width(int width) {
				return new Dimension(Orientiation.width, width);
			}

			public static Dimension height(int height) {
				return new Dimension(Orientiation.height, height);
			}
		}

		public static class Rect implements Shape {

			private int x, y;
			private int w, h;
			private int rx, ry;
			private Stroke stroke;
			private Fill fill;
			private String text;

			public Rect(int w, int h) {
				this.w = w;
				this.h = h;
			}

			public Rect(int x, int y, int w, int h) {
				this.x = x;
				this.y = y;
				this.w = w;
				this.h = h;
			}

			public Rect round(int rx, int ry) {
				this.rx = rx;
				this.ry = ry;
				return this;
			}

			public Rect stroke(java.awt.Color color) {
				this.stroke = new BasicStroke(color);
				return this;
			}

			public Rect gradient(java.awt.Color from, java.awt.Color to) {
				this.fill = new LinearGradient(from, to);
				return this;
			}

			public Rect fill(java.awt.Color color) {
				this.fill = new BasicFill(color);
				return this;
			}

			public Rect min(Dimension... dim) {
				return this;
			}

			public Rect max(Dimension... dim) {
				return this;
			}

			public Rect text(String text) {
				this.text = text;
				return this;
			}

			public ProceduralRenderer.Shape build() {
				return new ProceduralRenderer.Rect(x, y, w, h, rx, ry, stroke, fill, text);
			}
		}

		public static class Polygon implements Shape {

			private List<Point.Double> points = new LinkedList<Point.Double>();
			private Stroke stroke;
			private Fill fill;

			public Polygon() {

			}

			public Polygon points(double... xy) {
				for (int i = 0; i < xy.length; i += 2) {
					points.add(new Point.Double(xy[i], xy[i + 1]));
				}
				return this;
			}

			public Polygon stroke(java.awt.Color color) {
				this.stroke = new BasicStroke(color);
				return this;
			}

			public Polygon gradient(java.awt.Color from, java.awt.Color to) {
				this.fill = new LinearGradient(from, to);
				return this;
			}

			public Polygon fill(java.awt.Color color) {
				this.fill = new BasicFill(color);
				return this;
			}

			public ProceduralRenderer.Shape build() {
				return new ProceduralRenderer.Polygon(new Contour(points), stroke, fill);
			}
		}

		public static Rect rect(int w, int h) {
			return new Rect(w, h);
		}

		public static Rect rect(int x, int y, int w, int h) {
			return new Rect(x, y, w, h);
		}

		public static Polygon polygon() {
			return new Polygon();
		}

		public static Composite child() {
			return new Composite();
		}

		public static Type text() {
			return new Type();
		}

		public static Type icon() {
			return new Type();
		}

		public Builder shape(Shape shape) {
			ProceduralRenderer.Shape desc = shape.build();
			proc.shapes.add(desc);
			proc.w = Math.max(proc.w, desc.getWidth());
			proc.h = Math.max(proc.h, desc.getHeight());
			return this;
		}

		public Builder shape(Shape shape, Composite composite) {
			ProceduralRenderer.Shape desc = shape.build();
			proc.shapes.add(desc);
			proc.w = Math.max(proc.w, desc.getWidth());
			proc.h = Math.max(proc.h, desc.getHeight());
			return this;
		}

		public static Builder describe() {
			return new Builder();
		}

		public Procedure build() {
			return proc;
		}
	}

}