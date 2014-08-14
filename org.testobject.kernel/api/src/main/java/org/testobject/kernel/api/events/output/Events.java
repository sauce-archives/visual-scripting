package org.testobject.kernel.api.events.output;

import java.util.LinkedList;
import java.util.List;

import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.util.collections.Lists;
import org.testobject.kernel.api.events.input.DeviceEvent;

/**
 * 
 * @author enijkamp
 *
 */
public interface Events {

	interface Event {
		
		long getTimestamp();
		
		Event clone();
	}

	class Mouse implements Event {

		public static class ClickPoint {

			public final Point.Int point;
			public final long delay;

			public ClickPoint(Point.Int point, long delay) {
				this.point = point;
				this.delay = delay;
			}
		}

		public final long timestamp;
		public final LinkedList<ClickPoint> path;
		public final boolean waitLoaded;

		public Mouse(long timestamp, LinkedList<ClickPoint> path, boolean waitUpdates) {
			this.timestamp = timestamp;
			this.path = path;
			this.waitLoaded = waitUpdates;
		}

		@Override
		public long getTimestamp() {
			return timestamp;
		}

		@Override
		public Mouse clone() {
			return new Mouse(timestamp, Lists.newLinkedList(path), waitLoaded);
		}
	}

	class Type implements Event {

		public static class Key {
			public final long timestamp;
			public final int key;
			public final boolean downFlag;
			public final boolean controlKey;

			public Key(long timestamp, int key, boolean controlKey, boolean downFlag) {
				this.timestamp = timestamp;
				this.key = key;
				this.downFlag = downFlag;
				this.controlKey = controlKey;
			}
		}

		public final long timestamp;
		public final List<Key> keys;
		public final boolean waitLoaded;

		public Type(long timestamp, List<Key> keys, boolean waitUpdates) {
			this.timestamp = timestamp;
			this.keys = keys;
			this.waitLoaded = waitUpdates;
		}
		
		@Override
		public long getTimestamp() {
			return timestamp;
		}
		
		@Override
		public Type clone() {
			return new Type(timestamp, Lists.newLinkedList(keys), waitLoaded);
		}
	}
	
	class Device implements Event {

		private final DeviceEvent deviceEvent;
		private final long timestamp;

		public Device(DeviceEvent deviceEvent, long timestamp) {
			this.deviceEvent = deviceEvent;
			this.timestamp = timestamp;
		}

		public DeviceEvent getDeviceEvent() {
			return deviceEvent;
		}

		@Override
		public long getTimestamp() {
			return deviceEvent.getTimeStamp().getServerTimestamp();
		}
		
		@Override
		public Device clone() {
			return new Device(deviceEvent, timestamp);
		}
	}
}