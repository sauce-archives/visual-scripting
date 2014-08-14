package org.testobject.kernel.platform;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.events.ButtonMask;
import org.testobject.commons.events.KeyEvent;
import org.testobject.commons.events.PointerClickEvent;
import org.testobject.commons.events.PointerClickEvent.ClickType;

/**
 * 
 * @author enijkamp
 *
 */
public class InputEventRobotAdapter implements KeyEvent.Handler, PointerClickEvent.Handler {

	public static class Factory {
		public InputEventRobotAdapter create(Robot robot) {
			return new InputEventRobotAdapter(robot);
		}
	}

	private final Log log = LogFactory.getLog(InputEventRobotAdapter.class);
	private final Robot robot;

	private InputEventRobotAdapter(Robot robot) {
		this.robot = robot;
	}

	@Override
	public void keyEvent(long timestamp, int key, boolean hardwareButton, boolean down) {
		log.info("keyEvent(" + key + "," + down + ")");
		if (down) {
			robot.keyDown(key);
		} else {
			robot.keyUp(key);
		}
	}

	@Override
	public void pointerEvent(long timestamp, ClickType clickType, ButtonMask buttonMask, int x, int y) {
		switch (clickType) {
		case DOWN:
			robot.mouseDown(x, y);
			break;
		case UP:
			robot.mouseUp(x, y);
			break;
		case DRAG:
			robot.mouseMove(x, y);
			break;
		case DRAG_END:
			robot.mouseUp(x, y);
			break;
		default:
			break;
		}

	}
}