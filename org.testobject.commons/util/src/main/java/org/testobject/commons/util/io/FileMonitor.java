package org.testobject.commons.util.io;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.util.process.LinuxProcessInspector;

/**
 * 
 * @author enijkamp
 *
 */
public class FileMonitor {
	
	private static final Log log = LogFactory.getLog(FileMonitor.class);
	
	public interface Callback {
		void handleOpenFileCount(long count);
	}
	
	public static class LogCallback implements Callback {
		@Override
		public void handleOpenFileCount(long count) {
			log.info("Count of lsof open files descriptors is " + count);
		}
	}
	
	public static class ExceedsCallback implements Callback {

		private final long threshold;

		public ExceedsCallback(long threshold) {
			this.threshold = threshold;
		}

		@Override
		public void handleOpenFileCount(long count) {
			if(count > threshold) {
				log.error("Count of lsof open files descriptors exceeds threshold: " + count + " > " + threshold);
			}
		}
	}
	
	private final ScheduledExecutorService scheduledExecutor;
	private final long scheduleRateSecs;
	private final Callback[] callbacks;
	
	public FileMonitor(ThreadFactory threadFactory, long scheduleRateSecs, Callback ... callbacks) {
		this.scheduleRateSecs = scheduleRateSecs;
		this.callbacks = callbacks;
		this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor(threadFactory);
	}
	
	public void open() {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				for(Callback callback : callbacks) {
					try {
						callback.handleOpenFileCount(countOpenFiles());
					} catch(Throwable e) {
						log.warn(e.getMessage(), e);
					}
				}
			}
		};
		scheduledExecutor.scheduleWithFixedDelay(runnable, scheduleRateSecs, scheduleRateSecs, TimeUnit.SECONDS);
	}
	
	private long countOpenFiles() throws IOException {
		return FileUtil.lsof(new LinuxProcessInspector().getCurrentProcess());
	}

	public void close() {
		scheduledExecutor.shutdownNow();
	}

}
