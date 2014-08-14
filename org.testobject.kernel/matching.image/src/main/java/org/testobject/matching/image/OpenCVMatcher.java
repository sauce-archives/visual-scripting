package org.testobject.matching.image;

import static org.opencv.core.Core.minMaxLoc;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.TM_CCORR_NORMED;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.matchTemplate;
import static org.opencv.imgproc.Imgproc.resize;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.Size;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.math.statistics.Histogram;
import org.testobject.commons.tools.plot.VisualizerUtil;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.Image.Int;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.kernel.imaging.color.contrast.Contrast;
import org.testobject.kernel.imaging.fingerprint.ImageFingerprint;

import com.google.common.collect.Lists;

/**
 * Template-matching algorithm using normalized cross-correlation with hierarchical zooming
 * 
 * http://docs.opencv.org/2.4.4-beta/doc/tutorials/introduction/desktop_java/java_dev_intro.html
 * 
 * @author enijkamp, lbielski
 *
 */
public class OpenCVMatcher {
	
	static {
		// load open-cv
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static final int CV_INTER_NN = 0, CV_INTER_LINEAR = 1, CV_INTER_CUBIC = 2, CV_INTER_AREA = 3, CV_INTER_LANCZOS4 = 4;
	
	private static final DecimalFormat df = new DecimalFormat("0.00");
	private static final DecimalFormat df2 = new DecimalFormat("0");
	
	private static BigDecimal START_SCALE = BigDecimal.valueOf(0.5);
	private static BigDecimal END_SCALE = BigDecimal.valueOf(2.1);
	private static BigDecimal SCALE_STEP_STAGE_1 = BigDecimal.valueOf(0.2);
	private static BigDecimal SCALE_STEP_STAGE_2 = BigDecimal.valueOf(0.05);
	private static BigDecimal SCALE_STEP_STAGE_3 = BigDecimal.valueOf(0.03);
	
	private static final int MAX_IMAGE_PERIMETER = 1280;
	private static final int MIN_TEMPLATE_PIXEL_COUNT = 250;
	private static final int MIN_TEMPLATE_HEIGHT = 10;
	private static final int MIN_TEMPLATE_WIDTH = 10;
	private static final int FINGERPRINT_SIZE = 64;
	private static final double STDDEV_THRESHOLD = 50d;
	private static final double NO_MATCH_THRESHOLD = 0.85;
	
	private static final int EXECUTOR_THREADS = 4;
	
	private static final boolean DEBUG = false;
	
	public static class ScanMatch {
		
		public final double probability;
		public final Rectangle.Int result;

		public final BigDecimal scale;

		public ScanMatch(double probability, Rectangle.Int result, BigDecimal scale) {
			this.probability = probability;
			this.result = result;
			this.scale = scale;
		}

		@Override
		public String toString() {
			return probability + " " + df2.format(result.x) + ", " + df2.format(result.y) + " " + df.format(scale);
		}
	}
	
	public static class SingleScaleMatch {
		
		public final double fingerprintMatch;
		public final MinMaxLocResult minMaxLocResult;
		public final Rectangle.Int result;

		public SingleScaleMatch(double fingerprintMatch, MinMaxLocResult minMaxLocResult, Rectangle.Int result) {
			this.fingerprintMatch = fingerprintMatch;
			this.minMaxLocResult = minMaxLocResult;
			this.result = result;
		}

		@Override
		public String toString() {
			return "Fingerprint match: " + fingerprintMatch + " " + df2.format(result.x) + ", " + df2.format(result.y);
		}
	}	

	public final static class ScanMatchComparator implements Comparator<ScanMatch> {
		@Override
		public int compare(ScanMatch m1, ScanMatch m2) {
			return Double.compare(m2.probability, m1.probability);
		}
	}
	
	public static List<ScanMatch> findMatches(Image.Int searchImage, Image.Int template) {	
	
		/* -------------------- stage 0 - prepare -------------------- */
		
		long stage0_begin = System.currentTimeMillis();
		
		Mat searchImageMat = OpenCV.convertBufferedImageToMat(ImageUtil.Convert.toBufferedImage(searchImage));
		Mat templateMat = OpenCV.convertBufferedImageToMat(ImageUtil.Convert.toBufferedImage(template));

		cvtColor(searchImageMat, searchImageMat, COLOR_BGR2GRAY);
		cvtColor(templateMat, templateMat, COLOR_BGR2GRAY);

		ImageFingerprint stddevFingerprint = new ImageFingerprint(ImageUtil.toSquare(template), 0xf2, 0xf1, 0xf0, FINGERPRINT_SIZE);
		
		double templateStdDev = stddev(stddevFingerprint);		
		double currentPerimeter = searchImageMat.cols() + searchImageMat.rows();
		double scale = currentPerimeter > MAX_IMAGE_PERIMETER ? MAX_IMAGE_PERIMETER / currentPerimeter : 1;
		
		scale = Math.max(scale, 0.4);

		// only resize if scale is different from 1
		if (scale != 1.0) {
			resize(searchImageMat, searchImageMat, new Size(searchImageMat.width() * scale, searchImageMat.height() * scale), 0, 0, CV_INTER_AREA);
		}		
		
		Image.Int searchImageScaled = ImageUtil.Convert.toImage(OpenCV.matToBufferedImage(searchImageMat));
			
		
		long stage0_end = System.currentTimeMillis();
		
		
		/* -------------------- stage 1 - course-grained pass -------------------- */
		
		long stage1_begin = System.currentTimeMillis();
		
		List<BigDecimal> stage1Scales = Lists.newLinkedList(createScales(new HashSet<BigDecimal>(), START_SCALE, END_SCALE, SCALE_STEP_STAGE_1));
		List<ScanMatch> matches = findMatchesForScales(searchImageMat, templateMat, templateStdDev, scale, searchImageScaled, stage1Scales);

		if (DEBUG) {
			for (ScanMatch match : matches) {
				System.out.println(match);
			}
		}
		
		long stage1_end = System.currentTimeMillis();		
		
		/* -------------------- stage 2 - fine-grained pass -------------------- */
		
		long stage2_begin = System.currentTimeMillis();
		
		Set<BigDecimal> stage2BaseScales = new HashSet<>();
		
		for (ScanMatch match : matches) {
			if (stage2BaseScales.size() < 4) {
				match.scale.setScale(2, RoundingMode.HALF_UP);
				stage2BaseScales.add(match.scale);
			}
		}		
		
		Set<BigDecimal> stage2Scales = new HashSet<>(); 		
		for (BigDecimal stage2Scale : stage2BaseScales) {
			stage2Scales = createScales(stage2Scales, stage2Scale.subtract(new BigDecimal(0.15)), stage2Scale.add(new BigDecimal(0.15)), SCALE_STEP_STAGE_2);						
		}

		// remove stage1 scales
		for (BigDecimal s : stage1Scales) {
			if (stage2Scales.contains(s)) {
				stage2Scales.remove(s);
			}
		}
		
		matches.addAll(findMatchesForScales(searchImageMat, templateMat, templateStdDev, scale, searchImageScaled, Lists.newLinkedList(stage2Scales)));
		
		long stage2_end = System.currentTimeMillis();		
		
		/* -------------------- debug -------------------- */
		
		long stage0_time = stage0_end - stage0_begin;
		long stage1_time = stage1_end - stage1_begin;
		long stage2_time = stage2_end - stage2_begin;
		
		if (DEBUG) {
			System.out.println("stage 0: " + (stage0_time));
			System.out.println("stage 1: " + (stage1_time));
			System.out.println("stage 2: " + (stage2_time));
			System.out.println("total: " + (stage0_time + stage1_time + stage2_time));			
		}
		
		Collections.sort(matches, new ScanMatchComparator());

		if (!matches.isEmpty() && matches.get(0).probability >= NO_MATCH_THRESHOLD) {
			return matches;
		}
		
		/* -------------------- stage 3 - no match found - iterating through all scales -------------------- */
		
		long stage3_begin = System.currentTimeMillis();
		
		Set<BigDecimal> stage3Scales = createScales(new HashSet<BigDecimal>(), START_SCALE, END_SCALE, SCALE_STEP_STAGE_3);		
		List<ScanMatch> stage3Matches = findMatchesForScales(searchImageMat, templateMat, templateStdDev, scale, searchImageScaled, Lists.newLinkedList(stage3Scales));
	
		long stage3_time = System.currentTimeMillis() - stage3_begin;
		
		if (DEBUG) {	
			System.out.println("stage 3: " + (stage3_time));
			System.out.println("total: " + (stage0_time + stage1_time + stage2_time + stage3_time));			
		}
		
		return stage3Matches;	
	}

	private static Set<BigDecimal> createScales(Set<BigDecimal> scales, BigDecimal startScale, BigDecimal endScale, BigDecimal step) {
		
		for (BigDecimal scale = startScale; scale.compareTo(endScale) <= 0;  scale = scale.add(step)) {		
			scale = scale.setScale(2, RoundingMode.HALF_UP);		
			scales.add(scale);
		}
		
		return scales;
	}
	
	private static List<ScanMatch> findMatchesForScales(Mat searchImageMat, Mat templateMat, double templateStdDev, double scale,
			Image.Int searchImageScaled, List<BigDecimal> scales) {
		List<ScanMatch> matches = new ArrayList<>();		
		
		// brute-force scaling
		// process matching for each scale in a thread
		List<Future<ScanMatch>> resultList = new LinkedList<Future<ScanMatch>>();
		ExecutorService executor = Executors.newFixedThreadPool(EXECUTOR_THREADS);
						
		for (BigDecimal s : scales) {					
			MatchFinder matchFinder = new MatchFinder(searchImageMat, templateMat, templateStdDev, scale, searchImageScaled, s);			
			resultList.add(executor.submit(matchFinder));
		}
		
		executor.shutdown();		
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			for (Future<ScanMatch> result : resultList) {
				if (result.get() != null) {
					matches.add(result.get());					
				}
			}			
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
		
		Collections.sort(matches, new ScanMatchComparator());
		
		return matches;
	}	
	
	private static class MatchFinder implements Callable<ScanMatch> {

		private final Mat searchImageMat;
		private final Mat templateMat;
		private final double templateStdDev;
		private final double scale;
		private final Int searchImageScaled;
		private final BigDecimal s;

		public MatchFinder(Mat searchImageMat, Mat templateMat, double templateStdDev, double scale,
				Image.Int searchImageScaled, BigDecimal s) {
					this.searchImageMat = searchImageMat;
					this.templateMat = templateMat;
					this.templateStdDev = templateStdDev;
					this.scale = scale;
					this.searchImageScaled = searchImageScaled;
					this.s = s;		
		}
		
		@Override
		public ScanMatch call() throws Exception {
			return findMatch(searchImageMat, templateMat, templateStdDev, scale, searchImageScaled, s);
		}		
	}
	
	private static ScanMatch findMatch(Mat searchImageMat, Mat templateMat, double templateStdDev, double scale,
			Image.Int searchImageScaled, BigDecimal s) {		
		
		double templateScale = s.doubleValue() * scale;
		int w = (int) Math.round(templateMat.width() * templateScale);
		int h = (int) Math.round(templateMat.height() * templateScale);
		
		// early exit - template is bigger than search image
		if (templateMat.cols() * templateScale >= searchImageMat.cols() || templateMat.rows() * templateScale >= searchImageMat.rows()) {
			return null;
		}
		
		if (isTemplateTooSmall(w, h, s)) {
			return null;
		}			
		
		// scale
		Mat scaledTemplateMat = new MatOfFloat();
		resize(templateMat, scaledTemplateMat, new Size(w, h), 0, 0, CV_INTER_AREA);
		
		// normalized cross-corr
		Mat resultMatrix = new MatOfFloat();
		matchTemplate(searchImageMat, scaledTemplateMat, resultMatrix, TM_CCORR_NORMED);			
		MinMaxLocResult minMaxResult = minMaxLoc(resultMatrix);			
		
		// compute fingerprint for scaled template
		Image.Int templateForFingerprint = ImageUtil.Convert.toImage(OpenCV.matToBufferedImage(scaledTemplateMat));
		ImageFingerprint templateFingerprint = new ImageFingerprint(ImageUtil.toSquare(templateForFingerprint), 0xf2, 0xf1, 0xf0, FINGERPRINT_SIZE);
		
		// if template has low contrast bump it up
		if (templateStdDev < STDDEV_THRESHOLD) {
			
			Image.Int contrastedImage = ImageUtil.Convert.toImageInt(Contrast.autoContrast(ImageUtil.Convert.toImageByte(templateForFingerprint)));
			templateFingerprint = new ImageFingerprint(ImageUtil.toSquare(contrastedImage), 0xf2, 0xf1, 0xf0, FINGERPRINT_SIZE);
		}
		
		// cut the possible area from the image and get fingerprint probability for it		
		Rectangle.Int resultRectangle = new Rectangle.Int((int) minMaxResult.maxLoc.x, (int) minMaxResult.maxLoc.y, w, h);
		SingleScaleMatch singleScaleMatch = getMatchForRectangle(searchImageScaled, templateFingerprint, templateStdDev, minMaxResult, resultRectangle);
			
		// free
		resultMatrix.release();
		
		return new ScanMatch(singleScaleMatch.fingerprintMatch, scaleRectangle(singleScaleMatch.result, 1 / scale), s);
	}

	private static SingleScaleMatch getMatchForRectangle(Image.Int searchImage, ImageFingerprint templateFingerprint,
			double templateStdDev, MinMaxLocResult result, Rectangle.Int resultRectangle) {
		Image.Int crop = ImageUtil.Cut.crop(searchImage, resultRectangle);
		
		// also increase contrast for crop of search image
		Image.Int contrast = applyContrast(crop, templateStdDev < STDDEV_THRESHOLD);
		ImageFingerprint resultFingerprint = new ImageFingerprint(ImageUtil.toSquare(contrast), 0xf2, 0xf1, 0xf0, FINGERPRINT_SIZE);		
		
		double stddev1 = stddev(templateFingerprint);
		double stddev2 = stddev(resultFingerprint);
		double stddevMatch = Math.min(stddev1, stddev2) / Math.max(stddev1, stddev2);
		
		
		double fingerprintProbability = fingerprintMatch(templateFingerprint, resultFingerprint);			
		
		double matchProbability = stddevMatch > 0.8 ? fingerprintProbability : fingerprintProbability * stddevMatch;		
		
		return new SingleScaleMatch(matchProbability, result, resultRectangle);
	}	
	
	private static boolean isTemplateTooSmall(double w, double h, BigDecimal scale) {

		if (scale.compareTo(new BigDecimal(1.0)) == 0) {
			return false;
		}
		
		if (w * h < MIN_TEMPLATE_PIXEL_COUNT ||
				w < MIN_TEMPLATE_WIDTH ||
				h < MIN_TEMPLATE_HEIGHT) {
			return true;
		}
		
		return false;
	}
	
	private static Image.Int applyContrast(Image.Int image, boolean isLowContrast) {
		return isLowContrast ? ImageUtil.Convert.toImageInt(Contrast.autoContrast(ImageUtil.Convert.toImageByte(image))) : image;
	}	

	@SuppressWarnings("unused")
	private static void displayMat(Mat mat) {
		
		Image.Int toDisplay = new Image.Int(mat.cols(), mat.rows());
		
		for (int x = 0; x < mat.cols(); x++) {
			for (int y = 0; y < mat.rows(); y++) {
				double[] pixel = mat.get(y, x);
				int intencity = (int) (255 * pixel[0]);
				
				toDisplay.set(x, y, ImageUtil.toInt(intencity, intencity, intencity));				
			}
		}
		
		VisualizerUtil.show(toDisplay);
	}

	public static double fingerprintMatch(ImageFingerprint print1, ImageFingerprint print2) {

		double offset = 2d;
		double limit = (FINGERPRINT_SIZE * FINGERPRINT_SIZE) / offset;

		double distance = ImageFingerprint.lumaDistanceL1(print1, print2);
		double error = Math.abs(distance / limit);
		double likelihood = 1d - Math.min(1d, error);

		if (Double.isNaN(likelihood)) {
			return 0;
		}
		
		return likelihood;
	}		
	
	private static double stddev(ImageFingerprint print) {
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

	private static Rectangle.Int scaleRectangle(Rectangle.Int rectangle, double scale) {
		return new Rectangle.Int((int) (rectangle.x * scale), (int) (rectangle.y * scale), (int) (rectangle.w * scale),
				(int) (rectangle.h * scale));
	}
}