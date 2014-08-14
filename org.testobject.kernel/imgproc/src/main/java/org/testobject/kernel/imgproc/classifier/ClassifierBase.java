package org.testobject.kernel.imgproc.classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.Meta;
import org.testobject.kernel.imgproc.blob.Mutator;

/**
 * 
 * @author nijkamp
 * 
 */
public abstract class ClassifierBase implements Classifier {
	public static final class Failed extends Classes.WidgetBase {
	}

	public static final Match failed = new Match(new Failed(), 0.0f, Collections.<Mutator.Mutation> emptyList());

	public static class Match implements Classifier.Match {
		public final Classes.Widget type;
		public final float certainty;
		public final List<Mutator.Mutation> mutations;

		public Match(Classes.Widget type, float certainty, Mutator.Mutation... mutations) {
			this(type, certainty, Arrays.asList(mutations));
		}

		public Match(Classes.Widget type, float certainty, List<Mutator.Mutation> mutations) {
			this.type = type;
			this.certainty = certainty;
			this.mutations = mutations;
		}

		@Override
		public Classes.Widget getWidget() {
			return type;
		}

		@Override
		public float getCertainty() {
			return certainty;
		}

		@Override
		public List<Mutator.Mutation> getMutations() {
			return mutations;
		}
	}

	protected static boolean isBlob(Blob blob) {
		return blob.meta == Meta.blob;
	}

	protected static boolean isGroup(Blob blob) {
		return blob.meta.getClass() == Classes.Group.class;
	}

	protected static Match setClass(float certainty, Blob blob, Classes.Widget widget) {
		Mutator.SetMeta mutation = new Mutator.SetMeta(blob, widget);
		return new Match(widget, certainty, mutation);
	}
	
	protected static Match setClass(float certainty, List<Blob> blobs, Classes.Widget widget) {
		List<Mutator.Mutation> mutations = new ArrayList<>(blobs.size());
		for(Blob blob : blobs) {
			mutations.add(new Mutator.SetMeta(blob, widget));
		}
		return new Match(widget, certainty, mutations);
	}

	protected static Utils.UnaryFilter<Blob> filter(final Class<? extends Classes.Widget> type) {
		return new Utils.UnaryFilter<Blob>() {
			@Override
			public boolean pass(Blob blob) {
				return blob.meta.getClass() == type;
			}
		};
	}
	
	protected static Utils.UnaryFilter<Blob> filter(final Class<? extends Classes.Widget> type1, final Class<? extends Classes.Widget> type2) {
		return new Utils.UnaryFilter<Blob>() {
			@Override
			public boolean pass(Blob blob) {
				return blob.meta.getClass() == type1 || blob.meta.getClass() == type2;
			}
		};
	}

	protected static List<Class<? extends Classes.Widget>> none() {
		return Collections.<Class<? extends Classes.Widget>> emptyList();
	}

	protected static List<Class<? extends Classes.Widget>> toList(Class<? extends Classes.Widget> c1) {
		List<Class<? extends Classes.Widget>> list = new ArrayList<Class<? extends Classes.Widget>>();
		list.add(c1);
		return list;
	}

	protected static List<Class<? extends Classes.Widget>> toList(Class<? extends Classes.Widget> c1, Class<? extends Classes.Widget> c2) {
		List<Class<? extends Classes.Widget>> list = toList(c1);
		list.add(c2);
		return list;
	}

	protected static List<Class<? extends Classes.Widget>> toList(Class<? extends Classes.Widget> c1, Class<? extends Classes.Widget> c2, Class<? extends Classes.Widget> c3) {
		List<Class<? extends Classes.Widget>> list = toList(c1, c2);
		list.add(c3);
		return list;
	}

	protected static class SpecificationBuilder {
		private final List<Class<? extends Classes.Widget>> requires = new LinkedList<Class<? extends Classes.Widget>>();
		private Class<? extends Classes.Widget> returns;

		public SpecificationBuilder requires(List<Class<? extends Classes.Widget>> requires) {
			requires.addAll(requires);
			return this;
		}

		public SpecificationBuilder requires(Class<? extends Classes.Widget> require1) {
			requires.add(require1);
			return this;
		}

		public SpecificationBuilder requires(Class<? extends Classes.Widget> require1, Class<? extends Classes.Widget> require2) {
			requires(require1);
			requires(require2);
			return this;
		}

		public SpecificationBuilder requires(Class<? extends Classes.Widget> require1, Class<? extends Classes.Widget> require2,
				Class<? extends Classes.Widget> require3) {
			requires(require1);
			requires(require2);
			requires(require3);
			return this;
		}

		public SpecificationBuilder returns(Class<? extends Classes.Widget> returns) {
			this.returns = returns;
			return this;
		}

		public Specification build() {
			return new Specification(requires, returns);
		}
	}

	protected static SpecificationBuilder spec() {
		return new SpecificationBuilder();
	}

	protected boolean hasBackpointerTo(Blob blob, Class<? extends Classes.Widget> widget) {
		for (Blob backpointer : blob.backpointers) {
			if (widget.equals(backpointer.meta.getClass())) {
				return true;
			}
		}
		return false;
	}
}