package org.testobject.kernel.imaging.procedural;

import static org.testobject.kernel.imaging.procedural.Layout.Builder.layout;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author enijkamp
 *
 */
public interface Node {
	
	Transform getTransform();
	
	List<Edge<Element>> getElements();
	
	List<Edge<Node>> getNodes();
	
	
	class Builder {
		
		private Transform transform;
		private List<Edge<Element>> elements = new LinkedList<>();
		private List<Edge<Node>> nodes = new LinkedList<>();
		
		public static Node.Builder node(Node.Builder parent, Transform transform) {
			Node.Builder child = new Node.Builder(transform);
			parent.child(child);
			return child;
		}
		
		public static Node.Builder node(Transform transform) {
			return new Node.Builder(transform);
		}
		
		public static Node.Builder node() {
			return new Node.Builder(new Transform.Identity());
		}
		
		public Builder(Transform transform) {
			this.transform = transform;
		}
		
		public Builder element(Element element) {
			this.elements.add(new Impl.EdgeImpl<Element>(element, layout().none()));
			return this;
		}
		
		public Builder element(Element element, Layout layout) {
			this.elements.add(new Impl.EdgeImpl<Element>(element, layout));
			return this;
		}
		
		public Builder child(Builder node) {
			this.nodes.add(new Impl.EdgeImpl<Node>(node.build(), layout().none()));
			return this;
		}
		
		public Builder child(Node node) {
			this.nodes.add(new Impl.EdgeImpl<Node>(node, layout().none()));
			return this;
		}
		
		public Node build() {
			return new Impl.NodeImpl(nodes, elements, transform);
		}
		
		interface Impl {
			
			class NodeImpl implements Node {
				
				public final List<Edge<Element>> elements;
				public final List<Edge<Node>> nodes;
				public final Transform transform;
				
				public NodeImpl(List<Edge<Node>> nodes, List<Edge<Element>> elements, Transform transform) {
					this.nodes = nodes;
					this.elements = elements;
					this.transform = transform;
				}
				
				@Override
				public Transform getTransform() {
					return transform;
				}
				
				@Override
				public List<Edge<Element>> getElements() {
					return elements;
				}
				
				@Override
				public List<Edge<Node>> getNodes() {
					return nodes;
				}
				
				@Override
				public String toString() {
					return "count(nodes)=" + nodes.size();
				}
			}
			
			class EdgeImpl<T> implements Edge<T> {
				
				public final T target;
				public final Layout layout;
				
				public EdgeImpl(T target, Layout layout) {
					this.target = target;
					this.layout = layout;
				}
	
				@Override
				public Layout getLayout() {
					return layout;
				}
	
				@Override
				public T getTarget() {
					return target;
				}
			}
			
		}
	}

}
