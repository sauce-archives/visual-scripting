package org.testobject.kernel.imaging.procedural;

/**
 * 
 * @author enijkamp
 *
 */
public interface Transform {
	
	class Identity implements Transform {
		
		@Override
		public String toString() {
			return "identity";
		}
		
	}
	
	class Scale implements Transform {
		
		public final double sx, sy;
		
		public Scale(double sx, double sy) {
			this.sx = sx;
			this.sy = sy;
		}

		public double getScaleX() {
			return sx;
		}

		public double getScaleY() {
			return sy;
		}

		@Override
		public String toString() {
			return "scale";
		}
		
		public static class Builder {
			public static Scale identity() {
				return new Scale(1d, 1d);
			}
		}
	}
	
	class Translate implements Transform {
		
		public final double x, y;

		public Translate(double x, double y) {
			super();
			this.x = x;
			this.y = y;
		}

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}
		
		@Override
		public String toString() {
			return "translate x=" + x + " y=" + y;
		}
		
		public static class Builder {
			public static Translate identity() {
				return new Translate(0d, 0d);
			}
		}
	}
	
	class Rotate implements Transform {
		
	}
	
	class Builder {
		
		public static Translate translate(double x, double y) {
			return new Transform.Translate(x, y);
		}
		
		public static Scale scale(double sx, double sy) {
			return new Transform.Scale(sx, sy);
		}
	}

}
