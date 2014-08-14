package org.testobject.kernel.api.events.input;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.testobject.commons.events.PointerClickEvent;
import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.util.exceptions.Exceptions;
import org.testobject.kernel.api.events.Timestamp;

public class MouseEvent implements Event {

	public enum Type {
		DOWN, MOVE, UP, DRAG;
		public static Type toType(PointerClickEvent.ClickType type) {
			switch (type) {
			case DOWN:
				return Type.DOWN;
			case MOVE:
				return Type.MOVE;
			case UP:
				return Type.UP;
			default:
				throw Exceptions.newUnsupportedTypeException(type.name(), PointerClickEvent.ClickType.class);
			}
		}
	};

	public static class WithTimestamp extends MouseEvent implements Timestamp.Event {

		private final Timestamp timestamp;

		public WithTimestamp(Timestamp timestamp, MouseEvent event) {
			super(event.type, event.point);
			this.timestamp = timestamp;
		}

		public WithTimestamp(Timestamp timestamp, Type type, Point.Int point) {
			super(type, point);
			this.timestamp = timestamp;
		}

		@Override
		public Timestamp getTimeStamp() {
			return timestamp;
		}

		@Override
		public String toString() {
			return "mouse(time = " + timestamp + ", type = " + this.type + ", point = " + this.point + ")";
		}

	}

	public final Type type;
	public final Point.Int point;

	@JsonCreator
	public MouseEvent(@JsonProperty("type") Type type, @JsonProperty("point") Point.Int point) {
		this.type = type;
		this.point = point;
	}

	@Override
	public String toString() {
		return type.name() + " " + point.toString();
	}

}