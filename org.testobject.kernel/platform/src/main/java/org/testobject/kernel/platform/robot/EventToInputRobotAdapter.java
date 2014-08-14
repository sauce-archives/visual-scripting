package org.testobject.kernel.platform.robot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.events.KeyEvent;
import org.testobject.commons.events.PointerClickEvent;
import org.testobject.commons.events.PointerClickEvent.ClickType;
import org.testobject.commons.events.TimestampEvent.Timestamp;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class EventToInputRobotAdapter implements KeyEvent.Handler, PointerClickEvent.Handler {

	public static interface Factory {
		EventToInputRobotAdapter create(InputRobot robot);
	}

	private final Log log = LogFactory.getLog(EventToInputRobotAdapter.class);
	private final InputRobot robot;

	@AssistedInject
	public EventToInputRobotAdapter(@Assisted InputRobot robot) {
		this.robot = robot;
	}

	@Override
	public void keyEvent(Timestamp timestamp, int key, boolean hardwareButton, boolean down) {
		if(log.isDebugEnabled()) {
			log.debug("keyEvent(" + key + "," + down + ")");
		}
		if (down) {
			robot.keyDown(key);
		} else {
			robot.keyUp(key);
		}
	}

	@Override
	public void pointerEvent(Timestamp timestamp, ClickType clickType, int x, int y) {
		if(log.isDebugEnabled()) {
			log.debug("pointerEvent(" + clickType + "," + x + "," + y + ")");
		}
		switch (clickType) {
		case DOWN:
			robot.mouseDown(x, y);
			return;
		case UP:
			robot.mouseUp(x, y);
			return;
		case MOVE:
			robot.mouseMove(x, y);
			return;
		}
	}
}