package org.testobject.commons.route;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.testobject.commons.bus.Event;
import org.testobject.commons.bus.Event.Handler;

/**
 * This construct executes a given piece of code and awaits a specific event on the event bus.
 * 
 * <code>
 *   TimeoutHandler timeoutHandler = new TimeoutHandler() { public void timeout() { ... } };
 *   Runnable doSomething = new Runnable() { public void run() { ... } };
 *   await(bus).events(Foo.class, Foo.Handler.class).timeout(1000, timeoutHandler).run(doSomething);
 * </code>
 * 
 * @author enijkamp
 * 
 */
public class Await {

	public interface TimeoutHandler {
		void timeout();
	}

	public static Await await() {
		return new Await();
	}

	private final Lock lock = new ReentrantLock();
	private final Condition fired = lock.newCondition();

	private Class<? extends Event<?>> eventClass;
	private long timoutMs;
	private TimeoutHandler timeoutHandler;

	public Await() {

	}

	public Await from(Object source) {
		Class<? extends Event.Handler> handlerClass = RouteUtils.getHandlerClass(eventClass);
		Handler handler = createEventHandlerProxy(handlerClass);
		RouteUtils.register(eventClass, handlerClass, source, handler);
		return this;
	}

	public <H extends Handler> Await event(Class<? extends Event<H>> event) {
		this.eventClass = event;
		return this;
	}

	private <H extends Handler> H createEventHandlerProxy(Class<H> handler) {
		final java.lang.reflect.InvocationHandler invocationHandler = new InvocationHandler() {
			@Override
			public Object invoke(Object arg0, Method arg1, Object[] arg2)
					throws Throwable {
				lock.lock();
				try {
					fired.signal();
				} finally {
					lock.unlock();
				}
				return null;
			}
		};
		@SuppressWarnings("unchecked")
		final H proxy = (H) java.lang.reflect.Proxy.newProxyInstance(getClass()
				.getClassLoader(), new Class<?>[] { handler },
				invocationHandler);
		return proxy;
	}

	public Await timeout(long timoutMs, TimeoutHandler timeoutHandler) {
		this.timoutMs = timoutMs;
		this.timeoutHandler = timeoutHandler;
		return this;
	}

	public void run(Runnable run) {
		lock.lock();
		try {
			new Thread(run).start();
			if (this.fired.await(this.timoutMs, TimeUnit.MILLISECONDS) == false) {
				this.timeoutHandler.timeout();
			}
		} catch (final InterruptedException e) {
			// ignore
		} finally {
			lock.unlock();
		}
	}
}
