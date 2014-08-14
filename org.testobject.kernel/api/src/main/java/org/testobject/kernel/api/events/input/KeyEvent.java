package org.testobject.kernel.api.events.input;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.testobject.kernel.api.events.Timestamp;

public class KeyEvent implements Event {
	
	public static class WithTimestamp extends KeyEvent implements Timestamp.Event {

		private final Timestamp timestamp;

		public WithTimestamp(Timestamp timestamp, KeyEvent event) {
			super(event.downFlag, event.keySym, event.controlKey);
			this.timestamp = timestamp;
		}

		@Override
		public Timestamp getTimeStamp() {
			return timestamp;
		}
		
		@Override
		public String toString() {
			return "key(time = " + timestamp + ", '" + this.keySym + "')";
		}
		
	}

	public final boolean downFlag;
	public final boolean controlKey;
	public final int keySym;

	@JsonCreator
	public KeyEvent(@JsonProperty("downFlag") boolean downFlag, @JsonProperty("keySym") int keySym, @JsonProperty("controlKey") boolean controlKey) {
		this.downFlag = downFlag;
		this.keySym = keySym;
		this.controlKey = controlKey;
	}
	
}
