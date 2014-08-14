package org.testobject.kernel.classification.graph;

import java.util.Collections;
import java.util.List;

import org.testobject.kernel.api.classification.graph.Element;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.imaging.segmentation.Mask;
import org.testobject.commons.util.collections.Lists;

/**
 * 
 * @author enijkamp
 *
 */
public interface Graph {
	
	interface Transform {
		
		Node apply(Node to);
		
		class Builder {
			
			private final List<Element.Builder> elements = Lists.newLinkedList();
			
			public static Builder transform() {
				return new Builder();
			}
			
			public static Builder transform(Element.Builder element) {
				Builder builder = transform();
				builder.elements.add(element);
				
				return builder;
			}
			
			public Element.Builder element(Mask mask) {
				Element.Builder element = Element.Builder.element(mask);
				elements.add(element);
				
				return element;
			}
			
			public Graph.Transform identity() {
				return new Graph.Transform() {
					@Override
					public Node apply(Node to) {
						return to;
					}
				};
			}
			
			public Graph.Transform build() {
				return new Graph.Transform() {
					@Override
					public Node apply(Node to) {
						for(Element.Builder builder : elements) {
							Element element = builder.build();
							// FIXME mutating 'getChildren()' is messy (en)
							// FIXME mutating node 'to' is messy --> deep copy (en)
							to.getChildren().add(Node.Factory.create(Collections.<Node>emptyList(), element));
						}
						return to;
					}
				};
			}
		}
		
	}
}
