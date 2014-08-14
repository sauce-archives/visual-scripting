package org.testobject.kernel.classification.graph;

import java.awt.Rectangle;
import java.util.List;
import java.util.Map;

import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.imaging.segmentation.Blob;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.Image.Int;

/**
 * 
 * @author enijkamp
 *
 */
public class Context {
	
	public static class Mapping {
		
		public final Map<Blob, List<Node>> blobToNodes;
		public final Map<Node, List<Blob>> nodeToBlobs;
		
		public Mapping(Map<Blob, List<Node>> blobToNodes, Map<Node, List<Blob>> nodeToBlobs) {
			this.blobToNodes = blobToNodes;
			this.nodeToBlobs = nodeToBlobs;
		}

		public Blob getBlob(Node node) {
			return nodeToBlobs.get(node).get(0); // FIXME (en)
		}
	}
	
	public interface Lookup {
		
		List<Node> lookup(Rectangle rect);
		
	}
	
	public final Image.Int image;
	public final Context.Mapping mapping;
	public final Map<Blob, List<Blob>> neighbours;
	public final Lookup lookup = null;
	
	public Context(Int image, Context.Mapping mapping, Map<Blob, List<Blob>> neighbours) {
		this.image = image;
		this.mapping = mapping;
		this.neighbours = neighbours;
	}
}