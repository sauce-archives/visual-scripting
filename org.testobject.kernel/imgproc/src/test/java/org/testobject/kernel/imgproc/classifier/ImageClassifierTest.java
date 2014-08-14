package org.testobject.kernel.imgproc.classifier;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.testobject.kernel.imgproc.classifier.TestUtils.classify;

import org.junit.Test;
import org.testobject.kernel.imgproc.classifier.Classes;
import org.testobject.kernel.imgproc.classifier.Classifier;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.commons.util.config.Debug;
import org.testobject.kernel.imgproc.classifier.Classes.Image;
import org.testobject.kernel.imgproc.classifier.ImageClassifier;

/**
 * 
 * @author enijkamp
 *
 */
public class ImageClassifierTest
{
	public static final boolean DEBUG = Debug.toDebugMode(false);

	@Test
	public void testKaufdaImages() throws Throwable
	{
		// classify
		Classifier[] before = {};
		
		// classify
		TestUtils.Result<Image> result = TestUtils.classify(ImageUtil.read(toAppPath("kaufda-angebote.png")), before, new ImageClassifier(), DEBUG);

		// assert			
		assertThat(result.widgets.isEmpty(), is(false));
		assertThat(result.widgets.size(), is(4));
		assertThat(result.widgets.peek().cls, is(Classes.Image.class));
		
	}
	
	private static String toAppPath(String file) {
		return "android/4_0_3/screenshots/kaufda/" + file;
	}
}