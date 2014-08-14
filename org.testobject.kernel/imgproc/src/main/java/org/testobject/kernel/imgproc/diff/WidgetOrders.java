package org.testobject.kernel.imgproc.diff;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.testobject.kernel.imgproc.blob.BoundingBox;
import org.testobject.commons.util.tree.interval.FakeIntervalTree;
import org.testobject.commons.util.tree.interval.IIntervalTree;
import org.testobject.commons.util.tree.interval.Interval;

/**
 * 
 * @author nijkamp
 *
 */
public interface WidgetOrders
{
	public interface Order<T extends BoundingBox>
	{
		List<T> order(List<T> boxes);
	}

	/**
	 * algorithm:
	 * 
	 *  1. determine top-left rect (x1, y1, w1, h1) -> id 1
	 *  2. interval intersection on y for (y1, y1+h1)
	 *  3. select next x with minimal y -> id 2
	 *  4. interval intersection on y for union((y1, y1+h1), (y2, y2+h2))
	 *  5. goto 2
	 */
	class XY<T extends BoundingBox> implements Order<T>
	{
		@Override
		public List<T> order(List<T> boxes)
		{
			List<T> remaining = new ArrayList<T>(boxes);
			List<T> order = new ArrayList<T>();

			while (!remaining.isEmpty())
			{
				// 1
				T query = remaining.get(0);
				for (T probe : remaining)
				{
					if (probe.getBoundingBox().x <= query.getBoundingBox().x && probe.getBoundingBox().y <= query.getBoundingBox().y)
					{
						query = probe;
					}
				}
				order.add(query);
				remaining.remove(query);

				// 2
				order(query.getBoundingBox(), remaining, order);
			}

			return order;
		}

		private void order(Rectangle query, List<T> remaining, List<T> order)
		{
			// 2
			IIntervalTree<T> intervalTree = new FakeIntervalTree<T>();
			for (T probe : remaining)
			{
				final Rectangle probeBox = probe.getBoundingBox();
				intervalTree.put(new Interval(probeBox.y, probeBox.y + probeBox.height), probe);
			}

			// 3
			List<Map.Entry<Interval, T>> hits = new ArrayList<Map.Entry<Interval, T>>();
			for (Map.Entry<Interval, T> hit : intervalTree.intersectingEntries(new Interval(query.y, query.y + query.height)))
			{
				hits.add(hit);
			}

			if (hits.isEmpty())
			{
				return;
			}

			T next = hits.get(0).getValue();
			for (Map.Entry<Interval, T> hit : hits)
			{
				T probe = hit.getValue();
				if (probe.getBoundingBox().x < next.getBoundingBox().x)
				{
					next = probe;
				}
				else if (probe.getBoundingBox().x == next.getBoundingBox().x)
				{
					next = probe.getBoundingBox().y < next.getBoundingBox().y ? probe : next;
				}
			}
			remaining.remove(next);
			order.add(next);

			// 4
			order(query.union(next.getBoundingBox()), remaining, order);
		}
	}
}