package org.testobject.kernel.api.events.output;

import java.util.LinkedList;

import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Size;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.classification.graph.Locator.Descriptor;
import org.testobject.kernel.api.events.input.DeviceEvent;
import org.testobject.kernel.api.util.DescriptorUtil;

import com.google.common.base.Preconditions;

/**
 * 
 * @author enijkamp
 *
 */
public interface Requests {

	class Factory {

		public static Click createClick(Events.Mouse click, Locator.Qualifier locator,
				ClickStrategy clickStrategy) {
			return new Requests.Click(click, locator, getAbsoluteOffset(click, locator.getPath()),
					getRelativeOffset(click, locator.getPath()), clickStrategy);
		}

		public static Point.Int getAbsoluteOffset(Events.Mouse click, LinkedList<Descriptor> locator) {

			Preconditions.checkArgument(click.path.size() > 0);

			final int clickX = click.path.getLast().point.x;
			final int clickY = click.path.getLast().point.y;

			Point.Int position = DescriptorUtil.getPosition(locator.getLast());
			final int locatorX = position.x;
			final int locatorY = position.y;

			final int clickOffsetX = (clickX - locatorX);
			final int clickOffsetY = (clickY - locatorY);

			return new Point.Int(clickOffsetX, clickOffsetY);
		}

		public static Point.Double getRelativeOffset(Events.Mouse click, LinkedList<Descriptor> locator) {

			Preconditions.checkArgument(click.path.size() > 0);

			final int clickX = click.path.getLast().point.x;
			final int clickY = click.path.getLast().point.y;

			Point.Int position = DescriptorUtil.getPosition(locator.getLast());
			final int locatorX = position.x;
			final int locatorY = position.y;

			Size.Int size = DescriptorUtil.getSize(locator.getLast());
			final double locatorW = size.w;
			final double locatorH = size.h;

			final double clickOffsetX = (clickX - locatorX);
			final double clickOffsetY = (clickY - locatorY);

			final double clickRelativeX = clickOffsetX / locatorW;
			final double clickRelativeY = clickOffsetY / locatorH;

			return new Point.Double(clickRelativeX, clickRelativeY);
		}
	}

	interface Request extends Event {

	}

	enum ClickStrategy {
		LOCATOR, POSITION
	}

	class Generic implements Requests.Request {

		public final DeviceEvent event;

		public Generic(DeviceEvent event) {
			this.event = event;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "()";
		}
	}

	class Click implements Requests.Request {

		public final Events.Mouse click;
		public final Locator.Qualifier locator;
		public final Point.Int localAbsoluteOffset;
		public final Point.Double localRelativeOffset;
		public final ClickStrategy clickStrategy;

		public Click(Events.Mouse click, Locator.Qualifier locator, Point.Int localAbsoluteOffset,
				Point.Double localRelativeOffset, ClickStrategy clickStrategy) {
			this.click = click;
			this.locator = locator;
			this.localAbsoluteOffset = localAbsoluteOffset;
			this.localRelativeOffset = localRelativeOffset;
			this.clickStrategy = clickStrategy;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "(" + Util.toString(locator) + ", " + localRelativeOffset + ")";
		}
	}

	class Drag implements Requests.Request {

		public final Events.Mouse click;

		public Drag(Events.Mouse click) {
			this.click = click;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "(" + click.path.getFirst() + ", " + click.path.getLast() + ")";
		}
	}

	class Type implements Requests.Request {
		
		public final Events.Type type;

		public Type(Events.Type type) {
			this.type = type;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "('" + toInputString() + "')";
		}

		public String toInputString() {
			String string = "";
			for (Events.Type.Key key : type.keys) {
				if (key.downFlag) {
					string += (char) key.key;
				}
			}
			return string;
		}
		
		public int getAction() {
			return type.keys.get(0).key;
		}

		public int[] getInput() {
			int[] keys = new int[type.keys.size()];
			int i = 0;
			for (Events.Type.Key key : type.keys) {
				keys[i++] = key.key;
			}

			return keys;
		}

		public boolean isControlKey() {
			return type.keys.size() == 1 ? type.keys.get(0).controlKey : false;
		}
	}
}