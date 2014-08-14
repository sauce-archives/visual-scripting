package org.testobject.kernel.imaging.color.contrast;

import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.kernel.imaging.filters.LookupTableFilter;
import org.testobject.commons.math.statistics.Histogram;

/**
 * 
 * @author enijkamp
 *
 */
public class Contrast {
	
	public static Image.Byte autoContrast(Image.Byte source) {
		
		Histogram.Byte hist = Histogram.Byte.Builder.compute(source.pixels);
		Image.Byte target = ImageUtil.clone(source);
		
		equalize(target, hist);
		
		return target;
	}
	
	private static void equalize(Image.Byte image, Histogram.Byte hist) {
		
		final int max = 255;
		final int range = 255;
		
		double scale = 0d;
		{
			double sum = getWeightedValue(hist.bins, 0);
			for (int i=1; i < max; i++) {
				sum += 2 * getWeightedValue(hist.bins, i);
			}
			sum += getWeightedValue(hist.bins, max);
			scale = range/sum;
		}
		
		int[] lut = new int[range+1];
		{
			double sum = getWeightedValue(hist.bins, 0);
			for (int i=1; i<max; i++) {
				double delta = getWeightedValue(hist.bins, i);
				sum += delta;
				lut[i] = (int)Math.round(sum*scale);
				sum += delta;
			}
			lut[max] = max;
		}
		
		LookupTableFilter.apply(image, lut);
	}
	

	
	private static double getWeightedValue(int[] histogram, int i) {
		return Math.sqrt((double)(histogram[i]));
	}
}
