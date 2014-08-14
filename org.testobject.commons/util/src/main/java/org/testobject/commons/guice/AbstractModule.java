package org.testobject.commons.guice;

import com.google.inject.Injector;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * 
 * @author enijkamp
 *
 */
public abstract class AbstractModule extends com.google.inject.AbstractModule {
	
	protected static FactoryModuleBuilder factory() {
		return new FactoryModuleBuilder();
	}
	
	@SuppressWarnings("unchecked")
	protected static <T> T getInstanceByClassName(Injector injector, String className) {
		try {
			Class<?> cls = Class.forName(className);
			return (T) injector.getInstance(cls);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Unable to get instance for class '" + className + "'");
		}
	}

}
