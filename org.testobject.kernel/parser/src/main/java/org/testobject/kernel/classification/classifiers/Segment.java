package org.testobject.kernel.classification.classifiers;

import static org.testobject.kernel.api.classification.classifiers.Classifier.Likelihood.Builder.likelihood;
import static org.testobject.kernel.api.classification.graph.Element.Builder.element;
import static org.testobject.kernel.api.classification.graph.Element.Builder.fill;
import static org.testobject.kernel.api.classification.graph.Element.Builder.fingerprint;
import static org.testobject.kernel.api.classification.graph.Element.Builder.position;
import static org.testobject.kernel.api.classification.graph.Element.Builder.size;
import static org.testobject.kernel.classification.matching.Matcher.Util.checkFeatures;
import static org.testobject.kernel.classification.matching.Matcher.Util.checkType;

import java.util.List;
import java.util.Set;

import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.collections.Sets;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.kernel.api.classification.classifiers.Classifier.Qualifier;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.classification.graph.Variable;
import org.testobject.kernel.api.classification.graph.Variable.Names;
import org.testobject.kernel.api.util.VariableUtil;
import org.testobject.kernel.classification.matching.Matcher.Util.Factors;
import org.testobject.kernel.imaging.contours.ColorExtractor;
import org.testobject.kernel.imaging.fingerprint.ImageFingerprint;
import org.testobject.kernel.imaging.segmentation.BlobUtils;
import org.testobject.kernel.imaging.segmentation.Mask;



/**
 * 
 * @author enijkamp
 *
 */
public interface Segment {
	
    class Shared {
    	public static ImageFingerprint createFingerprint(org.testobject.commons.util.image.Image.Int image) {
    		return new ImageFingerprint(ImageUtil.toSquare(image), 0xf2, 0xf1, 0xf0);
    	}
    	
    	public static org.testobject.commons.util.image.Image.Int cutByMask(org.testobject.commons.util.image.Image.Int raw, Mask mask) {
			return BlobUtils.Cut.cutByMask(raw, mask);
		}

		public static org.testobject.commons.util.image.Image.Int cutByBox(org.testobject.commons.util.image.Image.Int raw, org.testobject.commons.math.algebra.Rectangle.Int boundingBox) {
			return ImageUtil.Cut.crop(raw, boundingBox);
		}
    }
	
	interface Compression {
		
		class Zip implements org.testobject.kernel.classification.classifiers.Compression.Zip {
		
			private static final Set<String> filter = Sets.from(Names.Geometric.position, Names.Geometric.size);
			
			@Override
			public List<Variable<?>> zip(Qualifier qualifier, List<Variable<?>> source) {
				List<Variable<?>> target = Lists.newArrayList(filter.size());
				for(Variable<?> variable : source) {
					for(String name : filter) {
						if(variable.getName().startsWith(name)) {
							target.add(variable);
						}
					}
				}
				
				return target;
			}
			
			@Override
			public boolean supports(Qualifier qualifier) {
				return qualifier.getType().equals(Classifier.Qualifier.Factory.Class.segment);
			}
		}
		
		class Unzip implements org.testobject.kernel.classification.classifiers.Compression.Unzip {
			
			@Override
			public List<Variable<?>> unzip(Qualifier qualifier, List<Variable<?>> source, Classifier.Images images) {
				List<Variable<?>> target = Lists.newArrayList(source);

				List<Mask> masks = VariableUtil.getMasks(source);
				
				// union
				Mask mask = Mask.Builder.create(masks);
				
				// image
				Image.Int image = ImageUtil.Cut.crop(images.raw(), mask.getBoundingBox());
	            
	            // fingerprint
				ImageFingerprint fingerprint = Shared.createFingerprint(image);
				target.add(fingerprint(fingerprint));
				
				// color
				target.add(fill(ColorExtractor.extractColor(image)));	
				
				return target;
			}
			
			@Override
			public boolean supports(Qualifier qualifier) {
				return qualifier.getType().equals(Classifier.Qualifier.Factory.Class.segment);
			}
		}
	}
	
	class Classifier implements org.testobject.kernel.api.classification.classifiers.Classifier {
		
		@Override
		public List<Proposal> classify(Images images, Lookup lookup, List<Mask> masks, Context context) {
			
			Proposal.Builder proposals = Proposal.Builder.create();
			
			// union
			Mask mask = Mask.Builder.create(masks);
			
			// image
			Image.Int image = ImageUtil.Cut.crop(images.raw(), mask.getBoundingBox());
            
            // features
			ImageFingerprint fingerprint = Shared.createFingerprint(image);
			
			// proposal
			proposals.proposal(
					likelihood()
						.geometric(0d)
						.photometric(0d)
					.build(),
					element(mask)
						.qualifier(context.qualifier())
						.likelihood(0d, 0d)
						.feature(position(mask.getBoundingBox().getLocation()))
						.feature(size(mask.getBoundingBox().getSize()))
						.feature(fill(ColorExtractor.extractColor(image)))
						.feature(fingerprint(fingerprint))
					.build());
	
			return proposals.build();
		}
	
		@Override
		public String toString() {
			return Classifier.Qualifier.Factory.Class.segment;
		}
	}
	
	public static class Matcher implements org.testobject.kernel.classification.matching.Matcher {
		
		@Override
		public Match match(Locator.Descriptor descriptor1, Context context1, Locator.Descriptor descriptor2, Context context2) {
			
			// preconditions
			{
				checkType(descriptor1, descriptor2, Classifier.Qualifier.Factory.Class.segment);
				checkFeatures(descriptor1, descriptor2, Names.Geometric.position, Names.Geometric.size, Names.Depiction.fill,
						Names.Depiction.contours, Names.Depiction.fingerprint);
			}
			
			// probabilities
			{
				Factors factors = new Factors(descriptor1, context1, descriptor2, context2); 
			
				factors
					.color(.1)
					.size(.1)
					.position(.3)
					.fingerprint(.5);
				
				return Match.Factory.create(factors.probabilities(), factors.probability());
			}
		}
	}
}