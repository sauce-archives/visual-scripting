package org.testobject.kernel.api.events.input;

public interface Event {
	
	interface Callback {
		
		interface Handle {
			void release();
		}
		
		class Stub implements Event.Callback{
			@Override
			public void onEvent(Event event) { }
			
		}
		
		void onEvent(Event event);
		
	}
}