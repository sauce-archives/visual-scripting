package org.testobject.kernel.classification.util;

import java.util.List;

import org.testobject.commons.util.collections.Lists;
import org.testobject.kernel.api.classification.graph.Element;
import org.testobject.kernel.api.classification.graph.Node;

/**
 * 
 * @author enijkamp
 *
 */
public class ElementUtil {
    
    public static List<Element> flatten(Node node) {
    	List<Element> result = Lists.newLinkedList();
    	flatten(node, result);
		
		return result;
	}
    
	private static void flatten(Node node, List<Element> result) {
		result.add(node.getElement());
		for(Node child : node.getChildren()) {
			flatten(child, result);
		}
	}

}
