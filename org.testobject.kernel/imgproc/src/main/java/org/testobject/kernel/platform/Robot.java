package org.testobject.kernel.platform;

import java.awt.Point;
import java.awt.Rectangle;

import org.testobject.commons.util.lifecycle.Closable;


public interface Robot extends Closable{

	void keyPress(int keycode);
	
	void keyUp(int keycode);
	
	void keyDown(int keycode);
	
	void mouseUp(int x, int y);
	
	void mouseDown(int x, int y);
	
	void mouseClick(int x, int y);

	void mouseMove(int x, int y);
	
	void mouseDrag(int startx, int starty, int endx, int endy, int steps, long ms);
	
    Rectangle getInnerBounds(Window window);
    
    Rectangle getOuterBounds(Window window);
    
    void raiseWindow(Window window);
    
    void moveWindow(Window window, int x, int y);

    Point getMousePosition();

}
