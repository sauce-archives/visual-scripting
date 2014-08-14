package org.testobject.kernel.imgproc.blob;

import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.testobject.commons.util.collections.Maps;

/**
 * 
 * @author enijkamp
 *
 */
public class Neighbours {
	
	public static Map<Blob, List<Blob>> findNeighbours(Blob blob) {
		
		Map<Blob, List<Blob>> neighbours = Maps.newIdentityMap();
		
		findNeighbours(blob.children, neighbours);
		
		return neighbours;
	}

	// TODO improve -> scan only intersection of bounding boxes (only these pixel can touch each other) (en)
	private static void findNeighbours(List<Blob> children,	Map<Blob, List<Blob>> neighbours) {
		
		// FIXME we don't need the full cartesian product here to get symmetry (en)
		for(Blob child1 : children) {
			for(Blob child2 : children) {
				if(child1 != child2) {
					if(touches(child1.getBoundingBox(), child2.getBoundingBox())) {
						if(touches(child1, child2)) {
							add(neighbours, child1, child2);
						}
					}
				}
			}
			findNeighbours(child1.children, neighbours);
		}
		
	}

	private static boolean touches(Blob child1, Blob child2) {
		
		//  a b c
		//  d   e
		//  f g h
		
		Rectangle box1 = child1.getBoundingBox();
		
		for(int y = box1.y; y < box1.y + box1.height; y++) {
			for(int x = box1.x; x < box1.x + box1.width; x++) {
				
				// a
				if(y > 0 && x > 0) {
					if(child1.ids[y-1][x-1] == child2.id) {
						return true;
					}
				}
				
				// b
				if(y > 0) {
					if(child1.ids[y-1][x] == child2.id) {
						return true;
					}
				}
				
				// c
				if(y > 0 && x+1 < child1.ids[0].length) {
					if(child1.ids[y-1][x+1] == child2.id) {
						return true;
					}
				}
				
				// d
				if(x > 0) {
					if(child1.ids[y][x-1] == child2.id) {
						return true;
					}
				}
				
				// e
				if(x+1 < child1.ids[0].length) {
					if(child1.ids[y][x+1] == child2.id) {
						return true;
					}
				}
				
				// f
				if(y+1 < child1.ids.length && x > 0) {
					if(child1.ids[y+1][x-1] == child2.id) {
						return true;
					}
				}
				
				// g
				if(y+1 < child1.ids.length) {
					if(child1.ids[y+1][x] == child2.id) {
						return true;
					}
				}
				
				// h
				if(y+1 < child1.ids.length && x+1 < child1.ids[0].length) {
					if(child1.ids[y+1][x+1] == child2.id) {
						return true;
					}
				}
			}
		}
		
		return false;
	}

	private static void add(Map<Blob, List<Blob>> neighbours, Blob child1, Blob child2) {
		if(neighbours.containsKey(child1) == false) {
			neighbours.put(child1, new LinkedList<Blob>());
		}
		
		neighbours.get(child1).add(child2);
	}

	private static boolean touches(Rectangle rect1, Rectangle rect2) {
		if(rect1.intersects(rect2)) {
			return true;
		}
		
		if((rect1.x + rect1.width) - rect2.x == 0) {
			return true;
		}
		
		if((rect2.x + rect2.width) - rect1.x == 0) {
			return true;
		}
		
		if((rect1.y + rect1.height) - rect2.y == 0) {
			return true;
		}
		
		if((rect2.y + rect2.height) - rect1.y == 0) {
			return true;
		}
		
		return false;
	}

}
