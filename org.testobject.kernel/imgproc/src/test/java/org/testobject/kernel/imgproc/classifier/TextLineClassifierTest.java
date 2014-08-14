package org.testobject.kernel.imgproc.classifier;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.testobject.kernel.imgproc.classifier.TestUtils.classify;

import org.junit.Test;
import org.testobject.commons.util.config.Debug;
import org.testobject.kernel.imgproc.classifier.*;
import org.testobject.kernel.imgproc.classifier.Classes.Icon;
import org.testobject.kernel.imgproc.classifier.Classes.TextLine;
import org.testobject.kernel.imgproc.classifier.TestUtils.Result;
import org.testobject.kernel.imgproc.util.ImageUtil;

/**
 * 
 * @author enijkamp
 *
 */
public class TextLineClassifierTest
{
	public static final boolean DEBUG = Debug.toDebugMode(false);

	@Test
	public void testTweet() throws Throwable
	{
		// classify
		Classifier[] before = {
				new GroupClassifier(),
				new TextCharClassifier(),
				new TextWordClassifier()
		};
		
		// classify
		Result<Icon> result = classify(ImageUtil.read(toTwitterPath("tweet.png")), before, new TextLineClassifier(), DEBUG);

		// assert			
		assertThat(result.widgets.isEmpty(), is(false));
		assertThat(result.widgets.peek().cls, is(TextLine.class));
		
		// TODO check content (en)
		TextLine line = (TextLine) result.widgets.peek().cls;
	}
	
	private static String toTwitterPath(String file) {
		return "android/4_0_3/classifier/textline/positives/" + file;
	}
}