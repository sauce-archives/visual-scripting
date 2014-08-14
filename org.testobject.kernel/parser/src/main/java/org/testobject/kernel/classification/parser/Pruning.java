package org.testobject.kernel.classification.parser;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.kernel.api.classification.classifiers.Classifier;
import org.testobject.kernel.api.classification.graph.Element;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.classification.parser.Operations.Map;

/**
 * 
 * @author enijkamp
 *
 */
public interface Pruning {
	
	class LowPass implements Map<Node, Node> {
	
		@Override
		public Node apply(Node source, Context context) {
			// prepare
			Node target = Node.Factory.create(Element.Factory.node("root", Rectangle.Int.ZERO));
			
			// go
			for(Node child : source.getChildren()) {
				prune(child, target, context);
			}
			
			return target;
		}

		// FIXME this is messy (en)
		private void prune(Node source, Node target, Context context) {
			
			Element sourceElement = source.getElement();
			Element targetElement = Element.Builder.element(sourceElement.getMasks()).qualifier(sourceElement.getLabel().getQualifier()).build();
			
			// filter
			if(spansScreen(targetElement, context) == false) {
				Node element = Node.Factory.create(targetElement);
				target.getChildren().add(element);
				
				// FIXME messy (en)
				target = element;
			}
			
			if(isGroup(source) && hasSingleLevel(source)) {
				
				// childs
				for(Node child1 : source.getChildren()) {
					for(Node child2 : child1.getChildren()) {
						prune(child2, target, context);
					}
				}
				
			} else {
				
				// childs
				for(Node child : source.getChildren()) {
					prune(child, target, context);
				}
			}
		}
		
		private boolean hasSingleLevel(Node node) {
			for(Node child : node.getChildren()) {
				if(child.getChildren().size() > 0) {
					return false;
				}
			}
			return true;
		}
		
		private boolean spansScreen(Element sourceElement, Context context) {
			return sourceElement.getBoundingBox().w >= context.raw().w;
		}

		private boolean isGroup(Node node) {
			return Classifier.Qualifier.Factory.Class.group.equals(node.getElement().getLabel().getQualifier().getType());
		}
		
		@Override
		public String toString() {
			return "prune()";
		}
	
	}
	
}