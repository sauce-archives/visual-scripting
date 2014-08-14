package org.testobject.kernel.imaging.filters;

import org.testobject.commons.util.image.Image;

/**
 * 
 * @author enijkamp
 *
 */
public class LookupTableFilter {
	
	public static void apply(Image.Byte image, int[] lut) {
		for(int i = 0; i < image.pixels.length; i++) {
			image.pixels[i] = (byte) lut[image.pixels[i] & 0xff];
		}
	}

}
