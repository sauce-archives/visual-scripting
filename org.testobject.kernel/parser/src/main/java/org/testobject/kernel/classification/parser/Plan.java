package org.testobject.kernel.classification.parser;

import java.util.List;

import org.testobject.kernel.classification.parser.Operations.Input;
import org.testobject.kernel.classification.parser.Operations.Operation;

/**
 * 
 * @author enijkamp
 *
 */
public interface Plan extends Operation {
	
	String getQualifier();
	
	List<Operation> getOperations();
	
	List<Cache.IsCache<?>> getCaches();
	
	interface Builder {
	
		Plan.Builder input(Input input);
		
		Plan.Builder caches(Operation ... operations);

		<From, To> Plan.Builder map(Operations.Map<From, To> map);
		
		<From, To> Plan.Builder reduce(Operations.Reduce<From, To> reduce);
		
		Plan.Builder map(Plan.Builder ... parallel);
		
		Plan build();
	}
	
}