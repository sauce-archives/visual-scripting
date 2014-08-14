package org.testobject.commons.util.collections;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author nijkamp
 *
 */
public class EqualityTree<T>
{
    public interface Identity<T>
    {
        boolean equals(T t1, T t2);
    }
    
    public interface Container<T>
    {
        boolean isContainer(T t);
        
        List<T> getChilds(T t);
    }
    
    private final Identity<T> identity;
    private final Container<T> container;    
    private final List<T> roots = new LinkedList<T>();
    
    public EqualityTree(Identity<T> identity, Container<T> container)
    {
        this.identity = identity;
        this.container = container;
    }
    
    public void addRoot(T root)
    {
        roots.add(root);
    }
    
    public List<T> getRoots()
    {
        return Collections.unmodifiableList(roots);
    }    
    
    public boolean containsRoot(T root)
    {
        for(T current : roots)
        {
            if(identity.equals(current, root))
            {
                return true;
            }
        }        
        return false;
    }
    
    public boolean contains(List<T> sourcePath)
    {
        // sanity
        if(sourcePath.isEmpty())
        {
            return false;
        }
        
        // traverse
        return traverseRoots(sourcePath).isEmpty() == false;
    }
    
    public List<T> resolve(List<T> sourcePath)
    {
        // sanity
        if(sourcePath.isEmpty())
        {
            throw new IllegalArgumentException("empty path");
        }
        
        // traverse
        List<LinkedList<T>> targetPaths = traverseRoots(sourcePath);
        
        // sanity
        if(targetPaths.isEmpty())
        {
            throw new IllegalArgumentException("no match candidate found");
        }        
        if(targetPaths.size() > 1)
        {
            // FIXME this should not happen, but does (en)
            System.out.println("warning: EqualityTree.resolve() cannot resolve ambiguity");
            // throw new IllegalArgumentException("cannot resolve ambiguity");
        }
        
        // return
        return targetPaths.get(0);
    }
    
    private List<LinkedList<T>> traverseRoots(List<T> sourcePath)
    {        
        // initiate paths
        List<LinkedList<T>> targetPaths = new LinkedList<LinkedList<T>>();
        int sourcePosition = 0;
        T sourceCurrent = sourcePath.get(sourcePosition);
        for(T targetCurrent : roots)
        {
            if(identity.equals(sourceCurrent, targetCurrent) == true)
            {
                targetPaths.addAll(traverse(sourcePath, sourcePosition+1, Lists.toLinkedList(targetCurrent)));
            }
        }
        return targetPaths;
    }
    
    private List<LinkedList<T>> traverse(List<T> sourcePath, int sourcePosition, LinkedList<T> targetPath)
    {
        // current
        T sourceCurrent = sourcePath.get(sourcePosition);
        
        // match
        List<T> targetMatches = match(sourceCurrent, targetPath.getLast());
        if(targetMatches.isEmpty())
        {
            return Collections.emptyList();
        }
        
        // go deeper
        List<LinkedList<T>> targetPaths = new LinkedList<LinkedList<T>>();
        for(T targetMatch : targetMatches)
        {
            if(isLast(sourcePath, sourcePosition))
            {
                targetPaths.add(Lists.concat(targetPath, targetMatch));
            }
            else
            {
                targetPaths.addAll(traverse(sourcePath, sourcePosition + 1, Lists.concat(targetPath, targetMatch)));
            }
        }
        return targetPaths;
    }
    
    private boolean isLast(List<T> list, int position)
    {
        return list.size() <= position + 1;
    }
    
    private List<T> match(T sourceChild, T targetCurrent)
    {
        // has to be a container
        if(container.isContainer(targetCurrent) == false)
        {
            return Collections.emptyList();
        }
        
        // target paths
        List<T> targetPaths = new LinkedList<T>();
        
        // check childs
        for(T targetChild : container.getChilds(targetCurrent))
        {
            if(identity.equals(sourceChild, targetChild) == true)
            {
                targetPaths.add(targetChild);
            }
        }
        
        return targetPaths;
    }
}