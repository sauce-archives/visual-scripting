package org.testobject.kernel.platform.robot;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class ExerciserMonkeyResponse {

	public final boolean success;
	public final String errorMessage;

	@JsonCreator
	public ExerciserMonkeyResponse(@JsonProperty("success") boolean success, @JsonProperty("errorMessage") String errorMessage) {
		this.success = success;
		this.errorMessage = errorMessage;
	}

	public static class Factory {

		public static ExerciserMonkeyResponse stub() {
			return new ExerciserMonkeyResponse(true, "");
		}

	}
}