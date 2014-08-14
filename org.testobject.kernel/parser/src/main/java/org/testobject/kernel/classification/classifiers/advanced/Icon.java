package org.testobject.kernel.classification.classifiers.advanced;

import static org.testobject.kernel.api.classification.classifiers.Classifier.Likelihood.Builder.likelihood;
import static org.testobject.kernel.api.classification.graph.Element.Builder.contours;
import static org.testobject.kernel.api.classification.graph.Element.Builder.depict;
import static org.testobject.kernel.api.classification.graph.Element.Builder.element;
import static org.testobject.kernel.api.classification.graph.Element.Builder.fill;
import static org.testobject.kernel.api.classification.graph.Element.Builder.position;
import static org.testobject.kernel.api.classification.graph.Element.Builder.size;
import static org.testobject.kernel.imaging.procedural.Element.Builder.polyline;
import static org.testobject.kernel.imaging.procedural.Graph.Builder.graph;
import static org.testobject.kernel.imaging.procedural.Style.Builder.stroke;
import static org.testobject.kernel.imaging.procedural.Style.Stroke.Builder.none;
import static org.testobject.kernel.imaging.procedural.Transform.Builder.translate;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.math.algebra.Size;
import org.testobject.commons.tools.plot.VisualizerUtil;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.Image.Int;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.kernel.api.classification.graph.Contour;
import org.testobject.kernel.api.classification.graph.Depiction;
import org.testobject.kernel.api.classification.graph.Locator.Descriptor;
import org.testobject.kernel.imaging.color.models.YCrCb;
import org.testobject.kernel.imaging.contours.ColorExtractor;
import org.testobject.kernel.imaging.contours.PolyMatch;
import org.testobject.kernel.imaging.contours.Trace;
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
import org.testobject.kernel.imaging.segmentation.Blob;
import org.testobject.kernel.imaging.segmentation.BlobUtils;
import org.testobject.kernel.imaging.segmentation.GraphBlobBuilder;
import org.testobject.kernel.imaging.segmentation.Mask;

/**
 * 
 * @author enijkamp
 *
 */
public interface Icon {
	
	public static class Trainer implements org.testobject.kernel.classification.classifiers.advanced.Trainer {
		
		private static final boolean debug = Debug.toDebugMode(false);

		public Icon.Classifier train(List<String> names, List<Image.Int> images) {

			List<Registry.Sample> samples = Lists.newLinkedList();
			
			for(int i = 0; i < images.size(); i++) {
                samples.add(train(images.get(i), names.get(i)));
			}
			
			return new Classifier(samples);
		}
		
		@Override
		public org.testobject.kernel.classification.classifiers.advanced.Registry.Sample train(Int image, String id) {
			
            float sigma = 0.5f;
            double threshold = 4500d;
			
			Classifier.Qualifier qualifier = Classifier.Qualifier.Factory.Class.icon(id);
			Depiction depiction = Depiction.Builder.create(toRenderGraph(image, sigma, threshold));
			
            return Registry.Sample.Factory.create(qualifier, image, depiction);
		}
		
		@Override
		public Classifier create(
				List<org.testobject.kernel.classification.classifiers.advanced.Registry.Sample> samples) {
			return new Classifier(samples);
		}

		public static Node toRenderGraph(Image.Int image, float sigma, double threshold) {
            // translation
            final int translateX = 1;
            final int translateY = 1;

			// segment		
			Image.Int large = ImageUtil.expand(image, translateX, translateY, translateX * 2, translateY * 2);
			Blob[] blobs = new GraphBlobBuilder(large.w, large.h, sigma, threshold).build(large);

            if(debug) {
                BlobUtils.Print.printBlobs(blobs[0]);
                VisualizerUtil.show("raw", large, 8f);
				VisualizerUtil.show("blobs", BlobUtils.Draw.drawHierarchy(blobs), 8f);
			}
			
			// locate
			Rectangle.Int query = new Rectangle.Int(translateX, translateY, image.w, image.h);
			List<Blob> candidates = BlobUtils.Locate.locate(blobs[0], query);
			
			// trace
			Graph.Builder graph = Graph.Builder.graph();
			Node.Builder node = graph.node();
			for(Blob candidate : candidates) {
				toNode(large, candidate, new Point.Int(translateX, translateY), node);
			}
			
			return graph.build();
		}
	    
	    private static void toNode(Image.Int image, Blob blob, Point.Int parentOffset, Node.Builder parent) {
	    	    	
	    	if(debug) {
		    	// VisualizerUtil.show("blob", BlobUtils.Cut.cutByMask(image, blob), 8f);
	    	}
	    	
			// transformations
			int translateX = (blob.bbox.x - parentOffset.x);
			int translateY = (blob.bbox.y - parentOffset.y);
	    	
			// contour
	    	List<Point.Double> contour = Trace.contourTrace(blob);
	    	Color color = ColorExtractor.extractColor(image, blob);

	    	// node
	    	Node.Builder node = Node.Builder.node(parent, translate(translateX, translateY));
	    	node.element(polyline(contour, Style.Builder.style(Style.Builder.fill(color), none())));
	    	
	    	// childs
			for (Blob childBlob : blob.children) {
				toNode(image, childBlob, toPoint(blob.bbox), node);
			}
		}

		private static Point.Int toPoint(Rectangle.Int box) {
			return new Point.Int(box.x, box.y);
		}
	}
	
	// FIXME offset of komoot map icon is slightly off, compare blob and synthetic view -> issue most likely in icon classifier offset
	// from mask union (en)
	public static class Classifier implements org.testobject.kernel.api.classification.classifiers.Classifier {
		
		private static final Log log = LogFactory.getLog(Classifier.class);

		private static final boolean debug = Debug.toDebugMode(false);
		
		public static class Sample {

            public final Qualifier qualifier;
			public final Node graph;
			public final List<Contour> contours;

			public Sample(Qualifier qualifier, Node graph, List<Contour> contours) {
                this.qualifier = qualifier;
				this.graph = graph;
				this.contours = contours;
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
            if(elements.size() > 1) {
                throw new IllegalStateException();
            }

            if(elements.isEmpty()) {
                throw new IllegalStateException();
            }

            Element element = elements.get(0).getTarget();
            return ContourExtractor.getContour(element);
        }

        @Override
		public List<Proposal> classify(Images images, Lookup lookup, List<Mask> masks, Context context) {
			
			Proposal.Builder proposals = Proposal.Builder.create();

            Mask union = Mask.Builder.create(masks);
			
			for(Sample sample : samples) {
				
				// geometric deformation
				double geometric = geometric(sample.contours, masks, lookup);
				log.trace("geometric " + geometric);
				
				// photometric deformation
				double photometric = photometric(sample.graph, images, union);
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
							.feature(fill(ColorExtractor.extractColor(images.raw(), union)))
						.build());
			}
	
			return proposals.build();
		}
        
		private static org.testobject.kernel.imaging.procedural.Transform.Translate translate(Mask mask) {
			return Transform.Builder.translate(mask.getBoundingBox().x, mask.getBoundingBox().y);
		}
		
		private static org.testobject.kernel.imaging.procedural.Transform.Translate translate(double x, double y) {
			return Transform.Builder.translate(x, y);
		}

		private static org.testobject.kernel.imaging.procedural.Transform.Scale scale(Node source, Mask target) {
			Dimension.Double sourceSize = Renderer.Size.getSize(source);
			Size.Int targetSize = target.getBoundingBox().getSize();
			
			return Transform.Builder.scale(targetSize.w / sourceSize.w, targetSize.h / sourceSize.h);
		}

		// TODO normalization does not spit out favorable results in some cases (en)
		private static double photometric(Node graph, Images images, Mask mask) {
			
			Image.Int realImage = BlobUtils.Cut.cutByMask(images.raw(), mask);
			Image.Int synthImage = ImageUtil.Cut.trim(Util.render(graph), Color.Builder.argb(0, 0, 0, 0).toRgb());
			
			if(debug) {
				VisualizerUtil.show("real", realImage);
				VisualizerUtil.show("synth", synthImage);
			}
			
			final int width = 100;
			final int height = 100;
			
			Image.Int lumaRealImage = ImageUtil.Convert.toImageInt(YCrCb.y(realImage), realImage);
			Image.Int lumaSynthImage = ImageUtil.Convert.toImageInt(YCrCb.y(synthImage), synthImage);

			if(debug) {
				VisualizerUtil.show("real (luma)", lumaRealImage);
				VisualizerUtil.show("synth (luma)", lumaSynthImage);
			}
			
			Image.Int grayRealImage = normalizeIntensity(lumaRealImage);
			Image.Int graySynthImage = normalizeIntensity(lumaSynthImage);
			
			BufferedImage mismatchImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			BufferedImage scaledRealImage = scale(ImageUtil.Convert.toBufferedImage(grayRealImage), width, height);
			BufferedImage scaledSynthImage = scale(ImageUtil.Convert.toBufferedImage(graySynthImage), width, height);
			
			if(debug) {
				VisualizerUtil.show("real (scaled)", scaledRealImage);
				VisualizerUtil.show("synth (scaled)", scaledSynthImage);
			}

			double error = computeError(scaledSynthImage, scaledRealImage, mismatchImage);
			double probability = Math.min(1d, Math.abs(1d - error));
			
			return probability;
		}
		
		private static BufferedImage scale(BufferedImage src, int w, int h) {
			BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = dest.createGraphics();
			{
				AffineTransform at = AffineTransform.getScaleInstance((double) w / src.getWidth(), (double) h / src.getHeight());
				g.drawRenderedImage(src, at);
			}
			g.dispose();
			return dest;
		}
		
		private static Image.Int normalizeIntensity(Image.Int in) {
			Image.Int out = new Image.Int(in.w, in.h, Image.Int.Type.ARGB);
			// interval
			int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
			for (int y = 0; y < in.h; y++) {
				for (int x = 0; x < in.w; x++) {
					int pixel = in.get(x, y);
					if (in.type == Image.Int.Type.RGB || getAlpha(pixel) != 0) {
						int intensity = toIntensity(pixel);
						min = Math.min(min, intensity);
						max = Math.max(max, intensity);
					}
				}
			}
			// normalize
			for (int y = 0; y < in.h; y++) {
				for (int x = 0; x < in.w; x++) {
					int pixel = in.get(x, y);
					if (in.type == Image.Int.Type.RGB || getAlpha(pixel) != 0) {
						int intensity = toIntensity(pixel);
						int scaled = toInt((intensity - min) * (255f / (max - min)));
						out.pixels[y * in.w + x] = ImageUtil.toInt(getAlpha(pixel), scaled, scaled, scaled);
					}
				}
			}
			return out;
		}
		
		private static final int getAlpha(int argb) {
			return (argb >> 24) & 0xff;
		}

		private final static int toInt(float value) {
			return (int) value;
		}

		private final static int toIntensity(int rgb) {
			return (rgb >> 16) & 0xff;
		}
		
		private static double computeError(BufferedImage train, BufferedImage test, BufferedImage mismatch) {
			double sum = 0f;
			int n = 0;
			for (int x = 0; x < train.getWidth(); x++) {
				for (int y = 0; y < train.getHeight(); y++) {
					if (isTransparent(train.getRGB(x, y)) == false) {
						int sampleRgb = train.getRGB(x, y);
						int candidateRgb = test.getRGB(x, y);
						double distance = distance(sampleRgb, candidateRgb);
						sum += distance;
						n++;
						mismatch.setRGB(x, y, new java.awt.Color((float) distance, (float) distance, (float) distance, 1f).getRGB());
					} else {
						mismatch.setRGB(x, y, java.awt.Color.blue.getRGB());
					}
				}
			}
			return n == 0 ? 1d : (sum / n);
		}

		private static boolean isTransparent(int argb) {
			return getAlpha(argb) == 0;
		}

		private static double distance(int rgb1, int rgb2) {
			int r1 = (rgb1 >> 16) & 0xff;
			int g1 = (rgb1 >> 8) & 0xff;
			int b1 = (rgb1 >> 0) & 0xff;

			int r2 = (rgb2 >> 16) & 0xff;
			int g2 = (rgb2 >> 8) & 0xff;
			int b2 = (rgb2 >> 0) & 0xff;

			return (1d / 3d) * (Math.abs(r1 - r2) / 255d + Math.abs(g1 - g2) / 255d + Math.abs(b1 - b2) / 255d);
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

			Polyline polyline = polyline(contour, Style.Builder.style(Style.Builder.fill(Color.white), stroke(Color.black)));
			
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
			return Classifier.Qualifier.Factory.Class.icon;
		}
	}
	
	public static class Matcher implements org.testobject.kernel.classification.matching.Matcher {

		@Override
		public Match match(Descriptor descriptor1, Context context1, Descriptor descriptor2, Context context2) {
			throw new IllegalStateException("implement");
		}
	}

}