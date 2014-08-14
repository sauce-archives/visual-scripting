package org.testobject.kernel.inference.interpreter;

import org.apache.commons.logging.Log;

/**
 * 
 * @author enijkamp
 * 
 */
public class LogStagesAdapter implements Stages {
	
	private final Log log;

	private long start, step, nr;

	public LogStagesAdapter(Log log) {
		this.log = log;
	}

	public final void start() {
		log.info("0. start");
		nr = 1;
		start = step = System.currentTimeMillis();
	}

	public final void end() {
		long time = System.currentTimeMillis() - start;
		log.info(String.format("%-20s %5dms\n", "= total", time));
	}

	public final void done(String stage) {
		long time = System.currentTimeMillis() - step;
		log.info(String.format("%-20s %5dms", nr++ + ". " + stage, time));
		step = System.currentTimeMillis();
	}
}