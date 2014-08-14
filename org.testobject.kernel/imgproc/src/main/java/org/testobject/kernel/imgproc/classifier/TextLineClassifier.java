package org.testobject.kernel.imgproc.classifier;

import static org.testobject.kernel.imgproc.classifier.Utils.find;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.testobject.kernel.imgproc.blob.Group;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.GroupBuilder;
import org.testobject.kernel.imgproc.blob.Mutator;
import org.testobject.kernel.imgproc.blob.Mutator.CreateGroup;
import org.testobject.kernel.imgproc.classifier.Classes.TextLine;
import org.testobject.kernel.imgproc.classifier.Classes.TextWord;

/**
 * 
 * @author enijkamp
 * 
 */
public class TextLineClassifier extends ClassifierBase {

	private final int FAT_X = 40, FAT_Y = 5;

	private final GroupBuilder<Blob> grouping = new GroupBuilder<>();

	@Override
	public Specification getSpec() {
		return spec().requires(TextWord.class).returns(TextLine.class).build();
	}

	@Override
	public Classifier.Match match(Context context, Blob blob) {

		List<Blob> texts = find(blob.children, filter(TextWord.class));

		if (texts.size() <= 1) {
			return failed;
		}

		// FIXME use 'potential' vertical center of the words for grouping on y (en)
		List<Group<Blob>> lines = sortTopToBottom(grouping.buildGroups(texts, FAT_X, FAT_Y));
		

		List<Mutator.Mutation> createGroups = new ArrayList<>();

		for (Group<Blob> line : lines) {
			createGroups.add(new CreateGroup(blob, line, new Classes.TextLine(sortLeftToRight(line.getContent()))));
		}

		//FIXME lines with one word are still in texts al
		return new Match(new Classes.TextLine(texts), 1.0f, createGroups);
	}
	
	private List<Group<Blob>> sortTopToBottom(List<Group<Blob>> content) {
		
		List<Group<Blob>> sorted = new ArrayList<>(content);
		
		Collections.sort(sorted, new Comparator<Group<Blob>>() {
			@Override
			public int compare(Group<Blob> b1, Group<Blob> b2) {
				return Integer.compare(b1.getBoundingBox().y, b2.getBoundingBox().y);
			}
		});
		
		return sorted;
	}

	private List<Blob> sortLeftToRight(List<Blob> content) {
		
		List<Blob> sorted = new ArrayList<>(content);
		
		Collections.sort(sorted, new Comparator<Blob>() {
			@Override
			public int compare(Blob b1, Blob b2) {
				return Integer.compare(b1.getBoundingBox().x, b2.getBoundingBox().x);
			}
		});
		
		return sorted;
	}
}
