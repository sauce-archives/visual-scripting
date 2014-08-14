//package org.testobject.kernel.inference.input;
//
//import java.util.Collections;
//
//import org.junit.Test;
//import org.mockito.Mockito;
//import org.testobject.commons.bus.EventBus;
//import org.testobject.commons.events.ButtonMask;
//import org.testobject.commons.events.PointerClickEvent.ClickType;
//import org.testobject.commons.math.algebra.Rectangle;
//import org.testobject.commons.util.image.Image;
//
//public class InputFiniteStateMachineTest {
//	
//	@Test
//	public void test() throws InterruptedException {
//		EventBus bus = Mockito.mock(EventBus.class);
//		InputFiniteStateMachine ism = new InputFiniteStateMachine(bus, 2000, 500);
//		
//		long now = System.currentTimeMillis();
//		ism.updateFrameBuffer(now, now, (Image.Int) null, Collections.<Rectangle.Int>emptyList());
//		ism.pointerEvent(now, now, ClickType.DOWN, ButtonMask.ButtonLeft, 0, 0);
//		ism.pointerEvent(now, now, ClickType.UP, ButtonMask.ButtonLeft, 0, 0);
//		ism.updateFrameBuffer(now, now, (Image.Int) null, Collections.<Rectangle.Int>emptyList());
//	
//		Thread.sleep(2000);
//	}
//
//}
