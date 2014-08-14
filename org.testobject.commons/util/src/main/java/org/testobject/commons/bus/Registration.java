package org.testobject.commons.bus;

/**
 * Handle to a registered resource. Allows one to unregister it. Used in many places, including (but not limited to) registering of event
 * handlers.
 */
public interface Registration
{
	void unregister();
}
