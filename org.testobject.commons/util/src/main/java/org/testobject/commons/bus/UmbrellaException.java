package org.testobject.commons.bus;

import java.util.Iterator;
import java.util.List;

/**
 * Umbrella exception is a container for one or more exceptions.
 * 
 * Useful for event-handling mechanism, where events are dispatched to multiple destinations, and
 * each destination (in theory) can throw an exception during event handling.
 */
public class UmbrellaException extends RuntimeException implements Iterable<Throwable>
{
    private static final long serialVersionUID = 1L;

    private final List<Throwable> causes;

    public UmbrellaException(List<Throwable> causes)
    {
        super("One or more exceptions caught, see getCauses() for complete list", causes.size() == 0 ? null : causes.get(0));
        System.err.println(causes.get(0));
        causes.get(0).printStackTrace();
        
        this.causes = causes;
    }

    @Override
    public Iterator<Throwable> iterator()
    {
        return causes.iterator();
    }

    public List<Throwable> getCauses()
    {
        return causes;
    }
}
