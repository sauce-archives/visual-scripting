package org.testobject.commons.util.thread;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class TimeoutTest {

	@Test(expected=TimeoutException.class)
	public void testCheckExceptionOnTimeout() throws InterruptedException {
		Timeout timeout = new Timeout(1, TimeUnit.SECONDS);
		Thread.sleep(2000);
		timeout.check();
	}
	
	@Test
	public void testCheckNoExceptionBeforeTimeout() throws InterruptedException {
		Timeout timeout = new Timeout(10, TimeUnit.SECONDS);
		Thread.sleep(2000);
		timeout.check();
	}

}
