package org.testobject.kernel.classification.polymatch;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.testobject.kernel.classification.procedural.ProceduralRenderer.Builder.rect;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.testobject.kernel.classification.contours.Contour;
import org.testobject.kernel.classification.contours.Contours;
import org.testobject.kernel.classification.contours.IconContour;
import org.testobject.kernel.classification.polymatch.PolyMatch;
import org.testobject.kernel.classification.procedural.ProceduralRenderer;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.BlobUtils;
import org.testobject.kernel.imgproc.blob.GraphBlobBuilder;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.plot.VisualizerUtil;
import org.testobject.commons.math.algebra.Point;

/**
 * 
 * @author enijkamp
 *
 */
public class PolyMatchTest {

	public static final boolean DEBUG = Debug.toDebugMode(true);
	
	private static final double[] twitter_bird_points = { 29, 1, 27, 3, 27, 4, 26, 5, 26, 6, 25, 7, 25, 8, 24, 9, 24, 10, 23, 11, 21, 11,
	        19, 9, 18, 9, 17, 8, 16, 8, 15, 7, 14, 7, 13, 6, 11, 6, 10, 5, 9, 5, 8, 4, 6, 4, 6, 5, 10, 9, 9, 10, 8, 10, 8, 11, 12, 15, 11,
	        16, 10, 16, 10, 17, 11, 18, 12, 18, 13, 19, 14, 19, 15, 20, 14, 21, 14, 22, 15, 23, 16, 23, 17, 24, 16, 25, 15, 25, 13, 27, 3,
	        27, 2, 26, 1, 26, 4, 29, 5, 29, 7, 31, 8, 31, 9, 32, 9, 33, 28, 33, 29, 32, 30, 32, 32, 30, 33, 30, 40, 23, 40, 22, 41, 21, 41,
	        20, 43, 18, 47, 18, 47, 16, 45, 16, 44, 15, 45, 14, 46, 14, 47, 13, 47, 12, 46, 12, 45, 13, 43, 13, 42, 12, 42, 9, 37, 4, 36,
	        4, 35, 3, 35, 1 };

	public static ProceduralRenderer.Procedure button(int w, int h) {
		return ProceduralRenderer.Builder.describe()
		        .shape(ProceduralRenderer.Builder.rect(w, h)
		                .round(12, 12)
		                .stroke(Color.darkGray)
		                .gradient(Color.white, Color.lightGray))
		        .build();
	}
	
	private static String toResourcePath(String file) {
		return "android/4_0_3/test/polymatch/" + file;
	}

	@Test
	public void testTrivialMatch() {
		Contour points1 = toContour(new double[] { 5, 5, 10, 5, 10, 10, 5, 10 });
		Contour points2 = toContour(new double[] { 5, 5, 10, 5, 10, 10, 5, 10 });
		double mismatch = PolyMatch.match(points1, points2);
		assertThat(mismatch, is(0d));
	}

	@Test
	public void testTrivialResizeMatch() {
		Contour points1 = toContour(new double[] { 5, 5, 10, 5, 10, 10, 5, 10 });
		Contour points2 = toContour(new double[] { 5 * 2, 5 * 2, 10 * 2, 5 * 2, 10 * 2, 10 * 2, 5 * 2, 10 * 2 });
		double mismatch = PolyMatch.match(points1, points2);
		assertThat(mismatch, is(0d));
	}

	@Test
	public void testTwitterMatch() {
		Contour points1 = toContour(twitter_bird_points);
		Contour points2 = toContour(twitter_bird_points);

		double mismatch = PolyMatch.match(points1, points2);
		assertThat(mismatch, is(0d));
	}

	@Test
	public void testTwitterMatchDeform() {
		Contour points1 = toContour(twitter_bird_points);
		Contour points2 = toContour(twitter_bird_points);

		for (int i : new int[] { 0, 10, 20, 30, 40 }) {
			points2.xpoints[i] += 10;
			points2.ypoints[i] += 10;
		}

		double mismatch = PolyMatch.match(points1, points2);
		System.out.println(mismatch);
	}

	@Test
	public void testTwitterMatchDeformExtreme() {
		Contour points1 = toContour(twitter_bird_points);
		Contour points2 = toContour(new double[] { 5, 5, 10, 5, 10, 10, 5, 10 });

		double mismatch = PolyMatch.match(points1, points2);
		System.out.println(mismatch);
	}

	@Test
	public void testTwitterMatchTiming() {
		Contour points1 = toContour(twitter_bird_points);
		Contour points2 = toContour(twitter_bird_points);

		float n = 1000f;
		long start = System.currentTimeMillis();
		{
			for (int i = 0; i < n; i++) {
				PolyMatch.match(points1, points2);
			}
		}
		long end = System.currentTimeMillis();

		System.out.println((end - start) / n + "ms");
	}
	
	private static List<Contour> extractContour(Image.Int image) {
		// blobs
		Image.Int largeImage = ImageUtil.grow(image, 1, 1);
		GraphBlobBuilder builder = new GraphBlobBuilder(largeImage.w, largeImage.h);
		Blob[] blobs = builder.build(largeImage);
		
		// blobs
		if(DEBUG) {
			Image.Int hierarchy = new Image.Int(largeImage.w, largeImage.h);
			BlobUtils.drawHierarchy(blobs, hierarchy);
			VisualizerUtil.show("blobs", ImageUtil.toBufferedImage(hierarchy), 1f);
		}

		// contours
		Blob blob = blobs[0].children.get(0).children.get(0);
		if(DEBUG) {
			VisualizerUtil.show("blob", ImageUtil.toBufferedImage(BlobUtils.cutByMask(largeImage, blob)));
		}
		
		return extractContours(blob);
	}
			

	@Test
	public void testContourCam() throws IOException {
		
		Image.Int input1 = ImageUtil.read(ClassLoader.getSystemResource(toResourcePath("cam.png")));
		Image.Int input2 = ImageUtil.read(ClassLoader.getSystemResource(toResourcePath("cam_deform.png")));
		Image.Int input3 = ImageUtil.read(ClassLoader.getSystemResource(toResourcePath("cam_deform_2.png")));

		List<Contour> contours1 = extractContour(input1);
		List<Contour> contours2 = extractContour(input2);
		List<Contour> contours3 = extractContour(input3);

		// check
		{
			assertThat(contours1.size(), is(2));
			assertThat(contours2.size(), is(2));
			assertThat(contours3.size(), is(2));
		}

		// render
		if(DEBUG) {
			ProceduralRenderer.Procedure proc = ProceduralRenderer.Builder.describe()
			        .shape(ProceduralRenderer.Builder.polygon()
			                .points(toXY(contours1.get(0)))
			                .stroke(Color.black)
			                .fill(Color.blue))
			        .shape(ProceduralRenderer.Builder.polygon()
			                .points(toXY(contours1.get(1)))
			                .stroke(Color.black)
			                .fill(Color.red))
			        .build();

			BufferedImage image = new ProceduralRenderer().render(proc);
			VisualizerUtil.show("contours1", image);
		}

		// render
		if(DEBUG) {
			ProceduralRenderer.Procedure proc = ProceduralRenderer.Builder.describe()
			        .shape(ProceduralRenderer.Builder.polygon()
			                .points(toXY(contours2.get(0)))
			                .stroke(Color.black)
			                .fill(Color.blue))
			        .shape(ProceduralRenderer.Builder.polygon()
			                .points(toXY(contours2.get(1)))
			                .stroke(Color.black)
			                .fill(Color.red))
			        .build();

			BufferedImage image = new ProceduralRenderer().render(proc);
			VisualizerUtil.show("contours2", image);
		}

		// render
		if(DEBUG) {
			ProceduralRenderer.Procedure proc = ProceduralRenderer.Builder.describe()
			        .shape(ProceduralRenderer.Builder.polygon()
			                .points(toXY(contours3.get(0)))
			                .stroke(Color.black)
			                .fill(Color.blue))
			        .shape(ProceduralRenderer.Builder.polygon()
			                .points(toXY(contours3.get(1)))
			                .stroke(Color.black)
			                .fill(Color.red))
			        .build();

			BufferedImage image = new ProceduralRenderer().render(proc);
			VisualizerUtil.show("contours3", image);
		}

		double mismatch_1_2 = PolyMatch.match(contours1, contours2);
		double mismatch_1_3 = PolyMatch.match(contours1, contours3);

		assertTrue(mismatch_1_2 < mismatch_1_3);
	}

	@Test
	public void testClassifyTwitterIcon() throws IOException {
		Image.Int testImage = ImageUtil.grow(ImageUtil.read(ClassLoader.getSystemResource(toResourcePath("home_screen.png"))), 2, 2);

		GraphBlobBuilder testBuilder = new GraphBlobBuilder(testImage.w, testImage.h);
		Blob[] testBlobs = testBuilder.build(testImage);

		// render
		if (DEBUG) {
			Image.Int hierarchy = new Image.Int(testImage.w, testImage.h);
			BlobUtils.drawHierarchy(testBlobs, hierarchy);
			VisualizerUtil.show("blobs", ImageUtil.toBufferedImage(hierarchy), 1f);
		}

		// match
		{
			Contour train = toContour(twitter_bird_points);

			for (int i = 1; i < testBlobs.length; i++) {
				Blob child = testBlobs[i];
				Contour test = new Contour(Contours.contourTrace(child));
				
				double geometricMismatch = compareContours(train, test);
				
				if (geometricMismatch < 10d) {
					Color color = IconContour.extractColor(testImage, child);

					ProceduralRenderer.Procedure proc = ProceduralRenderer.Builder.describe()
					        .shape(ProceduralRenderer.Builder.polygon()
					                .points(toXY(train))
					                .stroke(color)
					                .fill(color))
					        .build();

					BufferedImage trainImage = new ProceduralRenderer().render(proc);
					BufferedImage testImageCut = scale(ImageUtil.toBufferedImage(BlobUtils.cutByMask(testImage, child)),
					        trainImage.getWidth(),
					        trainImage.getHeight());

					BufferedImage mismatchImage = new BufferedImage(trainImage.getWidth(), trainImage.getHeight(), trainImage.getType());

					float photometricMismatch = computeError(trainImage, testImageCut, mismatchImage);

					if(DEBUG) {
						VisualizerUtil.show("mismatch -> geometric " + geometricMismatch + " photometric " + photometricMismatch, mismatchImage, 1f);
						VisualizerUtil.show("test", testImageCut, 1f);
						VisualizerUtil.show("train", trainImage, 1f);
					}
				}
			}
		}

		if(DEBUG) {
			System.in.read();
		}
	}

	@Test
	public void testClassifyButton() throws IOException {

		// train
		Contour trainContour;
		BufferedImage trainImageCut;
		{
			Image.Int trainImage = ImageUtil.grow(ImageUtil.read(ClassLoader.getSystemResource(toResourcePath("twitter_button.png"))), 2, 2);
			GraphBlobBuilder trainBuilder = new GraphBlobBuilder(trainImage.w, trainImage.h);
			Blob[] blobs = trainBuilder.build(trainImage);
			Blob buttonBlob = blobs[0].children.get(0).children.get(0);

			trainImageCut = ImageUtil.toBufferedImage(BlobUtils.cutByMask(trainImage, buttonBlob));
			trainContour = new Contour(Contours.contourTrace(buttonBlob));
		}

		int matches = 0;
		
		// test
		{
			Image.Int testImage = ImageUtil.grow(ImageUtil.read(ClassLoader.getSystemResource(toResourcePath("tweet_screen.png"))), 2, 2);
			GraphBlobBuilder testBuilder = new GraphBlobBuilder(testImage.w, testImage.h);
			Blob[] testBlobs = testBuilder.build(testImage);

			// blobs
			if(DEBUG) {
				Image.Int hierarchy = new Image.Int(testImage.w, testImage.h);
				BlobUtils.drawHierarchy(testBlobs, hierarchy);
				VisualizerUtil.show("blobs", ImageUtil.toBufferedImage(hierarchy), 1f);
				VisualizerUtil.show("contours", renderContours(testBlobs[0].children.get(0)), 1f);
			}

			// match
			for (int i = 1; i < testBlobs.length; i++) {
				Blob blob = testBlobs[i];
				Contour testContour = new Contour(Contours.contourTrace(blob));

				double geometricMismatch = compareContours(trainContour, testContour);
				
				if (geometricMismatch < 10f) {
					matches++;
					if(DEBUG) {
						ProceduralRenderer.Procedure procTrain = ProceduralRenderer.Builder.describe()
						        .shape(ProceduralRenderer.Builder.polygon()
						                .points(toXY(trainContour))
						                .stroke(Color.blue)
						                .fill(Color.white))
						        .build();
	
						ProceduralRenderer.Procedure procTest = ProceduralRenderer.Builder.describe()
						        .shape(ProceduralRenderer.Builder.polygon()
						                .points(toXY(testContour))
						                .stroke(Color.blue)
						                .fill(Color.white))
						        .build();
	
						VisualizerUtil.show("contour (train)", new ProceduralRenderer().render(procTrain), 1f);
						VisualizerUtil.show("contour (test)", new ProceduralRenderer().render(procTest), 1f);
	
						BufferedImage testImageCut = ImageUtil.toBufferedImage(BlobUtils.cutByMask(testImage, blob));
	
						VisualizerUtil.show("test", testImageCut, 1f);
						VisualizerUtil.show("train", trainImageCut, 1f);
					}
				}
			}
		}
		
		if(DEBUG) {
			System.in.read();
		}

		assertThat(matches, is(not(0)));
	}

	@Test
	public void testClassifyButtonProcedure() throws IOException {

		Image.Int testImage = ImageUtil.grow(ImageUtil.read(ClassLoader.getSystemResource(toResourcePath("tweet_screen.png"))), 2, 2);
		GraphBlobBuilder testBuilder = new GraphBlobBuilder(testImage.w, testImage.h);
		Blob[] testBlobs = testBuilder.build(testImage);

		// blobs
		if(DEBUG) {
			Image.Int hierarchy = new Image.Int(testImage.w, testImage.h);
			BlobUtils.drawHierarchy(testBlobs, hierarchy);
			VisualizerUtil.show("blobs", ImageUtil.toBufferedImage(hierarchy), 1f);
			VisualizerUtil.show("contours", renderContours(testBlobs[0].children.get(0)), 1f);
		}
		
		int matches = 0;

		// match
		for (int i = 1; i < testBlobs.length; i++) {
			Blob blob = testBlobs[i];
			if (blob.bbox.width < 100 || blob.bbox.height < 20 || blob.bbox.width > 200 || blob.bbox.height > 100) {
				continue;
			}

			Contour testContour = new Contour(Contours.contourTrace(blob));

			ProceduralRenderer.Procedure button = button(blob.bbox.width, blob.bbox.height);
			Contour trainContour = ProceduralRenderer.getContour(button.shapes.get(0));

			double geometricMismatch = compareContours(trainContour, testContour);

			System.out.println(geometricMismatch);

			if (geometricMismatch < 1d) {
				matches++;
				if(DEBUG) {
					ProceduralRenderer.Procedure procTrain = ProceduralRenderer.Builder.describe()
					        .shape(ProceduralRenderer.Builder.polygon()
					                .points(toXY(trainContour))
					                .stroke(Color.blue)
					                .fill(Color.white))
					        .build();
	
					ProceduralRenderer.Procedure procTest = ProceduralRenderer.Builder.describe()
					        .shape(ProceduralRenderer.Builder.polygon()
					                .points(toXY(testContour))
					                .stroke(Color.blue)
					                .fill(Color.white))
					        .build();
	
					VisualizerUtil.show("contour (train)", new ProceduralRenderer().render(procTrain), 1f);
					VisualizerUtil.show("contour (test)", new ProceduralRenderer().render(procTest), 1f);
	
					BufferedImage testImageCut = ImageUtil.toBufferedImage(BlobUtils.cutByMask(testImage, blob));
	
					VisualizerUtil.show("test", testImageCut, 1f);
					VisualizerUtil.show("train", new ProceduralRenderer().render(button), 1f);
				}
			}
		}
		
		if(DEBUG) {
			System.in.read();
		}

		assertThat(matches, is(not(0)));
	}

	@Test
	public void testClassifyTwitterIconTiming() throws IOException {
		Image.Int image = ImageUtil.grow(ImageUtil.read(ClassLoader.getSystemResource(toResourcePath("home_screen.png"))), 2, 2);

		GraphBlobBuilder builder = new GraphBlobBuilder(image.w, image.h);
		Blob[] blobs = builder.build(image);

		// render
		if(DEBUG) {
			Image.Int hierarchy = new Image.Int(image.w, image.h);
			BlobUtils.drawHierarchy(blobs, hierarchy);
			VisualizerUtil.show("blobs", ImageUtil.toBufferedImage(hierarchy), 1f);
		}

		// match
		long start = System.currentTimeMillis();
		{
			Contour train = toContour(twitter_bird_points);

			for (int i = 1; i < blobs.length; i++) {
				Blob child = blobs[i];
				Contour test = new Contour(Contours.contourTrace(child));
				compareContours(train, test);
			}
		}
		long end = System.currentTimeMillis();

		System.out.println("classifying " + blobs.length + " blobs took " + (end - start) + "ms");
	}

	@Test
	public void testClassifyCamIcon() throws IOException {
		final Image.Int trainImage = ImageUtil.grow(ImageUtil.read(ClassLoader.getSystemResource(toResourcePath("cam.png"))), 2, 2);
		GraphBlobBuilder trainBuilder = new GraphBlobBuilder(trainImage.w, trainImage.h);
		Blob[] trainBlobs = trainBuilder.build(trainImage);

		final Image.Int testImage = ImageUtil.grow(ImageUtil.read(ClassLoader.getSystemResource(toResourcePath("cam_button.png"))), 2, 2);
		GraphBlobBuilder testBuilder = new GraphBlobBuilder(testImage.w, testImage.h);
		Blob[] testBlobs = testBuilder.build(testImage);

		// render
		if(DEBUG) {
			Image.Int hierarchy = new Image.Int(testImage.w, testImage.h);
			BlobUtils.drawHierarchy(testBlobs, hierarchy);
			VisualizerUtil.show("blobs", ImageUtil.toBufferedImage(hierarchy), 1f);
		}

		// match
		{
			Blob trainBlob = trainBlobs[0].children.get(0).children.get(0);
			List<Contour> train = extractContours(trainBlob);

			Blob testBlob = testBlobs[0].children.get(0).children.get(0).children.get(0);
			List<Contour> test = extractContours(testBlob);

			double geometricMismatch = compareContours(train, test);

			// train
			if(DEBUG) {
				ProceduralRenderer.Builder buildProc = ProceduralRenderer.Builder.describe();
				for (Contour contour : train) {
					buildProc.shape(ProceduralRenderer.Builder.polygon()
					        .points(toXY(contour))
					        .stroke(randomColor())
					        .fill(randomColor()));
				}

				ProceduralRenderer.Procedure proc = buildProc.build();
				VisualizerUtil.show("train", new ProceduralRenderer().render(proc), 1f);
			}

			// test
			if(DEBUG) {
				ProceduralRenderer.Builder buildProc = ProceduralRenderer.Builder.describe();
				for (Contour contour : test) {
					buildProc.shape(ProceduralRenderer.Builder.polygon()
					        .points(toXY(contour))
					        .stroke(randomColor())
					        .fill(randomColor()));
				}

				ProceduralRenderer.Procedure proc = buildProc.build();
				VisualizerUtil.show("test", new ProceduralRenderer().render(proc), 1f);
			}
			
			assertThat(geometricMismatch, is(0d));
		}
	}

	private static BufferedImage renderContours(Blob blob) {
		List<Contour> contours = extractContours(blob);
		ProceduralRenderer.Builder buildProc = ProceduralRenderer.Builder.describe();
		for (Contour contour : contours) {
			buildProc.shape(ProceduralRenderer.Builder.polygon()
			        .points(toXY(contour))
			        .stroke(randomColor())
			        .fill(Color.white));
		}

		ProceduralRenderer.Procedure proc = buildProc.build();
		return new ProceduralRenderer().render(proc);
	}

	private static Contour toContour(double... xy) {
		List<Point.Double> points = new LinkedList<>();
		for (int i = 0; i < xy.length; i += 2) {
			points.add(new Point.Double(xy[i + 0], xy[i + 1]));
		}
		return new Contour(points);
	}

	private static Color[] generateColors() {
		Random random = new Random();
		final int length = 256;
		Color[] colors = new Color[length];
		for (int i = 0; i < length; i++) {
			colors[i] = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
		}
		return colors;
	}

	private static Color randomColor() {
		return generateColors()[new Random().nextInt(255)];
	}

	private static List<Contour> extractContours(Blob blob) {
		List<Contour> contours = new LinkedList<Contour>();
		contours.add(new Contour(Contours.contourTrace(blob)));
		for (Blob child : blob.children) {
			extractContours(child, child.bbox.x - blob.bbox.x, child.bbox.y - blob.bbox.y, contours);
		}
		return contours;
	}

	private static void extractContours(Blob blob, int offsetX, int offsetY, List<Contour> contours) {
		contours.add(new Contour(offset(Contours.contourTrace(blob), offsetX, offsetY)));
		for (Blob child : blob.children) {
			extractContours(child, offsetX + child.bbox.x - blob.bbox.x, offsetY + child.bbox.y - blob.bbox.y, contours);
		}
	}

	private static List<Point.Double> offset(List<Point.Double> points, int x, int y) {
		for (Point.Double point : points) {
			point.x += x;
			point.y += y;
		}
		return points;
	}

	private static double compareContours(List<Contour> trainContour, List<Contour> testContour) {
		return PolyMatch.match(trainContour, testContour);
	}

	private static double compareContours(Contour trainContour, Contour testContour) {
		return PolyMatch.match(trainContour, testContour);
	}

	public static double[] toXY(Contour points) {
		double[] xy = new double[points.npoints * 2];
		for (int i = 0; i < points.npoints; i++) {
			xy[i * 2 + 0] = points.xpoints[i];
			xy[i * 2 + 1] = points.ypoints[i];
		}
		return xy;
	}
	
	public static float computeError(BufferedImage train,
	        BufferedImage test, BufferedImage mismatch) {
		float sum = 0f;
		int pixels = 0;
		for (int x = 0; x < train.getWidth(); x++) {
			for (int y = 0; y < train.getHeight(); y++) {
				if (isTransparent(test.getRGB(x, y)) == false) {
					int sampleRgb = train.getRGB(x, y);
					int candidateRgb = test.getRGB(x, y);
					float distance = distance(sampleRgb, candidateRgb);
					sum += distance;
					pixels++;
					mismatch.setRGB(x, y, new Color(distance, distance, distance, 1f).getRGB());
				} else {
					mismatch.setRGB(x, y, Color.blue.getRGB());
				}
			}
		}
		return (sum / pixels) * 100;
	}

	private static boolean isTransparent(int rgba) {
		int a = (rgba >> 24) & 0xff;
		return a != 255;
	}

	public static float distance(int rgb1, int rgb2) {
		int r1 = (rgb1 >> 16) & 0xff;
		int g1 = (rgb1 >> 8) & 0xff;
		int b1 = (rgb1 >> 0) & 0xff;

		int r2 = (rgb2 >> 16) & 0xff;
		int g2 = (rgb2 >> 8) & 0xff;
		int b2 = (rgb2 >> 0) & 0xff;

		return (Math.abs(r1 - r2) / 256f + Math.abs(g1 - g2) / 256f + Math
		        .abs(b1 - b2) / 256f) / 3f;
	}

	public static BufferedImage scale(BufferedImage src, int w, int h) {
		BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = dest.createGraphics();
		AffineTransform at = AffineTransform.getScaleInstance((double) w / src.getWidth(), (double) h / src.getHeight());
		g.drawRenderedImage(src, at);
		return dest;
	}
	
}
