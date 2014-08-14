package org.testobject.commons.bus;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that allows to accumulate event registrations and un-register them all at once.
 * 
 * @author mike
 *
 */
public class MultiRegistration implements Registration
{
    private final List<Registration> registrations = new ArrayList<Registration>();

    @Override
    public void unregister()
    {
        for(Registration r : registrations)
        {
            r.unregister();
        }
        
        registrations.clear();
    }
    
    public void add(Registration r)
    {
        registrations.add(r);
    }

}
