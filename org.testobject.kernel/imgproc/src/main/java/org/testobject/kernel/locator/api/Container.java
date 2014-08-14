package org.testobject.kernel.locator.api;

/**
 * 
 * @author enijkamp
 *
 */
public interface Container extends Locator
{
	Locator[] getChilds();
}