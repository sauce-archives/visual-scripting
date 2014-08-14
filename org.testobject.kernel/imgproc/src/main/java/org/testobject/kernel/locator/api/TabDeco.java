package org.testobject.kernel.locator.api;

import org.testobject.kernel.locator.api.annotations.Named;

/**
 * 
 * @author enijkamp
 *
 */
public class TabDeco implements Locator
{
	public final String label;
	public final boolean active;

	public TabDeco(@Named("label") String label, @Named("active") boolean active)
	{
		this.label = label;
		this.active = active;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "['" + label + "'" + (active ? ", active" : "") + "]";
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof TabDeco == false)
		{
			return false;
		}
		TabDeco deco = (TabDeco) other;
		if (label.equals(deco.label) == false)
		{
			return false;
		}
		if (active != deco.active)
		{
			return false;
		}
		return true;
	}
}