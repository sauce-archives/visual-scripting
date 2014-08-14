package org.testobject.kernel.imgproc.classifier;

import org.junit.Ignore;
import org.junit.Test;
import org.testobject.kernel.imgproc.classifier.Classes;
import org.testobject.kernel.imgproc.classifier.Classifier;
import org.testobject.kernel.imgproc.classifier.IconClassifier;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.classifier.Classes.Icon;
import org.testobject.kernel.mocks.Screen;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;
import static org.testobject.kernel.imgproc.classifier.TestUtils.classify;

/**
 * 
 * @author enijkamp
 *
 */
public class IconClassifierTest
{
	public static final boolean DEBUG = Debug.toDebugMode(false);

	private static final String[][] icons = {
			{ "cam.png", "camera" },
	        { "twitter.png", "bird" },
	        { "marker.png", "marker" },
	        { "write.png", "write" },
	        { "house.png", "house" },
	        { "pin.png", "pin" },
//	        { "MeinePositionTest.png", "Meine Position" },
//	        { "MeinKomootTest.png", "Mein Komoot" },
//	        { "RegionenTest.png", "Regionen" },
//	        { "TourPlanenTest.png", "Tour Planen" }
	};

	private static final double[] twitter_bird_points = { 29, 1, 27, 3, 27, 4, 26, 5, 26, 6, 25, 7, 25, 8, 24, 9, 24, 10, 23, 11, 21, 11, 19, 9,
	        18, 9, 17, 8, 16, 8, 15, 7, 14, 7, 13, 6, 11, 6, 10, 5, 9, 5, 8, 4, 6, 4, 6, 5, 10, 9, 9, 10, 8, 10, 8, 11, 12, 15, 11, 16, 10,
	        16, 10, 17, 11, 18, 12, 18, 13, 19, 14, 19, 15, 20, 14, 21, 14, 22, 15, 23, 16, 23, 17, 24, 16, 25, 15, 25, 13, 27, 3, 27, 2,
	        26, 1, 26, 4, 29, 5, 29, 7, 31, 8, 31, 9, 32, 9, 33, 28, 33, 29, 32, 30, 32, 32, 30, 33, 30, 40, 23, 40, 22, 41, 21, 41, 20,
	        43, 18, 47, 18, 47, 16, 45, 16, 44, 15, 45, 14, 46, 14, 47, 13, 47, 12, 46, 12, 45, 13, 43, 13, 42, 12, 42, 9, 37, 4, 36, 4,
	        35, 3, 35, 1 };

	@Test @Ignore
	public void testIconSynthetic() throws Throwable
	{
		// screen
		Image.Int screen = Screen.screen(400, 800).icon(10, 10, twitter_bird_points).build();

		// classify
		Classifier[] before = {};
		TestUtils.Result<Icon> result = TestUtils.classify(screen, before, new IconClassifier(), DEBUG);

		// assert
		assertThat(result.widgets.size(), is(1));
		assertThat(result.widgets.peek().cls, is(Classes.Icon.class));
	}

	@Test
	public void testIconPositives() throws Throwable
	{
		// classify
		Classifier[] before = {};

		// positives
		for (String[] test : icons) {
			// debug
			if (DEBUG) {
				System.out.println(test[0]);
			}

			// classify
			TestUtils.Result<Icon> result = TestUtils.classify(ImageUtil.read("android/4_0_3/classifier/icon/positives/" + test[0]), before, new IconClassifier(), DEBUG);

			// assert			
			assertThat(result.widgets.isEmpty(), is(false));
			assertThat(result.widgets.peek().cls, is(Classes.Icon.class));
			Classes.Icon icon = (Classes.Icon) result.widgets.peek().cls;
			assertThat(icon.name, is(test[1]));
		}
	}
}
