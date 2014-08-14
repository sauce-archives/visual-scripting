package org.testobject.kernel.api.events.input;

import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.kernel.api.events.Timestamp;

public interface DeviceEvent extends Timestamp.Event {

	abstract class WithTimeStamp implements DeviceEvent {

		private final Timestamp timestamp;

		public WithTimeStamp() {
			this.timestamp = new Timestamp(System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis());
		}

		@Override
		public Timestamp getTimeStamp() {
			return timestamp;
		}
	}

	class OrientationSensorEvent extends WithTimeStamp {

		public final org.testobject.commons.events.Orientation mode;

		public OrientationSensorEvent(org.testobject.commons.events.Orientation mode) {
			this.mode = mode;
		}

	}

	class GpsSensorEvent extends WithTimeStamp {

		public final double latitude;
		public final double longitude;
		public final double elevation;

		public GpsSensorEvent(double latitude, double longitude, double elevation) {
			this.latitude = latitude;
			this.longitude = longitude;
			this.elevation = elevation;
		}

	}

	class ReceiveCall extends WithTimeStamp {

		public final String number;

		public ReceiveCall(String number) {
			this.number = number;
		}

	}

	class HangupCall extends WithTimeStamp {

		public final String number;

		public HangupCall(String number) {
			this.number = number;
		}

	}

	class ReceiveSMS extends WithTimeStamp {

		public final String number;
		public final String text;

		public ReceiveSMS(String number, String text) {
			this.number = number;
			this.text = text;
		}
	}

	class ExecuteShellCommand extends WithTimeStamp {

		public final String shellCommand;

		public ExecuteShellCommand(String shellCommand) {
			this.shellCommand = shellCommand;
		}

	}
	
	class PushFile extends WithTimeStamp {
		
		public final String uploadId;
		public final String filePath;
		
		public PushFile(String uploadId, String filePath){
			this.uploadId = uploadId;
			this.filePath = filePath;
		}
		
	}

	class ExerciserMonkey extends WithTimeStamp {

		public final int eventCount;
		public final int throttle;
		public final int seed;

		public ExerciserMonkey(int eventCount, int throttle, int seed) {
			this.eventCount = eventCount;
			this.throttle = throttle;
			this.seed = seed;
		}
	}

	class InputText extends WithTimeStamp {

		public final String text;

		public InputText(String text) {
			this.text = text;
		}
	}

	public class LongPressPosition extends WithTimeStamp {

		public final Point.Int position;

		public LongPressPosition(Point.Int position) {
			this.position = position;
		}

	}

	public class ClickPosition extends WithTimeStamp {

		public final Point.Int position;

		public ClickPosition(Point.Int position) {
			this.position = position;
		}
	}

	public class LongPressLocator extends WithTimeStamp {

		public final Rectangle.Int locator;
		public final Point.Int offset;
		public final double timeout;

		public LongPressLocator(Rectangle.Int locator, Point.Int offset, double timeout) {
			this.locator = locator;
			this.offset = offset;
			this.timeout = timeout;
		}

	}

	public class ClickLocator extends WithTimeStamp {

		public Rectangle.Int locator;
		public final Point.Int offset;
		public double timeout;

		public ClickLocator(Rectangle.Int locator, Point.Int offset, double timeout) {
			this.locator = locator;
			this.offset = offset;
			this.timeout = timeout;
		}
	}
}
