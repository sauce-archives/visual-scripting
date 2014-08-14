package org.testobject.kernel.locator.api;

import java.awt.image.BufferedImage;

import org.testobject.kernel.locator.api.annotations.Named;

/**
 * 
 * @author enijkamp
 *
 */
public class IconButton implements Locator
{
	// FIXME Image.Int? but type is not exposed by public api (en)
	public final BufferedImage image;

	public IconButton(@Named("image") BufferedImage image)
	{
		this.image = image;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}
}