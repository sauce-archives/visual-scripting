package org.testobject.kernel.classification.parser;

import java.util.List;

import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Size;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.lang.mutable.MutableInt;
import org.testobject.kernel.api.classification.graph.Element;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.api.classification.graph.Variable;
import org.testobject.kernel.classification.parser.Operations.Map;

/**
 * 
 * @author enijkamp
 *
 */
public class Locators implements Map<Node, Locator.Node> {
	
	@Override
	public Locator.Node apply(Node node, Context context) {
		
		// root
		Locator.Node root = toLocatorNode(node, new MutableInt(0));
		{
			List<Variable<?>> variables = root.getDescriptor().getFeatures();
			{
				variables.add(Element.Builder.position(Point.Int.zero()));
				variables.add(Element.Builder.size(Size.Int.from(context.raw().w, context.raw().h)));
			}
		}
		return root;
	}
	
	public Locator.Node toLocatorNode(Node node, MutableInt id) {
		
		// childs
		List<Locator.Node> childs = Lists.newArrayList(node.getChildren().size());
		for(Node child : node.getChildren()) {
			childs.add(toLocatorNode(child, id));
		}
		
		return Locator.Node.Factory.create(Locator.Descriptor.Factory.toDescriptor(increment(id), node.getElement()), childs);
	}
	
	private static int increment(MutableInt id) {
		int value = id.value;
		id.increment();
		return value;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName().toLowerCase() + "()";
	}
	
}