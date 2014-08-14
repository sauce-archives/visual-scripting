//package org.testobject.kernel.inference.interpreter;
//
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Stack;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.testobject.commons.math.algebra.Point;
//import org.testobject.commons.math.algebra.Rectangle;
//import org.testobject.commons.util.collections.Lists;
//import org.testobject.commons.util.image.Image;
//import org.testobject.commons.util.image.ImageUtil;
//import org.testobject.kernel.classification.graph.Locator;
//import org.testobject.kernel.classification.graph.Locator.Descriptor;
//import org.testobject.kernel.classification.graph.Locator.Node;
//import org.testobject.kernel.classification.util.Find;
//import org.testobject.kernel.classification.util.Find.Adapter;
//import org.testobject.kernel.classification.util.LocatorUtil;
//import org.testobject.kernel.imaging.diff.ImageComparator;
//import org.testobject.kernel.imaging.segmentation.Mask;
//import org.testobject.kernel.inference.occlusion.OcclusionTracker;
//import org.testobject.kernel.inference.script.Events;
//import org.testobject.kernel.inference.script.Events.Event;
//import org.testobject.kernel.inference.script.Requests;
//import org.testobject.kernel.inference.script.Requests.Request;
//import org.testobject.kernel.inference.script.Responses;
//
///**
// * 
// * @author enijkamp
// *
// */
//// TODO re-implement occlusion tracking, partial classification and overlay-detection (see old FullScanPipeline) (en)
//public class OcclusionInterpreter implements Interpreter {
//
//	public static final Log log = LogFactory.getLog(OcclusionInterpreter.class);
//
//	public static class RectUtil {
//
//		public static Rectangle.Int expand(Rectangle.Int rect, int fatX, int fatY, int maxW, int maxH) {
//			int x = rect.x - fatX;
//			x = x < 0 ? 0 : x;
//
//			int y = rect.y - fatY;
//			y = y < 0 ? 0 : y;
//
//			int w = rect.w + (fatX * 2);
//			w = w + x > maxW ? maxW - x : w;
//
//			int h = rect.h + (fatY * 2);
//			h = h + y > maxH ? maxH - y : h;
//
//			return new Rectangle.Int(x, y, w, h);
//		}
//
//		public static Rectangle.Int union(List<Rectangle.Int> rects) {
//			Rectangle.Int rect = rects.get(0);
//			for (int i = 1; i < rects.size(); i++) {
//				rect = rect.union(rects.get(i));
//			}
//			return rect;
//		}
//	}
//
//	private static class Layer {
//		public enum Type {
//			OVERLAY, UPDATE
//		}
//
//		final Type type;
//		final Image.Int image;
//		final Rectangle.Int occlusion;
//		final Locator.Node locators;
//
//		public Layer(Type type, Image.Int image, Rectangle.Int occlusion, Locator.Node locators) {
//			this.type = type;
//			this.image = image;
//			this.occlusion = occlusion;
//			this.locators = locators;
//		}
//	}
//
//	private static class InputInference {
//
//		private static Adapter<Locator.Node> createAdapter(final Locator.Node root) {
//			return new Adapter<Locator.Node>() {
//				@Override
//				public LinkedList<Locator.Node> at(Point.Int location) {
//					return LocatorUtil.locate(root, location.x, location.y);
//				}
//
//				@Override
//				public List<Locator.Node> childs(Locator.Node parent) {
//					return parent.getChildren();
//				}
//
//				@Override
//				public Rectangle.Double toBox(Locator.Node node) {
//					return toDoubleRect(Mask.Builder.create(LocatorUtil.getMaskFeature(node).getValue()).getBoundingBox());
//				}
//
//				@Override
//				public Mask toMask(Locator.Node node) {
//					return Mask.Builder.create(LocatorUtil.getMaskFeature(node).getValue());
//				}
//
//				private Rectangle.Double toDoubleRect(Rectangle.Int box) {
//					return new Rectangle.Double(box.x, box.y, box.w, box.h);
//				}
//				
//				@Override
//				public boolean isGroup(Node t) {
//					// FIXME implement (en)
//					return false;
//				}
//			};
//		}
//
//		public static Request infer(Event event, Locator.Node locators) {
//
//			if (event instanceof Events.Click) {
//
//				Events.Click click = (Events.Click) event;
//				Locator.Qualifier qualifier = Locator.Qualifier.Factory.create(find(locators, click.x, click.y));
//
//				return new Requests.ClickOn(click, qualifier);
//			}
//
//			if (event instanceof Events.Type) {
//				Events.Type type = (Events.Type) event;
//				return new Requests.Type(type);
//			}
//
//			if (event instanceof Events.Wait) {
//				return new Requests.WaitFor();
//			}
//
//			throw new IllegalArgumentException("Unsupported event type '" + event.getClass() + "'");
//		}
//
//		private static LinkedList<Descriptor> find(org.testobject.kernel.classification.graph.Locator.Node locators, int x, int y) {
//			LinkedList<Locator.Node> path = new Find<Locator.Node>(createAdapter(locators)).at(x, y).path;
//			return LocatorUtil.toDescriptors(path);
//		}
//
//	}
//
//	private final Stack<Layer> layers = new Stack<>();
//	private final OcclusionTracker occlusionTracker;
//	private final Stages stages;
//	private final Tie tie;
//
//	public OcclusionInterpreter(OcclusionTracker occlusionTracker, Stages stages, Tie tie) {
//		this.occlusionTracker = occlusionTracker;
//		this.stages = stages;
//		this.tie = tie;
//	}
//
//	@Override
//	public Inference interpret(Event event, Input before, Input after) {
//
//		List<Responses.Response> response = Lists.newLinkedList();
//
//		stages.start();
//
//		// 0. init: create dummy occlusion and blobs
//		if (layers.isEmpty()) {
//			// occlusion
//			Rectangle.Int occlusion = new Rectangle.Int(0, 0, before.getRaw().w, before.getRaw().h);
//
//			// push onto stack
//			layers.push(new Layer(Layer.Type.UPDATE, before.getRaw(), occlusion, before.getLocators()));
//
//			stages.done("init");
//		}
//
//		// 1. script: infer user interactions (on before screen)
//		Requests.Request request = InputInference.infer(event, layers.peek().locators);
//		stages.done("interactions");
//
//		// 2. damages: regions of change
//		int tolerance = 5;
//		List<Rectangle.Int> damages = ImageComparator.compare(before.getRaw(), after.getRaw(), tolerance);
//		tie.handle(Damages.Factory.create(damages));
//		if (damages.isEmpty()) {
//			return Inference.Factory.none();
//		}
//		stages.done("damages");
//
//		// 3. occlusions: relevant regions
//		Rectangle.Int delta = RectUtil.union(damages);
//		Rectangle.Int region = occlusionTracker.process(before.getRaw(), after.getRaw(), delta);
//		if (isOcclusionDisappeared(region)) {
//			/*
//			* consider:
//			* - if we have a tab-panel with two tabs A and B, A is active
//			* - click on B, click on A
//			* - occlusion tracker tracks B as occlusion
//			* - with click on A initial state is restored and returns region.isEmpty()
//			* thus:
//			* - we have to differentiate between overlays and updates
//			*/
//
//			// is occlusion an overlay
//			Layer layer = layers.peek();
//			if (layer.type == Layer.Type.OVERLAY) {
//				// occlusion disappeared
//				layers.pop();
//
//				// disappears event
//				for (Locator.Node child : layer.locators.getChildren()) {
//					response.add(new Responses.Disappears(Locator.Qualifier.Factory.create(child.getDescriptor())));
//				}
//
//				stages.end();
//				return Inference.Factory.create(Damages.Factory.create(damages), Interaction.Factory.create(request, response),
//						toPass(before), toPass(after));
//			} else {
//				// if occlusion is not an overlay, continue with original delta
//				region = delta;
//			}
//		}
//		Rectangle.Int expanded = RectUtil.expand(region, 3, 3, after.getRaw().w, after.getRaw().h);
//		Image.Int occlusion = ImageUtil.Cut.crop(after.getRaw(), expanded);
//		stages.done("occlusions");
//		
//		// TODO implement, see remaining code in old FullScanPipeline (en)
//
//		return null;
//	}
//
//	private Pass toPass(Input input) {
//		return Pass.Factory.create(input.getRaw(), input.getLocators());
//	}
//
//	private boolean isOcclusionDisappeared(Rectangle.Int region) {
//		return region.isEmpty();
//	}
//}
