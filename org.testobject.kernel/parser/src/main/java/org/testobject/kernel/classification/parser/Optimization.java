package org.testobject.kernel.classification.parser;

import java.util.Iterator;
import java.util.List;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.functional.Functions.Function0;
import org.testobject.kernel.api.classification.graph.Element;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.classification.graph.Optimizer;
import org.testobject.kernel.classification.parser.Operations.Reduce;

/**
 * 
 * @author enijkamp
 *
 */
public interface Optimization extends Reduce<Node, Node> {
	
	class Flat implements Optimization {
		private static final int TOP_K = 1;
		
		private final Optimizer<Node, Node> optimizer;
		private final Function0<Node> lowpass;

		public Flat(Optimizer<Node, Node> optimizer, Function0<Node> lowpass) {
			this.optimizer = optimizer;
			this.lowpass = lowpass;
		}
		
		@Override
		public Node apply(List<Node> nodes, Context context) {
			
			Node segments = lowpass.apply();
			List<Node> winners = optimize(flat(segments.getChildren()), flat(nodes));
			Element root = Element.Factory.node("root", new Rectangle.Int(0, 0, segments.getElement().getBoundingBox().w, segments.getElement().getBoundingBox().h));
			
			return Node.Factory.create(winners, root);
		}

		private List<Node> flat(List<Node> sources) {
			List<Node> flat = Lists.newLinkedList();
			for(Node child : sources) {
				flat(child, flat);
			}
			
			return flat;
		}
		
		private void flat(Node source, List<Node> target) {
			target.add(source);
			for(Node child : source.getChildren()) {
				flat(child, target);
			}
		}

		private List<Node> optimize(List<Node> segments, List<Node> contestants) {
			
			List<Node> winners = Lists.newArrayList(segments.size() * TOP_K);
			for(Node segment : segments) {
				List<Node> topK = topK(optimizer.optimize(Lists.toList(segment), contestants));
				
				for(Node winner : topK) {
					List<Node> childs = optimize(segment.getChildren(), contestants);
					winners.add(Node.Factory.create(childs, winner.getElement()));
				}
			}
			
			return winners;
		}
		
		private List<Node> topK(Iterable<Node> candidates) {
			List<Node> topK = Lists.newArrayList(TOP_K);
			Iterator<Node> iter = candidates.iterator();
			
			while(iter.hasNext() && (topK.size() < TOP_K)) {
				topK.add(iter.next());
			}
			
			return topK;
		}

		@Override
		public String toString() {
			return "optimize(" + getClass().getSimpleName().toLowerCase() + ")";
		}
	}
	
	class Hierarchical implements Optimization {
		private static final int TOP_K = 1;
		
		private final Optimizer<Node, Node> optimizer;
		private final Function0<Node> lowpass;

		public Hierarchical(Optimizer<Node, Node> optimizer, Function0<Node> lowpass) {
			this.optimizer = optimizer;
			this.lowpass = lowpass;
		}
		
		@Override
		public Node apply(List<Node> nodes, Context context) {
			
			Node segments = lowpass.apply();
			List<Node> winners = optimize(segments.getChildren(), flat(nodes));
			Element root = Element.Factory.node("root", new Rectangle.Int(0, 0, segments.getElement().getBoundingBox().w, segments.getElement().getBoundingBox().h));
			
			return Node.Factory.create(winners, root);
		}

		private List<Node> flat(List<Node> sources) {
			List<Node> flat = Lists.newLinkedList();
			for(Node child : sources) {
				flat(child, flat);
			}
			
			return flat;
		}
		
		private void flat(Node source, List<Node> target) {
			target.add(source);
			for(Node child : source.getChildren()) {
				flat(child, target);
			}
		}

		private List<Node> optimize(List<Node> segments, List<Node> contestants) {
			
			List<Node> winners = Lists.newArrayList(segments.size() * TOP_K);
			for(Node segment : segments) {
				List<Node> topK = topK(optimizer.optimize(Lists.toList(segment), contestants));
				
				for(Node winner : topK) {
					List<Node> childs = optimize(segment.getChildren(), contestants);
					winners.add(Node.Factory.create(childs, winner.getElement()));
				}
			}
			
			return winners;
		}
		
		private List<Node> topK(Iterable<Node> candidates) {
			List<Node> topK = Lists.newArrayList(TOP_K);
			Iterator<Node> iter = candidates.iterator();
			
			while(iter.hasNext() && (topK.size() < TOP_K)) {
				topK.add(iter.next());
			}
			
			return topK;
		}

		@Override
		public String toString() {
			return "optimize(" + getClass().getSimpleName().toLowerCase() + ")";
		}
	}
}