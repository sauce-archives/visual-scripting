package org.testobject.kernel.ocr;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.awt.Rectangle;
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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.testobject.kernel.imgproc.classifier.Classifier;
import org.testobject.kernel.ocr.AdditiveMask;
import org.testobject.kernel.ocr.freetype.FT2Library;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.file.FileUtil;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.BlobUtils;
import org.testobject.kernel.imgproc.blob.GraphBlobBuilder;
import org.testobject.kernel.imgproc.classifier.Classes;
import org.testobject.kernel.imgproc.classifier.Context;
import org.testobject.kernel.imgproc.classifier.GroupClassifier;
import org.testobject.kernel.imgproc.classifier.TextCharClassifier;
import org.testobject.kernel.imgproc.classifier.TextWordClassifier;
import org.testobject.kernel.imgproc.classifier.VisitingMutator;
import org.testobject.kernel.imgproc.plot.VisualizerUtil;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.kernel.ocr.MaskClusterer;
import org.testobject.kernel.ocr.TextRecognizer.Match;
import org.testobject.kernel.ocr.freetype.FreeTypeGrayscaleFontRenderer;
import org.testobject.kernel.ocr.FontRenderer;
import org.testobject.kernel.ocr.SlidingMaskTextRecognizer;

@Ignore
@RunWith(value = Parameterized.class)
public class SlidingMaskTextRecognizerRealImagesTest {

	private static final Log log = LogFactory
			.getLog(SlidingMaskTextRecognizerRealImagesTest.class);

	private static final boolean DEBUG = Debug.toDebugMode(true);

	private static final int[] FONT_SIZE = { 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25 };
	private static final int HISTOGRAM_N = 7;
	private static final double MAX_DIST = 1d;
	private static final int MAX_TOLERANCE_X = 2;

	private static final List<List<File>> sanfSerifFonts = getFonts(ClassLoader.getSystemResource("./android/4_0_3/fonts"), "sans-serif");

	private static final Object[][] komootImages = {
			{ "UBER", toKomootPath("about.png"), new Rectangle(10, 380, 90, 40) },
			{ "KOMOOT", toKomootPath("about.png"), new Rectangle(100, 380, 150, 40) },
			{ "DIE", toKomootPath("about.png"), new Rectangle(10, 600, 45, 40) },
			{ "ONLINE", toKomootPath("about.png"), new Rectangle(45, 600, 90, 40) },
			{ "TOURENPLANUNG", toKomootPath("about.png"), new Rectangle(145, 600, 210, 40) },
			{ "Meinkomoot", toKomootPath("home.png"), new Rectangle(50, 460, 150, 40) },
			{ "Regionen", toKomootPath("home.png"), new Rectangle(300, 460, 110, 40) },
			{ "Tourilanen", toKomootPath("home.png"), new Rectangle(50, 250, 150, 40) },
			{ "MeineFOsitiOn", toKomootPath("home.png"), new Rectangle(280, 250, 150, 40) } };

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(komootImages);
	}

	private static SlidingMaskTextRecognizer textRecognizer;

	private Rectangle bbox;
	private final String word;
	private final Image.Int image;


	public SlidingMaskTextRecognizerRealImagesTest(String word, String imagePath, Rectangle bbox)
			throws IOException {
		this.bbox = bbox;
		this.word = word;
		this.image = ImageUtil.read(imagePath);
	}

	@BeforeClass
	public static void setUp() throws IOException, Exception {
		FontRenderer renderer = new FreeTypeGrayscaleFontRenderer(160, 240, FT2Library.FT_LCD_FILTER_LIGHT);
		MaskClusterer maskClusterer = new MaskClusterer(renderer, HISTOGRAM_N, 1400f, 800f);
		List<AdditiveMask> additiveMasks = maskClusterer.generateMasksFast(sanfSerifFonts, FONT_SIZE);

		textRecognizer = new SlidingMaskTextRecognizer(additiveMasks, HISTOGRAM_N, MAX_DIST, MAX_TOLERANCE_X);
	}

	@Test
	public void testOCR() throws Throwable {

		// classification
		Blob[] blobs = new GraphBlobBuilder(image.w, image.h).build(image);
		Classifier[] before = { new GroupClassifier(), new TextCharClassifier(), new TextWordClassifier() };
		for (Classifier beforeClassifier : before) {
			new VisitingMutator(beforeClassifier).mutate(new Context(image, image), blobs[0]);
		}

		// debug
		if (DEBUG) {
			System.out.println("(input hierarchy)");
			BlobUtils.printMeta(blobs[0]);
		}

		
		// text
		
		List<Blob> texts = locate(blobs[0], bbox);
		assertTrue(texts.size() > 0);
		
		VisualizerUtil.show("Input", ImageUtil.crop(image, bbox));

		// recognize
		long start = System.currentTimeMillis();
		String result = "";
		for (Blob blob : sort(texts)) {
			VisualizerUtil.show(ImageUtil.crop(image, blob.bbox));
			Match<Blob> recognize = textRecognizer.recognize(image,
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

	private static String toKomootPath(String file) {
		return "android/4_0_3/screenshots/komoot/" + file;
	}
	
    private static List<Blob> locate(Blob root, Rectangle query) {
        List<Blob> result = new LinkedList<>();
        for(Blob child : root.children) {
            contained(child, query, result);
        }
        return result;
    }
    
    private static void contained(Blob blob, Rectangle rect, List<Blob> result) {
        if(rect.contains(blob.bbox) && blob.meta instanceof Classes.TextChar) {
        	result.add(blob);
        }

        for(Blob child : blob.children) {
            contained(child, rect, result);
        }
    }


}
