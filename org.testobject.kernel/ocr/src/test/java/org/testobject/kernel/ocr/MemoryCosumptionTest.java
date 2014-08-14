package org.testobject.kernel.ocr;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.testobject.kernel.ocr.tesseract.TesseractCopyRectOCR;

public class MemoryCosumptionTest {

	public static void main(String[] args) throws IOException {
		while (true) {
			OCR ocr = new TesseractCopyRectOCR();
			ocr.getText(image(), 240, 1.0);
			ocr.getText(image(), 240, 1.0);
			ocr.getText(image(), 240, 1.0);
			ocr.getText(image(), 240, 1.0);
			ocr.getText(image(), 240, 1.0);
			ocr.getText(image(), 240, 1.0);
			ocr.getText(image(), 240, 1.0);
			ocr.getText(image(), 240, 1.0);
			ocr.getText(image(), 240, 1.0);
			ocr.getText(image(), 240, 1.0);
			ocr.getText(image(), 240, 1.0);
			ocr.getText(image(), 240, 1.0);
			ocr.getText(image(), 240, 1.0);
			ocr.getText(image(), 240, 1.0);
			ocr.getText(image(), 240, 1.0);
			//			ocr.close();
		}

	}

	private static BufferedImage image() throws IOException {
		InputStream stream = MemoryCosumptionTest.class.getResourceAsStream("apps.png");
		try {
			return ImageIO.read(stream);
		} finally {
			stream.close();
		}
	}

}
