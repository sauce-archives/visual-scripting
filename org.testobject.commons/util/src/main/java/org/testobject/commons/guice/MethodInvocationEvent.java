package org.testobject.commons.guice;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.testobject.commons.bus.Emits;
import org.testobject.commons.bus.Event;
import org.testobject.commons.bus.EventEmitter;
import org.testobject.commons.bus.EventSource;
import org.testobject.commons.bus.HasEvents;

import com.google.inject.Injector;

/**
 * 
 * @author enijkamp
 *
 */
public interface MethodInvocationEvent {

	@Emits(MethodInvocationEvent.Response.class)
	public static class Dispatcher implements Request.Handler, HasEvents {
		
		private final EventEmitter emitter;
		private final Injector injector;

		public Dispatcher(EventEmitter emitter, Injector injector) {
			this.emitter = emitter;
			this.injector = injector;
		}

		@Override
		public void handleRequest(Request request) {
			Object object = injector.getInstance(request.method.getDeclaringClass());
			try {
				Object returnValue = request.method.invoke(object, request.parameters.toArray());
				emitter.emit(new Response(request, returnValue));
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public EventSource getEventSource() {
			return emitter;
		}
	}
	
	public static class Request implements Event<Request.Handler> {
		
		public static interface Handler extends Event.Handler {
			
			void handleRequest(Request request);
			
		}
		
		private final int id;
		private final Method method;
		private final List<Object> parameters;
		
		public Request(int id, Method method, List<Object> parameters) {
			this.id = id;
			this.method = method;
			this.parameters = parameters;
		}
		
		public int getId() {
			return id;
		}
		
		public Method getMethod() {
			return method;
		}

		public List<Object> getParameters() {
			return parameters;
		}

		@Override
		public void dispatch(Handler handler) {
			handler.handleRequest(this);
		}
	}
	
	public static class Response implements Event<Response.Handler> {
		
		public static interface Handler extends Event.Handler {
			void handleResponse(Response response);
		}
		
		private final Request request;
		private final Object returnValue;
		
		public Response(Request request, Object returnValue) {
			this.request = request;
			this.returnValue = returnValue;
		}

		public Request getRequest() {
			return request;
		}

		public Object getReturnValue() {
			return returnValue;
		}

		@Override
		public void dispatch(Handler handler) {
			handler.handleResponse(this);
		}
	}
}