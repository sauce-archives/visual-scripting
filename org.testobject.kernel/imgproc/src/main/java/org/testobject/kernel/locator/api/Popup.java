package org.testobject.kernel.locator.api;

import org.testobject.kernel.locator.api.annotations.Named;

/**
 * 
 * @author enijkamp
 *
 */
public class Popup implements Container
{
	public final Locator[] childs;

	public Popup(@Named("childs") Locator[] childs)
	{
		this.childs = childs;
	}

	@Override
	public Locator[] getChilds()
	{
		return childs;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}

	// FIXME (en)
	@Override
	public boolean equals(Object other)
	{
		if (other instanceof Popup == false)
		{
			return false;
		}
		return true;
	}
}