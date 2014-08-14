package org.testobject.kernel.classification.classifiers;

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
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.commons.util.io.FileUtil;
import org.testobject.kernel.api.classification.classifiers.Classifier;
import org.testobject.kernel.api.classification.classifiers.Classifier.Context;
import org.testobject.kernel.api.classification.graph.Element;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.classification.classifiers.advanced.Image;
import org.testobject.kernel.imaging.fingerprint.ImageFingerprint;
import org.testobject.kernel.imaging.procedural.Printer;
import org.testobject.kernel.imaging.procedural.Util;
import org.testobject.kernel.imaging.segmentation.Blob;
import org.testobject.kernel.imaging.segmentation.BlobUtils;
import org.testobject.kernel.imaging.segmentation.GraphBlobBuilder;
import org.testobject.kernel.imaging.segmentation.Mask;

/**
 *
 * @author enijkamp
 *
 */
public class ImageClassifierTest {

    public static final boolean debug = Debug.toDebugMode(false);
	
	@Test
	public void toRenderGraphAt() throws IOException {
		
		org.testobject.commons.util.image.Image.Int image = readImage("twitter/segments/at.png");
		
		org.testobject.kernel.imaging.procedural.Node graph = Image.Trainer.toRenderGraph(image);
		
		Printer.print(graph);
		
		render(graph);

        if(debug) System.in.read();
	}
	
	@Test
	public void classifyAt() throws IOException {
		
		org.testobject.commons.util.image.Image.Int image = ImageUtil.expand(readImage("twitter/segments/at.png"), 1, 1, 2, 2);
		
		Image.Trainer trainer = new Image.Trainer();
		
		Image.Classifier classifier = trainer.train(Lists.toList("at"), readImages("twitter/segments/at.png"));
		
		Classifier.Images images = Classifier.Images.Factory.create(image);
		Classifier.Lookup lookup = Classifier.Lookup.Factory.blob();
		
		Blob[] blobs = segment(image);
		Mask mask = BlobUtils.Locate.locate(blobs[0], new org.testobject.commons.math.algebra.Rectangle.Int(1, 1, image.w - 2 , image.h - 2)).get(0);

        if(debug) {
            VisualizerUtil.show("blobs", BlobUtils.Draw.drawHierarchy(blobs), 8f);
            VisualizerUtil.show("blob", BlobUtils.Cut.cutByMask(image, mask), 8f);
        }
		
        Node graph = Node.Factory.create(Element.Factory.node("root", new Rectangle.Int(0, 0, image.w, image.h)));
        for(Classifier.Proposal proposal : classifier.classify(images, lookup, Lists.toList(mask), Context.Factory.none())) {
        	graph.getChildren().add(org.testobject.kernel.api.classification.graph.Node.Factory.create(proposal.element()));
        }
		
		org.testobject.kernel.classification.graph.Printer.print(graph);

		// assert
		{
			assertThat(graph.getChildren().size(), is(1));
			Node child = graph.getChildren().get(0);
			Element element = child.getElement();
			assertThat(element.getLabel().getLikelihood().geometric(), is(closeTo(1.0d, 1e-10)));
			assertThat(element.getLabel().getLikelihood().photometric(), is(closeTo(1.0d, 1e-10)));
		}

        if(debug) System.in.read();
	}
	
	@Test
	public void classifyNotAt() throws IOException {
		
		// input
		org.testobject.commons.util.image.Image.Int image = ImageUtil.expand(readImage("system/segments/2.png"), 1, 1, 2, 2);

		// segment
		Blob[] blobs = segment(image);
		Mask mask = BlobUtils.Locate.locate(blobs[0], new org.testobject.commons.math.algebra.Rectangle.Int(1, 1, image.w - 2 , image.h - 2)).get(0);

        if(debug) {
            VisualizerUtil.show("blobs", BlobUtils.Draw.drawHierarchy(blobs), 8f);
            VisualizerUtil.show("blob", BlobUtils.Cut.cutByMask(image, mask), 8f);
        }
		
        // classify
		Image.Trainer trainer = new Image.Trainer();
		Image.Classifier classifier = trainer.train(Lists.toList("at"), readImages("twitter/segments/at.png"));
		Classifier.Images images = Classifier.Images.Factory.create(image);
		Classifier.Lookup lookup = Classifier.Lookup.Factory.blob();
        Node graph = Node.Factory.create(Element.Factory.node("root", new Rectangle.Int(0, 0, image.w, image.h)));
        for(Classifier.Proposal proposal : classifier.classify(images, lookup, Lists.toList(mask), Context.Factory.none())) {
        	graph.getChildren().add(org.testobject.kernel.api.classification.graph.Node.Factory.create(proposal.element()));
        }
		
		org.testobject.kernel.classification.graph.Printer.print(graph);

		// assert
		{
			assertThat(graph.getChildren().size(), is(1));
			Node child = graph.getChildren().get(0);
			Element element = child.getElement();
			assertThat(element.getLabel().getLikelihood().geometric(), is(closeTo(0.68d, 1e-2)));
			assertThat(element.getLabel().getLikelihood().photometric(), is(closeTo(0.31d, 1e-2)));
		}

        if(debug) System.in.read();
	}
	
	@Test
	public void komootMap() throws IOException {
		
		// input
		org.testobject.commons.util.image.Image.Int image = readImage("komoot/screenshots/menu.png");

		// FIXME icon-classifier does not use locateWithChilds with childs ... unify (en)
		// segment
		Blob[] blobs = segment(image);
		List<Blob> masks = BlobUtils.Locate.locateWithChilds(blobs[0], new Rectangle.Int(315, 145, 80, 80));

        if(debug) {
            VisualizerUtil.show("blobs", BlobUtils.Draw.drawHierarchy(blobs), 8f);
            VisualizerUtil.show("masks", BlobUtils.Cut.cutByMask(image, Mask.Builder.create(masks)), 8f);
        }
		
        // classify
		Image.Trainer trainer = new Image.Trainer();
		Image.Classifier classifier = trainer.train(Lists.toList("map"), readImages("komoot/segments/map.png"));
		Classifier.Images images = Classifier.Images.Factory.create(image);
		Classifier.Lookup lookup = Classifier.Lookup.Factory.blob();
        Node graph = Node.Factory.create(Element.Factory.node("root", new Rectangle.Int(0, 0, image.w, image.h)));
        for(Classifier.Proposal proposal : classifier.classify(images, lookup, Lists.<Mask>castList(masks), Context.Factory.none())) {
        	graph.getChildren().add(org.testobject.kernel.api.classification.graph.Node.Factory.create(proposal.element()));
        }
		
		org.testobject.kernel.classification.graph.Printer.print(graph);

		// assert
		{
			assertThat(graph.getChildren().size(), is(1));
			Node child = graph.getChildren().get(0);
			Element element = child.getElement();
			assertThat(element.getLabel().getLikelihood().geometric(), is(closeTo(0.98d, 1e-2)));
			assertThat(element.getLabel().getLikelihood().photometric(), is(closeTo(0.99d, 1e-2)));
		}

        if(debug) System.in.read();
	}
	
	@Test
	public void testLikelihoodSameImage() throws IOException {
		org.testobject.commons.util.image.Image.Int navContourImage = readImage("komoot/segments/nav.contour.png");

		ImageFingerprint navContourFingerPrint = new ImageFingerprint(navContourImage, 0xf2, 0xf1, 0xf0);

		double likelihood = Image.Classifier.photometric(navContourFingerPrint, navContourFingerPrint);
		assertThat(likelihood, is(1.0));
	}

	@Test
	public void testLikelihoodNotSameImage() throws IOException {
		org.testobject.commons.util.image.Image.Int navContourImage = readImage("komoot/segments/nav.contour.png");
		org.testobject.commons.util.image.Image.Int navMaskImage = readImage("komoot/segments/nav.mask.png");

		ImageFingerprint navContourFingerPrint = new ImageFingerprint(navContourImage, 0xf2, 0xf1, 0xf0);
		ImageFingerprint navMaskFingerPrint = new ImageFingerprint(navMaskImage, 0xf2, 0xf1, 0xf0);

		double likelihood = Image.Classifier.photometric(navContourFingerPrint, navMaskFingerPrint);
		assertThat(likelihood, is(closeTo(0.9d, 1e-1)));
	}

	@Test
	public void testClassifier() throws IOException {
		org.testobject.commons.util.image.Image.Int raw = readImage("komoot/segments/nav.png");

		if (debug) {
			VisualizerUtil.show(raw);
		}

		Classifier.Images images = Classifier.Images.Factory.create(raw);
		Classifier.Lookup lookup = Classifier.Lookup.Factory.blob();

		// FIXME icon-classifier does not use locateWithChilds with childs ... unify (en)
		Blob[] blobs = segment(raw);
		List<Blob> masks = BlobUtils.Locate.locateWithChilds(blobs[0], new Rectangle.Int(1, 1, raw.w - 2, raw.h - 2));
		
		if(debug) {
			VisualizerUtil.show("mask", BlobUtils.Cut.cutByMask(raw, Mask.Builder.create(masks)));
		}

		Classifier classifier = new Image.Trainer().train(Lists.toList("nav"), Lists.toList(readImage("komoot/segments/nav.contour.png")));
		
		org.testobject.kernel.api.classification.graph.Node graph = org.testobject.kernel.api.classification.graph.Node.Factory.create(Element.Factory.node("root", new Rectangle.Int(0, 0, raw.w, raw.h)));
		for (Classifier.Proposal proposal : classifier.classify(images, lookup, Lists.<Mask>castList(masks), Context.Factory.none())) {
        	graph.getChildren().add(org.testobject.kernel.api.classification.graph.Node.Factory.create(proposal.element()));
        }

		org.testobject.kernel.classification.graph.Printer.print(graph);

		// assert
		{
			assertThat(graph.getChildren().size(), is(1));
			org.testobject.kernel.api.classification.graph.Node child = graph.getChildren().get(0);
			Element element = child.getElement();
			assertThat(element.getLabel().getLikelihood().geometric(), is(closeTo(1d, 1e-2)));
			assertThat(element.getLabel().getLikelihood().photometric(), is(closeTo(0.93d, 1e-2)));
		}

		if (debug)
			System.in.read();
	}

    private static Blob[] segment(org.testobject.commons.util.image.Image.Int image) {
        float sigma = 0.5f;
        double threshold = 4500d;
		return new GraphBlobBuilder(image.w, image.h, sigma, threshold).build(image);
	}
	
	private static List<org.testobject.commons.util.image.Image.Int> readImages(String ... files) throws IOException {
		List<org.testobject.commons.util.image.Image.Int> images = Lists.newArrayList(files.length);
		for(String file : files) {
			images.add(readImage(file));
		}
		return images;
	}
	
	private static org.testobject.commons.util.image.Image.Int readImage(String file) throws IOException {
		return ImageUtil.Read.read(FileUtil.readFileFromClassPath("android/4_0_3/classifiers/image/" + file));
	}
	
	private static void render(org.testobject.kernel.imaging.procedural.Node node) {
        if(debug) VisualizerUtil.show("synthetic", Util.render(node), 8);
	}

}
