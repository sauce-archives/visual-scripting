package org.testobject.kernel.imaging.procedural;

import static org.testobject.commons.util.collections.Lists.concat;
import static org.testobject.commons.util.collections.Lists.newArrayList;
import static org.testobject.commons.util.collections.Lists.toLinkedList;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedList;
import java.util.List;

import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.commons.math.algebra.Point;

/**
 * 
 * @author enijkamp
 *
 */
public class Renderer {
	
	public static class Size {
		
		private static class SizeOfNode {
			public double w, h;
		}
		
		public static Dimension.Double getSize(Node node) {
			SizeOfNode size = new SizeOfNode();
			computeSize(node, size, 0, 0);
			return new Dimension.Double(size.w, size.h);
		}

		private static void computeSize(Node node, SizeOfNode sizeOfNode, double translateX, double translateY) {
			
			// transform
			{
				Transform transform = node.getTransform();
				if(transform instanceof Transform.Translate) {
					Transform.Translate translate = (Transform.Translate) transform;
					translateX += translate.x;
					translateY += translate.y;
				}
				
				// TODO take scale transform into account (en)
			}
			
			// element
			{
				for(Edge<Element> element : node.getElements()) {
					Point.Double location = element.getTarget().getLocation();
					Dimension.Double size = element.getTarget().getSize();
					
					double globalW = translateX + location.x + size.w;
					double globalH = translateY + location.y + size.h;
					
					sizeOfNode.w = Math.max(sizeOfNode.w, globalW);
					sizeOfNode.h = Math.max(sizeOfNode.h, globalH);
				}
			}
			
			// childs
			{
				for(Edge<Node> child : node.getNodes()) {
					computeSize(child.getTarget(), sizeOfNode, translateX, translateY);
				}
			}
		}
	}
	
	private static class Layer {
		
		public final List<LinkedList<Node>> nodes = new LinkedList<>();
		
	}
	
	private static class LayerBuilder {
		
		public static List<Layer> build(Node node) {
			List<Layer> layers = newArrayList();
			build(0, toLinkedList(node), layers);
			return layers;
		}
		
		private static void build(int z, LinkedList<Node> path, List<Layer> layers) {
			get(layers, z).nodes.add(path);
			for(Edge<Node> child : path.getLast().getNodes()) {
				build(z+1, concat(path, child.getTarget()), layers);
			}
		}
		
		private static Layer get(List<Layer> layers, int z) {
			if(z - layers.size() > 1) {
				throw new IllegalStateException();
			}
			if(layers.size() == z) {
				layers.add(new Layer());
			} 
			return layers.get(z);
		}
		
	}
	
	public static void render(Graphics2D graphics, Node node) {
		
		Composite composite = graphics.getComposite();
		{
			// graphics
			{
				graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				graphics.setComposite(AlphaComposite.Src);
			}
			// layers
			{
				List<Layer> layers = LayerBuilder.build(node);
				for(Layer layer : layers) {
					for(LinkedList<Node> child : layer.nodes) {
						List<Transform> transforms = toTransforms(child);
						for(Edge<Element> element : child.getLast().getElements()) {
							render(graphics, element.getTarget(), transforms);
						}
					}
				}
			}
		}
		graphics.setComposite(composite);
	}
	
	private static List<Transform> toTransforms(LinkedList<Node> nodes) {
		List<Transform> transforms = Lists.newArrayList(nodes.size());
		for(Node node : nodes) {
			transforms.add(node.getTransform());
		}
		return transforms;
	}

	private static void render(Graphics2D graphics, Element element, List<Transform> transforms) {
		
		AffineTransform transform = graphics.getTransform();
		{
			transform(graphics, transforms);
			
			render(graphics, element);
		}
		graphics.setTransform(transform);
	}
	
	private static void render(Graphics2D graphics, Element element) {
		
		if(element instanceof Element.Rect) {
			Element.Rect rect = (Element.Rect) element;
			render(graphics, rect);
			return;
		}
		
		if(element instanceof Element.Circle) {
			Element.Circle circle = (Element.Circle) element;
			render(graphics, circle);
			return;
		}
		
		if(element instanceof Element.Polyline) {
			Element.Polyline polyline = (Element.Polyline) element;
			render(graphics, polyline);
			return;
		}
		
		if(element instanceof Element.Image) {
			Element.Image image = (Element.Image) element;
			render(graphics, image);
			return;
		}
		
		throw new IllegalArgumentException(element.getClass().getSimpleName());	
	}
	
	private static void transform(Graphics2D graphics, List<Transform> transforms) {
		for(Transform transform : transforms) {
			graphics.transform(toAwtTransform(transform));
		}
	}

	private static AffineTransform toAwtTransform(Transform transform) {
		
		if(transform instanceof Transform.Identity) {
			return new java.awt.geom.AffineTransform();
		}
		
		if(transform instanceof Transform.Translate) {
			Transform.Translate translate = (Transform.Translate) transform;
			java.awt.geom.AffineTransform awt = new java.awt.geom.AffineTransform();
			awt.translate(translate.getX(), translate.getY());
			return awt;
		}
		
		if(transform instanceof Transform.Scale) {
			Transform.Scale scale = (Transform.Scale) transform;
			java.awt.geom.AffineTransform awt = new java.awt.geom.AffineTransform();
			awt.scale(scale.getScaleX(), scale.getScaleY());
			return awt;
		}
		
		throw new IllegalArgumentException();
		
	}

	private static void render(Graphics2D graphics, Element.Rect rect) {

		int x = 0, y = 0;
		RoundRectangle2D r = new RoundRectangle2D.Double(x, y, rect.getSize().w, rect.getSize().h, rect.getRoundX(), rect.getRoundY());
		
		// fill
		{
			fill(graphics, rect.getStyle().getFill(), r);
		}
		
		// stroke
		{
			setStroke(graphics, rect.getStyle().getStroke());
			graphics.draw(r);
		}
	}
	
	private static void render(Graphics2D graphics, Element.Circle circle) {

		double x = circle.getLocation().x, y = circle.getLocation().y, r = circle.getRadius();
		Ellipse2D c = new Ellipse2D.Double(x - (r / 2), y - (r / 2), r, r);
		
		// fill
		{
			fill(graphics, circle.getStyle().getFill(), c);
		}
		
		// stroke
		{
			setStroke(graphics, circle.getStyle().getStroke());
			graphics.draw(c);
		}
	}
	
	private static void render(Graphics2D graphics, Element.Polyline polyline) {
		
		if(polyline.getPath().isEmpty() == false) {
		
			// path
			java.awt.geom.Path2D.Double path = new java.awt.geom.Path2D.Double();
			{
				List<Point.Double> points = polyline.getPath();
				path.moveTo(points.get(0).x, points.get(0).y);
				for (int i = 1; i < points.size(); i++) {
					path.lineTo(points.get(i).x, points.get(i).y);
				}
				path.closePath();
			}
			
			// fill
			{
				fill(graphics, polyline.getStyle().getFill(), path);
			}
			
			// stroke
			{
				setStroke(graphics, polyline.getStyle().getStroke());
				graphics.draw(path);
			}
		}
	}

	private static void fill(Graphics2D graphics, Style.Fill fill, java.awt.Shape shape) {
		if (fill instanceof Style.BasicFill) {
			Style.BasicFill basic = (Style.BasicFill) fill;
			graphics.setPaint(toAwtColor(basic.color));
			graphics.fill(shape);
		}
	}
	
	private static void render(Graphics2D graphics, Element.Image image) {

		// image
		{
			graphics.drawImage(ImageUtil.Convert.toBufferedImage(image.getRaw()), toInt(image.getLocation().x), toInt(image.getLocation().y), null);
		}
	}
	
	private static final int toInt(double value) {
		return (int) value;
	}
	
	private static void setStroke(Graphics2D g, Style.Stroke stroke) {
		if (stroke instanceof Style.BasicStroke) {
			Style.BasicStroke basic = (Style.BasicStroke) stroke;
			g.setColor(toAwtColor(basic.color));
			g.setStroke(new java.awt.BasicStroke(1.0f));
		}
	}
	
	private static java.awt.Color toAwtColor(Color color) {
		return new java.awt.Color(color.r, color.g, color.b, color.a);
	}

}
