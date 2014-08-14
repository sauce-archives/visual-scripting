package org.testobject.kernel.imgproc.blob;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.testobject.commons.util.image.Image;

/**
 * 
 * @author enijkamp
 *
 */
public class BlobUtils {

	private static final Color[] colors = generateColors();

	private static Color[] generateColors() {
		Random random = new Random();
		final int length = 512;
		Color[] colors = new Color[length];
		for (int i = 0; i < length; i++) {
			colors[i] = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
		}
		return colors;
	}

	public static void visit(Blob blob, Visitor visitor)
	{
		visitor.visit(blob);

		for (Blob child : blob.children)
		{
			visit(child, visitor);
		}
	}

	public static void printMeta(Blob blob)
	{
		printMeta(blob, 0);
	}

	public static void printMeta(Blob blob, int level)
	{
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < level; i++)
		{
			sb.append("  ");
		}

		sb.append(blob.meta.getClass().getSimpleName() + " (" + blob.id + ", [x=" + blob.bbox.x + ", y=" + blob.bbox.y + "]" + ")");

		System.out.println(sb.toString());

		for (Blob child : blob.children)
		{
			printMeta(child, level + 1);
		}
	}

	public static void drawHierarchy(Blob[] blobs, Image.Int image) {
		Blob blob = blobs[0];
		int width = blob.bbox.width;
		int height = blob.bbox.height;

		// generate some nice colors
		int[] pixels = image.pixels;

		for (int y = 0; y < height; y++) {
			final int[] row = blob.ids[y];
			for (int x = 0; x < width; x++) {
				final int id = row[x];
				Blob currentBlob = blobs[id];
				int position = currentBlob.bbox.y * image.w + currentBlob.bbox.x + currentBlob.bbox.width + currentBlob.bbox.height;
				Color c = colors[position % colors.length];
				pixels[y * width + x] = c.getRGB();
			}
		}
	}

	public static Image.Int drawHierarchy(Blob[] blobs) {
		Blob blob = blobs[0];
		int width = blob.bbox.width;
		int height = blob.bbox.height;

		// generate some nice colors
		Color[] colors = generateColors();
		int[] pixels = new int[width * height];
		Image.Int coloredImage = new Image.Int(pixels, 0, 0, width, height, width, Image.Int.Type.RGB);

		for (int y = 0; y < height; y++) {
			final int[] row = blob.ids[y];
			for (int x = 0; x < width; x++) {
				final int id = row[x];
				Blob currentBlob = blobs[id];
				int position = currentBlob.bbox.x + currentBlob.bbox.width + currentBlob.bbox.y + currentBlob.bbox.height;
				Color c = colors[position % colors.length];
				pixels[y * width + x] = c.getRGB();
			}
		}

		return coloredImage;
	}

	public static BufferedImage drawHierarchyByLevel(Blob blob) {
		int width = blob.bbox.width;
		int height = blob.bbox.height;

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
		Map<Integer, Integer> levelById = new HashMap<Integer, Integer>();
		findBlobLevels(levelById, blob, 0);

		BufferedImage colorCodedimage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);

		for (int y = 0; y < height; y++) {
			final int[] row = blob.ids[y];
			for (int x = 0; x < width; x++) {
				final int id = row[x];

				Color c = Color.black;
				if (levelById.containsKey(id)) {
					int level = levelById.get(id);
					c = colors[level % colors.length];
				}

				colorCodedimage.setRGB(x, y, c.getRGB());
			}
		}

		return colorCodedimage;
	}

	public static void findBlobLevels(Map<Integer, Integer> levels, Blob root, int currentLevel) {
		levels.put(root.id, currentLevel);

		for (Blob b : root.children) {
			findBlobLevels(levels, b, currentLevel + 1);
		}
	}

	public static void toBufferedImage(BufferedImage image, BooleanRaster raster, int xOffset, int yOffset, Color color)
	{
		final Dimension size = raster.getSize();
		final int rgb = color.getRGB();

		for (int x = 0; x < size.width; x++)
		{
			for (int y = 0; y < size.height; y++)
			{
				if (raster.get(x, y))
				{
					image.setRGB(xOffset + x, yOffset + y, rgb);
				}
			}
		}
	}

	public static void printBlob(Blob blob, int level)
	{
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < level; i++)
		{
			sb.append("  ");
		}

		sb.append(blob.toString());

		System.out.println(sb.toString());

		for (Blob c : blob.children)
		{
			printBlob(c, level + 1);
		}
	}

	public static void toBufferedImage(BufferedImage image, Blob blob, int xOffset, int yOffset, Color color)
	{
		final Dimension size = blob.getSize();
		final int rgb = color.getRGB();

		final int id = blob.id;
		final int[][] ids = blob.ids;
		final Rectangle bbox = blob.bbox;

		for (int y = 0; y < size.height; y++)
		{
			final int[] row = ids[y + bbox.y];
			for (int x = 0; x < size.width; x++)
			{
				if (id == row[x + bbox.x])
				{
					image.setRGB(xOffset + x, yOffset + y, rgb);
				}
			}
		}
	}

	public static <T extends BoundingBox & BooleanRaster> Image.Int cutByMask(Image.Int image, T mask) {
		Rectangle box = mask.getBoundingBox();
		Image.Int subimage = new Image.Int(new int[box.width * box.height], 0, 0, box.width, box.height,
		        box.width, Image.Int.Type.ARGB);

		int alpha = (255 << 24);

		for (int y = 0; y < box.height; y++) {
			for (int x = 0; x < box.width; x++) {
				final int globalX = box.x + x;
				final int globalY = box.y + y;
				if (mask.get(x, y)) {
					subimage.pixels[y * box.width + x] = image.pixels[globalY * image.w + globalX] | alpha;
				}
			}
		}

		return subimage;
	}

	public static Image.Int cutByContour(Image.Int image, Blob blob) {
		Rectangle box = blob.getBoundingBox();
		Image.Int subimage = new Image.Int(new int[box.width * box.height], 0, 0, box.width, box.height,
		        box.width, Image.Int.Type.ARGB);

		int alpha = (255 << 24);

		for (int y = box.y; y < box.y + box.height; y++) {
			for (int x = box.x; x < box.x + box.width; x++) {
				if (isAssigned(blob, x, y)) {
					subimage.pixels[(y- box.y) * box.width + (x - box.x)] = image.pixels[y * image.w + x] | alpha;
				}
			}
		}
		return subimage;
	}
	
	private static boolean isAssigned(Blob blob, int x, int y) {
		
		if(blob.bbox.contains(x, y) == false) {
			return false;
		}
		
		if(blob.get(x - blob.bbox.x, y - blob.bbox.y)) {
			return true;
		}
		
		for(Blob child : blob.children) {
			if(isAssigned(child, x, y)) {
				return true;
			}
		}
		
		return false;
	}
	
    public static int countBlobs(Blob blob) {
        int count = 1;

        for (Blob c : blob.children) {
            count += countBlobs(c);
        }

        return count;
    }

}
