package org.testobject.kernel.mocks;

import java.util.LinkedList;
import java.util.List;

import org.testobject.commons.events.ButtonMask;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.script.api.Events;

/**
 * 
 * @author enijkamp
 *
 */
public interface Inputs {

	class Input {
		public final Events.Event event;
		public final Image.Int before, after;

		public Input(Events.Event event, Image.Int before, Image.Int after) {
			this.event = event;
			this.before = before;
			this.after = after;
		}
	}

	class InputBuilder {
		private List<Input> inputs = new LinkedList<Input>();

		public static InputBuilder input() {
			return new InputBuilder();
		}

		public InputBuilder click(Image.Int before, Image.Int after, int x, int y) {
			inputs.add(new Input(new Events.Click(System.currentTimeMillis(), ButtonMask.ButtonLeft, x, y), before, after));
			return this;
		}

		public List<Input> build() {
			return inputs;
		}
	}
}
