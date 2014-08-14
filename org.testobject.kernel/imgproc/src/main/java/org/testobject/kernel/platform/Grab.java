package org.testobject.kernel.platform;

import org.testobject.commons.util.image.Image;

/**
 * 
 * @author nijkamp
 * 
 */
public interface Grab extends org.testobject.commons.util.lifecycle.Closable
{
	boolean grab();

	void reset();

	Image.Int getImage();
}
