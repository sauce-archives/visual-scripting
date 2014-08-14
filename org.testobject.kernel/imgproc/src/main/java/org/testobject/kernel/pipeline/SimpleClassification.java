package org.testobject.kernel.pipeline;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.testobject.kernel.imgproc.classifier.Classifier;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.Image.Int;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.GraphBlobBuilder;
import org.testobject.kernel.imgproc.classifier.ButtonClassifier;
import org.testobject.kernel.imgproc.classifier.Context;
import org.testobject.kernel.imgproc.classifier.Dependencies;
import org.testobject.kernel.imgproc.classifier.GroupClassifier;
import org.testobject.kernel.imgproc.classifier.IconClassifier;
import org.testobject.kernel.imgproc.classifier.PopupClassifier;
import org.testobject.kernel.imgproc.classifier.TextBoxClassifier;
import org.testobject.kernel.imgproc.classifier.TextCharClassifier;
import org.testobject.kernel.imgproc.classifier.TextWordClassifier;
import org.testobject.kernel.imgproc.classifier.VisitingMutator;
import org.testobject.kernel.locator.api.Locator;
import org.testobject.kernel.locator.api.Root;
import org.testobject.kernel.ocr.TextRecognizer;

/**
 * 
 * @author enijkamp
 * 
 */
public class SimpleClassification implements Classification {

	public static final int BLACK_WHITE_THRESHOLD = 10;

	private static Classifier[] getDefaultClassifiers() {
		return new Classifier[] { new GroupClassifier(),
				new TextCharClassifier(),
				new TextWordClassifier(),
				new IconClassifier(),
				new TextBoxClassifier(),
				new ButtonClassifier(),
				new PopupClassifier() };
	}

	private final Classifier[] classifiers;

	private final Locators.Transform transform;

	public SimpleClassification(TextRecognizer<Blob> recognizer) {
		this(recognizer, getDefaultClassifiers());
	}

	public SimpleClassification(TextRecognizer<Blob> recognizer, Classifier[] classifiers) {
		this.transform = new Locators.Transform(recognizer);
		this.classifiers = Dependencies.order(classifiers);
	}

	@Override
	public Blob toBlob(Int image, Stages stages) {
		// context: additional information for classifiers
		Context context = new Context(image, image);

		// blobs: extract connected components
		Blob[] blobs = new GraphBlobBuilder(image.w, image.h).build(image);
		stages.done("blobs");

		// classification: widgets
		for (Classifier classifier : classifiers) {
			new VisitingMutator(classifier).mutate(context, blobs[0]);
		}
		stages.done("classifiers");

		return blobs[0];
	}

	@Override
	public Root toLocator(Image.Int image, Blob blob, Map<Locator, Blob> locatorToBlob, Map<Blob, Locator> blobToLocator) {
		// locators: map blob meta to locator hierarchy
		return transform.blobToLocator(image, blob, locatorToBlob, blobToLocator);
	}

	@Override
	public LinkedList<Locator> pathToLocator(Image.Int image, List<Blob> blobs) {
		return transform.pathToLocator(image, blobs);
	}
}
