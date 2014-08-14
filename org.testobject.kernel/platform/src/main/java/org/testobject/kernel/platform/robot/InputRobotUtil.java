package org.testobject.kernel.platform.robot;

import java.util.LinkedList;

import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.util.thread.ThreadUtil;
import org.testobject.kernel.api.events.output.Events.Mouse.ClickPoint;

public class InputRobotUtil {

	private static final long DEFAULT_CLICK_SLEEP_TIME = 50;

	public static void click(InputRobot robot, int x, int y) {
		robot.mouseDown(x, y);
		ThreadUtil.sleep(DEFAULT_CLICK_SLEEP_TIME);
		robot.mouseUp(x, y);
		ThreadUtil.sleep(DEFAULT_CLICK_SLEEP_TIME);
	}

	public static void click(InputRobot robot, Point.Int point) {
		click(robot, point.x, point.y);
	}

	public static void drag(InputRobot inputRobot, Point.Int from, Point.Int to) {
		drag(inputRobot, from, to, 20);
	}

	public static void drag(InputRobot inputRobot, Point.Int from, Point.Int to, int step) {
		final int distance = (int) Point.Int.distance(from, to);
		final float steps = distance / step;
		final float dx = (to.x - from.x) / steps;
		final float dy = (to.y - from.y) / steps;

		inputRobot.mouseDown(to.x, to.y);

		float x = from.x, y = from.y;
		for (int i = 0; i < steps; i++)
		{
			ThreadUtil.sleep(DEFAULT_CLICK_SLEEP_TIME);
			x += dx;
			y += dy;
			inputRobot.mouseMove((int) x, (int) y);
		}

		inputRobot.mouseUp(to.x, to.y);
	}

	public static void click(InputRobot inputRobot, LinkedList<ClickPoint> path) {
		if (path.isEmpty()) {
			return;
		}

		// down
		{
			ClickPoint first = path.getFirst();
			inputRobot.mouseDown(first.point.x, first.point.y);
		}

		// move
		{
			for (int i = 1; i < (path.size() - 1); i++) {
				ClickPoint current = path.get(i);
				ThreadUtil.sleep(current.delay);
				inputRobot.mouseMove(current.point.x, current.point.y);
			}
		}

		// up
		{
			ClickPoint last = path.getLast();
			ThreadUtil.sleep(last.delay);
			inputRobot.mouseUp(last.point.x, last.point.y);
		}
	}

}
