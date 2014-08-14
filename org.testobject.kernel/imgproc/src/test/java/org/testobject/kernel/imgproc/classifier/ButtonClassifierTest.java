package org.testobject.kernel.imgproc.classifier;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.testobject.kernel.imgproc.classifier.TestUtils.classify;
import static org.testobject.kernel.imgproc.classifier.TestUtils.testPositives;
import static org.testobject.kernel.mocks.Screen.screen;

import java.awt.Rectangle;
import java.io.File;

import org.junit.Test;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.classifier.*;
import org.testobject.kernel.imgproc.classifier.Classes.Button;
import org.testobject.kernel.imgproc.classifier.TestUtils.Result;
import org.testobject.kernel.ocr.FontRenderer;
import org.testobject.kernel.ocr.swing.SwingFontRenderer;

/**
 * 
 * @author enijkamp
 *
 */
public class ButtonClassifierTest
{
	public static final boolean DEBUG = Debug.toDebugMode(true);

	@Test
	public void testButtonSynthetic() throws Throwable
	{
		// screen
		Rectangle bounds = new Rectangle(200, 10, 80, 40);
		String label = "Press";
		Image.Int screen = screen(400, 800).button(bounds, label).build();

		// classify
		Classifier[] before =
		{
		        new GroupClassifier(),
		        new TextCharClassifier(createSwingRenderer(), toFont("arial"), toSize(18))
		};
		Result<Button> result = classify(screen, before, new ButtonClassifier(), DEBUG);

		// assert
		assertThat(result.widgets.isEmpty(), is(false));
		assertThat(result.widgets.peek().cls, is(Button.class));
	}

	@Test
	public void testButtonPositives233() throws Throwable
	{
		// classify
		Classifier[] before =
		{
		        new GroupClassifier(),
		        new IconClassifier(),
		        new TextCharClassifier()
		};

		// positives
		testPositives("android/2_3_3/classifier/button/positives", before, new ButtonClassifier(), DEBUG);
	}

	@Test
	public void testButtonPositives403() throws Throwable
	{
		// classify
		Classifier[] before =
		{
		        new GroupClassifier(),
		        new IconClassifier(),
		        new TextCharClassifier()
		};

		// positives
		testPositives("android/4_0_3/classifier/button/positives", before, new ButtonClassifier(), DEBUG);
	}

	private static FontRenderer createSwingRenderer() {
		return new SwingFontRenderer();
	}
	
	private static File[] toFont(String file) {
		return new File[] { new File(file) };
	}
	
	private static int[] toSize(int size) {
		return new int[] { size };
	}
}