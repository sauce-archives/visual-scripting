package org.testobject.kernel.classification.graph;

import java.util.Set;

import org.testobject.commons.util.collections.Sets;
import org.testobject.kernel.api.classification.classifiers.Classifier;
import org.testobject.kernel.api.classification.graph.Element;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.api.classification.graph.Variable;

/**
 * 
 * @author enijkamp
 *
 */
public class Printer {
	
	public static void print(Node node, String ... variables) {
		print(node, 0, variables);
	}
	
	private static void print(Node node, int indent, String ... variables) {
		System.out.println(spaces(indent) + "node");
		indent++;
		// elements
		{
			Element element = node.getElement();
			if(element != null) {
				print(indent, element, variables);
			}
		}
		// nodes
		{
			for(Node child : node.getChildren()) {
				print(child, indent, variables);
			}
		}
		indent--;
	}
	
	private static void print(int indent, Element element, String ... variables) {
		System.out.println(spaces(indent) + toString(element));
		Set<String> filter = Sets.from(variables);
		indent++;
		{
			Element.Label label = element.getLabel();
			System.out.println(spaces(indent) + "label");
			indent++;
			{
				System.out.println(spaces(indent) + "qualifier = (" + label.getQualifier().toString() + ")");
				
				if(label.getLikelihood() != Classifier.Likelihood.Factory.none()) {
					System.out.println(spaces(indent) + "likelihood");
					indent++;
					{
						System.out.println(spaces(indent) + "geometric = " + label.getLikelihood().geometric());
						System.out.println(spaces(indent) + "photometric = " + label.getLikelihood().photometric());
					}
					indent--;
				}
				
				if(label.getFeatures().size() > 0) {
					System.out.println(spaces(indent) + "features");
					indent++;
					for(Variable<?> feature : label.getFeatures()) {
						if(filter.isEmpty() || filter.contains(feature.getName())) {
							System.out.println(spaces(indent) + featureToString(feature));
						}
					}
					indent--;
				}
			}
			indent--;
		}
		indent--;
	}

	private static String spaces(int indent) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < indent; i++) {
			builder.append(" ");
		}
		return builder.toString();
	}
	
	private static String toString(Element element) {
		return element.getClass().getInterfaces()[0].getSimpleName().toLowerCase();
	}
	
	private static String featureToString(Variable<?> element) {
		return element.getName().toLowerCase() + " = " + element.getValue().toString();
	}
}
