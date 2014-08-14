package org.testobject.commons.events;

import org.testobject.commons.bus.Event;

/**
 * 
 * @author enijkamp
 *
 */
public interface DeviceEvent extends TimestampEvent<DeviceEvent.Handler> {

	interface Handler extends Event.Handler {
		void handle(DeviceEvent event);
	}

	interface Gps extends DeviceEvent {

		double getLongitude();

		double getLatitude();

		double getElevation();

	}

	interface Orientation extends DeviceEvent {

		org.testobject.commons.events.Orientation getMode();

	}

	interface CallReceive extends DeviceEvent {

		String getPhoneNumber();

	}

	interface CallHangup extends DeviceEvent {

		String getPhoneNumber();

	}

	interface SmsReceive extends DeviceEvent {

		String getPhoneNumber();

		String getMessage();

	}

	interface InstallApp extends DeviceEvent {

		long getAppId();

	}

	interface Restart extends DeviceEvent {

	}
}
