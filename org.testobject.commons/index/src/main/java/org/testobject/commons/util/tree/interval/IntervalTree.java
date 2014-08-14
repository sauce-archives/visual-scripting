package org.testobject.commons.util.tree.interval;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * Interval tree. This implementation does not accept duplicate keys,
 * see {@link #put(Interval, Object)} for more detail.
 * 
 * Implementation is based on RBTree, augmented (via sub-classing and method override)
 * to maintain 'max' value per node.
 * 
 * @author mike
 *
 * @param <V> value type.
 */
public class IntervalTree<V> extends RBTree<Interval,V> {
	
	private static final boolean DEBUG = false;
	
	private static Comparator<Interval> COMPARATOR = new Comparator<Interval>() {

		@Override
		public int compare(Interval o1, Interval o2) {
			if(o1.start < o2.start) {
				return -1;
			} else if(o1.start > o2.start) {
				return 1;
			} else if(o1.end < o2.end) {
				return -1;
			} else if(o1.end > o2.end) {
				return 1;
			} else {
				return 0;
			}
		}
	};
	
	/**
	 * Extends RBTree.Node to hold max value.
	 */
	protected static class INode<V> extends RBTree.Node<Interval, V> {
		
		public int max;

		public INode(Interval key, V value) {
			super(key, value);
			
			max = key.end;
		}
		
		public int computeMax() {
			int max = key.end;
			
			if(left != null) {
				max = Math.max(max, ((INode<V>) left).max);
			}
			
			if(right != null) {
				max = Math.max(max, ((INode<V>) right).max);
			}
			
			return max;
		}

		public boolean recomputeMax() {
			int max = computeMax();
			
			if(this.max == max) {
				return false;
			} else {
				this.max = max;
				return true;
			}
		}
	}

	public IntervalTree() {
		super(COMPARATOR);
	}

	@Override
	protected void rotateLeft(Node<Interval,V> node)	{
		INode<V> x = (INode<V>) node;
		INode<V> y = (INode<V>) node.right;
		
		super.rotateLeft(node);
		
		y.max = x.max;
		x.recomputeMax();

		if(DEBUG) validateIntervals();
	}

	@Override
	protected void rotateRight(Node<Interval,V> node)	{
		INode<V> x = (INode<V>) node;
		INode<V> y = (INode<V>) node.left;

		super.rotateRight(node);

		y.max = x.max;
		x.recomputeMax();
		
		if(DEBUG) validateIntervals();
	}

	/**
	 * Just like underlying RBTree, this method does not store
	 * duplicate keys. If we call it with some key, and this key is
	 * already in the tree, we will replace old value for this key with
	 * the new value, and return the old value. Thus, if this method returns 
	 * non-null value, that means that key was duplicate and value replacement 
	 * occurred.
	 * 
	 *  @param key key
	 *  @param value value (assumed to be not null)
	 *  @return null if key/value was inserted, not null if key already in the
	 *  tree and value has been replace (retuyrns old value).
	 */
	@Override
	public V put(Interval key, V value) {
		LookupResult<Interval,V> res = doLookup(key);
		
		if(res.node != null) {
			V oldValue = res.node.value;
			res.node.value = value;
			
			if(DEBUG) validateIntervals();
			
			return oldValue;
		}
		
		INode<V> node = new INode<V>(key, value);
		if(res.parent != null) {
			if(res.isLeft) {
				res.parent.left = node;
			} else {
				res.parent.right = node;
			}
			
			INode<V> x = (INode<V>) res.parent;
			while(x != null) {
				if(!x.recomputeMax()) break;
				x = (INode<V>) x.parent;
			}
		}
		
		super.internalInsert(res, node);
		
		if(DEBUG) validateIntervals();

		return null;
	}

	/**
	 * Removes value for the given key.
	 * 
	 * @param key key to remove
	 * @return associated value (if exists), null if key is not in the map.
	 */
	public V remove(Interval key) {
		LookupResult<Interval,V> res = doLookup(key);
		
		if(res.node == null) {
			return null;
		}
		
		Node<Interval,V> node = res.node;
		
		V value = node.value;
		
		if(node.left != null && node.right != null) {
			Node<Interval,V> next = node.right.getFirst();
				
			V nodeValue = node.value;
				
			node.key = next.key;
			node.value = next.value;
				
			next.value = nodeValue;

			// since key changed, recompute max
			INode<V> x = (INode<V>) node;
			while(x != null) {
				if(!x.recomputeMax()) break;
					
				x = (INode<V>) x.parent;
			}

			if(DEBUG) validateIntervals();
			
			node = next;
		}

		if(node.parent != null) {
		    if(node.left != null) {
		        ((INode<V>) node).max = ((INode<V>) node.left).max;
		    } else if(node.right != null) {
                ((INode<V>) node).max = ((INode<V>) node.right).max;
		    } else {
                ((INode<V>) node).max = -Integer.MAX_VALUE;
		    }
			
			INode<V> x = (INode<V>) node.parent;
			while(x != null) {
				if(!x.recomputeMax()) break;
				
				x = (INode<V>) x.parent;
			}
		}
		
		super.internalRemove(node);
		
		if(DEBUG) validateIntervals();

		return value;
	}


	/**
	 * For testing
	 */
	void validateIntervals() {
		if(root != null) {
			validateIntervals((INode<V>) root);
		}
	}

	private void validateIntervals(INode<V> node) {
		if(node.max != node.computeMax()) {
			throw new AssertionError("wrong max");
		}
		
		if(node.left != null) {
			validateIntervals((INode<V>) node.left);
		}

		if(node.right != null) {
			validateIntervals((INode<V>) node.right);
		}
	}
	
	private static class Intersection<V> implements Iterator<INode<V>>, Iterable<INode<V>>
	{
	    private final LinkedList<INode<V>> stack = new LinkedList<INode<V>>();
	    private final Interval probe;
        private INode<V> current = null;
	    
	    Intersection(INode<V> root, Interval probe)
	    {
	        if(root != null) 
	        {
	            stack.push(root);
	        }
	        this.probe = probe;
	        this.current = nextNode();
	    }
	    
	    private INode<V> nextNode()
	    {
	        while(!stack.isEmpty())
	        {
	            INode<V> node = stack.pop();

	            if(probe.start > node.max)
	            {
	                // ignore subtree
	                continue;
	            }
	            
	            if(node.left != null && probe.end >= node.key.start)
	            {
	                stack.push((INode<V>) node.left);
	            }

	            if(node.right != null && probe.end >= node.key.start)
                {
                    stack.push((INode<V>) node.right);
                }

	            if(probe.overlaps(node.key))
	            {
	                return node;
	            }
	        }
	        
	        return null;
	    }
	    
        @Override
        public boolean hasNext()
        {
            return current != null;
        }

        @Override
        public INode<V> next()
        {
            INode<V> node = current;
            current = nextNode();
            
            return node;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<INode<V>> iterator()
        {
            return this;
        }
	}
	
    private static <V> void recursivelyVisitIntersecting(INode<V> node, Interval probe, IVisitor<Interval, V> visitor)
    {
        
        if(node == null)
        {
            return;
        }
        
        // start of the probe is to the right of all intervals at and below this point
        if(probe.start > node.max)
        {
            return;
        }
        
        if(node.left != null)
        {
            recursivelyVisitIntersecting((INode<V>) node.left, probe, visitor);
        }
        
        // check this
        if(probe.overlaps(node.key))
        {
            visitor.visit(node.key, node.value);
        }
        
        // end of the probe is to the left of beginning of all intervals below
        if(probe.end < node.key.start)
        {
            return;
        }
        
        if(node.right != null)
        {
            recursivelyVisitIntersecting((INode<V>) node.right, probe, visitor);
        }
    }
    
    public void visitIntersecting(Interval probe, IVisitor<Interval, V> visitor)
    {
        recursivelyVisitIntersecting((INode<V>) root, probe, visitor);
    }
    
    public Iterable<INode<V>> intersecting(Interval probe)
    {
        return new Intersection<V>((INode<V>) root, probe);
    }
}