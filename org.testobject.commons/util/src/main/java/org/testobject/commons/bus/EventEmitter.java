package org.testobject.commons.bus;

import com.google.inject.Inject;

/**
 * 
 * @author enijkamp
 *
 */
public class EventEmitter implements EventSource {
	
	private final EventBus bus;
	
	@Inject
	public EventEmitter(EventBus bus) {
		this.bus = bus;
	}
	
	public EventBus getEventBus() {
		return bus;
	}
	
	public void emit(Event<?> event) {
		bus.fireEvent(event);
	}
	
	public <T extends Event<?>> void emit(Class<T> type, T event) {
		bus.fireEvent(type, event);
	}
	
	public <T extends Event.Handler> Registration register(Class<? extends Event<T>> event, T handler) {
		return Register.registerHandler(bus, event, handler);
	}
	
	public <T extends Event<?>> Registration register(Class<T> event, Dispatcher<T> dispatcher) {
		return bus.registerDispatcher(event, dispatcher);
	}
}