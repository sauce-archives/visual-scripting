package org.testobject.kernel.inference.input;

import java.util.LinkedList;

import org.testobject.commons.util.exceptions.Exceptions;
import org.testobject.kernel.api.events.output.Events;
import org.testobject.kernel.api.events.output.Events.Event;
import org.testobject.kernel.api.events.output.Events.Mouse.ClickPoint;
import org.testobject.kernel.api.events.output.Events.Type.Key;

import com.google.common.base.Preconditions;

/**
 * 
 * @author enijkamp
 *
 */
public class InputTransition {
	public enum Type {
		Click, Key, Generic
	}

	Framebuffer before;
	Framebuffer after;
	Events.Event event;
	
	public InputTransition() {

	}
	
	public InputTransition(Framebuffer before, Framebuffer after, Event event) {
		this.before = before;
		this.after = after;
		this.event = event;
	}

	public boolean isInitialized() {
		return event != null;
	}
	
	public InputTransition.Type getType() {

		Preconditions.checkNotNull(event);

		if (event instanceof Events.Device) {
			return Type.Generic;
		}

		if (event instanceof Events.Type) {
			return Type.Key;
		}

		if (event instanceof Events.Mouse) {
			return Type.Click;
		}

		throw Exceptions.newUnsupportedTypeException("event", event.getClass());
	}

	public void setEvent(Events.Event event) {
		this.event = event;
	}

	public Events.Mouse getClickEvent() {
		Preconditions.checkState(getType() == Type.Click);
		return (Events.Mouse) event;
	}

	public Events.Type getTypeEvent() {
		Preconditions.checkState(getType() == Type.Key);
		return (Events.Type) event;
	}

	public static InputTransition copy(InputTransition transition) {

		InputTransition clone = new InputTransition();

		clone.setBefore(transition.getBefore().clone());
		clone.setAfter(transition.getAfter().clone());

		if (transition.getType() == InputTransition.Type.Key) {
			clone.event = transition.event.clone();
			transition.event = new Events.Type(0l, new LinkedList<Key>(), false);
			return clone;
		}

		if (transition.getType() == InputTransition.Type.Click) {
			clone.event = transition.event.clone();
			transition.event = new Events.Mouse(0l, new LinkedList<ClickPoint>(), false);
			return clone;
		}

		if (transition.getType() == InputTransition.Type.Generic) {
			clone.event = transition.event.clone();
			return clone;
		}

		throw Exceptions.newUnsupportedTypeException("type", transition.getType().toString());
	}

	public Framebuffer getBefore() {
		return before;
	}

	public Framebuffer getAfter() {
		return after;
	}

	public void setBefore(Framebuffer before) {
		this.before = before;
	}

	public void setAfter(Framebuffer after) {
		this.after = after;
	}

	public Event getEvent() {
		return event;
	}
}