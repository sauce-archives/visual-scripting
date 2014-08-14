package org.testobject.kernel.replay.impl;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.kernel.inputs.InputStateMachine;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.log.LogUtil;
import org.testobject.commons.util.thread.ThreadUtil;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.locator.api.Container;
import org.testobject.kernel.locator.api.Locator;
import org.testobject.kernel.locator.api.Root;
import org.testobject.kernel.pipeline.Classification;
import org.testobject.kernel.pipeline.Locators;
import org.testobject.kernel.pipeline.Pipeline;
import org.testobject.kernel.pipeline.Stages;
import org.testobject.kernel.pipeline.event.PipelineResultEvent;
import org.testobject.kernel.platform.Grab;
import org.testobject.kernel.platform.Robot;
import org.testobject.kernel.platform.Window;
import org.testobject.kernel.replay.AwaitPipelineEvent;
import org.testobject.kernel.replay.LocatorMatcher;
import org.testobject.kernel.replay.Playback.Delta;
import org.testobject.kernel.replay.Playback.Replay;
import org.testobject.kernel.script.api.Script;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * 
 * @author enijkamp
 * 
 */
public class SimpleReplay implements Replay
{
	private static final Log log = LogFactory.getLog(SimpleReplay.class);

	private final Classification classification;
	private final AwaitPipelineEvent await;
	private final Window window;
	private final Robot robot;
	private final Grab screen;
	private final boolean interpolatePosition = false;
	private DebugHandler debug;

	@AssistedInject
	public SimpleReplay(Classification classification, @Assisted AwaitPipelineEvent await, Window window, @Assisted Robot robot, @Assisted Grab grab, @Assisted DebugHandler debug)
	{
		this.classification = classification;
		this.await = await;
		this.window = window;
		this.robot = robot;
		this.screen = grab;
		this.debug = debug;
	}

	@Override
	public void setDebugHandler(DebugHandler debug) {
		this.debug = debug;
	}

	@Override
	public Delta replay(Script.Requests.Request request) throws ReplayException
	{
		// debug
		log.info("replay-action = " + request);

		// wait for ui
		sleep(2000);

		// dispatch
		Delta.Meta meta;
		if (request instanceof Script.Requests.ClickOn)
		{
			Script.Requests.ClickOn click = (Script.Requests.ClickOn) request;

			// get screenshot
			if (screen.grab() == false)
			{
				throw new IllegalStateException("grab failed");
			}

			// blobs
			Image.Int image = screen.getImage();
			Blob blobs = toBlob(image);

			// locators
			Map<Locator, Blob> locatorToBlob = new IdentityHashMap<>();
			Map<Blob, Locator> blobToLocator = new IdentityHashMap<>();
			Root locators = toLocators(image, blobs, locatorToBlob, blobToLocator);
			debug.handle(new DebugHandler.Locators(locators));

			// target
			Locator targetLocator = matchLocatorPath(locators, click.path).getLast();
			Blob targetBlob = locatorToBlob.get(targetLocator);
			debug.handle(new DebugHandler.Target(targetBlob));

			// click
			Point location = clickOn(targetBlob);
			meta = new Delta.ClickMeta(location, targetBlob);
			// TODO fuzzy equality match required (en)
		}
		else if (request instanceof Script.Requests.Type)
		{
			Script.Requests.Type typeRequest = (Script.Requests.Type) request;

			// type
			type(typeRequest);
			meta = new Delta.TypeMeta();
		}
		else
		{
			throw new IllegalArgumentException(request.getClass().getName());
		}

		// wait for response
		{
			// listen damage events -> grab -> state-machine -> state changes -> pipeline -> locator diff
			PipelineResultEvent event = await.await();

			// create delta object
			Pipeline.Result result = event.result;
			Delta.State before = new Delta.State(event.timeBefore, result.images.before, result.meta.before, result.locator.before,
					result.locator.beforeLocatorToBlob);
			Delta.State after = new Delta.State(event.timeAfter, result.images.after, result.meta.after, result.locator.after,
					result.locator.afterLocatorToBlob);
			return new Delta(request, result.action.response, before, after, result.diff.diffs, meta);
		}
	}

	private void type(Script.Requests.Type input) {
		for (InputStateMachine.State.Key key : input.type.keys) {
			if(key.downFlag) {
				robot.keyDown(key.key);
			} else {
				robot.keyUp(key.key);
			}
		}
	}

	private Point clickOn(Blob blob)
	{
		// translate
		Point relTarget = new Point(blob.bbox.x + blob.bbox.width / 2, blob.bbox.y + blob.bbox.height / 2);
		Rectangle bounds = robot.getInnerBounds(window);

		// positions
		Point absTarget = new Point(bounds.x + relTarget.x, bounds.y + relTarget.y);
		Point current = robot.getMousePosition();

		// debug
		log.info("moving mouse to (" + absTarget.x + "," + absTarget.y + ")");

		// move
		if(interpolatePosition) {
			final int step = 5;
			final int distance = (int) current.distance(absTarget);
			final float steps = distance / step;
			final float dx = (absTarget.x - current.x) / steps;
			final float dy = (absTarget.y - current.y) / steps;

			float x = current.x, y = current.y;
			for (int i = 0; i < steps; i++)
			{
				sleep(20);
				x += dx;
				y += dy;
				robot.mouseMove((int) x, (int) y);
			}
		}
		robot.mouseMove(absTarget.x, absTarget.y);

		// click
		debug.handle(new DebugHandler.Click(new Point(absTarget.x, absTarget.y)));
		sleep(2000);
		robot.mouseClick(absTarget.x, absTarget.y);

		return relTarget;
	}

	private LinkedList<Locator> matchLocatorPath(Root root, LinkedList<Locator> sourcePath) throws ReplayException
	{
		// debug
		log.info("locators:");
		Locators.Util.print(LogUtil.getDebugStream(log), root, 1);
		log.info("path: " + Script.Util.toString(sourcePath));

		// path
		LinkedList<Locator> targetPath = new LinkedList<Locator>();
		Locator[] targetLocators = { root };
		Iterator<Locator> sourcePathIter = sourcePath.iterator();

		// traverse
		while (sourcePathIter.hasNext())
		{
			// next
			Locator pathLocator = sourcePathIter.next();

			// debug
			{
				log.info("looking for path locator:");
				Locators.Util.print(LogUtil.getDebugStream(log), pathLocator, 1);
			}

			// match
			LinkedList<Locator> candidates = new LinkedList<Locator>();
			for (Locator locator : targetLocators)
			{
				if (LocatorMatcher.equalsLocator(locator, pathLocator))
				{
					candidates.add(locator);
				}
			}

			// ambiguity resolution
			if (candidates.size() > 1)
			{
				log.warn("set of ambiguous candidates with size " + candidates.size());
				ListIterator<Locator> iter = candidates.listIterator();
				while (iter.hasNext())
				{
					Locator candidate = iter.next();
					// FIXME go deeper until ambiguity is resolved (en)
					if (LocatorMatcher.equalsRecursive(pathLocator, candidate) == false)
					{
						iter.remove();
					}
				}
			}

			// ambiguity resolution
			if (candidates.size() > 1)
			{
				log.error("ambiguity not resolved");
				throw new ReplayException("ambiguity not resolved");
			}

			// nothing left
			if (candidates.isEmpty())
			{
				log.error("no candidates");
				throw new ReplayException("no candidates");
			}

			// append
			Locator targetLocator = candidates.getFirst();
			{
				// debug
				log.info("selected path locator:");
				Locators.Util.print(LogUtil.getDebugStream(log), targetLocator, 1);

				// add
				targetPath.add(targetLocator);
			}

			// continue
			if (sourcePathIter.hasNext())
			{
				// sanity
				if (pathLocator instanceof Container == false)
				{
					throw new IllegalStateException("container mismatch");
				}

				// next layer
				Container container = (Container) targetLocator;
				targetLocators = container.getChilds();
			}
		}
		return targetPath;
	}

	private Root toLocators(Image.Int image, Blob blob, Map<Locator, Blob> locatorToBlob, Map<Blob, Locator> blobToLocator)
	{
		return classification.toLocator(image, blob, locatorToBlob, blobToLocator);
	}

	private Blob toBlob(org.testobject.commons.util.image.Image.Int image)
	{
		return classification.toBlob(image, Stages.Builder.mock());
	}

	private void sleep(int ms)
	{
		ThreadUtil.sleep(ms);
	}
}