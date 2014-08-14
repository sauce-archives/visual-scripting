package org.testobject.kernel.classification.parser;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.collections.Lists;
import org.testobject.kernel.api.classification.classifiers.Classifier;
import org.testobject.kernel.api.classification.classifiers.Classifier.Lookup;
import org.testobject.kernel.api.classification.classifiers.Classifier.Proposal;
import org.testobject.kernel.api.classification.graph.Element;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.classification.parser.Operations.Map;
import org.testobject.kernel.imaging.segmentation.Mask;

// TODO parameters / config by @Named setters / constructors?
/**
 * 
 * @author enijkamp
 *
 */
public class Classification implements Map<Node, Node> {
	
	private static final int topK = 1;
	
	private final List<Classifier> classifiers;
	
	public Classification(List<Classifier> classifiers) {
		this.classifiers = classifiers;
	}

	@Override
	public Node apply(Node source, Context context) {
		
		// prepare
		Classifier.Images images = Classifier.Images.Factory.create(context.raw());
		
		// go
		List<Node> childs = Lists.newLinkedList();
		for(Node child : source.getChildren()) {
			childs.addAll(classify(child, images));
		}
		
		return Node.Factory.create(childs, Element.Factory.node("root", new Rectangle.Int(0, 0, context.raw().w, context.raw().h)));
	}

	private List<Node> classify(Node source, Classifier.Images images) {
		
		List<Node> childs = Lists.newLinkedList();
		for(Node child : source.getChildren()) {
			childs.addAll(classify(child, images));
		}
		
		List<Node> target = Lists.newLinkedList();
		
		Lookup lookup = Classifier.Lookup.Factory.node(source);
		for(Classifier classifier : classifiers) {
			List<Mask> masks = source.getElement().getMasks();
			Classifier.Context context = Classifier.Context.Factory.create(source.getElement().getLabel().getQualifier());
			List<Proposal> topK = topK(classifier.classify(images, lookup, masks, context));
			for(Proposal proposal : topK) {
				target.add(Node.Factory.create(childs, proposal.element()));
			}
		}
		
		return target;
	}

	private List<Classifier.Proposal> topK(List<Classifier.Proposal> proposals) {
		
		// sort
		Queue<Classifier.Proposal> pq = new PriorityQueue<>(topK, new Comparator<Classifier.Proposal>() {
			@Override
			public int compare(Classifier.Proposal p1, Classifier.Proposal p2) {
				return Double.compare(p2.likelihood().normalized(), p1.likelihood().normalized());
			}
		});
		pq.addAll(proposals);
		
		// subset
		List<Classifier.Proposal> subset = Lists.newArrayList(topK);
		for(int i = 0; i < Math.min(pq.size(), topK); i++) {
			subset.add(pq.poll());
		}
		
		return subset;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName().toLowerCase() + "(" + toString(classifiers) + ")";
	}

	private String toString(List<Classifier> classifiers) {
		String result = "";
		for(int i = 0; i < classifiers.size() - 1; i++) {
			result += classifiers.get(i).toString() + ", ";
		}
		if(classifiers.size() > 0) {
			result += classifiers.get(classifiers.size() - 1).toString();
		}
		return result;
	}
	
}