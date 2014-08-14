package org.testobject.kernel.imaging.segmentation;

import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.math.algebra.Rectangle.Int;
import org.testobject.commons.util.tree.interval.FakeIntervalTree;
import org.testobject.commons.util.tree.interval.IIntervalTree;
import org.testobject.commons.util.tree.interval.Interval;
import org.testobject.kernel.imaging.segmentation.Group.Adapter;

/**
 * Detects Blob groups by trying to merge the ones that would overlap if extended by the given value.
 * 
 * @author enijkamp
 *
 */
public class GroupBuilder<T> {
	
	private static final int MIN_ARRAY_SIZE = 64;

	//
	// Both FakeIntervalTree (a list) and true IntervalTree do very fast. Fake one seem to be very slightly faster when 
	// canonical input image is used (see TestGroupBuilder.main), but the diff is well within statistical deviation.
	// This can be explained by the fact that the largest number of intervals that we search in that case is just 146, therefore
	// exhaustive list search is as fast as tree search.
	// We know that "real" IntervalTree provides better asymptotic behavior, when number of elements is very large.
	// Therefore preferring "true" IntervalTree.
	//

	private final IIntervalTree<Integer> intervalTree = new FakeIntervalTree<Integer>();
	// private final IIntervalTree<Integer> intervalTree = new IntervalTreeAdapter<Integer>();
	
	private static class Op implements Comparable<Op> {
		final int x;
		final int blobIndex;
		final boolean isInsertion; // if not, its interval deletion

		public Op(int x, int blobIndex, boolean isInsertion) {
			this.x = x;
			this.blobIndex = blobIndex;
			this.isInsertion = isInsertion;
		}

		@Override
		public int compareTo(Op o) {
			if (x < o.x) {
				return -1;
			} else if (x > o.x)	{
				return 1;
			} else {
				// inserts go first!
				if (isInsertion && !o.isInsertion) {
					return -1;
				} else if (!isInsertion && o.isInsertion) {
					return 1;
				} else {
					return 0;
				}
			}
		}
	}
	
	private final Adapter<T> adapter;

	// cache array allocations for re-use
	private Object[] blobs = null;
	private Op[] ops = null;
	private int[] mergemap = null;
	private Group<T>[] grps = null;

	// statistics
	private int maxTreeSize = 0;
	
	public GroupBuilder() {
		this(new Group.Adapter<T>() {
			@Override
			public Int getBoundingBox(T t) {
				HasBoundingBox hasBoundingBox = (HasBoundingBox) t;
				return hasBoundingBox.getBoundingBox();
			}
		});
	}
	
	public GroupBuilder(Group.Adapter<T> adapter) {
		this.adapter = adapter;
	}

	public int getMaxTreeSize() {
		return maxTreeSize;
	}

	public List<Group<T>> buildGroups(List<T> list, int fatX, int fatY) {
		return buildGroups(list, new Insets(fatY, fatX, 0, 0));
	}

	/**
	* Finds groupings of rectangles, using the following logic:
	* 
	* Extends all rectangles using the supplied Insets object (note that content of the input does not change, all "extension" is purely virtual).
	* Then it locates all rectangles that intersect, and groups them in Group<T> objects. 
	* 
	* The list of detected groups is returned. If no intersections, then each input blob will be wrapped in a trivial singleton Group.
	* 
	* By using non-zero horizontal insets, one can detect horizontal strings. Similarly, by using vertical-only insets, 
	* one can group in vertical direction. Of course, its possible to "fatten" rectangles in both directions, if needed.
	* 
	* Uses algorithm from there: http://wwwisg.cs.uni-magdeburg.de/ag/lehre/SS2009/GDS/slides/S6.pdf
	* 
	* @param list
	* @param insets
	* @return
	*/
	@SuppressWarnings("unchecked")
	public List<Group<T>> buildGroups(List<T> list, Insets insets) {
		final ArrayList<Group<T>> out = new ArrayList<Group<T>>();

		final int length = list.size();

		if (length == 0)
		{
			return out;
		}
		else if (length == 1)
		{
			Group<T> g = new Group<T>(adapter);
			g.add(list.get(0));
			out.add(g);

			return out;
		}

		if (this.blobs == null || this.blobs.length < list.size())
		{
			int newsize = list.size() * 2;
			if (newsize < MIN_ARRAY_SIZE)
			{
				newsize = MIN_ARRAY_SIZE;
			}

			this.blobs = new Object[newsize];
			this.ops = new Op[blobs.length * 2];
			this.mergemap = new int[blobs.length];
			this.grps = new Group[blobs.length];
		}

		// sanity: interval tree must be empty at start
		if (!intervalTree.isEmpty())
		{
			throw new IllegalStateException("Delete operation not implemented correctly??");
		}

		final Object[] blobs = this.blobs;
		final Op[] ops = this.ops;
		final int[] mergemap = this.mergemap;
		final Group<T>[] grps = this.grps;
		final IIntervalTree<Integer> intervalTree = this.intervalTree;

		// lay out input arrays
		int idx = 0;
		for (T b : list)
		{
			blobs[idx] = b;
			grps[idx] = null;
			mergemap[idx] = 0;

			Rectangle.Int r = adapter.getBoundingBox(b);
			// insertion (right) and deletion (left)
			ops[idx * 2] = new Op(r.x - insets.left, idx + 1, true);
			ops[idx * 2 + 1] = new Op(r.x + r.w - 1 + insets.right, idx + 1, false);

			idx++;
		}

		// sort operations (by x value)
		Arrays.sort(ops, 0, 2 * length);

		int maxTreeSize = 0;

		// start scanning ops (this moves "scanline") from left to right
		for (int i = 0; i < 2 * length; i++)
		{
			Op op = ops[i];
			T b = (T) blobs[op.blobIndex - 1];
			Rectangle.Int r = adapter.getBoundingBox(b);

			int start = r.y - insets.top;
			int end = r.y + r.h - 1 + insets.bottom;

			Interval probe = new Interval(start, end);

			if (op.isInsertion)
			{
				// scan tree for intersecting intervals...
				for (Map.Entry<Interval, Integer> ent : intervalTree.intersectingEntries(probe))
				{
					int bi = ent.getValue(); // index of intersecting blob
					int mi = op.blobIndex; // index of probe blob

					if (bi != mi)
					{
						// normalize bi
						while (mergemap[bi - 1] != 0)
						{
							bi = mergemap[bi - 1];
						}

						// normalize mi
						while (mergemap[mi - 1] != 0)
						{
							mi = mergemap[mi - 1];
						}

						if (bi != mi)
						{
							// need to merge for real
							if (bi > mi)
							{
								int t = bi;
								bi = mi;
								mi = t;
							}

							mergemap[mi - 1] = bi;
						}
					}
				}

				intervalTree.put(probe, op.blobIndex);

				maxTreeSize = Math.max(maxTreeSize, intervalTree.size());
			}
			else
			{
				// removal
				intervalTree.remove(probe);
			}
		}

		this.maxTreeSize = Math.max(this.maxTreeSize, maxTreeSize);

		// scan all blobs and find out which group they belong
		for (int i = 0; i < length; i++)
		{
			T b = (T) blobs[i];

			int groupIndex = i + 1;
			while (mergemap[groupIndex - 1] != 0)
			{
				groupIndex = mergemap[groupIndex - 1];
			}

			Group<T> g = grps[groupIndex - 1];
			if (g == null)
			{
				g = new Group<T>(adapter);
				grps[groupIndex - 1] = g;
				out.add(g);
			}

			g.add(b);
		}

		return out;
	}
}
