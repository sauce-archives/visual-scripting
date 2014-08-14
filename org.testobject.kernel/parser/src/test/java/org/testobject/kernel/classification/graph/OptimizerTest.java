package org.testobject.kernel.classification.graph;

import org.junit.Test;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.tools.plot.VisualizerUtil;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.commons.util.io.FileUtil;
import org.testobject.commons.util.tree.r.RTree;
import org.testobject.kernel.api.classification.classifiers.Classifier;
import org.testobject.kernel.api.classification.classifiers.Classifier.Context;
import org.testobject.kernel.api.classification.graph.Element;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.classification.classifiers.advanced.Icon;
import org.testobject.kernel.classification.graph.Optimizer;
import org.testobject.kernel.classification.graph.Printer;
import org.testobject.kernel.imaging.procedural.Util;
import org.testobject.kernel.imaging.segmentation.Blob;
import org.testobject.kernel.imaging.segmentation.BlobUtils;
import org.testobject.kernel.imaging.segmentation.GraphBlobBuilder;
import org.testobject.kernel.imaging.segmentation.Mask;

import java.io.IOException;
import java.util.List;

/**
 * @author enijkamp
 */
public class OptimizerTest {

    public static final boolean debug = Debug.toDebugMode(false);

    @Test
    public void optimize() throws IOException {

        Image.Int image = ImageUtil.expand(readImage("test/komoot/framebuffer/menu.png"), 1, 1, 2, 2);
        GraphBlobBuilder builder = new GraphBlobBuilder(image.w, image.h);
        Blob[] blobs = builder.build(image);

        org.testobject.kernel.classification.classifiers.advanced.Icon.Trainer trainer = new Icon.Trainer();
        Icon.Classifier classifier = trainer.train(Lists.toList("region", "tour"), readImages("test/komoot/train/images/regions.png", "test/komoot/train/images/tour.png"));
        Classifier.Images images = Classifier.Images.Factory.create(image);
        Classifier.Lookup lookup = Classifier.Lookup.Factory.blob();

        List<Blob> regionBlobs = BlobUtils.Locate.locate(blobs[0], new org.testobject.commons.math.algebra.Rectangle.Int(315, 350, 80, 80));

        Node labels = Node.Factory.create(Element.Factory.node("root", new Rectangle.Int(0, 0, image.w, image.h)));
        for(Classifier.Proposal proposal : classifier.classify(images, lookup, Lists.<Mask>castList(regionBlobs), Context.Factory.none())) {
        	labels.getChildren().add(org.testobject.kernel.api.classification.graph.Node.Factory.create(proposal.element()));
        }

        Printer.print(labels);

        Optimizer.Greedy<Node> optimizer = new Optimizer.Greedy<Node>(new RTree.Factory<Node>(), new Optimizer.LabelAdapter<Node>() {
            @Override
            public org.testobject.commons.math.algebra.Rectangle.Double getBoundingBox(Node contestant) {
                return toDoubleRect(contestant.getElement().getBoundingBox());
            }

            private org.testobject.commons.math.algebra.Rectangle.Double toDoubleRect(org.testobject.commons.math.algebra.Rectangle.Int rect) {
                return new org.testobject.commons.math.algebra.Rectangle.Double(rect.x, rect.y, rect.w, rect.h);
            }

            @Override
            public double getProbability(Node contestant) {
                return contestant.getElement().getLabel().getLikelihood().geometric();
            }
        });

        if(debug) {
            System.in.read();
        }
    }

    private static List<Image.Int> readImages(String ... files) throws IOException {
        List<Image.Int> images = Lists.newArrayList(files.length);
        for(String file : files) {
            images.add(readImage(file));
        }
        return images;
    }

    private static Image.Int readImage(String file) throws IOException {
        return ImageUtil.Read.read(FileUtil.readFileFromClassPath("android/4_0_3/" + file));
    }

    private static void render(org.testobject.kernel.imaging.procedural.Node node) {
        if(debug) VisualizerUtil.show("synthetic", Util.render(node), 8);
    }

}
