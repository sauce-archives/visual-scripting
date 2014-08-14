package org.testobject.kernel.imaging.procedural;


public interface Dimension {
	
	class Double {
	
		public final double w, h;
		
		public Double(double w, double h) {
			this.w = w;
			this.h = h;
		}
	
		public double getHeight() {
			return h;
		}
		
		public double getWidth() {
			return w;
		}
	}

}
