package org.testobject.kernel.ocr;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.testobject.commons.util.file.FileUtil;
import org.testobject.kernel.imgproc.classifier.Classes;
import org.testobject.kernel.imgproc.plot.VisualizerUtil;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.kernel.ocr.AdditiveMask;
import org.testobject.kernel.ocr.FontRenderer;
import org.testobject.kernel.ocr.MaskClusterer;
import org.testobject.kernel.ocr.SlidingMaskTextRecognizer;
import org.testobject.kernel.ocr.SlidingMaskTextRecognizer.Word;
import org.testobject.kernel.ocr.cairo.CairoFontRenderer;
import org.testobject.kernel.ocr.freetype.FT2Library;
import org.testobject.kernel.ocr.freetype.FreeTypeGrayscaleFontRenderer;

public class SlidingMaskTextRecognizerSyntheticTest {

	private static final String[] WORDS = { "a", "bier" };

	private static final List<List<File>> fonts = getFonts(ClassLoader.getSystemResource("./android/4_0_3/fonts"), "sans-serif");
	private static final int fontSize = 10;
	private static final int filter = FT2Library.FT_LCD_FILTER_LIGHT;
	private static final int histogramN = 7;
	private static final float maskThresholdUpperCase = 1400f;
	private static final float maskThresholdLowerCase = 800f;

	private static final double MAX_DIST = 0.9d; // 2d; // swing
	private static final int MAX_TOLERANCE_X = 2;

	private static SlidingMaskTextRecognizer textRecognizer;

	@BeforeClass
	public static void setUp() throws IOException, Exception {
		FontRenderer renderer = new FreeTypeGrayscaleFontRenderer(72, 96, filter);
		MaskClusterer maskClusterer = new MaskClusterer(renderer, histogramN, maskThresholdUpperCase, maskThresholdLowerCase);
		List<AdditiveMask> additiveMasks = maskClusterer.generateMasksFast(fonts, fontSize);

		textRecognizer = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X);
	}

	@Test
	@Ignore
	public void testOCR() throws Exception {
		for (String word : WORDS) {
			CairoFontRenderer cairo = new CairoFontRenderer(FileUtil.toFileFromSystem("android/4_0_3/fonts/sans-serif/DroidSans.ttf"),
					96, 72,
					fontSize);
			BufferedImage image = cairo.drawString(word);

			int fontSize = 30;

			VisualizerUtil.show("Cairo", image, fontSize);

			Classes.TextChar text = new Classes.TextChar(Collections.<Classes.TextChar.Candidate> emptyList());
			List<Word> classifyText = textRecognizer.classifyText(ImageUtil.toImage(image), image.getWidth(), Collections.<Integer> emptySet(), text);

			assertThat(classifyText.get(0).toString(), is(word));
		}
	}

	private static List<List<File>> getFonts(URL basePath, String... fontFamilies) {
		assert basePath != null;
		assert fontFamilies != null;
		List<List<File>> fonts = new ArrayList<>(fontFamilies.length);
		for (String fontFamily : fontFamilies) {
			File file = new File(FileUtil.toFile(basePath), fontFamily);
			file.exists();
			file.isDirectory();
			File[] fontFiles = file.listFiles();
			fonts.add(Arrays.asList(fontFiles));
		}

		return fonts;
	}

}
