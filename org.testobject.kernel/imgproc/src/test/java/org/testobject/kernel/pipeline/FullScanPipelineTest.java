package org.testobject.kernel.pipeline;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.testobject.kernel.classification.procedural.ProceduralRenderer.Builder.rect;
import static org.testobject.kernel.ocr.TextRecognizer.Builder.Mocks.text;
import static org.testobject.kernel.pipeline.FullScanPipelineTest.Screen.screen;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.testobject.kernel.classification.procedural.ProceduralRenderer;
import org.testobject.kernel.imgproc.blob.BlobUtils;
import org.testobject.kernel.imgproc.blob.GraphBlobBuilder;
import org.testobject.kernel.imgproc.classifier.Classifier;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.kernel.locator.api.Button;
import org.testobject.kernel.locator.api.Label;
import org.testobject.kernel.locator.api.Locator;
import org.testobject.kernel.ocr.FontRenderer;
import org.testobject.kernel.ocr.MaskClusterer;
import org.testobject.kernel.ocr.TextRecognizer;
import org.testobject.kernel.ocr.freetype.FT2Library;
import org.testobject.kernel.ocr.swing.SwingFontRenderer;
import org.testobject.commons.util.file.FileUtil;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.Group;
import org.testobject.kernel.imgproc.blob.GroupBuilder;
import org.testobject.kernel.imgproc.classifier.ButtonClassifier;
import org.testobject.kernel.imgproc.classifier.Classes.Popup;
import org.testobject.kernel.imgproc.classifier.ClassifierBase;
import org.testobject.kernel.imgproc.classifier.Context;
import org.testobject.kernel.imgproc.classifier.GroupClassifier;
import org.testobject.kernel.imgproc.classifier.IconClassifier;
import org.testobject.kernel.imgproc.classifier.TextBoxClassifier;
import org.testobject.kernel.imgproc.classifier.TextCharClassifier;
import org.testobject.kernel.imgproc.classifier.TextWordClassifier;
import org.testobject.kernel.imgproc.plot.VisualizerUtil;
import org.testobject.kernel.locator.api.TextBox;
import org.testobject.kernel.mocks.Inputs;
import org.testobject.kernel.ocr.AdditiveMask;
import org.testobject.kernel.ocr.SlidingMaskTextRecognizer;
import org.testobject.kernel.ocr.freetype.FreeTypeGrayscaleFontRenderer;
import org.testobject.kernel.pipeline.*;
import org.testobject.kernel.script.api.Script;
import org.testobject.kernel.script.api.Script.Action;
import org.testobject.kernel.script.api.Script.Requests.ClickOn;
import org.testobject.kernel.script.api.Script.Responses.Appears;
import org.testobject.kernel.script.api.Script.Responses.Disappears;

/**
 * 
 * @author enijkamp
 * 
 */
public class FullScanPipelineTest {

	@Test
	@Ignore
	public void testBlobImage() throws IOException {
		// screen
		Rectangle button1 = new Rectangle(10, 10, 80, 40);
		Rectangle button2 = new Rectangle(200, 10, 80, 40);

		String button1Title = "Press";
		String button2Title = "Appear";

		Image.Int screen1 = screen(400, 800).button(button1, button1Title).build();
		Image.Int screen2 = screen(400, 800).button(button2, button2Title).build();

		// show
		VisualizerUtil.show(screen1);
		VisualizerUtil.show(screen2);

		// show
		Blob[] blobs = new GraphBlobBuilder(screen1.w, screen1.h).build(screen1);
		GroupBuilder<Blob> groupBuilder = new GroupBuilder<Blob>();

		VisualizerUtil.show(BlobUtils.drawHierarchy(blobs));

		BufferedImage buffer = new BufferedImage(screen1.w, screen1.h, BufferedImage.TYPE_INT_RGB);
		for (Blob blob : blobs[0].children) {
			drawGroup(groupBuilder, blob, buffer.getGraphics());
		}
		VisualizerUtil.show("groups", buffer);

		System.in.read();
	}

	private static void drawGroup(GroupBuilder<Blob> groupBuilder, Blob blob, Graphics g) {
		List<Group<Blob>> groups = groupBuilder.buildGroups(blob.children, 6, 6);
		if (groups.isEmpty() == false) {
			for (Group<Blob> group : groups) {
				g.drawRect(group.getBoundingBox().x, group.getBoundingBox().y, group.getBoundingBox().width, group.getBoundingBox().height);
			}
		}
		for (Blob child : blob.children) {
			drawGroup(groupBuilder, child, g);
		}
	}

	@Test
	public void testPipelineSynthetic() {

		// classifiers
		Classifier[] widgetClassifiers = {
				new GroupClassifier(),
				new TextCharClassifier(createSwingRenderer(), toFont("arial"), toSize(18)),
				new TextWordClassifier(),
				new IconClassifier(),
				new TextBoxClassifier(),
				new ButtonClassifier() };

		// FIXME (en)
		Classifier[] overlayClassifiers = { new Mocks.PopupClassifier() };

		// screen
		Rectangle button1 = new Rectangle(10, 10, 80, 40);
		Rectangle button2 = new Rectangle(200, 10, 80, 40);

		String button1Title = "Press";
		String button2Title = "Appear";

		Image.Int screen1 = screen(400, 800).button(button1, button1Title).build();
		Image.Int screen2 = screen(400, 800).button(button1, button1Title).button(button2, button2Title).build();

		// VisualizerUtil.show(screen1);
		// VisualizerUtil.show(screen2);

		String[] texts = { button1Title, button2Title };

		// inputs
		List<Inputs.Input> inputs = Inputs.InputBuilder.input().click(screen1, screen2, button1.x + 10, button1.y + 10).build();

		// classifier
		Classification classification = new SimpleClassification(TextRecognizer.Builder.mock(texts), widgetClassifiers);

		// pipeline
		Pipeline pipeline = new FullScanPipeline(classification, DebugHandler.Builder.sysout(), new LogStagesAdapter(FullScanPipeline.log),
				overlayClassifiers);

		// results
		LinkedList<Script.Action> results = new LinkedList<>();
		{
			for (Inputs.Input input : inputs) {
				Pipeline.Result result = pipeline.process(input.event, input.before, input.after);

				// assert
				{
					if (result.action.request instanceof Script.Requests.ClickOn) {
						Script.Requests.ClickOn click = (Script.Requests.ClickOn) result.action.request;
						for (Locator locator : click.path) {
							assertThat(result.locator.beforeLocatorToBlob.containsKey(locator), is(true));
						}
					}
				}

				results.add(new Script.Action(result.images.before, result.images.after, result.action.request, result.action.response));
			}
		}

		// print
		{
			for (int i = 0; i < results.size(); i++) {
				System.out.println(i + ". " + results.get(i).request);
				for (int j = 0; j < results.get(i).response.size(); j++) {
					System.out.println("   " + j + ". " + results.get(i).response.get(j));
				}
			}
		}

		// assert
		{
			assertThat(results.size(), is(1));
			{
				Action action = results.get(0);

				// request
				assertEquals(ClickOn.class, results.get(0).request.getClass());

				// response
				assertEquals(Appears.class, action.response.get(0).getClass());
				{
					Appears appears = (Appears) action.response.get(0);
					assertEquals(Button.class, appears.path.getLast().getClass());
				}
			}
		}
	}

	@Test
	public void testPipelineSyntheticWidthLargeDiff() {

		// classifiers
		Classifier[] widgetClassifiers = {
				new GroupClassifier(),
				new TextCharClassifier(createSwingRenderer(), toFont("arial"), toSize(18)),
				new TextWordClassifier(),
				new IconClassifier(),
				new TextBoxClassifier(),
				new ButtonClassifier() };
		Classifier[] overlayClassifiers = { new Mocks.PopupClassifier() };

		Rectangle button1 = new Rectangle(10, 10, 80, 40);

		String button1Title = "Press";

		Image.Int screen1 = screen(400, 800).button(button1, button1Title).build();
		Image.Int screen2 = screen(400, 800).rectangle(10, 10, 389, 789, Color.WHITE).build();

		String[] texts = { button1Title };

		// inputs
		List<Inputs.Input> inputs = Inputs.InputBuilder.input().click(screen1, screen2, button1.x + 10, button1.y + 10).build();

		// classifier
		Classification classification = new SimpleClassification(TextRecognizer.Builder.mock(texts), widgetClassifiers);

		// pipeline
		Pipeline pipeline = new FullScanPipeline(classification, DebugHandler.Builder.sysout(), new LogStagesAdapter(FullScanPipeline.log),
				overlayClassifiers);

		// results
		LinkedList<Script.Action> results = new LinkedList<>();
		{
			for (Inputs.Input input : inputs) {
				Pipeline.Result result = pipeline.process(input.event, input.before, input.after);

				// assert
				{
					if (result.action.request instanceof Script.Requests.ClickOn) {
						Script.Requests.ClickOn click = (Script.Requests.ClickOn) result.action.request;
						for (Locator locator : click.path) {
							assertThat(result.locator.beforeLocatorToBlob.containsKey(locator), is(true));
						}
					}
				}

				results.add(new Script.Action(result.images.before, result.images.after, result.action.request, result.action.response));
			}
		}

		// print
		{
			for (int i = 0; i < results.size(); i++) {
				System.out.println(i + ". " + results.get(i).request);
				for (int j = 0; j < results.get(i).response.size(); j++) {
					System.out.println("   " + j + ". " + results.get(i).response.get(j));
				}
			}
		}

		// assert
		{
			assertThat(results.size(), is(1));
			{
				Action action = results.get(0);
				// request
				assertEquals(ClickOn.class, results.get(0).request.getClass());
				assertThat(action.response.size(), is(1));

				// response
				assertEquals(Disappears.class, action.response.get(0).getClass());
				{
					Disappears appears = (Disappears) action.response.get(0);
					assertEquals(Button.class, appears.path.getLast().getClass());
				}
			}
		}
	}

	// FIXME enable (en)
	@Ignore
	@Test
	public void testPipelineTweetScreenshots() throws IOException {
		testPipelineTweetScreenshots(createTextRecognizer());
	}

	@Test
	public void testPipelineTweetScreenshotsMockOcr() throws IOException {
		Map<TextRecognizer.Builder.Mocks.TextDescriptor, String> texts = new HashMap<>();
		{
			texts.put(TextRecognizer.Builder.Mocks.text(TextRecognizer.Builder.Mocks.box(333, 74, 8, 18)), "1");
			texts.put(TextRecognizer.Builder.Mocks.text(TextRecognizer.Builder.Mocks.box(344, 75, 24, 17), TextRecognizer.Builder.Mocks.area(296)), "40");
			texts.put(TextRecognizer.Builder.Mocks.text(TextRecognizer.Builder.Mocks.box(344, 75, 24, 17), TextRecognizer.Builder.Mocks.area(310)), "36");

			texts.put(TextRecognizer.Builder.Mocks.text(TextRecognizer.Builder.Mocks.box(399, 76, 47, 13)), "Tweet");

			texts.put(TextRecognizer.Builder.Mocks.text(TextRecognizer.Builder.Mocks.box(28, 198, 14, 17)), "T");
			texts.put(TextRecognizer.Builder.Mocks.text(TextRecognizer.Builder.Mocks.box(43, 202, 11, 13)), "e");
			texts.put(TextRecognizer.Builder.Mocks.text(TextRecognizer.Builder.Mocks.box(56, 202, 11, 13)), "s");
			texts.put(TextRecognizer.Builder.Mocks.text(TextRecognizer.Builder.Mocks.box(68, 189, 8, 39)), "t");
		}
		testPipelineTweetScreenshots(createMockTextRecognizer(texts));
	}

	public void testPipelineTweetScreenshots(TextRecognizer<Blob> recognizer) throws IOException {

		// classifiers
		Classifier[] widgetClassifiers = {
				new GroupClassifier(),
				new TextCharClassifier(createSwingRenderer(), toFont("arial"), toSize(18)),
				new TextWordClassifier(),
				new IconClassifier(),
				new TextBoxClassifier(),
				new ButtonClassifier() };

		Classifier[] overlayClassifiers = { new Mocks.PopupClassifier() };

		// screens
		Image.Int before = ImageUtil.read("android/4_0_3/pipeline/twitter/tweet_before.png");
		Image.Int after = ImageUtil.read("android/4_0_3/pipeline/twitter/tweet_after.png");

		// inputs
		List<Inputs.Input> inputs = Inputs.InputBuilder.input().click(before, after, 100, 250).build();

		// classifier
		Classification classification = new SimpleClassification(recognizer, widgetClassifiers);

		// pipeline
		Pipeline pipeline = new FullScanPipeline(classification, DebugHandler.Builder.sysout(), new LogStagesAdapter(FullScanPipeline.log),
				overlayClassifiers);

		// results
		LinkedList<Script.Action> results = new LinkedList<>();
		{
			for (Inputs.Input input : inputs) {
				Pipeline.Result result = pipeline.process(input.event, input.before, input.after);
				results.add(new Script.Action(result.images.before, result.images.after, result.action.request, result.action.response));
			}
		}

		// print
		{
			for (int i = 0; i < results.size(); i++) {
				System.out.println(i + ". " + results.get(i).request);
				for (int j = 0; j < results.get(i).response.size(); j++) {
					System.out.println("   " + j + ". " + results.get(i).response.get(j));
				}
			}
		}

		// assert
		{
			assertThat(results.size(), is(1));
			{
				Action action = results.get(0);

				// request
				assertEquals(ClickOn.class, results.get(0).request.getClass());
				ClickOn clickOn = (ClickOn) results.get(0).request;
				assertEquals(TextBox.class, clickOn.path.getLast().getClass());

				// response
				assertThat(action.response.size(), is(3));
				{
					assertEquals(Appears.class, action.response.get(2).getClass());
					{
						Appears appears = (Appears) action.response.get(2);
						assertEquals(Button.class, appears.path.getLast().getClass());
					}
				}
				{
					assertEquals(Script.Responses.Update.class, action.response.get(0).getClass());
					{
						Script.Responses.Update update = (Script.Responses.Update) action.response.get(0);
						assertEquals(Label.class, update.afterPath.getLast().getClass());
						{
							assertThat(update.properties.size() >= 1, is(true));
							Script.Responses.Update.Property property = update.properties.get(0);
							assertThat(property.oldValue.toString(), is("140"));
							assertThat(property.newValue.toString(), is("136"));
						}
					}
				}
				{
					assertEquals(Script.Responses.Update.class, action.response.get(1).getClass());
					{
						Script.Responses.Update update = (Script.Responses.Update) action.response.get(1);
						assertEquals(TextBox.class, update.afterPath.getLast().getClass());
						{
							assertThat(update.properties.size() >= 1, is(true));
							Script.Responses.Update.Property property = update.properties.get(0);
							assertThat(property.oldValue.toString(), is(""));
							assertThat(property.newValue.toString(), is("Test"));
						}
					}
				}
			}
		}
	}

	private static TextRecognizer<Blob> createTextRecognizer() {
		final int[] FONT_SIZE = { 14, 15, 16, 17, 18, 19 };
		final int histogramN = 7;
		final double MAX_DIST = 1.75d;
		final int MAX_TOLERANCE_X = 2;
		final List<List<File>> sanfSerifFonts = getFonts(ClassLoader.getSystemResource("android/4_0_3/fonts"), "sans-serif");

		FontRenderer renderer = new FreeTypeGrayscaleFontRenderer(160, 240, FT2Library.FT_LCD_FILTER_LIGHT);
		MaskClusterer maskClusterer = new MaskClusterer(renderer, histogramN, 1400f, 800f);
		List<AdditiveMask> additiveMasks = maskClusterer.generateMasksFast(sanfSerifFonts, FONT_SIZE);
		return new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X);
	}

	private static TextRecognizer<Blob> createMockTextRecognizer(Map<TextRecognizer.Builder.Mocks.TextDescriptor, String> texts) {
		return TextRecognizer.Builder.mock(texts);
	}

	private static List<List<File>> getFonts(URL basePath, String... fontFamilies) {
		List<List<File>> fonts = new ArrayList<>(fontFamilies.length);
		for (String fontFamily : fontFamilies) {
			File file = new File(FileUtil.toFile(basePath), fontFamily);
			File[] fontFiles = file.listFiles();
			fonts.add(Arrays.asList(fontFiles));
		}
		return fonts;
	}

	public interface Mocks {

		class PopupClassifier extends ClassifierBase {

			@Override
			public Specification getSpec() {
				return spec().returns(Popup.class).build();
			}

			@Override
			public Match match(Context context, Blob blob) {
				return failed;
			}
		}
	}

	public static class Screen {

		public static class Builder {

			public final int w, h;
			public final List<ProceduralRenderer.Procedure> procedures = new LinkedList<>();

			public Builder(int w, int h) {
				this.w = w;
				this.h = h;
			}

			public Builder button(int x, int y, int w, int h, String title) {
				procedures.add(Screen.button(x, y, w, h, title));
				return this;
			}

			public Builder button(Rectangle r, String title) {
				procedures.add(Screen.button(r.x, r.y, r.width, r.height, title));
				return this;
			}

			public Builder icon(int x, int y, double[] contour) {
				procedures.add(Screen.icon(x, y, contour));
				return this;
			}

			public Builder rectangle(int x, int y, int width, int height, Color c) {
				procedures.add(Screen.rectangle(x, y, width, height, c));
				return this;
			}

			public Image.Int build() {
				return new ProceduralRenderer().render(w, h, procedures);
			}
		}

		public static Builder screen(int w, int h) {
			return new Builder(w, h);
		}

		public static ProceduralRenderer.Procedure button(int x, int y, int w, int h, String title) {
			return ProceduralRenderer.Builder.describe().shape(ProceduralRenderer.Builder.rect(x, y, w, h).round(12, 12).stroke(ProceduralRenderer.Builder.Color.darkGray()).gradient(ProceduralRenderer.Builder.Color.white(), ProceduralRenderer.Builder.Color.lightGray()).text(title)).build();
		}

		public static ProceduralRenderer.Procedure rectangle(int x, int y, int width, int height, Color color) {
			return ProceduralRenderer.Builder.describe().shape(ProceduralRenderer.Builder.rect(x, y, width, height).fill(color)).build();
		}

		public static ProceduralRenderer.Procedure icon(int x, int y, double[] contour) {
			double[] shape = translate(contour, x, y);
			return ProceduralRenderer.Builder.describe().shape(ProceduralRenderer.Builder.polygon().points(shape).stroke(ProceduralRenderer.Builder.Color.blue()).fill(ProceduralRenderer.Builder.Color.blue())).build();
		}

		private static double[] translate(double[] contour, int x, int y) {
			double[] translated = new double[contour.length];
			for (int i = 0; i < contour.length; i += 2) {
				translated[i + 0] = contour[i + 0] + x;
				translated[i + 1] = contour[i + 1] + y;
			}
			return translated;
		}
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
