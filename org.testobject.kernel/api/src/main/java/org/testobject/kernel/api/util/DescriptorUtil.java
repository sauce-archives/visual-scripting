package org.testobject.kernel.api.util;

import java.util.List;

import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.math.algebra.Size;
import org.testobject.kernel.api.classification.graph.Contour;
import org.testobject.kernel.api.classification.graph.Locator.Descriptor;
import org.testobject.kernel.imaging.fingerprint.ImageFingerprint;
import org.testobject.kernel.imaging.procedural.Color;
import org.testobject.kernel.imaging.segmentation.Mask;

/**
 * 
 * @author enijkamp
 *
 */
public class DescriptorUtil {
	
	public static Color getFill(Descriptor descriptor) {
		return VariableUtil.getFill(descriptor.getFeatures());
	}
	
	public static ImageFingerprint getFingerprint(Descriptor descriptor) {
		return VariableUtil.getFingerprint(descriptor.getFeatures());
	}
	
	public static List<Contour> getContours(Descriptor descriptor) {
		return VariableUtil.getContours(descriptor.getFeatures());
	}
	
	public static Point.Int getPosition(Descriptor descriptor) {
		return VariableUtil.getPosition(descriptor.getFeatures());
	}

	public static Size.Int getSize(Descriptor descriptor) {
		return VariableUtil.getSize(descriptor.getFeatures());
	}
	
	public static List<Mask> getMasks(Descriptor descriptor) {
		return VariableUtil.getMasks(descriptor.getFeatures());
	}

	public static Rectangle.Int getBoundingBox(Descriptor locator) {
		Point.Int position = getPosition(locator);
		Size.Int size = getSize(locator);
		return new Rectangle.Int(position.x, position.y, size.w, size.h);
	}

}
