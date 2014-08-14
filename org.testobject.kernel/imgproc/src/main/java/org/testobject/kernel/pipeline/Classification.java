package org.testobject.kernel.pipeline;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.locator.api.Locator;
import org.testobject.kernel.locator.api.Root;
import org.testobject.commons.util.image.Image;

/**
 * 
 * @author enijkamp
 * 
 */
public interface Classification {
	
	Blob toBlob(Image.Int image, Stages stages);
	
	Root toLocator(Image.Int image, Blob blob, Map<Locator, Blob> locatorToBlob, Map<Blob, Locator> blobToLocator);

	LinkedList<Locator> pathToLocator(Image.Int image, List<Blob> blobs);

}
