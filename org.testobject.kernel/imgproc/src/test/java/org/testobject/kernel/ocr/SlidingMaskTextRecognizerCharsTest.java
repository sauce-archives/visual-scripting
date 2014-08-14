package org.testobject.kernel.ocr;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import org.junit.Ignore;
import org.junit.Test;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.BlobUtils;
import org.testobject.kernel.imgproc.blob.GraphBlobBuilder;
import org.testobject.kernel.imgproc.classifier.Classes;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.kernel.ocr.*;
import org.testobject.kernel.ocr.freetype.FT2Library;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.file.FileUtil;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.plot.VisualizerUtil;
import org.testobject.kernel.ocr.SlidingMaskTextRecognizer.MaskMatch;
import org.testobject.kernel.ocr.freetype.FreeTypeGrayscaleFontRenderer;

/**
 * 
 * @author enijkamp
 * 
 */
public class SlidingMaskTextRecognizerCharsTest {

	private static final boolean DEBUG = Debug.toDebugMode(true);

	private static final int HISTOGRAM_N = 7;
	private static final double MAX_DIST = 1d;
	private static final int MAX_TOLERANCE_X = 2;

	@Test
	public void test_T() throws Throwable {

		final String image = "Test.png";
		final int blobId = 2;
		final char chr = 'T';
		final int size = 16;
		final double maxDist = 0.78f;
		final int offsetX = 0;

		testChar(image, chr, size, blobId, maxDist, offsetX);
	}

	@Test
	public void test_e() throws Throwable {

		final String image = "Test.png";
		final int blobId = 4;
		final char chr = 'e';
		final int size = 16;
		final double maxDist = 0.4f;
		final int offsetX = 0;

		testChar(image, chr, size, blobId, maxDist, offsetX);
	}

	@Test
	public void test_s() throws Throwable {

		final String image = "Test.png";
		final int blobId = 5;
		final char chr = 's';
		final int size = 16;
		final double maxDist = 0.57f;
		final int offsetX = 0;

		testChar(image, chr, size, blobId, maxDist, offsetX);
	}

	@Test
	public void test_t() throws Throwable {

		final String image = "Test.png";
		final int blobId = 3;
		final char chr = 't';
		final int size = 16;
		final double maxDist = 0.7f;
		final int offsetX = 0;

		testChar(image, chr, size, blobId, maxDist, offsetX);
	}
	
	// FIXME get working (en)
	@Ignore
	@Test
	public void Discard_i() throws Throwable {

		final String image = "Discard.png";
		final int blobId = 6;
		final char chr = 'i';
		final int size = 14;
		final double maxDist = 0.7f;
		final int offsetX = 0;

		testChar(image, chr, size, blobId, maxDist, offsetX);
	}

	@Test
	public void testDebugManyMasks_t() throws Throwable {
		final List<List<File>> fontFiles = toFonts(ClassLoader.getSystemResource("./android/4_0_3/fonts"), "sans-serif");
		final int[] fontSizes = { 14, 15, 16, 17, 18, 19, 20 };
		final int blobId = 3;
		final char chr = 't';

		// train
		FontRenderer renderer = new FreeTypeGrayscaleFontRenderer(160, 240, FT2Library.FT_LCD_FILTER_LIGHT);
		MaskClusterer maskClusterer = new MaskClusterer(renderer, HISTOGRAM_N, 1400f, 800f);
		List<AdditiveMask> additiveMasks = maskClusterer.generateMasksFast(fontFiles, fontSizes);

		if (DEBUG) {
			VisualizerUtil.show("train char '" + chr + "'", renderer.drawChar(toFont("Roboto-Regular.ttf"), 16, chr), 20f);
		}

		// test
		Image.Int image = ImageUtil.read(toTwitterPath("Test.png"));
		Blob[] blobs = new GraphBlobBuilder(image.w, image.h).build(image);
		Blob blob = blobs[blobId];

		if (DEBUG) {
			VisualizerUtil.show("test char '" + chr + "'", BlobUtils.cutByMask(image, blob), 20f);
		}

		Image.Int decolor = new Decolorizer().decolorize(image, blob);

		if (DEBUG) {
			VisualizerUtil.show("test char '" + chr + "' (decolor)", decolor, 20f);
		}

		SlidingMaskTextRecognizer recognizer = new SlidingMaskTextRecognizer(additiveMasks, HISTOGRAM_N, MAX_DIST, MAX_TOLERANCE_X);
		Image.Int fat = SlidingMaskTextRecognizer.fatImage(decolor);

		Classes.TextChar text = new Classes.TextChar(Collections.<Classes.TextChar.Candidate> emptyList());
		Queue<MaskMatch> masks = recognizer.calculateMasks(ImageUtil.toImageByte(fat), fat.w, Collections.<Integer> emptySet(), text);

		// assert
		{
			assertThat(masks.isEmpty(), is(false));
			MaskMatch match = masks.poll();

			if (DEBUG) {
				System.out.println(match.chars + " -> " + String.format("%.3f", match.dist) + "\t" + "x = " + match.offsetX + "\ty = "
						+ match.offsetY + "\tw = " + match.characterMask.width + "\th = " + match.characterMask.height);
			}

			assertThat(match.chars.charAt(0), is(chr));
			assertThat(match.dist < 0.65d, is(true));
			assertThat(match.offsetX, is(0));
		}
	}

	private void testChar(String file, char chr, int fontSize, int blobId, double maxDistance, int offsetX) throws Throwable {
		final File fontFile = toFont("Roboto-Regular.ttf");

		// train
		FontRenderer renderer = new FreeTypeGrayscaleFontRenderer(160, 240, FT2Library.FT_LCD_FILTER_LIGHT);
		MaskClusterer maskClusterer = new MaskClusterer(renderer, HISTOGRAM_N, 1400f, 800f);
		List<AdditiveMask> additiveMasks = maskClusterer.generateMasksFast(toFonts(fontFile), fontSize, new char[] { chr });

		if (DEBUG) {
			VisualizerUtil.show("train char '" + chr + "'", renderer.drawChar(fontFile, fontSize, chr), 20f);
		}

		// test
		Image.Int image = ImageUtil.read(toTwitterPath(file));
		Blob[] blobs = new GraphBlobBuilder(image.w, image.h).build(image);
		Blob blob = blobs[blobId];

		if (DEBUG) {
			VisualizerUtil.show("test char '" + chr + "'", BlobUtils.cutByMask(image, blob), 20f);
		}

		Image.Int decolor = new Decolorizer().decolorize(image, blob);

		if (DEBUG) {
			VisualizerUtil.show("test char '" + chr + "' (decolor)", decolor, 20f);
		}

		SlidingMaskTextRecognizer recognizer = new SlidingMaskTextRecognizer(additiveMasks, HISTOGRAM_N, MAX_DIST, MAX_TOLERANCE_X);
		Image.Int fat = SlidingMaskTextRecognizer.fatImage(decolor);

		Classes.TextChar text = new Classes.TextChar(Collections.<Classes.TextChar.Candidate> emptyList());
		Queue<MaskMatch> masks = recognizer.calculateMasks(ImageUtil.toImageByte(fat), fat.w, Collections.<Integer> emptySet(), text);

		// assert
		{
			assertThat(masks.isEmpty(), is(false));
			MaskMatch match = masks.poll();

			if (DEBUG) {
				System.out.println(match.chars + " -> " + String.format("%.3f", match.dist) + "\t" + "x = " + match.offsetX + "\ty = "
						+ match.offsetY + "\tw = " + match.characterMask.width + "\th = " + match.characterMask.height);
			}

			assertThat(match.chars.charAt(0), is(chr));
			assertThat(match.dist < maxDistance, is(true));
			assertThat(match.offsetX, is(offsetX));
		}
	}

	private static String toTwitterPath(String file) {
		return "android/4_0_3/classifier/text/positives/twitter/" + file;
	}

	private static final File toFont(String font) {
		return FileUtil.toFileFromSystem("android/4_0_3/fonts/sans-serif/" + font);
	}

	private static final List<List<File>> toFonts(File file) {
		return Lists.toLinkedList(Lists.toList(file));
	}

	private static List<List<File>> toFonts(URL basePath, String... fontFamilies) {
		List<List<File>> fonts = new ArrayList<>(fontFamilies.length);
		for (String fontFamily : fontFamilies) {
			File file = new File(FileUtil.toFile(basePath), fontFamily);
			File[] fontFiles = file.listFiles();
			fonts.add(Arrays.asList(fontFiles));
		}

		return fonts;
	}

}
