package org.testobject.kernel.inputs;

import org.testobject.commons.events.ButtonMask;
import org.testobject.commons.events.PointerClickEvent;

public class PointerClickEventImpl implements PointerClickEvent {

	private ClickType clickType;
	private ButtonMask buttonMask;
	private int x;
	private int y;
	private long timestamp;

	public PointerClickEventImpl(ClickType clickType, ButtonMask buttonMask, int x, int y, long timestamp) {
		this.clickType = clickType;
		this.buttonMask = buttonMask;
		this.x = x;
		this.y = y;
		this.timestamp = timestamp;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public void dispatch(Handler handler) {
		handler.pointerEvent(timestamp, clickType, buttonMask, x, y);
	}
}
