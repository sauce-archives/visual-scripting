package org.testobject.kernel.inputs;

import static org.testobject.commons.bus.Register.registerHandler;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.bus.EventBus;
import org.testobject.commons.bus.Registration;
import org.testobject.commons.events.ButtonMask;
import org.testobject.commons.events.FrameBufferUpdateEvent;
import org.testobject.commons.events.KeyEvent;
import org.testobject.commons.events.PointerClickEvent;
import org.testobject.commons.events.PointerClickEvent.ClickType;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.inputs.InputStateMachine.State.Key;
import org.testobject.kernel.inputs.InputStateMachine.State.Type;
import org.testobject.kernel.script.api.Events;
import org.testobject.kernel.script.api.Events.Click;

/**
 * This component listens for ClickEvent and RepaintEvent and emits events with indicate a state change.
 * 
 * @author enijkamp
 * 
 */
public class InputStateMachine implements KeyEvent.Handler, PointerClickEvent.Handler, FrameBufferUpdateEvent.Handler {
	private static final Log log = LogFactory.getLog(InputStateMachine.class);

	public enum States {
		INIT, BEFORE, CLICK, KEY, WAITING, AFTER
	}

	public class TimestampedFrameBuffer {
		public long timestamp;
		public Image.Int framebuffer;

		public TimestampedFrameBuffer(long timestamp, Image.Int framebuffer) {
			this.timestamp = timestamp;
			this.framebuffer = framebuffer;
		}

		@Override
		public TimestampedFrameBuffer clone() {
			return new TimestampedFrameBuffer(timestamp, framebuffer);
		}
	}

	public static class State {
		public enum Type {
			Click, Key
		};

		public Type type;
		public States state = States.INIT;
		public TimestampedFrameBuffer before, after;
		public Events.Type typeEvent = new Events.Type();
		public Events.Click clickEvent;

		public static class Key {
			public final long timestamp;
			public final int key;
			public final boolean downFlag;

			public Key(long timestamp, int key, boolean downFlag) {
				this.timestamp = timestamp;
				this.key = key;
				this.downFlag = downFlag;
			}
		}
	}

	// FIXME doesn't work for buttons which blinks on click (al)
	final static int WAIT_FOR_DAMAGES_MS = 5000;
	final static int WAIT_FOR_DAMAGES_END_MS = 2000;

	private final State state = new State();
	private final EventBus eventBus;
	private final int waitForDamages;
	private final int waitForDamagesEnd;
	private final Timer timer = new Timer();

	public InputStateMachine(EventBus eventBus) {
		this(eventBus, WAIT_FOR_DAMAGES_MS, WAIT_FOR_DAMAGES_END_MS);
	}

	public InputStateMachine(EventBus eventBus, int waitForDamages, int waitForDamagesEnd) {
		this.eventBus = eventBus;
		this.waitForDamages = waitForDamages;
		this.waitForDamagesEnd = waitForDamagesEnd;
	}

	private void setState(States update, long timestamp) {
		log.trace(timestamp + " setState " + timestamp + " transition: " + state.state + "->" + update);
		state.state = update;
	}

	private void setType(Key key) {
		state.typeEvent.keys.add(key);
		state.type = Type.Key;
	}

	private void setType(Click click) {
		state.clickEvent = click;
		state.type = Type.Click;
	}

	private void fireDelayedTimeout(final long timestamp, long delay) {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				handleTimeout(timestamp);
			}
		};
		timer.schedule(task, delay);
	}

	@Override
	public void updateFrameBuffer(long timestamp, Image.Int framebuffer, List<Rectangle.Int> updates) {
		log.trace(timestamp + " updateFrameBuffer(" + timestamp + ", " + framebuffer + ")");

		if (state.state == States.INIT) {
			state.before = new TimestampedFrameBuffer(timestamp, framebuffer);
			state.after = new TimestampedFrameBuffer(timestamp, framebuffer);
			setState(States.BEFORE, timestamp);
		} else if (state.state == States.BEFORE) {
			state.before.timestamp = timestamp;
			state.before.framebuffer = framebuffer;
			setState(States.BEFORE, timestamp);
		} else if (state.state == States.KEY || state.state == States.CLICK) {
			state.after.timestamp = timestamp;
			state.after.framebuffer = framebuffer;
			setState(States.AFTER, timestamp);
			fireDelayedTimeout(timestamp, waitForDamagesEnd);
		} else if (state.state == States.AFTER) {
			state.after.timestamp = timestamp;
			state.after.framebuffer = framebuffer;
			setState(States.AFTER, timestamp);
			fireDelayedTimeout(timestamp, waitForDamagesEnd);
		}
	}

	@Override
	public void pointerEvent(long timestamp, ClickType clickType, ButtonMask buttonMask, int x, int y) {
		log.trace(timestamp + " pointerEvent(" + timestamp + ", " + clickType + ", " + buttonMask + ", " + x + ", " + y + ")");
		// disregard other events
		if (ClickType.DOWN != clickType) {
			return;
		}

		// handle click
		if (state.state == States.INIT) {
		} else if (state.state == States.KEY) {
			eventBus.fireEvent(new InputStateChangeEvent(clone(state)));
			state.before = state.after.clone();
			setType(new Click(timestamp, buttonMask, x, y));
			setState(States.CLICK, timestamp);
			fireDelayedTimeout(timestamp, waitForDamagesEnd);
		} else if (state.state == States.BEFORE) {
			setType(new Click(timestamp, buttonMask, x, y));
			setState(States.CLICK, timestamp);
			fireDelayedTimeout(timestamp, waitForDamages);
		} else if (state.state == States.CLICK) {
			setType(new Click(timestamp, buttonMask, x, y));
			setState(States.CLICK, timestamp);
			fireDelayedTimeout(timestamp, waitForDamagesEnd);
		} else if (state.state == States.AFTER) {
			fireInputChangeEvent(state);
			state.before = state.after.clone();
			setType(new Click(timestamp, buttonMask, x, y));
			setState(States.CLICK, timestamp);
			fireDelayedTimeout(timestamp, waitForDamages);
		}
	}

	@Override
	public void keyEvent(long timestamp, int key, boolean controlKey, boolean downFlag) {
		log.trace(timestamp + " keyEvent(" + timestamp + ", " + key + ", " + downFlag + ")");
		// disregard other events
		if (!downFlag) {
			return;
		}

		// handle
		if (state.state == States.INIT) {
		} else if (state.state == States.CLICK ||
				(state.type == Type.Click && state.state == States.AFTER ||
				controlKey && state.state != States.BEFORE)) {
			eventBus.fireEvent(new InputStateChangeEvent(clone(state)));
			state.before = state.after.clone();
			setType(new State.Key(timestamp, key, downFlag));
			setState(States.KEY, timestamp);
			fireDelayedTimeout(timestamp, waitForDamagesEnd);
		} else if (state.state == States.BEFORE) {
			setType(new State.Key(timestamp, key, downFlag));
			setState(States.KEY, timestamp);
			fireDelayedTimeout(timestamp, waitForDamages);
		} else if (state.state == States.KEY) {
			state.typeEvent.keys.add(new State.Key(timestamp, key, downFlag));
			setState(States.KEY, timestamp);
			fireDelayedTimeout(timestamp, waitForDamagesEnd);
		} else if (state.state == States.AFTER) {
			state.typeEvent.keys.add(new State.Key(timestamp, key, downFlag));
			fireDelayedTimeout(timestamp, waitForDamagesEnd);
		}
	}

	private void handleTimeout(long timestamp) {
		if (state.state == States.AFTER) {
			if (timestamp >= state.after.timestamp) {

				log.trace(timestamp + " handleTimeout(before " + timestamp + " after " + state.after.timestamp + ", " + state.type + ")");
				fireInputChangeEvent(state);
				setState(States.BEFORE, timestamp);
				state.before = state.after.clone();
			}
		}
	}

	private void fireInputChangeEvent(State state) {
		log.trace(state.after.timestamp + " fireInputChangeEvent(" + state.after.timestamp + ", " + state.type + ")");
		eventBus.fireEvent(new InputStateChangeEvent(clone(state)));
	}

	private State clone(State state) {
		State clone = new State();
		clone.state = state.state;
		clone.before = state.before.clone();
		clone.after = state.after.clone();
		clone.type = state.type;
		if (state.type == Type.Key) {
			clone.typeEvent = new Events.Type(Lists.newLinkedList(state.typeEvent.keys));
			state.typeEvent = new Events.Type();
		} else if (state.type == Type.Click) {
			clone.clickEvent = new Events.Click(state.clickEvent.timestamp, state.clickEvent.buttonMask, state.clickEvent.x,
					state.clickEvent.y);
		}
		return clone;
	}

	public Registration register(Class<InputStateChangeEvent> clazz, InputStateChangeEvent.Handler handler) {
		return registerHandler(this.eventBus, clazz, handler);
	}
}