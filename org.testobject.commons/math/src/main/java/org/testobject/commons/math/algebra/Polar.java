package org.testobject.commons.math.algebra;


public class Polar {

	public final double angle;
	public final double distance;

	public Polar(double angle, double distance) {
		this.angle = angle;
		this.distance = distance;
	}

	public Polar(Point.Double p, Point.Double q) {
		this.angle = Math.atan2(p.x - q.x, p.y - q.y);
		this.distance = Math.sqrt(Math.pow(p.x - q.x, 2) + Math.pow(p.y - q.y, 2));
	}

	@Override
	public String toString() {
		return String.format("%s[%s,%s]", Polar.class.getCanonicalName(), angle, distance);
	}

}
