package org.testobject.kernel.api.classification.graph;

import java.util.List;

import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.util.collections.Lists;

/**
 * 
 * @author enijkamp
 *
 */
public class Contour {

	public final List<Contour> childs;
    public final List<Point.Double> points;
    
    public Contour(List<Point.Double> points) {
    	this(points, Lists.<Contour>empty());
    }
	
	public Contour(List<Point.Double> points, List<Contour> childs) {
		this.points = points;
		this.childs = childs;
	}
	
	@Override
	public String toString() {
		return points.toString();
	}
}