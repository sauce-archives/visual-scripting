package org.testobject.commons.util.config;

import java.util.Properties;

/**
 * 
 * @author enijkamp
 *
 */
public class Debug {

	private static final Properties configuration = Configuration.load(Configuration.APP);

	private static final boolean isDebugEnabled = isEnabled(Constants.runtime_debug_mode);

	private static boolean isEnabled(String key) {
		return configuration.containsKey(key) && "true".equals(configuration.get(key));
	}

	public static boolean isDebugEnabled() {
		return isDebugEnabled;
	}

	public static boolean toDebugMode(boolean debug) {
		return debug && isDebugEnabled();
	}

}
