package org.testobject.kernel.imgproc.diff;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.testobject.commons.util.distances.Levenshtein;
import org.testobject.commons.util.distances.Levenshtein.Costs;
import org.testobject.commons.util.distances.Levenshtein.Op;

/**
 * Compares leaf-nodes of a "left" and "right" tree and determines tree-diffs in a top-down fashion.
 * 
 * @author nijkamp
 *
 */
public class TopDownStringTreeDiff<T>
{    
    public interface Adapter<T>
    {
        boolean isContainer(T node);
        
        T[] getChilds(T node);
        
        boolean equalsType(T left, T right);
    }
    
    public static class Mutation<T>
    {
        public enum Type { INSERT, DELETE, UPDATE };
        
        public final Type type;
        public T before, after;
        
        public Mutation(Type type, T after)
        {
            this.type = type;
            this.after = after;
        }
        
        public Mutation(Type type, T before, T after)
        {
            this.type = type;
            this.before = before;
            this.after = after;
        }
    }
    
    private final Costs<T> costs = new Costs<T>()
    {
        @Override
        public float substitution(T s, T t)
        {
            // FIXME costs for widget property updates and propagation of property updates (en)
            // update could cost less than 1 (e.g. label change)
            return 1f;
        }
    };
    
    private final Adapter<T> adapter;
    
    public TopDownStringTreeDiff(Adapter<T> adapter)
    {
        this.adapter = adapter;
    }
    
    public List<Mutation<T>> mutations(T left, T right)
    {
        List<Mutation<T>> mutations = new LinkedList<Mutation<T>>();
        match(left, right, mutations);
        return mutations;
    }      

    private void match(T leftParent, T rightParent, List<Mutation<T>> mutations)
    {
        if(adapter.isContainer(leftParent) && adapter.isContainer(rightParent))
        {
            List<T> leftChilds = Arrays.asList(adapter.getChilds(leftParent));
            List<T> rightChilds = Arrays.asList(adapter.getChilds(rightParent));
            
            Levenshtein<T> levenshtein = new Levenshtein<T>(costs, leftChilds, rightChilds);
            Iterator<T> source = leftChilds.iterator();
            Iterator<T> target = rightChilds.iterator();
            
            for(Op op : levenshtein.getAlignment())
            {
                if(op == Op.Match)
                {                    
                    T left = source.next();
                    T right = target.next();
                    match(left, right, mutations);
                }
                else if(op == Op.Insert)
                {
                    T inserted = target.next();
                    mutations.add(new Mutation<T>(Mutation.Type.INSERT, inserted));
                }
                else if(op == Op.Substitute)
                {
                    T left = source.next();
                    T right = target.next();
                    if(adapter.equalsType(left, right) == false)
                    {
                        mutations.add(new Mutation<T>(Mutation.Type.DELETE, left));
                        mutations.add(new Mutation<T>(Mutation.Type.INSERT, right));
                    }
                    else
                    {
                        mutations.add(new Mutation<T>(Mutation.Type.UPDATE, left, right));
                    }
                }
                else if(op == Op.Delete)
                {
                    T deleted = source.next();
                    mutations.add(new Mutation<T>(Mutation.Type.DELETE, deleted));
                }
            }
        }
    }
}
