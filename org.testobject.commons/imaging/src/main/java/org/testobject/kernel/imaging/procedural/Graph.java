package org.testobject.kernel.imaging.procedural;

import static org.testobject.kernel.imaging.procedural.Layout.Builder.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testobject.kernel.imaging.procedural.Node.Builder.Impl;

/**
 * 
 * @author enijkamp
 *
 */
public interface Graph {
	
	class Builder {
		
		public static <T> Edge<T> edge(T to) {
			return new Impl.EdgeImpl<T>(to, layout().none());
		}
		
		public static <T> Edge<T> edge(T to, Layout layout) {
			return new Impl.EdgeImpl<T>(to, layout);
		}
			
		public static Node node(List<Node> nodes, List<? extends Element> elements, Transform transform) {
			return new Impl.NodeImpl(toEdges(nodes), toEdges(elements), transform);
		}
		
		public static Node node(List<? extends Element> elements, Transform transform) {
			return new Impl.NodeImpl(toEdges(Collections.<Node>emptyList()), toEdges(elements), transform);
		}
		
		public static <T> List<Edge<T>> toEdges(List<? extends T> nodes) {
			List<Edge<T>> edges = new ArrayList<>(nodes.size());
			for(T node : nodes) {
				edges.add(edge(node));
			}
			return edges;
		}
		
		public static Builder graph() {
			return new Builder();
		}
		
		private Node.Builder builder;
		
		public Node.Builder node(Transform transform) {
			this.builder = new Node.Builder(transform);
			return builder;
		}
		
		public Node.Builder node() {
			this.builder = new Node.Builder(new Transform.Identity());
			return builder;
		}
		
		public Node build() {
			return builder.build();
		}
	}

}
