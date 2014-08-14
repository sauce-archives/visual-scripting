package org.testobject.kernel.replay.impl;

import org.testobject.commons.events.Orientation;
import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.exceptions.Exceptions;
import org.testobject.commons.util.io.Closeables;
import org.testobject.kernel.platform.robot.DeviceRobot;
import org.testobject.kernel.platform.robot.InputRobot;

/**
 * @author enijkamp
 */
public class ReplayInputRobot {

	private final InputRobot inputRobot;
	private final DeviceRobot deviceRobot;

	public ReplayInputRobot(InputRobot inputRobot, DeviceRobot deviceRobot) {
		this.inputRobot = inputRobot;
		this.deviceRobot = deviceRobot;
	}

	public void close() {
		Closeables.close(inputRobot);
	}

	//	public InputRobot input(final Replay.Executor.Callback callback) {
//		return new InputRobot() {
//			@Override
//			public void keyPress(int keycode) {
//				inputRobot.keyPress(keycode);
//			}
//			
//			@Override
//			public void keyUp(int keycode) {
//				inputRobot.keyUp(keycode);
//			}
//			
//			@Override
//			public void keyDown(int keycode) {
//				inputRobot.keyDown(keycode);
//			}
//			
//			@Override
//			public void mouseUp(int x, int y) {
//				callback.clickAt(transform(x, y));
//				inputRobot.mouseUp(x, y);
//			}
//			
//			@Override
//			public void mouseDown(int x, int y) {
//				callback.clickAt(transform(x, y));
//				inputRobot.mouseDown(x, y);
//			}
//			
//			@Override
//			public void mouseMove(int x, int y) {
//				callback.clickAt(transform(x, y));
//				inputRobot.mouseMove(x, y);
//			}
//			
//			@Override
//			public Point.Int getMousePosition() {
//				return inputRobot.getMousePosition();
//			}
//			
//			@Override
//			public void close() {
//				inputRobot.close();
//			}
//			
//			@Override
//			public Rectangle.Int getBounds() {
//				return inputRobot.getBounds();
//			}
//		};
//	}
	
}
