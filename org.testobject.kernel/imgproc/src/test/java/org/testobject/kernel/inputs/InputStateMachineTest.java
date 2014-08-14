package org.testobject.kernel.inputs;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.testobject.commons.bus.SimpleEventBus;
import org.testobject.commons.events.ButtonMask;
import org.testobject.commons.events.PointerClickEvent.ClickType;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.image.Image;

/**
 * 
 * @author enijkamp
 *
 */
public class InputStateMachineTest {

	private static class InputBuilder {
		long currentTime = 0l;
		InputStateMachine stateMachine = new InputStateMachine(new SimpleEventBus());
		Image.Int buffer = new Image.Int(100, 100);
		List<Rectangle.Int> updates = Collections.singletonList(new Rectangle.Int(0, 0, 100, 100));

		final BlockingQueue<InputStateMachine.State> states = new LinkedBlockingQueue<>();
		InputStateChangeEvent.Handler handler = new InputStateChangeEvent.Handler() {
			@Override
			public void stateChanged(InputStateMachine.State state) {
				states.add(state);
			}
		};

		public InputBuilder() {
			stateMachine.register(InputStateChangeEvent.class, handler);
		}

		private InputBuilder frame(long targetTime) throws IOException, InterruptedException {
			Thread.sleep(targetTime - currentTime);
			stateMachine.updateFrameBuffer(targetTime, buffer, updates);
			this.currentTime = targetTime;
			
			return this;
		}

		private InputBuilder click(long targetTime) throws IOException, InterruptedException {
			Thread.sleep(targetTime - currentTime);
			stateMachine.pointerEvent(targetTime, ClickType.DOWN, ButtonMask.ButtonLeft, 50, 50);
			this.currentTime = targetTime;
			
			return this;
		}

		private InputStateMachine.State waitForStateChange() throws InterruptedException {
			return states.poll(InputStateMachine.WAIT_FOR_DAMAGES_MS + 100, TimeUnit.MILLISECONDS);
		}
	}

	@Test
	public void testWrongBeforeImage() throws IOException, InterruptedException {

		InputBuilder inputs = new InputBuilder();
		{
			inputs.frame(0l);
			inputs.click(100l);
			inputs.frame(150l);

			InputStateMachine.State state = inputs.waitForStateChange();
			assertThat(state.before.timestamp, is(0l));
			assertThat(state.after.timestamp, is(150l));
		}
		{
			inputs.click(1000l);
			inputs.frame(1100l);
			inputs.click(1300l);
			inputs.frame(1400l);
			{
				InputStateMachine.State state = inputs.waitForStateChange();
				assertThat(state.before.timestamp, is(150l));
				assertThat(state.after.timestamp, is(1100l));
			}
			{
				InputStateMachine.State state = inputs.waitForStateChange();
				assertThat(state.before.timestamp, is(1100l));
				assertThat(state.after.timestamp, is(1400l));
			}
		}
	}

}
