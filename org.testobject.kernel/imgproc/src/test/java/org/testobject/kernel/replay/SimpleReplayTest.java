package org.testobject.kernel.replay;

import static org.testobject.kernel.ocr.TextRecognizer.Builder.Mocks.area;
import static org.testobject.kernel.ocr.TextRecognizer.Builder.Mocks.box;
import static org.testobject.kernel.ocr.TextRecognizer.Builder.Mocks.text;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.testobject.commons.events.ButtonMask;
import org.testobject.kernel.imgproc.classifier.Classifier;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.kernel.locator.api.Button;
import org.testobject.kernel.locator.api.Locator;
import org.testobject.kernel.locator.api.Popup;
import org.testobject.kernel.locator.api.Root;
import org.testobject.kernel.ocr.freetype.FT2Library;
import org.testobject.kernel.pipeline.DebugHandler;
import org.testobject.kernel.pipeline.SimpleClassification;
import org.testobject.kernel.platform.Grab;
import org.testobject.kernel.replay.Playback;
import org.testobject.kernel.replay.impl.ReplayException;
import org.testobject.kernel.replay.impl.SimpleMatcher;
import org.testobject.kernel.replay.impl.SimpleReplay;
import org.testobject.commons.util.file.FileUtil;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.classifier.ButtonClassifier;
import org.testobject.kernel.imgproc.classifier.GroupClassifier;
import org.testobject.kernel.imgproc.classifier.IconClassifier;
import org.testobject.kernel.imgproc.classifier.PopupClassifier;
import org.testobject.kernel.imgproc.classifier.TextBoxClassifier;
import org.testobject.kernel.imgproc.classifier.TextCharClassifier;
import org.testobject.kernel.imgproc.classifier.TextWordClassifier;
import org.testobject.kernel.locator.api.TextBox;
import org.testobject.kernel.ocr.AdditiveMask;
import org.testobject.kernel.ocr.FontRenderer;
import org.testobject.kernel.ocr.MaskClusterer;
import org.testobject.kernel.ocr.SlidingMaskTextRecognizer;
import org.testobject.kernel.ocr.TextRecognizer;
import org.testobject.kernel.ocr.TextRecognizer.Builder.Mocks.TextDescriptor;
import org.testobject.kernel.ocr.freetype.FreeTypeGrayscaleFontRenderer;
import org.testobject.kernel.pipeline.Classification;
import org.testobject.kernel.pipeline.FullScanPipeline;
import org.testobject.kernel.pipeline.LogStagesAdapter;
import org.testobject.kernel.pipeline.Pipeline;
import org.testobject.kernel.pipeline.event.PipelineResultEvent;
import org.testobject.kernel.platform.Robot;
import org.testobject.kernel.platform.Window;
import org.testobject.kernel.script.api.Events;
import org.testobject.kernel.replay.AwaitPipelineEvent;
import org.testobject.kernel.script.api.Script;

/**
 * 
 * @author enijkamp
 * 
 */
public class SimpleReplayTest {

	@Test
	public void testSimpleReplay() throws IOException, ReplayException {
		testSimpleReplay(createTextRecognizer());
	}

	// FIXME ocr still incorrect (en)
	@Ignore
	@Test
	public void testSimpleReplayOcrMock() throws IOException, ReplayException {

		Map<TextDescriptor, String> texts = new HashMap<>();
		{
			// Cancel
			{
				texts.put(text(box(35, 76, 19, 13), area(152)), "Ca");
				texts.put(text(box(56, 79, 18, 10), area(147)), "nc");
				texts.put(text(box(75, 79, 8, 10), area(63)), "e");
				texts.put(text(box(85, 75, 3, 14), area(42)), "l");
			}
			// Tweet
			{
				texts.put(text(box(399, 76, 47, 13), area(374)), "Tweet");
			}
			// Discard
			{
				texts.put(text(box(207, 494, 11, 15), area(82)), "D");
				texts.put(text(box(220, 497, 3, 12), area(36)), "i");
				texts.put(text(box(225, 498, 9, 11), area(72)), "s");
				texts.put(text(box(236, 497, 9, 12), area(62)), "c");
				texts.put(text(box(247, 497, 10, 13), area(90)), "a");
				texts.put(text(box(259, 497, 6, 12), area(43)), "r");
				texts.put(text(box(266, 493, 10, 16), area(119)), "d");
			}
		}

		testSimpleReplay(createMockTextRecognizer(texts));
	}

	public void testSimpleReplay(TextRecognizer<Blob> recognizer) throws IOException, ReplayException
	{
		// image mocks
		final Image.Int[] images =
		{
				read("1.png"),
				read("2.png"),
				read("3.png"),
				read("4.png"),
				read("5.png")
		};

		// script
		List<Script.Requests.Request> script = new LinkedList<>();
		// 1
		LinkedList<Locator> pathToTweetButton = new LinkedList<>();
		// 2
		LinkedList<Locator> pathEmptyPopup = new LinkedList<>();
		LinkedList<Locator> pathToEmptyTextBox = new LinkedList<>();
		// 3
		LinkedList<Locator> pathFullPopup = new LinkedList<>();
		LinkedList<Locator> pathToFullTextBox = new LinkedList<>();
		// 4
		LinkedList<Locator> pathToCancelButton = new LinkedList<>();
		// 5
		LinkedList<Locator> pathToDiscardButton = new LinkedList<>();
		{
			// 1
			{
				Button button = new Button("write");
				Root root = new Root(new Locator[] { button });
				pathToTweetButton.addAll(Arrays.asList(new Locator[] { root, button }));
			}

			// 2
			{
				TextBox textbox = new TextBox("");
				Popup popup = new Popup(new Locator[] { textbox });
				Root root = new Root(new Locator[] { popup });

				pathEmptyPopup.addAll(Arrays.asList(new Locator[] { root, popup }));
				pathToEmptyTextBox.addAll(Arrays.asList(new Locator[] { root, popup, textbox }));
			}

			// 3
			{
				TextBox textbox = new TextBox("Heililo"); // FIXME ocr should return "Hello" (en)
				Popup popup = new Popup(new Locator[] { textbox });
				Root root = new Root(new Locator[] { popup });

				pathFullPopup.addAll(Arrays.asList(new Locator[] { root, popup }));
				pathToFullTextBox.addAll(Arrays.asList(new Locator[] { root, popup, textbox }));
			}

			// 4
			{
				Button button = new Button("Cancel");
				Popup popup = new Popup(new Locator[] { button });
				Root root = new Root(new Locator[] { popup });

				pathToCancelButton.addAll(Arrays.asList(new Locator[] { root, popup, button }));
			}

			// 5
			{
				Button button = new Button("Discard");
				Root root = new Root(new Locator[] { button });

				pathToDiscardButton.addAll(Arrays.asList(new Locator[] { root, button }));
			}

			// requests
			script.add(new Script.Requests.ClickOn(new Events.Click(1, ButtonMask.ButtonLeft, 420, 60), pathToTweetButton));
			script.add(new Script.Requests.ClickOn(new Events.Click(2, ButtonMask.ButtonLeft, 200, 200), pathToEmptyTextBox));
			script.add(new Script.Requests.ClickOn(new Events.Click(3, ButtonMask.ButtonLeft, 30, 70), pathToCancelButton));
			script.add(new Script.Requests.ClickOn(new Events.Click(4, ButtonMask.ButtonLeft, 200, 500), pathToDiscardButton));
		}

		// pipeline
		Classifier[] widgetClassifiers = {
				new GroupClassifier(),
				new TextCharClassifier(),
				new TextWordClassifier(),
				new IconClassifier(),
				new TextBoxClassifier(),
				new ButtonClassifier(),
				new PopupClassifier() };
		Classifier[] overlayClassifiers = {};
		Classification classification = new SimpleClassification(recognizer, widgetClassifiers);
		FullScanPipeline pipe = new FullScanPipeline(classification, DebugHandler.Builder.sysout(), new LogStagesAdapter(
				FullScanPipeline.log),
				overlayClassifiers);

		// replay mocks
		final List<Point> clicks = new LinkedList<Point>();

		Window window = new Window()
		{

		};

		Robot robot = new Robot()
		{
			@Override
			public Rectangle getInnerBounds(Window window)
			{
				return new Rectangle(0, 0, images[0].w, images[0].h);
			}

			@Override
			public Point getMousePosition()
			{
				return new Point(0, 0);
			}

			@Override
			public void mouseClick(int x, int y)
			{
				clicks.add(new Point(x, y));
			}

			@Override
			public Rectangle getOuterBounds(Window window)
			{
				return new Rectangle(0, 0, images[0].w, images[0].h);
			}

			@Override
			public void raiseWindow(Window window)
			{

			}

			@Override
			public void keyPress(int keycode)
			{

			}

			@Override
			public void moveWindow(Window window, int x, int y)
			{

			}

			@Override
			public void keyUp(int keycode) {

			}

			@Override
			public void keyDown(int keycode) {

			}

			@Override
			public void mouseUp(int x, int y) {

			}

			@Override
			public void mouseDown(int x, int y) {
				clicks.add(new Point(x, y));
			}

			@Override
			public void mouseMove(int x, int y) {

			}

			@Override
			public void mouseDrag(int startx, int starty, int endx, int endy,
					int steps, long ms) {

			}

			@Override
			public void close() {
				
			}
		};

		Grab grab = new Grab()
		{
			int image = 0;

			@Override
			public void close()
			{

			}

			@Override
			public boolean grab()
			{
				return true;
			}

			@Override
			public void reset()
			{

			}

			@Override
			public Image.Int getImage()
			{
				return images[image++];
			}
		};

		// pipeline events
		final Queue<PipelineResultEvent> queue = new LinkedList<PipelineResultEvent>();
		AwaitPipelineEvent await = new AwaitPipelineEvent()
		{
			@Override
			public PipelineResultEvent await()
			{
				return queue.poll();
			}

			@Override
			public void handlePipelineResultEvent(PipelineResultEvent event) {

			}
		};

		// replay
		SimpleReplay replay = new SimpleReplay(classification, await, window, robot, grab, Playback.Replay.DebugHandler.Builder.mock());
		for (int i = 0; i < script.size(); i++)
		{
			// request
			Script.Requests.Request request = script.get(i);

			// images
			Image.Int before = images[i];
			Image.Int after = images[i + 1];

			// pipeline
			if (request instanceof Script.Requests.ClickOn)
			{
				Script.Requests.ClickOn clickOn = (Script.Requests.ClickOn) request;
				Pipeline.Result result = pipe.process(clickOn.click, before, after);
				queue.add(new PipelineResultEvent(0, 0, result));
			}

			// replay
			Playback.Delta delta = replay.replay(request);

			// deltas
			System.out.println(request);
			for (Script.Responses.Response response : delta.response)
			{
				System.out.println("  " + response);
			}

			// assertions
			if (request instanceof Script.Requests.ClickOn)
			{
				Script.Requests.ClickOn clickOn = (Script.Requests.ClickOn) request;

				if (clickOn.path == pathToTweetButton)
				{
					// Disappears(Root->Button['icon'])
					{
						Script.Responses.Response buttonDisappears = new Script.Responses.Disappears(pathToTweetButton);
						Assert.assertEquals(Playback.Matcher.Match.SUCCESS, new SimpleMatcher(delta.response).match(buttonDisappears));
					}

					// Appears(Root->Popup)
					{
						Script.Responses.Response popupAppears = new Script.Responses.Appears(pathEmptyPopup);
						Assert.assertEquals(Playback.Matcher.Match.SUCCESS, new SimpleMatcher(delta.response).match(popupAppears));
					}
				}

				if (clickOn.path == pathToCancelButton)
				{
					// Disappears(Root->Popup)
					{
						Script.Responses.Response popupDisappears = new Script.Responses.Disappears(pathFullPopup);
						Assert.assertEquals(Playback.Matcher.Match.SUCCESS, new SimpleMatcher(delta.response).match(popupDisappears));
					}

					// Appears(Root->Button['Discard'])
					{
						Script.Responses.Response buttonAppears = new Script.Responses.Appears(pathToDiscardButton);
						Assert.assertEquals(Playback.Matcher.Match.SUCCESS, new SimpleMatcher(delta.response).match(buttonAppears));
					}
				}

				if (clickOn.path == pathToDiscardButton)
				{
					// Disappears(Root->Button['Cancel'])
					{
						Script.Responses.Response buttonDisappears = new Script.Responses.Disappears(pathToDiscardButton);
						Assert.assertEquals(Playback.Matcher.Match.SUCCESS, new SimpleMatcher(delta.response).match(buttonDisappears));
					}

					// Appears(Root->Button['icon'])
					{
						Script.Responses.Response buttonAppears = new Script.Responses.Appears(pathToTweetButton);
						Assert.assertEquals(Playback.Matcher.Match.SUCCESS, new SimpleMatcher(delta.response).match(buttonAppears));
					}
				}
			}
		}

		// assert on clicks
		{
			final int fat = 100;
			Assert.assertEquals(4, clicks.size());
			Assert.assertTrue(new Rectangle(400, 50, fat * 2, fat * 2).contains(clicks.get(0)));
			Assert.assertTrue(new Rectangle(200, 400, fat * 2, fat * 2).contains(clicks.get(1)));
			Assert.assertTrue(new Rectangle(50, 60, fat * 2, fat * 2).contains(clicks.get(2)));
			Assert.assertTrue(new Rectangle(220, 450, fat * 2, fat * 2).contains(clicks.get(3)));
		}
	}

	private static TextRecognizer<Blob> createTextRecognizer() {
		final int[] FONT_SIZE = { 12, 13, 14, 15, 16 };
		final int HISTOGRAM_N = 7;
		final double MAX_DIST = 1d;
		final int MAX_TOLERANCE_X = 2;
		final List<List<File>> sanfSerifFonts = getFonts(ClassLoader.getSystemResource("android/4_0_3/fonts"), "sans-serif");

		FontRenderer renderer = new FreeTypeGrayscaleFontRenderer(160, 240, FT2Library.FT_LCD_FILTER_LIGHT);
		MaskClusterer maskClusterer = new MaskClusterer(renderer, HISTOGRAM_N, 1400f, 800f);
		List<AdditiveMask> additiveMasks = maskClusterer.generateMasksFast(sanfSerifFonts, FONT_SIZE);
		return new SlidingMaskTextRecognizer(additiveMasks, HISTOGRAM_N, MAX_DIST, MAX_TOLERANCE_X);
	}

	private static TextRecognizer<Blob> createMockTextRecognizer(Map<TextDescriptor, String> texts) {
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

	private static Image.Int read(String file) throws IOException {
		return ImageUtil.read("android/4_0_3/replay/twitter/" + file);
	}
}
