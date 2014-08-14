package org.testobject.commons.util.thread;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author enijkamp
 *
 */
public class ThreadUtil {
	
	private final static Log log = LogFactory.getLog(ThreadUtil.class);
	
    public static void sleep(long time) {
        if (time <= 0) {
            return;
        }
        
        try {
            Thread.sleep(time);
        } catch (InterruptedException ignore) {
        	log.warn("sleep interrupted");
        }
    }
    
    public static void safeSleep(long time) {
        if (time <= 0) {
            return;
        }
        
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        	throw new RuntimeException(e);
        }
    }

}
