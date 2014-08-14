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
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.commons.util.io.FileUtil;
import org.testobject.kernel.api.classification.classifiers.Classifier;
import org.testobject.kernel.api.classification.classifiers.Classifier.Context;
import org.testobject.kernel.api.classification.graph.Element;
import org.testobject.kernel.classification.classifiers.advanced.Icon;
import org.testobject.kernel.imaging.procedural.Node;
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
// TODO bug - geometric likelihood between region / tour icon and arbitraty second icon is > 0.7 (en)
public class IconClassifierTest {

    public static final boolean debug = Debug.toDebugMode(false);
	
	@Test
	public void toRenderGraphAt() throws IOException {
		
		Image.Int image = readImage("twitter/segments/at.png");
		
		float sigma = 0.5f;
		double threshold = 4500d;
		
		Node graph = Icon.Trainer.toRenderGraph(image, sigma, threshold);
		
		Printer.print(graph);
		
		render(graph);

        if(debug) System.in.read();
	}

	@Test
	public void toRenderGraphRegions() throws IOException {
		
		Image.Int image = readImage("komoot/segments/regions.png");
		
		// float sigma = 0.425f;
		// double threshold = 5800d;
		
		float sigma = 0.5f;
		double threshold = 4500d;
		
		Node graph = Icon.Trainer.toRenderGraph(image, sigma, threshold);
		
		Printer.print(graph);
		
		render(graph);

        if(debug) System.in.read();
	}

    @Test
    public void toRenderGraphTours() throws IOException {

        Image.Int image = readImage("komoot/segments/tour.png");

        float sigma = 0.5f;
        double threshold = 4500d;

        Node graph = Icon.Trainer.toRenderGraph(image, sigma, threshold);

        Printer.print(graph);

        render(graph);

        if(debug) System.in.read();
    }
	
	@Test
	public void classifyAt() throws IOException {
		
		Image.Int image = ImageUtil.expand(readImage("twitter/segments/at.png"), 1, 1, 2, 2);
		
		Icon.Trainer trainer = new Icon.Trainer();
		
		Icon.Classifier classifier = trainer.train(Lists.toList("at"), readImages("twitter/segments/at.png"));
		
		Classifier.Images images = Classifier.Images.Factory.create(image);
		
		Classifier.Lookup lookup = Classifier.Lookup.Factory.blob();
		
		Blob[] blobs = segment(image);
		Mask mask = BlobUtils.Locate.locate(blobs[0], new org.testobject.commons.math.algebra.Rectangle.Int(1, 1, image.w - 2 , image.h - 2)).get(0);

        if(debug) {
            VisualizerUtil.show("blobs", BlobUtils.Draw.drawHierarchy(blobs), 8f);
            VisualizerUtil.show("blob", BlobUtils.Cut.cutByMask(image, mask), 8f);
        }
		
        org.testobject.kernel.api.classification.graph.Node graph = org.testobject.kernel.api.classification.graph.Node.Factory.create(Element.Factory.node("root", new Rectangle.Int(0, 0, image.w, image.h)));
        for(Classifier.Proposal proposal : classifier.classify(images, lookup, Lists.toList(mask), Context.Factory.none())) {
        	graph.getChildren().add(org.testobject.kernel.api.classification.graph.Node.Factory.create(proposal.element()));
        }
        
		org.testobject.kernel.classification.graph.Printer.print(graph);

		// assert
		{
			assertThat(graph.getChildren().size(), is(1));
			org.testobject.kernel.api.classification.graph.Node child = graph.getChildren().get(0);
			Element element = child.getElement();
			assertThat(element.getLabel().getLikelihood().geometric(), is(closeTo(1.0d, 1e-10)));
			assertThat(element.getLabel().getLikelihood().photometric(), is(closeTo(0.7d, 2e-2)));
		}

        if(debug) System.in.read();
	}
	
	@Test
	public void classifyRegions() throws IOException {
		
		Image.Int image = ImageUtil.expand(readImage("komoot/screenshots/menu.png"), 1, 1, 2, 2);
		
		Icon.Trainer trainer = new Icon.Trainer();
		
		Icon.Classifier classifier = trainer.train(Lists.toList("regions"), readImages("komoot/segments/regions.png"));
		
		Classifier.Images images = Classifier.Images.Factory.create(image);
		
		Classifier.Lookup lookup = Classifier.Lookup.Factory.blob();

        Blob[] blobs = segment(image);

        List<Blob> masks = BlobUtils.Locate.locate(blobs[0], new org.testobject.commons.math.algebra.Rectangle.Int(315, 350, 80, 80));

        if(debug) {
            VisualizerUtil.show("blobs", BlobUtils.Draw.drawHierarchy(blobs), 1f);
            VisualizerUtil.show("blob", BlobUtils.Cut.cutByMask(image, Mask.Builder.create(masks)), 8f);
        }

        org.testobject.kernel.api.classification.graph.Node graph = org.testobject.kernel.api.classification.graph.Node.Factory.create(Element.Factory.node("root", new Rectangle.Int(0, 0, image.w, image.h)));
        for(Classifier.Proposal proposal : classifier.classify(images, lookup, Lists.<Mask>castList(masks), Context.Factory.none())) {
        	graph.getChildren().add(org.testobject.kernel.api.classification.graph.Node.Factory.create(proposal.element()));
        }

        org.testobject.kernel.classification.graph.Printer.print(graph);
        
		// assert
		{
			assertThat(graph.getChildren().size(), is(1));
			org.testobject.kernel.api.classification.graph.Node child = graph.getChildren().get(0);
			Element element = child.getElement();
			assertThat(element.getLabel().getLikelihood().geometric(), is(closeTo(1.0d, 1e-10)));
			assertThat(element.getLabel().getLikelihood().photometric(), is(closeTo(0.88d, 1e-2)));
		}

        if(debug) System.in.read();
	}

    @Test
    public void classifyRegionsMismatch() throws IOException {

        Image.Int image = ImageUtil.expand(readImage("komoot/screenshots/menu.png"), 1, 1, 2, 2);

        Icon.Trainer trainer = new Icon.Trainer();

        Icon.Classifier classifier = trainer.train(Lists.toList("regions"), readImages("komoot/segments/regions.png"));

        Classifier.Images images = Classifier.Images.Factory.create(image);

        Classifier.Lookup lookup = Classifier.Lookup.Factory.blob();

        Blob[] blobs = segment(image);

        List<Blob> masks = BlobUtils.Locate.locate(blobs[0], new org.testobject.commons.math.algebra.Rectangle.Int(422, 8, 16, 22));

        if(debug) {
            VisualizerUtil.show("blobs", BlobUtils.Draw.drawHierarchy(blobs), 1f);
            VisualizerUtil.show("blob", BlobUtils.Cut.cutByMask(image, Mask.Builder.create(masks)), 8f);
        }

        org.testobject.kernel.api.classification.graph.Node graph = org.testobject.kernel.api.classification.graph.Node.Factory.create(Element.Factory.node("root", new Rectangle.Int(0, 0, image.w, image.h)));
        for(Classifier.Proposal proposal : classifier.classify(images, lookup, Lists.<Mask>castList(masks), Context.Factory.none())) {
        	graph.getChildren().add(org.testobject.kernel.api.classification.graph.Node.Factory.create(proposal.element()));
        }

        org.testobject.kernel.classification.graph.Printer.print(graph);

        // assert
        {
            assertThat(graph.getChildren().size(), is(1));
            org.testobject.kernel.api.classification.graph.Node child = graph.getChildren().get(0);
            Element element = child.getElement();
            assertThat(element.getLabel().getLikelihood().geometric(), is(closeTo(0.57d, 1e-1)));
            assertThat(element.getLabel().getLikelihood().photometric(), is(closeTo(0.43d, 1e-1)));
        }

        if(debug) System.in.read();
    }

    @Test
    public void classifyTours() throws IOException {

        Image.Int image = ImageUtil.expand(readImage("komoot/screenshots/menu.png"), 1, 1, 2, 2);

        Icon.Trainer trainer = new Icon.Trainer();

        Icon.Classifier classifier = trainer.train(Lists.toList("tour"), readImages("komoot/segments/tour.png"));

        Classifier.Images images = Classifier.Images.Factory.create(image);

        Classifier.Lookup lookup = Classifier.Lookup.Factory.blob();

        Blob[] blobs = segment(image);

        List<Blob> masks = BlobUtils.Locate.locate(blobs[0], new org.testobject.commons.math.algebra.Rectangle.Int(75, 145, 80, 80));

        if(debug) {
            VisualizerUtil.show("blobs", BlobUtils.Draw.drawHierarchy(blobs), 1f);
            VisualizerUtil.show("blob", BlobUtils.Cut.cutByMask(image, Mask.Builder.create(masks)), 8f);
        }

        org.testobject.kernel.api.classification.graph.Node graph = org.testobject.kernel.api.classification.graph.Node.Factory.create(Element.Factory.node("root", new Rectangle.Int(0, 0, image.w, image.h)));
        for(Classifier.Proposal proposal : classifier.classify(images, lookup, Lists.<Mask>castList(masks), Context.Factory.none())) {
        	graph.getChildren().add(org.testobject.kernel.api.classification.graph.Node.Factory.create(proposal.element()));
        }

        org.testobject.kernel.classification.graph.Printer.print(graph);

        // assert
        {
            assertThat(graph.getChildren().size(), is(1));
            org.testobject.kernel.api.classification.graph.Node child = graph.getChildren().get(0);
            Element element = child.getElement();
            assertThat(element.getLabel().getLikelihood().geometric(), is(closeTo(1d, 1e-2)));
            assertThat(element.getLabel().getLikelihood().photometric(), is(closeTo(0.75d, 1e-2)));
        }

        if(debug) System.in.read();
    }

    @Test
    public void classifyMap() throws IOException {

        Image.Int image = ImageUtil.expand(readImage("komoot/screenshots/menu.png"), 1, 1, 2, 2);

        Icon.Trainer trainer = new Icon.Trainer();

        Icon.Classifier classifier = trainer.train(Lists.toList("map"), readImages("komoot/segments/map.png"));

        Classifier.Images images = Classifier.Images.Factory.create(image);

        Classifier.Lookup lookup = Classifier.Lookup.Factory.blob();

        Blob[] blobs = segment(image);

        List<Blob> masks = BlobUtils.Locate.locate(blobs[0], new org.testobject.commons.math.algebra.Rectangle.Int(315, 145, 80, 80));

        if(debug) {
            VisualizerUtil.show("blobs", BlobUtils.Draw.drawHierarchy(blobs), 1f);
            VisualizerUtil.show("blob", BlobUtils.Cut.cutByMask(image, Mask.Builder.create(masks)), 8f);
        }

        org.testobject.kernel.api.classification.graph.Node graph = org.testobject.kernel.api.classification.graph.Node.Factory.create(Element.Factory.node("root", new Rectangle.Int(0, 0, image.w, image.h)));
        for(Classifier.Proposal proposal : classifier.classify(images, lookup, Lists.<Mask>castList(masks), Context.Factory.none())) {
        	graph.getChildren().add(org.testobject.kernel.api.classification.graph.Node.Factory.create(proposal.element()));
        }

        org.testobject.kernel.classification.graph.Printer.print(graph);

        // assert
        {
            assertThat(graph.getChildren().size(), is(1));
            org.testobject.kernel.api.classification.graph.Node child = graph.getChildren().get(0);
            Element element = child.getElement();
            assertThat(element.getLabel().getLikelihood().geometric(), is(closeTo(1d, 1e-2)));
            assertThat(element.getLabel().getLikelihood().photometric(), is(closeTo(0.88d, 1e-2)));
        }

        if(debug) System.in.read();
    }

    private static Blob[] segment(Image.Int image) {
        float sigma = 0.5f;
        double threshold = 4500d;
		return new GraphBlobBuilder(image.w, image.h, sigma, threshold).build(image);
	}

	private static List<Image.Int> readImages(String ... files) throws IOException {
		List<Image.Int> images = Lists.newArrayList(files.length);
		for(String file : files) {
			images.add(readImage(file));
		}
		return images;
	}
	
	private static org.testobject.commons.util.image.Image.Int readImage(String file) throws IOException {
		return ImageUtil.Read.read(FileUtil.readFileFromClassPath("android/4_0_3/classifiers/icon/" + file));
	}
	
	private static void render(Node node) {
        if(debug) VisualizerUtil.show("synthetic", Util.render(node), 8);
	}

}
