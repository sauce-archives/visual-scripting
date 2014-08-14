package org.testobject.kernel.inference.interpreter;

import java.util.LinkedList;
import java.util.List;

import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.exceptions.Exceptions;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.classification.graph.Locator.Descriptor;
import org.testobject.kernel.api.classification.graph.Locator.Node;
import org.testobject.kernel.api.events.output.Events;
import org.testobject.kernel.api.events.output.Requests;
import org.testobject.kernel.api.events.output.Events.Event;
import org.testobject.kernel.api.events.output.Events.Mouse.ClickPoint;
import org.testobject.kernel.api.events.output.Requests.Click;
import org.testobject.kernel.api.events.output.Requests.ClickStrategy;
import org.testobject.kernel.api.events.output.Requests.Drag;
import org.testobject.kernel.api.events.output.Requests.Request;
import org.testobject.kernel.api.util.DescriptorUtil;
import org.testobject.kernel.api.util.LocatorUtil;
import org.testobject.kernel.classification.util.Find;
import org.testobject.kernel.classification.util.Find.Adapter;
import org.testobject.kernel.imaging.segmentation.Mask;
import org.testobject.kernel.inference.interpreter.Interpreter.Input;

/**
 * 
 * @author enijkamp
 *
 */
public class InputInference {

	private static final int MAX_CLICK_TIME = 500;
	private static final double MAX_CLICK_DISTANCE = 20;

	private final static long TIMEOUT_REQUEST_CLICK_MS = 10 * 1000;
	private final static long TIMEOUT_REQUEST_OTHER_MS = 3 * 1000;
	private final static long TIMEOUT_RESPONSE_BUFFER_MS = 5 * 1000;

	private static Adapter<Locator.Node> createAdapter(final Locator.Node node) {

		return new Adapter<Locator.Node>() {
			@Override
			public LinkedList<Locator.Node> at(Point.Int location) {

				LinkedList<Locator.Node> nodes = LocatorUtil.locate(node, location.x, location.y);

				SimpleInterpreter.log.debug("located '" + LocatorUtil.toPathString(nodes) + "' at position (" + location.x + ", "
						+ location.y + ")");

				return nodes;
			}

			@Override
			public List<Node> childs(Node parent) {
				return parent.getChildren();
			}

			@Override
			public Rectangle.Double toBox(Node node) {
				return toDoubleRect(LocatorUtil.union(LocatorUtil.toRectangles(DescriptorUtil.getMasks(node.getDescriptor()))));
			}

			@Override
			public Mask toMask(Node node) {
				return Mask.Builder.create(DescriptorUtil.getMasks(node.getDescriptor()));
			}

			private Rectangle.Double toDoubleRect(Rectangle.Int box) {
				return new Rectangle.Double(box.x, box.y, box.w, box.h);
			}

			@Override
			public boolean isGroup(Node node) {
				return DescriptorUtil.getMasks(node.getDescriptor()).size() > 1;
			}
		};
	}

	public static Request inferRequest(Event event, Input before) {

		Locator.Node locators = before.getLocators();

		if (event instanceof Events.Mouse) {
			Events.Mouse click = (Events.Mouse) event;

			if (isClickRequest(click.path)) {
				LinkedList<Descriptor> locator = find(locators, click.path.getLast().point.x, click.path.getLast().point.y);
				return createClickRequest(click, locator);
			} else {
				LinkedList<Descriptor> start = find(locators, click.path.getFirst().point.x, click.path.getFirst().point.y);
				LinkedList<Descriptor> end = find(locators, click.path.getLast().point.x, click.path.getLast().point.y);
				return createDragRequest(click, start, end);
			}
		}

		if (event instanceof Events.Type) {
			Events.Type type = (Events.Type) event;
			return new Requests.Type(type);
		}

		if (event instanceof Events.Device) {
			Events.Device generic = (Events.Device) event;

			return new Requests.Generic(generic.getDeviceEvent());
		}

		throw Exceptions.newUnsupportedTypeException("event", event.getClass());
	}

	public static long inferRequestTimeoutMs(Event event, Input before) {

		final long requestTimeoutMs = isClickRequest(event) ? TIMEOUT_REQUEST_CLICK_MS : TIMEOUT_REQUEST_OTHER_MS;

		return requestTimeoutMs;
	}

	public static long inferResponseTimeoutMs(Event event, Input before, Input after) {

		final long responseTimeoutMs = roundUp(greaterThanZero(after.getTimestamp() - event.getTimestamp()));

		return responseTimeoutMs;
	}

	private static boolean isClickRequest(Event event) {
		return event instanceof Events.Mouse;
	}

	private static Click createClickRequest(Events.Mouse click, LinkedList<Descriptor> locator) {
		return Requests.Factory.createClick(click, Locator.Qualifier.Factory.create(locator), ClickStrategy.LOCATOR);
	}

	private static Drag createDragRequest(Events.Mouse click, LinkedList<Descriptor> start, LinkedList<Descriptor> end) {
		return new Requests.Drag(click);
	}

	private static long greaterThanZero(long delay) {
		return delay < 0 ? 0 : delay;
	}

	private static long roundUp(long delay) {
		return ((withBuffer(delay) / 1000) + 1) * 1000;
	}

	private static long withBuffer(long delayMs) {
		return delayMs + TIMEOUT_RESPONSE_BUFFER_MS;
	}

	private static boolean isClickRequest(LinkedList<ClickPoint> path) {
		if (path.isEmpty()) {
			return true;
		}

		ClickPoint first = path.getFirst();
		long delaySum = 0;
		for (ClickPoint current : path) {
			delaySum += current.delay;
			if ((delaySum >= MAX_CLICK_TIME) || (Point.Int.distance(first.point, current.point) >= MAX_CLICK_DISTANCE)) {
				return false;
			}
		}

		return true;
	}

	private static LinkedList<Descriptor> find(org.testobject.kernel.api.classification.graph.Locator.Node locators, int x, int y) {
		LinkedList<Locator.Node> path = new Find<>(createAdapter(locators)).at(x, y).path;
		return LocatorUtil.toDescriptors(path);
	}
}