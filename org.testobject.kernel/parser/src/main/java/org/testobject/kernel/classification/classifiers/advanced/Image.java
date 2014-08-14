package org.testobject.kernel.classification.classifiers.advanced;

import static org.testobject.kernel.api.classification.classifiers.Classifier.Likelihood.Builder.likelihood;
import static org.testobject.kernel.api.classification.graph.Element.Builder.contours;
import static org.testobject.kernel.api.classification.graph.Element.Builder.depict;
import static org.testobject.kernel.api.classification.graph.Element.Builder.element;
import static org.testobject.kernel.api.classification.graph.Element.Builder.position;
import static org.testobject.kernel.api.classification.graph.Element.Builder.size;
import static org.testobject.kernel.classification.matching.Matcher.Util.checkFeatures;
import static org.testobject.kernel.classification.matching.Matcher.Util.checkType;
import static org.testobject.kernel.imaging.procedural.Element.Builder.image;
import static org.testobject.kernel.imaging.procedural.Element.Builder.polyline;
import static org.testobject.kernel.imaging.procedural.Graph.Builder.graph;
import static org.testobject.kernel.imaging.procedural.Style.Builder.fill;
import static org.testobject.kernel.imaging.procedural.Style.Builder.stroke;
import static org.testobject.kernel.imaging.procedural.Style.Builder.style;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Size;
import org.testobject.commons.tools.plot.VisualizerUtil;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.collections.Sets;
import org.testobject.commons.util.config.Debug;
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
import org.testobject.kernel.imaging.contours.PolyMatch;
import org.testobject.kernel.imaging.contours.Trace;
import org.testobject.kernel.imaging.fingerprint.ImageFingerprint;
import org.testobject.kernel.imaging.procedural.Color;
import org.testobject.kernel.imaging.procedural.ContourExtractor;
import org.testobject.kernel.imaging.procedural.Dimension;
import org.testobject.kernel.imaging.procedural.Edge;
import org.testobject.kernel.imaging.procedural.Element;
import org.testobject.kernel.imaging.procedural.Element.Polyline;
import org.testobject.kernel.imaging.procedural.Graph;
import org.testobject.kernel.imaging.procedural.Node;
import org.testobject.kernel.imaging.procedural.Renderer;
import org.testobject.kernel.imaging.procedural.Style;
import org.testobject.kernel.imaging.procedural.Transform;
import org.testobject.kernel.imaging.procedural.Util;
import org.testobject.kernel.imaging.segmentation.BlobUtils;
import org.testobject.kernel.imaging.segmentation.Mask;

/**
 *
 * @author enijkamp
 *
 */
public interface Image {
	
	// FIXME re-use code for all types (en)
	interface Compression {
		
		class Zip implements org.testobject.kernel.classification.classifiers.Compression.Zip {
		
			private static final Set<String> filter = Sets.from(Names.Geometric.position, Names.Geometric.size);
			
			@Override
			public List<Variable<?>> zip(Qualifier qualifier, List<Variable<?>> source) {
				List<Variable<?>> target = Lists.newArrayList(filter.size());
				for(Variable<?> variable : source) {
					if(filter.contains(variable.getName())) {
						target.add(variable);
					}
				}
				
				return target;
			}
			
			@Override
			public boolean supports(Qualifier qualifier) {
				return qualifier.getType().equals(Classifier.Qualifier.Factory.Class.image);
			}
		}
		
		class Unzip implements org.testobject.kernel.classification.classifiers.Compression.Unzip {
			
			private final Registry registry;
			private final Lookups.Contour lookup;
			
			public Unzip(Registry registry, Lookups.Contour lookup) {
				this.registry = registry;
				this.lookup = lookup;
			}

			@Override
			public List<Variable<?>> unzip(Qualifier qualifier, List<Variable<?>> source, org.testobject.kernel.api.classification.classifiers.Classifier.Images images) {
				List<Variable<?>> target = Lists.newArrayList(source);
				
				Registry.Sample sample = registry.get(qualifier);
				
				// FIXME factor out related code from classifier (en)
				List<Mask> masks = VariableUtil.getMasks(source);
				
//				// depict
//				{
//					Depiction depiction = Depiction.Builder.create(sample.getDepiction().getGraph(), Classifier.translate(mask), Transform.Scale.Builder.identity());
//					target.add(depict(depiction));
//				}
//				
//				// contour
//				{
//					List<Contour> contours = Lists.toList(new Contour(lookup.contour(mask)));
//					target.add(Variable.Builder.value(Names.Depiction.contours, contours));					
//				}
				
				return target;
				
				// FIXME somehow consolidate with trainer and classifier (en)
				
				

//				// proposal
//				proposals.proposal(
//						likelihood()
//							.geometric(geometric)
//							.photometric(photometric)
//						.build(),
//						transform(
//							element(union)
//								.qualifier(sample.qualifier)
//								.likelihood(geometric, photometric)
//								.feature(depict(depiction))
//								.feature(contours(sample.contours))
//								.feature(position(union.getBoundingBox().getLocation()))
//								.feature(size(union.getBoundingBox().getSize())))
//				
//				
//				
//
//				Mask mask = getMask(source);
//				
//				// depict
//				{
//					Depiction depiction = Depiction.Builder.create(sample.getDepiction(), Classifier.translate(mask), Transform.Scale.Builder.identity());
//					target.add(depict(depiction));
//				}
//				
//				// contour
//				{
//					List<Point.Double> contours = sample.
//					target.add(Variable.Builder.value(Names.Depiction.contours, contours));					
//				}
			}
			
			@Override
			public boolean supports(Qualifier qualifier) {
				return qualifier.getType().equals(Classifier.Qualifier.Factory.Class.image);
			}
		}
	}

    class Shared {
    	public static ImageFingerprint createFingerprint(org.testobject.commons.util.image.Image.Int image) {
    		return new ImageFingerprint(ImageUtil.toSquare(image), 0xf2, 0xf1, 0xf0);
    	}
    }

	class Trainer implements org.testobject.kernel.classification.classifiers.advanced.Trainer {
	
		public Image.Classifier train(List<String> names, List<org.testobject.commons.util.image.Image.Int> images) {

			List<Registry.Sample> samples = Lists.newLinkedList();
			
			for(int i = 0; i < images.size(); i++) {
                samples.add(train(images.get(i), names.get(i)));
			}
			
			return new Classifier(samples);
		}
		
		@Override
		public Registry.Sample train(org.testobject.commons.util.image.Image.Int image, String id) {
			
			Classifier.Qualifier qualifier = Classifier.Qualifier.Factory.Class.image(id);
			Depiction depiction = Depiction.Builder.create(toRenderGraph(image));
			
            return Registry.Sample.Factory.create(qualifier, image, depiction);
		}
		
		@Override
		public Classifier create(List<Registry.Sample> samples) {
			return new Classifier(samples);
		}

		public static Node toRenderGraph(org.testobject.commons.util.image.Image.Int image) {

			// trim
			org.testobject.commons.util.image.Image.Int trimmed = ImageUtil.Cut.trim(image, Color.Builder.transparent().toRgb());
			
			// extract contour
			Mask mask = Mask.Builder.create(trimmed);
	    	List<Point.Double> contour = Trace.contourTrace(mask);
	    	
	    	// graph
			Graph.Builder graph = Graph.Builder.graph();
			Node.Builder node = graph.node();
	    	node.element(image(trimmed, mask));
	    	node.element(polyline(contour, style(Style.Fill.Builder.none(), Style.Stroke.Builder.none())));

			return graph.build();
		}
	}

	class Classifier implements org.testobject.kernel.api.classification.classifiers.Classifier {

		private static final Log log = LogFactory.getLog(Classifier.class);

		private static final boolean debug = Debug.toDebugMode(false);

		public static class Sample {

            public final Qualifier qualifier;
			public final Node graph;
			public final List<Contour> contours;
			public final ImageFingerprint print;

			public Sample(Qualifier qualifier, Node graph, List<Contour> contours) {
                this.qualifier = qualifier;
				this.graph = graph;
				this.contours = contours;
				this.print = Shared.createFingerprint(getRawImage(graph));
			}

			private org.testobject.commons.util.image.Image.Int getRawImage(Node graph) {
				for(Edge<Element> edge : graph.getElements()) {
					if(edge.getTarget() instanceof Element.Image) {
						return ((Element.Image) edge.getTarget()).getRaw();
					}
				}

				throw new IllegalStateException();
			}
		}

		private final List<Sample> samples;

		public Classifier(List<Registry.Sample> samples) {
			this.samples = toSamples(samples);
		}

		private static List<Sample> toSamples(List<Registry.Sample> samples) {
			List<Sample> train = Lists.newArrayList(samples.size());
			for(Registry.Sample sample : samples) {
                Node graph = sample.getDepiction().getGraph();
                train.add(new Sample(sample.getQualifier(), graph, toContours(graph)));
			}

			return train;
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

		@Override
		public List<Proposal> classify(Images images, Lookup lookup, List<Mask> masks, Context context) {

			Proposal.Builder proposals = Proposal.Builder.create();
			
            Mask union = Mask.Builder.create(masks);
            
            org.testobject.commons.util.image.Image.Int image = cutByMask(images.raw(), union);
            
            ImageFingerprint test = Shared.createFingerprint(image);

            for(Sample sample : samples) {

				// geometric deformation
				double geometric = geometric(sample.contours, masks, lookup);
				log.trace("geometric " + geometric);

				// photometric deformation
				ImageFingerprint train = sample.print;
				double photometric = photometric(test, train); // FIXME also use fingerprints in matching, see segment code (en)
				log.trace("photometric " + photometric);
				
				// depiction
				Depiction depiction = Depiction.Builder.create(sample.graph, translate(union), scale(sample.graph, union));

				// proposal
				proposals.proposal(
						likelihood()
							.geometric(geometric)
							.photometric(photometric)
						.build(),
						element(union)
							.qualifier(sample.qualifier)
							.likelihood(geometric, photometric)
							.feature(depict(depiction))
							.feature(contours(sample.contours))
							.feature(position(union.getBoundingBox().getLocation()))
							.feature(size(union.getBoundingBox().getSize()))
						.build());
			}

			return proposals.build();
		}
		
		public static org.testobject.kernel.imaging.procedural.Transform.Scale scale(Node source, Mask target) {
			Dimension.Double sourceSize = Renderer.Size.getSize(source);
			Size.Int targetSize = target.getBoundingBox().getSize();
			
			return Transform.Builder.scale(targetSize.w / sourceSize.w, targetSize.h / sourceSize.h);
		}

		public static org.testobject.kernel.imaging.procedural.Transform.Translate translate(Mask mask) {
			return Transform.Builder.translate(mask.getBoundingBox().x, mask.getBoundingBox().y);
		}
		
		public static org.testobject.kernel.imaging.procedural.Transform.Translate translate(double x, double y) {
			return Transform.Builder.translate(x, y);
		}

		public static double photometric(ImageFingerprint test, ImageFingerprint train) {

			double offset = 2d;
			double limit = (ImageFingerprint.SIZE * ImageFingerprint.SIZE) / offset;

			double distance = ImageFingerprint.lumaDistanceL1(test, train);
			double error = Math.abs(distance / limit);
			double likelihood = 1d - Math.min(1d, error);

			return likelihood;
		}

		private static org.testobject.commons.util.image.Image.Int cutByMask(org.testobject.commons.util.image.Image.Int raw, Mask mask) {
			return BlobUtils.Cut.cutByMask(raw, mask);
		}

		private static double geometric(List<Contour> trainContours, List<Mask> testMasks, Lookup lookup) {

			double sum = 0;
			int n = 0;

            for(Contour trainContour : trainContours) {

                // greedy
                Mask bestMask = null;
                {
                    double bestProbability = 0d;

                    for(Mask testMask : testMasks) {
                        List<Point.Double> test = lookup.contour(testMask);
                        List<Point.Double> train = trainContour.points;

                        if(debug) {
                            plot("test", test);
                            plot("train", train);
                        }

                        double probability = match(train, test);

                        if(probability > bestProbability) {
                            bestProbability = probability;
                            bestMask = testMask;
                        }
                    }

                    sum += bestProbability;
                    n += 1;
                }

                // skip
                if(bestMask == null) {
                    continue;
                }

                // childs
                if(trainContour.childs.size() > 0) {
                    double childProbability = geometric(trainContour.childs, lookup.childs(bestMask), lookup);

                    sum += childProbability;
                    n += 1;
                }
            }

			return n == 0 ? 0 : sum / n;
		}

		private static double match(List<Point.Double> train, List<Point.Double> test) {
            if(train.isEmpty() || test.isEmpty()) {
                return 0d;
            } else {
                double distance = PolyMatch.match(train, test);
                double error = Math.abs(distance / Math.min(train.size(), test.size()));
                return Math.min(1d, Math.abs(1d - error));
            }
		}

		private static void plot(String title, List<Point.Double> contour) {

			Polyline polyline = polyline(contour, style(fill(Color.white), stroke(Color.black)));

			Graph.Builder template = graph();

			template
				.node(translate(10, 10))
					.element(polyline);

			Node node = template.build();

			render(title, node);
		}

		private static void render(final String title, final Node node) {
            VisualizerUtil.show(title, Util.render(node), 8f);
		}
		
		@Override
		public String toString() {
			return Classifier.Qualifier.Factory.Class.image;
		}

	}
	
	public static class Matcher implements org.testobject.kernel.classification.matching.Matcher {
		
		@Override
		public Match match(Locator.Descriptor descriptor1, Context context1, Locator.Descriptor descriptor2, Context context2) {
			
			// sanity
			{
				checkType(descriptor1, descriptor2, Classifier.Qualifier.Factory.Class.image);
				checkFeatures(descriptor1, descriptor2, Names.Geometric.position, Names.Geometric.size, Names.Depiction.contours);
			}
			
			// early exit
			{
				if(descriptor1.getLabel().equals(descriptor2.getLabel()) == false) {
					return Match.Factory.none();
				}
			}
			
			// probabilities
			{
				Factors factors = new Factors(descriptor1, context1, descriptor2, context2); 
				
				factors
					.size(.2)
					.position(.2)
					.contours(.6);
				
				return Match.Factory.create(factors.probabilities(), factors.probability());
			}
		}
	}

}
