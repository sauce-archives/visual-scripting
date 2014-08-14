package org.testobject.commons.util.functional;

/**
 * 
 * @author enijkamp
 *
 */
public interface Functions {
	
	interface VoidFunction0 {
		void apply();
	}
	
	interface VoidFunction1<A1> {
		void apply(A1 a1);
	}

	interface VoidFunction2<A1, A2> {
		void apply(A1 a1, A2 a2);
	}
	
	interface Function0<R> {
		R apply();
	}
	
	interface Function1<R, A1> {
		R apply(A1 a1);
	}

	interface Function2<R, A1, A2> {
		R apply(A1 a1, A2 a2);
	}

}
