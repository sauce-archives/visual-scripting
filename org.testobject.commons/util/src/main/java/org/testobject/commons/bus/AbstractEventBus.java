package org.testobject.commons.bus;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author enijkamp
 *
 */
public abstract class AbstractEventBus implements EventBus {

	protected final ConcurrentHashMap<Class<? extends Event<?>>, ConcurrentHashMap<Integer, Dispatcher<?>>> dispatcherByTypeById = new ConcurrentHashMap<Class<? extends Event<?>>, ConcurrentHashMap<Integer, Dispatcher<?>>>();
	protected final AtomicInteger uniqueId = new AtomicInteger(1);

	public static final class Key<T> {
		public final int id;
		public final Class<? extends Event<?>> type;
		public final T target;
		private final ConcurrentHashMap<Class<? extends Event<?>>, ConcurrentHashMap<Integer, T>> registry;

		public Key(int id, Class<? extends Event<?>> type, T subject,
				ConcurrentHashMap<Class<? extends Event<?>>, ConcurrentHashMap<Integer, T>> registry) {
			this.id = id;
			this.type = type;
			this.target = subject;
			this.registry = registry;
		}

		@Override
		public int hashCode() {
			return id;
		}

		@Override
		public boolean equals(Object other) {
			return this == other;
		}
	}

	protected class SimpleRegistration<T> implements Registration {
		private final Key<T> key;

		public SimpleRegistration(Key<T> key) {
			this.key = key;

			// register with bus
			final ConcurrentHashMap<Integer, T> r = new ConcurrentHashMap<Integer, T>();
			ConcurrentHashMap<Integer, T> h = key.registry.putIfAbsent(key.type, r);
			if (h == null) {
				h = r;
			}
			h.put(key.id, key.target);
		}

		@Override
		public void unregister() {
			final ConcurrentHashMap<Integer, Dispatcher<?>> h = dispatcherByTypeById.get(key.type);
			if (h != null) {
				h.remove(key.id);
			}
		}
	}

	@Override
	public Registration registerDispatcher(Class<? extends Event<?>> type, Dispatcher<?> dispatcher) {
		return new SimpleRegistration<Dispatcher<?>>(new Key<Dispatcher<?>>(uniqueId.getAndIncrement(), type, dispatcher,
				dispatcherByTypeById));
	}

}
