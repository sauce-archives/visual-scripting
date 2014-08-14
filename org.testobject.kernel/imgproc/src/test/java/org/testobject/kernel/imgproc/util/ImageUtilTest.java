package org.testobject.kernel.imgproc.util;

import java.awt.image.BufferedImage;

import org.junit.Ignore;
import org.junit.Test;
import org.testobject.kernel.imgproc.plot.VisualizerUtil;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.util.ImageUtil;

/**
 * 
 * @author enijkamp
 * 
 */
public class ImageUtilTest {

	@Test
	@Ignore
	public void toBufferedImage() {

		Image.Int image = new Image.Int(10, 10);
		image.pixels[10 * 2 + 4] = (255 << 24) | (255 << 16) | (255 << 8) | (255 << 0);
		image.pixels[10 * 2 + 5] = (255 << 24) | (255 << 16) | (255 << 8) | (255 << 0);
		image.pixels[10 * 2 + 6] = (255 << 24) | (255 << 16) | (255 << 8) | (255 << 0);
		BufferedImage bufferedImage = ImageUtil.toBufferedImage(image);
		VisualizerUtil.show("alpha", bufferedImage, 4f);

		System.out.println();

	}

}
