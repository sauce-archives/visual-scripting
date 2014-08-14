package org.testobject.kernel.classification.parser;

import java.util.List;

import org.testobject.kernel.classification.parser.Operations.Operation;

/**
 * 
 * @author enijkamp
 *
 */
public class Parallel implements Operation {
	
	private final List<Plan> plans;

	public Parallel(List<Plan> plans) {
		this.plans = plans;
	}

	public List<Plan> getPlans() {
		return plans;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName().toLowerCase() + "()";
	}
}