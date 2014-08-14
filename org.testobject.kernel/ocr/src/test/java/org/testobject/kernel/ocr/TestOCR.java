package org.testobject.kernel.ocr;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testobject.kernel.ocr.OCR.Result;
import org.testobject.kernel.ocr.TestData.TextMatch;
import org.testobject.kernel.ocr.tesseract.TesseractCopyRectOCR;

import samples.ResourceResolver;

public class TestOCR {

	private static OCRTestDataGenerator ocrTestDataGenerator;
	private static TesseractCopyRectOCR tesseractOCR;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		tesseractOCR = new TesseractCopyRectOCR();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {}

	@Test
	public void allImages() throws IOException {

		ocrTestDataGenerator = new OCRTestDataGenerator("search/calendar/4.1.2/1/480x800.png");

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ocrTestDataGenerator.display();
			}
		});

		for (String path : TestData.allImages()) {
			System.out.println(path);
			ocrTestDataGenerator.setImage(path);
			System.in.read();
		}

	}

	@Test
	public void goodMatches() throws IOException {

		Map<String, List<TextMatch>> matches = TestData.getGoodOcrMatches().getMatches();

		for (String path : matches.keySet()) {
			List<TextMatch> list = matches.get(path);
			for (TextMatch textMatch : list) {
				BufferedImage image = ImageIO.read(ResourceResolver.getResource(path));
				List<Result> results = tesseractOCR.getText(image, textMatch.getRegion(), toDPI(path), 1.0);
				String text = results.get(0).getText();
				System.out.println(text);
				assertThat(text, is(textMatch.getText()));
			}
		}

	}

	@Test
	public void badMatches() throws IOException {

		Map<String, List<TextMatch>> matches = TestData.getBadOcrMatches().getMatches();

		for (String path : matches.keySet()) {
			List<TextMatch> list = matches.get(path);
			for (TextMatch textMatch : list) {
				BufferedImage image = ImageIO.read(ResourceResolver.getResource(path));
				List<Result> results = tesseractOCR.getText(image, textMatch.getRegion(), toDPI(path), 1.0);
				String text = results.get(0).getText();
				System.out.println("correct: '" + textMatch.getText() + "' found: '" + text + "'");
				assertThat(text, is(not(textMatch.getText())));
			}
		}
	}

	private int toDPI(String path) {
		if (path.endsWith("480x800.png")) {
			return 240;
		} else {
			return 320;
		}
	}

}
