package org.testobject.kernel.classification.matching;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.commons.util.io.FileUtil;
import org.testobject.kernel.api.classification.classifiers.Classifier.Context;
import org.testobject.kernel.api.classification.graph.Element;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.classification.graph.Locator.Descriptor;
import org.testobject.kernel.classification.classifiers.advanced.Image;
import org.testobject.kernel.classification.classifiers.advanced.MaskSegment.Classifier;
import org.testobject.kernel.classification.matching.Matcher.Match;
import org.testobject.kernel.imaging.segmentation.Blob;
import org.testobject.kernel.imaging.segmentation.BlobUtils;
import org.testobject.kernel.imaging.segmentation.GraphBlobBuilder;
import org.testobject.kernel.imaging.segmentation.Mask;

/**
 * 
 * @author enijkamp
 *
 */
public class ImageMatcherTest {
	
    public static final boolean debug = Debug.toDebugMode(false);
	
	@Test
	public void fuzzy() throws IOException {
		
		Descriptor descriptor0, descriptor1;

		org.testobject.commons.util.image.Image.Int train = readImage("train/dhl/images/jetzt_finden.mask.png");
		
		org.testobject.commons.util.image.Image.Int raw0 = readImage("replay/dhl/record/3.png");
		org.testobject.commons.util.image.Image.Int raw1 = readImage("replay/dhl/replay/3.png");
		
		// 0
		{
			Rectangle.Int box0 = new Rectangle.Int(107, 597, 126, 35);
			Mask mask0 = Mask.Builder.create(train, box0);
			
			Image.Classifier classifier = new Image.Trainer().train(Lists.toList("jetzt_finden"), Lists.toList(train));
			
			Classifier.Images images = Classifier.Images.Factory.create(raw0);
			Classifier.Lookup lookup = Classifier.Lookup.Factory.blob();
			
			Element element = classifier.classify(images, lookup, Lists.toList(mask0), Context.Factory.none()).get(0).element();
			descriptor0 = Locator.Descriptor.Factory.toDescriptor(0, element);
		}
		
		// 1
		{
			Blob[] blobs = segment(raw1);
			List<Blob> masks = BlobUtils.Locate.locate(blobs[0], new Rectangle.Int(44, 666, 140, 45));
			
			Image.Classifier classifier = new Image.Trainer().train(Lists.toList("jetzt_finden"), Lists.toList(train));
			
			Classifier.Images images = Classifier.Images.Factory.create(raw1);
			Classifier.Lookup lookup = Classifier.Lookup.Factory.blob();
			
			Element element = classifier.classify(images, lookup, Lists.<Mask>castList(masks), Context.Factory.none()).get(0).element();
			descriptor1 = Locator.Descriptor.Factory.toDescriptor(0, element);
		}
		
		// assert
		{
			Matcher.Context context = Matcher.Context.Factory.create(new org.testobject.commons.util.image.Image.Int(480, 800));
			
			Image.Matcher matcher = new Image.Matcher();
			
			Match match = matcher.match(descriptor0, context, descriptor1, context);
			
			print(descriptor0, descriptor1, match);
			
			assertThat(match.getProbability(), is(closeTo(0.95d, 1e-2)));
		}
		
	}
	
	private void print(Descriptor descriptor0, Descriptor descriptor1, Match match) {
		Locator.Printer.print(descriptor0, System.out);
		System.out.println();
		Locator.Printer.print(descriptor1, System.out);
		System.out.println();
		System.out.println(match.getProbability());
		System.out.println(match.getComponents());
	}
	
    private static Blob[] segment(org.testobject.commons.util.image.Image.Int image) {
        float sigma = 0.5f;
        double threshold = 4500d;
		return new GraphBlobBuilder(image.w, image.h, sigma, threshold).build(image);
	}
	
	private static org.testobject.commons.util.image.Image.Int readImage(String file) throws IOException {
		return ImageUtil.Read.read(FileUtil.readFileFromClassPath("android/4_0_3/" + file));
	}

}
