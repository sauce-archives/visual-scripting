package org.testobject.kernel.imaging.segmentation;

import java.util.ArrayList;
import java.util.List;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.math.algebra.Size;
import org.testobject.commons.math.algebra.Size.Int;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imaging.procedural.Color;

/**
 * 
 * @author enijkamp
 *
 */
public interface Mask extends BooleanRaster, HasBoundingBox {

	class Builder {
		
		public static Mask none() {
			return new Mask() {
				@Override
				public Size.Int getSize() {
					return getBoundingBox().getSize();
				}

				@Override
				public boolean get(int x, int y) {
					return false;
				}

				@Override
				public void set(int x, int y, boolean what) {
					throw new UnsupportedOperationException();
				}

				@Override
				public org.testobject.commons.math.algebra.Rectangle.Int getBoundingBox() {
					return org.testobject.commons.math.algebra.Rectangle.Int.ZERO;
				}
				
				@Override
				public String toString() {
					return getBoundingBox().toString();
				}
			};
		}
		
		public static Mask create(final Rectangle.Int box) {
			
			return new Mask() {

				@Override
				public Size.Int getSize() {
					return box.getSize();
				}

				@Override
				public boolean get(int x, int y) {
					return box.contains(x, y);
				}

				@Override
				public void set(int x, int y, boolean what) {
					throw new UnsupportedOperationException();
				}

				@Override
				public Rectangle.Int getBoundingBox() {
					return box;
				}
				
				@Override
				public String toString() {
					return getBoundingBox().toString();
				}
			};
		}
		
		public static Mask create(final org.testobject.commons.util.image.Image.Int image) {
			return new Mask() {
				@Override
				public org.testobject.commons.math.algebra.Size.Int getSize() {
					return getBoundingBox().getSize();
				}

				@Override
				public boolean get(int x, int y) {
					return image.get(x, y) != Color.Builder.transparent().toRgb();
				}

				@Override
				public void set(int x, int y, boolean what) {
					throw new UnsupportedOperationException();
				}

				@Override
				public org.testobject.commons.math.algebra.Rectangle.Int getBoundingBox() {
					return new Rectangle.Int(0, 0, image.w, image.h);
				}
			};
		}
		
		public static Mask create(final Image.Int image, final Rectangle.Int bbox) {
			
			return new Mask() {

				@Override
				public Size.Int getSize() {
					return bbox.getSize();
				}

				@Override
				public boolean get(int x, int y) {
					return image.get(x, y) != Color.Builder.transparent().toRgb();
				}

				@Override
				public void set(int x, int y, boolean what) {
					throw new UnsupportedOperationException();
				}

				@Override
				public Rectangle.Int getBoundingBox() {
					return bbox;
				}
				
				@Override
				public String toString() {
					return getBoundingBox().toString();
				}
			};
		}
		
		public static Mask create(final Image.Bool image, final Rectangle.Int box) {
			
			return new Mask() {

				@Override
				public Size.Int getSize() {
					return box.getSize();
				}

				@Override
				public boolean get(int x, int y) {
					return image.get(x, y);
				}

				@Override
				public void set(int x, int y, boolean what) {
					throw new UnsupportedOperationException();
				}

				@Override
				public Rectangle.Int getBoundingBox() {
					return box;
				}
				
				@Override
				public String toString() {
					return getBoundingBox().toString();
				}
			};
		}
		
		public static Mask create(final Blob blob) {
			
			final Rectangle.Int box = blob.getBoundingBox();
			
			return new Mask() {

				@Override
				public Size.Int getSize() {
					return box.getSize();
				}

				@Override
				public boolean get(int x, int y) {
					return blob.get(x, y);
				}

				@Override
				public void set(int x, int y, boolean what) {
					throw new UnsupportedOperationException();
				}

				@Override
				public Rectangle.Int getBoundingBox() {
					return box;
				}
				
				@Override
				public String toString() {
					return getBoundingBox().toString();
				}
			};
		}
		
		public static Mask create(final List<? extends Mask> masks) {
			
			final Rectangle.Int box = Rectangle.Int.union(toRectangles(masks));
			
			return new Mask() {

				@Override
				public Size.Int getSize() {
					return box.getSize();
				}

				@Override
				public boolean get(int x, int y) {
					
					int globalX = x + box.x;
					int globalY = y + box.y;
					
					if(box.contains(globalX, globalY) == false) {
						return false;
					}
					
					for(Mask mask : masks) {
						if(mask.getBoundingBox().contains(globalX, globalY)) {
							int localX = globalX - mask.getBoundingBox().x;
							int localY = globalY - mask.getBoundingBox().y;
							if(mask.get(localX, localY)) {
								return true;
							}
						}
					}
					
					return false;
				}

				@Override
				public void set(int x, int y, boolean what) {
					throw new UnsupportedOperationException();
				}

				@Override
				public Rectangle.Int getBoundingBox() {
					return box;
				}
				
				@Override
				public String toString() {
					return getBoundingBox().toString();
				}
			};
		}
		
		public static Mask merge(final List<? extends Mask> masks) {
			
			final Rectangle.Int box = Rectangle.Int.union(toRectangles(masks));
			
			final boolean[][] raster = new boolean[box.h][box.w];
			
			for(int y = 0; y < box.h; y++) {
				for(int x = 0; x < box.w; x++) {
					raster[y][x] = contains(masks, box, x, y);
				}
			}
			
			return new Mask() {

				@Override
				public Size.Int getSize() {
					return box.getSize();
				}

				@Override
				public boolean get(int x, int y) {
					int globalX = x + box.x;
					int globalY = y + box.y;
					
					if(box.contains(globalX, globalY) == false) {
						return false;
					}
					
					return raster[y][x];
				}

				@Override
				public void set(int x, int y, boolean what) {
					throw new UnsupportedOperationException();
				}

				@Override
				public Rectangle.Int getBoundingBox() {
					return box;
				}
				
				@Override
				public String toString() {
					return getBoundingBox().toString();
				}
			};
		}

		private static boolean contains(List<? extends Mask> masks, Rectangle.Int global, int x, int y) {
			for(Mask mask : masks) {
				if(mask.getBoundingBox().contains(global.x + x, global.y + y)) {
					int localX = global.x + x - mask.getBoundingBox().x;
					int localY = global.y + y - mask.getBoundingBox().y;
					if(mask.get(localX, localY)) {
						return true;
					}
				}
			}
			
			return false;
		}

		private static List<Rectangle.Int> toRectangles(List<? extends Mask> masks) {
			List<Rectangle.Int> rects = new ArrayList<>(masks.size());
			for(Mask mask : masks) {
				rects.add(mask.getBoundingBox());
			}
			return rects;
		}

		public static Mask create(final Mask parent, final Rectangle.Int relativeSubBox) {
			
			final Rectangle.Int parentBox = parent.getBoundingBox();
			final Rectangle.Int absoluteSubBox = new Rectangle.Int(parentBox.x + relativeSubBox.x, parentBox.y + relativeSubBox.y, relativeSubBox.w, relativeSubBox.h);
			final Rectangle.Int box = parentBox.intersection(absoluteSubBox);
			
			return new Mask() {
				@Override
				public Size.Int getSize() {
					return box.getSize();
				}

				@Override
				public boolean get(int x, int y) {
					int globalX = box.x + x;
					int globalY = box.y + y;
					if(box.contains(globalX, globalY) == false) {
						return false;
					}
					
					int localX = globalX - parentBox.x;
					int localY = globalY - parentBox.y;
					
					return parent.get(localX, localY);
				}

				@Override
				public void set(int x, int y, boolean what) {
					throw new UnsupportedOperationException();				
				}

				@Override
				public Rectangle.Int getBoundingBox() {
					return box;
				}
				
				@Override
				public String toString() {
					return getBoundingBox().toString();
				}
			};
		}

		public static Mask translate(final Mask mask, final int dx, final int dy) {
			
			final Rectangle.Int bbox = new Rectangle.Int(mask.getBoundingBox().x + dx, mask.getBoundingBox().y + dy, mask.getBoundingBox().w, mask.getBoundingBox().h);
			
			return new Mask() {
				@Override
				public Int getSize() {
					return mask.getSize();
				}

				@Override
				public boolean get(int x, int y) {
					return mask.get(Math.max(0, x + dx), Math.max(0, y + dx));
				}

				@Override
				public void set(int x, int y, boolean what) {
					
				}

				@Override
				public Rectangle.Int getBoundingBox() {
					return bbox;
				}
			};
		}
    }
}
