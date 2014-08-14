package org.testobject.kernel.inputs;

import java.util.Comparator;

import org.testobject.commons.bus.Event;
import org.testobject.commons.events.TimestampEvent;

/**
 *
 * @author nijkamp
 *
 */
public class TimestampComparator implements Comparator<Event<?>>
{
    @Override
    public int compare(Event<?> e1, Event<?> e2)
    {
        TimestampEvent<?> t1 = (TimestampEvent<?>) e1;
        TimestampEvent<?> t2 = (TimestampEvent<?>) e2;
		return Long.compare(t1.getTimestamp(), t2.getTimestamp());
    }
}