package org.testobject.kernel.ocr;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.testobject.kernel.imgproc.classifier.Classes;
import org.testobject.kernel.imgproc.classifier.Classifier;
import org.testobject.kernel.imgproc.classifier.Context;
import org.testobject.kernel.imgproc.blob.BlobUtils;
import org.testobject.kernel.imgproc.classifier.VisitingMutator;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.kernel.ocr.*;
import org.testobject.kernel.ocr.freetype.FT2Library;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.file.FileUtil;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.GraphBlobBuilder;
import org.testobject.kernel.imgproc.classifier.GroupClassifier;
import org.testobject.kernel.imgproc.classifier.TextCharClassifier;
import org.testobject.kernel.imgproc.classifier.TextWordClassifier;
import org.testobject.kernel.ocr.freetype.FreeTypeGrayscaleFontRenderer;

@RunWith(value = Parameterized.class)
public class SlidingMaskTextRecognizerImagesTest {

	private static final Log log = LogFactory
			.getLog(SlidingMaskTextRecognizerImagesTest.class);

	private static final boolean DEBUG = Debug.toDebugMode(true);

	private static final int[] FONT_SIZE = { 12, 13, 14, 15, 16 };
	private static final int HISTOGRAM_N = 7;
	private static final double MAX_DIST = 1d;
	private static final int MAX_TOLERANCE_X = 2;

	private static final List<List<File>> sanfSerifFonts = getFonts(
			ClassLoader.getSystemResource("./android/4_0_3/fonts"),
			"sans-serif");

	private static final Object[][] systemImages = { { "1233",
			toSystemPath("1233.png") }, };

	private static final Object[][] komootImages = {
			{ "Meinkomoot", toKomootPath("Mein Komoot.png") },
			{ "Regionen", toKomootPath("Regionen.png") },
			{ "Tourpianen", toKomootPath("Tour Planen.png") },
			{ "iMeinePostion", toKomootPath("Meine Position.png") } };

	private static final Object[][] twitterImages = {
			{ "136", toTwitterPath("136.png") },
			{ "Cancel", toTwitterPath("Cancel.png") },
			{ "Test", toTwitterPath("Test.png") },
			{ "20Apr", toTwitterPath("20Apr.png") },
			{ "Discard", toTwitterPath("Discard.png") },
			{ "OkDerRaseniststellenweise", toTwitterPath("line1.png") },
			// { "hoheralsderHundIchsollte", toTwitterPath("line2.png") },
			{ "mahen", toTwitterPath("line3.png") },

			{ "Tweet", toTwitterPath("Tweet.png") } };

	// FIXME dont work (en)
	private static final Object[][] googleDocsImages = {
			{ "summar", toDocsPath("Summary.png") },
			{ "wachstumsmarkt", toDocsPath("Wachstumsmarkt.png") },
			{ "mob.?le", toDocsPath("mobile.png") },
			{ "Rap.?d", toDocsPath("Rapid.png") },
			{ "TestOb.?ect", toDocsPath("TestObject.png") },
			{ "entw.?ckelt", toDocsPath("entwickelt.png") },
			{ "L.?sungen", toDocsPath("Lösungen.png") } };

	// FIXME dont work (en)
	private static final Object[][] fontRenderingAppImages = { { "Hello",
			"android/4_0_3/ocr/font-rendering-app/Hello.png" } };

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(twitterImages);
	}

	private static String toSystemPath(String file) {
		return "android/4_0_3/classifier/text/positives/system/" + file;
	}

	private static String toTwitterPath(String file) {
		return "android/4_0_3/classifier/text/positives/twitter/" + file;
	}

	private static String toDocsPath(String file) {
		return "android/4_0_3/classifier/text/positives/gdocs/" + file;
	}

	private static String toKomootPath(String file) {
		return "android/4_0_3/classifier/text/positives/komoot/" + file;
	}

	private static SlidingMaskTextRecognizer textRecognizer;

	private final String word;
	private final Image.Int image;

	public SlidingMaskTextRecognizerImagesTest(String word, String imagePath)
			throws IOException {
		this.word = word;
		this.image = ImageUtil.read(imagePath);
	}

	@BeforeClass
	public static void setUp() throws IOException, Exception {
		FontRenderer renderer = new FreeTypeGrayscaleFontRenderer(160, 240,
				FT2Library.FT_LCD_FILTER_LIGHT);
		MaskClusterer maskClusterer = new MaskClusterer(renderer, HISTOGRAM_N,
				1400f, 800f);
		List<AdditiveMask> additiveMasks = maskClusterer.generateMasksFast(
				sanfSerifFonts, FONT_SIZE);

		textRecognizer = new SlidingMaskTextRecognizer(additiveMasks,
				HISTOGRAM_N, MAX_DIST, MAX_TOLERANCE_X);
	}

	@Test
	public void testOCR() throws Throwable {

		// classification
		Blob[] blobs = new GraphBlobBuilder(image.w, image.h).build(image);
		Classifier[] before = { new GroupClassifier(),
				new TextCharClassifier(), new TextWordClassifier() };
		for (Classifier beforeClassifier : before) {
			new VisitingMutator(beforeClassifier).mutate(new Context(image,
					image), blobs[0]);
		}

		// debug
		if (DEBUG) {
			System.out.println("(input hierarchy)");
			BlobUtils.printMeta(blobs[0]);
		}

		// text
		List<Blob> texts = new LinkedList<>();
		for (Blob blob : blobs) {
			if (blob.meta instanceof Classes.TextChar) {
				texts.add(blob);
			}
		}
		assertTrue(texts.size() > 0);

		// recognize
		long start = System.currentTimeMillis();
		String result = "";
		for (Blob blob : sort(texts)) {
			TextRecognizer.Match<Blob> recognize = textRecognizer.recognize(image,
					Lists.toLinkedList(blob));
			result += recognize.toString();
		}
		log.info("'" + word + "' took " + (System.currentTimeMillis() - start)
				+ "ms");

		// assert
		assertThat(result.toString(), matches(word));
	}

	private static List<Blob> sort(Collection<Blob> blobs) {
		List<Blob> sorted = Lists.newLinkedList(blobs);
		Collections.sort(sorted, new Comparator<Blob>() {
			@Override
			public int compare(Blob b1, Blob b2) {
				return Integer.compare(b1.bbox.x, b2.bbox.x);
			}
		});
		return sorted;
	}

	private static List<List<File>> getFonts(URL basePath,
			String... fontFamilies) {
		List<List<File>> fonts = new ArrayList<>(fontFamilies.length);
		for (String fontFamily : fontFamilies) {
			File file = new File(FileUtil.toFile(basePath), fontFamily);
			File[] fontFiles = file.listFiles();
			fonts.add(Arrays.asList(fontFiles));
		}

		return fonts;
	}

	private static RegexMatcher matches(String regex) {
		return new RegexMatcher(regex);
	}

	private static class RegexMatcher extends BaseMatcher<String> {
		private final String regex;

		public RegexMatcher(String regex) {
			this.regex = regex;
		}

		public boolean matches(Object o) {
			return ((String) o).matches(regex);

		}

		public void describeTo(Description description) {
			description.appendText(regex);
		}

	}

}
