package org.testobject.kernel.classification.classifiers.advanced;

import java.util.List;

import org.testobject.commons.util.image.Image;
import org.testobject.kernel.api.classification.classifiers.Classifier;

/**
 * 
 * @author enijkamp
 *
 */
public interface Trainer {
	
	Registry.Sample train(Image.Int image, String id);
	
	Classifier create(List<Registry.Sample> samples);

}
