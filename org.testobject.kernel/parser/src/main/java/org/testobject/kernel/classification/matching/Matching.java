package org.testobject.kernel.classification.matching;

import java.util.List;
import java.util.Map;

import org.testobject.commons.util.image.Image;
import org.testobject.kernel.api.classification.graph.Locator;

/**
 * 
 * @author enijkamp
 *
 */
public class Matching {
	
	public static class Match {
		public final double probability;
		public final Locator.Node locator;
		
		public Match(double probability, Locator.Node locator) {
			this.probability = probability;
			this.locator = locator;
		}
	}
	
	private final Map<String, Matcher> matchers;
	private final Image.Int raw1, raw2;

	public Matching(Map<String, Matcher> matchers, Image.Int raw1, Image.Int raw2) {
		this.matchers = matchers;
		this.raw1 = raw1;
		this.raw2 = raw2;
	}
	
	public Match best(List<Locator.Node> source, Locator.Descriptor target) {
		
		if(source.isEmpty()) {
			throw new IllegalArgumentException();
		}
		
		Locator.Node bestDescriptor = source.get(0);
		double bestProbability = 0d;
		
		for(Locator.Node candidate : source) {
			double probability = match(candidate.getDescriptor(), target);
			if(probability > bestProbability) {
				bestProbability = probability;
				bestDescriptor = candidate;
			}
		}
		
		return new Match(bestProbability, bestDescriptor);
	}
	
	public double match(Locator.Descriptor locator1, Locator.Descriptor locator2) {
		String type1 = locator1.getLabel().getType();
		String type2 = locator2.getLabel().getType();

		if(type1.equals(type2) == false) {
			return 0d;
		}
		
		if(matchers.containsKey(type1) == false) {
			throw new IllegalStateException("No matcher registered for type '" + type1 + "'");
		}
		
		Matcher matcher = matchers.get(type1);
		
		Matcher.Match match = matcher.match(locator1, Matcher.Context.Factory.create(raw1), locator2, Matcher.Context.Factory.create(raw2));
		
		return match.getProbability();
	}
	
}
