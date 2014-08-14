package org.testobject.kernel.classification.gui;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.collections.Lists;
import org.testobject.kernel.api.classification.classifiers.Classifier;
import org.testobject.kernel.api.classification.graph.Element;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.api.classification.graph.Variable;
import org.testobject.kernel.api.classification.graph.Locator.Descriptor;
import org.testobject.kernel.api.util.DescriptorUtil;
import org.testobject.kernel.api.util.LocatorUtil;
import org.testobject.kernel.api.util.VariableUtil;
import org.testobject.kernel.imaging.procedural.Dimension;
import org.testobject.kernel.imaging.procedural.Edge;
import org.testobject.kernel.imaging.procedural.Renderer;
import org.testobject.kernel.imaging.procedural.Transform;
import org.testobject.kernel.imaging.segmentation.Blob;
import org.testobject.kernel.imaging.segmentation.Mask;

/**
 * 
 * @author enijkamp
 *
 */
public interface Tree {
	
    interface Adapter<T> {
    	
    	Rectangle.Int getBoundingBox(T node);
    	
    	String getName(T node);
        
        List<T> getChildren(T node);
    }
    
    interface Translater<T> {
    	
    	interface Node {
        	
        	String getName();
        	
        	Rectangle.Int getBoundingBox();
        	
        	List<Node> getChildren();
        }
    	
    	class Adapter implements Tree.Adapter<Node> {

			@Override
			public Rectangle.Int getBoundingBox(Node node) {
				return node.getBoundingBox();
			}

			@Override
			public String getName(Node node) {
				return node.getName();
			}

			@Override
			public List<Node> getChildren(Node node) {
				return node.getChildren();
			}
    		
    	}
    	
    	Node translate(T source);
    }
    
    class BlobAdapter implements Adapter<Blob> {

		@Override
		public Rectangle.Int getBoundingBox(Blob blob) {
			return blob.bbox;
		}

		@Override
		public String getName(Blob blob) {
			return Integer.toString(blob.id);
		}

		@Override
		public List<Blob> getChildren(Blob blob) {
			return blob.children;
		}
    }
    
    class NodeTranslater implements Translater<Node> {

		@Override
		public Node translate(final org.testobject.kernel.api.classification.graph.Node source) {
			final List<Node> children = Lists.newLinkedList();
			{
				addMasks(children, source);
				addDistance(children, source);
				addFeatures(children, source);
				addChildren(children, source);
			}
			
			return toNode(source.getElement().getLabel().getQualifier().toString(), source, children);
		}
		
		private void addMasks(List<Node> children, org.testobject.kernel.api.classification.graph.Node source) {
			Element element = source.getElement();
			
			if(element.getMasks().isEmpty() == false) {
				List<Node> masks = Lists.newArrayList(element.getMasks().size());
				for(Mask mask : element.getMasks()) {
					masks.add(toNode("box = " + mask.getBoundingBox().toString(), mask));
				}
				children.add(toNode("masks", source, masks));
			}
		}

		private void addChildren(List<Node> children, org.testobject.kernel.api.classification.graph.Node source) {
			if(source.getChildren().isEmpty() == false) {
				children.add(toNode("children", source, toNodes(source.getChildren())));
			}
		}

		private List<Node> toNodes(List<org.testobject.kernel.api.classification.graph.Node> children) {
			List<Node> target = Lists.newArrayList(children.size());
			for(org.testobject.kernel.api.classification.graph.Node child : children) {
				target.add(translate(child));
			}
			return target;
		}

		private void addDistance(List<Node> children, final org.testobject.kernel.api.classification.graph.Node source) {
			Element.Label label = source.getElement().getLabel();
			
			if(label.getLikelihood() != Classifier.Likelihood.Factory.none()) {
				Classifier.Likelihood distance = source.getElement().getLabel().getLikelihood();
				children.add(toNode("likelihood", source,
						Lists.toList(
								toNode("geometric = " + distance.geometric(), source),
								toNode("photometric = " + distance.photometric(), source))));
			}
		}
		
		private void addFeatures(List<Node> children, final org.testobject.kernel.api.classification.graph.Node source) {
			Element.Label label = source.getElement().getLabel();
			
			if(label.getFeatures().isEmpty() == false) {
				List<Node> features = Lists.newArrayList(label.getFeatures().size());
				for(Variable<?> feature : label.getFeatures()) {
					features.add(toNode(featureToString(feature), source));
				}
				children.add(toNode("features", source, features));
			}
		}
		
		private static String featureToString(Variable<?> variable) {
			return variable.getName() + " = " + variable.getValue().toString();
		}

		private Node toNode(final String name, final org.testobject.kernel.api.classification.graph.Node parent) {
			return toNode(name, parent, Collections.<Node>emptyList());
		}
		
		private Node toNode(final String name, final Mask mask) {
			return new Node() {
				@Override
				public String getName() {
					return name;
				}

				@Override
				public Rectangle.Int getBoundingBox() {
					return mask.getBoundingBox();
				}

				@Override
				public List<Node> getChildren() {
					return Lists.empty();
				}
			};
		}
		
		private Node toNode(final String name, final org.testobject.kernel.api.classification.graph.Node parent, final List<Node> children) {
			return new Node() {
				@Override
				public String getName() {
					return name;
				}

				@Override
				public Rectangle.Int getBoundingBox() {
					return box(parent);
				}

				@Override
				public List<Node> getChildren() {
					return children;
				}
			};
		}

		private Rectangle.Int box(org.testobject.kernel.api.classification.graph.Node source) {
			return source.getElement().getBoundingBox();
		}
    }
    
    class BlobTranslater implements Translater<Node> {

		@Override
		public Node translate(final org.testobject.kernel.api.classification.graph.Node source) {
			final List<Node> children = Lists.newLinkedList();
			{
				addChildren(children, source);
			}
			
			return toNode(source.getElement().getLabel().getQualifier().toString(), source, children);
		}

		private void addChildren(List<Node> children, org.testobject.kernel.api.classification.graph.Node source) {
			if(source.getChildren().isEmpty() == false) {
				children.addAll(toNodes(source.getChildren()));
			}
		}

		private List<Node> toNodes(List<org.testobject.kernel.api.classification.graph.Node> children) {
			List<Node> target = Lists.newArrayList(children.size());
			for(org.testobject.kernel.api.classification.graph.Node child : children) {
				target.add(translate(child));
			}
			return target;
		}
		
		private Node toNode(final String name, final org.testobject.kernel.api.classification.graph.Node parent, final List<Node> children) {
			return new Node() {
				@Override
				public String getName() {
					return name;
				}

				@Override
				public Rectangle.Int getBoundingBox() {
					return box(parent);
				}

				@Override
				public List<Node> getChildren() {
					return children;
				}
			};
		}

		private Rectangle.Int box(org.testobject.kernel.api.classification.graph.Node source) {
			return source.getElement().getBoundingBox();
		}
    }
    
    class RenderGraphTranslater implements Translater<org.testobject.kernel.imaging.procedural.Node> {

		@Override
		public Node translate(final org.testobject.kernel.imaging.procedural.Node source) {
			return translate(source, 0, 0);
		}
		
		private Node translate(final org.testobject.kernel.imaging.procedural.Node source, int offsetX, int offsetY) {
			final org.testobject.commons.math.algebra.Rectangle.Int box = box(offsetX, offsetY, source);
			final List<Node> children = Lists.newLinkedList();
			{
				Point.Int transform = getTransform(source);
				addTransform(children, box, source);
				addElements(children, source, offsetX + transform.x, offsetY + transform.y);
				addNodes(children, source, offsetX + transform.x, offsetY + transform.y);
			}
			
			return toNode("node", box, children);
		}

		private Point.Int getTransform(org.testobject.kernel.imaging.procedural.Node node) {
			
			// TODO take scaling into account (en)
			Transform transform = node.getTransform();
			if(transform instanceof Transform.Translate) {
				Transform.Translate translate = (Transform.Translate) transform;
				return new Point.Int((int) translate.x, (int) translate.y);
			}
			
			return new Point.Int(0, 0);
		}

		private void addNodes(List<Node> children, org.testobject.kernel.imaging.procedural.Node source, int offsetX, int offsetY) {
			if(source.getNodes().isEmpty() == false) {
				children.add(toNode("nodes", box(offsetX, offsetY, source), toNodes(source.getNodes(), offsetX, offsetY)));
			}
		}

		private List<Node> toNodes(List<Edge<org.testobject.kernel.imaging.procedural.Node>> children, int offsetX, int offsetY) {
			List<Node> target = Lists.newArrayList(children.size());
			for(Edge<org.testobject.kernel.imaging.procedural.Node> child : children) {
				target.add(translate(child.getTarget(), offsetX, offsetY));
			}
			return target;
		}
		
		private void addTransform(List<org.testobject.kernel.classification.gui.Tree.Translater.Node> children, org.testobject.commons.math.algebra.Rectangle.Int box, org.testobject.kernel.imaging.procedural.Node node) {
			
			Transform transform = node.getTransform();
			
			if(transform instanceof Transform.Translate) {
				Transform.Translate translate = (Transform.Translate) transform;
				Node child = toNode("translate x=" + translate.x + " y=" + translate.y, box);
				children.add(toNode("transform", box, Lists.toList(child)));
			}
			
			if(transform instanceof Transform.Scale) {
				Transform.Scale scale = (Transform.Scale) transform;
				Node child = toNode("scale sx=" + scale.sx + " sy=" + scale.sy, box);
				children.add(toNode("transform", box, Lists.toList(child)));
			}
		}

		private void addElements(List<Node> children, final org.testobject.kernel.imaging.procedural.Node source, int offsetX, int offsetY) {
			if(source.getElements().size() > 0) {
				List<Node> elements = Lists.newArrayList(source.getElements().size());
				for(Edge<org.testobject.kernel.imaging.procedural.Element> edge : source.getElements()) {
					elements.add(toNode(toString(edge.getTarget()), box(offsetX, offsetY, edge.getTarget())));
				}
				
				children.add(toNode("elements", box(offsetX, offsetY, source), elements));	
			}
		}
		
		private static String toString(org.testobject.kernel.imaging.procedural.Element element) {
			return element.getClass().getInterfaces()[0].getSimpleName().toLowerCase();
		}

		private Node toNode(String name, Rectangle.Int box) {
			return toNode(name, box, Collections.<Node>emptyList());
		}
		
		private Node toNode(final String name, final Rectangle.Int box, final List<Node> children) {
			return new Node() {
				@Override
				public String getName() {
					return name;
				}

				@Override
				public Rectangle.Int getBoundingBox() {
					return box;
				}

				@Override
				public List<Node> getChildren() {
					return children;
				}
			};
		}
		
		private Rectangle.Int box(double x, double y, double w, double h) {
			return new Rectangle.Int((int) x, (int) y, (int) w, (int) h);
		}
		
		private Rectangle.Int box(double x, double y, org.testobject.kernel.imaging.procedural.Node node) {
			Dimension.Double size = Renderer.Size.getSize(node);
			return box(x, y, size.w, size.h);
		}
		
		private Rectangle.Int box(double x, double y, org.testobject.kernel.imaging.procedural.Element element) {
			Dimension.Double size = element.getSize();
			return box(x, y, size.w, size.h);
		}
    }
    
    class LocatorTranslater implements Translater<org.testobject.kernel.api.classification.graph.Locator.Node> {

		@Override
		public Node translate(final org.testobject.kernel.api.classification.graph.Locator.Node source) {
			final List<Node> children = Lists.newLinkedList();
			{
				addMasks(children, source);
				addFeatures(children, source);
				addChildren(children, source);
			}
			
			return toNode(toString(source), source, children);
		}
		
		private void addMasks(List<Node> children, final org.testobject.kernel.api.classification.graph.Locator.Node source) {
			Locator.Descriptor element = source.getDescriptor();
			
			List<Mask> masks = VariableUtil.getMasks(element.getFeatures());
			if(masks.isEmpty() == false) {
				List<Node> copy = Lists.newArrayList(masks.size());
				for(Mask mask : masks) {
					copy.add(toNode("box = " + mask.getBoundingBox().toString(), mask));
				}
				children.add(toNode("masks", source, copy));
			}
		}

		private String toString(org.testobject.kernel.api.classification.graph.Locator.Node source) {
			return source.getDescriptor().getId() + " -> " + source.getDescriptor().getLabel().toString();
		}

		private void addChildren(List<Node> children, org.testobject.kernel.api.classification.graph.Locator.Node source) {
			if(source.getChildren().isEmpty() == false) {
				children.add(toNode("children", source, toNodes(source.getChildren())));
			}
		}
		
		private void addFeatures(List<Node> children, final org.testobject.kernel.api.classification.graph.Locator.Node source) {
			Descriptor descriptor = source.getDescriptor();

			if(descriptor.getFeatures().isEmpty() == false) {
				List<Node> variables = Lists.newArrayList(descriptor.getFeatures().size());
				for(Variable<?> variable : descriptor.getFeatures()) {
					variables.add(toNode(toString(variable), source));
				}
				children.add(toNode("features", source, variables));
			}
		}

		private static String toString(Variable<?> variable) {
			return variable.getName() + " = " + variable.getValue().toString();
		}

		private List<Node> toNodes(List<org.testobject.kernel.api.classification.graph.Locator.Node> children) {
			List<Node> target = Lists.newArrayList(children.size());
			for(org.testobject.kernel.api.classification.graph.Locator.Node child : children) {
				target.add(translate(child));
			}
			return target;
		}
		
		private Node toNode(String name, Locator.Node parent) {
			return toNode(name, parent, Lists.<Node>empty());
		}
		
		private Node toNode(final String name, final Mask mask) {
			return new Node() {
				@Override
				public String getName() {
					return name;
				}

				@Override
				public Rectangle.Int getBoundingBox() {
					return mask.getBoundingBox();
				}

				@Override
				public List<Node> getChildren() {
					return Lists.empty();
				}
			};
		}

		private Node toNode(final String name, final Locator.Node parent, final List<Node> children) {
			return new Node() {
				@Override
				public String getName() {
					return name;
				}

				@Override
				public Rectangle.Int getBoundingBox() {
					return boxFromVariables(parent);
				}

				@Override
				public List<Node> getChildren() {
					return children;
				}
			};
		}
		
		private Rectangle.Int boxFromVariables(Locator.Node source) {
			return DescriptorUtil.getBoundingBox(source.getDescriptor());
		}

		private Rectangle.Int boxFromMasks(Locator.Node source) {
			List<Mask> masks = VariableUtil.getMasks(source.getDescriptor().getFeatures());
			Rectangle.Int box = LocatorUtil.union(LocatorUtil.toRectangles(masks));
			return box;
		}
    }
    
    class Util {
	
		@SuppressWarnings("serial")
		private static class InternalTreeNode extends DefaultMutableTreeNode {
			
			public final String name;
			public final Rectangle.Int bbox;
			public final Object payload;
	
			InternalTreeNode(String name, Rectangle.Int bbox, Object payload) {
				this.name = name;
				this.bbox = bbox;
				this.payload = payload;
			}
	
			public String toString() {
				return name;
			}
		}
		
		public static Rectangle.Int getBoundingBox(TreeNode treeNode) {
			InternalTreeNode internal = (InternalTreeNode) treeNode;
			return internal.bbox;
		}
		
		@SuppressWarnings("unchecked")
		public static <T> T getPayload(TreeNode treeNode) {
			InternalTreeNode internal = (InternalTreeNode) treeNode;
			return (T) internal.payload;
		}
    }
    
	class Factory {
		
		public static JTree empty() {
			DefaultTreeModel model = new DefaultTreeModel(null);
			return new JTree(model);
		}
		
		public static <T> JTree create(Adapter<T> adapter, T node) {
			DefaultTreeModel model = new DefaultTreeModel(toTreeNode(adapter, node, new LinkedList<Rectangle.Int>()));
			return new JTree(model);
		}
		
		public static <T> List<Rectangle.Int> fill(JTree tree, Adapter<T> adapter, T node) {
			List<Rectangle.Int> boxes = new LinkedList<>();
			TreeNode treeNode = toTreeNode(adapter, node, boxes);
			((DefaultTreeModel) tree.getModel()).setRoot(treeNode);
			return boxes;
		}
		
		public static <T> List<Rectangle.Int> fill(JTree tree, Translater<T> translater, T node) {
			List<Rectangle.Int> boxes = new LinkedList<>();
			Translater.Node target = translater.translate(node);
			TreeNode treeNode = toTreeNode(new Translater.Adapter(), target, boxes);
			((DefaultTreeModel) tree.getModel()).setRoot(treeNode);
			return boxes;
		}
		
		private static <T> MutableTreeNode toTreeNode(Adapter<T> adapter, T node, List<Rectangle.Int> boxes) {
			Rectangle.Int box = adapter.getBoundingBox(node);
			boxes.add(box);
			Util.InternalTreeNode treeNode = new Util.InternalTreeNode(adapter.getName(node), box, node);
			for(T child : adapter.getChildren(node)) {
				treeNode.add(toTreeNode(adapter, child, boxes));
			}
			return treeNode;
		}
	}
}