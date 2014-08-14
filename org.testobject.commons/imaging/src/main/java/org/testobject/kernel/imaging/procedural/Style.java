package org.testobject.kernel.imaging.procedural;


/**
 * 
 * @author enijkamp
 *
 */
public interface Style {
	
	Fill getFill();
	
	Stroke getStroke();
	
	interface Fill {
		
		class Builder {
			public static Fill none() {
				return new NoneFill();
			}
			
			public static Fill color(Color color) {
				return new BasicFill(color);
			}
		}
	}

	interface Stroke {
		
		class Builder {
			public static Stroke none() {
				return new NoneStroke();
			}
			
			public static Stroke color(Color color) {
				return new BasicStroke(color);
			}
		}
	}
	
	class NoneFill implements Fill {
		@Override
		public String toString() {
			return "none";
		}
	}
	
	class NoneStroke implements Stroke {
		@Override
		public String toString() {
			return "none";
		}
	}
	
	class BasicFill implements Fill {
		public final Color color;

		public BasicFill(Color color) {
			this.color = color;
		}
		
		@Override
		public String toString() {
			return color.toString();
		}
	}
	
	class BasicStroke implements Stroke {
		public final Color color;

		public BasicStroke(Color color) {
			this.color = color;
		}
		
		@Override
		public String toString() {
			return color.toString();
		}
	}
	
	class Builder {
		
		public static Style style(Style.Fill fill, Style.Stroke stroke) {
			return new Impl.StyleImpl(fill, stroke);
		}
		
		public static Style style(Style.Fill fill) {
			return new Impl.StyleImpl(fill, Style.Stroke.Builder.none());
		}
		
		public static Style.Fill fill(Color color) {
			return Style.Fill.Builder.color(color);
		}
		
		public static Style.Stroke stroke(Color color) {
			return Style.Stroke.Builder.color(color);
		}
		
		public static Color rgb(int r, int g, int b) {
			return new Color(r, g, b, 255);
		}
		
		public static Color rgba(int r, int g, int b, int a) {
			return new Color(r, g, b, a);
		}
	
		interface Impl {
			
			class StyleImpl implements Style {
				
				private final Style.Fill fill;
				private final Style.Stroke stroke;
	
				public StyleImpl(Style.Fill fill, Style.Stroke stroke) {
					this.fill = fill;
					this.stroke = stroke;
				}
	
				@Override
				public Style.Fill getFill() {
					return fill;
				}
				
				@Override
				public Style.Stroke getStroke() {
					return stroke;
				}
			}
		}
	
	}

}
