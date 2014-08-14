package org.testobject.kernel.ocr;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.testobject.kernel.imgproc.classifier.Classes;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.kernel.ocr.*;
import org.testobject.kernel.ocr.cairo.CairoFontRenderer;
import org.testobject.kernel.ocr.freetype.FT2Library;
import org.testobject.kernel.ocr.freetype.FreeTypeColorFontRenderer;
import org.testobject.commons.util.collections.Sets;
import org.testobject.kernel.ocr.SlidingMaskTextRecognizer.Word;

// FIXME use old clusterer code with specialized cut masks for 'i' etc. (en)
@Ignore
@RunWith(value = Parameterized.class)
public class SlidingMaskTextRecognizerCairoTest {

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] { { "a" }, { "bier" }, { "erik" } };
		return Arrays.asList(data);
	}

	private static final String[] FONT_FAMILIES = { "sans-serif" }; // , "serif" };

	private static final String PATH_400_FONTS = "../../../org.testobject.experiments.data/ocr/fonts/family";
	private static final String PATH_UBUNTU_FONTS = "../../../org.testobject.experiments.data/ocr/fonts/system/ubuntu";

	private static final int fontSize = 10;
	private static final int baselineDpi = 96, targetDpi = 72; // parameters swapped on purpose (en)
	private static final int filter = FT2Library.FT_LCD_FILTER_LIGHT;

	private static final int histogramN = 7;

	private static final float maskThresholdUpperCase = 1400f;
	private static final float maskThresholdLowerCase = 800f;

	private static final double MAX_DIST = 0.9d; // 2d; // swing
	private static final int MAX_TOLERANCE_X = 2;

	private static final Set<java.lang.Character> characters = Sets.fromArray(org.testobject.kernel.ocr.Character.getAllChars());

	private static SlidingMaskTextRecognizer textRecognizer;

	private final String word;

	public SlidingMaskTextRecognizerCairoTest(String word) {
		this.word = word;
	}

	@BeforeClass
	public static void setUp() throws IOException, Exception {
		List<List<File>> fonts = getAllFonts400();
		FontRenderer renderer = new FreeTypeColorFontRenderer(baselineDpi, targetDpi, filter);
		MaskClusterer maskClusterer = new MaskClusterer(renderer, histogramN, maskThresholdUpperCase,
				maskThresholdLowerCase);
		List<AdditiveMask> additiveMasks = maskClusterer.generateMasksFast(fonts, fontSize);

		textRecognizer = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X);
	}

	@Test
	public void testOCR() throws Exception {
		// test
		// final String[] texts = { "bier" }; // {"erik", "Erik", "Beruf", "beruf", "Bier", "Andreas", "hannes" };
		// final String[] texts = { "flasche", "vw", "volk", "korn", "mama", "Avu", "lalu", "urgh", "test" };
		// final String[] texts = { "affe", "waffel", "wurf" };
		// final String[] texts = { "pussy", "cat", "girl", "nice", "fast", "slow", "blah" };

		// render
		File font = new File(PATH_UBUNTU_FONTS + "/sans-serif/Arial.ttf");
		CairoFontRenderer cairo = new CairoFontRenderer(font, fontSize, baselineDpi, targetDpi);
		BufferedImage image = cairo.drawString(word);

		// classify
		Classes.TextChar text = new Classes.TextChar(Collections.<Classes.TextChar.Candidate> emptyList());
		List<Word> classifyText = textRecognizer.classifyText(ImageUtil.toImage(image), image.getWidth(), Collections.<Integer> emptySet(), text);

		// assert
		assertThat(classifyText.get(0).toString(), is(word));
	}

	private static List<List<File>> getAllFonts400() {
		List<List<File>> fonts = new LinkedList<List<File>>();
		for (String fontFamily : FONT_FAMILIES) {
			fonts.add(getFonts(PATH_400_FONTS, fontFamily));
		}
		return fonts;
	}

	private static List<File> getFonts(String basePath, String fontFamily) {
		File path = new File(basePath + File.separator + fontFamily);
		File[] fontFiles = path.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile();
			}
		});
		return Arrays.asList(fontFiles);
	}
}
