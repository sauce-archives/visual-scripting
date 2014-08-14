package org.testobject.kernel.imgproc.classifier;

import org.testobject.commons.util.image.Image;

public class Context {

	public final Image.Int before;
	public final Image.Int after;

	public Context(Image.Int before, Image.Int after) {
		this.before = before;
		this.after = after;
	}
}