package org.testobject.kernel.app.modules;

import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.guice.AbstractModule;

import com.google.inject.Provides;

/**
 * 
 * @author enijkamp
 *
 */
public class DebugModule extends AbstractModule {
	
	private static final Log log = LogFactory.getLog(DebugModule.class);

	@Override
	protected void configure() {
		
	}
	
	@Provides
	public UncaughtExceptionHandler uncaughtExceptionHandler() {
		return new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				log.error("Uncaught exception caused by thread '" + t.getName() + "'", e);
			}
		};
	}

}
