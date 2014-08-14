package org.testobject.kernel.imaging.procedural;

/**
 * 
 * @author enijkamp
 *
 */
public interface Layout {
	
	interface Align extends Layout {
		
		boolean getVertical();
		
		boolean getHorizontal();
		
	}
	
	
	class Builder {
		
		public static Builder layout() {
			return new Builder();
		}
		
		public Layout none() {
			return new Layout() {
				
			};
		}
		
		public Layout centered() {
			return new Layout.Align() {
				@Override
				public boolean getVertical() {
					return true;
				}
				
				@Override
				public boolean getHorizontal() {
					return true;
				}
			};
		}
	}

}
