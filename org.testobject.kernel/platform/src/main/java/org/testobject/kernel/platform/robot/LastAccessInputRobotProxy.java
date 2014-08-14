package org.testobject.kernel.platform.robot;

import java.io.IOException;

import org.testobject.commons.math.algebra.Rectangle.Int;

/**
 * 
 * @author enijkamp
 *
 */
public class LastAccessInputRobotProxy implements InputRobot, RemembersLastAccess {

	private final InputRobot robot;

	private long lastAccess = now();

	public LastAccessInputRobotProxy(InputRobot robot) {
		this.robot = robot;
	}

	private long now() {
		return System.currentTimeMillis();
	}

	@Override
	public void close() throws IOException {
		robot.close();
	}

	@Override
	public void keyPress(int keycode) {
		access();
		robot.keyPress(keycode);
	}

	@Override
	public void keyUp(int keycode) {
		access();
		robot.keyUp(keycode);
	}

	@Override
	public void keyDown(int keycode) {
		access();
		robot.keyDown(keycode);
	}

	@Override
	public void mouseUp(int x, int y) {
		access();
		robot.mouseUp(x, y);
	}

	@Override
	public void mouseDown(int x, int y) {
		access();
		robot.mouseDown(x, y);
	}

	@Override
	public void mouseMove(int x, int y) {
		access();
		robot.mouseMove(x, y);
	}

	@Override
	public void mouseDrag(int[][] points) {
		access();
		robot.mouseDrag(points);
	}

	@Override
	public Int getBounds() {
		return robot.getBounds();
	}

	@Override
	public org.testobject.commons.math.algebra.Point.Int getMousePosition() {
		return robot.getMousePosition();
	}

	private void access() {
		this.lastAccess = now();
	}

	@Override
	public long getLastAccessTime() {
		return lastAccess;
	}

}
