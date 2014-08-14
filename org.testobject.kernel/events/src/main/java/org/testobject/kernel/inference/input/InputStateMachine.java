package org.testobject.kernel.inference.input;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.math.algebra.Rectangle.Int;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.exceptions.Exceptions;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.api.events.Timestamp;
import org.testobject.kernel.api.events.input.DeviceEvent;
import org.testobject.kernel.api.events.input.FramebufferUpdateEvent;
import org.testobject.kernel.api.events.input.KeyEvent;
import org.testobject.kernel.api.events.input.MouseEvent;
import org.testobject.kernel.api.events.output.Events;
import org.testobject.kernel.api.events.output.Events.Mouse.ClickPoint;
import org.testobject.kernel.api.events.output.Events.Type;
import org.testobject.kernel.api.events.output.Events.Type.Key;

import com.google.common.base.Preconditions;

/**
 * This state-machine listens for input- / framebuffer-events and emits events which indicate a state change.
 * 
 * @author enijkamp
 * 
 */
// TODO refactor, ISM should only handle click-{start, end}, factor out gesture-related aspects (en)
// TODO use proper state-machine pattern with explicit transistions etc. (en)
public class InputStateMachine {

	public interface Factory {

		InputStateMachine create(GetOrientation getOrientation, TransitionSequence transitionSequence, Callback callbackFactory);

	}

	public interface TransitionSequence {

		long next();

		class Stub {

			public static TransitionSequence create() {
				return new TransitionSequence() {
					private int sequence = 0;

					@Override
					public long next() {
						return sequence++;
					}
				};
			}
		}

	}

	public interface Callback {

		void beginTransition(long transition);

		void beginRequest(long transition, Framebuffer before, Events.Event event);

		void endRequest(long transition, Framebuffer before, Events.Event event);

		void endTransition(long transition, Framebuffer before, Framebuffer after, Events.Event event);

	}

	public interface IsIgnorableRegion {

		boolean isIgnorable(Rectangle.Int damageArea);

		class Factory {
			public static IsIgnorableRegion stub() {
				return new InputStateMachine.IsIgnorableRegion() {
					@Override
					public boolean isIgnorable(Int damageArea) {
						return false;
					}
				};
			}
		}
	}
	
	private static class FramebufferUpdate {
		
		private double lastUpdateArea;
		private long framebufferTimestamp;

		public FramebufferUpdate(FramebufferUpdateEvent fbuEvent) {
			double totalArea = fbuEvent.getFramebuffer().w * fbuEvent.getFramebuffer().h;
			double updatedArea = 0;		
			for (Rectangle.Int update : fbuEvent.getUpdates()) {
				updatedArea += (update.w * update.h);
			}
			
			this.lastUpdateArea = updatedArea / totalArea;
			this.framebufferTimestamp = fbuEvent.getTimeStamp().getFramebufferTimestamp();
		}
	}

	private static final Log log = LogFactory.getLog(InputStateMachine.class);

	public enum State {
		INIT, BEFORE, CLICK, KEY, PAYLOAD, AFTER
	}

	
	
	public static final long WAIT_FOR_DAMAGES_FBU_MS = 5 * 1000;
	public static final long WAIT_FOR_DAMAGES_MAX_MS = 10 * 1000;
	
	private static double FBU_SMALL_UPDATE_THRESHOLD = 0.012;
	private static double FBU_SMALL_UPDATE_TIMEOUT_MS = 1500;
	
	private final ReentrantLock lock = new ReentrantLock();
	private final InputTransition transition = new InputTransition();
	private final Timer timer = new Timer();

	private final IsIgnorableRegion isIgnorableRegion;
	private final long waitForDamagesFbuMs;
	private final long waitForDamagesMaxMs;

	private final TransitionSequence transitionSequence;
	private final Callback callback;

	private State state = State.INIT;
	private long currentTransitionId = 0l;
	private long lastInputTimestamp = 0l;

	List<FramebufferUpdate> transitionUpdates = Lists.newLinkedList();

	public InputStateMachine(IsIgnorableRegion isIgnorableRegion, TransitionSequence transitionSequence, Callback callback) {
		this(isIgnorableRegion, WAIT_FOR_DAMAGES_FBU_MS, WAIT_FOR_DAMAGES_MAX_MS, transitionSequence, callback);
	}

	public InputStateMachine(IsIgnorableRegion isIgnorableRegion, long waitForDamagesFbuMs, long waitForDamagesMaxMs,
			TransitionSequence transitionSequence, Callback callback) {
		this.isIgnorableRegion = isIgnorableRegion;
		this.waitForDamagesFbuMs = waitForDamagesFbuMs;
		this.waitForDamagesMaxMs = waitForDamagesMaxMs;
		this.transitionSequence = transitionSequence;
		this.callback = callback;
	}

	public void onEvent(org.testobject.kernel.api.events.Timestamp.Event event) {
		if (event instanceof FramebufferUpdateEvent) {
			FramebufferUpdateEvent fbuEvent = (FramebufferUpdateEvent) event;
			updateFrameBuffer(fbuEvent.getTimeStamp(), fbuEvent.getFramebuffer(), fbuEvent.getUpdates());
			transitionUpdates.add(new FramebufferUpdate(fbuEvent));
			return;
		}

		if (event instanceof MouseEvent.WithTimestamp) {
			MouseEvent.WithTimestamp mouseEvent = (MouseEvent.WithTimestamp) event;
			mouseEvent(mouseEvent.getTimeStamp(), mouseEvent.type, mouseEvent.point.x, mouseEvent.point.y);
			return;
		}

		if (event instanceof KeyEvent.WithTimestamp) {
			KeyEvent.WithTimestamp keyEvent = (KeyEvent.WithTimestamp) event;
			keyEvent(keyEvent.getTimeStamp(), keyEvent.keySym, keyEvent.controlKey, keyEvent.downFlag);
			return;
		}

		if (event instanceof DeviceEvent) {
			DeviceEvent deviceEvent = (DeviceEvent) event;
			deviceEvent(deviceEvent);
			return;
		}

		throw Exceptions.newUnsupportedTypeException("event", event.getClass());
	}

	public void updateFrameBuffer(Timestamp timestamp, Image.Int framebuffer, List<Rectangle.Int> updates) {
		lock.lock();
		try {

			log.trace(timestamp.getServerTimestamp() + " frame(" + timestamp.getFramebufferTimestamp() + ", " + framebuffer + ")");

			if (state != State.INIT && state != State.BEFORE) {
				Rectangle.Int damageArea = Rectangle.Int.union(updates);
				if (isIgnorableRegion.isIgnorable(damageArea)) {
					log.debug(timestamp.getServerTimestamp() + " updateFrameBuffer() -> ignoring damage area " + damageArea);
					transition.setAfter(new Framebuffer(transition.getAfter().timestamp, framebuffer));
					handleMaxTimeout(timestamp.getServerTimestamp());
					return;
				}
			}

			if (state == State.INIT || state == State.BEFORE) {

				transition.setBefore(new Framebuffer(timestamp.getServerTimestamp(), framebuffer));
				transition.setAfter(new Framebuffer(timestamp.getServerTimestamp(), framebuffer));
				setState(State.BEFORE, timestamp.getServerTimestamp());

			} else if (state == State.KEY || state == State.PAYLOAD) {

				transition.setAfter(new Framebuffer(timestamp.getServerTimestamp(), framebuffer));
				setState(State.AFTER, timestamp.getServerTimestamp());
				scheduleFbuTimeout(timestamp.getServerTimestamp());

			} else if (state == State.CLICK) {

				transition.setAfter(new Framebuffer(timestamp.getServerTimestamp(), framebuffer));

			} else if (state == State.AFTER) {

				transition.setAfter(new Framebuffer(timestamp.getServerTimestamp(), framebuffer));
				setState(State.AFTER, timestamp.getServerTimestamp());
				scheduleFbuTimeout(timestamp.getServerTimestamp());

				handleMaxTimeout(timestamp.getServerTimestamp());
			}
		} finally {
			lock.unlock();
		}
	}

	public void mouseEvent(final Timestamp timestamp, MouseEvent.Type clickType, int x, int y) {
		lock.lock();
		try {

			long serverTimestamp = timestamp.getServerTimestamp();
			log.trace(serverTimestamp + " mouse(" + timestamp.getFramebufferTimestamp() + ", " + clickType + ", " + x + ", " + y + ")");

			long timeDiff = serverTimestamp - lastInputTimestamp;
			rememberInputTimestamp(serverTimestamp);

			// handle click
			if (state == State.INIT) {

				log.warn("received mouse-event in state 'init'");

			} else if (state == State.KEY || state == State.PAYLOAD || state == State.AFTER) {

				fireInputChangeEvent();
				transition.setBefore(transition.getAfter().clone());
				setClick(new ClickPoint(new Point.Int(x, y), 0), serverTimestamp, isWaitingForUpdates());
				setState(State.CLICK, serverTimestamp);
				beginTransition();

			} else if (state == State.BEFORE) {

				setClick(new ClickPoint(new Point.Int(x, y), 0), serverTimestamp, isWaitingForUpdates());
				setState(State.CLICK, serverTimestamp);
				beginTransition();

			} else if (state == State.CLICK) {

				transition.getClickEvent().path.add(new ClickPoint(new Point.Int(x, y), timeDiff));

				if (clickType == MouseEvent.Type.UP) {
					setState(State.AFTER, serverTimestamp);
					callback.endRequest(currentTransitionId, transition.before, transition.event);
					transitionUpdates.clear();
				} else {
					setState(State.CLICK, serverTimestamp);
				}

			}
		} finally {
			lock.unlock();
		}
	}

	public void keyEvent(final Timestamp timestamp, int key, boolean controlKey, boolean downFlag) {
		
		lock.lock();
		try {

			long serverTimestamp = timestamp.getServerTimestamp();
			log.trace(serverTimestamp + " key(" + timestamp.getFramebufferTimestamp() + ", " + key + ", " + downFlag + ")");

			// disregard other events
			if (!downFlag) {
				return;
			}

			rememberInputTimestamp(serverTimestamp);

			// handle
			if (state == State.INIT) {

				log.warn("received key-event in state 'init'");

			} else if ((transition.isInitialized()
					&& (transition.getType() == InputTransition.Type.Click || transition.getType() == InputTransition.Type.Generic) && state == State.AFTER)
					|| (state != State.BEFORE && controlKey)
					|| (state != State.BEFORE && transition.getType() == InputTransition.Type.Key && transition.getTypeEvent().keys.get(0).controlKey)) {

				fireInputChangeEvent();
				transition.setBefore(transition.getAfter().clone());
				setType(new Type.Key(serverTimestamp, key, controlKey, downFlag), serverTimestamp, isWaitingForUpdates());
				
				setState(State.KEY, serverTimestamp);
				beginTransition();
				scheduleFbuTimeout(serverTimestamp);

			} else if (state == State.BEFORE) {

				setType(new Type.Key(serverTimestamp, key, controlKey, downFlag), serverTimestamp, isWaitingForUpdates());
				setState(State.KEY, serverTimestamp);
				beginTransition();
				scheduleFbuTimeout(serverTimestamp);

			} else if (state == State.KEY) {

				transition.getTypeEvent().keys.add(new Type.Key(serverTimestamp, key, controlKey, downFlag));
				setState(State.KEY, serverTimestamp);
				scheduleFbuTimeout(serverTimestamp);

			} else if (state == State.AFTER) {

				transition.getTypeEvent().keys.add(new Type.Key(serverTimestamp, key, controlKey, downFlag));
				scheduleFbuTimeout(serverTimestamp);

			}
		} finally {
			lock.unlock();
		}
		
		transitionUpdates.clear();
	}

	public void deviceEvent(DeviceEvent event) {
		lock.lock();
		try {

			final long serverTimestamp = event.getTimeStamp().getServerTimestamp();

			log.trace(serverTimestamp + " handle(" + event + ")");

			rememberInputTimestamp(serverTimestamp);

			// handle
			if (state == State.INIT) {

				log.warn("received sensor-event in state 'init'");

			} else if (state == State.BEFORE) {

				setPayload(event, serverTimestamp);
				setState(State.PAYLOAD, serverTimestamp);
				beginTransition();
				scheduleFbuTimeout(serverTimestamp);

			} else {

				fireInputChangeEvent();
				transition.setBefore(transition.getAfter().clone());
				setPayload(event, serverTimestamp);
				setState(State.PAYLOAD, serverTimestamp);
				beginTransition();
				scheduleFbuTimeout(serverTimestamp);

			}
		} finally {
			lock.unlock();
		}
	}

	private void beginTransition() {
		this.currentTransitionId = transitionSequence.next();
		callback.beginTransition(currentTransitionId);
	}
	
	private boolean isWaitingForUpdates() {
		long updatesTimeSpan = 0;
		for (FramebufferUpdate fbu : transitionUpdates) {
			if (fbu.lastUpdateArea > FBU_SMALL_UPDATE_THRESHOLD) {
				updatesTimeSpan = (fbu.framebufferTimestamp - transitionUpdates.get(0).framebufferTimestamp);
			}
		}
		
		return updatesTimeSpan > FBU_SMALL_UPDATE_TIMEOUT_MS;
	}

	private void scheduleFbuTimeout(final long eventTimestamp) {
		final long inputTimestamp = getLastInputTimestamp();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				handleFbuTimeout(eventTimestamp, inputTimestamp);
			}
		};
		timer.schedule(task, waitForDamagesFbuMs);
	}

	private void handleFbuTimeout(long eventTimestamp, long inputTimestamp) {
		lock.lock();
		try {
			final boolean isSameInputTimestamp = getLastInputTimestamp() == inputTimestamp;
			final boolean isLatestFbu = getLastFramebufferTimestamp() <= eventTimestamp;

			// key events may not lead to framebuffer events
			if (state == State.AFTER || state == State.KEY || state == State.PAYLOAD) {
				if (isSameInputTimestamp && isLatestFbu) {
					log.info(eventTimestamp + " handleFbuTimeout(before " + transition.getBefore().timestamp + ", after "
							+ transition.getAfter().timestamp + ", " + transition.getType() + ")");
					fireInputChangeEvent();
					setState(State.BEFORE, eventTimestamp);
					transition.setBefore(transition.getAfter().clone());
				}
			}
		} finally {
			lock.unlock();
		}
	}

	private void handleMaxTimeout(long eventTimestamp) {
		lock.lock();
		try {
			final boolean hasExceededTimeout = eventTimestamp >= (getLastInputTimestamp() + waitForDamagesMaxMs);

			if (state == State.AFTER) {
				if (hasExceededTimeout) {
					log.info(eventTimestamp + " handleMaxTimeout(before " + transition.getBefore().timestamp + ", after "
							+ transition.getAfter().timestamp + ", " + transition.getType() + ")");
					fireInputChangeEvent();
					setState(State.BEFORE, eventTimestamp);
					transition.setBefore(transition.getAfter().clone());
				}
			}
		} finally {
			lock.unlock();
		}
	}

	private void fireInputChangeEvent() {
		log.info(transition.getAfter().timestamp + " fireInputChangeEvent(before " + transition.getBefore().timestamp + ", input "
				+ transition.event.getTimestamp() + ", after "
				+ transition.getAfter().timestamp + ", " + transition.getType() + ")");

		InputTransition copy = InputTransition.copy(transition);

		if (transition.getType() != InputTransition.Type.Click) {
			callback.endRequest(currentTransitionId, copy.before, copy.event);
		}
		callback.endTransition(currentTransitionId, copy.before, copy.after, copy.event);
	}

	private void rememberInputTimestamp(long framebufferTimestamp) {
		lastInputTimestamp = framebufferTimestamp;
	}

	private long getLastInputTimestamp() {
		return lastInputTimestamp;
	}

	private long getLastFramebufferTimestamp() {
		return transition.getAfter().timestamp;
	}

	private void setState(State newState, long timestamp) {
		lock.lock();
		try {
			log.trace(timestamp + " setState(" + newState + "," + timestamp + ") -> transition: " + state + "->" + newState);
			this.state = newState;
		} finally {
			lock.unlock();
		}
	}

	private void setType(Key key, long timestamp, boolean waitUpdates) {
		transition.setEvent(new Events.Type(timestamp, Lists.toLinkedList(key), waitUpdates));
		Preconditions.checkArgument(transition.getType() == InputTransition.Type.Key);
		callback.beginRequest(currentTransitionId, transition.before, transition.event);
	}

	private void setClick(ClickPoint clickPoint, long timestamp, boolean waitUpdates) {
		transition.setEvent(new Events.Mouse(timestamp, Lists.toLinkedList(clickPoint), waitUpdates));
		Preconditions.checkArgument(transition.getType() == InputTransition.Type.Click);
		callback.beginRequest(currentTransitionId, transition.before, transition.event);
	}

	private void setPayload(DeviceEvent event, long timestamp) {
		transition.setEvent(toEvent(event, timestamp));
		Preconditions.checkArgument(transition.getType() == InputTransition.Type.Generic);
		callback.beginRequest(currentTransitionId, transition.before, transition.event);
	}

	private Events.Device toEvent(DeviceEvent deviceEvent, long timestamp) {
		return new Events.Device(deviceEvent, timestamp);
	}
}
