package org.testobject.kernel.ocr;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.junit.Test;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.GraphBlobBuilder;
import org.testobject.kernel.imgproc.classifier.Classes;
import org.testobject.kernel.imgproc.classifier.Classifier;
import org.testobject.kernel.imgproc.classifier.Context;
import org.testobject.kernel.imgproc.classifier.VisitingMutator;
import org.testobject.kernel.imgproc.plot.VisualizerUtil;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.kernel.ocr.AdditiveMask;
import org.testobject.kernel.ocr.freetype.FT2Library;
import org.testobject.commons.util.collections.Sets;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.file.FileUtil;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.classifier.GroupClassifier;
import org.testobject.kernel.imgproc.classifier.TextCharClassifier;
import org.testobject.kernel.imgproc.classifier.TextWordClassifier;
import org.testobject.kernel.ocr.Decolorizer;
import org.testobject.kernel.ocr.MaskClusterer;
import org.testobject.kernel.ocr.SlidingMaskTextRecognizer;
import org.testobject.kernel.ocr.SlidingMaskTextRecognizer.MaskMatch;
import org.testobject.kernel.ocr.freetype.FreeTypeGrayscaleFontRenderer;

/**
 * 
 * @author enijkamp
 * 
 */
public class SlidingMaskTextRecognizerStagingTest {

	private static final boolean DEBUG = Debug.toDebugMode(true);

	private static final int HISTOGRAM_N = 7;
	private static final double MAX_DIST = 1d;
	private static final int MAX_TOLERANCE_X = 2;

	private static final FreeTypeGrayscaleFontRenderer fontRenderer = new FreeTypeGrayscaleFontRenderer(160, 240,
			FT2Library.FT_LCD_FILTER_LIGHT);

	private static final MaskClusterer maskClusterer = new MaskClusterer(fontRenderer, HISTOGRAM_N, 1400f, 800f);

	public static void main(String... args) throws IOException {
		BufferedImage image = fontRenderer.drawChar(getFont("Roboto-Regular.ttf"), 12, '2');
		VisualizerUtil.show("gray (freetype)", image, 20f);
	}

	@Test
	public void testCancel_Ca() throws Throwable {
		// const
		final File font = getFont("Roboto-Bold.ttf");
		final int fontSize = 12;
		final int blobId = 3;
		final char[] chars = { 'C', 'a' };
		final Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/twitter/Cancel.png");

		// classify
		Blob blob = classifyText(image, fontSize, new File[] { font })[blobId];
		Classes.TextChar text = (Classes.TextChar) blob.meta;
		Image.Byte grayImage = prepareImage(image, blob);

		// recognize
		Queue<MaskMatch> masks = createMaskQueue();
		boolean[] coverage = new boolean[image.w];
		computeMasks(font, fontSize, chars, blob, text, grayImage, masks, coverage);

		// print
		int covered = sum(coverage);
		if (DEBUG) {
			System.out.println(covered);
			for (MaskMatch mask : masks) {
				System.out.println(mask.chars + " x=" + mask.offsetX + " dist=" + mask.dist);
			}
		}

		// assert
		{
			assertThat(covered >= 17, is(true));

			assertThat(masks.size() >= 2, is(true));

			// a x=10 dist=0.3144036127845812
			{
				MaskMatch mask = masks.poll();
				assertThat(mask.chars, is("a"));
				assertThat(mask.dist <= .32d, is(true));
			}
			// C x=0 dist=0.41327466442644417
			{
				MaskMatch mask = masks.poll();
				assertThat(mask.chars, is("C"));
				assertThat(mask.dist <= .42d, is(true));
			}
		}
	}

	@Test
	public void testCancel_nc() throws Throwable {
		// const
		final File font = getFont("Roboto-Bold.ttf");
		final int fontSize = 12;
		final int blobId = 5;
		final char[] chars = { 'n', 'c' };
		final Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/twitter/Cancel.png");

		if (DEBUG) {
			VisualizerUtil.show("input complete", image);
		}

		// classify
		Blob blob = classifyText(image, fontSize, new File[] { font })[blobId];
		Classes.TextChar text = (Classes.TextChar) blob.meta;
		Image.Byte grayImage = prepareImage(image, blob);

		if (DEBUG) {
			VisualizerUtil.show("input", ImageUtil.toImageInt(grayImage), 20f);
		}

		// recognize
		Queue<MaskMatch> masks = createMaskQueue();
		boolean[] coverage = new boolean[image.w];
		computeMasks(font, fontSize, chars, blob, text, grayImage, masks, coverage);

		// print
		int covered = sum(coverage);
		if (DEBUG) {
			System.out.println(covered);
			for (MaskMatch mask : masks) {
				System.out.println(mask.chars + " x=" + mask.offsetX + " dist=" + mask.dist);
			}
		}

		// assert
		{
			assertThat(covered >= 17, is(true));

			assertThat(masks.size() >= 2, is(true));
			// a x=10 dist=0.3144036127845812
			{
				MaskMatch mask = masks.poll();
				assertThat(mask.chars, is("n"));
				assertThat(mask.dist <= .4d, is(true));
			}
			// C x=0 dist=0.9170301722360927
			{
				MaskMatch mask = masks.poll();
				assertThat(mask.chars, is("c"));
				assertThat(mask.dist <= .4d, is(true));
			}
		}
	}

	@Test
	public void test1233_2() throws Throwable {
		// const
		final File font = getFont("Roboto-Regular.ttf");
		final int fontSize = 16;
		final int blobId = 1;
		final char[] chars = { '2' };
		final Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/system/1233.png");

		if (DEBUG) {
			VisualizerUtil.show("input complete", image);
		}

		// classify
		Blob blob = classifyText(image, fontSize, new File[] { font })[blobId];
		Classes.TextChar text = (Classes.TextChar) blob.meta;
		Image.Byte grayImage = prepareImage(image, blob);

		if (DEBUG) {
			VisualizerUtil.show("input", ImageUtil.toImageInt(grayImage), 20f);
		}

		// recognize
		Queue<MaskMatch> masks = createMaskQueue();
		boolean[] coverage = new boolean[image.w];
		computeMasks(font, fontSize, chars, blob, text, grayImage, masks, coverage);

		// print
		int covered = sum(coverage);
		if (DEBUG) {
			System.out.println(covered);
			for (MaskMatch mask : masks) {
				System.out.println(mask.chars + " x=" + mask.offsetX + " dist=" + mask.dist);
			}
		}

		// assert
		{
			assertThat(covered >= 10, is(true));

			assertThat(masks.size() >= 1, is(true));
			// 2 x=0 dist=0.19179451599611755
			{
				MaskMatch mask = masks.poll();
				assertThat(mask.chars, is("2"));
				assertThat(mask.dist <= .2d, is(true));
			}

		}
	}

	@Test
	public void testTweet_t() throws Throwable {
		// const
		final File font = getFont("Roboto-Bold.ttf");
		final int fontSize = 12;
		final int blobId = 3;
		final char[] chars = "t".toCharArray();
		final Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/twitter/Tweet.png");

		if (DEBUG) {
			VisualizerUtil.show("input complete", image);
		}

		// classify
		Blob blob = classifyText(image, fontSize, new File[] { font })[blobId];
		Classes.TextChar text = (Classes.TextChar) blob.meta;
		Image.Byte grayImage = prepareImage(image, blob);

		if (DEBUG) {
			VisualizerUtil.show("input", ImageUtil.toImageInt(grayImage), 20f);
		}

		// recognize
		Queue<MaskMatch> masks = createMaskQueue();
		boolean[] coverage = new boolean[image.w];
		computeMasks(font, fontSize, chars, blob, text, grayImage, masks, coverage);

		// print
		int covered = sum(coverage);
		if (DEBUG) {
			System.out.println(covered);
			for (MaskMatch mask : masks) {
				System.out.println(mask.chars + " x=" + mask.offsetX + " dist=" + mask.dist);
			}
		}

		// assert
		{
			assertThat(covered >= 6, is(true));

			assertThat(masks.size() >= 1, is(true));
			// t x=41 dist=0.361841167327077
			{
				MaskMatch mask = masks.poll();
				assertThat(mask.chars, is("t"));
				assertThat(mask.dist <= .37d, is(true));
			}

		}
	}

	@Test
	public void testTweet_w() throws Throwable {
		// const
		final File font = getFont("Roboto-Bold.ttf");
		final int fontSize = 12;
		final int blobId = 3;
		final char[] chars = "w".toCharArray();
		final Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/twitter/Tweet.png");

		if (DEBUG) {
			VisualizerUtil.show("input complete", image);
		}

		// classify
		Blob blob = classifyText(image, fontSize, new File[] { font })[blobId];
		Classes.TextChar text = (Classes.TextChar) blob.meta;
		Image.Byte grayImage = prepareImage(image, blob);

		if (DEBUG) {
			VisualizerUtil.show("input", ImageUtil.toImageInt(grayImage), 20f);
		}

		// recognize
		Queue<MaskMatch> masks = createMaskQueue();
		boolean[] coverage = new boolean[image.w];
		computeMasks(font, fontSize, chars, blob, text, grayImage, masks, coverage);

		// print
		int covered = sum(coverage);
		if (DEBUG) {
			System.out.println(covered);
			for (MaskMatch mask : masks) {
				System.out.println(mask.chars + " x=" + mask.offsetX + " dist=" + mask.dist);
			}
		}

		// assert
		{
			assertThat(covered >= 12, is(true));

			assertThat(masks.size() >= 1, is(true));
			// w x=10 dist=0.6050902997619737
			{
				MaskMatch mask = masks.poll();
				assertThat(mask.chars, is("w"));
				assertThat(mask.dist <= .61d, is(true));
			}

		}
	}

	@Test
	public void testTweet_e() throws Throwable {
		// const
		final File font = getFont("Roboto-Bold.ttf");
		final int fontSize = 12;
		final int blobId = 3;
		final char[] chars = "e".toCharArray();
		final Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/twitter/Tweet.png");

		if (DEBUG) {
			VisualizerUtil.show("input complete", image);
		}

		// classify
		Blob blob = classifyText(image, fontSize, new File[] { font })[blobId];
		Classes.TextChar text = (Classes.TextChar) blob.meta;
		Image.Byte grayImage = prepareImage(image, blob);

		if (DEBUG) {
			VisualizerUtil.show("input", ImageUtil.toImageInt(grayImage), 20f);
		}

		// recognize
		Queue<MaskMatch> masks = createMaskQueue();
		boolean[] coverage = new boolean[image.w];
		computeMasks(font, fontSize, chars, blob, text, grayImage, masks, coverage);

		// print
		int covered = sum(coverage);
		if (DEBUG) {
			System.out.println(covered);
			for (MaskMatch mask : masks) {
				System.out.println(mask.chars + " x=" + mask.offsetX + " dist=" + mask.dist);
			}
		}

		// assert
		{
			assertThat(covered >= 18, is(true));

			assertThat(masks.size() >= 2, is(true));
			// e x=32 dist=0.3726118208709544
			{
				MaskMatch mask = masks.poll();
				assertThat(mask.chars, is("e"));
				assertThat(mask.dist <= 0.38d, is(true));
			}
			// e x=23 dist=0.38070290959646846
			{
				MaskMatch mask = masks.poll();
				assertThat(mask.chars, is("e"));
				assertThat(mask.dist <= 0.39d, is(true));
			}

		}
	}

	@Test
	public void test20Apr_2() throws Throwable {
		// const
		final File font = getFont("Roboto-Regular.ttf");
		final int fontSize = 12;
		final int blobId = 2;
		final char[] chars = "2".toCharArray();
		final Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/twitter/20Apr.png");

		if (DEBUG) {
			VisualizerUtil.show("input complete", image);
		}

		// classify
		Blob blob = classifyText(image, fontSize, new File[] { font })[blobId];
		Classes.TextChar text = (Classes.TextChar) blob.meta;
		Image.Byte grayImage = prepareImage(image, blob);

		if (DEBUG) {
			VisualizerUtil.show("input", ImageUtil.toImageInt(grayImage), 20f);
		}

		// recognize
		Queue<MaskMatch> masks = createMaskQueue();
		boolean[] coverage = new boolean[image.w];
		computeMasks(font, fontSize, chars, blob, text, grayImage, masks, coverage);

		// print
		int covered = sum(coverage);
		if (DEBUG) {
			System.out.println(covered);
			for (MaskMatch mask : masks) {
				System.out.println(mask.chars + " x=" + mask.offsetX + " dist=" + mask.dist);
			}
		}

		// assert
		{
			assertThat(covered >= 8, is(true));

			assertThat(masks.size() >= 1, is(true));
			// 2 x=0 dist=0.5535281578474344
			{
				MaskMatch mask = masks.poll();
				assertThat(mask.chars, is("2"));
				assertThat(mask.dist <= 0.56d, is(true));
			}

		}
	}

	private static Image.Byte prepareImage(Image.Int image, Blob blob) {
		Image.Int decolorImage = new Decolorizer().decolorize(image, blob);
		Image.Int fatImage = SlidingMaskTextRecognizer.fatImage(decolorImage);
		return ImageUtil.toImageByte(fatImage);
	}

	private static void computeMasks(final File font, final int fontSize,
			final char[] chars, Blob blob, Classes.TextChar text,
			Image.Byte byteImage, Queue<MaskMatch> masks, boolean[] coverage) {
		List<AdditiveMask> additiveMasks = maskClusterer.generateMasksFast(toListList(font), new int[] { fontSize }, chars);
		SlidingMaskTextRecognizer recognizer = new SlidingMaskTextRecognizer(additiveMasks, HISTOGRAM_N, MAX_DIST, MAX_TOLERANCE_X);
		recognizer.calculateMaskWithClassifierCharsAndSize(byteImage, blob.bbox.width, Sets.fromArray(fontSize), text, masks, coverage);
	}

	private static int sum(boolean[] array) {
		int sum = 0;
		for (boolean bool : array) {
			sum += bool ? 1 : 0;
		}
		return sum;
	}

	private static Queue<MaskMatch> createMaskQueue() {
		Comparator<MaskMatch> maskComparator = new Comparator<MaskMatch>() {
			@Override
			public int compare(MaskMatch m1, MaskMatch m2) {
				return Double.compare(m1.dist, m2.dist);
			}
		};

		return new PriorityQueue<MaskMatch>(200, maskComparator);
	}

	private static List<List<File>> toListList(File file) {
		return Collections.singletonList(Collections.singletonList(file));
	}

	private static File getFont(String font) {
		return FileUtil.toFileFromSystem("android/4_0_3/fonts/sans-serif/" + font);
	}

	private static Blob[] classifyText(Image.Int image, final int fontSize, final File[] font) throws Throwable {
		Blob[] blobs = new GraphBlobBuilder(image.w, image.h).build(image);
		// classification
		Classifier[] before = { new GroupClassifier(), new TextCharClassifier(), new TextWordClassifier() };
		for (Classifier beforeClassifier : before) {
			new VisitingMutator(beforeClassifier).mutate(new Context(image, image), blobs[0]);
		}

		return blobs;
	}
}
