package org.testobject.kernel.locator.api;

import org.testobject.kernel.locator.api.annotations.Named;

/**
 * 
 * @author enijkamp
 *
 */
public class Tab implements Container
{
	public final TabDeco decoration;
	public final TabContent content;

	public Tab(@Named("decoration") TabDeco decoration, @Named("content") TabContent content)
	{
		this.decoration = decoration;
		this.content = content;
	}

	@Override
	public Locator[] getChilds()
	{
		return new Locator[] { decoration, content };
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}

	@Override
	public boolean equals(Object other)
	{
		return other instanceof Tab;
	}
}