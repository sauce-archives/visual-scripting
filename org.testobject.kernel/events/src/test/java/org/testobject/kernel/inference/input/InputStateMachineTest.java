package org.testobject.kernel.inference.input;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.thread.ThreadUtil;
import org.testobject.kernel.api.events.Timestamp;
import org.testobject.kernel.api.events.input.MouseEvent;
import org.testobject.kernel.api.events.output.Events.Event;
import org.testobject.kernel.api.events.output.Events.Type.Key;
import org.testobject.kernel.inference.input.InputStateMachine.Callback;

/**
 * 
 * @author enijkamp
 *
 */
public class InputStateMachineTest {

	@Test
	public void testWrongBeforeImage() throws IOException, InterruptedException {

		InputBuilder inputs = new InputBuilder();
		{
			inputs.frame(0l);
			inputs.click(100l);
			inputs.frame(150l);

			InputTransition state = inputs.waitForStateChange();
			assertThat(state.getBefore().timestamp, is(0l));
			assertThat(state.getAfter().timestamp, is(150l));
		}
		{
			inputs.click(1000l);
			inputs.frame(1100l);
			inputs.click(1300l);
			inputs.frame(1400l);
			{
				InputTransition state = inputs.waitForStateChange();
				assertThat(state.getBefore().timestamp, is(150l));
				assertThat(state.getAfter().timestamp, is(1100l));
			}
			{
				InputTransition state = inputs.waitForStateChange();
				assertThat(state.getBefore().timestamp, is(1100l));
				assertThat(state.getAfter().timestamp, is(1400l));
			}
		}
	}

	@Test
	public void testFbuTimeout() throws IOException, InterruptedException {

		final int fbuTimeout = 1000;
		final int maxTimeout = 2000;

		InputBuilder inputs = new InputBuilder(fbuTimeout, maxTimeout);
		{
			inputs.frame(0l);
			inputs.click(100l);
			inputs.frame(200l);
			inputs.frame(2000l);
			inputs.frame(3000l);

			InputTransition state = inputs.waitForStateChange();

			assertThat(inputs.states.isEmpty(), is(true));
			assertThat(state.getBefore().timestamp, is(0l));
			assertThat(state.getAfter().timestamp, is(200l));
		}
	}

	@Test
	public void testMaxTimeout() throws IOException, InterruptedException {

		final int fbuTimeout = 1000;
		final int maxTimeout = 2000;

		InputBuilder inputs = new InputBuilder(fbuTimeout, maxTimeout);
		{
			inputs.frame(0l);
			inputs.click(1100l);
			inputs.frame(1500l);
			inputs.frame(2000l);
			inputs.frame(2500l);
			inputs.frame(3000l);
			inputs.frame(3500l);
			inputs.frame(4000);

			InputTransition state = inputs.waitForStateChange();

			assertThat(inputs.states.isEmpty(), is(true));
			assertThat(state.getBefore().timestamp, is(0l));
			assertThat(state.getAfter().timestamp, is(3500l));
		}
	}

	@Test
	public void testTimeoutTriggersOnlyOneTransition() throws IOException, InterruptedException {

		final int fbuTimeout = 1000;
		final int maxTimeout = 2000;

		InputBuilder inputs = new InputBuilder(fbuTimeout, maxTimeout);
		{
			// can trigger either handleFbuTimeout() or handleMaxTimeout(), but never both
			inputs.frame(0l);
			inputs.click(2000l);
			inputs.frame(2500l);
			inputs.frame(3501l);
			inputs.frame(4500l);
			inputs.frame(5000l);

			Thread.sleep(1000);

			InputTransition state = inputs.waitForStateChange(maxTimeout + 100);

			assertThat(inputs.states.isEmpty(), is(true));
			assertThat(state.getBefore().timestamp, is(0l));
			assertThat(state.getAfter().timestamp, anyOf(is(2500l), is(4500l)));
		}
	}

	@Test
	public void testCharKeysConcat() throws IOException, InterruptedException {

		InputBuilder inputs = new InputBuilder();
		{
			inputs.frame(0l);
			inputs.charKey(100l, (int) 'a');
			inputs.charKey(150l, (int) 'b');
			inputs.charKey(200l, (int) 'c');
			inputs.frame(600l);

			{
				InputTransition abc = inputs.waitForStateChange();
				assertThat(abc.getBefore().timestamp, is(0l));
				assertThat(abc.getAfter().timestamp, is(600l));
				assertThat(toString(abc.getTypeEvent().keys), is("abc"));
			}
		}
	}

	@Test
	public void testHardwareKeysTransition() throws IOException, InterruptedException {

		InputBuilder inputs = new InputBuilder();
		{
			final int ENTER = 13;

			inputs.frame(0l);
			inputs.charKey(100l, (int) 'a');
			inputs.charKey(150l, (int) 'b');
			inputs.charKey(200l, (int) 'c');
			inputs.ctrlKey(250l, ENTER);
			inputs.charKey(300l, (int) 'd');
			inputs.charKey(300l, (int) 'e');
			inputs.frame(600l);

			{
				InputTransition abc = inputs.waitForStateChange();
				assertThat(abc.getBefore().timestamp, is(0l));
				assertThat(abc.getAfter().timestamp, is(0l));
				assertThat(toString(abc.getTypeEvent().keys), is("abc"));
			}

			{
				InputTransition enter = inputs.waitForStateChange();
				assertThat(enter.getBefore().timestamp, is(0l));
				assertThat(enter.getAfter().timestamp, is(0l));
				assertThat(enter.getTypeEvent().keys.get(0).key, is(ENTER));
			}

			{
				InputTransition de = inputs.waitForStateChange();
				assertThat(de.getBefore().timestamp, is(0l));
				assertThat(de.getAfter().timestamp, is(600l));
				assertThat(toString(de.getTypeEvent().keys), is("de"));
			}
		}
	}

	private String toString(List<Key> keys) {
		String string = "";
		for (Key key : keys) {
			string += (char) key.key;
		}
		return string;
	}

	private static class InputBuilder {
		public long currentTime = 0l;
		public InputStateMachine stateMachine;
		public Image.Int buffer = new Image.Int(100, 100);
		public List<Rectangle.Int> updates = Collections.singletonList(new Rectangle.Int(0, 0, 100, 100));
		public BlockingQueue<InputTransition> states = new LinkedBlockingQueue<>();

		public InputBuilder() {
			this(InputStateMachine.WAIT_FOR_DAMAGES_FBU_MS, InputStateMachine.WAIT_FOR_DAMAGES_MAX_MS);
		}

		public InputBuilder(long waitForDamagesFbuMs, long waitForDamagesMaxMs) {
			this.stateMachine = new InputStateMachine(InputStateMachine.IsIgnorableRegion.Factory.stub(), waitForDamagesFbuMs,
					waitForDamagesMaxMs, InputStateMachine.TransitionSequence.Stub.create(), createCallback());
		}

		private Callback createCallback() {
			return new Callback() {
				@Override
				public void beginTransition(long transition) {}

				@Override
				public void beginRequest(long transition, Framebuffer before, Event event) {}

				@Override
				public void endRequest(long transition, Framebuffer before, Event event) {}

				@Override
				public void endTransition(long transition, Framebuffer before, Framebuffer after, Event event) {
					states.add(new InputTransition(before, after, event));
				}
			};
		}

		public InputBuilder frame(long targetTime) throws IOException, InterruptedException {
			Thread.sleep(targetTime - currentTime);
			stateMachine.updateFrameBuffer(new Timestamp(targetTime, targetTime, targetTime), buffer, updates);
			this.currentTime = targetTime;

			return this;
		}

		public InputBuilder click(long targetTime) throws IOException, InterruptedException {
			Thread.sleep(targetTime - currentTime);
			stateMachine.mouseEvent(new Timestamp(targetTime, targetTime, targetTime), MouseEvent.Type.DOWN, 50, 50);
			ThreadUtil.sleep(10);
			stateMachine.mouseEvent(new Timestamp(targetTime, targetTime, targetTime), MouseEvent.Type.UP, 50, 50);
			this.currentTime = targetTime;

			return this;
		}

		public InputBuilder charKey(long targetTime, int keycode) throws IOException, InterruptedException {
			Thread.sleep(targetTime - currentTime);
			stateMachine.keyEvent(new Timestamp(targetTime, targetTime, targetTime), keycode, false, true);
			this.currentTime = targetTime;

			return this;
		}

		public InputBuilder ctrlKey(long targetTime, int keycode) throws IOException, InterruptedException {
			Thread.sleep(targetTime - currentTime);
			stateMachine.keyEvent(new Timestamp(targetTime, targetTime, targetTime), keycode, true, true);
			this.currentTime = targetTime;

			return this;
		}

		public InputTransition waitForStateChange() throws InterruptedException {
			return states.poll(InputStateMachine.WAIT_FOR_DAMAGES_FBU_MS + 500, TimeUnit.MILLISECONDS);
		}

		public InputTransition waitForStateChange(long ms) throws InterruptedException {
			return states.poll(ms, TimeUnit.MILLISECONDS);
		}
	}
}