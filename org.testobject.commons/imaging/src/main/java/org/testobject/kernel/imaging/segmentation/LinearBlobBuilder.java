package org.testobject.kernel.imaging.segmentation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.math.algebra.Rectangle;

/**
 *  A builder class that constructs the blob hierarchy for a given boolean raster
 *  using a linear line-by-line scan method. In essence, each line is processed
 *  from left to right. The current position is marked as dot, we keep track of the local 
 *  neighbourhood (at positions a,b,c,d) and ignore the rest (denoted by x):
 * 
 *   x x x x x b c d x x x x x x x x x x x x
 *   x x x x x a .
 * 
 *  Algorithm:
 * 
 *    // compute color at current position (.)
 *    if FG:
 *      if any of a, b, c, or d are FG (positive), then assign same id there
 *      else generate new FG blob id 
 *    if BG:
 *      if any of a or c are BG, then propagate their id
 *      else generate new BG blob id (negative)
 *      
 *    then move one spot to the right
 *    
 *    // merge connected blobs
 *    if FG:
 *      possible merge between b and d (need to check only if c is BG (negative))
 *      possible merge between a and d (need to check only if b and c are both BG)    
 *    if BG:
 *      possible merge between a and c (need to check only if b is FG (positive))
 *      
 *
 *  Interestingly, it is guaranteed that we have to merge at most two blobs. We denote 
 *  the ids of background blobs as zero and show that the former statement can be easily 
 *  proved by enumerating the following cases:
 *  
 *   (1) b != 0, c == 0, d != 0, a != 0  =>  b == a, c == 0, d != 0  =>  2 blobs 
 *   (2) b == 0, c != 0, d != 0, a != 0  =>  a == c, c == d, d != 0  =>  2 blobs 
 *   (3) b == 0, c == 0, d != 0, a != 0  =>  b == 0, c == 0          =>  2 blobs
 *   
 *  Thus the construction of the linear scan guarantees that the assumption of merging at most two blobs
 *  holds and we can exploit it by using a single one-dimensional id merge "relation".
 * 
 *  With respect to performance a linear-scan has beneficial side-effects:
 *   - memory locality: due to the nature of the scan memory locality is guranteed (cache lines)
 *   - avoid multi-dim access: we avoid (expensive) multi-dim array access by "fixing" the current line
 *   - re-using arrays: we can re-use most of the arrays and avoid allocation
 *   - reduced memory access: for each pixel we only have to check 2 neighbouring pixels & ids
 *  
 *  
 * 
 * @author enijkamp
 *
 */
public class LinearBlobBuilder
{
	public static final Log log = LogFactory.getLog(LinearBlobBuilder.class);
	
	final static boolean DEBUG = false;

	private final static class B
	{
		int id;
		int parid;
		int area;
		int minx;
		int maxx;
		int miny;
		int maxy;

		final List<B> children = new ArrayList<B>(16);

		void add(int x, int y)
		{
			area++;
			minx = Math.min(minx, x);
			miny = Math.min(miny, y);
			maxx = Math.max(maxx, x);
			maxy = Math.max(maxy, y);
		}

		void merge(B other)
		{
			this.area += other.area;
			this.minx = Math.min(this.minx, other.minx);
			this.miny = Math.min(this.miny, other.miny);
			this.maxx = Math.max(this.maxx, other.maxx);
			this.maxy = Math.max(this.maxy, other.maxy);
		}

		B(int id, int parid)
		{
			this.id = id;
			this.parid = parid;
			area = 0;
			minx = Integer.MAX_VALUE;
			miny = Integer.MAX_VALUE;
			maxx = -1;
			maxy = -1;
		}
	}

	private int[] idmap = null;
	private int[][] ids = null;
	private B[] bmap = null;

	private final void merge(int low, int high)
	{

		int l = low;
		int h = high;

		while (idmap[l] > 0)
		{
			l = idmap[l];
		}

		while (idmap[h] > 0)
		{
			h = idmap[h];
		}

		if (l == h)
		{
			return;
		}
		else if (l > h)
		{
			int t = low;
			low = high;
			high = t;
			t = l;
			l = h;
			h = t;
		}

		idmap[h] = l;
	}

	private static final void assertThat(boolean what)
	{
		if (!what)
		{
			throw new AssertionError();
		}
	}

	private static void validate(int x, int y, int[][] ids, int a, int b, int c, int d)
	{
		int width = ids[0].length;

		if (y == 0)
		{
			assertThat(b == -1);
			assertThat(c == -1);
			assertThat(d == -1);

			if (x == 0)
			{
				assertThat(a == -1);
			}
			else
			{
				assertThat(a == ids[y][x - 1]);
			}
		}
		else
		{
			if (x == 0)
			{
				assertThat(a == -1);
				assertThat(b == -1);
			}
			else
			{
				assertThat(a == ids[y][x - 1]);
				assertThat(b == ids[y - 1][x - 1]);
			}

			assertThat(c == ids[y - 1][x]);
			if (x == width - 1)
			{
				assertThat(d == -1);
			}
			else
			{
				assertThat(d == ids[y - 1][x + 1]);
			}
		}
	}

	private final void assertMerged(int a, int b)
	{
		if (a == b)
			return;

		while (idmap[a] > 0)
		{
			a = idmap[a];
		}

		while (idmap[b] > 0)
		{
			b = idmap[b];
		}

		if (a != b)
		{
			throw new IllegalStateException();
		}
	}

	//
	//  dot shows the "current position" with coordinates (x, y)
	//  x, a, b, c, d show processed (non-zero) ids
	//  we keep track of neighboring ids, at positions a, b, c, and d
	//  (do not care about the rest, denoted by x)
	//
	//  x x x x x b c d x x x x x x x x x x x x
	//  x x x x x a .
	//
	//  algo:
	//  compute color at current point (.)
	//  if FG:
	//    if any of a, b, c, or d are FG (positive), then assign same id there
	//    else generate new FG blob id 
	//  if BG:
	//    if any of a or c are BG, then propagate their id
	//    else generate new BG blob id (negative)
	//
	//  then move one spot to the right.
	//
	//  merging:
	//  if FG:
	//  possible merge between b and d (need to check only if c is BG (negative))
	//  possible merge between a and d (need to check only if b and c are both BG)
	//
	//  if BG:
	//  possible merge between a and c (need to check only if b is FG (positive))
	public Blob build(ArrayRaster r)
	{
		int idSource = 1;
		int merges = 0;

		final int width = r.getSize().w;
		final int height = r.getSize().h;

		if (width <= 0 || height <= 0)
		{
			throw new IllegalArgumentException("can not deal with zero-sized rasters");
		}

		if (ids == null || ids.length != height || ids[0].length != width)
		{
			/* System.out.println("(RE)ALLOCATING WORK BUFFERS"); */
			this.ids = new int[height][width];

			// array length computation:
			// number of blobs can not be more than half of the number of pixels.
			// but note the initial "outer" BG blob (the root), that is artificial. 
			// and, finally, remember that slot 0 is not used at all (wasted)
			// So the final formula that gurantees no ArrayBoundException is:
			// (h * w + 2). Just in case i forgot something, will add arbitrary number 32
			this.bmap = new B[height * width + 32];
			this.idmap = new int[this.bmap.length];
		}

		final boolean[][] raster = r.fg;
		final int[][] ids = this.ids;
		final B[] bmap = this.bmap;
		final int[] idmap = this.idmap;
		bmap[1] = new B(-1, 0);
		bmap[1].minx = 0;
		bmap[1].miny = 0;
		bmap[1].maxx = width - 1;
		bmap[1].maxy = height - 1;

		for (int y = 0; y < height; y++)
		{
			final boolean[] row = raster[y];
			final int[] currentIdRow = ids[y];
			final int[] previousIdRow = y > 0 ? ids[y - 1] : null;

			int a = -1;
			int b = -1;
			int c;
			int d;

			if (y == 0)
			{
				c = -1;
				d = -1;
			}
			else
			{
				c = previousIdRow[0];
				if (width > 1)
					d = previousIdRow[1];
				else
					d = -1;
			}

			for (int x = 0; x < width; x++)
			{

				if (DEBUG)
				{
					validate(x, y, ids, a, b, c, d);
				}

				int newa;

				if (row[x])
				{
					if (a > 0)
					{
						newa = a;

						// check for FG blob merge
						if (b < 0 && c < 0 && d > 0 && a != d)
						{
							merge(newa, d);
							merges++;
						}
					}
					else if (b > 0)
					{
						newa = b;
					}
					else if (c > 0)
					{
						newa = c;
					}
					else if (d > 0)
					{
						newa = d;
					}
					else
					{
						// new FG blob
						newa = ++idSource;
						B blob = new B(newa, c);
						bmap[newa] = blob;
					}

					// check for FG blob merge
					if (c < 0 && b > 0 && d > 0 && b != d)
					{
						merge(newa, d);
						merges++;
					}

					if (DEBUG)
					{
						if (a > 0)
						{
							assertMerged(newa, a);
						}
						if (b > 0)
						{
							assertMerged(newa, b);
						}
						if (c > 0)
						{
							assertMerged(newa, c);
						}
						if (d > 0)
						{
							assertMerged(newa, d);
						}
					}

					B blob = bmap[newa];
					blob.add(x, y);
				}
				else
				{
					if (a < 0)
					{
						newa = a;

						// check for BG blob merge
						if (b > 0 && c < 0 && a != c)
						{
							merge(-newa, -c);
							merges++;
						}

					}
					else if (c < 0)
					{
						newa = c;
					}
					else
					{
						// new BG blob
						newa = -++idSource;

						B blob = new B(newa, c);
						bmap[-newa] = blob;
					}

					if (DEBUG)
					{
						if (a < 0)
						{
							assertMerged(-newa, -a);
						}
						if (c < 0)
						{
							assertMerged(-newa, -c);
						}
					}

					B blob = bmap[-newa];
					blob.add(x, y);
				}

				currentIdRow[x] = newa;

				a = newa;
				b = c;
				c = d;
				if (x + 2 < width && y > 0)
				{
					d = previousIdRow[x + 2];
				}
				else
				{
					d = -1;
				}
			}
		}

		// beyond last scanline - possibly merge BG blobs
		for (int x = 0; x < width; x++)
		{
			int id = ids[height - 1][x];

			if (id < 0 && id != -1)
			{
				merge(-id, 1);
				merges++;
			}

		}

		// normalize idmap array
		for (int y = 0; y < height; y++)
		{
			int[] row = ids[y];

			for (int x = 0; x < width; x++)
			{
				final int id = row[x];

				if (id > 0)
				{
					int i = idmap[id];
					if (i > 0)
					{
						while (idmap[i] > 0)
						{
							i = idmap[i];
						}
						row[x] = i;
					}
				}
				else
				{
					int i = idmap[-id];
					if (i > 0)
					{
						while (idmap[i] > 0)
						{
							i = idmap[i];
						}
						row[x] = -i;
					}
				}

			}
		}

		// build hierarchy
		B root = null;
		for (int i = 1; i < idSource + 1; i++)
		{
			if (idmap[i] == 0)
			{
				B b = bmap[i];
				if (DEBUG)
				{
					if (b == null)
						throw new AssertionError();
				}

				if (b.parid != 0)
				{
					B parent = bmap[Math.abs(b.parid)];
					parent.children.add(b);
				}
				else if (root == null)
				{
					root = b;
				}
				else
				{
					throw new AssertionError();
				}
			}
			else
			{
				int origin = i;
				while (idmap[origin] > 0)
				{
					origin = idmap[origin];
				}

				B b = bmap[origin]; // "normal"

				int j = i;
				while (j != origin)
				{
					b.merge(bmap[j]);
					bmap[j] = b;
					int nextj = idmap[j];
					idmap[j] = origin;
					j = nextj;
				}
			}
		}

		/*
		for(int i = 1; i < idSource; i++)
		{
		    B b = bmap[i];
		    
		    boolean found = false;
		    for(int j = 0; j < blobs.length; j++)
		    {
		        if(b == blobs[j])
		        {
		            found = true;
		            break;
		        }
		    }
		    
		    if(!found)
		    {
		        throw new AssertionError();
		    }
		}
		*/

		if (root == null)
		{
			throw new AssertionError();
		}

		/*
		Arrays.sort(blobs, new Comparator<B>() {

		    @Override
		    public int compare(B a, B b)
		    {
		        if(a.minx < b.minx)
		        {
		            return -1;
		        }
		        else if(a.minx == b.minx)
		        {
		            return 0;
		        }
		        else
		        {
		            return 1;
		        }
		    }
		});
		*/

		// compute the number of unique blobs

		log.debug("idSource=" + idSource + ", merges=" + merges);

		// build "standard Blob tree so that I can display it
		return buildTree(root, ids);
		// Utils.displayHierarchy(sroot);
		// Utils.displayIds(ids);
	}

	private static final Blob buildTree(B b, int[][] ids)
	{
		List<Blob> ch = new LinkedList<Blob>();

		for (B c : b.children)
		{
			ch.add(buildTree(c, ids));
		}

		Rectangle.Int bbox = new Rectangle.Int(b.minx, b.miny, b.maxx - b.minx + 1, b.maxy - b.miny + 1);
		// FIXME abs? what about bg blobs? (en)
		// return new Blob(Math.abs(b.id), bbox, b.area, ch, ids);
		return new Blob(b.id, bbox, b.area, ch, ids);
	}
}
