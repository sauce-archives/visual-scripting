package org.testobject.kernel.imaging.procedural;

import static org.testobject.kernel.imaging.procedural.Style.Builder.style;

import java.util.List;

import org.testobject.commons.math.algebra.Point;
import org.testobject.kernel.imaging.contours.Contour;
import org.testobject.kernel.imaging.segmentation.Mask;

/**
 * 
 * @author enijkamp
 *
 */
public interface Element {
	
	Point.Double getLocation();
	
	Dimension.Double getSize();
	
	Style getStyle();
	
	boolean contains(Point.Double point);
	
	
	interface Text extends Element {
		
		String getText();
		
	}
	
	interface Rect extends Element {
		
		double getRoundX();
		
		double getRoundY();
		
	}
	
	interface Circle extends Element {
		
		double getRadius();
		
	}
	
	interface Polyline extends Element {
		
		List<Point.Double> getPath();
		
	}
	
	interface Image extends Element {
		
		Mask getMask();
		
		org.testobject.commons.util.image.Image.Int getRaw();
		
	}
	
	class Builder {
		
		public static Element.Text text(String text, Style style) {
			return new Impl.Text(new Point.Double(0, 0), text, style);
		}
		
		public static Element.Rect rect(double w, double h, double rx, double ry, Style style) {
			return new Impl.Rect(new Point.Double(0, 0), new Dimension.Double(w, h), rx, ry, style);
		}
		
		public static Element.Polyline polyline(List<Point.Double> path, Style style) {
			return new Impl.Polyline(new Point.Double(0, 0), path, style);
		}
		
		public static Element.Image image(org.testobject.commons.util.image.Image.Int image, Mask mask) {
			return new Impl.Image(new Point.Double(0, 0), image, mask);
		}
		
		public static Element.Rect rect(double x, double y, double w, double h, double rx, double ry, Style style) {
			return new Impl.Rect(new Point.Double(x, y), new Dimension.Double(w, h), rx, ry, style);
		}
		
		public static Element.Circle circle(double radius, Style style) {
			return new Impl.Circle(new Point.Double(0, 0), radius, style);
		}
		
		public static Element.Circle circle(double x, double y, double radius, Style style) {
			return new Impl.Circle(new Point.Double(x, y), radius, style);
		}
		
		interface Impl {
		
			abstract class Shared implements Element {
				
				protected final Point.Double location;
				protected final Dimension.Double size;
				protected final Style style;
				
				protected Shared(Point.Double location, Dimension.Double size, Style style) {
					this.location = location;
					this.size = size;
					this.style = style;
				}
	
				@Override
				public Point.Double getLocation() {
					return location;
				}
	
				@Override
				public Dimension.Double getSize() {
					return size;
				}
	
				@Override
				public Style getStyle() {
					return style;
				}
				
				@Override
				public boolean contains(Point.Double point) {
					throw new UnsupportedOperationException();
				}
			}
			
			class Rect extends Shared implements Element.Rect {
				
				private final double rx, ry;
				
				public Rect(Point.Double location, Dimension.Double size, double rx, double ry, Style style) {
					super(location, size, style);
					this.rx = rx;
					this.ry = ry;
				}
	
				@Override
				public double getRoundX() {
					return rx;
				}
	
				@Override
				public double getRoundY() {
					return ry;
				}
			}
			
			class Circle extends Shared implements Element.Circle {
				
				private final double r;
				
				public Circle(Point.Double location, double r, Style style) {
					super(location, new Dimension.Double(r*2, r*2), style);
					this.r = r;
				}
	
				@Override
				public double getRadius() {
					return r;
				}
			}
			
			class Polyline extends Shared implements Element.Polyline {
				
				public final List<Point.Double> points;
				
				public Polyline(Point.Double location, List<Point.Double> points, Style style) {
					super(location, getDimension(points), style);
					this.points = points;
				}
	
				@Override
				public boolean contains(Point.Double point) {
					return Contour.contains(points, point.x, point.y);
				}
	
				@Override
				public List<Point.Double> getPath() {
					return points;
				}
				
				private static Dimension.Double getDimension(List<Point.Double> points) {
					double w = 0, h = 0;
					for(Point.Double point : points) {
						w = Math.max(w, point.x);
						h = Math.max(h, point.y);
					}
					
					return new Dimension.Double(w, h);
				}
			}
			
			public class Image extends Shared implements Element.Image {
				
				public final org.testobject.commons.util.image.Image.Int raw;
				public final Mask mask;
				
				public Image(Point.Double location, org.testobject.commons.util.image.Image.Int raw, Mask mask) {
					super(location, new Dimension.Double(mask.getBoundingBox().w, mask.getBoundingBox().h), style(Style.Fill.Builder.none()));
					this.mask = mask;
					this.raw = raw;
				}
	
				@Override
				public Mask getMask() {
					return mask;
				}
	
				@Override
				public org.testobject.commons.util.image.Image.Int getRaw() {
					return raw;
				}
				
			}
			
			class Text extends Shared implements Element.Text {
				
				private final String text;
				
				public Text(Point.Double location, String text, Style style) {
					super(location, new Dimension.Double(0, 0), style);
					this.text = text;
				}
	
				@Override
				public String getText() {
					return text;
				}
			}
		
		}
	}

}
