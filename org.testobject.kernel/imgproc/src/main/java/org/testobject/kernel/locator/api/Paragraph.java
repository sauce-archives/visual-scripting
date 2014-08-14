package org.testobject.kernel.locator.api;

import java.util.Arrays;

import org.testobject.kernel.locator.api.annotations.Named;

/**
 * 
 * @author enijkamp
 *
 */
public class Paragraph implements Container
{
	// FIXME hack to get identity for tech-demo (en)
	public final int[] blobs;
	
	public final Locator[] childs;

	public Paragraph(@Named("childs") Locator[] childs, int[] blobs)
	{
		this.childs = childs;
		this.blobs = blobs;
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
		if (other instanceof Paragraph == false)
		{
			return false;
		}
		Paragraph paragraph = (Paragraph) other;
		return Arrays.equals(paragraph.blobs, blobs);
	}
}