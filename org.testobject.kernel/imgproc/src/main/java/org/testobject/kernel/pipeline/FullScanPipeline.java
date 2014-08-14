package org.testobject.kernel.pipeline;

import static org.testobject.commons.util.collections.Lists.toLinkedList;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.Mutator;
import org.testobject.kernel.imgproc.classifier.Classifier;
import org.testobject.kernel.imgproc.classifier.Context;
import org.testobject.kernel.imgproc.classifier.VisitingMutator;
import org.testobject.kernel.imgproc.diff.ImageComparator;
import org.testobject.kernel.imgproc.diff.OcclusionTracker;
import org.testobject.kernel.imgproc.diff.StackOcclusionTracker;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.kernel.locator.api.Locator;
import org.testobject.kernel.locator.api.Root;
import org.testobject.kernel.script.api.Events;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.Image.Int;
import org.testobject.kernel.script.api.Script;

/**
 * 
 * @author enijkamp
 * 
 */
public class FullScanPipeline implements Pipeline {

	public static final Log log = LogFactory.getLog(FullScanPipeline.class);

	public static class RectUtil {

		public static Rectangle.Int enlargeRect(Rectangle.Int rect, int fatX, int fatY, int maxW, int maxH) {
			int x = rect.x - fatX;
			x = x < 0 ? 0 : x;

			int y = rect.y - fatY;
			y = y < 0 ? 0 : y;

			int w = rect.w + (fatX * 2);
			w = w + x > maxW ? maxW - x : w;

			int h = rect.h + (fatY * 2);
			h = h + y > maxH ? maxH - y : h;

			return new Rectangle.Int(x, y, w, h);
		}

		public static Rectangle.Int union(List<Rectangle.Int> rects) {
			Rectangle.Int rect = rects.get(0);
			for (int i = 1; i < rects.size(); i++) {
				rect = rect.union(rects.get(i));
			}
			return rect;
		}
	}

	private final OcclusionTracker occlusionTracker = new StackOcclusionTracker();

	// FIXME de-activated for the demo (en)
	// private final static Classifier[] DEFAULT_OVERLAY_CLASSIFIERS = { new DialogClassifier(), new ContextMenuClassifier() };

	private final static Classifier[] DEFAULT_OVERLAY_CLASSIFIERS = {};

	private final Classifier[] classifiersOverlay;

	private static class Layer {
		public enum Type {
			OVERLAY, UPDATE
		}

		final Type type;
		final Int image;
		final Rectangle.Int occlusion;
		final Blob blobs;
		final Root locators;
		final Map<Locator, Blob> locatorToBlob; // FIXME locator doesnt always map to a single blob? (en)
		final Map<Blob, Locator> blobToLocator; // FIXME hacky (en)

		public Layer(Type type, Image.Int image, Rectangle.Int occlusion, Blob blobs, Root locators, Map<Locator, Blob> locatorToBlob,
				Map<Blob, Locator> blobToLocator) {
			this.type = type;
			this.image = image;
			this.occlusion = occlusion;
			this.blobs = blobs;
			this.locators = locators;
			this.locatorToBlob = locatorToBlob;
			this.blobToLocator = blobToLocator;
		}
	}

	private final Stack<Layer> layers = new Stack<>();
	private final Stages stages;
	private final DebugHandler debug;
	private final Classification classification;

	public FullScanPipeline(Classification classification, DebugHandler debug, Stages stages, Classifier[] classifiersOverlay) {
		this.classification = classification;
		this.debug = debug;
		this.stages = stages;
		this.classifiersOverlay = classifiersOverlay;
	}

	public FullScanPipeline(Classification classification, DebugHandler debug) {
		this(classification, debug, new LogStagesAdapter(log), DEFAULT_OVERLAY_CLASSIFIERS);
	}

	public FullScanPipeline(Classification classification) {
		this(classification, DebugHandler.Builder.mock(), new LogStagesAdapter(log), DEFAULT_OVERLAY_CLASSIFIERS);
	}

	@Override
	public Result process(Events.Event event, Image.Int before, Image.Int after) {
		Script.Requests.Request request = null;
		List<Script.Responses.Response> response = new LinkedList<>();

		stages.start();

		// context
		Context context = new Context(before, after);

		// 0. init: create dummy occlusion and blobs
		if (layers.isEmpty()) {
			// image
			Image.Int image = before;

			// occlusion
			Rectangle.Int occlusion = new Rectangle.Int(0, 0, image.w, image.h);

			// classification
			Blob blobs = classification.toBlob(image, stages);

			// locators
			Map<Locator, Blob> locatorToBlob = new IdentityHashMap<>();
			Map<Blob, Locator> blobToLocator = new IdentityHashMap<>();
			Root locator = classification.toLocator(image, blobs, locatorToBlob, blobToLocator);

			// push onto stack
			layers.push(new Layer(Layer.Type.UPDATE, before, occlusion, blobs, locator, locatorToBlob, blobToLocator));

			stages.done("init");
		}
		
		// FIXME don't use old blob hierarchy if the image has changed (al)
		if (layers.peek().image != before) {
			// image
			Image.Int image = before;

			// occlusion
			Rectangle.Int occlusion = new Rectangle.Int(0, 0, image.w, image.h);

			// classification
			Blob blobs = classification.toBlob(image, stages);

			// locators
			Map<Locator, Blob> locatorToBlob = new IdentityHashMap<>();
			Map<Blob, Locator> blobToLocator = new IdentityHashMap<>();
			Root locator = classification.toLocator(image, blobs, locatorToBlob, blobToLocator);

			// push onto stack
			layers.pop();
			layers.push(new Layer(Layer.Type.UPDATE, before, occlusion, blobs, locator, locatorToBlob, blobToLocator));

			stages.done("init");
		}
		
		debug.handle(new Images(before, after));

		// 1. script: infer user actions (on before screen)
		Target target = null;
		if (event instanceof Events.Click) {

			Layer layer = layers.peek();
			Rectangle.Int occlusion = layer.occlusion;
			Root locators = layer.locators;
			Map<Locator, Blob> locatorToBlob = layer.locatorToBlob;
			Map<Blob, Locator> blobToLocator = layer.blobToLocator;
			Events.Click click = (Events.Click) event;

			LinkedList<Locator> path = Locate.locate(layer.blobs, locators, locatorToBlob, blobToLocator, new Point(click.x - occlusion.x,
					click.y - occlusion.y));
			if (path.isEmpty() == false) {			
				target = new Target(locatorToBlob.get(path.getLast()));
				debug.handle(target);
			}
			request = new Script.Requests.ClickOn(click, path);
		} else if (event instanceof Events.Type) {
			Events.Type type = (Events.Type) event;
			request = new Script.Requests.Type(type);
		} else if (event instanceof Events.Wait) {
			request = new Script.Requests.WaitFor();
		} else {
			throw new IllegalArgumentException("Unsupported event type '" + event.getClass() + "'");
		}
		stages.done("actions");

		// 2. deltas: regions of change
		int tolerance = 5;
		List<Rectangle.Int> deltas = ImageComparator.compare(before, after, tolerance);
		debug.handle(new Diffs(deltas));
		if (deltas.isEmpty()) {
			// since nothing changed, re-use intermediate results of previous run
			Layer layer = layers.peek();
			return new Result(new Images(before, after), new Action(request, Collections.<Script.Responses.Response> emptyList()),
					new Metas(layer.blobs, layer.blobs), new Locators(layer.locators, layer.locators, layer.locatorToBlob,
							layer.locatorToBlob), new Diffs(deltas), target);
		}
		stages.done("deltas");

		// FIXME disabled for kaufda demo (en)

		// 3. occlusions: relevant regions
		// Rectangle delta = RectUtil.union(deltas);
		// Rectangle region = occlusionTracker.process(before, after, delta);
		// if (isOcclusionDisappeared(region)) {
		// /*
		// * consider:
		// * - if we have a tab-panel with two tabs A and B, A is active
		// * - click on B, click on A
		// * - occlusion tracker tracks B as occlusion
		// * - with click on A initial state is restored and returns region.isEmpty()
		// * thus:
		// * - we have to differentiate between overlays and updates
		// */
		//
		// // is occlusion an overlay
		// Layer layer = layers.peek();
		// if (layer.type == Layer.Type.OVERLAY) {
		// // occlusion disappeared
		// layers.pop();
		//
		// // disappears event
		// for (Locator child : layer.locators.childs) {
		// response.add(new Script.Responses.Disappears(toLinkedList(child)));
		// }
		//
		// stages.end();
		// return new Result(new Images(before, after), new Action(request, response), new Metas(layer.blobs, layer.blobs),
		// new Locators(layer.locators, layer.locators, layer.locatorToBlob, layer.locatorToBlob), new Diffs(deltas), target);
		// } else {
		// // if occlusion is not an overlay, continue with original delta
		// region = delta;
		// }
		// }
		// Rectangle enlarged = RectUtil.enlargeRect(region, 3, 3, after.w, after.h);
		// Image.Int occlusion = ImageUtil.crop(after, enlarged);
		// stages.done("occlusions");

		Rectangle.Int region = new Rectangle.Int(0, 0, after.w, after.h);
		Rectangle.Int enlarged = new Rectangle.Int(0, 0, after.w, after.h);
		Image.Int occlusion = ImageUtil.crop(after, enlarged);

		// 4. classification (1): widgets
		Blob blobsAfter = classification.toBlob(occlusion, stages);
		Blob blobsBefore = layers.peek().blobs;
		debug.handle(new Metas(blobsBefore, blobsAfter));

		// FIXMED disabled for kaufda demo (en)

		// 5. classification (2): overlays
		// List<Mutator.Mutation> overlays = new ArrayList<Mutator.Mutation>();
		// for (Classifier classifier : classifiersOverlay) {
		// overlays.addAll(new VisitingMutator(classifier).mutate(context, blobsAfter));
		// }
		// stages.done("overlays");
		// debug.handle(new Metas(blobsBefore, blobsAfter));

		List<Mutator.Mutation> overlays = new ArrayList<Mutator.Mutation>();

		Layer layerBefore, layerAfter;

		if (overlays.isEmpty() == false) {
			// 6.1 locators: map widget hierarchy to locator model
			{
				layerBefore = layers.peek();

				Map<Locator, Blob> locatorToBlob = new IdentityHashMap<>();
				Map<Blob, Locator> blobToLocator = new IdentityHashMap<>();
				Root locators = classification.toLocator(context.after, blobsAfter, locatorToBlob, blobToLocator);

				for (Locator child : locators.childs) {
					response.add(new Script.Responses.Appears(toLinkedList(child)));
				}

				// debug
				debug.handle(new Locators(layerBefore.locators, locators, layerBefore.locatorToBlob, locatorToBlob));

				// push onto stack
				layerAfter = new Layer(Layer.Type.OVERLAY, after, region, blobsAfter, locators, locatorToBlob, blobToLocator);
			}
			stages.done("locators");
		} else {
			// blob hierarchy
			layerBefore = layers.pop();

			// 6.1.a hierarchy: updates
			{
				// occlusion
				region = layerBefore.occlusion;
				enlarged = RectUtil.enlargeRect(region, 3, 3, after.w, after.h);
				occlusion = ImageUtil.crop(after, enlarged);

				// classification (1): widgets
				blobsAfter = classification.toBlob(occlusion, stages);

				// classification (2): overlays
				for (Classifier classifier : classifiersOverlay) {
					new VisitingMutator(classifier).mutate(context, blobsAfter);
				}
			}
			stages.done("classification");

			// 6.1.b locators: map widget hierarchy to locator model
			{
				// locators
				Map<Locator, Blob> locatorToBlob = new IdentityHashMap<Locator, Blob>();
				Map<Blob, Locator> blobToLocator = new IdentityHashMap<>();
				Root locators = classification.toLocator(context.after, blobsAfter, locatorToBlob, blobToLocator);

				// debug
				debug.handle(new Locators(layerBefore.locators, locators, layerBefore.locatorToBlob, locatorToBlob));

				// determine delta to previous locator model (ui responses)
				response.addAll(org.testobject.kernel.pipeline.Locators.Diff.between(layerBefore.locators, locators));

				// push onto stack
				layerAfter = new Layer(layerBefore.type, after, region, blobsAfter, locators, locatorToBlob, blobToLocator);
			}
			stages.done("locators");
		}

		layers.push(layerAfter);

		stages.end();

		return new Result(new Images(before, after), new Action(request, response), new Metas(layerBefore.blobs, layerAfter.blobs),
				new Locators(layerBefore.locators, layerAfter.locators, layerBefore.locatorToBlob, layerAfter.locatorToBlob), new Diffs(
						deltas), target);
	}

	private boolean isOcclusionDisappeared(Rectangle.Int region) {
		return region.isEmpty();
	}

}
