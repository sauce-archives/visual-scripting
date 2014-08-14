package org.testobject.kernel.locator.api;

import org.testobject.kernel.locator.api.annotations.Named;

/**
 * 
 * @author enijkamp
 *
 */
public class Toolbar implements Container
{
	public final Locator[] icons;

	public Toolbar(@Named("icons") Locator[] icons)
	{
		this.icons = icons;
	}

	@Override
	public Locator[] getChilds()
	{
		return icons;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}

	@Override
	public boolean equals(Object other)
	{
		return other instanceof Toolbar;
	}
}