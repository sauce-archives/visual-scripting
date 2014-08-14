package org.testobject.kernel.classification.matching;

import static org.testobject.kernel.api.util.DescriptorUtil.getContours;
import static org.testobject.kernel.api.util.DescriptorUtil.getFill;
import static org.testobject.kernel.api.util.DescriptorUtil.getFingerprint;
import static org.testobject.kernel.api.util.DescriptorUtil.getPosition;
import static org.testobject.kernel.api.util.DescriptorUtil.getSize;
import static org.testobject.kernel.api.util.VariableUtil.has;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Size;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.collections.Maps;
import org.testobject.kernel.api.classification.classifiers.Classifier.Images;
import org.testobject.kernel.api.classification.graph.Contour;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.classification.graph.Locator.Descriptor;
import org.testobject.kernel.api.classification.graph.Variable.Names;
import org.testobject.kernel.classification.classifiers.advanced.MaskSegment.Classifier;
import org.testobject.kernel.imaging.contours.PolyMatch;
import org.testobject.kernel.imaging.fingerprint.ImageFingerprint;
import org.testobject.kernel.imaging.procedural.Color;

/**
 * 
 * @author enijkamp
 *
 */
public interface Matcher {

	interface Context {

		Classifier.Images images();

		class Factory {
			public static Context create(final org.testobject.commons.util.image.Image.Int raw) {
				return new Context() {
					@Override
					public Images images() {
						return Classifier.Images.Factory.create(raw);
					}
				};
			}
		}
	}

	interface Match {

		Map<String, Double> getComponents();

		double getProbability();

		class Factory {
			public static Match none() {
				return new Match() {
					@Override
					public Map<String, Double> getComponents() {
						return Maps.<String, Double> empty();
					}

					@Override
					public double getProbability() {
						return 0d;
					}
				};
			}

			public static Match create(final Map<String, Double> components, final double probability) {
				return new Match() {
					@Override
					public Map<String, Double> getComponents() {
						return components;
					}

					@Override
					public double getProbability() {
						return probability;
					}
				};
			}
		}
	}

	Match match(Locator.Descriptor descriptor1, Context context1, Locator.Descriptor descriptor2, Context context2);

	class Util {

		public static class Factors {

			private static class Factor {
				public final String name;
				public final double probability;
				public final double weight;

				public Factor(String name, double probability, double weight) {
					this.name = name;
					this.probability = probability;
					this.weight = weight;
				}
			}

			private final List<Factor> probabilities = Lists.newLinkedList();
			private final int maxW, maxH;
			private Descriptor descriptor1, descriptor2;

			public Factors(final Locator.Descriptor descriptor1, final Context context1, final Locator.Descriptor descriptor2, final Context context2) {
				this.descriptor1 = descriptor1;
				this.descriptor2 = descriptor2;
				this.maxW = Math.max(context1.images().raw().w, context2.images().raw().w);
				this.maxH = Math.max(context1.images().raw().h, context2.images().raw().h);
			}
			
			public Factors color(double weight) {
				double probability = Util.color(descriptor1, descriptor2);
				probabilities.add(new Factor(Names.Depiction.fill, probability, weight));
				return this;
			}

			public Factors contours(double weight) {
				double probability = Util.contours(descriptor1, descriptor2);
				probabilities.add(new Factor(Names.Depiction.contours, probability, weight));
				return this;
			}

			public Factors position(double weight) {
				Point.Int point1 = getPosition(descriptor1);
				Point.Int point2 = getPosition(descriptor2);
				double probability = Util.position(maxW, maxH, point1, point2);
				probabilities.add(new Factor(Names.Geometric.position, probability, weight));
				return this;
			}

			public Factors size(double weight) {
				double probability = Util.size(descriptor1, descriptor2);
				probabilities.add(new Factor(Names.Geometric.size, probability, weight));
				return this;
			}

			public Factors fingerprint(double weight) {
				double probability = Util.fingerprints(getFingerprint(descriptor1), getFingerprint(descriptor2));
				probabilities.add(new Factor(Names.Depiction.fingerprint, probability, weight));
				return this;
			}

			public Map<String, Double> probabilities() {
				Map<String, Double> map = new HashMap<>();
				for (Factor probability : probabilities) {
					map.put(probability.name, probability.probability);
				}
				return map;
			}

			public double probability() {
				double sum = 0d, weights = 0d;

				for (Factor probability : probabilities) {
					sum += (probability.weight * probability.probability);
					weights += probability.weight;
				}

				if (weights != 1d) {
					throw new IllegalStateException();
				}

				return sum;
			}
		}

		public static void checkType(Locator.Descriptor descriptor1, Locator.Descriptor descriptor2, String type) {
			for (Locator.Descriptor descriptor : new Locator.Descriptor[] { descriptor1, descriptor2 }) {
				if (type.equals(descriptor.getLabel().getType()) == false) {
					throw new IllegalArgumentException("'" + type + "' != '" + descriptor.getLabel().getType() + "'");
				}
			}
		}

		public static void checkFeatures(Locator.Descriptor descriptor1, Locator.Descriptor descriptor2, String... variables) {
			for (Locator.Descriptor descriptor : new Locator.Descriptor[] { descriptor1, descriptor2 }) {
				for (String feature : variables) {
					if (has(descriptor.getFeatures(), feature) == false) {
						throw new IllegalArgumentException("descriptor '" + descriptor.getLabel() + "' is missing mandatory feature '"
								+ feature + "'");
					}
				}
			}
		}

		public static double color(Locator.Descriptor descriptor1, Locator.Descriptor descriptor2) {
			
			Color fill1 = getFill(descriptor1);
			Color fill2 = getFill(descriptor2);
			
			return Color.l2(fill1, fill2);
		}

		public static double contours(Locator.Descriptor descriptor1, Locator.Descriptor descriptor2) {

			List<Contour> contours1 = getContours(descriptor1);
			List<Contour> contours2 = getContours(descriptor2);

			return contours(contours1, contours2);
		}

		public static double fingerprints(ImageFingerprint print1, ImageFingerprint print2) {

			double offset = 2d;
			double limit = (ImageFingerprint.SIZE * ImageFingerprint.SIZE) / offset;

			double distance = ImageFingerprint.lumaDistanceL1(print1, print2);
			double error = Math.abs(distance / limit);
			double likelihood = 1d - Math.min(1d, error);

			return likelihood;
		}

		public static double position(int maxH, int maxW, Point.Int point1, Point.Int point2) {

			double max = Math.sqrt(maxH * maxH + maxW * maxW);

			double distance = l2(point1, point2);
			double factor = (distance / max) * 5;

			return Math.exp(-(factor * factor));
		}

		public static double size(Locator.Descriptor descriptor1, Locator.Descriptor descriptor2) {

			Size.Int size1 = getSize(descriptor1);
			Size.Int size2 = getSize(descriptor2);

			double area1 = size1.w * size1.h;
			double area2 = size2.w * size2.h;

			double factor = (Math.max(area1, area2) / Math.min(area1, area2)) - 1;

			return Math.exp(-(factor * factor) / 2d);
		}

		private static double contours(List<Contour> trainContours, List<Contour> testContours) {

			double sum = 0;
			int n = 0;

			for (Contour trainContour : trainContours) {

				// greedy
				Contour bestContour = null;
				{
					double bestProbability = 0d;

					for (Contour testContour : testContours) {
						List<Point.Double> test = testContour.points;
						List<Point.Double> train = trainContour.points;

						double probability = match(train, test);

						if (probability > bestProbability) {
							bestProbability = probability;
							bestContour = testContour;
						}
					}

					sum += bestProbability;
					n += 1;
				}

				// skip
				if (bestContour == null) {
					continue;
				}

				// childs
				if (trainContour.childs.size() > 0) {
					double childProbability = contours(trainContour.childs, bestContour.childs);

					sum += childProbability;
					n += 1;
				}
			}

			return n == 0 ? 0 : sum / n;
		}

		private static double match(List<Point.Double> train, List<Point.Double> test) {
			if (train.isEmpty() || test.isEmpty()) {
				return 0d;
			} else {
				double distance = PolyMatch.match(train, test);
				double error = Math.abs(distance / Math.min(train.size(), test.size()));
				return Math.min(1d, Math.max(0, 1d - error));
			}
		}

		private static double l2(org.testobject.commons.math.algebra.Point.Int p1, org.testobject.commons.math.algebra.Point.Int p2) {
			return Math.sqrt(((p1.x - p2.x) * (p1.x - p2.x)) + ((p1.y - p2.y) * (p1.y - p2.y)));
		}
	}

}
