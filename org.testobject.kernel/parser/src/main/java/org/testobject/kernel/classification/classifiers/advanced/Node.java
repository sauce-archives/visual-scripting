package org.testobject.kernel.classification.classifiers.advanced;

import static org.testobject.kernel.classification.matching.Matcher.Util.checkFeatures;
import static org.testobject.kernel.classification.matching.Matcher.Util.checkType;

import java.util.List;
import java.util.Set;

import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.collections.Sets;
import org.testobject.kernel.api.classification.classifiers.Classifier.Qualifier;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.classification.graph.Variable;
import org.testobject.kernel.api.classification.graph.Variable.Names;
import org.testobject.kernel.classification.classifiers.advanced.Image.Classifier;
import org.testobject.kernel.classification.matching.Matcher.Util.Factors;

/**
 * 
 * @author enijkamp
 *
 */
public interface Node {
	
	interface Compression {
		
		class Zip implements org.testobject.kernel.classification.classifiers.Compression.Zip {
		
			private static final Set<String> filter = Sets.from(Names.Geometric.position, Names.Geometric.size);
			
			@Override
			public List<Variable<?>> zip(Qualifier qualifier, List<Variable<?>> source) {
				List<Variable<?>> target = Lists.newArrayList(filter.size());
				for(Variable<?> variable : source) {
					if(filter.contains(variable.getName())) {
						target.add(variable);
					}
				}
				
				return target;
			}
			
			@Override
			public boolean supports(Qualifier qualifier) {
				return qualifier.getType().equals(Classifier.Qualifier.Factory.Class.node);
			}
		}
		
		class Unzip implements org.testobject.kernel.classification.classifiers.Compression.Unzip {
			
			@Override
			public List<Variable<?>> unzip(Qualifier qualifier, List<Variable<?>> source, Classifier.Images images) {
				return source;
			}
			
			@Override
			public boolean supports(Qualifier qualifier) {
				return qualifier.getType().equals(Classifier.Qualifier.Factory.Class.node);
			}
		}
	}
	
	public static class Matcher implements org.testobject.kernel.classification.matching.Matcher {
		
		@Override
		public Match match(Locator.Descriptor descriptor1, Context context1, Locator.Descriptor descriptor2, Context context2) {
			
			// sanity
			{
				checkType(descriptor1, descriptor2, Classifier.Qualifier.Factory.Class.node);
				checkFeatures(descriptor1, descriptor2, Names.Geometric.position, Names.Geometric.size);
			}
			
			// probabilities
			{
				Factors factors = new Factors(descriptor1, context1, descriptor2, context2); 
			
				factors
					.size(.5)
					.position(.5);
				
				return Match.Factory.create(factors.probabilities(), factors.probability());
			}
		}
	}

}
