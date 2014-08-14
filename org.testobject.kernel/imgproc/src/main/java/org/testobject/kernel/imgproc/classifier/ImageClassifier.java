package org.testobject.kernel.imgproc.classifier;

import java.awt.Rectangle;
import java.util.List;

import org.testobject.kernel.imgproc.blob.Blob;

public class ImageClassifier extends ClassifierBase {

	private static final Rectangle[] IMAGE_RECTS = {
			new Rectangle(0, 190, 240, 240),
			new Rectangle(240, 190, 240, 240),
			new Rectangle(0, 500, 240, 240),
			new Rectangle(240, 500, 240, 240) };

	@Override
	public Specification getSpec() {
		return spec().requires(Classes.Group.class).returns(Classes.Image.class).build();
	}

	@Override
	public Classifier.Match match(Context context, Blob blob) {
		int position = -1;
		for (int i = 0; i < IMAGE_RECTS.length; i++) {
			if (IMAGE_RECTS[i].contains(blob.bbox)) {
				position = i;
				break;
			}
		}

		if (position == -1) {
			return failed;
		}

		if (blob.bbox.width < 140 || blob.bbox.height < 160) {
			return failed;
		}

		int childs = countRecursive(blob.children);
		if (childs < 5) {
			return failed;
		}

		return ClassifierBase.setClass(1.0f, blob, new Classes.Image(Integer.toString(position), blob.children));
	}

	private int countRecursive(List<Blob> children) {
		int count = children.size();
		for (Blob blob : children) {
			count += countRecursive(blob.children);
		}
		return count;
	}

}
