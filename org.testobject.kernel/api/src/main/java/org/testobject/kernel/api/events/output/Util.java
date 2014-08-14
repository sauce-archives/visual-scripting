package org.testobject.kernel.api.events.output;

import java.util.Iterator;

import org.testobject.kernel.api.classification.graph.Locator;

/**
 * 
 * @author enijkamp
 *
 */
public class Util {
	
	public static String toString(Locator.Qualifier qualifier) {
		String path = "";
		if (qualifier.getPath().isEmpty()) {
			return path;
		} else {
			Iterator<Locator.Descriptor> iter = qualifier.getPath().iterator();
			for (int i = 0; i < qualifier.getPath().size() - 1; i++) {
				Locator.Descriptor widget = iter.next();
				path += widget + "->";
			}
			return path + iter.next().toString();
		}
	}
	
}