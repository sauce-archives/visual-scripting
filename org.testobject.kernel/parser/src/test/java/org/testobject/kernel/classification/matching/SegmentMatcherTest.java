package org.testobject.kernel.classification.matching;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.tools.plot.VisualizerUtil;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.commons.util.io.FileUtil;
import org.testobject.kernel.api.classification.classifiers.Classifier.Context;
import org.testobject.kernel.api.classification.graph.Element;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.api.classification.graph.Locator.Descriptor;
import org.testobject.kernel.api.util.VariableUtil;
import org.testobject.kernel.classification.classifiers.advanced.MaskSegment;
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
public class SegmentMatcherTest {
	
    public static final boolean debug = Debug.toDebugMode(false);
	
	@Test
	public void exact() throws IOException {
		
		// image
		org.testobject.commons.util.image.Image.Int image = ImageUtil.expand(readImage("classifiers/image/twitter/segments/at.png"), 1, 1, 2, 2);
		
		// segment
		Blob[] blobs = segment(image);
		Mask mask = BlobUtils.Locate.locate(blobs[0], new org.testobject.commons.math.algebra.Rectangle.Int(1, 1, image.w - 2 , image.h - 2)).get(0);

		// classify
		MaskSegment.Classifier classifier = new MaskSegment.Classifier();
		Classifier.Images images = Classifier.Images.Factory.create(image);
		Classifier.Lookup lookup = Classifier.Lookup.Factory.blob();
		Node graph = Node.Factory.create(Element.Factory.node("root", new Rectangle.Int(0, 0, image.w, image.h)));
        for(Classifier.Proposal proposal : classifier.classify(images, lookup, Lists.toList(mask), Context.Factory.none())) {
        	graph.getChildren().add(org.testobject.kernel.api.classification.graph.Node.Factory.create(proposal.element()));
        }
        
        if(debug) {
        	org.testobject.kernel.classification.graph.Printer.print(graph);
            VisualizerUtil.show("blobs", BlobUtils.Draw.drawHierarchy(blobs), 8f);
            VisualizerUtil.show("blob", BlobUtils.Cut.cutByMask(image, mask), 8f);
        }

        // element
		assertThat(graph.getChildren().size(), is(1));
		Node child = graph.getChildren().get(0);
		Element element = child.getElement();
		
		// assert
		{
			Descriptor descriptor = Locator.Descriptor.Factory.toDescriptor(0, element);
			Matcher.Context context = Matcher.Context.Factory.create(images.raw());
			
			MaskSegment.Matcher matcher = new MaskSegment.Matcher();
			
			Match match = matcher.match(descriptor, context, descriptor, context);
			print(descriptor, descriptor, match);
			
			assertThat(match.getProbability(), is(closeTo(1.0d, 1e-10)));
		}
	}
	
	@Test
	public void fuzzy() throws IOException {
		
		Descriptor descriptor0, descriptor1;
		
		final int fuzzyPosition = 10;
		
		// 0
		{
			org.testobject.commons.util.image.Image.Int image = readImage("matching/segments/twitter/fuzzy/0.png");			
			Rectangle.Int box = new Rectangle.Int(0, 0, image.w, image.h);
			Mask mask = Mask.Builder.create(image, box);

			MaskSegment.Classifier classifier = new MaskSegment.Classifier();
			Classifier.Images images = Classifier.Images.Factory.create(image);
			Classifier.Lookup lookup = Classifier.Lookup.Factory.blob();
			
			Element element = classifier.classify(images, lookup, Lists.toList(mask), Context.Factory.none()).get(0).element();
			descriptor0 = Locator.Descriptor.Factory.toDescriptor(0, element);			
		}
		
		// 1
		{
			org.testobject.commons.util.image.Image.Int image = readImage("matching/segments/twitter/fuzzy/1.png");			
			Rectangle.Int box = new Rectangle.Int(0, 0, image.w, image.h);
			Mask mask = Mask.Builder.create(image, box);

			MaskSegment.Classifier classifier = new MaskSegment.Classifier();
			Classifier.Images images = Classifier.Images.Factory.create(image);
			Classifier.Lookup lookup = Classifier.Lookup.Factory.blob();
			
			Element element = classifier.classify(images, lookup, Lists.toList(mask), Context.Factory.none()).get(0).element();
			descriptor1 = Locator.Descriptor.Factory.toDescriptor(0, element);	
		}
		
		// fuzzy
		{
			VariableUtil.getPosition(descriptor1.getFeatures()).x += fuzzyPosition;
			VariableUtil.getPosition(descriptor1.getFeatures()).y += fuzzyPosition;
		}
		
		// assert
		{
			Matcher.Context context = Matcher.Context.Factory.create(new org.testobject.commons.util.image.Image.Int(480, 800));
			
			MaskSegment.Matcher matcher = new MaskSegment.Matcher();
			
			Match match = matcher.match(descriptor0, context, descriptor1, context);
			print(descriptor0, descriptor1, match);
			
			assertThat(match.getProbability(), is(closeTo(0.62d, 1e-2)));
		}
	}
	
	@Test
	public void failAralLineFav() throws IOException {
		
		Descriptor line = line();
		Descriptor fav = fav();
		
		// assert
		{
			Matcher.Context context = Matcher.Context.Factory.create(new org.testobject.commons.util.image.Image.Int(480, 800));
			
			MaskSegment.Matcher matcher = new MaskSegment.Matcher();
			
			Match match = matcher.match(line, context, fav, context);
			print(line, fav, match);
			
			assertThat(match.getProbability(), is(closeTo(0.15d, 1e-2)));
		}
	}
	
	@Test
	public void failAralMapFav() throws IOException {
		
		Descriptor map = map();
		Descriptor fav = fav();
			
		// assert
		{
			Matcher.Context context = Matcher.Context.Factory.create(new org.testobject.commons.util.image.Image.Int(480, 800));
			
			MaskSegment.Matcher matcher = new MaskSegment.Matcher();
			
			Match match = matcher.match(map, context, fav, context);
			print(map, fav, match);
			
			assertThat(match.getProbability(), is(closeTo(0.45d, 1e-2)));
		}
		
	}
	
	@Test
	public void failAralMapList() throws IOException {
		
		Descriptor map = map();
		Descriptor list = list();
			
		// assert
		{
			Matcher.Context context = Matcher.Context.Factory.create(new org.testobject.commons.util.image.Image.Int(480, 800));
			
			MaskSegment.Matcher matcher = new MaskSegment.Matcher();
			
			Match match = matcher.match(map, context, list, context);
			print(map, list, match);
			
			assertThat(match.getProbability(), is(closeTo(0.53d, 1e-2)));
		}
		
	}
	
	@Test
	public void failAppsBookmark() throws IOException {
		
		Descriptor map = map();
		Descriptor list = list();
			
		// assert
		{
			Matcher.Context context = Matcher.Context.Factory.create(new org.testobject.commons.util.image.Image.Int(480, 800));
			
			MaskSegment.Matcher matcher = new MaskSegment.Matcher();
			
			Match match = matcher.match(map, context, list, context);
			print(map, list, match);
			
			assertThat(match.getProbability(), is(closeTo(0.53d, 1e-2)));
		}
		
	}
	
	private static Descriptor list() throws IOException {
		
		String image = "replay/aral/1/before.raw.png";
		Rectangle.Int box = new Rectangle.Int(419 - 1, 57 - 1, 26 + 2, 33 + 2);
		
		return descriptor(image, box);
	}
	
	private static Descriptor map() throws IOException {
		
		String image = "replay/aral/2/before.raw.png";
		Rectangle.Int box = new Rectangle.Int(413 - 1, 57 - 1, 37 + 2, 33 + 2);
		
		return descriptor(image, box);
	}

	private static Descriptor fav() throws IOException {
		
		String image = "replay/aral/4/before.raw.png";
		Rectangle.Int box = new Rectangle.Int(422 - 2, 52 - 2, 48 + 4, 48 + 4);
		
		return descriptor(image, box);
	}
	
	private static Descriptor line() throws IOException {
		
		String image = "replay/aral/3/before.raw.png";
		Rectangle.Int box = new Rectangle.Int(240 - 2, 128 - 2, 1 + 4, 36 + 4);
		
		return descriptor(image, box);
	}
	
	private static Descriptor descriptor(String file, Rectangle.Int box) throws IOException {
		// user-defined
		Image.Int image = readImage(file);
		
		// masks
		Blob[] blobs = segment(image);
		List<Mask> masks = Lists.castList(BlobUtils.Locate.locate(blobs[0], box));
		
		if(debug) {
			VisualizerUtil.show("union", BlobUtils.Cut.cutByMask(image, Mask.Builder.create(masks)));
		}
		
		// classifier
		MaskSegment.Classifier classifier = new MaskSegment.Classifier();
		Classifier.Images images = Classifier.Images.Factory.create(image);
		Classifier.Lookup lookup = Classifier.Lookup.Factory.blob();
		
		// locator
		Element element = classifier.classify(images, lookup, masks, Context.Factory.none()).get(0).element();
		return Locator.Descriptor.Factory.toDescriptor(0, element);
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
        float sigma = 0f;
        double threshold = 50000d;
		return new GraphBlobBuilder(image.w, image.h, sigma, threshold).build(image);
	}
	
	private static org.testobject.commons.util.image.Image.Int readImage(String file) throws IOException {
		return ImageUtil.Read.read(FileUtil.readFileFromClassPath("android/4_0_3/" + file));
	}

}
