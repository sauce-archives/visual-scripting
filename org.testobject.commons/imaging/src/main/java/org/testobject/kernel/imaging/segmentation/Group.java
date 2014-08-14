package org.testobject.kernel.imaging.segmentation;

import org.testobject.commons.math.algebra.Rectangle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class that represents a set of bounding boxes, keeps track of the total bounding box.
 * 
 * @author enijkamp
 */
public class Group<T> implements HasBoundingBox, Iterable<T>
{
	public interface Adapter<T> {
		
		Rectangle.Int getBoundingBox(T t);
		
	}
	
    private final Rectangle.Int bbox = new Rectangle.Int();
    {
        bbox.w = -1;
        bbox.h = -1;
        bbox.x = -1;
        bbox.y = -1;
    }
    private final List<T> content = new ArrayList<>();
	private final Adapter<T> adapter;
    
    public Group(Adapter<T> adapter) {
		this.adapter = adapter;
    }
    
    public Group(Adapter<T> adapter, List<T> seeds) {
    	this(adapter);
        addAll(seeds);
    }

    public Rectangle.Int getBoundingBox() {
        return bbox;
    }
        
    public List<T> getContent() {
        return content;
    }
        
    public void add(T b) {
        bbox.add(adapter.getBoundingBox(b));
        content.add(b);
    }

    public void addAll(List<T> seeds) {
        content.addAll(seeds);
            
        for(T b : seeds) {
            bbox.add(adapter.getBoundingBox(b));
        }
    }
    
    public boolean isEmpty() {
        return size() == 0;
    }
    
    public int size() {
        return content.size();
    }

    @Override
    public Iterator<T> iterator() {
        return content.iterator();
    }
}