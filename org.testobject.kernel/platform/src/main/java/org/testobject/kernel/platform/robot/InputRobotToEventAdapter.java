package org.testobject.kernel.platform.robot;

import org.testobject.commons.bus.EventBus;
import org.testobject.commons.bus.Register;
import org.testobject.commons.bus.Registration;
import org.testobject.commons.events.KeyEvent;
import org.testobject.commons.events.PointerClickEvent.ClickType;
import org.testobject.commons.events.TimestampEvent.Timestamp;
import org.testobject.commons.events.PointerEvent;
import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.io.Closeables;
import org.testobject.commons.util.thread.ThreadUtil;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * 
 * @author enijkamp
 *
 */
// FIXME refactor - separation of concerns - should not implement Handler (en)
public class InputRobotToEventAdapter implements InputRobot, PointerEvent.Handler, KeyEvent.Handler {

	public interface Factory {
		InputRobotToEventAdapter create(InputRobot robot);
	}

	private final EventBus bus;
	private final InputRobot robot;

	@AssistedInject
	public InputRobotToEventAdapter(EventBus bus, @Assisted InputRobot robot) {
		this.bus = bus;
		this.robot = robot;
	}

	public Registration register(Class<PointerEvent> clazz, PointerEvent.Handler handler) {
		return Register.registerHandler(bus, clazz, handler);
	}

	public Registration register(Class<KeyEvent> clazz, KeyEvent.Handler handler) {
		return Register.registerHandler(bus, clazz, handler);
	}

	// FIXME these methods are used by the vnc server (recording) (en)
	
	@Override
	public void keyEvent(Timestamp timestamp, int key, boolean controlKey, boolean downFlag) {
		bus.fireEvent(KeyEvent.Factory.create(timestamp, key, controlKey, downFlag));
	}

	@Override
	public void pointerEvent(Timestamp timestamp, ClickType clickType, int x, int y) {
		bus.fireEvent(PointerEvent.Factory.create(timestamp, clickType, x, y));
	}
	
	// FIXME these methods are used by replay executor (en)

	@Override
	public void keyPress(int keycode) {
		long now = System.currentTimeMillis();
		Timestamp timestamp = new Timestamp(now, now, now);
		bus.fireEvent(KeyEvent.class, KeyEvent.Factory.create(timestamp, keycode, false, true));
		ThreadUtil.sleep(10);
		bus.fireEvent(KeyEvent.class, KeyEvent.Factory.create(timestamp, keycode, false, false));
	}

	@Override
	public void keyDown(int keycode) {
		long now = System.currentTimeMillis();
		Timestamp timestamp = new Timestamp(now, now, now);
		bus.fireEvent(KeyEvent.class, KeyEvent.Factory.create(timestamp, keycode, false, true));
	}

	@Override
	public void keyUp(int keycode) {
		long now = System.currentTimeMillis();
		Timestamp timestamp = new Timestamp(now, now, now);
		bus.fireEvent(KeyEvent.class, KeyEvent.Factory.create(timestamp, keycode, false, false));
	}

	@Override
	public void mouseDown(int x, int y) {
		long now = System.currentTimeMillis();
		Timestamp timestamp = new Timestamp(now, now, now);
		bus.fireEvent(PointerEvent.Factory.create(timestamp, ClickType.DOWN, x, y));
	}

	@Override
	public void mouseUp(int x, int y) {
		long now = System.currentTimeMillis();
		Timestamp timestamp = new Timestamp(now, now, now);
		bus.fireEvent(PointerEvent.Factory.create(timestamp, ClickType.UP, x, y));
	}

	@Override
	public void mouseMove(int x, int y) {
		long now = System.currentTimeMillis();
		Timestamp timestamp = new Timestamp(now, now, now);
		bus.fireEvent(PointerEvent.Factory.create(timestamp, ClickType.MOVE, x, y));
	}

	@Override
	public Rectangle.Int getBounds() {
		return robot.getBounds();
	}

	@Override
	public Point.Int getMousePosition() {
		return robot.getMousePosition();
	}

	@Override
	public void close() {
		Closeables.close(robot);
	}

	@Override
	public void mouseDrag(int[][] points) {
		// TODO Auto-generated method stub
		
	}
}