package org.testobject.kernel.imgproc.classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.testobject.kernel.imgproc.blob.Meta;

/**
 * 
 * @author enijkamp
 *
 */
public class Dependencies
{
	public static List<Classifier> order(List<Classifier> classifiers)
	{
		return Arrays.asList(order(classifiers.toArray(new Classifier[] {})));
	}

	// FIXME runs in endless loop if classifier dependency is missing (en)
	/**
	 * Generates a partial order as dependency tree and return the classifiers in a total order
	 * which takes dependencies between classifiers into account.
	 * 
	 * @param classifiers
	 * @return
	 */
	public static Classifier[] order(Classifier... classifiers)
	{
		// copy
		List<Classifier> remaining = new ArrayList<Classifier>(Arrays.asList(classifiers));

		// hashset
		Set<Class<? extends Meta>> provided = new HashSet<Class<? extends Meta>>();

		// order
		Classifier[] total = new Classifier[classifiers.length];
		int current = 0;

		// leafs
		for (Classifier classifier : classifiers)
		{
			if (classifier.getSpec().requires.isEmpty())
			{
				provided.add(classifier.getSpec().returns);
				total[current++] = classifier;
				remaining.remove(classifier);
			}
		}

		// leafs are mandatory
		if (current == 0)
		{
			throw new IllegalArgumentException("leaf nodes missing");
		}

		// traverse
		int before = remaining.size();
		do
		{
			before = remaining.size();
			ListIterator<Classifier> iter = remaining.listIterator();
			candidate: while (iter.hasNext())
			{
				Classifier classifier = iter.next();
				for (Class<? extends Meta> required : classifier.getSpec().requires)
				{
					if (!provided.contains(required))
					{
						continue candidate;
					}
				}
				provided.add(classifier.getSpec().returns);
				total[current++] = classifier;
				iter.remove();
			}
		} while (!remaining.isEmpty() && remaining.size() < before);

		// cycles
		if (!remaining.isEmpty())
		{
			throw new IllegalArgumentException("dependency mismatch");
		}

		return total;
	}
}