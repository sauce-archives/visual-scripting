package org.testobject.kernel.locator.api;

import org.testobject.kernel.locator.api.annotations.Named;

/**
 * 
 * @author enijkamp
 *
 */
public class TabPanel implements Container
{
	public final Locator[] tabs;

	public TabPanel(@Named("tabs") Locator[] tabs)
	{
		this.tabs = tabs;
	}

	@Override
	public Locator[] getChilds()
	{
		return tabs;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}

	@Override
	public boolean equals(Object other)
	{
		return other instanceof TabPanel;
	}
}