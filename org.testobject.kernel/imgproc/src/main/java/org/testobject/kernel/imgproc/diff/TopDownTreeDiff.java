package org.testobject.kernel.imgproc.diff;

import java.util.LinkedList;
import java.util.List;

/**
 * Compares leaf-nodes of a "left" and "right" tree and determines tree-diffs in a top-down fashion.
 * 
 * @author nijkamp
 *
 */
public class TopDownTreeDiff<T>
{
    private static final float MIN_MATCH_SIM = .5f;
    
    public interface Adapter<T>
    {
        boolean isContainer(T node);
        
        List<? extends T> getChilds(T node);
        
        float similarity(T node1, T node2);
    }
    
    private final Adapter<T> adapter;
    
    public TopDownTreeDiff(Adapter<T> adapter)
    {
        this.adapter = adapter;
    }
    
    public List<T> inserts(T left, T right)
    {
        List<T> inserts = new LinkedList<T>();
        match(left, right, inserts);
        return inserts;
    }      

    private void match(T leftParent, T rightParent, List<T> rightInserts)
    {
        if(adapter.isContainer(rightParent))
        {
            for(T rightChild : adapter.getChilds(rightParent))
            {
                List<T> leftMatches = findAmbiguousCandidates(adapter.getChilds(leftParent), rightChild);
                if(leftMatches.isEmpty())
                {
                    rightInserts.add(rightChild);
                }
                else
                {                  
                    // ambiguity resolution                    
                    float bestSim = 0f;
                    T bestNode = null;
                    for(T leftNode : leftMatches)
                    {
                        float current = adapter.similarity(leftNode, rightChild);
                        if(current > bestSim)
                        {
                            bestSim = current;
                            bestNode = leftNode;
                        }
                    }
                    if(bestSim > MIN_MATCH_SIM)
                    {
                        match(bestNode, rightChild, rightInserts);
                    }
                    else
                    {
                        rightInserts.add(rightChild);
                    }                    
                }  
            }
        }
    }
    
    private List<T> findAmbiguousCandidates(List<? extends T> leftNodes, T rightNode)
    {
        List<T> candidates = new LinkedList<T>();
        for(T leftNode : leftNodes)
        {
            if(leftNode.getClass() == rightNode.getClass())
            {
                candidates.add(leftNode);
            }
        }
        return candidates;
    }
}
