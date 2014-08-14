package org.testobject.kernel.locator.api;

import java.util.List;

/**
 * 
 * @author enijkamp
 *
 */
// TODO replace LinkedList<Locator> by Qualifier (en)
public interface Qualifier
{
	Locator getLocator();

	List<Locator> getPath();
}
