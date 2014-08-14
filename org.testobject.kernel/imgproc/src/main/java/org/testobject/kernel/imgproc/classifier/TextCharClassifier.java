package org.testobject.kernel.imgproc.classifier;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.BlobUtils;
import org.testobject.kernel.imgproc.plot.VisualizerUtil;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.kernel.imgproc.util.YCrCb;
import org.testobject.kernel.ocr.FontRenderer;
import org.testobject.kernel.ocr.freetype.FT2Library;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.file.FileUtil;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.Image.Int.Type;
import org.testobject.kernel.ocr.freetype.FreeTypeGrayscaleFontRenderer;

/**
 * 
 * @author enijkamp
 * 
 */
public class TextCharClassifier extends ClassifierBase {

	private static final boolean DEBUG = Debug.toDebugMode(false);

	static final char[] abc = "abcdefghijklmnopqrstuvwxyz".toCharArray();
	static final char[] ABC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
	static final char[] numbers = "0123456789".toCharArray();

	 static final String[] FONTS = { "Roboto-Bold.ttf", "Roboto-Regular.ttf" };
	 
	static final char[] CHARS = toArray(abc, ABC, numbers);

	static final int[] SIZES = { 12, 13, 14, 15, 16 };

	static int MAX_CHAR_WIDTH = 20, MAX_CHAR_HEIGHT = 25, MIN_CHAR_HEIGHT = 10;

	static final int fatX = 1, fatY = 1;

	private final List<Sample> trainHistograms;

	public TextCharClassifier() {
		this(FileUtil.toFileFromSystem("android/4_0_3/fonts"));
	}

	public TextCharClassifier(File fontLocation) {
		this(new FreeTypeGrayscaleFontRenderer(160, 240, FT2Library.FT_LCD_FILTER_LIGHT), toFonts(fontLocation, FONTS), SIZES);
	}

	public TextCharClassifier(FontRenderer renderer, File[] fonts, int[] sizes) {
		this.trainHistograms = trainHistograms(renderer, fonts, sizes);
	}

	@Override
	public Specification getSpec() {
		// FIXME dirty hack to run text classifier after icon classifier (en)
		// return spec().requires(none()).returns(Text.class).build();
		return spec().requires(Classes.Icon.class).returns(Classes.TextChar.class).build();
	}

	// TODO re-use heuristis of old TextClassifier code (en)
	// TODO constraints (distribution/histogram, size, color of chars, groups, w/h ratio of chars, upper/lower case char toplines) (en)
	// TODO return normalized likelihood (en)
	@Override
	public Match match(Context context, Blob blob) {

		// print
		if (DEBUG) {
			System.out.println(blob);
		}

		// stage 1 : pruning
		{
			// TODO maybe that not a good idea for characters which stick together
			if (hasTooManyChildrens(blob)) {
				return failed;
			}

			if (hasTooDeepHierarchy(blob)) {
				return failed;
			}

			if (isOutOfBounds(blob)) {
				return failed;
			}

			if (isLeftToRight(blob) == false) {
				return failed;
			}

			// FIXME this is intensity, not color variance, compute mean color etc. (en)
			if (hasLowColorVariance(context.after, blob)) {
				return failed;
			}
		}

		// stage 2 : scanning
		{
			Statistics stats = computeStatistiscs(trainHistograms, context.after, blob);

			debug(blob, context, stats);

			if (stats.coverage < .7f) {
				return failed;
			}

			double relativeUpperDistance = getRelativeUpperDistance(blob, stats.inverted);
			if (stats.avgDistance > relativeUpperDistance) {
				return failed;
			}

			return ClassifierBase.setClass(1.0f, blob, new Classes.TextChar(toCandidates(stats.candidates, relativeUpperDistance)));
		}
	}

	private List<Classes.TextChar.Candidate> toCandidates(List<Candidate> candidates,
			double relativeUpperDistance) {
		List<Classes.TextChar.Candidate> result = new LinkedList<>();
		for (Candidate candidate : candidates) {
			int x = candidate.x - fatX - candidate.histogramTrain.offsetX;
			result.add(new Classes.TextChar.Candidate(candidate.chr, candidate.size, candidate.font, x, candidate.distance, candidate.w));
		}
		return result;
	}

	private void debug(Blob blob, Context context, Statistics stats) {
		if (DEBUG) {
			VisualizerUtil.show("blob " + blob.id, BlobUtils.cutByMask(context.after, blob));
			VisualizerUtil.show("histogram", drawHistogram(stats.histogram));
			System.out.println("coverage: " + stats.coverage);
			System.out.println("matches:");
			for (Candidate match : stats.candidates) {
				System.out.println("   " + match);
			}
		}
	}

	private boolean hasLowColorVariance(Image.Int image, Blob blob) {
		Image.Int cut = ImageUtil.crop(image, blob.bbox);
		Image.Int luma = ImageUtil.toImageInt(YCrCb.y(cut));
		Histogram hist = toHistogram(luma);
		float mean = computeMean(hist.bins);
		float variance = computeVariance(hist.bins, mean);
		if (DEBUG) {
			System.out.println("variance: " + variance);
		}
		return variance < 10000;
	}

	private static final float computeMean(int[] values) {
		int sum = 0;
		for (int value : values) {
			sum += value;
		}
		return toFloat(sum) / values.length;
	}

	private static final float computeVariance(int[] values, float mean) {
		float error = 0;
		for (int value : values) {
			error += (value - mean) * (value - mean);
		}
		return error / values.length;
	}

	private boolean isOutOfBounds(Blob blob) {
		if (blob.getBoundingBox().height > MAX_CHAR_HEIGHT || blob.getBoundingBox().height < MIN_CHAR_HEIGHT) {
			return true;
		}
		for (Blob box : blob.children) {
			if (box.getBoundingBox().height > MAX_CHAR_HEIGHT || box.getBoundingBox().height < MIN_CHAR_HEIGHT
					|| box.getBoundingBox().width > MAX_CHAR_WIDTH) {
				return true;
			}
		}
		return false;
	}

	private boolean hasTooManyChildrens(Blob blob) {
		for (Blob character : blob.children) {
			if (character.children.size() > 2) {
				return true;
			}
		}

		return false;
	}

	private boolean hasTooDeepHierarchy(Blob blob) {
		for (Blob character : blob.children) {
			if (!hasChildren(character)) {
				continue;
			}
			for (Blob characterChild : character.children) {
				if (hasChildren(characterChild)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean isLeftToRight(Blob blob) {
		if (blob.children.isEmpty()) {
			return true;
		}

		List<Blob> boxes = new ArrayList<Blob>(blob.children);
		Collections.sort(boxes, new Comparator<Blob>() {
			@Override
			public int compare(Blob b1, Blob b2) {
				return Integer.compare(b1.bbox.x, b2.bbox.x);
			}
		});

		int negatives = 0;
		for (int i = 1; i < boxes.size(); i++) {
			Rectangle b1 = boxes.get(i - 1).bbox;
			Rectangle b2 = boxes.get(i - 0).bbox;

			if ((b1.x + b1.width) > b2.x) {
				negatives++;
			}
		}

		return ((float) negatives / boxes.size()) < 0.50;
	}

	static Statistics computeStatistiscs(List<Sample> trainHistograms, Image.Int image, Blob blob) {

		// cut
		Image.Int testLuma = ImageUtil.toImageInt(YCrCb.y(image)); // FIXME hold precomputed luma in context (en)
		Image.Int testCut = BlobUtils.cutByMask(testLuma, blob);
		Image.Int testNorm = normalizeIntensity(testCut);
		Image.Int testFat = fatImage(testNorm, fatX, fatY);

		// invert if required
		boolean inverted = hasBrightIntensity(testFat, blob.area);
		if (inverted) {
			testFat = invert(testFat);
		}

		if (DEBUG) {
			VisualizerUtil.show("testCut", testCut, 20f);
			VisualizerUtil.show("testFat", testFat, 20f);
		}

		// histogram
		Histogram testHist = toHistogram(testFat);

		// scan
		Queue<Candidate> queue = new PriorityQueue<Candidate>(64, new Comparator<Candidate>() {
			@Override
			public int compare(Candidate m1, Candidate m2) {
				return Double.compare(m1.distance, m2.distance);
			}
		});
		for (Sample trainSample : trainHistograms) {
			if (isWithinHeight(trainSample, blob)) {
				Histogram trainHist = trainSample.histogram;
				for (int x = 0; x < (testHist.w - trainHist.w + 1); x++) {
					Histogram cutHist = cut(trainHist, testHist, x);
					double dist = dist(trainHist, cutHist);
					queue.add(new Candidate(trainSample.chr, trainSample.size, trainSample.font, x, trainSample.histogram.w,
							trainSample.histogram.h,
							trainSample.histogram, cutHist, dist));
				}
			}
		}

		// filter
		LinkedList<Candidate> top = new LinkedList<>();
		{
			while (queue.isEmpty() == false) {
				Candidate candidate = queue.poll();

				boolean isWithinWidth = top.size() < blob.bbox.width;

				if (isWithinWidth == false) {
					break;
				}

				int trainIntensity = candidate.histogramTrain.sum;
				int testIntensity = candidate.histogramTest.sum;

				double relativeIntensityLimit = getRelativeIntensityLimit(candidate, inverted);
				double relativeDistanceLimit = getRelativeUpperDistance(trainIntensity, testIntensity, inverted);

				boolean isWithinIntensity = Math.abs(trainIntensity - testIntensity) < relativeIntensityLimit;
				boolean isWithinDistance = candidate.distance < relativeDistanceLimit;
				boolean hasIntensityLowPoint = hasIntensityLowPoint(testHist, candidate);

				if (isWithinIntensity && isWithinDistance && hasIntensityLowPoint) {
					top.add(candidate);
				}
			}
		}

		// x coverage top
		float coverage = 0f;
		if (top.isEmpty() == false) {
			int[][] intervals = new int[top.size()][2];
			int i = 0;
			for (Candidate match : top) {
				intervals[i++] = new int[] { match.x, match.x + match.w };
			}

			int count = 0;
			for (int x = 0; x < blob.bbox.width; x++) {
				if (contains(intervals, x)) {
					count++;
				}
			}
			coverage = toFloat(count) / blob.bbox.width;
		}
		return new Statistics(testHist, top, coverage, inverted);
	}

	private static double getRelativeUpperDistance(Blob blob, boolean inverted) {
		return (blob.area * 40) * (inverted ? 2 : 1);
	}

	private static double getRelativeUpperDistance(int intensity1, int intensity2, boolean inverted) {
		final int x = Math.abs(intensity1 - intensity2);
		final double scale = 12d / Math.log(1 + x / 10d);
		return x * scale * (inverted ? 2d : 1d);
	}

	private static double getRelativeIntensityLimit(Candidate candidate, boolean inverted) {
		return (candidate.histogramTrain.sum * 0.15) * (inverted ? 2.5 : 2);
	}

	private static boolean hasIntensityLowPoint(Histogram testHist, Candidate candidate) {
		int offset = candidate.x + candidate.w;
		offset = (offset >= testHist.bins.length ? testHist.bins.length - 1 : offset);

		// if(DEBUG) {
		// System.out.println(candidate.chr + " -> low-point = " + offset + " avg = " + testHist.avg);
		// int x = 0;
		// for(int bin : testHist.bins) {
		// System.out.println("  " + x++ + " " + bin);
		// }
		// }

		return testHist.bins[offset] < (testHist.avg * .5);
	}

	private static boolean isWithinHeight(Sample trainSample, Blob blob) {
		return Math.abs(trainSample.histogram.h - blob.bbox.height) <= 1 ||
				Math.abs(trainSample.size - blob.bbox.height) <= 1;
	}

	private List<Sample> trainHistograms(FontRenderer renderer, File[] fonts, int[] sizes) {
		List<Sample> trainHistograms = new LinkedList<>();

		// cartesian product
		for (File font : fonts) {
			for (int size : sizes) {
				for (char chr : CHARS) {
					Histogram trainHist = generateHistogram(renderer, font, size, chr);
					trainHistograms.add(new Sample(chr, size, font.getName(), trainHist));
				}
			}
		}

		return trainHistograms;
	}

	static Histogram generateHistogram(FontRenderer renderer, File font, float size, char chr) {
		// compute luminance and histogram
		Image.Int trainImage = ImageUtil.toImage(renderer.drawChar(font, size, chr));
		Image.Int trainLuma = ImageUtil.toImageInt(YCrCb.y(trainImage));
		Image.Int trainNorm = normalizeIntensity(trainLuma);
		Rectangle cut = removeAntiAliasedBorder(trainNorm);
		Image.Int trainAnti = ImageUtil.crop(trainNorm, cut);

		if (DEBUG) {
			// VisualizerUtil.show("trainAnti", trainAnti);
		}

		return toHistogram(trainAnti, cut.x);
	}

	static class Statistics {
		public final Histogram histogram;
		public final float coverage;
		public final LinkedList<Candidate> candidates;
		public final double avgDistance, minDistance, maxDistance;
		public final boolean inverted;

		public Statistics(Histogram histogram, LinkedList<Candidate> candidates, float coverage, boolean inverted) {
			this.histogram = histogram;
			this.candidates = candidates;
			this.coverage = coverage;
			this.avgDistance = average(candidates);
			this.minDistance = min(candidates);
			this.maxDistance = max(candidates);
			this.inverted = inverted;
		}

		private float average(LinkedList<Candidate> candidates) {
			float sum = 0f;
			for (Candidate candidate : candidates) {
				sum += candidate.distance;
			}
			return sum / candidates.size();
		}

		private double min(LinkedList<Candidate> candidates) {
			double min = Float.MAX_VALUE;
			for (Candidate candidate : candidates) {
				min = Math.min(min, candidate.distance);
			}
			return min;
		}

		private double max(LinkedList<Candidate> candidates) {
			double max = Float.MIN_VALUE;
			for (Candidate candidate : candidates) {
				max = Math.max(max, candidate.distance);
			}
			return max;
		}
	}

	static class Sample {
		public final char chr;
		public final int size;
		public final String font;
		public final Histogram histogram;

		public Sample(char chr, int size, String font, Histogram histogram) {
			this.chr = chr;
			this.size = size;
			this.font = font;
			this.histogram = histogram;
		}
	}

	static class Candidate {
		public final char chr;
		public final int size;
		public final String font;
		public final Histogram histogramTrain, histogramTest;
		public final double distance;
		public final int w, h;
		public final int x;

		public Candidate(char chr, int size, String font, int x, int w, int h, Histogram histogramTrain, Histogram histogramTest,
				double distance) {
			this.chr = chr;
			this.size = size;
			this.font = font;
			this.x = x;
			this.w = w;
			this.h = h;
			this.histogramTrain = histogramTrain;
			this.histogramTest = histogramTest;
			this.distance = distance;
		}

		@Override
		public String toString() {
			return String.format("[char=%s, size=%d, x=%d, dist=%.0f]", chr, size, x, distance);
		}
	}

	static class Histogram {
		public final int[] bins;
		public final int min, max;
		public final int w, h;
		public final int avg;
		public final int sum;
		public final int offsetX;

		public Histogram(int[] bins, int w, int h, int offsetX) {
			this.bins = bins;
			this.min = min(bins);
			this.max = max(bins);
			this.w = w;
			this.h = h;
			this.offsetX = offsetX;
			this.avg = avg(bins);
			this.sum = sum(bins);
		}

		private static int avg(int[] bins) {
			return sum(bins) / bins.length;
		}

		private static int sum(int[] bins) {
			int sum = 0;
			for (int bin : bins) {
				sum += bin;
			}
			return sum;
		}

		private static int max(int[] bins) {
			int max = Integer.MIN_VALUE;
			for (int bin : bins) {
				max = Math.max(max, bin);
			}
			return max;
		}

		private static int min(int[] bins) {
			int min = Integer.MAX_VALUE;
			for (int bin : bins) {
				min = Math.min(min, bin);
			}
			return min;
		}
	}

	static BufferedImage drawHistogram(Histogram histogram) {
		final int scale = 32;

		int range = (histogram.max - histogram.min) / scale;

		BufferedImage output = new BufferedImage((histogram.w * 2) + 10, range + 10, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = output.createGraphics();
		{
			// fill
			{
				g.setColor(Color.white);
				g.fillRect(0, 0, output.getWidth(), output.getHeight());
			}

			// bins
			{
				g.setColor(Color.black);
				for (int i = 0; i < histogram.bins.length; i++) {
					int x = (i * 2) + 5;
					int y = range + 5;
					int h = (histogram.bins[i] - histogram.min) / scale;
					if (h > 0)
						g.drawLine(x, y, x, y - h);
				}
			}
		}
		g.dispose();
		return output;
	}

	static void printHistogram(Histogram hist) {
		for (int bin : hist.bins) {
			System.out.println(bin);
		}
	}

	static Histogram toHistogram(Image.Int image) {
		return toHistogram(image, 0);
	}

	static Histogram toHistogram(Image.Int image, int offsetX) {
		// histogram
		int[] histogram = new int[image.w];
		{
			for (int x = 0; x < image.w; x++) {
				int sum = 0;
				for (int y = 0; y < image.h; y++) {
					int pixel = image.get(x, y);
					if (image.type == Type.RGB || getAlpha(pixel) != 0) {
						sum += 255 - toIntensity(pixel);
					}
				}
				histogram[x] = sum;
			}
		}
		return new Histogram(histogram, image.w, image.h, offsetX);
	}

	private static Rectangle removeAntiAliasedBorder(Image.Int in) {
		final int intensity_limit = 300;
		Histogram hist = toHistogram(in);
		int x = 0;
		if (hist.bins[0] < intensity_limit) {
			x++;
		}
		int w = in.w - x;
		if (hist.bins[in.w - 1] < intensity_limit) {
			w--;
		}
		return new Rectangle(x, 0, w, in.h);
	}

	private static Image.Int invert(Image.Int in) {
		Image.Int out = new Image.Int(in.w, in.h, Image.Int.Type.ARGB);
		for (int y = 0; y < in.h; y++) {
			for (int x = 0; x < in.w; x++) {
				int rgb = in.get(x, y);
				int intensity = (rgb >> 8) & 0xff;
				int alpha = (rgb >> 24) & 0xff;

				if (alpha != 0) {
					out.pixels[y * out.w + x] = ImageUtil.toInt(255 - intensity, 255 - intensity, 255 - intensity);
				} else {
					out.pixels[y * out.w + x] = in.pixels[y * out.w + x];
				}
			}
		}
		return out;
	}

	private static boolean hasBrightIntensity(Image.Int in, int area) {
		int intensity = 0;
		for (int y = 0; y < in.h; y++) {
			for (int x = 0; x < in.w; x++) {
				int pixel = in.get(x, y);
				if (getAlpha(pixel) != 0) {
					intensity += toIntensity(pixel);
				}
			}
		}
		float average = toFloat(intensity) / area;
		return average > 150f;
	}

	static Image.Int normalizeIntensity(Image.Int in) {
		Image.Int out = new Image.Int(in.w, in.h, Image.Int.Type.ARGB);
		// interval
		int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		for (int y = 0; y < in.h; y++) {
			for (int x = 0; x < in.w; x++) {
				int pixel = in.get(x, y);
				if (in.type == Type.RGB || getAlpha(pixel) != 0) {
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
				if (in.type == Type.RGB || getAlpha(pixel) != 0) {
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

	private final static float toFloat(int value) {
		return (float) value;
	}

	private final static int toIntensity(int rgb) {
		int intensity = (rgb >> 16) & 0xff;
		return intensity;
	}

	private static final boolean contains(int[][] intervals, int value) {
		for (int[] interval : intervals) {
			if (interval[0] < value && interval[1] > value) {
				return true;
			}
		}
		return false;
	}

	private static final File[] toFonts(File fontBaseLocation, String... fonts) {
		File fontLocation = new File(fontBaseLocation, "sans-serif");
		File[] fontFiles = new File[fonts.length];
		for (int i = 0; i < fonts.length; i++) {
			fontFiles[i] = toFont(fonts[i], fontLocation);
		}

		return fontFiles;
	}

	private static final File toFont(String font, File fontLocation) {
		return new File(fontLocation, font);
	}

	private static final char[] toArray(char[]... in) {
		int length = 0;
		for (char[] array : in) {
			length += array.length;
		}
		char[] out = new char[length];
		int pos = 0;
		for (char[] array : in) {
			System.arraycopy(array, 0, out, pos, array.length);
			pos += array.length;
		}
		return out;
	}

	private static Image.Int fatImage(Image.Int source, int fatX, int fatY) {
		final Image.Int image = new Image.Int(source.w + (fatX * 2), source.h + (fatY * 2), Image.Int.Type.ARGB);

		Arrays.fill(image.pixels, 0, image.w * image.h, new Color(0, 0, 0, 0).getRGB());
		for (int y = 0; y < source.h; y++) {
			System.arraycopy(source.pixels, y * source.w, image.pixels, ((y + fatY) * (source.w + fatX * 2)) + fatX, source.w);
		}

		return image;
	}

	private static double dist(Histogram trainHist, Histogram testHist) {
		int sum = 0;
		for (int i = 0; i < trainHist.w; i++) {
			sum += Math.abs(trainHist.bins[i] - testHist.bins[i]) * Math.abs(trainHist.bins[i] - testHist.bins[i]);
		}
		return Math.sqrt(sum);
	}

	private static Histogram cut(Histogram trainHist, Histogram testHist, int offset) {
		int[] bins = new int[trainHist.w];
		for (int i = 0; i < trainHist.w; i++) {
			bins[i] = testHist.bins[offset + i];
		}
		return new Histogram(bins, trainHist.w, testHist.h, offset);
	}

	private static boolean hasChildren(Blob characterChild) {
		return characterChild.children != null && characterChild.children.size() > 0;
	}
}
