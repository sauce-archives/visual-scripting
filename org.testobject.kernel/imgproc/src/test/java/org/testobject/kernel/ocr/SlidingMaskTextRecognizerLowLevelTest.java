package org.testobject.kernel.ocr;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.lang.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.testobject.kernel.imgproc.classifier.Classifier;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.file.FileUtil;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.GraphBlobBuilder;
import org.testobject.kernel.imgproc.classifier.Context;
import org.testobject.kernel.imgproc.classifier.GroupClassifier;
import org.testobject.kernel.imgproc.classifier.TextCharClassifier;
import org.testobject.kernel.imgproc.classifier.TextWordClassifier;
import org.testobject.kernel.imgproc.classifier.VisitingMutator;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.kernel.ocr.*;
import org.testobject.kernel.ocr.TextRecognizer.Match;
import org.testobject.kernel.ocr.freetype.FT2Library;
import org.testobject.kernel.ocr.freetype.FreeTypeGrayscaleFontRenderer;

public class SlidingMaskTextRecognizerLowLevelTest {

	private static final boolean DEBUG = Debug.toDebugMode(false);

	private static final File[] SANS_SERIF_FONTS = getFonts(ClassLoader.getSystemResource("./android/4_0_3/fonts"), "sans-serif");

	private static final FreeTypeGrayscaleFontRenderer fontRenderer = new FreeTypeGrayscaleFontRenderer(160, 240,
			FT2Library.FT_LCD_FILTER_LIGHT);

	private static final int histogramN = 7;
	private static final double MAX_DIST = 1d;
	private static final int MAX_TOLERANCE_X = 2;
	private static final int[] fontSize = { 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25 };

	private static List<AdditiveMask> additiveMasks = generateMasks(histogramN, org.testobject.kernel.ocr.Character.getAllChars(),
			fontSize, SANS_SERIF_FONTS,
			fontRenderer);

	@Test
	public void testCAFromCancel() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/twitter/Cancel.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[3];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image,
				Lists.toList(blob));

		assertThat(match.toString(), is("Ca"));
	}

	@Test
	public void testEFromCancel() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/twitter/Cancel.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[4];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image,
				Lists.toList(blob));

		assertThat(match.toString(), is("e"));
	}

	@Test
	public void testNCFromCancel() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/twitter/Cancel.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[5];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("nc"));
	}

	@Test
	public void testLFromCancel() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/twitter/Cancel.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[2];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("l"));
	}

	@Test
	public void test3_1From1233() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/system/1233.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[2];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("3"));
	}

	@Test
	public void test3_2From1233() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/system/1233.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[3];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("3"));
	}

	@Test
	public void test1From1233() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/system/1233.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[4];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("1"));
	}

	@Test
	public void test2From1233() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/system/1233.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[1];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("2"));
	}

	@Test
	public void test6From136() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/twitter/136.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[3];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("6"));
	}

	@Test
	public void test3From136() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/twitter/136.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[2];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("3"));
	}

	@Test
	public void test1From136() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/twitter/136.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[4];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("1"));
	}

	@Test
	public void testT_1FromTest() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/twitter/Test.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[2];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("T"));
	}

	@Test
	public void testT_2FromTest() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/twitter/Test.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[3];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("t"));
	}

	@Test
	public void testEFromTest() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/twitter/Test.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[4];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("e"));
	}

	@Test
	public void testSFromTest() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/twitter/Test.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[5];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("s"));
	}

	@Test
	public void testTweetFromTweet() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/twitter/Tweet.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[3];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("Tweet"));
	}
	
	@Ignore
	@Test
	public void testMfromMeinKomoot() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/komoot/Mein Komoot.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[7];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("M"));
	}
	
	@Ignore
	@Test
	public void testEfromMeinKomoot() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/komoot/Mein Komoot.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[5];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("e"));
	}
	
	@Ignore
	@Test
	public void testIfromMeinKomoot() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/komoot/Mein Komoot.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[3];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("i"));
	}
	
	@Ignore
	@Test
	public void testNfromMeinKomoot() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/komoot/Mein Komoot.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[4];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("n"));
	}
	
	@Ignore
	@Test
	public void testo1fromMeinKomoot() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/komoot/Mein Komoot.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[8];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("o"));
	}
	
	@Ignore
	@Test
	public void testM1fromMeinKomoot() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/classifier/text/positives/komoot/Mein Komoot.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[6];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("m"));
	}
	
	@Ignore
	@Test
	public void testMfromMeinePosition() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/screenshots/komoot/home.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[37];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("M"));
	}
	
	@Ignore
	@Test
	public void testE1fromMeinePosition() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/screenshots/komoot/home.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[34];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("e"));
	}
	
	@Ignore
	@Test
	public void testI1fromMeinePosition() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/screenshots/komoot/home.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[26];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("i"));
	}
	
	@Ignore
	@Test
	public void testNfromMeinePosition() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/screenshots/komoot/home.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[27];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("n"));
	}
	
	@Ignore
	@Test
	public void testE2fromMeinePosition() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/screenshots/komoot/home.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[35];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("e"));
	}
	
	@Ignore
	@Test
	public void testPfromMeinePosition() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/screenshots/komoot/home.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[21];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("F"));
	}
	
	@Ignore
	@Test
	public void testOfromMeinePosition() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/screenshots/komoot/home.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[36];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("O"));
	}
	
	@Ignore
	@Test
	public void testSfromMeinePosition() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/screenshots/komoot/home.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[39];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("s"));
	}
	
	@Ignore
	@Test
	public void testI2fromMeinePosition() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/screenshots/komoot/home.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[28];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("i"));
	}
	
	@Ignore
	@Test
	public void testTfromMeinePosition() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/screenshots/komoot/home.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[41];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("t"));
	}
	
	@Ignore
	@Test
	public void testI3fromMeinePosition() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/screenshots/komoot/home.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[29];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("i"));
	}
	
	@Ignore
	@Test
	public void testO2fromMeinePosition() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/screenshots/komoot/home.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[40];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("O"));
	}
	
	@Ignore
	@Test
	public void testN2fromMeinePosition() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/screenshots/komoot/home.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[30];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("n"));
	}
	
	@Ignore
	@Test
	public void testUfromUber() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/screenshots/komoot/about.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[19];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("U"));
	}
	
	@Ignore
	@Test
	public void testBfromUber() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/screenshots/komoot/about.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[28];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("B"));
	}
	
	@Ignore
	@Test
	public void testEfromUber() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/screenshots/komoot/about.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[22];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("E"));
	}
	
	@Ignore
	@Test
	public void testRfromUber() throws Throwable {
		Image.Int image = ImageUtil.read("android/4_0_3/screenshots/komoot/about.png");

		Blob blob = classifyText(image, fontSize, SANS_SERIF_FONTS)[30];

		Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image
				, Lists.toList(blob));

		assertThat(match.toString(), is("R"));
	}
	

	private static List<AdditiveMask> generateMasks(final int histogramN, char[] chars, int[] fontSize, File[] font,
			FontRenderer fontRenderer) {
		List<List<File>> fonts = Collections.singletonList(Arrays.asList(font));
		List<AdditiveMask> additiveMasks = new MaskClusterer(fontRenderer, histogramN, 1400f, 800f)
				.generateMasksFast(fonts, fontSize, chars);
		return additiveMasks;
	}

	private Blob[] classifyText(Image.Int image, final int[] fontSize, final File[] font) throws Throwable {
		Blob[] blobs = new GraphBlobBuilder(image.w, image.h).build(image);
		// classification
		Classifier[] before = { new GroupClassifier(), new TextCharClassifier(), new TextWordClassifier() };
		for (Classifier beforeClassifier : before) {
			new VisitingMutator(beforeClassifier).mutate(new Context(image, image), blobs[0]);
		}

		return blobs;
	}

	private static File[] getFonts(URL basePath, String fontFamily) {
		File file = new File(FileUtil.toFile(basePath), fontFamily);
		File[] fontFiles = file.listFiles();

		return fontFiles;
	}
}
