package org.testobject.kernel.classification.parser;

import org.testobject.commons.util.image.Image;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.classification.parser.Operations.Input;

/**
 * 
 * @author enijkamp
 *
 */
public class Segmentation implements Input {
	
	private final Segmenter segmenter;
	
	public Segmentation(Segmenter segmenter) {
		this.segmenter = segmenter;
	}

	@Override
	public Node apply(Image.Int raw) {
		return segmenter.segment(raw);
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName().toLowerCase() + "(" + segmenter.toString() + ")";
	}
}