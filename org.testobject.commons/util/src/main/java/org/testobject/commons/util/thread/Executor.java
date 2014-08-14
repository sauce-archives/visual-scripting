package org.testobject.commons.util.thread;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;
import com.google.inject.name.Named;

@BindingAnnotation
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Executor {

	Class<?> value();

	public static class Factory {
		public static Executor typed(final Class<?> value) {
			return new Executor() {
				@Override
				public Class<? extends Executor> annotationType() {
					return Executor.class;
				}

				@Override
				public Class<?> value() {
					return value;
				}

				@Override
				public int hashCode() {
					// This is specified in java.lang.Annotation.
					return (127 * "value".hashCode()) ^ value.hashCode();
				}

				public boolean equals(Object o) {
					if (!(o instanceof Named)) {
						return false;
					}

					Named other = (Named) o;
					return value.equals(other.value());
				}
			};
		}
	}

}
