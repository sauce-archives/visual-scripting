package org.testobject.commons.bus;

/**
 * Interface for an event bus. Most prominent implementation of in-process in-memory event bus is {@link SimpleEventBus}
 * . However, one is free to put forward other implementations, including remote delivery, and whatever features one
 * needs.
 */
public interface EventBus {

	/**
	 * Fires an event on the bus. It will be delivered to all handlers that are registered for this event type (see
	 * {@link Event#getType()}). If some handlers throw an exception it will be intercepted and after all handlers are
	 * processed this method will throw an {@link UmbrellaException}.
	 *
	 * @param event
	 *            event to deliver.
	 */
	<T extends Event<?>> void fireEvent(T event);

	/**
	 * Fires an event on the bus. It will be delivered to all handlers that are registered for this event type (see
	 * {@link Event#getType()}). If some handlers throw an exception it will be intercepted and after all handlers are
	 * processed this method will throw an {@link UmbrellaException}.
	 *
	 * @param type
	 *            event type.
	 * @param event
	 *            event to deliver.
	 */
	<T extends Event<?>> void fireEvent(Class<T> type, T event);

	/**
	 * Registers an event bus for a particular event type.
	 *
	 * @param type
	 *            event type.
	 * @param bu
	 *            event bus.
	 * @return registration handle.
	 */
	Registration registerDispatcher(Class<? extends Event<?>> type, Dispatcher<?> dispatcher);

}
