package org.testobject.kernel.locator.api;

import org.testobject.kernel.locator.api.annotations.Named;

/**
 * 
 * @author enijkamp
 *
 */
public class Image implements Locator
{

	public final String name;

	public Image(@Named("name") String label)
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
		if (other instanceof Image == false)
		{
			return false;
		}
		Image icon = (Image) other;
		if (!name.equals(icon.name))
		{
			return false;
		}
		return true;
	}

}
