package org.testobject.commons.util.debug;

import java.io.PrintStream;

/**
 * 
 * @author enijkamp
 *
 */
public class StopWatch {

	private final PrintStream out;
	
	public long start, stop;
	private String task;
	
	public StopWatch(PrintStream out) {
		this.out = out;
	}
	
	public void start(String task) {
		this.task = task;
		this.start = System.currentTimeMillis();
	}
	
	public long stop() {
		this.stop = System.currentTimeMillis();
		long time = (stop - start);
		out.println(task + " took " + time + "ms");
		
		return time;
	}
	
}
