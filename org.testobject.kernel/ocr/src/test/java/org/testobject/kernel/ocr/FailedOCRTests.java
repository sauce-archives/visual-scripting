package org.testobject.kernel.ocr;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.testobject.kernel.ocr.OCR.Result;
import org.testobject.kernel.ocr.TestData.TextMatch;
import org.testobject.kernel.ocr.TestData.TextMatches;
import org.testobject.kernel.ocr.tesseract.OptimizedTesseractOCR;

import samples.ResourceResolver;

@RunWith(Parameterized.class)
public class FailedOCRTests {

	private static final boolean DEBUG = isDebug();

	private static boolean isDebug() {
		return ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("jdwp") >= 0;
	}

	private OCR ocr;

	private String path;
	private TextMatch textMatch;
	private int dpi;

	public FailedOCRTests(String path, TextMatch textMatch, int dpi) {
		this.path = path;
		this.textMatch = textMatch;
		this.dpi = dpi;
	}

	@Parameters
	public static List<Object[]> data() {
		List<Object[]> data = new LinkedList<>();
		TextMatches goodOcrMatches = TestData.getBadOcrMatches();
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

	@Before
	public void setUp() {
		ocr = new OptimizedTesseractOCR();
	}

	@Test
	public void testFailed() throws IOException {
		debug();
		
		BufferedImage image = ImageIO.read(ResourceResolver.getResource(path));

		List<Result> results = ocr.getText(image, textMatch.getRegion(), dpi, 1.0);
		String textResult = results.get(0).getText();
		String textOrigin = textMatch.getText();
		
		System.out.println("print 'similarity(" + textOrigin + "," + textResult + "):' + str(strSimilarity.howConfusableAre_chkSym('" + textOrigin + "','" + textResult + "'))");
//		System.out.println("stringDistance('" + textMatch.getText() + "','" + text.trim() +"') = " + StringDistances.getNormalizedDistance(textMatch.getText().trim(), text.trim()));
		assertThat(textOrigin, is(textResult));
	}
	
	private static int toDPI(String path) {
		if (path.endsWith("480x800.png")) {
			return 240;
		} else {
			return 320;
		}
	}

	private void debug() throws IOException {
		System.out.println(path + ": " + textMatch);
		if (DEBUG) {
			final OCRTestDataGenerator gui = new OCRTestDataGenerator();
			gui.setImage(path);
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
