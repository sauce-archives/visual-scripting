package org.testobject.commons.util.tree.interval;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;


/**
 * Quick-and-dirty implementation of Interval Tree contract, but uses (slow) data structures,
 * so that lookups, inserts, and deletes are O(n) instead of optimal O(log(N)).
 * 
 * @author mike
 *
 * @param <T>
 */
public class FakeIntervalTree<T> implements IIntervalTree<T>
{
    private final LinkedList<Entry<T>> content = new LinkedList<Entry<T>>();
    
    private static class Entry<T> implements Map.Entry<Interval, T>
    {
        public final Interval key;
        public final T value;
        
        Entry(Interval key, T value)
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
        public T getValue()
        {
            return value;
        }

        @Override
        public T setValue(T value)
        {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public int size()
    {
        return content.size();
    }

    @Override
    public boolean isEmpty()
    {
        return size() == 0;
    }

    @Override
    public void put(Interval key, T payload)
    {
        if(payload == null)
        {
            throw new NullPointerException("payload");
        }
        
        content.addLast(new Entry<T>(key, payload));
    }

    @Override
    public T remove(Interval key)
    {
        for(Entry<T> e : content)
        {
            if(Interval.equal(key, e.key))
            {
                content.remove(e);
                return e.value;
            }
        }
        
        return null;
    }

    @Override
    public Iterable<? extends Map.Entry<Interval, T>> entries()
    {
        return content;
    }
    
    private static class It<T> implements Iterable<Entry<T>>, Iterator<Entry<T>>
    {
        private final Interval probe;
        private final Iterator<Entry<T>> dataIterator;
        
        It(Interval probe, Collection<Entry<T>> data)
        {
            this.probe = probe;
            this.dataIterator = data.iterator();
        }

        @Override
        public Iterator<Entry<T>> iterator()
        {
            return this;
        }
        
        private Entry<T> next = null;

        @Override
        public boolean hasNext()
        {
            if(next != null)
            {
                return true;
            }
            
            while(dataIterator.hasNext())
            {
                Entry<T> n = dataIterator.next();
                if(probe.overlaps(n.key))
                {
                    next = n;
                    return true;
                }
            }

            return false;
        }

        @Override
        public Entry<T> next()
        {
            Entry<T> out = next;
            next = null;
            return out;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
        
    }

    @Override
    public Iterable<? extends Map.Entry<Interval, T>> intersectingEntries(Interval probe)
    {
        return new It<T>(probe, content);
    }

    @Override
    public void visitIntersecting(Interval probe, IIntervalTree.IVisitor<Map.Entry<Interval,T>> visitor)
    {
        for(Map.Entry<Interval,T> e : intersectingEntries(probe))
        {
            visitor.visit(e);
        }
    }

}
