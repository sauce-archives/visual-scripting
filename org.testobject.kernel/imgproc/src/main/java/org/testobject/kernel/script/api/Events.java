package org.testobject.kernel.script.api;

import java.util.LinkedList;
import java.util.List;

import org.testobject.commons.events.ButtonMask;
import org.testobject.kernel.inputs.InputStateMachine;

/**
 * 
 * @author enijkamp
 * 
 */
public interface Events {
	
	interface Event {
		
	}

	class Click implements Event {

		public final long timestamp;
		public final ButtonMask buttonMask;
		public final int x, y;

		public Click(long timestamp, ButtonMask buttonMask, int x, int y) {
			this.timestamp = timestamp;
			this.buttonMask = buttonMask;
			this.x = x;
			this.y = y;
		}
	}

	class Type implements Event {

		public List<InputStateMachine.State.Key> keys;

		public Type() {
			this(new LinkedList<InputStateMachine.State.Key>());
		}

		public Type(List<InputStateMachine.State.Key> keys) {
			this.keys = keys;
		}
	}

	class Wait implements Event {

	}
}