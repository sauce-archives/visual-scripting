package org.testobject.kernel.classification.classifiers.advanced;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.Image.Int;
import org.testobject.kernel.api.classification.classifiers.Classifier;
import org.testobject.kernel.api.classification.classifiers.Classifier.Qualifier;
import org.testobject.kernel.api.classification.graph.Depiction;


/**
 * 
 * @author enijkamp
 *
 */
public interface Registry {

	Sample get(Qualifier qualifier);
	
	List<Sample> get(String type);
	
	class Mutable implements Registry {
		
		private final Map<Qualifier, Sample> samples = new HashMap<>();
		
		public void put(Sample sample) {
			samples.put(sample.getQualifier(), sample);
		}

		@Override
		public Sample get(Qualifier qualifier) {
			return samples.get(qualifier);
		}

		@Override
		public List<Sample> get(String type) {
			List<Sample> byType = Lists.newLinkedList();
			for(Map.Entry<Classifier.Qualifier, Sample> entry : samples.entrySet()) {
				if(entry.getKey().getType().equals(type)) {
					byType.add(entry.getValue());
				}
			}
			
			return byType;
		}
		
	}
	
	interface Sample {
		
		Qualifier getQualifier();
		
		Image.Int getImage();
		
		Depiction getDepiction();
		
		class Factory {
			
			public static Sample create(final Classifier.Qualifier qualifier, final Image.Int image, final Depiction depiction) {
				return new Sample() {
					@Override
					public Qualifier getQualifier() {
						return qualifier;
					}

					@Override
					public Int getImage() {
						return image;
					}

					@Override
					public Depiction getDepiction() {
						return depiction;
					}
				};
			}
			
		}
	}
}
