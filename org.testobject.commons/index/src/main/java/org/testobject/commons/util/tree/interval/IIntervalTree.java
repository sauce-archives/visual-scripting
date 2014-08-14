package org.testobject.commons.util.tree.interval;

import java.util.Map;

/**
 * Interface for the Interval tree implementations.
 * 
 * @author mike
 *
 * @param <T> payload data type.
 */
public interface IIntervalTree<T>
{
    /**
     * Returns size of the tree. 
     * @return
     */
    int size();

    /**
     * Returns true is tree is empty.
     * @return
     */
    boolean isEmpty();
    
    /**
     * Puts data into the tree. ATTENTION: implementation may allow putting duplicates.
     * @param key
     * @param payload
     */
    void put(Interval key, T payload);
    
    /**
     * Removes specified key from the tree.
     * @param key interval to remove
     * @return payload value if interval was removed successfully, null otherwise
     */
    T remove(Interval key);
    
    /**
     * Iterates through all elements.
     * @return
     */
    Iterable<? extends Map.Entry<Interval,T>> entries();
    
    /**
     * Iterates through all elements that have non-empty intersection with probe.
     * @return
     */
    Iterable<? extends Map.Entry<Interval,T>> intersectingEntries(Interval probe);

    /**
     * Interface for visitors. 
     *
     * @param <T>
     */
    public interface IVisitor<T>
    {
        void visit(T data);
    }

    /**
     * Another way of iterating trough intersecting elements is via visitor.
     * @param probe
     * @param visitor
     */
    void visitIntersecting(Interval probe, IVisitor<Map.Entry<Interval,T>> visitor);
}
