package org.testobject.kernel.imaging.segmentation;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.math.algebra.Size;
import org.testobject.commons.util.image.Image;

/**
 * 
 * @author enijkamp
 *
 */
public interface BlobUtils {
	
	class Print {
		
		public static void printBlobs(Blob blob) {
			printBlobs(blob, 0);
		}
		
		public static void printBlobs(Blob blob, int level) {
			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < level; i++) {
				sb.append("  ");
			}

			sb.append(blob.toString());

			System.out.println(sb.toString());

			for (Blob c : blob.children) {
				printBlobs(c, level + 1);
			}
		}
	}
	
	class Draw {
		
		private static final Color[] colors = generateColors();

		public static Color[] generateColors() {
			Random random = new Random(0);
			final int length = 512;
			Color[] colors = new Color[length];
			for (int i = 0; i < length; i++) {
				colors[i] = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
			}
			return colors;
		}
		
		public static void drawHierarchy(Blob[] blobs, Image.Int image) {
			Blob blob = blobs[0];
			int width = blob.bbox.w;
			int height = blob.bbox.h;

			int[] pixels = image.pixels;

			for (int y = 0; y < height; y++) {
				final int[] row = blob.ids[y];
				for (int x = 0; x < width; x++) {
					int id = row[x];
					Blob currentBlob = blobs[id];
					int position = currentBlob.bbox.y * image.w + currentBlob.bbox.x + currentBlob.bbox.w + currentBlob.bbox.h;
					Color c = colors[position % colors.length];
					pixels[y * width + x] = c.getRGB();
				}
			}
		}

		public static Image.Int drawHierarchy(Blob[] blobs) {
			Blob blob = blobs[0];
			int width = blob.bbox.w;
			int height = blob.bbox.h;
			
			int[] pixels = new int[width * height];
			Image.Int image = new Image.Int(pixels, 0, 0, width, height, width, Image.Int.Type.RGB);
			
			drawHierarchy(blobs, image);

			return image;
		}
		
		public static Image.Int drawHierarchy(Blob blob) {
			Blob[] blobs = Auxiliary.collectBlobs(blob);

			int width = blob.bbox.w;
			int height = blob.bbox.h;

			int[] pixels = new int[width * height];
			Image.Int image = new Image.Int(pixels, 0, 0, width, height, width, Image.Int.Type.RGB);

			for (int y = 0; y < height; y++) {
				final int[] row = blob.ids[y];
				for (int x = 0; x < width; x++) {
					int id = row[x];
					Blob currentBlob = blobs[id];
					int position = currentBlob.bbox.y * image.w + currentBlob.bbox.x + currentBlob.bbox.w + currentBlob.bbox.h;
					Color c = colors[position % colors.length];
					pixels[y * width + x] = c.getRGB();
				}
			}
			
			return image;
		}
		
		public static BufferedImage drawHierarchyByLevel(Blob blob) {
			int width = blob.bbox.w;
			int height = blob.bbox.h;

			// Now we have a hierarchy of blobs, represented by root and tracker
			// root knows containment relations, while tracker labels each pixel as
			// belonging to a particular blob
			// Lets show a visual representation of the tracker by coloring pixels
			// according to the blob distance (in the hierarchy)
			// to the root blob.

			// generate some nice colors
			Color[] colors = generateColors();

			// for every blob (identified by its id), find its depth in the
			// hierarchy, root is level 0
			Map<Integer, Integer> levelById = new HashMap<>();
			Auxiliary.findBlobLevels(levelById, blob, 0);

			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);

			for (int y = 0; y < height; y++) {
				final int[] row = blob.ids[y];
				for (int x = 0; x < width; x++) {
					final int id = row[x];

					Color c = Color.black;
					if (levelById.containsKey(id)) {
						int level = levelById.get(id);
						c = colors[level % colors.length];
					}

					image.setRGB(x, y, c.getRGB());
				}
			}

			return image;
		}

		public static void toBufferedImage(BufferedImage image, BooleanRaster raster, int xOffset, int yOffset, Color color)
		{
			final Size.Int size = raster.getSize();
			final int rgb = color.getRGB();

			for (int x = 0; x < size.w; x++)
			{
				for (int y = 0; y < size.h; y++)
				{
					if (raster.get(x, y))
					{
						image.setRGB(xOffset + x, yOffset + y, rgb);
					}
				}
			}
		}
	}
	
	class Auxiliary {

		public static void findBlobLevels(Map<Integer, Integer> levels, Blob root, int currentLevel) {
			levels.put(root.id, currentLevel);

			for (Blob b : root.children) {
				findBlobLevels(levels, b, currentLevel + 1);
			}
		}
		
	    public static Blob[] collectBlobs(Blob blob) {
	    	Blob[] blobs = new Blob[maxBlobId(blob) + 1];
	    	collectBlobs(blob, blobs);
			return blobs;
		}

		private static void collectBlobs(Blob blob, Blob[] blobs) {
			blobs[blob.id] = blob;
			for(Blob child : blob.children) {
				collectBlobs(child, blobs);
			}
		}
		
		public static int maxBlobId(Blob blob) {
	        int id = blob.id;

	        for (Blob c : blob.children) {
	        	id = Math.max(id, maxBlobId(c));
	        }

	        return id;
	    }

		public static int countBlobs(Blob blob) {
	        int count = 1;

	        for (Blob c : blob.children) {
	            count += countBlobs(c);
	        }

	        return count;
	    }
		
	}
	
	class Locate {

		public static List<Blob> locateWithChilds(Blob root, Rectangle.Int query) {
	        List<Blob> result = new LinkedList<>();
	        for(Blob child : root.children) {
	        	containedWithChilds(child, query, result);
	        }
	        return result;
	    }
		
	    
	    private static void containedWithChilds(Blob blob, Rectangle.Int rect, List<Blob> result) {
	        if(rect.contains(blob.bbox)) {
	        	result.add(blob);
	        }

	        for(Blob child : blob.children) {
	        	containedWithChilds(child, rect, result);
	        }
	    }

		public static List<Blob> locate(Blob root, Rectangle.Int query) {
	        List<Blob> result = new LinkedList<>();
	        for(Blob child : root.children) {
	            contained(child, query, result);
	        }
	        return result;
	    }
		
	    
	    private static void contained(Blob blob, Rectangle.Int rect, List<Blob> result) {
	        if(rect.contains(blob.bbox)) {
	        	result.add(blob);
	        	return;
	        }

	        for(Blob child : blob.children) {
	            contained(child, rect, result);
	        }
	    }
	}
	
	class Cut {

		public static Image.Int cutByMask(Image.Int image, Mask mask) {
			Rectangle.Int box = mask.getBoundingBox();
			Image.Int subimage = new Image.Int(new int[box.w * box.h], 0, 0, box.w, box.h, box.w, Image.Int.Type.ARGB);
	
			int alpha = (255 << 24);
	
			for (int y = 0; y < box.h; y++) {
				for (int x = 0; x < box.w; x++) {
					final int globalX = box.x + x;
					final int globalY = box.y + y;
					if (mask.get(x, y)) {
						subimage.pixels[y * box.w + x] = image.pixels[globalY * image.w + globalX] | alpha;
					}
				}
			}
	
			return subimage;
		}
	
		public static Image.Int cutByContour(Image.Int image, Blob blob) {
			Rectangle.Int box = blob.getBoundingBox();
			Image.Int subimage = new Image.Int(new int[box.w * box.h], 0, 0, box.w, box.h,
			        box.w, Image.Int.Type.ARGB);
	
			int alpha = (255 << 24);
	
			for (int y = box.y; y < box.y + box.h; y++) {
				for (int x = box.x; x < box.x + box.w; x++) {
					if (isAssigned(blob, x, y)) {
						subimage.pixels[(y- box.y) * box.w + (x - box.x)] = image.pixels[y * image.w + x] | alpha;
					}
				}
			}
			return subimage;
		}
		
		private static boolean isAssigned(Blob blob, int x, int y) {
			
			Rectangle.Int bbox = blob.getBoundingBox();
			
			if(bbox.contains(x, y) == false) {
				return false;
			}
			
			if(blob.get(x - bbox.x, y - bbox.y)) {
				return true;
			}
			
			for(Blob child : blob.children) {
				if(isAssigned(child, x, y)) {
					return true;
				}
			}
			
			return false;
		}
	}
}
