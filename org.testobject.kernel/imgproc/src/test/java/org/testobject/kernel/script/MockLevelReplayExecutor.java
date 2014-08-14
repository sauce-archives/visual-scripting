package org.testobject.kernel.script;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.testobject.commons.util.file.FileUtil;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.classifier.ButtonClassifier;
import org.testobject.kernel.imgproc.classifier.Classifier;
import org.testobject.kernel.imgproc.classifier.GroupClassifier;
import org.testobject.kernel.imgproc.classifier.IconClassifier;
import org.testobject.kernel.imgproc.classifier.PopupClassifier;
import org.testobject.kernel.imgproc.classifier.TextBoxClassifier;
import org.testobject.kernel.imgproc.classifier.TextCharClassifier;
import org.testobject.kernel.imgproc.classifier.TextWordClassifier;
import org.testobject.kernel.ocr.AdditiveMask;
import org.testobject.kernel.ocr.FontRenderer;
import org.testobject.kernel.ocr.MaskClusterer;
import org.testobject.kernel.ocr.SlidingMaskTextRecognizer;
import org.testobject.kernel.ocr.TextRecognizer;
import org.testobject.kernel.ocr.freetype.FT2Library;
import org.testobject.kernel.ocr.freetype.FreeTypeGrayscaleFontRenderer;
import org.testobject.kernel.pipeline.Classification;
import org.testobject.kernel.pipeline.DebugHandler;
import org.testobject.kernel.pipeline.FullScanPipeline;
import org.testobject.kernel.pipeline.LogStagesAdapter;
import org.testobject.kernel.pipeline.Pipeline;
import org.testobject.kernel.pipeline.SimpleClassification;
import org.testobject.kernel.pipeline.event.PipelineResultEvent;
import org.testobject.kernel.platform.Grab;
import org.testobject.kernel.platform.Robot;
import org.testobject.kernel.platform.Window;
import org.testobject.kernel.replay.AwaitPipelineEvent;
import org.testobject.kernel.replay.Playback;
import org.testobject.kernel.replay.Playback.Delta;
import org.testobject.kernel.replay.Playback.Matcher.Match;
import org.testobject.kernel.replay.impl.ReplayException;
import org.testobject.kernel.replay.impl.SimpleMatcher;
import org.testobject.kernel.replay.impl.SimpleReplay;
import org.testobject.kernel.script.Script.Step;
import org.testobject.kernel.script.api.Events.Event;
import org.testobject.kernel.script.api.Script.Requests;
import org.testobject.kernel.script.api.Script.Requests.Request;
import org.testobject.kernel.script.api.Script.Responses.Response;

public class MockLevelReplayExecutor implements ScriptExecutor {

	private final Queue<PipelineResultEvent> queue;
	private final FullScanPipeline pipe;
	private final Classification classification;
	private final AwaitPipelineEvent await;
	private final Window window;

	private SimpleReplay replay;

	public MockLevelReplayExecutor() {
		classification = createClassifier();
		Classifier[] overlayClassifiers = {};
		pipe = new FullScanPipeline(classification, DebugHandler.Builder.sysout(), new LogStagesAdapter(
				FullScanPipeline.log), overlayClassifiers);
		window = createWindow();
		queue = new LinkedList<PipelineResultEvent>();
		await = createAwait(queue);
	}

	@Override
	public ScriptExecutor.Result replay(Script script) {
		if (script.getSteps().size() == 0) {
			throw new IllegalArgumentException("Script has no steps!");
		}
		List<Image.Int> images = getImages(script);
		// replay mocks
		final List<Point> clicks = new LinkedList<Point>();
		Robot robot = createRobot(new Rectangle(images.get(0).w, images.get(0).h), clicks);
		Grab grab = createGrab(images);
		replay = new SimpleReplay(classification, await, window, robot, grab, Playback.Replay.DebugHandler.Builder.mock());

		for (Step step : script.getSteps()) {
			Pipeline.Result result = pipe.process(getEvent(step.getRequest()), step.getBefore(), step.getAfter());
			queue.add(new PipelineResultEvent(0, 0, result));
			try {
				Delta delta = replay.replay(step.getRequest());
				for (Response expected : step.getResponses()) {
					if (new SimpleMatcher(delta.response).match(expected) != Match.SUCCESS) {
						return new Result(Match.FAILURE, "Response '" + expected + "' was expected, but was not available in replay result");
					}
				}
			} catch (ReplayException e) {
				throw new RuntimeException(e);
			}
		}

		return new Result(Match.SUCCESS);
	}

	private List<Image.Int> getImages(Script script) {
		List<Step> steps = script.getSteps();
		List<Image.Int> result = new ArrayList<>(steps.size());
		result.add(steps.get(0).getBefore());
		for (Step step : steps) {
			result.add(step.getAfter());
		}
		return result;
	}

	private Event getEvent(Request request) {
		if (request instanceof Requests.ClickOn) {
			return ((Requests.ClickOn) request).click;
		}
		if (request instanceof Requests.Type) {
			return ((Requests.Type) request).type;
		}
		throw new RuntimeException("no event defined for the request: " + request);
	}

	private Classification createClassifier() {
		Classifier[] widgetClassifiers = {
				new GroupClassifier(),
				new TextCharClassifier(),
				new TextWordClassifier(),
				new IconClassifier(),
				new TextBoxClassifier(),
				new ButtonClassifier(),
				new PopupClassifier() };
		Classification classification = new SimpleClassification(createTextRecognizer(), widgetClassifiers);
		return classification;
	}

	private AwaitPipelineEvent createAwait(final Queue<PipelineResultEvent> queue) {
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
		return await;
	}

	private Grab createGrab(final List<Image.Int> images) {
		Grab grab = new Grab()
		{
			int imageIndex = 0;

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
				return images.get(imageIndex++);
			}
		};
		return grab;
	}

	private Robot createRobot(final Rectangle bounds, final List<Point> clicks) {
		Robot robot = new Robot()
		{
			@Override
			public Rectangle getInnerBounds(Window window)
			{
				return bounds;
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
				return bounds;
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
		return robot;
	}

	private Window createWindow() {
		Window window = new Window()
		{

		};
		return window;
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

	private static List<List<File>> getFonts(URL basePath, String... fontFamilies) {
		List<List<File>> fonts = new ArrayList<>(fontFamilies.length);
		for (String fontFamily : fontFamilies) {
			File file = new File(FileUtil.toFile(basePath), fontFamily);
			File[] fontFiles = file.listFiles();
			fonts.add(Arrays.asList(fontFiles));
		}
		return fonts;
	}

}
