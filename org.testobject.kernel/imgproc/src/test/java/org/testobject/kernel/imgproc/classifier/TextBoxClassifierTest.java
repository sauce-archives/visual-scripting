package org.testobject.kernel.imgproc.classifier;

import java.awt.Rectangle;

import org.junit.Test;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.classifier.Classes;
import org.testobject.kernel.imgproc.classifier.Classes.Button;
import org.testobject.kernel.imgproc.classifier.Classifier;
import org.testobject.kernel.imgproc.classifier.TextBoxClassifier;
import org.testobject.kernel.mocks.Screen;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;
import static org.testobject.kernel.imgproc.classifier.TestUtils.classify;

/**
 * 
 * @author enijkamp
 *
 */
public class TextBoxClassifierTest
{
	public static final boolean DEBUG = Debug.toDebugMode(false);

	@Test
	public void testTextBoxSynthetic() throws Throwable
	{
		// screen
		Rectangle bounds = new Rectangle(20, 20, 300, 300);
		Image.Int screen = Screen.screen(400, 800).textbox(bounds).build();

		// classify
		Classifier[] before = {};
		TestUtils.Result<Button> result = TestUtils.classify(screen, before, new TextBoxClassifier(), DEBUG);

		// assert
		assertThat(result.widgets.isEmpty(), is(false));
		assertThat(result.widgets.peek().cls, is(Classes.TextBox.class));
	}

	@Test
	public void testTextBoxPositives() throws Throwable
	{
		// classify
		Classifier[] before = {};

		// positives
		TestUtils.testPositives("android/4_0_3/classifier/textbox/positives", before, new TextBoxClassifier(), DEBUG);
	}
}
