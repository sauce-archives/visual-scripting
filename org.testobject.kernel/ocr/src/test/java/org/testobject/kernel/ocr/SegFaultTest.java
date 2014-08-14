package org.testobject.kernel.ocr;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.kernel.ocr.tesseract.TesseractCopyRectOCR;

/**
 * 
 * @author enijkamp
 *
 */
public class SegFaultTest {

	private static final BufferedImage IMAGE_363_603 = image_363_603();
	private static final BufferedImage IMAGE_1440_2400 = image_1440_2400();

	public static void main(String[] args) throws IOException {
		
		final OCR ocr = new TesseractCopyRectOCR();

		for(int i = 0; i < 4; i++) {
			Runnable run = new Runnable() {
				@Override
				public void run() {
					while (true) {
						ocr.getText(IMAGE_1440_2400, new Rectangle.Int(0, 0, 1440, 2000), 240, 1.0);
						ocr.getText(IMAGE_363_603, new Rectangle.Int(10, 50, 300, 500), 240, 1.0);
						ocr.getText(IMAGE_363_603, 240, 1.0);
						
						ocr.getText(IMAGE_363_603, new Rectangle.Int(10, 50, 300, 500), 240, 1.0);
						ocr.getText(IMAGE_1440_2400, new Rectangle.Int(0, 0, 1440, 2000), 240, 1.0);
						ocr.getText(IMAGE_363_603, 240, 1.0);
						
						ocr.getText(IMAGE_1440_2400, new Rectangle.Int(0, 0, 1440, 2000), 240, 1.0);
						ocr.getText(IMAGE_363_603, 240, 1.0);
						ocr.getText(IMAGE_363_603, new Rectangle.Int(10, 50, 300, 500), 240, 1.0);
						
						ocr.getText(IMAGE_363_603, 240, 1.0);
						ocr.getText(IMAGE_363_603, new Rectangle.Int(10, 50, 300, 500), 240, 1.0);
						ocr.getText(IMAGE_1440_2400, new Rectangle.Int(0, 0, 1440, 2000), 240, 1.0);
					}
				}
			};
			new Thread(run).start();
		}

		System.in.read();
	}

	private static BufferedImage image_1440_2400() {
		try(InputStream stream = SegFaultOptimizedTest.class.getResourceAsStream("apps.png")) {
			return ImageIO.read(stream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static BufferedImage image_363_603() {
		try(InputStream stream = SegFaultOptimizedTest.class.getResourceAsStream("ocr1.png")) {
			return ImageIO.read(stream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}