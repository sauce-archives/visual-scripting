package org.testobject.commons.bus;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TestTypeErasure {

	public interface Event<T> {

	}

	public class MyEvent implements Event<Integer> {

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Event<String> event = new Event<String>() {

		};

		for (Type superr : event.getClass().getGenericInterfaces()) {
			ParameterizedType pt = (ParameterizedType) superr;
			System.out.println(pt.getActualTypeArguments()[0]);
			System.out.println(superr.getClass());
			System.out.println(superr);
		}
	}
}
