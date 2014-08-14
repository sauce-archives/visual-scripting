package org.testobject.kernel.locator.api;

import org.testobject.kernel.locator.api.annotations.Named;

/**
 * 
 * @author enijkamp
 *
 */
public class Label implements Locator
{
	public final String label;

	public Label(@Named("label") String label)
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
		if (other instanceof Label == false)
		{
			return false;
		}
		Label text = (Label) other;
		return label.equals(text.label);
	}
}