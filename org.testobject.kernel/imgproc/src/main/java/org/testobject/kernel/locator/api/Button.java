package org.testobject.kernel.locator.api;

import org.testobject.kernel.locator.api.annotations.Named;

/**
 * 
 * @author enijkamp
 *
 */
public class Button implements Locator
{
	public final String label;

	public Button(@Named("label") String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "['" + label + "']";
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof Button == false)
		{
			return false;
		}
		Button button = (Button) other;
		if (!label.equals(button.label))
		{
			return false;
		}
		return true;
	}
}