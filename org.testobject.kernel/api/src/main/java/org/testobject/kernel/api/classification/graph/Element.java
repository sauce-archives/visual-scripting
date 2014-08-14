package org.testobject.kernel.api.classification.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.math.algebra.Size;
import org.testobject.commons.util.collections.Lists;
import org.testobject.kernel.api.classification.classifiers.Classifier;
import org.testobject.kernel.api.classification.classifiers.Classifier.Likelihood;
import org.testobject.kernel.api.classification.graph.Variable.Names;
import org.testobject.kernel.imaging.fingerprint.ImageFingerprint;
import org.testobject.kernel.imaging.procedural.Color;
import org.testobject.kernel.imaging.segmentation.Blob;
import org.testobject.kernel.imaging.segmentation.HasBoundingBox;
import org.testobject.kernel.imaging.segmentation.Mask;

/**
 * An element in the node hierarchy represents a classified image segment.
 * The label of this elements holds the class properties where some features are specific to this instance of the class
 * and can be obtained (e.g. while deserialization) from combining of the raw input image, the mask
 * and the classifier logic which constructred the label object in the first place.  
 * 
 * @author enijkamp
 *
 */
public interface Element extends HasBoundingBox {
	
	interface Label {
		
		Classifier.Qualifier getQualifier();
		
		List<Variable<?>> getFeatures();
		
		Classifier.Likelihood getLikelihood();
		
	}
	
	List<Mask> getMasks();
	
	Label getLabel();

	class Builder {
		
		// mandatory
		private final List<Mask> masks;
		private Classifier.Qualifier qualifier;
		
		// optional
		private final List<Variable<?>> features = Lists.newLinkedList();
		private Classifier.Likelihood likelihood = Classifier.Likelihood.Factory.none();
		
		public static Builder element(Mask ... masks) {
			return new Builder(Arrays.asList(masks));
		}
		
		public static Builder element(List<Mask> masks) {
			return new Builder(masks);
		}
		
		private Builder(List<Mask> masks) {
			this.masks = masks;
		}
		
		public Builder likelihood(final double geometric, final double photometric) {
			this.likelihood = Likelihood.Factory.likelihood(geometric, photometric);
			return this;
		}
		
		public Builder qualifier(Classifier.Qualifier qualifier) {
			this.qualifier = qualifier;
			return this;
		}
		
		public Builder feature(Variable<?> feature) {
			this.features.add(feature);
			return this;
		}
		
		public Element build() {
			Rectangle.Int box = union(toRectangles(masks));

			return build(box);
		}
		
		public Element build(final Rectangle.Int box) {
			// sanity
			if(qualifier == null) {
				throw new IllegalStateException("type missing");
			}
			
			final Label label = new Label() {
				@Override
				public Classifier.Qualifier getQualifier() {
					return qualifier;
				}

				@Override
				public Classifier.Likelihood getLikelihood() {
					return likelihood;
				}

				@Override
				public List<Variable<?>> getFeatures() {
					return features;
				}
			};
			
			return new Element() {
				@Override
				public List<Mask> getMasks() {
					return masks;
				}

				@Override
				public Label getLabel() {
					return label;
				}

				@Override
				public Rectangle.Int getBoundingBox() {
					return box;
				}
			};
		}
		
		private static Rectangle.Int union(List<Rectangle.Int> rectangles) {
			Rectangle.Int union = new Rectangle.Int(rectangles.get(0));
			for(int i = 1; i < rectangles.size(); i++) {
				union = union.union(rectangles.get(i));
			}
			return union;
		}

		private static List<Rectangle.Int> toRectangles(List<? extends Mask> masks) {
			List<Rectangle.Int> rects = new ArrayList<>(masks.size());
			for(Mask mask : masks) {
				rects.add(mask.getBoundingBox());
			}
			return rects;
		}
		
		public static Variable<org.testobject.kernel.api.classification.graph.Depiction> depict(org.testobject.kernel.api.classification.graph.Depiction depiction) {
			return toFeature(Names.Depiction.graph, depiction);
		}

		public static Variable<List<Contour>> contours(List<Contour> contours) {
			return toFeature(Names.Depiction.contours, contours);
		}
		
		public static Variable<Size.Int> size(Size.Int size) {
			return toVariable(Names.Geometric.size, size);
		}
		
		public static Variable<Point.Int> position(Point.Int position) {
			return toVariable(Names.Geometric.position, position);
		}
		
		public static Variable<ImageFingerprint> fingerprint(ImageFingerprint print) {
			return toVariable(Names.Depiction.fingerprint, print);
		}

		public static Variable<Color> fill(Color color) {
			return toVariable(Names.Depiction.fill, color);
		}
		
		private static <T> Variable<T> toFeature(final String name, final T value) {
			return toVariable(name, value);
		}
		
		public static <T> Variable<T> value(String name, T value) {
			return toVariable(name, value);
		}
		
		private static <T> Variable<T> toVariable(final String name, final T value) {
			return Variable.Builder.value(name, value);
		}
	}
	
	class Factory {
		
		public static Element node(final String qualifier, final Rectangle.Int box) {
			final Classifier.Qualifier type = Classifier.Qualifier.Factory.Class.node(qualifier);
			
			// FIXME we should not use bboxes for masks (en)
			final Mask mask = Mask.Builder.create(box);
			return new Element() {
				@Override
				public Label getLabel() {
					return new Label() {
						@Override
						public Classifier.Qualifier getQualifier() {
							return type;
						}

						@Override
						public Classifier.Likelihood getLikelihood() {
							return Classifier.Likelihood.Factory.none();
						}

						@Override
						public List<Variable<?>> getFeatures() {
							return Lists.empty();
						}
					};
				}

				@Override
				public List<Mask> getMasks() {
					return Lists.toList(mask);
				}

				@Override
				public Rectangle.Int getBoundingBox() {
					return box;
				}
			};
		}
		
		public static Element blob(final Blob blob) {
			final Classifier.Qualifier type = Classifier.Qualifier.Factory.Class.blob(blob.id);
			return new Element() {
				@Override
				public Label getLabel() {
					return new Label() {
						@Override
						public Classifier.Qualifier getQualifier() {
							return type;
						}

						@Override
						public Classifier.Likelihood getLikelihood() {
							return Classifier.Likelihood.Factory.none();
						}
						
						@Override
						public List<Variable<?>> getFeatures() {
							return Lists.empty();
						}
					};
				}

				@Override
				public List<Mask> getMasks() {
					return Lists.<Mask>toList(blob);
				}

				@Override
				public Rectangle.Int getBoundingBox() {
					return blob.getBoundingBox();
				}
			};
		}
	}
}