package org.testobject.kernel.ocr;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.BooleanRaster;
import org.testobject.kernel.imgproc.blob.BoundingBox;
import org.testobject.kernel.imgproc.classifier.Classes;
import org.testobject.kernel.imgproc.plot.VisualizerUtil;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.debug.StopWatch;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.log.LogUtil;

// TODO issues with 'f' -> high mismatch
// TODO special cases -> 'rn' != 'm'
// TODO subpixel overlapping with next char for 'r', 'f' -> learn histograms with additive subpixels
// TODO only use best-masks in greedy fashion or pruning e.g. using vertical histograms over word and single chars
// to prune? (en)

// TODO multiply average additive masks with input image to weight average intensities? -> 'o' would fit, only apply
// for outer contours (en)
// TODO put key thoughts on paper (over-sized masks, cutting e.g. small chars, remaining pixels, ...), plots,
// samples, ...

// NOTE is using only one histogram of additive mask sufficient? -> no, distance increases (en)
public class SlidingMaskTextRecognizer implements TextRecognizer<Blob> {

	private Log log = LogFactory.getLog(SlidingMaskTextRecognizer.class);

	private static boolean DEBUG = Debug.toDebugMode(false);

	public static class MaskMatch {
		public final double dist;
		public final String chars;
		public final AdditiveMask mask;
		public final int offsetX, offsetY;
		public final CharacterMask characterMask;
		public final int fill;

		public MaskMatch(double dist, String chars, AdditiveMask mask, CharacterMask characterMask, int offsetX, int offsetY) {
			this.dist = dist;
			this.chars = chars;
			this.mask = mask;
			this.characterMask = characterMask;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			this.fill = 0;
		}
	}

	public static class Word {
		public final List<MaskMatch> letters;
		public final int mismatchIntensity;
		public final double averageDistance;
		public final double sumDistance;

		public Word(List<MaskMatch> letters, int mismatchIntensity, double averageDistance, double sumDistance) {
			this.sumDistance = sumDistance;
			this.letters = letters;
			this.mismatchIntensity = mismatchIntensity;
			this.averageDistance = averageDistance;
		}

		@Override
		public String toString() {
			String chars = "";
			for (MaskMatch match : letters) {
				chars += match.chars;
			}
			return chars;
		}
	}

	private static final boolean PRUNE_WITH_CLASSIFIER_RESULTS = true;
	private static final boolean PRUNE_FALLBACK_WITH_FONTSIZES = true;

	private static final int MAX_BLOB_AREA = 600;

	private final Decolorizer decolorizer = new Decolorizer();

	private final int histogramN;
	private final double maxDistance;
	private final int maxToleranceX;
	private final List<AdditiveMask> additiveMasks;

	public SlidingMaskTextRecognizer(List<AdditiveMask> additiveMasks, int histogramN, double mAX_DIST, int mAX_TOLERANCE_X) {
		this.histogramN = histogramN;
		this.maxDistance = mAX_DIST;
		this.maxToleranceX = mAX_TOLERANCE_X;
		this.additiveMasks = additiveMasks;
	}

	private static class LeftToRightSorter implements Comparator<Blob> {
		@Override
		public int compare(Blob b1, Blob b2) {
			return Integer.compare(b1.bbox.x, b2.bbox.x);
		}
	}

	@Override
	public Match<Blob> recognize(Image.Int image, List<Blob> blobs) {

		// sort by x
		Collections.sort(blobs, new LeftToRightSorter());

		// recognize chars
		List<Word> words = new LinkedList<>();
		for (Blob blob : blobs) {

			// large blobs are most likely mis-classified
			if (blob.area > MAX_BLOB_AREA || blob.bbox.width > 100 || blob.bbox.height > 30) {
				log.warn("area of blob " + blob.id + " exceeds feasible size");
				continue;
			}

			// determine font sizes
			Classes.TextChar textMeta = (Classes.TextChar) blob.meta;
			Set<Integer> fontSizes = getFontSizes(blob.backpointers);
			if (fontSizes.isEmpty()) {
				for (Classes.TextChar.Candidate candidate : textMeta.candidates) {
					fontSizes.add(candidate.size);
				}
			}

			// FIXME remove, added for komoot demo (en)
			/*
			List<Integer> sizes = new ArrayList<>(fontSizes);
			Collections.sort(sizes, new Comparator<Integer>() {
				@Override
				public int compare(Integer x, Integer y) {
					return Integer.compare(x, y);
				}
			});
			if (sizes.isEmpty() || sizes.get(0) > 16) {
				return new Match<>(Collections.<Word> emptyList());
			}
			*/

			// TODO add support for diacritics (en)
			// Image.Int decolor = decolorizer.decolorize(image, includeDiacriticPoint(image, blob));
			Image.Int decolor = decolorizer.decolorize(image, blob);

			if (DEBUG) {
				VisualizerUtil.show("decolorized input", decolor, 20f);
			}

			LinkedList<Word> text = classifyText(decolor, blob.bbox.width, fontSizes, textMeta);

			words.add(text.getFirst());
		}

		return new Match<>(words);
	}

	// FIXME replace intensity by fore / background color from decolorizer code (en)
	private <T extends BooleanRaster & BoundingBox> Raster includeDiacriticPoint(Image.Int image, T raster) {
		final int TOLERANCE_Y = 6;
		final int TOLERANCE_INTENSITY = 50;

		final int minIntensity = getMinIntensity(image, raster);

		boolean[][] newRaster = new boolean[raster.getBoundingBox().height + TOLERANCE_Y][raster.getBoundingBox().width];

		// copy old values
		for (int y = 0; y < raster.getBoundingBox().height; y++) {
			for (int x = 0; x < raster.getBoundingBox().width; x++) {
				newRaster[y + TOLERANCE_Y][x] = raster.get(x, y);
			}
		}

		// check for diacritics
		int minY = TOLERANCE_Y;
		for (int y = 0; y > -TOLERANCE_Y; y--) {
			for (int x = 0; x < raster.getBoundingBox().width; x++) {
				int intensity = image.get(raster.getBoundingBox().x + x, raster.getBoundingBox().y + y) >> 16 & 0xff;
				int diff = Math.abs(minIntensity - intensity);
				if (diff < TOLERANCE_INTENSITY) {
					newRaster[TOLERANCE_Y + y][x] = true;
					minY = Math.min(minY, y);
				}
			}
		}

		Rectangle newBox = new Rectangle(raster.getBoundingBox().x, raster.getBoundingBox().y - minY, raster.getBoundingBox().width,
				raster.getBoundingBox().height + minY);

		return new Raster(newRaster, newBox, -minY);
	}

	private static class Raster implements BooleanRaster, BoundingBox {

		private final boolean[][] raster;
		private final Rectangle box;
		private final int offsetY;

		public Raster(boolean[][] raster, Rectangle box, int offsetY) {
			this.raster = raster;
			this.box = box;
			this.offsetY = offsetY;
		}

		@Override
		public Rectangle getBoundingBox() {
			return box;
		}

		@Override
		public Dimension getSize() {
			return new Dimension(box.width, box.height);
		}

		@Override
		public boolean get(int x, int y) {
			return raster[y + offsetY][x];
		}

		@Override
		public void set(int x, int y, boolean what) {

		}
	}

	private <T extends BooleanRaster & BoundingBox> int getMinIntensity(Image.Int image, T raster) {
		int min = Integer.MAX_VALUE;
		for (int y = 0; y < raster.getBoundingBox().height; y++) {
			for (int x = 0; x < raster.getBoundingBox().width; x++) {
				if (raster.get(x, y)) {
					int intensity = image.get(raster.getBoundingBox().x + x, raster.getBoundingBox().y + y) >> 16 & 0xff;
					min = Math.min(min, intensity);
				}
			}
		}
		return min;
	}

	private Set<Integer> getFontSizes(List<Blob> backpointers) {
		Set<Integer> fontSizes = new HashSet<>();
		for (Blob blob : backpointers) {
			if (blob.meta instanceof Classes.TextWord) {
				Classes.TextWord textGroup = (Classes.TextWord) blob.meta;
				fontSizes.addAll(textGroup.fontSizes);
			}
		}
		return fontSizes;
	}

	public LinkedList<Word> classifyText(Image.Int source, int width, Set<Integer> fontSizes, Classes.TextChar text) {
		StopWatch watch = new StopWatch(LogUtil.getDebugStream(log));

		Image.Int fatImage = fatImage(source);
		Image.Byte byteImage = ImageUtil.toImageByte(fatImage);

		watch.start("calculateMaskMap");
		Queue<MaskMatch> masks = calculateMasks(byteImage, width, fontSizes, text);
		watch.stop();

		watch.start("calculateTopCharacters");
		List<MaskMatch> topK = calculateTopCharacters(masks);
		watch.stop();

		watch.start("calculateTopWords");
		// FIXME reuse byteImage? (en)
		Queue<Word> topWords = calculateTopWords(fatImage, topK);
		watch.stop();

		watch.start("normalizeTopWords");
		LinkedList<Word> bestWords = normalizeTopWords(topWords);
		watch.stop();

		return bestWords;
	}

	private Image.Byte cutImage(Image.Byte source, Rectangle targetImageRect, Rectangle cutImagekRect) {
		Image.Byte dest = new Image.Byte((int) targetImageRect.getWidth(), (int) targetImageRect.getHeight());

		Arrays.fill(dest.pixels, ImageUtil.toByte(Color.white.getRGB()));

		int sourceX = cutImagekRect.x;
		int targetX = targetImageRect.x;
		int copyHeight = Math.min(dest.h, source.h - cutImagekRect.y);
		int copyWidth = Math.min(dest.w - targetX, source.w - sourceX);
		for (int y = 0; y < copyHeight; y++) {

			System.arraycopy(source.pixels, (cutImagekRect.y + y) * source.w + sourceX, dest.pixels,
					y * dest.w + targetX, copyWidth);
		}

		return dest;
	}

	private LinkedList<Word> normalizeTopWords(Queue<Word> topWords) {
		// TODO top-k only to limit extreme outliers in median? (en)
		LinkedList<Word> bestWords = new LinkedList<SlidingMaskTextRecognizer.Word>();
		while (topWords.isEmpty() == false) {
			bestWords.add(topWords.poll());
		}

		Collections.sort(bestWords, new Comparator<Word>() {
			@Override
			public int compare(Word w1, Word w2) {
				return Double.compare(w1.averageDistance, w2.averageDistance);
			}
		});
		final double medianAvgDist = bestWords.get(bestWords.size() / 2).averageDistance;

		Collections.sort(bestWords, new Comparator<Word>() {
			@Override
			public int compare(Word w1, Word w2) {
				return Double.compare(w1.sumDistance, w2.sumDistance);
			}
		});
		final double medianSumDist = bestWords.get(bestWords.size() / 2).sumDistance;

		Collections.sort(bestWords, new Comparator<Word>() {
			@Override
			public int compare(Word w1, Word w2) {
				return Integer.compare(w1.mismatchIntensity, w2.mismatchIntensity);
			}
		});
		final double medianIntensity = bestWords.get(bestWords.size() / 2).mismatchIntensity;

		// REMARK: plausibility is inverted
		Collections.sort(bestWords, new Comparator<Word>() {
			@Override
			public int compare(Word w1, Word w2) {
				double plausibility1 = divide(w1.averageDistance, medianAvgDist) + divide(w1.sumDistance, medianSumDist)
						+ divide(w1.mismatchIntensity, medianIntensity);
				double plausibility2 = divide(w2.averageDistance, medianAvgDist) + divide(w2.sumDistance, medianSumDist)
						+ divide(w2.mismatchIntensity, medianIntensity);
				return Double.compare(plausibility1, plausibility2);
			}
		});

		for (int i = 0; i < Math.min(50, bestWords.size()); i++) {
			Word word = bestWords.get(i);
			double normAvgDist = divide(word.averageDistance, medianAvgDist);
			double normSumDist = divide(word.sumDistance, medianSumDist);
			double normMis = divide(word.mismatchIntensity, medianIntensity);
			double normTotal = normAvgDist + normSumDist + normMis;
			log.debug(i + " " + String.format("%1$-15s", word) + "\tplaus=" + String.format("%.3f", normTotal) + "%\tavg="
					+ String.format("%.3f", normAvgDist) + "%\tsum=" + String.format("%.3f", normSumDist) + "%\tmis="
					+ String.format("%.3f", normMis) + "%\tavg=" + String.format("%.3f", word.averageDistance) + "\tsum="
					+ String.format("%.3f", word.sumDistance) + "\tmis=" + word.mismatchIntensity);
		}
		return bestWords;
	}

	private Queue<Word> calculateTopWords(final Image.Int image, List<MaskMatch> topK) {
		log.debug("### words ###");
		Queue<Word> topWords = new PriorityQueue<Word>(100000, new Comparator<Word>() {
			@Override
			public int compare(Word w1, Word w2) {
				return w1.averageDistance > w2.averageDistance ? +1 : -1;
			}
		});
		{
			// word candidates
			List<List<MaskMatch>> words = computeWordCandidates(topK, image.w);

			// sort
			final Image.Int gray = ImageUtil.toGrayscaleImage(image); // FIXME we already have grayscale image (en)
			for (List<MaskMatch> word : words) {
				int mismatchRemaining = computeMismatchIntensity(gray, word);
				double averageDistance = getAverageDistance(word);
				double sumDistance = getSumDistance(word);
				topWords.add(new Word(word, mismatchRemaining, averageDistance, sumDistance));
			}
		}
		return topWords;
	}

	private List<MaskMatch> calculateTopCharacters(Queue<MaskMatch> topMasks) {
		if (topMasks.isEmpty()) {
			return Collections.emptyList();
		}
		// top-k chars
		log.debug("### chars ###");
		List<MaskMatch> topK = new ArrayList<MaskMatch>(30);
		{
			while (!topMasks.isEmpty() && topK.size() < 30) {
				MaskMatch match = topMasks.poll();
				log.debug(match.chars + " -> " + String.format("%.3f", match.dist) + "\t" + "x = " + match.offsetX + "\ty = "
						+ match.offsetY + "\tw = " + match.characterMask.width + "\th = " + match.characterMask.height);
				topK.add(match);
			}
		}
		return topK;
	}

	Queue<MaskMatch> calculateMasks(final Image.Byte image, int width, Set<Integer> fontSizes, Classes.TextChar text) {
		log.debug(String.format("### top masks for image [w=%d, h=%d] ###", image.w, image.h));

		if (DEBUG) {
			VisualizerUtil.show("input", ImageUtil.toImageInt(image), 20f);
		}

		Comparator<MaskMatch> maskComparator = new Comparator<MaskMatch>() {
			@Override
			public int compare(MaskMatch m1, MaskMatch m2) {
				return Double.compare(m1.dist, m2.dist);
			}
		};

		Queue<MaskMatch> topMasks = new PriorityQueue<MaskMatch>(200, maskComparator);

		// stage 1 : re-use classifier hints (chars + sizes)
		boolean[] coverageStage1 = new boolean[image.w];
		if (PRUNE_WITH_CLASSIFIER_RESULTS) {
			calculateMaskWithClassifierCharsAndSize(image, width, fontSizes, text, topMasks, coverageStage1);
		}

		// stage 2 : re-use classifier hints (chars)
		boolean[] coverageStage2 = Arrays.copyOf(coverageStage1, coverageStage1.length);
		if (PRUNE_FALLBACK_WITH_FONTSIZES) {
			for (char chr : Character.getAllChars()) {
				List<AdditiveMask> masks = getMasksFor(additiveMasks, chr, fontSizes, width);
				for (int x = 0; x < image.w; x++) {
					if (coverageStage1[x]) {
						continue;
					}
					Queue<MaskMatch> topMaskAtX = getTopMasks(image, masks, x, 0);

					if (!topMaskAtX.isEmpty()) {
						MaskMatch topMask = topMaskAtX.poll();
						// cover
						for (int i = x; i < Math.min(x + topMask.characterMask.width, image.w); i++) {
							coverageStage2[i] = true;
						}

						topMasks.offer(topMask);
					}
				}
			}
		}

		// stage 3 : fallback
		for (char chr : Character.getAllChars()) {
			List<AdditiveMask> masks = getMasksFor(additiveMasks, chr, width);
			for (int x = 0; x < image.w; x++) {
				if (coverageStage2[x]) {
					continue;
				}
				Queue<MaskMatch> topMaskAtX = getTopMasks(image, masks, x, 0);

				if (!topMaskAtX.isEmpty()) {
					topMasks.offer(topMaskAtX.poll());
				}
			}
		}

		if (log.isDebugEnabled()) {
			double dist = topMasks.peek() != null ? topMasks.peek().dist : Double.NaN;
			log.debug(String.format("Found %d top masks with smallest dist %f", topMasks.size(), dist));
		}

		return topMasks;

	}

	void calculateMaskWithClassifierCharsAndSize(final Image.Byte image, int width,
			Set<Integer> fontSizes, Classes.TextChar text,
			Queue<MaskMatch> topMasks, boolean[] coverageStage1) {
		for (Classes.TextChar.Candidate candidate : text.candidates) {
			log.debug("trying proposal candidate: " + candidate);

			// if classifier cuts histograms x might be negative
			int widthReduction = Math.abs(Math.min(0, candidate.x));
			int xPosition = Math.max(0, candidate.x);

			List<AdditiveMask> masks = getMasksFor(additiveMasks, candidate.chr, candidate.size, width);
			Queue<MaskMatch> topMasksAtX = getTopMasks(image, masks, xPosition, widthReduction);

			if (!topMasksAtX.isEmpty()) {
				// cover
				for (int i = xPosition; i < Math.min(candidate.x + candidate.width, image.w); i++) {
					coverageStage1[i] = true;
				}
				// publish to topMasks
				topMasks.offer(topMasksAtX.poll());
			}
		}
	}

	private Queue<MaskMatch> getTopMasks(final Image.Byte image, List<AdditiveMask> masks, int x, int widthReduction) {

		// ############## top masks ###############
		Comparator<MaskMatch> maskComparator = new Comparator<MaskMatch>() {
			@Override
			public int compare(MaskMatch m1, MaskMatch m2) {
				return Double.compare(m1.dist, m2.dist);
			}
		};

		Queue<MaskMatch> topMaskAtX = new PriorityQueue<MaskMatch>(10, maskComparator);
		for (AdditiveMask mask : masks) {
			if (x + mask.width > image.w + maxToleranceX) {
				continue;
			}
			final int diffHeight = image.h - mask.height;
			final int yStart = 0, yEnd = diffHeight;

			for (int y = yStart; y <= yEnd; y++) {

				// rects
				Rectangle targetImageRect = new Rectangle(widthReduction, 0, mask.width, mask.height);
				Rectangle cutImageRect = new Rectangle(x, y, mask.width, mask.height);

				// cut
				Image.Byte cutImage = cutImage(image, targetImageRect, cutImageRect);
				if (DEBUG) {
					VisualizerUtil.show("cut", ImageUtil.toImageInt(cutImage), 20f);
				}

				Image.Byte maskImage = applyMask(cutImage, mask.mask);
				if (DEBUG) {
					VisualizerUtil.show("mask", ImageUtil.toImageInt(maskImage), 20f);
				}

				// FIXME (en)
				// remove because cutted images must be as wide as masks (al)
				// caution!!!!: this is not true if char fits nicely in over-sized mask (en)

				// Image.Byte trimmedImage = trimWidth(maskImage);
				// if (DEBUG) {
				// VisualizerUtil.show("mask trimmed", ImageUtil.toImageInt(trimmedImage), 20f);
				// }

				// n x n histogram
				log.trace(String.format("computing histogram for (%d, %d) (%d, %d)", x, y, mask.width, mask.height));
				double[][] testHistogram = HistogramFeature.computeHistogram(maskImage, histogramN);
				List<double[][]> charHistograms = getCharHistograms(mask);
				double distance = HistogramFeature.classify(charHistograms, testHistogram);
				log.trace(String.format("distance for character mask %s : %f", mask.chr + "", distance));

				// memorize
				// FIXME max distance shouldn't be static distance for large images is larger
				if (distance < (maxDistance * (maxDistance / 14) * mask.fontSize) && !Double.isNaN(distance)) {
					topMaskAtX.offer(new MaskMatch(distance, String.valueOf(mask.chr), mask, mask.mask, Math.max(x, 0), y));
				}
			}
		}
		return topMaskAtX;
	}

	static Image.Int fatImage(Image.Int source) {
		final Image.Int image = new Image.Int(source.w, source.h + 2);

		Arrays.fill(image.pixels, 0, image.w, Color.WHITE.getRGB());
		for (int y = 0; y < source.h; y++) {
			System.arraycopy(source.pixels, y * source.w, image.pixels, (y + 1) * source.w, source.w);
		}
		Arrays.fill(image.pixels, (image.h * image.w) - image.w, image.h * image.w, Color.WHITE.getRGB());
		return image;
	}

	private static float getSumDistance(List<MaskMatch> word) {
		float avgDistance = 0f;
		for (MaskMatch mask : word) {
			avgDistance += mask.dist;
		}
		return avgDistance;
	}

	private static float getAverageDistance(List<MaskMatch> word) {
		float avgDistance = 0f;
		for (MaskMatch mask : word) {
			avgDistance += mask.dist / word.size();
		}
		return avgDistance;
	}

	private static int computeMismatchIntensity(Image.Int gray, List<MaskMatch> word) {
		boolean[][] mask = mergeMasks(gray.w, gray.h, word);
		// mismatch pixels
		int mismatches = 0;
		for (int y = 0; y < gray.h; y++) {
			for (int x = 0; x < gray.w; x++) {
				int pixel = gray.pixels[y * gray.w + x];
				if (mask[y][x] == false && pixel != Color.white.getRGB()) {
					int intensity = (pixel & 0xff);
					mismatches += (255 - intensity);
				}
			}
		}
		return mismatches;
	}

	private static boolean[][] mergeMasks(int width, int height, List<MaskMatch> word) {
		boolean[][] mask = new boolean[height][width];
		for (MaskMatch match : word) {
			for (int y = 0; y < match.characterMask.height; y++) {
				int[] interval = match.characterMask.mask[y];
				for (int x = interval[0]; x <= interval[1]; x++) {
					if (match.offsetX + x < width) {
						mask[match.offsetY + y][match.offsetX + x] = true;
					}
				}
			}
		}
		return mask;
	}

	private List<List<MaskMatch>> computeWordCandidates(List<MaskMatch> matches, int width) {
		// sort
		Collections.sort(matches, new Comparator<MaskMatch>() {
			@Override
			public int compare(MaskMatch m1, MaskMatch m2) {
				return Integer.compare(m1.offsetX, m2.offsetX);
			}
		});
		return computeWordCandidates(matches, width, -1, new LinkedList<MaskMatch>());
	}

	private List<List<MaskMatch>> computeWordCandidates(List<MaskMatch> matches, int width, int offsetX, LinkedList<MaskMatch> word) {
		// resulting words
		List<List<MaskMatch>> words = new LinkedList<List<MaskMatch>>();

		// pick potential candidates
		boolean foundMatch = false;
		for (MaskMatch match : matches) {
			int halfMaskWidth = match.characterMask.width / 2;
			int toleranceX = maxToleranceX < halfMaskWidth ? maxToleranceX : halfMaskWidth;
			// is offset within x-tolerance and subsequent to last offset
			boolean withinTolerance = Math.abs(match.offsetX - offsetX) <= toleranceX;
			int lastWidth = word.isEmpty() ? 0 : word.getLast().characterMask.width;
			boolean notLastOffset = match.offsetX > (offsetX - lastWidth);
			if (withinTolerance && notLastOffset) {
				// this match might be the last char e.g. adds 'erik' and later adds 'erikf'
				boolean endWithinTolerance = width - offsetX - toleranceX <= 0;
				if (endWithinTolerance) {
					words.add(word);
				}
				// concat current word with next match
				LinkedList<MaskMatch> newWord = Lists.concat(word, match);
				int newOffset = match.offsetX + match.characterMask.width;
				words.addAll(computeWordCandidates(matches, width, newOffset, newWord));
				foundMatch = true;
			}
		}

		// if we found another match, then concat with word
		if (foundMatch == false) {
			words.add(word);
		}

		return words;
	}

	private List<AdditiveMask> getMasksFor(List<AdditiveMask> additiveMasks, char chr, int width) {
		List<AdditiveMask> result = new LinkedList<AdditiveMask>();
		for (AdditiveMask mask : additiveMasks) {
			if (mask.chr != chr) {
				continue;
			} else if (mask.width > width + maxToleranceX) {
				continue;
			}

			result.add(mask);
		}
		return result;
	}

	private List<AdditiveMask> getMasksFor(List<AdditiveMask> additiveMasks, char chr, int fontSize, int width) {
		List<AdditiveMask> result = new LinkedList<>();
		for (AdditiveMask mask : additiveMasks) {
			if (mask.chr != chr) {
				continue;
			} else if (mask.width > width + maxToleranceX) {
				continue;
			} else if (fontSize != mask.fontSize) {
				continue;
			}

			result.add(mask);
		}
		return result;
	}

	private List<AdditiveMask> getMasksFor(List<AdditiveMask> additiveMasks, char chr, Collection<Integer> fontSizes, int width) {
		List<AdditiveMask> result = new LinkedList<AdditiveMask>();
		for (AdditiveMask mask : additiveMasks) {
			if (mask.chr != chr) {
				continue;
			} else if (mask.width > width + maxToleranceX) {
				continue;
			} else if (fontSizes.contains(mask.fontSize) == false) {
				continue;
			}

			result.add(mask);
		}
		return result;
	}

	private static List<double[][]> getCharHistograms(AdditiveMask mask) {
		List<double[][]> list = new LinkedList<double[][]>();
		for (CharacterMask cm : mask.masks) {
			list.add(cm.histogram);
		}
		return list;
	}

	private Image.Byte applyMask(Image.Byte source, CharacterMask mask) {
		Image.Byte crop = new Image.Byte(mask.width, mask.height);

		Arrays.fill(crop.pixels, ImageUtil.toByte(Color.WHITE.getRGB()));

		for (int y = 0; y < mask.height; y++) {
			for (int x = mask.mask[y][0]; x <= mask.mask[y][1]; x++) {
				crop.pixels[y * source.w + x] = source.pixels[y * source.w + x];
			}
		}

		return crop;
	}

	private static final double divide(double a, double b) {
		return b == 0 ? 0 : a / b;
	}
}
