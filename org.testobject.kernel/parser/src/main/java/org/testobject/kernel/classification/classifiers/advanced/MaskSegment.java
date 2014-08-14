package org.testobject.kernel.classification.classifiers.advanced;

import static org.testobject.kernel.api.classification.classifiers.Classifier.Likelihood.Builder.likelihood;
import static org.testobject.kernel.api.classification.graph.Element.Builder.contours;
import static org.testobject.kernel.api.classification.graph.Element.Builder.depict;
import static org.testobject.kernel.api.classification.graph.Element.Builder.element;
import static org.testobject.kernel.api.classification.graph.Element.Builder.fill;
import static org.testobject.kernel.api.classification.graph.Element.Builder.fingerprint;
import static org.testobject.kernel.api.classification.graph.Element.Builder.position;
import static org.testobject.kernel.api.classification.graph.Element.Builder.size;
import static org.testobject.kernel.classification.matching.Matcher.Util.checkFeatures;
import static org.testobject.kernel.classification.matching.Matcher.Util.checkType;
import static org.testobject.kernel.imaging.procedural.Element.Builder.image;
import static org.testobject.kernel.imaging.procedural.Element.Builder.polyline;
import static org.testobject.kernel.imaging.procedural.Style.Stroke.Builder.none;

import java.util.List;
import java.util.Set;

import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.collections.Sets;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.kernel.api.classification.classifiers.Classifier.Lookups;
import org.testobject.kernel.api.classification.classifiers.Classifier.Qualifier;
import org.testobject.kernel.api.classification.graph.Contour;
import org.testobject.kernel.api.classification.graph.Depiction;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.classification.graph.Variable;
import org.testobject.kernel.api.classification.graph.Variable.Names;
import org.testobject.kernel.api.util.VariableUtil;
import org.testobject.kernel.classification.matching.Matcher.Util.Factors;
import org.testobject.kernel.imaging.contours.ColorExtractor;
import org.testobject.kernel.imaging.fingerprint.ImageFingerprint;
import org.testobject.kernel.imaging.procedural.Color;
import org.testobject.kernel.imaging.procedural.ContourExtractor;
import org.testobject.kernel.imaging.procedural.Edge;
import org.testobject.kernel.imaging.procedural.Element;
import org.testobject.kernel.imaging.procedural.Graph;
import org.testobject.kernel.imaging.procedural.Node;
import org.testobject.kernel.imaging.procedural.Style;
import org.testobject.kernel.imaging.procedural.Transform;
import org.testobject.kernel.imaging.segmentation.BlobUtils;
import org.testobject.kernel.imaging.segmentation.Mask;


/**
 * 
 * @author enijkamp
 *
 */
public interface MaskSegment {
	
	// FIXME re-use code for all types (en)
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
			
			private final Lookups.Contour lookup;
			
			public Unzip(Lookups.Contour lookup) {
				this.lookup = lookup;
			}

			@Override
			public List<Variable<?>> unzip(Qualifier qualifier, List<Variable<?>> source, org.testobject.kernel.api.classification.classifiers.Classifier.Images images) {
				List<Variable<?>> target = Lists.newArrayList(source);

				List<Mask> masks = VariableUtil.getMasks(source);
				Classifier.Sample sample = Classifier.toSample(images.raw(), masks, lookup, Qualifier.Factory.none());
				
				// depiction
				{
					Depiction depiction = Depiction.Builder.create(sample.graph, Transform.Builder.translate(VariableUtil.getPosition(source).x, VariableUtil.getPosition(source).y), Transform.Scale.Builder.identity());
					target.add(depict(depiction));
				}
				
				// contours
				{
					target.add(contours(sample.contours));
				}
				
				// fingerprint
				{
					target.add(fingerprint(sample.fingerprint));
				}
				
				// color
				{
					target.add(fill(ColorExtractor.extractColor(images.raw(), Mask.Builder.create(masks))));	
				}
				
				return target;
			}
			
			@Override
			public boolean supports(Qualifier qualifier) {
				return qualifier.getType().equals(Classifier.Qualifier.Factory.Class.segment);
			}
		}
	}
	
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
	
	class Classifier implements org.testobject.kernel.api.classification.classifiers.Classifier {
		
		public static class Sample {

            public final Qualifier qualifier;
			public final Node graph;
			public final List<Contour> contours;
			public final ImageFingerprint fingerprint;

			public Sample(Qualifier qualifier, Node graph, List<Contour> contours, ImageFingerprint fingerprint) {
                this.qualifier = qualifier;
				this.graph = graph;
				this.contours = contours;
				this.fingerprint = fingerprint;
			}
		}
		
		@Override
		public List<Proposal> classify(Images images, Lookup lookup, List<Mask> masks, Context context) {
			
			Proposal.Builder proposals = Proposal.Builder.create();
			
			// childs
			List<Mask> masksWithChilds = withChilds(masks, lookup);
			
			// masks
            Mask union = Mask.Builder.create(masksWithChilds);
            
            // sample
            Sample sample = toSample(images.raw(), masksWithChilds, lookup, context.qualifier());
			
			// depiction
			Depiction depiction = Depiction.Builder.create(sample.graph, translate(union), Transform.Scale.Builder.identity());

			// proposal
			proposals.proposal(
					likelihood()
						.geometric(0d)
						.photometric(0d)
					.build(),
					element(masksWithChilds)
						.qualifier(sample.qualifier)
						.likelihood(0d, 0d)
						.feature(depict(depiction))
						.feature(contours(sample.contours))
						.feature(position(union.getBoundingBox().getLocation()))
						.feature(size(union.getBoundingBox().getSize()))
						.feature(fill(ColorExtractor.extractColor(images.raw(), union)))
						.feature(fingerprint(sample.fingerprint))
					.build());
	
			return proposals.build();
		}
	
		private List<Mask> withChilds(List<Mask> masks, Lookup lookup) {
			
			List<Mask> unions = Lists.newArrayList(masks.size());
			
			for(Mask mask : masks) {
				List<Mask> withChilds = Lists.toLinkedList(mask);
				withChilds(mask, withChilds, lookup);
				Mask union = Mask.Builder.merge(withChilds);
				unions.add(union);
			}
			
			return unions;
		}

		private void withChilds(Mask mask, List<Mask> withChilds, Lookup lookup) {
			for(Mask child : lookup.childs(mask)) {
				withChilds.add(child);
				withChilds(child, withChilds, lookup);
			}
		}

		public static Sample toSample(org.testobject.commons.util.image.Image.Int image, List<Mask> masks, Lookups.Contour lookup, Qualifier id) {
			
			Classifier.Qualifier qualifier = Classifier.Qualifier.Factory.Class.segment(id);
			
			Node graph = toRenderGraph(image, masks, lookup);
			
			List<Contour> contours = toContours(graph);
			
            Mask union = Mask.Builder.create(masks);
            org.testobject.commons.util.image.Image.Int imageCutByBox = Shared.cutByBox(image, union.getBoundingBox());
            
            ImageFingerprint fingerprint = Shared.createFingerprint(imageCutByBox);
			
            return new Sample(qualifier, graph, contours, fingerprint);
		}
		
        private static List<Contour> toContours(Node node) {

            // sanity
            if(node.getElements().isEmpty() && node.getNodes().isEmpty()) {
                throw new IllegalStateException();
            }

            if(node.getElements().isEmpty()) {
                // nodes
                List<Contour> childs = Lists.newArrayList(node.getNodes().size());
                for(Edge<Node> nodeEdge : node.getNodes()) {
                    Node childNode = nodeEdge.getTarget();
                    childs.add(toChildContour(childNode));
                }

                return childs;

            } else {
                // elements
                List<Point.Double> contour = toPoints(node.getElements());

                // nodes
                List<Contour> childs = Lists.newArrayList(node.getNodes().size());
                for(Edge<Node> nodeEdge : node.getNodes()) {
                    Node childNode = nodeEdge.getTarget();
                    childs.add(toChildContour(childNode));
                }

                return Lists.toList(new Contour(contour, childs));
            }
        }

        private static Contour toChildContour(Node node) {

            // sanity
            if(node.getElements().isEmpty() && node.getNodes().isEmpty()) {
                throw new IllegalStateException();
            }

            // elements
            List<Point.Double> contour = toPoints(node.getElements());

            // nodes
            List<Contour> childs = Lists.newArrayList(node.getNodes().size());
            for(Edge<Node> nodeEdge : node.getNodes()) {
                Node childNode = nodeEdge.getTarget();
                childs.add(toChildContour(childNode));
            }

            return new Contour(contour, childs);
        }

        private static List<Point.Double> toPoints(List<Edge<Element>> elements) {
            // sanity
            if(elements.isEmpty()) {
                throw new IllegalStateException();
            }

            // polyline
            for(Edge<Element> edge : elements) {
            	if(edge.getTarget() instanceof Element.Polyline) {
            		Element element = edge.getTarget();
            		return ContourExtractor.getContour(element);
            	}
            }

            // sanity
            throw new IllegalStateException();
        }
		
		public static Node toRenderGraph(Image.Int image, List<Mask> masks, Lookups.Contour lookup) {
			
			// trace
			Graph.Builder graph = Graph.Builder.graph();
			Node.Builder node = graph.node();
			for(Mask mask : masks) {
				toNode(image, mask, new Point.Int(0, 0), node, lookup);
			}
			
			return graph.build();
		}
		
	    private static void toNode(Image.Int image, Mask mask, Point.Int parentOffset, Node.Builder parent, Lookups.Contour lookup) {
	    	
			// transformations
			int translateX = (mask.getBoundingBox().x - parentOffset.x);
			int translateY = (mask.getBoundingBox().y - parentOffset.y);
	    	
			// contour
	    	List<Point.Double> contour = lookup.contour(mask);
	    	Color color = ColorExtractor.extractColor(image, mask);

	    	// node
	    	Node.Builder node = Node.Builder.node(parent, org.testobject.kernel.imaging.procedural.Transform.Builder.translate(translateX, translateY));
	    	node.element(image(image, mask));
	    	node.element(polyline(contour, Style.Builder.style(Style.Builder.fill(color), none())));
		}
		
		public static Mask toMask(final org.testobject.commons.util.image.Image.Int image) {
			return new Mask() {
				@Override
				public org.testobject.commons.math.algebra.Size.Int getSize() {
					return getBoundingBox().getSize();
				}

				@Override
				public boolean get(int x, int y) {
					return image.get(x, y) != Color.Builder.transparent().toRgb();
				}

				@Override
				public void set(int x, int y, boolean what) {
					throw new UnsupportedOperationException();
				}

				@Override
				public org.testobject.commons.math.algebra.Rectangle.Int getBoundingBox() {
					return new Rectangle.Int(0, 0, image.w, image.h);
				}
			};
		}
		
		public static org.testobject.kernel.imaging.procedural.Transform.Translate translate(Mask mask) {
			return Transform.Builder.translate(mask.getBoundingBox().x, mask.getBoundingBox().y);
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
				checkFeatures(descriptor1, descriptor2, Names.Geometric.position, Names.Geometric.size, Names.Depiction.contours, Names.Depiction.fingerprint);
			}
			
			// probabilities
			{
				Factors factors = new Factors(descriptor1, context1, descriptor2, context2); 
			
				// TODO add color (en)
				factors
					.size(.1)
					.position(.3)
					.fingerprint(.6);
				
				return Match.Factory.create(factors.probabilities(), factors.probability());
			}
		}
	}
}