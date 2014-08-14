package org.testobject.commons.util.rest;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@SuppressWarnings("serial")
@JsonIgnoreProperties({ "cause", "stackTrace", "localizedMessage", "suppressed"})
public class RestException extends RuntimeException {

	private final String exceptionClass;
	private final String exceptionClassName;
	private final String message;
	private final String causeAsString;
	private final String stackTraceAsString;
	
	@JsonCreator
	public RestException(@JsonProperty("exceptionClass") String exceptionClass, 
			@JsonProperty("exceptionClassName") String exceptionClassName,
			@JsonProperty("message") String message,
			@JsonProperty("causeAsString") String causeAsString,
			@JsonProperty("stackTraceAsString") String stackTraceAsString) {
				
		this.exceptionClass = exceptionClass;
		this.exceptionClassName = exceptionClassName;
		this.message = message;
		this.causeAsString = causeAsString;
		this.stackTraceAsString = stackTraceAsString;
		
	}
	
	public RestException(Exception exception) {
		exception.printStackTrace();
		this.exceptionClass = exception.getClass().toString();

		String[] nameArray = exception.getClass().getSimpleName().split("\\.");
		this.exceptionClassName = nameArray[nameArray.length - 1];
		
		this.message = exception.getMessage();
		this.causeAsString = exception.getCause() != null ? exception.getCause().toString() : "";
		this.stackTraceAsString = stacktraceToString(exception);
	}

	private static String stacktraceToString(Throwable t) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	public String getExceptionClass() {
		return exceptionClass;
	}

	public String getExceptionClassName() {
		return exceptionClassName;
	}
	
	public String getMessage() {
		return message;
	}

	public String getCauseAsString() {
		return causeAsString;
	}

	public String getStackTraceAsString() {
		return stackTraceAsString;
	}
}
