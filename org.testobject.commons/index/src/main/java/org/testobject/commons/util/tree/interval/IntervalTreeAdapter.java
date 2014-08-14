package org.testobject.commons.util.tree.interval;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.testobject.commons.util.tree.interval.IntervalTree.INode;

/**
 * 
 * @author mike
 *
 * @param <V>
 */
public class IntervalTreeAdapter<V> implements IIntervalTree<V>
{

    static class RefCounter<V>
    {
        public final V value;
        public int refcount = 0;

        public RefCounter(V value)
        {
            this.value = value;
        }
    }

    private IntervalTree<RefCounter<V>> engine = new IntervalTree<RefCounter<V>>();

    private static class E<V> implements Map.Entry<Interval, V>
    {
        private final Interval key;
        private final V value;

        public E(Interval key, V value)
        {
            this.key = key;
            this.value = value;
        }

        @Override
        public Interval getKey()
        {
            return key;
        }

        @Override
        public V getValue()
        {
            return value;
        }

        @Override
        public V setValue(V value)
        {
            throw new UnsupportedOperationException();
        }

    }

    @Override
    public int size()
    {
        return engine.getSize();
    }

    @Override
    public boolean isEmpty()
    {
        return size() == 0;
    }

    @Override
    public void put(Interval key, V payload)
    {
        RefCounter<V> val = engine.lookup(key);
        if (val == null)
        {
            val = new RefCounter<V>(payload);
            engine.put(key, val);
        }
        val.refcount++;
    }

    @Override
    public V remove(Interval key)
    {
        RefCounter<V> val = engine.lookup(key);
        if (val == null)
        {
            return null;
        }

        if (--val.refcount == 0)
        {
            engine.remove(key);
        }

        return val.value;
    }

    @Override
    public Iterable<? extends Entry<Interval, V>> entries()
    {
        throw new UnsupportedOperationException();
    }

    static class It<V> implements Iterable<Entry<Interval, V>>, Iterator<Entry<Interval, V>>
    {
        private final Iterator<INode<RefCounter<V>>> engine;

        public It(Iterator<INode<RefCounter<V>>> engine)
        {
            this.engine = engine;
        }

        @Override
        public boolean hasNext()
        {
            return engine.hasNext();
        }

        @Override
        public Entry<Interval, V> next()
        {
            INode<RefCounter<V>> next = engine.next();

            if (next == null)
            {
                return null;
            }

            return new E<V>(next.key, next.value.value);
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<Entry<Interval, V>> iterator()
        {
            return this;
        }

    }

    @Override
    public Iterable<? extends Entry<Interval, V>> intersectingEntries(Interval probe)
    {
        return new It<V>(engine.intersecting(probe).iterator());
    }

    @Override
    public void visitIntersecting(Interval probe, IIntervalTree.IVisitor<Entry<Interval, V>> visitor)
    {
        for (Entry<Interval, V> hit : intersectingEntries(probe))
        {
            visitor.visit(hit);
        }
    }
}
