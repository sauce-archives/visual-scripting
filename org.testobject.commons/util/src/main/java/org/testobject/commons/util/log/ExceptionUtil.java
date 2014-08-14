package org.testobject.commons.util.log;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * 
 * @author enijkamp
 *
 */
public class ExceptionUtil {
    
    public static String printMessages(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        if(throwable.getMessage() != null) {
        	sb.append(throwable.getMessage()).append('\n');
        }
        if (throwable.getCause() != null) {
            return sb.toString() + printMessages(throwable.getCause());
        } else {
            return sb.toString();
        }
    }
    
    public static String printStackTraceUsingStream(Throwable throwable) {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	PrintStream ps = new PrintStream(baos);
    	throwable.printStackTrace(ps);
    	return baos.toString();
    }
    
    public static String printStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append("Caused by: ").append(throwable).append('\n');
        for (StackTraceElement ste : throwable.getStackTrace())
        {
            sb.append('\t').append(ste.toString()).append('\n');
        }
        if (throwable.getCause() != null)
        {
            return sb.toString() + printStackTrace(throwable.getCause());
        }
        else
        {
            return sb.toString();
        }
    }
    
	public static Throwable toRootCause(Throwable cause) {
		while(cause.getCause() != null) {
			cause = cause.getCause();
		}
		return cause;
	}
	
}
