package org.testobject.kernel.imgproc.diff;

import java.awt.Rectangle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.testobject.kernel.imgproc.blob.Blob;

/**
 * 
 * @author nijkamp
 *
 */
public class BlobRebuilder
{
	/*
	 * FIXME
	 * 
	 * put this in blob builder
	 * 
	 * - re-use ids (in stack)
	 * - write directly to global id array
	 */

	public static Blob findParent(Blob blob, Rectangle box)
	{
		if (blob.bbox.contains(box))
		{
			for (Blob child : blob.children)
			{
				if (child.bbox.contains(box))
				{
					Blob leaf = findParent(child, box);
					return (leaf != null ? leaf : child);
				}
			}
			return blob;
		}
		return null;
	}

	public static void updateBlob(Blob blob, Blob oldParent, Blob newParent)
	{
		// FIXME prevent blob id collision (en)

		// merge rasters
		for (int y = 0; y < oldParent.bbox.height; y++)
		{
			final int globalY = oldParent.bbox.y + y;
			final int globalX = oldParent.bbox.x;
			final int[] dstRow = blob.ids[globalY];
			final int[] srcRow = newParent.ids[y];
			System.arraycopy(srcRow, 0, dstRow, globalX, oldParent.bbox.width);
		}

		// re-assign raster
		setRaster(newParent, oldParent.ids);

		// translate coordinate system
		translate(oldParent.bbox.x, oldParent.bbox.y, newParent);

		// rebuild hierarchy
		buildTree(blob, oldParent, newParent);
	}

	private static void setRaster(Blob blob, int[][] ids)
	{
		try
		{
			setFinalField(blob, Blob.class.getField("ids"), ids);
			for (Blob child : blob.children)
			{
				setRaster(child, ids);
			}
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private static void setFinalField(Object object, Field field, Object newValue) throws Exception
	{
		field.setAccessible(true);

		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

		field.set(object, newValue);
	}

	private static void translate(int dx, int dy, Blob blob)
	{
		blob.bbox.translate(dx, dy);
		for (Blob child : blob.children)
		{
			translate(dx, dy, child);
		}
	}

	private static void buildTree(Blob blob, Blob oldParent, Blob newParent)
	{
		if (blob.children.contains(oldParent))
		{
			blob.children.remove(oldParent);
			blob.children.add(newParent);
		}
		else
		{
			for (Blob child : blob.children)
			{
				buildTree(child, oldParent, newParent);
			}
		}
	}
}
