package org.testobject.kernel.locator.api;

import org.testobject.kernel.locator.api.annotations.Named;

/**
 * 
 * @author enijkamp
 *
 */
public class TabContent implements Container
{
	public final Locator[] childs;

	public TabContent(@Named("childs") Locator[] childs)
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

	@Override
	public boolean equals(Object other)
	{
		return other instanceof TabContent;
	}
}