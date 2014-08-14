package org.testobject.commons.util.platform;

/**
 * 
 * @author enijkamp
 *
 */
public class Arch {
	
	private static final boolean is32Bit = "32".equals(System.getProperty("sun.arch.data.model"));
	private static final boolean is64Bit = "64".equals(System.getProperty("sun.arch.data.model"));
	
	public static final int getArchBits() {
		return is32Bit ? 32 : 64;
	}
	
	public static final boolean is32Bit() {
		return is32Bit;
	}
	
	public static final boolean is64Bit() {
		return is64Bit;
	}

}
