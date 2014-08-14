package org.testobject.commons.guice;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.testobject.commons.guice.AsyncGuiceProxy.Factory.async;

import java.util.LinkedList;

import org.junit.Test;
import org.testobject.commons.bus.Event;
import org.testobject.commons.bus.EventBus;
import org.testobject.commons.bus.Register;
import org.testobject.commons.bus.Registration;
import org.testobject.commons.bus.SimpleEventBus;
import org.testobject.commons.route.RouteBuilder;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class AsyncGuiceProxyTest {

	private static LinkedList<String> queue = new LinkedList<>();

	@Test
	public void testAsyncService() throws InterruptedException {
		Injector injector = Guice.createInjector(new TestModule());

		TestEventRequest.Handler handler = async(injector, TestEventRequest.Handler.class);
		EventSource in = new EventSource();

		new RouteBuilder().event(TestEventRequest.class).from(in).to(handler).build();
		new RouteBuilder().event(TestEventResponse.class).from(handler).to(new EventTarget()).build();

		String request = "Test";
		in.fireEvent(request);

		String response = queue.poll();
		assertThat(response, is(request));
	}

	public static class TestEventRequest implements Event<TestEventRequest.Handler> {

		private final String argValue;

		public TestEventRequest(String argValue) {
			this.argValue = argValue;
		}

		interface Handler extends Event.Handler {
			void test(String value);
		}

		@Override
		public void dispatch(Handler handler) {
			handler.test(argValue);
		}
	}

	public static class TestEventResponse implements Event<TestEventResponse.Handler> {

		private final String returnValue;

		public TestEventResponse(String returnValue) {
			this.returnValue = returnValue;
		}

		interface Handler extends Event.Handler {
			void test(String value);
		}

		@Override
		public void dispatch(Handler handler) {
			handler.test(returnValue);
		}
	}

	public static class EventSource {
		private EventBus bus = new SimpleEventBus();

		private void fireEvent(String payload) {
			bus.fireEvent(new TestEventRequest(payload));
		}

		public Registration register(Class<TestEventRequest> clazz, TestEventRequest.Handler handler) {
			return Register.registerHandler(bus, clazz, handler);
		}
	}

	public static class EventTarget implements TestEventResponse.Handler {
		@Override
		public void test(String value) {
			queue.add(value);
		}
	}

	public static class SyncEventService implements TestEventRequest.Handler {
		private EventBus bus = new SimpleEventBus();

		public void test(String value) {
			bus.fireEvent(new TestEventResponse(value));
		}

		public Registration register(Class<TestEventResponse> clazz, TestEventResponse.Handler handler) {
			return Register.registerHandler(bus, clazz, handler);
		}
	}

	private static class TestModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(TestEventRequest.Handler.class).to(SyncEventService.class);
			bind(TestEventResponse.Handler.class).to(EventTarget.class);
		}
	}

}
