package org.testobject.kernel.imgproc.diff;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author nijkamp
 *
 */
public class BottomUpTreeDiff<T>
{
    public interface Adapter<T>
    {
        boolean isContainer(T node);
        
        List<? extends T> getChilds(T node);
    }
    
    private final Adapter<T> adapter;
    
    public BottomUpTreeDiff(Adapter<T> adapter)
    {
        this.adapter = adapter;
    }
    
    /**
     * 
     * @param left
     * @param right
     * @return
     */
    public List<T> inserts(T left, T right)
    {
        List<T> inserts = new LinkedList<T>();
        List<T> leaves = new LinkedList<T>();
        leaves(left, leaves);
        if(!match(right, leaves, inserts))
        {
            inserts.add(right);
        }
        return inserts;
    }      
    
    /**
     * Determines insertions bottom-up (based on leaf-nodes).
     * 
     * @param node "right" tree node
     * @param leaves leaf-nodes of "left" tree
     * @param inserts list of insertions from "left" to "right" tree
     * @return true if node is leaf-node and contained in leaves
     */
    private boolean match(T node, List<T> leaves, List<T> inserts)
    {            
        if(adapter.isContainer(node) == false)
        {
            // 1. mark leaf-nodes
            return leaves.contains(node);
        }
        else
        {
            // 2. group matches
            List<T> marked = new LinkedList<T>();
            boolean partial = false;
            for(T child : adapter.getChilds(node))
            {
                if(!match(child, leaves, inserts))
                {
                    marked.add(child);
                }
                else
                {
                    partial = true;
                }
            }
            // 3. insertions
            if(partial)
            {
                // partial insertion
                inserts.addAll(marked);
                return true;
            }
            else
            {
                // branch insertion
                return false;
            }
        }
    }
    
    /**
     * 
     * @param node
     * @param leaves
     */
    private void leaves(T node, List<T> leaves)
    {
        if(adapter.isContainer(node) == false)
        {
            leaves.add(node);
        }
        else
        {
            for(T child : adapter.getChilds(node))
            {
                leaves(child, leaves);
            }
        }
    }
}