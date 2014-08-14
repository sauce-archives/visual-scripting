package org.testobject.kernel.locator.api;

import org.testobject.kernel.locator.api.annotations.Named;

/**
 * 
 * @author enijkamp
 *
 */
public class Icon implements Locator
{
	public final String name;

	public Icon(@Named("name") String label)
	{
		this.name = label;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "['" + name + "']";
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof Icon == false)
		{
			return false;
		}
		Icon icon = (Icon) other;
		if (!name.equals(icon.name))
		{
			return false;
		}
		return true;
	}
}