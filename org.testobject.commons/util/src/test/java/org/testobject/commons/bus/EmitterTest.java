package org.testobject.commons.bus;

import static org.testobject.commons.route.RouteBuilder.route;

import org.junit.Test;

/**
 * 
 * @author enijkamp
 *
 */
public class EmitterTest {
	
	public static class MyEvent implements Event<MyEvent.Handler> {
		public interface Handler extends Event.Handler {
			void handle(MyEvent event);
		}

		@Override
		public void dispatch(Handler handler) {
			handler.handle(this);
		}
		
		@Override
		public String toString() {
			return getClass().getSimpleName();
		}
	}
	
	@Emits(MyEvent.class)
	public static class MyService implements HasEvents {
		
		private final EventEmitter emitter;
		
		public MyService(EventEmitter emitter) {
			this.emitter = emitter;
		}

		public void fireMyEvent() {
			emitter.emit(new MyEvent());
		}

		@Override
		public EventSource getEventSource() {
			return emitter;
		}
		
	}
	
	private static class MyDispatcher implements Dispatcher<Event<?>> {
		@Override
		public void dispatch(Class<Event<?>> type, Event<?> event) {
			System.out.println(event);
		}
	}
	
	@Test
	public void testEmitter() {
		MyService service = new MyService(new EventEmitter(new SimpleEventBus()));
		MyDispatcher dispatcher = new MyDispatcher();
		
		route().event(MyEvent.class).from(service).to(dispatcher).build();
		
		service.fireMyEvent();
	}

}
