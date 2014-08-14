package org.testobject.kernel.imgproc.classifier;

import java.util.List;

import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.Mutator.Mutation;
import org.testobject.kernel.imgproc.classifier.Classes.Widget;

/**
 * 
 * @author nijkamp
 *
 */
public interface Classifier
{
	interface Match
	{
		Widget getWidget();

		float getCertainty();

		// FIXME use graph rewriting concepts e.g. which productions / rules should be applied (en)
		List<Mutation> getMutations();
	}

	class Specification
	{
		public final List<Class<? extends Widget>> requires;
		public final Class<? extends Widget> returns;

		public Specification(List<Class<? extends Widget>> requires, Class<? extends Widget> returns)
		{
			this.requires = requires;
			this.returns = returns;
		}
	}

	/**
	 * Required to build the dependency tree of classifiers which determines
	 * a partial order in which classifiers should be applied.
	 * 
	 * @return
	 */
	Specification getSpec();

	/**
	 * Perform classification.
	 * 
	 * @param node
	 * @return
	 */
	Match match(Context context, Blob blob);
}