package org.testobject.commons.util.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author nijkamp
 *
 */
public class EqualityMap<K, V> implements Map<K, V>
{
    private class Entry implements java.util.Map.Entry<K, V>
    {
        private final K k;
        private V v;
        
        public Entry(K k, V v)
        {
            this.k = k;
            this.v = v;
        }

        @Override
        public K getKey()
        {
            return k;
        }

        @Override
        public V getValue()
        {
            return v;
        }

        @Override
        public V setValue(V value)
        {
            V old = v;
            v = value;
            return old;
        }        
    }
    
    private final List<java.util.Map.Entry<K, V>> backing = new LinkedList<java.util.Map.Entry<K, V>>();

    @Override
    public int size()
    {
        return backing.size();
    }

    @Override
    public boolean isEmpty()
    {
        return backing.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        for(java.util.Map.Entry<K, V> pair : backing)
        {
            if(pair.getKey().equals(key))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value)
    {
        for(java.util.Map.Entry<K, V> pair : backing)
        {
            if(pair.getValue().equals(value))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key)
    {
        for(java.util.Map.Entry<K, V> pair : backing)
        {
            if(pair.getKey().equals(key))
            {
                return pair.getValue();
            }
        }
        return null;
    }

    @Override
    public V put(K key, V value)
    {
        backing.add(new Entry(key, value));
        return value;
    }

    @Override
    public V remove(Object key)
    {
        ListIterator<java.util.Map.Entry<K, V>> iter = backing.listIterator();
        while(iter.hasNext())
        {
            Map.Entry<? extends K, ? extends V> pair = iter.next();
            if(pair.getKey().equals(key))
            {
                iter.remove();
                return pair.getValue();
            }
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    {
        for(Map.Entry<? extends K, ? extends V> entry : m.entrySet())        
        {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear()
    {
        backing.clear();        
    }

    @Override
    public Set<K> keySet()
    {
        Set<K> keys = new HashSet<K>();
        for(java.util.Map.Entry<K, V> entry : backing)
        {
            keys.add(entry.getKey());
        }
        return Collections.unmodifiableSet(keys);
    }

    @Override
    public Collection<V> values()
    {
        List<V> values = new ArrayList<V>(backing.size());
        for(java.util.Map.Entry<K, V> entry : backing)
        {
            values.add(entry.getValue());
        }
        return Collections.unmodifiableList(values);
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet()
    {
        Set<java.util.Map.Entry<K, V>> values = new HashSet<java.util.Map.Entry<K, V>>(backing.size());
        for(java.util.Map.Entry<K, V> entry : backing)
        {
            values.add(entry);
        }
        return Collections.unmodifiableSet(values);
    }
}
