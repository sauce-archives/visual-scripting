package org.testobject.kernel.ocr;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.testobject.commons.util.distances.StringDistances;
import org.testobject.kernel.ocr.OCR.Result;
import org.testobject.kernel.ocr.TestData.TextMatch;
import org.testobject.kernel.ocr.TestData.TextMatches;
import org.testobject.kernel.ocr.tesseract.OptimizedTesseractOCR;

import samples.ResourceResolver;

@RunWith(Parameterized.class)
public class SuccessfullOCRTests {

	private static final Log log = LogFactory.getLog(SuccessfullOCRTests.class);

	private static final boolean DEBUG = java.lang.management.ManagementFactory.getRuntimeMXBean().
			getInputArguments().toString().indexOf("jdwp") >= 0;

	private OCR ocr;

	private String path;
	private TextMatch textMatch;
	private int dpi;

	public SuccessfullOCRTests(String path, TextMatch textMatch, int dpi) {
		this.path = path;
		this.textMatch = textMatch;
		this.dpi = dpi;
	}

	@Parameters
	public static List<Object[]> data() {
		List<Object[]> data = new LinkedList<>();
		TextMatches goodOcrMatches = TestData.getGoodOcrMatches();
		Set<Entry<String, List<TextMatch>>> testMatchesMap = goodOcrMatches.getMatches().entrySet();
		for (Entry<String, List<TextMatch>> entry : testMatchesMap) {
			String path = entry.getKey();
			List<TextMatch> testMatches = entry.getValue();
			for (TextMatch textMatch : testMatches) {
				data.add(new Object[] { path, textMatch, toDPI(path) });
			}
		}
		return data;
	}

	private static int toDPI(String path) {
		if (path.endsWith("480x800.png")) {
			return 240;
		} else {
			return 320;
		}
	}

	@Before
	public void setUp() {
		ocr = new OptimizedTesseractOCR();
	}

	@Test
	public void testSuccessful() throws IOException {
		debug();

		BufferedImage image = ImageIO.read(ResourceResolver.getResource(path));
		List<Result> results = ocr.getText(image, textMatch.getRegion(), dpi, 1.0);
		assertFalse(results.isEmpty());
		String text = results.get(0).getText();
		log.info("stringDistance( " + textMatch.getText() + " , " + text + ") = "
				+ StringDistances.getNormalizedDistance(textMatch.getText().trim(), text.trim()));
		assertThat(text, is(textMatch.getText()));
	}

	private void debug() throws IOException {
		System.out.println(path + ": " + textMatch);
		if (DEBUG) {
			final OCRTestDataGenerator gui = new OCRTestDataGenerator(path);
			gui.setRegion(textMatch.getRegion());

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					gui.display();
				}
			});

			System.in.read();
		}
	}

}
