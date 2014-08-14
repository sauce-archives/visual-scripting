package org.testobject.kernel.classification.parser;

import java.util.ArrayList;
import java.util.List;

import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.math.algebra.Rectangle.Int;
import org.testobject.commons.math.algebra.Size;
import org.testobject.commons.util.collections.Lists;
import org.testobject.kernel.api.classification.graph.Element;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.api.util.VariableUtil;
import org.testobject.kernel.classification.parser.Operations.Map;

/**
 * 
 * z-level fix makes sure that the containment order of the locator hierarchy is correct 
 * with respect of the rectangular bounding boxes of locators.
 * 
 * @author inbar
 *
 */
public class ZLevelFix implements Map<Node, Node> {

	private final Fix<Node> fix = new Fix<Node>(new LocatorAdapter());

	@Override
	public Node apply(Node node, Context context) {
		return fix.fix(node);
	}

	public interface Adapter<T> {

		Rectangle.Int getBox(T t);

		List<T> getChilds(T t);

		void setChilds(T t, List<T> childs);

	}

	public static class LocatorAdapter implements Adapter<Node> {
		@Override
		public Rectangle.Int getBox(Node node) {
			Element descriptor = node.getElement();

			Point.Int position = VariableUtil.getPosition(descriptor.getLabel().getFeatures());
			Size.Int size = VariableUtil.getSize(descriptor.getLabel().getFeatures());

			return new Rectangle.Int(position, size);
		}

		@Override
		public List<Node> getChilds(Node node) {
			return node.getChildren();
		}

		@Override
		public void setChilds(Node node, List<Node> childs) {
			node.getChildren().clear();
			node.getChildren().addAll(childs);
		}
	}

	public static class Fix<T> {
		private final Adapter<T> adapter;

		public Fix(Adapter<T> adapter) {
			this.adapter = adapter;
		}

		/**
		 * receives a node and fixes its children's containment hierarchy.
		 * 
		 * @param t
		 * @return
		 */
		public T fix(T t) {
			List<T> childs = adapter.getChilds(t);
			if (childs != null && !childs.isEmpty()) {
				adapter.setChilds(t, fixChilds(childs));
			}
			
			return t;
		}

		/**
		 * receives a list of sibling nodes and fixes containment hierarchy.
		 * 
		 * first pass: form all pairwise siblings (a,b) in ascending order of locator nodes, 
		 * check if b is contained in a, and if so, add b as a child of a, and mark b as 'contained' (in the boolean array).
		 * (this is so b will be contained in one and only one node.)
		 * 
		 * second pass: perform the same procedure as in the first pass, only in descending order. 
		 * the second pass exists to complete the containment check for all remaining pairs (in descending order),
		 * without interfering to the first pass.
		 * 
		 * returns a new list with the correct hierarchy.
		 */
		private List<T> fixChilds(List<T> childs) {

			ArrayList<T> newChilds = Lists.newArrayList();
			boolean contained[] = new boolean[childs.size()];

			// first pass (first to last)
			for (int i = 0; i < childs.size() - 1; i++) {
				for (int j = i + 1; j < childs.size(); j++) {
					T a = childs.get(i);
					T b = childs.get(j);
					if (!contained[j] && isContained(a, b)) {
						adapter.getChilds(a).add(b);
						contained[j] = true;
					}
				}
			}

			// second pass  (last to first)
			for (int i = childs.size() - 1; i >= 1; i--) {
				for (int j = i - 2; j >= 0; j--) {
					T b = childs.get(i);
					T a = childs.get(j);
					if (!contained[j] && isContained(b, a)) {
						adapter.getChilds(b).add(a);
						contained[j] = true;
					}
				}
			}

			for (int i = 0; i < contained.length; i++) {
				if (!contained[i]) {
					newChilds.add(childs.get(i));
				}
			}

			for (T elm : newChilds) {
				fix(elm);
			}

			return newChilds;
		}

		private boolean isContained(T t1, T t2) {

			Int box2 = adapter.getBox(t2);
			Int box1 = adapter.getBox(t1);

			return box1.contains(box2.x, box2.y, box2.h, box2.w);

		}

	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName().toLowerCase() + "()";
	}

}
