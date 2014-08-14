package org.testobject.commons.bus;

import java.lang.annotation.*;

/**
 * 
 * @author enijkamp
 *
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Emits {
	
	Class<? extends Event<?>>[] value();
	
}
