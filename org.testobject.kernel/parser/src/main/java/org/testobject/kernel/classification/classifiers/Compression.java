package org.testobject.kernel.classification.classifiers;

import java.util.List;

import org.testobject.kernel.api.classification.classifiers.Classifier;
import org.testobject.kernel.api.classification.classifiers.Classifier.Qualifier;
import org.testobject.kernel.api.classification.graph.Variable;

/**
 * 
 * @author enijkamp
 *
 */
public interface Compression {
	
	interface Zip {
		
		boolean supports(Qualifier qualifier);

		List<Variable<?>> zip(Qualifier qualifier, List<Variable<?>> variables);
		
	}
	
	interface Unzip {
		
		boolean supports(Qualifier qualifier);

		List<Variable<?>> unzip(Qualifier qualifier, List<Variable<?>> variables, Classifier.Images images);
		
	}

}
