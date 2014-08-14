package org.testobject.kernel.classification.parser;

import java.util.ArrayList;
import java.util.List;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.kernel.api.classification.graph.Element;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.classification.parser.Operations.Map;

/**
 * 
 * @author enijkamp
 *
 */
public class ExpandFix implements Map<Node, Node> {

	private static final int EXPAND_VERTICAL = 4;
	private static final int EXPAND_HORIZONTAL = 4;

	@Override
	public Node apply(Node node, Context context) {
		return Node.Factory.create(expand(node.getChildren(), node.getElement().getBoundingBox()), node.getElement());
	}

	private List<Node> expand(List<Node> oldChildren, Rectangle.Int rootBox) {
		List<Node> newChildren = new ArrayList<>(oldChildren.size());
		for (Node oldChild : oldChildren) {
			newChildren.add(expand(oldChild, rootBox));
		}

		return newChildren;
	}

	private Node expand(Node oldNode, Rectangle.Int rootBox) {

		Element newElement = Element.Builder
				.element(oldNode.getElement().getMasks())
				.qualifier(oldNode.getElement().getLabel().getQualifier())
				.build(toBox(oldNode, rootBox));

		return Node.Factory.create(expand(oldNode.getChildren(), rootBox), newElement);
	}

	private Rectangle.Int toBox(Node node, Rectangle.Int rootBox) {
		Rectangle.Int box = node.getElement().getBoundingBox();

		int x1 = Math.max(0, box.x - EXPAND_HORIZONTAL);
		int y1 = Math.max(0, box.y - EXPAND_VERTICAL);

		int x2 = Math.min(box.x + box.w + EXPAND_HORIZONTAL, rootBox.w);
		int y2 = Math.min(box.y + box.h + EXPAND_VERTICAL, rootBox.h);

		int w = x2 - x1;
		int h = y2 - y1;

		return new Rectangle.Int(x1, y1, w, h);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName().toLowerCase() + "()";
	}
}
