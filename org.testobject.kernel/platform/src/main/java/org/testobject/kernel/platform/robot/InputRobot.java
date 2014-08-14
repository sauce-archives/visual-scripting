package org.testobject.kernel.platform.robot;

import java.io.Closeable;

import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.math.algebra.Rectangle.Int;

/**
 * 
 * @author enijkamp
 *
 */
public interface InputRobot extends Closeable {

	void keyPress(int keycode);

	void keyUp(int keycode);

	void keyDown(int keycode);

	void mouseUp(int x, int y);

	void mouseDown(int x, int y);

	void mouseMove(int x, int y);

	void mouseDrag(int[][] points);
	
	Rectangle.Int getBounds();

	Point.Int getMousePosition();

	class Factory {
		public static InputRobot mock() {
			return new InputRobot() {

				@Override
				public void close() {}

				@Override
				public void keyPress(int keycode) {}

				@Override
				public void keyUp(int keycode) {}

				@Override
				public void keyDown(int keycode) {}

				@Override
				public void mouseUp(int x, int y) {}

				@Override
				public void mouseDown(int x, int y) {}

				@Override
				public void mouseMove(int x, int y) {}

				@Override
				public Int getBounds() {
					return new Int();
				}

				@Override
				public org.testobject.commons.math.algebra.Point.Int getMousePosition() {
					return new org.testobject.commons.math.algebra.Point.Int(0, 0);
				}

				@Override
				public void mouseDrag(int[][] points) {}
			};
		}

		public static InputRobot failingRobot() {
			return new InputRobot() {

				@Override
				public void close() {}

				@Override
				public void keyPress(int keycode) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void keyUp(int keycode) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void keyDown(int keycode) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void mouseUp(int x, int y) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void mouseDown(int x, int y) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void mouseMove(int x, int y) {
					throw new UnsupportedOperationException();
				}

				@Override
				public Int getBounds() {
					throw new UnsupportedOperationException();
				}

				@Override
				public org.testobject.commons.math.algebra.Point.Int getMousePosition() {
					throw new UnsupportedOperationException();
				}

				@Override
				public void mouseDrag(int[][] points) {
					throw new UnsupportedOperationException();					
				}
			};
		}
	}

}
