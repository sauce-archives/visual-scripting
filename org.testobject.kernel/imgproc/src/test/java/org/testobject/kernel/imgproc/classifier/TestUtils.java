package org.testobject.kernel.imgproc.classifier;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Comparator;
import java.util.PriorityQueue;

import org.junit.Assert;
import org.testobject.kernel.imgproc.classifier.Classifier;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.commons.util.file.FileUtil;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.BlobUtils;
import org.testobject.kernel.imgproc.blob.GraphBlobBuilder;
import org.testobject.kernel.imgproc.blob.Meta;
import org.testobject.kernel.imgproc.blob.Visitor;
import org.testobject.kernel.imgproc.classifier.Classes.Widget;
import org.testobject.kernel.imgproc.classifier.TestUtils.Result.BlobMatch;
import org.testobject.kernel.imgproc.plot.VisualizerUtil;
import org.testobject.kernel.imgproc.classifier.Context;
import org.testobject.kernel.imgproc.classifier.VisitingMutator;

/**
 * 
 * @author enijkamp
 * 
 */
public class TestUtils {
	public static class PngFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return (name.endsWith(".png"));
		}
	}

	public static class Result<T extends Widget> {

		public static class BlobMatch {
			public final Classifier.Match match;
			public final Blob blob;
			public final Meta cls;

			public BlobMatch(Classifier.Match match, Blob blob, Meta cls) {
				this.match = match;
				this.blob = blob;
				this.cls = cls;
			}
		}

		public PriorityQueue<BlobMatch> widgets;
		public Blob blobs;

		public Result(PriorityQueue<BlobMatch> widgets, Blob blobs) {
			this.widgets = widgets;
			this.blobs = blobs;
		}
	}

	public static void testPositives(String path, Classifier[] before, Classifier classifier, boolean debug) throws Throwable {
		File dir = FileUtil.toFileFromSystem(path);
		for (File file : dir.listFiles(new PngFilter())) {
			// debug
			if (debug) {
				System.out.println(file);
			}

			// classify
			Result<?> result = classify(ImageUtil.read(file.toURI()), before, classifier, debug);

			// assert
			Assert.assertTrue(file.getName() + " -> false-negative", result.widgets.isEmpty() == false);
		}
	}
	
	public static void testNegatives(String path, Classifier[] before, Classifier classifier, boolean debug) throws Throwable {
		File dir = FileUtil.toFileFromSystem(path);
		for (File file : dir.listFiles(new PngFilter())) {
			// debug
			if (debug) {
				System.out.println(file);
			}

			// classify
			Result<?> result = classify(ImageUtil.read(file.toURI()), before, classifier, debug);

			// assert
			Assert.assertTrue(file.getName() + " -> false-postives", result.widgets.isEmpty() == true);
		}
	}

	public static <T extends Widget> Result<T> classify(final Image.Int image, final Classifier[] before, final Classifier classifier,
			boolean debug) throws Throwable {
		// context
		final Context context = new Context(image, image);

		// identify blobs
		Blob[] blobs = new GraphBlobBuilder(image.w, image.h).build(image);

		if (debug) {
			VisualizerUtil.show(BlobUtils.drawHierarchy(blobs));
			System.out.println("(initial hierarchy)");
			BlobUtils.printMeta(blobs[0]);
		}

		return classify(context, blobs[0], before, classifier, debug);
	}

	public static <T extends Widget> Result<T> classify(final Context context, Blob blob, final Classifier[] before,
			final Classifier classifier, boolean debug) throws Throwable {

		for (Classifier beforeClassifier : before) {
			new VisitingMutator(beforeClassifier).mutate(context, blob);
		}

		if (debug) {
			System.out.println("(input hierarchy)");
			BlobUtils.printMeta(blob);
		}

		// sort w.r.t. certainty
		Comparator<Result.BlobMatch> comparator = new Comparator<Result.BlobMatch>() {
			@Override
			public int compare(BlobMatch m1, BlobMatch m2) {
				return Double.compare(m2.match.getCertainty(), m1.match.getCertainty());
			}
		};
		final PriorityQueue<Result.BlobMatch> widgets = new PriorityQueue<>(10, comparator);

		// apply classifier
		class CollectingVisitor implements Visitor {
			@Override
			public void visit(Blob blob) {
				Classifier.Match match = classifier.match(context, blob);
				if (match.getCertainty() > 0f) {
					@SuppressWarnings("unchecked")
					T widget = (T) match.getWidget();
					widgets.add(new Result.BlobMatch(match, blob, widget));
				}
			}
		}
		BlobUtils.visit(blob, new CollectingVisitor());

		return new Result<>(widgets, blob);
	}
}
