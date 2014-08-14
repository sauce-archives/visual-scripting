package org.testobject.kernel.inference.input;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.math.algebra.Point;
import org.testobject.kernel.api.events.Timestamp;
import org.testobject.kernel.api.events.input.MouseEvent;

public class MouseStateMachine {

	public interface Callback {

		void mouseEvent(MouseEvent.WithTimestamp event);

	}

	private static final Log log = LogFactory.getLog(MouseStateMachine.class);

	private final Callback callback;

	private MouseEvent.Type lastClickType = MouseEvent.Type.UP;

	public MouseStateMachine(Callback callback) {
		this.callback = callback;
	}

	public void putEvent(MouseEvent.WithTimestamp mouseEvent) {

		Timestamp timestamp = mouseEvent.getTimeStamp();
		MouseEvent.Type clickType = mouseEvent.type;
		Point.Int point = mouseEvent.point;

		if (log.isTraceEnabled()) {
			log.trace("pointerEvent(" + timestamp.getFramebufferTimestamp() + ", " + clickType + ", " + point.x + " ," + point.y + ")");
		}

		if (ignoreEvent(clickType)) {
			return;
		}

		MouseEvent.Type typeToFire = getClickType(clickType);

		this.lastClickType = clickType;

		callback.mouseEvent(new MouseEvent.WithTimestamp(timestamp, typeToFire, point));
	}

	private boolean ignoreEvent(MouseEvent.Type clickType) {
		return lastClickType == MouseEvent.Type.UP && clickType != MouseEvent.Type.DOWN;
	}

	private MouseEvent.Type getClickType(MouseEvent.Type clickType) {
		if (lastClickType == MouseEvent.Type.UP) {
			if (clickType == MouseEvent.Type.DOWN) {
				return MouseEvent.Type.DOWN;
			}
		} else {
			if (clickType == MouseEvent.Type.UP) {
				return MouseEvent.Type.UP;
			} else {
				return MouseEvent.Type.MOVE;
			}
		}

		throw new IllegalStateException("last click type = '" + lastClickType.name() + " and new click type = '" + clickType.name() + "'");
	}
}
