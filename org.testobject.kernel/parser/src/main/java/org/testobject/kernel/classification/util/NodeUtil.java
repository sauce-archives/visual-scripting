package org.testobject.kernel.classification.util;

import java.util.LinkedList;
import java.util.List;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.collections.Lists;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.imaging.segmentation.Mask;

/**
 * 
 * @author enijkamp
 *
 */
public class NodeUtil {
	
	public static LinkedList<Node> locate(Node node, int x, int y) {
		
		LinkedList<Node> path = Lists.newLinkedList();
				
		locate(node, x, y, path);
		
		path.addFirst(node);
		
		return path;
	}
	
	public static boolean locate(Node node, int x, int y, LinkedList<Node> path) {
		
		// childs
		for(Node child : node.getChildren()) {
			if(locate(child, x, y, path)) {
				path.addFirst(child);
				return true;
			}
		}

		// mask
		{
			List<Mask> masks = node.getElement().getMasks();
			Rectangle.Int box = node.getElement().getBoundingBox();
			
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
	
	public static String toPathString(List<Node> nodes) {
		String path = "";
		for(int i = 0; i < nodes.size(); i++) {
			path += nodes.get(i).getElement().getLabel().getQualifier();
			if(i < (nodes.size() - 1)) {
				path += "->";
			}
		}

		return path;
	}

}
