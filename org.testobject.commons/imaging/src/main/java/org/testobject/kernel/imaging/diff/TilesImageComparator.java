package org.testobject.kernel.imaging.diff;

import java.util.ArrayList;
import java.util.List;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.image.Image;

/**
 * Compares two given images and returns a set of bounding boxes which identify the delta regions.
 * 
 * @author enijkamp
 *
 */
public class TilesImageComparator {
	
	public static List<Rectangle.Int> compare(Image.Int image1, Image.Int image2) {
		
		final int size = 5; // 2^5, i.e. tiles of size 32x32
		
		List<Rectangle.Int> boxes = new ArrayList<>();
		
		final int w = image1.w;
		final int h = image1.h;
		
		final int n_w = (w >> size) + 1;
		final int n_h = (h >> size) + 1;
		
		boolean[][] cellChanged = new boolean[n_w][n_h];
		int index = 0;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (image1.pixels[index] != image2.pixels[index]) {
					cellChanged[x >> size][y >> size] = true;
				}
				index++;
			}
		}
			
		for (int y = 0; y < n_h; y++) {
			for (int x = 0; x < n_w; x++) {
				if(cellChanged[x][y]) {
					
					int g_x = Math.min(w, x << size);
					int g_y = Math.min(h, y << size);
					int g_w = Math.min(w - g_x, 2 << (size - 1));
					int g_h = Math.min(h - g_y, 2 << (size - 1));
					
					boxes.add(new Rectangle.Int(g_x, g_y, g_w, g_h));
				}
			}
		}
		
		return boxes;
	}

}
