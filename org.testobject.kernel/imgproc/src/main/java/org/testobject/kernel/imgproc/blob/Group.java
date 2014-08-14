package org.testobject.kernel.imgproc.blob;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class that represents a set of bounding boxes, keeps track of the total bounding box.
 * 
 * @author enijkamp
 */
public class Group<T extends BoundingBox> implements BoundingBox, Iterable<T>
{
    private final Rectangle bbox = new Rectangle();
    {
        bbox.width = -1;
        bbox.height = -1;
        bbox.x = -1;
        bbox.y = -1;
    }
    private final List<T> content = new ArrayList<T>();
    
    public Group()
    {
        
    }
    
    public Group(List<T> seeds)
    {
        addAll(seeds);
    }

    public Rectangle getBoundingBox()
    {
        return bbox;
    }
        
    public List<T> getContent()
    {
        return content;
    }
        
    public void add(T b)
    {
        bbox.add(b.getBoundingBox());
        content.add(b);
    }

    public void addAll(List<T> seeds)
    {
        content.addAll(seeds);
            
        for(T b : seeds)
        {
            bbox.add(b.getBoundingBox());
        }
    }
    
    public boolean isEmpty()
    {
        return size() == 0;
    }
    
    public int size()
    {
        return content.size();
    }

    @Override
    public Iterator<T> iterator()
    {
        return content.iterator();
    }
}
