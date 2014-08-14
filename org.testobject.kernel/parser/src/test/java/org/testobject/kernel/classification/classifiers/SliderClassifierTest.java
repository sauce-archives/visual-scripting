package org.testobject.kernel.classification.classifiers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.collections.Maps;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.api.classification.graph.Element;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.classification.classifiers.advanced.Slider;
import org.testobject.kernel.classification.graph.Context;
import org.testobject.kernel.imaging.segmentation.Blob;
import org.testobject.kernel.imaging.segmentation.BlobUtils;
import org.testobject.kernel.imaging.segmentation.GraphBlobBuilder;
import org.testobject.kernel.imaging.segmentation.Neighbours;
import org.testobject.commons.tools.plot.VisualizerUtil;
import org.testobject.commons.util.image.ImageUtil;

/**
 * 
 * @author enijkamp
 *
 */
public class SliderClassifierTest {

    public static final boolean debug = Debug.toDebugMode(false);
	
	@Test
	public void testClassifier() throws IOException {
		final Image.Int image = ImageUtil.expand(ImageUtil.Read.read(ClassLoader.getSystemResource("android/4_0_3/classifiers/slider/slider.png")), 2, 2);

		float sigma = 0.5f;
		double threshold = 4500d;
		
		GraphBlobBuilder builder = new GraphBlobBuilder(image.w, image.h, sigma, threshold);
		Blob[] blobs = builder.build(image);

		// render
        if(debug)
        {
			BlobUtils.Print.printBlobs(blobs[0]);
			Image.Int hierarchy = new Image.Int(image.w, image.h);
			BlobUtils.Draw.drawHierarchy(blobs, hierarchy);
			VisualizerUtil.show("blobs", ImageUtil.Convert.toBufferedImage(hierarchy), 3f);
		}
		
		// context
		Node nodes;
		Context context;
		{
			Map<Blob, List<Blob>> neighbours = Neighbours.findNeighbours(blobs[0]);
			printNeighbours(neighbours);
			
			Map<Blob, List<Node>> blobToNode = Maps.newIdentityMap();
			Map<Node, List<Blob>> nodeToBlob = Maps.newIdentityMap();
			nodes = mapBlobsToNodes(blobs[0], blobToNode, nodeToBlob);
			
			context = new Context(image, new Context.Mapping(blobToNode, nodeToBlob), neighbours);
		}
		
		// classify
		{
			Slider classifier = new Slider();
			Blob handleBlob = blobs[4];
			Node node = context.mapping.blobToNodes.get(handleBlob).get(0);
			
			classifier.classify(context, node);
		}

        if(debug) System.in.read();
	}

	private static void printNeighbours(Map<Blob, List<Blob>> neighbours) {
		for(Map.Entry<Blob, List<Blob>> map : neighbours.entrySet()) {
			System.out.print(map.getKey().id + " -> ");
			for(Blob neighbour : map.getValue()) {
				System.out.print(neighbour.id + " ");
			}
			System.out.println();
		}
	}

	private static Node mapBlobsToNodes(Blob blob, Map<Blob, List<Node>> blobToNode, Map<Node, List<Blob>> nodeToBlob) {
		
		List<Node> childs = Lists.newArrayList(blob.children.size());
		for(Blob child : blob.children) {
			childs.add(mapBlobsToNodes(child, blobToNode, nodeToBlob));
		}
		
		Node node = Node.Factory.create(childs, Element.Factory.blob(blob));
		blobToNode.put(blob, Lists.toList(node));
		nodeToBlob.put(node, Lists.toList(blob));
		return node;
	}

}
