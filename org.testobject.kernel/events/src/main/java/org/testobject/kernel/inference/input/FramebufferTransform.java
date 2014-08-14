package org.testobject.kernel.inference.input;

import javax.inject.Inject;

import org.testobject.commons.events.Orientation;
import org.testobject.commons.util.exceptions.Exceptions;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.api.events.input.DeviceEvent;
import org.testobject.kernel.api.events.output.Events;

import com.google.common.base.Preconditions;

/**
 * 
 * @author enijkamp
 *
 */
public class FramebufferTransform {

	private Orientation orientation;

	@Inject
	public FramebufferTransform(Orientation orientation) {
		this.orientation = orientation;
	}
	
	public InputTransition transform(InputTransition transition) {

		InputTransition rotateTransition = InputTransition.copy(transition);
		rotateTransition.setBefore(transformBefore(transition.getBefore(), transition.getEvent()));
		rotateTransition.setAfter(transformAfter(transition.getAfter(), transition.getEvent()));

		return rotateTransition;
	}

	public Framebuffer transformAfter(Framebuffer after, org.testobject.kernel.api.events.output.Events.Event event) {

		if (isOrientationEvent(event)) {

			// in orientation transition

			DeviceEvent.OrientationSensorEvent orientationEvent = toOrientationEvent(event);
			
			this.orientation = orientationEvent.mode;

			if (orientationEvent.mode == Orientation.PORTRAIT) {
				return after.clone();
			} else {
				return toLandscape(after.clone());
			}

		} else {

			// rotate image to current orientation

			return rotate(after);
		}
	}

	public Framebuffer transformBefore(Framebuffer before, org.testobject.kernel.api.events.output.Events.Event event) {

		if (isOrientationEvent(event)) {
			
			// in orientation transition
			
			DeviceEvent.OrientationSensorEvent orientationEvent = toOrientationEvent(event);
			
			this.orientation = orientationEvent.mode;

			if (orientationEvent.mode == Orientation.PORTRAIT) {
				return toLandscape(before.clone());
			} else {
				return before.clone();
			}

		} else {

			// rotate image to current orientation

			return rotate(before);
		}
	}

	private boolean isOrientationEvent(org.testobject.kernel.api.events.output.Events.Event event) {
		if (event instanceof Events.Device == false) {
			return false;
		} else {
			org.testobject.kernel.api.events.output.Events.Device genericEvent = (org.testobject.kernel.api.events.output.Events.Device) event;
			return genericEvent.getDeviceEvent() instanceof DeviceEvent.OrientationSensorEvent;
		}
	}

	private DeviceEvent.OrientationSensorEvent toOrientationEvent(org.testobject.kernel.api.events.output.Events.Event event) {
		org.testobject.kernel.api.events.output.Events.Device genericEvent = (org.testobject.kernel.api.events.output.Events.Device) event;
		return (DeviceEvent.OrientationSensorEvent) genericEvent.getDeviceEvent();
	}

	public Framebuffer rotate(Framebuffer framebuffer) {
		if (orientation == Orientation.PORTRAIT) {
			return framebuffer;
		}

		if (orientation == Orientation.LANDSCAPE) {
			return toLandscape(framebuffer);
		}

		throw Exceptions.newUnsupportedTypeException("orientation", orientation.toString());
	}

	private Framebuffer toLandscape(Framebuffer buffer) {
		return new Framebuffer(buffer.timestamp, toLandscape(buffer.framebuffer));
	}

	public static Image.Int toLandscape(Image.Int source) {
	//	Preconditions.checkArgument(isPortrait(source), "Source image is not in portrait orientation");
		
		if (isLandscape(source)) {
			return source;
		}
		
		Image.Int target = new Image.Int(source.h, source.w, source.type);
		int i = 0;
		for (int x = source.w - 1; x >= 0; x--) {
			for (int y = 0; y < source.h; y++) {
				target.pixels[i++] = source.pixels[y * source.w + x];
			}
		}

		return target;
	}

	public static Image.Int toPortrait(Image.Int source) {
		Preconditions.checkArgument(isLandscape(source), "Source image is not in landscape orientation");

		Image.Int target = new Image.Int(source.h, source.w, source.type);
		int i = 0;
		for (int x = 0; x < source.w; x++) {
			for (int y = source.h - 1; y >= 0; y--) {
				target.pixels[i++] = source.pixels[y * source.w + x];
			}
		}

		return target;
	}

	public static boolean isPortrait(Image.Int source) {
		return source.h > source.w;
	}

	public static boolean isLandscape(Image.Int source) {
		return source.w > source.h;
	}

	public static Image.Int[] toPortrait(Image.Int[] source) {
		Image.Int[] target = new Image.Int[source.length];
		for (int i = 0; i < source.length; i++) {
			target[i] = isLandscape(source[i]) ? toPortrait(source[i]) : source[i];
		}

		return target;
	}
}
