package org.testobject.kernel.inputs;

import static org.testobject.commons.bus.Register.registerHandler;
import static org.testobject.commons.events.ButtonMask.ButtonLeft;
import static org.testobject.commons.events.ButtonMask.None;

import javax.inject.Inject;

import org.testobject.commons.bus.EventBus;
import org.testobject.commons.bus.Registration;
import org.testobject.commons.events.ButtonMask;
import org.testobject.commons.events.PointerClickEvent;
import org.testobject.commons.events.PointerClickEvent.ClickType;
import org.testobject.commons.events.PointerEvent;

// FIXME send two-fold begin event & end event (drag or click) (en)
// FIXME determine click if ~50ms passed between begin and end (en)
public class PointerStateMachine implements PointerEvent.Handler {

	public enum States {
		NONE, PRESSED, DRAG
	};

	private final EventBus bus;
	private States state = States.NONE;

	@Inject
	public PointerStateMachine(EventBus bus) {
		this.bus = bus;
	}

	@Override
	public void pointerEvent(long timestamp, ButtonMask buttonMask, int x, int y) {
		ClickType clickType = null;
		if (ButtonLeft == buttonMask) {
			if (States.NONE == state) {
				state = States.PRESSED;
				clickType = ClickType.DOWN;
			} else if (States.PRESSED == state || States.DRAG == state) {
				state = States.DRAG;
				clickType = ClickType.DRAG;
			}
		} else if (None == buttonMask) {
			if (States.PRESSED == state) {
				state = States.NONE;
				clickType = ClickType.UP;
			} else if (States.DRAG == state) {
				state = States.NONE;
				clickType = ClickType.DRAG_END;
			}
		}

		if (clickType != null) {
			bus.fireEvent(PointerClickEvent.class, new PointerClickEventImpl(clickType, buttonMask, x, y, timestamp));
		}
	}

	public Registration register(Class<PointerClickEvent> clazz, PointerClickEvent.Handler handler) {
		return registerHandler(bus, clazz, handler);
	}
}
