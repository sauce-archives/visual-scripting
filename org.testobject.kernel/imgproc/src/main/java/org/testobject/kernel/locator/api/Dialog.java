package org.testobject.kernel.locator.api;

import org.testobject.kernel.locator.api.annotations.Named;

/**
 * 
 * @author enijkamp
 *
 */
public class Dialog implements Container
{
	public final Locator[] childs;
	public final String title;

	public Dialog(@Named("childs") Locator[] childs, @Named("title") String title)
	{
		this.childs = childs;
		this.title = title;
	}

	@Override
	public Locator[] getChilds()
	{
		return childs;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "['" + title + "']";
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof Dialog == false)
		{
			return false;
		}
		Dialog dialog = (Dialog) other;
		if (!title.equals(dialog.title))
		{
			return false;
		}
		return true;
	}
}