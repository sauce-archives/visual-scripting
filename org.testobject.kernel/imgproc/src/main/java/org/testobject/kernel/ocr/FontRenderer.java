package org.testobject.kernel.ocr;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * 
 * @author enijkamp
 *
 */
public interface FontRenderer {

	BufferedImage drawChar(File font, float size, char chr);

}
