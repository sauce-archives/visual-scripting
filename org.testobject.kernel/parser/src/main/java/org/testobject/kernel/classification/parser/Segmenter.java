package org.testobject.kernel.classification.parser;

import java.util.List;

import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.api.classification.graph.Element;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.imaging.segmentation.Blob;
import org.testobject.kernel.imaging.segmentation.GraphBlobBuilder;

/**
 * 
 * @author enijkamp
 *
 */
public interface Segmenter {
	
	Node segment(Image.Int image);
	
	class Graph implements Segmenter {
		
		private final float sigma;
		private final double threshold;
		private final int minBlobSize;

		public Graph(float sigma, double threshold) {
			this(sigma, threshold, GraphBlobBuilder.MIN_BLOB_SIZE);
		}
		
		public Graph(float sigma, double threshold, int minBlobSize) {
			this.sigma = sigma;
			this.threshold = threshold;
			this.minBlobSize = minBlobSize;
		}

		@Override
		public Node segment(Image.Int image) {
			
			Blob[] blobs = new GraphBlobBuilder(image.w, image.h, sigma, threshold, minBlobSize).build(image);

			return map(blobs[0]);
		}
		
		private static Node map(Blob blob) {
			List<Node> childs = Lists.newArrayList(blob.children.size());
			for(Blob child : blob.children) {
				childs.add(map(child));
			}
			
			return Node.Factory.create(childs, Element.Factory.blob(blob));
		}
		
		@Override
		public String toString() {
			return "graph(s=" + sigma +"f, t=" + threshold + "d)";
		}
		
	}

}
