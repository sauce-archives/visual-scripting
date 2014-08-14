package org.testobject.kernel.replay.impl;

import java.util.List;

import org.testobject.kernel.locator.api.Locator;
import org.testobject.kernel.replay.LocatorMatcher;
import org.testobject.kernel.replay.Playback;
import org.testobject.kernel.script.api.Script;
import org.testobject.kernel.script.api.Script.Responses.Response;

/**
 * 
 * @author nijkamp
 *
 */
public class SimpleMatcher implements Playback.Matcher
{        
    private final List<Script.Responses.Response> responses;
    
    public SimpleMatcher(List<Script.Responses.Response> responses)
    {
        this.responses = responses;
    }

    @Override
    public Playback.Matcher.Match match(Response response1)
    {
        for(Script.Responses.Response response2 : responses)
        {
            if(instanceOf(response1, response2, Script.Responses.Appears.class))
            {
                Script.Responses.Appears appears1 = (Script.Responses.Appears) response1;
                Script.Responses.Appears appears2 = (Script.Responses.Appears) response2;
                
                if(equalsAppears(appears1, appears2))
                {
                    return Playback.Matcher.Match.SUCCESS;
                }
            }
            
            if(instanceOf(response1, response2, Script.Responses.Disappears.class))
            {
                Script.Responses.Disappears disappears1 = (Script.Responses.Disappears) response1;
                Script.Responses.Disappears disappears2 = (Script.Responses.Disappears) response2;
                
                if(equalsDisappears(disappears1, disappears2))
                {
                    return Playback.Matcher.Match.SUCCESS;
                }
            }
            
            // FIXME implement (en)
            if(instanceOf(response1, response2, Script.Responses.Update.class))
            {
                return Playback.Matcher.Match.SUCCESS;
            }
        }
        
        return Playback.Matcher.Match.FAILURE;
    }
    
    private boolean instanceOf(Response response1, Response response2, Class<? extends Response> clazz)
    {
        return response1.getClass().equals(clazz) && response2.getClass().equals(clazz);
    }
    
    private boolean equalsAppears(Script.Responses.Appears appears1, Script.Responses.Appears appears2)
    {
        return equalsPaths(appears1.path, appears2.path);
    }
    
    private boolean equalsDisappears(Script.Responses.Disappears disappears1, Script.Responses.Disappears disappears2)
    {
        return equalsPaths(disappears1.path, disappears2.path);
    }
    
    private boolean equalsPaths(List<Locator> path1, List<Locator> path2)
    {
        if(path1.size() != path2.size())
        {
            return false;
        }
        
        for(int i = 0; i < path1.size(); i++)
        {
            if(LocatorMatcher.equalsRecursive(path1.get(i), path2.get(i)) == false)
            {
                return false;
            }
        }
        
        return true;
    }
}