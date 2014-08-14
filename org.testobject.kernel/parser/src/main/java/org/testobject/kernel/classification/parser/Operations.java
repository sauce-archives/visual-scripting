package org.testobject.kernel.classification.parser;

import java.util.List;

import org.testobject.commons.util.image.Image;
import org.testobject.kernel.api.classification.graph.Node;


/**
 * 
 * @author enijkamp
 *
 */
public interface Operations {
	
	interface Operation {
		
	}
	
	interface Input extends Operation {
		
		Node apply(Image.Int raw);
		
	}
	
	interface Map<From, To> extends Operation {
		
		To apply(From node, Context context);
		
	}
	
	interface Reduce<From, To> extends Operation {
		
		To apply(List<From> node, Context context);
		
	}
}
