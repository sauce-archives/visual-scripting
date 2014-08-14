package org.testobject.kernel.api.classification.graph;

import java.util.List;

import org.testobject.commons.util.collections.Lists;

// TODO consolidate with procedurale.Node interface? (en)
/**
 * 
 * @author enijkamp
 *
 */
public interface Node {
	
	List<Node> getChildren();
	
	Element getElement();
	
	class Factory {
		public static Node create(final List<Node> children, final Element element) {
			return new Node() {
				@Override
				public List<Node> getChildren() {
					return children;
				}
				
				@Override
				public Element getElement() {
					return element;
				}
			};
		}
		
		public static Node create(final Element element) {
			return new Node() {
				private final List<Node> children = Lists.newLinkedList();
				
				@Override
				public List<Node> getChildren() {
					return children;
				}
				
				@Override
				public Element getElement() {
					return element;
				}
			};
		}
	}
}