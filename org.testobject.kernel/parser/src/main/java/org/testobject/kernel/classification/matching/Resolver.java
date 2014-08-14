package org.testobject.kernel.classification.matching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.math.statistics.Histogram;
import org.testobject.commons.tools.plot.VisualizerUtil;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.collections.Maps;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.Image.Int;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.commons.util.log.LogUtil;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.classification.graph.Locator.Node;
import org.testobject.kernel.api.classification.graph.Locator.Qualifier;
import org.testobject.kernel.api.classification.graph.Variable;
import org.testobject.kernel.api.util.DescriptorUtil;
import org.testobject.kernel.api.util.VariableUtil;
import org.testobject.kernel.classification.classifiers.Segment;
import org.testobject.kernel.classification.matching.Matcher.Util;
import org.testobject.kernel.classification.matching.Matching.Match;
import org.testobject.kernel.imaging.fingerprint.ImageFingerprint;
import org.testobject.kernel.imaging.fingerprint.YCrCb;
import org.testobject.kernel.imaging.segmentation.Mask;
import org.testobject.matching.image.OpenCVMatcher;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * 
 * @author enijkamp
 *
 */
public class Resolver {

	private static final Log log = LogFactory.getLog(Resolver.class);

	public static interface GetLocatorImage {

		Image.Int get(Locator.Descriptor locator);

	}

	public static interface CreateDescriptorId {
		int createId();
	}

	public static class Resolution {

		public final Locator.Qualifier qualifier;
		public final double probability;
		public final double scale;
		public final Map<String, Double> factors;
		public final boolean resolved;

		public Resolution(Qualifier qualifier, double probability, double scale, Map<String, Double> factors, boolean resolved) {
			this.qualifier = qualifier;
			this.probability = probability;
			this.scale = scale;
			this.factors = factors;
			this.resolved = resolved;
		}

		public static class Factory {
			public static Resolution none() {
				return new Resolution(Qualifier.Factory.none(), 0d, 1.0, Maps.<String, Double> empty(), false);
			}
		}
	}

	public interface Strategy {

		Resolution resolve(Locator.Node target, Locator.Qualifier path);

	}

	public static class ResolveByPath implements Strategy {

		private final Matching matching;

		public ResolveByPath(Matching matching) {
			this.matching = matching;
		}

		@Override
		public Resolution resolve(Locator.Node target, Locator.Qualifier path) {

			// path
			LinkedList<Locator.Descriptor> targetPath = new LinkedList<>();
			List<Locator.Node> targetLocators = Lists.toList(target);
			Iterator<Locator.Descriptor> iter = path.getPath().iterator();

			double probability = 0d;

			// traverse
			while (iter.hasNext() && !targetLocators.isEmpty()) {

				// next
				Locator.Descriptor current = iter.next();

				// debug
				{
					log.trace("looking for path locator:");
					trace(current);
				}

				// match
				Match match = matching.best(targetLocators, current);
				probability = match.probability;

				// append
				Locator.Node targetLocator = match.locator;
				{
					// debug
					log.trace("selected path locator (probability=" + match.probability + "):");
					trace(targetLocator);

					// add
					targetPath.add(targetLocator.getDescriptor());
				}

				// continue
				if (iter.hasNext()) {
					// next layer
					targetLocators = targetLocator.getChildren();
				}
			}

			return new Resolution(Locator.Qualifier.Factory.create(targetPath), probability, 1.0, Maps.<String, Double> empty(), true);
		}
	}

	public static class ResolveByTail implements Strategy {

		private final Matching matching;

		public ResolveByTail(Matching matching) {
			this.matching = matching;
		}

		@Override
		public Resolution resolve(Locator.Node target, Locator.Qualifier path) {

			// tail
			Locator.Descriptor tail = path.getPath().getLast();

			// matching
			Queue<Resolution> queue = new PriorityQueue<>(256, new Comparator<Resolution>() {
				@Override
				public int compare(Resolution r1, Resolution r2) {
					return Double.compare(r2.probability, r1.probability);
				}
			});
			match(target, tail, matching, new LinkedList<Locator.Descriptor>(), queue);

			// pick
			return queue.poll();

		}

		private void match(Locator.Node target, Locator.Descriptor descriptor, Matching matching, LinkedList<Locator.Descriptor> parents,
				Queue<Resolution> queue) {

			// match
			double match = matching.match(target.getDescriptor(), descriptor);

			// path
			LinkedList<Locator.Descriptor> path = Lists.concat(parents, target.getDescriptor());
			Resolution resolution = new Resolution(Qualifier.Factory.create(path), match, 1.0, Maps.<String, Double> empty(), true);
			queue.add(resolution);

			// traverse
			for (Locator.Node child : target.getChildren()) {
				match(child, descriptor, matching, path, queue);
			}
		}
	}

	// FIXME get rid of this, required for GuiMatchingTool (en)
	private final static boolean STORE_MASKS_FOR_GUI_TOOL = false;

	public static class ResolveByScan implements Strategy {

		public final static class ScanMatch {
			public final double fingerMatch;
			public final double positionMatch;
			public final int x, y;
			private final boolean ignorePosition;

			public ScanMatch(double fingerMatch, double positionMatch, int x, int y, boolean ignorePosition) {
				this.fingerMatch = fingerMatch;
				this.positionMatch = positionMatch;
				this.x = x;
				this.y = y;
				this.ignorePosition = ignorePosition;
			}

			public double getProbability() {
				if (ignorePosition == false) {
					return fingerMatch * 0.95 + positionMatch * 0.05;
				} else {
					return fingerMatch;
				}
			}
		}

		public final static class IntensiveScanMatch {
			public final double fingerMatch;
			public final double positionMatch;
			public final double stddevMatch;
			public final double stddev;
			public final int x, y;
			public final double probability;
			private final boolean ignorePosition;

			public IntensiveScanMatch(double fingerMatch, double positionMatch, double stddevMatch, double stddev, int x, int y,
					boolean ignorePosition) {
				this.fingerMatch = fingerMatch;
				this.positionMatch = positionMatch;
				this.stddevMatch = stddevMatch;
				this.stddev = stddev;
				this.x = x;
				this.y = y;
				this.ignorePosition = ignorePosition;
				this.probability = computeProbability();
			}

			public double computeProbability() {
				boolean belowThreshold = (fingerMatch < 0.5) || (stddevMatch < 0.5) || (positionMatch < 0.5);
				double penalty = belowThreshold ? -0.1d : 0.0d;

				// if position is ignored all we need is fingerprint match
				if (ignorePosition) {
					return fingerMatch;
				} else {
					if (stddev > 40) {
						return fingerMatch * 0.5 + stddevMatch * 0.1 + positionMatch * 0.4 + penalty;
					} else {
						return fingerMatch * 0.65 + stddevMatch * 0.3 + positionMatch * 0.05 + penalty;
					}
				}
			}

			public double getProbability() {
				return probability;
			}

			public Map<String, Double> toMap() {
				Map<String, Double> map = new HashMap<>();
				{
					map.put("fingerprint", fingerMatch);
					map.put("position", positionMatch);
					map.put("intensity (stddev)", stddevMatch);
				}

				return map;
			}
		}

		public final static class MatchFingerComparator implements Comparator<ScanMatch> {
			@Override
			public int compare(ScanMatch m1, ScanMatch m2) {
				return Double.compare(m2.fingerMatch, m1.fingerMatch);
			}
		}

		public final static class MatchWeightComparator implements Comparator<IntensiveScanMatch> {
			@Override
			public int compare(IntensiveScanMatch m1, IntensiveScanMatch m2) {
				return Double.compare(m2.getProbability(), m1.getProbability());
			}
		}

		private final static boolean debug = Debug.toDebugMode(false);

		private final static double EXPAND_LOCATOR_BOX_SCREEN_PERCENT = 0.25d;
		private final static double LOWER_BOUND_FINGER = 0.7d;
		private final static double LOWER_BOUND_POSITION = 0.5d;

		private final CreateDescriptorId createDescriptorId;

		private final Image.Int replayRaw;
		private final Image.Int replayScaledRaw;
		private final Image.Double replayYRaw;

		private final Image.Int recordRaw;
		private final Image.Int recordScaledRaw;

		private final boolean ignorePosition;

		// FIXME use proper scaling, this wont work for small segments (en)
		// FIXME scaling the framebuffer each time is expensive and not necessary (en)
		public ResolveByScan(Image.Int recordRaw, Image.Int replayRaw, CreateDescriptorId createDescriptorId, boolean ignorePosition) {
			Preconditions.checkNotNull(recordRaw, "recordRaw");
			Preconditions.checkNotNull(replayRaw, "replayRaw");

			this.createDescriptorId = createDescriptorId;
			this.recordRaw = recordRaw;
			this.recordScaledRaw = ImageUtil.Convert.toImage(ImageUtil.scale(ImageUtil.Convert.toBufferedImage(recordRaw), recordRaw.w / 2,
					recordRaw.h / 2));
			this.replayRaw = replayRaw;
			this.replayScaledRaw = ImageUtil.Convert.toImage(ImageUtil.scale(ImageUtil.Convert.toBufferedImage(replayRaw), replayRaw.w / 2,
					replayRaw.h / 2));
			this.replayYRaw = YCrCb.y(this.replayScaledRaw);

			this.ignorePosition = ignorePosition;
		}

		@Override
		public Resolution resolve(Locator.Node target, Locator.Qualifier path) {

			// descriptor
			Locator.Descriptor descriptor = path.getPath().getLast();
			Rectangle.Int box = DescriptorUtil.getBoundingBox(descriptor);

			// image
			Image.Int segment = ImageUtil.Cut.crop(recordRaw, box);
			Image.Int scaledSegment = ImageUtil.Cut.crop(recordScaledRaw, new Rectangle.Int(box.x / 2, box.y / 2, box.w / 2, box.h / 2));
			Image.Double ySegment = YCrCb.y(scaledSegment);

			// scan
			// ## phase 1: co-local scan with respect to the locators position
			long beginScan1 = System.currentTimeMillis();
			Rectangle.Int bounds = new Rectangle.Int(0, 0, replayRaw.w, replayRaw.h);
			Rectangle.Int expandedBox = ignorePosition ? bounds : expand(box, bounds, EXPAND_LOCATOR_BOX_SCREEN_PERCENT);
			Rectangle.Int scaledBox = new Rectangle.Int(expandedBox.x / 2, expandedBox.y / 2, expandedBox.w / 2, expandedBox.h / 2);
			List<ScanMatch> quickScanCandidates = quickScanWithTiles(replayYRaw, ySegment, box.getLocation(), scaledBox);
			long endScan1 = System.currentTimeMillis();

			// early exit
			if (quickScanCandidates.isEmpty()) {
				return unresolved();
			}

			// ## phase 2: intensive scan using multiple features
			long beginScan2 = System.currentTimeMillis();
			List<IntensiveScanMatch> intensiveScanCandidates = intensiveScan(descriptor, segment, quickScanCandidates);
			IntensiveScanMatch bestCandidate = intensiveScanCandidates.get(0);
			long endScan2 = System.currentTimeMillis();

			// log
			if (log.isTraceEnabled()) {
				log.trace("scan 1 -> " + (endScan1 - beginScan1));
				log.trace("scan 2 -> " + (endScan2 - beginScan2));
			}

			// translation
			Point.Int point1 = DescriptorUtil.getPosition(descriptor);
			Point.Int point2 = new Point.Int(bestCandidate.x, bestCandidate.y);

			int dx = point2.x - point1.x;
			int dy = point2.y - point1.y;

			if (debug) {
				Image.Int segment2 = ImageUtil.Cut.crop(replayRaw,
						new Rectangle.Int(bestCandidate.x, bestCandidate.y, segment.w, segment.h));
				VisualizerUtil.show("segment1", segment);
				VisualizerUtil.show("segment2", segment2);
			}

			if (debug) {
				Image.Int segment2 = ImageUtil.Cut.crop(replayRaw,
						new Rectangle.Int(bestCandidate.x, bestCandidate.y, segment.w, segment.h));
				ImageFingerprint f1 = new ImageFingerprint(ImageUtil.toSquare(segment), 0xf2, 0xf1, 0xf0);
				ImageFingerprint f2 = new ImageFingerprint(ImageUtil.toSquare(segment2), 0xf2, 0xf1, 0xf0);
				VisualizerUtil.show("f1 stddev(" + stddev(f1) + ")", f1.restoreLuma());
				VisualizerUtil.show("f2 stddev(" + stddev(f2) + ")", f2.restoreLuma());
			}

			return resolved(descriptor, box, bestCandidate, point2, dx, dy);
		}

		private Resolution unresolved() {
			return Resolution.Factory.none();
		}

		private Resolution resolved(Locator.Descriptor recordDescriptor, Rectangle.Int box, IntensiveScanMatch bestCandidate,
				Point.Int point2, int dx, int dy) {
			Locator.Descriptor replayDescriptor = createDescriptor(createDescriptorId.createId(), recordDescriptor, point2.x, point2.y,
					box.w, box.h, dx, dy);

			return new Resolution(Locator.Qualifier.Factory.create(replayDescriptor), bestCandidate.getProbability(), 1.0,
					bestCandidate.toMap(), true);
		}

		private List<ScanMatch> quickScanWithTiles(Image.Double yRaw, Image.Double ySegment, Point.Int position, Rectangle.Int region) {

			final int windowHeight = 100;
			final int windowWidth = 200;
			final int windowMatchesMax = 2;

			List<ScanMatch> result = new ArrayList<ScanMatch>();

			for (int y = region.y; y < region.y + region.h; y += windowHeight) {
				for (int x = region.x; x < region.x + region.w; x += windowWidth) {

					List<ScanMatch> windowMatches = quickScan(yRaw, ySegment, position, new Rectangle.Int(x, y, windowWidth, windowHeight));

					int windowMatchesCount = 0;
					for (ScanMatch windowMatch : windowMatches) {
						if (windowMatchesCount < windowMatchesMax) {
							result.add(windowMatch);
						}
						windowMatchesCount++;
					}

				}
			}

			return result;
		}

		public List<IntensiveScanMatch> intensiveScan(Locator.Descriptor locator, Image.Int segment, List<ScanMatch> candidates) {

			List<IntensiveScanMatch> matches = new ArrayList<>();

			for (ScanMatch candidate : candidates) {

				IntensiveScanMatch bestMatch = null;
				double bestProbability = Double.NEGATIVE_INFINITY;

				// scan odd positions since image scale-down by factor of 2 in quick-scan eliminates odd positions
				for (int y = 0; y <= 1; y++) {
					for (int x = 0; x <= 1; x++) {
						IntensiveScanMatch match = computeIntensiveMatch(locator, segment, candidate, x, y);

						if (match.fingerMatch > bestProbability) {
							bestMatch = match;
							bestProbability = match.fingerMatch;
						}
					}
				}
				matches.add(bestMatch);
			}

			Collections.sort(matches, new MatchWeightComparator());

			return matches;
		}

		private IntensiveScanMatch computeIntensiveMatch(Locator.Descriptor descriptor, Image.Int segment1, ScanMatch candidate,
				int offsetX, int offsetY) {
			// position
			Point.Int point1 = limitToBounds(DescriptorUtil.getPosition(descriptor));
			Point.Int point2 = limitToBounds(new Point.Int(candidate.x * 2 + offsetX, candidate.y * 2 + offsetY));
			double positionMatch = Matcher.Util.position(replayRaw.h, replayRaw.w, point1, point2);

			// fingerprint
			Image.Int segment2 = ImageUtil.Cut.crop(replayRaw, new Rectangle.Int(point2.x, point2.y, segment1.w, segment1.h));
			ImageFingerprint f1 = Segment.Shared.createFingerprint(segment1);
			ImageFingerprint f2 = Segment.Shared.createFingerprint(segment2);
			double fingerprintMatch = Util.fingerprints(f1, f2);

			// std dev
			double stddev1 = stddev(f1);
			double stddev2 = stddev(f2);
			double stddevMatch = Math.min(stddev1, stddev2) / Math.max(stddev1, stddev2);

			// combine linearly
			return new IntensiveScanMatch(fingerprintMatch, positionMatch, stddevMatch, stddev1, point2.x, point2.y, ignorePosition);
		}

		public Point.Int limitToBounds(Point.Int point) {

			int x = Math.max(0, point.x);
			int y = Math.max(0, point.y);

			x = Math.min(x, replayRaw.w);
			y = Math.min(y, replayRaw.h);

			return new Point.Int(x, y);
		}

		public List<ScanMatch> quickScan(Image.Double yRaw, Image.Double ySegment, Point.Int position, Rectangle.Int region) {

			List<ScanMatch> matches = new ArrayList<>();
			for (int y = region.y; y < (region.y + region.h); y++) {
				for (int x = region.x; x < (region.x + region.w); x++) {

					// image
					double fingerprintMatch = match(yRaw, ySegment, x, y);

					// position
					Point.Int point1 = position;
					Point.Int point2 = limitToBounds(new Point.Int(x * 2, y * 2));
					double positionMatch = Matcher.Util.position(replayRaw.h, replayRaw.w, point1, point2);

					// thresholds && positionMatch > LOWER_BOUND_POSITION
					if (isAboveLowerBounds(fingerprintMatch, positionMatch)) {
						matches.add(new ScanMatch(fingerprintMatch, positionMatch, x, y, ignorePosition));
					}
				}
			}

			Collections.sort(matches, new MatchFingerComparator());

			return matches;
		}

		private boolean isAboveLowerBounds(double fingerprintMatch, double positionMatch) {
			if (ignorePosition) {
				return fingerprintMatch > LOWER_BOUND_FINGER;
			} else {
				return fingerprintMatch > LOWER_BOUND_FINGER && positionMatch > LOWER_BOUND_POSITION;
			}
		}

		public Rectangle.Int expand(Rectangle.Int box, Rectangle.Int bounds, double screenPercent) {

			int amount = (int) ((bounds.w + bounds.h) / 2 * screenPercent);

			int x1 = Math.max(0, box.x - amount);
			int y1 = Math.max(0, box.y - amount);

			int x2 = Math.min(box.x + box.w + amount, bounds.w);
			int y2 = Math.min(box.y + box.h + amount, bounds.h);

			int w = x2 - x1;
			int h = y2 - y1;

			return new Rectangle.Int(x1, y1, w, h);
		}

		private double stddev(ImageFingerprint print) {
			byte[] values = new byte[print.lumaFingerprint.length];
			for (int i = 0; i < print.lumaFingerprint.length; i++) {
				double pixelDouble = print.lumaFingerprint[i];
				int pixelInt = (int) (Math.min(255, pixelDouble * 255));
				byte pixelByte = (byte) pixelInt;

				values[i] = pixelByte;
			}
			Histogram.Byte histogram = Histogram.Byte.Builder.compute(values);

			return histogram.stddev;
		}

		private double match(Image.Double image, Image.Double segment, int offsetX, int offsetY) {

			double error = 0d;

			if (offsetX + segment.w > image.w || offsetY + segment.h > image.h) {
				return 0d;
			}

			int count = 0;

			for (int localY = 0; localY < segment.h; localY++) {
				for (int localX = 0; localX < segment.w; localX++) {
					count++;

					final double y1 = segment.get(localX, localY);
					final double y2 = image.get(offsetX + localX, offsetY + localY);

					error += lumaDistanceL1(y1, y2);
				}
			}

			return limit(1d - (error / count));
		}

		private double limit(double p) {
			if (p < 0) {
				return 0;
			}
			if (p > 1) {
				return 1;
			}
			return p;
		}

		private static double lumaDistanceL1(double y1, double y2) {
			double diff = y1 - y2;
			return Math.abs(diff) / 256;
		}

	}

	private static Locator.Descriptor createDescriptor(int locatorId, Locator.Descriptor source, int x, int y, int w, int h, int dx, int dy) {

		List<Variable<?>> variables = Lists.newArrayList(source.getFeatures().size());

		// variables
		{
			for (Variable<?> variable : source.getFeatures()) {
				if (!variable.getName().equals(Variable.Names.Geometric.position)
						&& !variable.getName().equals(Variable.Names.Geometric.size)
						&& !variable.getName().equals(Variable.Names.Depiction.masks)) {
					variables.add(variable);
				}
			}
			
			variables.add(Variable.Builder
					.point(Variable.Names.Geometric.position, new org.testobject.commons.math.algebra.Point.Int(x, y)));
			variables.add(Variable.Builder.size(Variable.Names.Geometric.size, new org.testobject.commons.math.algebra.Size.Int(w, h)));

			// masks
			if (STORE_MASKS_FOR_GUI_TOOL) {
				List<Mask> translatedMasks = translateMasks(VariableUtil.getMasks(source.getFeatures()), dx, dy);
				variables.add(Variable.Builder.value(Variable.Names.Depiction.masks, translatedMasks));
			}
			
		}

		return Locator.Descriptor.Factory.create(locatorId, source.getLabel(), variables);
	}

	private static List<Mask> translateMasks(List<Mask> source, int dx, int dy) {

		List<Mask> target = Lists.newArrayList(source.size());
		for (Mask mask : source) {
			target.add(Mask.Builder.translate(mask, dx, dy));
		}

		return target;
	}

	public static class ResolveWithOpenCv implements Strategy {

		public final static class MatchWeightComparator implements Comparator<ScanMatchWithPosition> {
			@Override
			public int compare(ScanMatchWithPosition m1, ScanMatchWithPosition m2) {
				return Double.compare(m2.getProbability(), m1.getProbability());
			}
		}		
		
		public final static class ScanMatchWithPosition {
			public final double fingerMatch;
			public final double positionMatch;
			public final double probability;
			public final double scale;
			private final boolean ignorePosition;
			private final Rectangle.Int resultBox;

			public ScanMatchWithPosition(double fingerMatch, double positionMatch, double scale, Rectangle.Int resultBox,
					boolean ignorePosition) {
				this.fingerMatch = fingerMatch;
				this.positionMatch = positionMatch;
				this.scale = scale;
				this.resultBox = resultBox;
				this.ignorePosition = ignorePosition;
				this.probability = computeProbability();
			}

			public double computeProbability() {
				boolean belowThreshold = (fingerMatch < 0.5) || (positionMatch < 0.5);
				double penalty = belowThreshold ? -0.1d : 0.0d;

				// if position is ignored all we need is fingerprint match
				if (ignorePosition) {
					return fingerMatch;
				} else {
					return fingerMatch * 0.85 + positionMatch * 0.15 + penalty;
				}
			}

			public double getProbability() {
				return probability;
			}

			public Map<String, Double> toMap() {
				Map<String, Double> map = new HashMap<>();
				{
					map.put("fingerprint", fingerMatch);
					map.put("position", positionMatch);
				}

				return map;
			}
		}		
		
		private final Image.Int recordRaw;
		private final Image.Int replayRaw;
		private final CreateDescriptorId createDescriptorId;
		private final boolean ignorePosition;

		public ResolveWithOpenCv(Image.Int recordRaw, Image.Int replayRaw, CreateDescriptorId createDescriptorId, boolean ignorePosition) {
			this.recordRaw = recordRaw;
			this.replayRaw = replayRaw;
			this.createDescriptorId = createDescriptorId;
			this.ignorePosition = ignorePosition;
		}

		@Override
		public Resolution resolve(Node target, Qualifier path) {
			Preconditions.checkNotNull(recordRaw, "recordRaw");
			Preconditions.checkNotNull(replayRaw, "replayRaw");

			// descriptor
			Locator.Descriptor descriptor = path.getPath().getLast();
			Rectangle.Int box = DescriptorUtil.getBoundingBox(descriptor);
			
			Image.Int template = ImageUtil.Cut.crop(recordRaw, box);

			List<OpenCVMatcher.ScanMatch> matches = OpenCVMatcher.findMatches(replayRaw, template);

			if (matches.isEmpty()) {
				return unresolved();
			}

			List<ScanMatchWithPosition> matchesWithPosition = new LinkedList<ScanMatchWithPosition>();
			
			for (OpenCVMatcher.ScanMatch match : matches) {				
				Point.Int point1 = limitToBounds(DescriptorUtil.getPosition(descriptor));
				Point.Int point2 = limitToBounds(new Point.Int(match.result.x, match.result.y));
				double positionMatch = Matcher.Util.position(replayRaw.h, replayRaw.w, point1, point2);				
				
				matchesWithPosition.add(new ScanMatchWithPosition(match.probability, positionMatch, match.scale.doubleValue(), match.result, ignorePosition));
			}
			
			
			Collections.sort(matchesWithPosition, new MatchWeightComparator());
			
			ScanMatchWithPosition match = matchesWithPosition.get(0); 

			// translation
			Point.Int point1 = DescriptorUtil.getPosition(descriptor);
			Point.Int point2 = new Point.Int(match.resultBox.x, match.resultBox.y);

			int dx = point2.x - point1.x;
			int dy = point2.y - point1.y;

			return resolved(descriptor, match.resultBox, match, point2, dx, dy);
		}

		public Point.Int limitToBounds(Point.Int point) {

			int x = Math.max(0, point.x);
			int y = Math.max(0, point.y);

			x = Math.min(x, replayRaw.w);
			y = Math.min(y, replayRaw.h);

			return new Point.Int(x, y);
		}		
		
		private Resolution unresolved() {
			return Resolution.Factory.none();
		}

		private Resolution resolved(Locator.Descriptor recordDescriptor, Rectangle.Int box, ScanMatchWithPosition match,
				Point.Int point2, int dx, int dy) {

			Locator.Descriptor replayDescriptor = createDescriptor(createDescriptorId.createId(), recordDescriptor, box.x, box.y,
					box.w, box.h, dx, dy);

			return new Resolution(Locator.Qualifier.Factory.create(replayDescriptor), match.probability, match.scale, Maps.<String, Double> empty(),
					true);
		}

	}

	private final double thresholdFail;
	private final double thresholdFuzzy;
	private final Strategy[] strategies;

	public Resolver(double thresholdFail, double thresholdFuzzy, boolean ignorePosition, Matching matching, Image.Int recordRaw,
			Image.Int replayRaw,
			CreateDescriptorId createDescriptorId) {
		
		this.thresholdFail = thresholdFail;
		this.thresholdFuzzy = thresholdFuzzy;
		this.strategies = new Strategy[] {
				//					new ResolveByPath(),
				//					new ResolveByTail(),
				//new ResolveByScan(recordRaw, replayRaw, createDescriptorId, ignorePosition)
				new ResolveWithOpenCv(recordRaw, replayRaw, createDescriptorId, ignorePosition)
		};
	}

	public Resolution resolve(Locator.Node target, Locator.Qualifier path) {
		// debug
		{
			log.trace("locators:");
			trace(target);
			log.debug("path: " + Locator.Printer.toString(path.getPath()));
		}

		// matching
		Resolution best = Resolution.Factory.none();

		for (Strategy strategy : strategies) {
			// go
			Resolution current = strategy.resolve(target, path);
			log.debug(String.format("resolving by strategy '" + strategy.getClass().getSimpleName() + "' with probability %.2f",
					current.probability));
			if (current.probability > best.probability) {
				best = current;
			}

			// early exit
			if (current.probability > thresholdFuzzy) {
				break;
			}
		}

		// debug
		{
			String outcome = "failed";
			if (best.probability > thresholdFail) {
				outcome = "fuzzy";
			}
			if (best.probability > thresholdFuzzy) {
				outcome = "exact";
			}
			String match = String.format(" - matched locator '%s' with probability %.2f", Locator.Printer.toString(path.getPath()),
					best.probability);
			log.debug(outcome + match);
		}

		return best;
	}

	public Set<Resolution> resolveAll(Locator.Node target, Locator.Qualifier path) {

		// matching
		Set<Resolution> results = Sets.newHashSet();

		for (Strategy strategy : strategies) {
			Resolution current = strategy.resolve(target, path);
			results.add(current);
		}

		return results;
	}

	private static void trace(Locator.Node node) {
		Locator.Printer.print(node, 1, LogUtil.getTraceStream(log), Variable.Names.Geometric.position, Variable.Names.Geometric.size);
	}

	private static void trace(Locator.Descriptor desciptor) {
		Locator.Printer.print(desciptor, 1, LogUtil.getTraceStream(log), Variable.Names.Geometric.position, Variable.Names.Geometric.size);
	}
}
