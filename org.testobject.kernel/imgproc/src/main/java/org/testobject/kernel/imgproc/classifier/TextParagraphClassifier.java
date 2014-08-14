package org.testobject.kernel.imgproc.classifier;

import static org.testobject.kernel.imgproc.classifier.Utils.find;

import java.util.ArrayList;
import java.util.List;

import org.testobject.kernel.imgproc.blob.Group;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.BlobUtils;
import org.testobject.kernel.imgproc.blob.GroupBuilder;
import org.testobject.kernel.imgproc.blob.Mutator;
import org.testobject.kernel.imgproc.blob.Mutator.CreateGroup;

/**
 * 
 * @author enijkamp
 *
 */
public class TextParagraphClassifier extends ClassifierBase {
	
	private final int FAT_X = 25, FAT_Y = 25;
	
	private final GroupBuilder<Blob> grouping = new GroupBuilder<>();

	@Override
	public Specification getSpec() {
		return spec().requires(Classes.TextLine.class).returns(Classes.TextParagraph.class).build();
	}

	@Override
	public Classifier.Match match(Context context, Blob blob) {
		
		List<Blob> texts = find(blob.children, filter(Classes.TextLine.class, Classes.TextWord.class));
		
		if(texts.size() <= 1) {
			return failed;
		}
		
		List<Group<Blob>> paragraphs = grouping.buildGroups(texts, FAT_X, FAT_Y);
		
		List<Mutator.Mutation> createGroups = new ArrayList<>();
		
		// FIXME hack for tech-demo (en)
		int numBlobs = 0;
		{
			for(Blob text : texts) {
				numBlobs += BlobUtils.countBlobs(text);
			}
		}
		
		for(Group<Blob> paragraph : paragraphs) {
			if(paragraph.size() > 1) {
				createGroups.add(new CreateGroup(blob, paragraph, new Classes.TextParagraph(paragraph.getContent(), numBlobs)));
			}
		}
		
		// FIXME incorrect, texts.length != createGroups (en)
		return new Match(new Classes.TextParagraph(texts, numBlobs), 1.0f, createGroups);
	}
}
