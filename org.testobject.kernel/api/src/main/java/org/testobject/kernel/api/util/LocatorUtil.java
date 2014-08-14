package org.testobject.kernel.api.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.collections.Lists;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.imaging.segmentation.Mask;

/**
 * 
 * @author enijkamp
 *
 */
// FIXME this is duplicate code with NodeUtil (en)
public class LocatorUtil {
	
	public static LinkedList<Locator.Node> locate(Locator.Node node, int x, int y) {
		
		LinkedList<Locator.Node> path = Lists.newLinkedList();
				
		locate(node, x, y, path);
		
		path.addFirst(node);
		
		return path;
	}
	
	public static boolean locate(Locator.Node node, int x, int y, LinkedList<Locator.Node> path) {
		
		// childs
		for(Locator.Node child : node.getChildren()) {
			if(locate(child, x, y, path)) {
				path.addFirst(child);
				return true;
			}
		}

		// mask
		{
			List<Mask> masks = VariableUtil.getMasks(node.getDescriptor().getFeatures());
			Rectangle.Int box = union(toRectangles(masks));
			
			int boxX = x - box.x;
			int boxY = y - box.y;
			
			if(boxX >= 0 && boxY >= 0 && boxX < box.w && boxY < box.h) {
				for(Mask mask : masks) {
					
					int maskX = x - mask.getBoundingBox().x;
					int maskY = y - mask.getBoundingBox().y;
					
					if(maskX >= 0 && maskY >= 0 && maskX < mask.getSize().w && maskY < mask.getSize().h && mask.get(maskX, maskY)) {
						return true;
					}
				}			
			}
		}
		
		return false;
	}
	
	public static String toPathString(List<Locator.Node> nodes) {
		String path = "";
		for(int i = 0; i < nodes.size(); i++) {
			path += nodes.get(i).getDescriptor().getLabel();
			if(i < (nodes.size() - 1)) {
				path += "->";
			}
		}

		return path;
	}
	
	public static Rectangle.Int union(List<Rectangle.Int> rectangles) {
		if(rectangles.isEmpty()) {
			throw new IllegalArgumentException();
		}
		
		Rectangle.Int union = new Rectangle.Int(rectangles.get(0));
		for(int i = 1; i < rectangles.size(); i++) {
			union = union.union(rectangles.get(i));
		}
		return union;
	}

	public static List<Rectangle.Int> toRectangles(List<? extends Mask> masks) {
		List<Rectangle.Int> rects = new ArrayList<>(masks.size());
		for(Mask mask : masks) {
			rects.add(mask.getBoundingBox());
		}
		return rects;
	}
	
	public static LinkedList<Locator.Descriptor> toDescriptors(LinkedList<Locator.Node> source) {
		LinkedList<Locator.Descriptor> target = Lists.newLinkedList();
		for (Locator.Node node : source) {
			target.add(node.getDescriptor());
		}
		
		return target;
	}
}
