package org.testobject.kernel.classification.contours;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.testobject.kernel.imgproc.blob.TestUtils.init;

import java.util.List;

import org.junit.Test;
import org.testobject.kernel.classification.contours.Contours;
import org.testobject.kernel.imgproc.blob.ArrayRaster;
import org.testobject.commons.math.algebra.Point;

/**
 * 
 * @author enijkamp
 *
 */
public class ContoursTest {
	
	@Test
	public void testLine() {
		int[][] pixels = { { 0, 0, 0, 0, 0  }, { 0, 1, 1, 1, 0 }, { 0, 0, 0, 0, 0 } };
		double[][] should = { { 2.0, 2.0 }, { 3.0, 2.0 }, { 4.0, 2.0 },  { 3.0, 2.0 }};
		
		ArrayRaster raster = init(pixels);
		List<Point.Double> is = Contours.contourTrace(raster);
		
		assertThat(is.size(), is(should.length));
		for(int i = 0; i < is.size(); i++) {
			assertThat(is.get(i).x, is(should[i][0]));
			assertThat(is.get(i).y, is(should[i][1]));
		}
	}

}
