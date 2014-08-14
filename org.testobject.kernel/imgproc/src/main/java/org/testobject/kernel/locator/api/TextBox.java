package org.testobject.kernel.locator.api;

/**
 * 
 * @author enijkamp
 *
 */
public class TextBox implements Locator
{
	public final String text;

	public TextBox(String text)
	{
		this.text = text;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "['" + text + "']";
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof TextBox == false)
		{
			return false;
		}
		TextBox box = (TextBox) other;
		if (text.equals(box.text) == false)
		{
			return false;
		}
		return true;
	}
}