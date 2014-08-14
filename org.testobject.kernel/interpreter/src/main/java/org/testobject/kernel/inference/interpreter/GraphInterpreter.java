//package org.testobject.kernel.inference.interpreter;
//
//import java.util.LinkedList;
//import java.util.List;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.testobject.commons.math.algebra.Point;
//import org.testobject.commons.math.algebra.Rectangle;
//import org.testobject.commons.util.collections.Lists;
//import org.testobject.kernel.classification.graph.Locator;
//import org.testobject.kernel.classification.graph.Locator.Descriptor;
//import org.testobject.kernel.classification.graph.Locator.Node;
//import org.testobject.kernel.classification.util.Find;
//import org.testobject.kernel.classification.util.Find.Adapter;
//import org.testobject.kernel.classification.util.LocatorUtil;
//import org.testobject.kernel.imaging.diff.ImageComparator;
//import org.testobject.kernel.imaging.segmentation.Mask;
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
//public class GraphInterpreter implements Interpreter {
//
//	public static final Log log = LogFactory.getLog(GraphInterpreter.class);
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
//	private final Stages stages;
//	private final Tie tie;
//
//	public GraphInterpreter(Stages stages, Tie tie) {
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
//		// 1. script: infer user interactions (on before screen)
//		Requests.Request request = InputInference.infer(event, before.getLocators());
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
//		// 3. disappear
//		for (Locator.Node child : before.getLocators().getChildren()) {
//			response.add(new Responses.Disappears(Locator.Qualifier.Factory.create(child.getDescriptor())));
//		}
//		stages.done("disappear");
//		
//		// 4. appear
//		for (Locator.Node child : after.getLocators().getChildren()) {
//			response.add(new Responses.Appears(Locator.Qualifier.Factory.create(child.getDescriptor())));
//		}
//		stages.done("appear");
//
//		return Inference.Factory.create(Damages.Factory.create(damages), Interaction.Factory.create(request, response), toPass(before), toPass(after));
//	}
//
//	private Pass toPass(Input input) {
//		return Pass.Factory.create(input.getRaw(), input.getLocators());
//	}
//}
