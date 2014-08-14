package org.testobject.commons.bus;

import static org.testobject.commons.bus.Register.registerHandler;
import junit.framework.Assert;

import org.junit.Test;

/**
 * 
 * @author nijkamp
 *
 */
public class SimpleEventBusTest {

	private interface MyEvent extends Event<MyEvent.Handler> {
		interface Handler extends Event.Handler {
			void hello(String world);
		}
	}

	private class ComponentA {
		private final EventBus bus;

		public ComponentA(EventBus bus) {
			this.bus = bus;
		}

		private class MyEventImpl implements MyEvent {

			private final String text = "foo";

			@Override
			public void dispatch(Handler handler) {
				handler.hello(text);
			}
		}

		public void go() {
			bus.fireEvent(MyEvent.class, new MyEventImpl());
		}
	}

	private class ComponentB {
		public boolean received = false;
		
		public ComponentB(EventBus bus) {
			registerHandler(bus, MyEvent.class, new MyEvent.Handler() {
				@Override
				public void hello(String world) {
					System.out.println(world);
					received = true;
				}
			});
		}
	}

	@Test
	public void testFireEvent() {
		// bus
		EventBus bus = new SimpleEventBus();

		// inject
		ComponentA a = new ComponentA(bus);
		ComponentB b = new ComponentB(bus);

		// go
		a.go();
		
		// assert
		Assert.assertTrue(b.received);
	}

}
