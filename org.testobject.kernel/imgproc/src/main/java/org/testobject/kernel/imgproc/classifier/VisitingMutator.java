package org.testobject.kernel.imgproc.classifier;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.testobject.kernel.imgproc.blob.BlobUtils;
import org.testobject.kernel.imgproc.blob.Visitor;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.Group;
import org.testobject.kernel.imgproc.blob.Mutator;

/**
 * 
 * @author enijkamp
 *
 */
public class VisitingMutator implements Mutator
{
	private class CollectingVisitor implements Visitor
	{
		final Context context;
		final List<Mutator.Mutation> mutations = new LinkedList<Mutator.Mutation>();

		public CollectingVisitor(Context context)
		{
			this.context = context;
		}

		@Override
		public void visit(Blob blob)
		{
			for (Classifier classifier : classifiers)
			{
				Classifier.Match match = classifier.match(context, blob);
				if (match.getCertainty() > 0.0f)
				{
					mutations.addAll(match.getMutations());
				}
			}
		}
	}

	private final List<Classifier> classifiers;

	public VisitingMutator(List<Classifier> classifiers)
	{
		this.classifiers = classifiers;
	}

	public VisitingMutator(Classifier... classifiers)
	{
		this(Arrays.asList(classifiers));
	}

	@Override
	public List<Mutation> mutate(Context context, Blob blob)
	{
		// traverse tree
		CollectingVisitor visitor = new CollectingVisitor(context);
		BlobUtils.visit(blob, visitor);

		// mutate tree
		for (Mutator.Mutation mutation : visitor.mutations)
		{
			if (mutation instanceof Mutator.CreateGroup)
			{
				// get mutation
				CreateGroup createGroup = (CreateGroup) mutation;

				// new group blob
				List<Blob> childs = toList(createGroup.group);
				Rectangle bbox = createGroup.group.getBoundingBox();
				int[][] ids = createGroup.parent.ids;
				Blob group = new Blob(0, bbox, 0, childs, ids);

				// set meta
				group.meta = createGroup.meta;

				// back pointers
				if (createGroup.meta instanceof Classes.Widget)
				{
					Classes.Widget widget = (Classes.Widget) createGroup.meta;
					for (Blob reference : widget.getReferences())
					{
						reference.backpointers.add(group);
					}
				}

				// update hierarchy
				createGroup.parent.children.removeAll(childs);
				createGroup.parent.children.add(group);
			}
			else if (mutation instanceof Mutator.SetMeta)
			{
				// get mutation
				SetMeta set = (SetMeta) mutation;

				// back pointers
				if (set.meta instanceof Classes.Widget)
				{
					Classes.Widget widget = (Classes.Widget) set.meta;
					for (Blob reference : widget.getReferences())
					{
						reference.backpointers.add(set.blob);
					}
				}

				// set meta
				set.blob.meta = set.meta;
			}
			else
			{
				throw new UnsupportedOperationException(mutation.getClass().getName());
			}
		}

		return visitor.mutations;
	}

	private List<Blob> toList(Group<Blob> group)
	{
		List<Blob> list = new ArrayList<Blob>(group.size());
		for (Blob blob : group)
		{
			list.add(blob);
		}
		return list;
	}
}