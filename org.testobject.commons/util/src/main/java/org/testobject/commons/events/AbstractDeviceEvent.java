package org.testobject.commons.events;


/**
 * 
 * @author enijkamp
 *
 */
public abstract class AbstractDeviceEvent implements DeviceEvent {
	
	private final org.testobject.commons.events.TimestampEvent.Timestamp timestamp;
	
	public AbstractDeviceEvent(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public void dispatch(Handler handler) {
		handler.handle(this);
	}

	@Override
	public TimestampEvent.Timestamp getTimestamp() {
		return timestamp;
	}

}