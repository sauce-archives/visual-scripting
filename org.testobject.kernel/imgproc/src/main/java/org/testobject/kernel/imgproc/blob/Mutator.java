package org.testobject.kernel.imgproc.blob;

import java.util.List;

import org.testobject.kernel.imgproc.classifier.Context;

/**
 * The mutator should be only class is allowed to mutate the blob hierarchy.
 * 
 * @author nijkamp
 *
 */
public interface Mutator
{
	interface Mutation
	{
	}

	class CreateGroup implements Mutation
	{
		public final Blob parent;
		public final Group<Blob> group;
		public final Meta meta;

		public CreateGroup(Blob parent, Group<Blob> group, Meta meta)
		{
			this.parent = parent;
			this.group = group;
			this.meta = meta;
		}
	}

	class SetMeta implements Mutation
	{
		public final Blob blob;
		public final Meta meta;

		public SetMeta(Blob blob, Meta meta)
		{
			this.blob = blob;
			this.meta = meta;
		}
	}

	List<Mutation> mutate(Context context, Blob blob);
}
