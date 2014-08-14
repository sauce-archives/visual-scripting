package org.testobject.kernel.imgproc.classifier;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.Mutator;

// FIXME keep discrete class distribution (e.g. group & text) (en)
public class TextWordClassifier extends ClassifierBase {

	private static final float MINIMUM_TEXT_SHARE = 0.5f;

	@Override
	public Specification getSpec() {
		return spec().requires(Classes.TextChar.class, Classes.Group.class).returns(Classes.TextWord.class).build();
	}

	@Override
	public Classifier.Match match(Context context, Blob blob) {
		
		// prune
		LinkedList<Blob> texts = new LinkedList<>();
		{
			if (blob.children.size() <= 1) {
				return failed;
			}
	
			for (Blob child : blob.children) {
				if (child.meta instanceof Classes.TextChar) {
					texts.add(child);
				}
			}
	
			if (toFloat(texts.size()) / blob.children.size() < MINIMUM_TEXT_SHARE) {
				return failed;
			}
		}
		
		// compute font sizes
		Set<Integer> fontSizes = new HashSet<>();
		{
			boolean isFirst = true;
			for (Blob child : texts) {
				Classes.TextChar text = (Classes.TextChar) child.meta;
				if (isFirst) {
					for (Classes.TextChar.Candidate candidate : text.candidates) {
						fontSizes.add(candidate.size);
					}
					isFirst = false;
				} else {
					Set<Integer> currentFontSizes = new HashSet<>();
					for (Classes.TextChar.Candidate candidate : text.candidates) {
						currentFontSizes.add(candidate.size);
					}
					fontSizes.retainAll(currentFontSizes);
				}
			}
		}

		// mutate
		List<Mutator.Mutation> mutations = new LinkedList<>();

		Classes.TextWord widget = new Classes.TextWord(texts, fontSizes);
		mutations.add(new Mutator.SetMeta(blob, widget));

		for (Blob child : blob.children) {
			if (child.meta instanceof Classes.TextChar == false) {
				// FIXME use probabilistic approach (utilize discrete distribution of text-classifier) (en)
				if(child.area < 500) {
					mutations.add(new Mutator.SetMeta(child, new Classes.TextChar(Collections.<Classes.TextChar.Candidate> emptyList())));
				}
			}
		}

		return new Match(widget, 1.0f, mutations);
	}
	
	private static final float toFloat(int value) {
		return (float) value;
	}
}
