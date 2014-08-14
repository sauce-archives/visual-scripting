package org.testobject.kernel.replay.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.events.output.Responses;
import org.testobject.kernel.api.events.output.Responses.Response;
import org.testobject.kernel.classification.matching.Matching;
import org.testobject.kernel.classification.matching.Resolver;
import org.testobject.kernel.classification.matching.Resolver.CreateDescriptorId;
import org.testobject.kernel.classification.matching.Resolver.ResolveByScan;
import org.testobject.kernel.replay.Replay;

/**
 * 
 * @author enijkamp
 *
 */
public class ResponsesReplayMatcher implements Replay.Matcher {
	
	public static final Log log = LogFactory.getLog(ResponsesReplayMatcher.class);
	
	private final Matching matching;
    private final List<Responses.Response> recordResponses;
	private final Image.Int recordBeforeRaw, recordAfterRaw;

	private final boolean ignorePosition;
    
    public ResponsesReplayMatcher(Matching matching, List<Responses.Response> recordResponses, Image.Int recordBeforeRaw, Image.Int recordAfterRaw, boolean ignorePosition) {
    	this.matching = matching;
        this.recordResponses = recordResponses;
        this.recordBeforeRaw = recordBeforeRaw;
        this.recordAfterRaw = recordAfterRaw;
		this.ignorePosition = ignorePosition;
    }

    @Override
    public Replay.Matcher.Result match(Response replayResponse, Image.Int replayBeforeRaw, Image.Int replayAfterRaw, CreateDescriptorId createDescriptorId) {
    	
    	Replay.Matcher.Level bestMatch = Replay.Matcher.Level.FAILURE;
    	Responses.Response bestResponse = null;
    	
    	// by path
    	log.debug("matching response '" + replayResponse + "' by path");
    	{
            for(Responses.Response recordResponse : recordResponses) {
                if(instanceOf(recordResponse, replayResponse, Responses.Appears.class)) {
                    Responses.Appears appears1 = (Responses.Appears) recordResponse;
                    Responses.Appears appears2 = (Responses.Appears) replayResponse;
                    
                    Level currentMatch = matchByPath(appears1, appears2);
                    if(currentMatch.isSevere(bestMatch)) {
                    	bestMatch = currentMatch;
                    	bestResponse = replayResponse;
                    }
                }
                
                if(instanceOf(recordResponse, replayResponse, Responses.Disappears.class)) {
                    Responses.Disappears disappears1 = (Responses.Disappears) recordResponse;
                    Responses.Disappears disappears2 = (Responses.Disappears) replayResponse;
                    
                    Level currentMatch = matchByPath(disappears1, disappears2);
                    if(currentMatch.isSevere(bestMatch)) {
                    	bestMatch = currentMatch;
                    	bestResponse = replayResponse;
                    }
                }
                
                if(instanceOf(recordResponse, replayResponse, Responses.Update.class)) {
                    Responses.Update update1 = (Responses.Update) recordResponse;
                    Responses.Update update2 = (Responses.Update) replayResponse;
                    
                    Level currentMatch = matchByPath(update1, update2);
                    if(currentMatch.isSevere(bestMatch)) {
                    	bestMatch = currentMatch;
                    	bestResponse = replayResponse;
                    }
                }
                
                if(bestMatch == Replay.Matcher.Level.SUCCESS) {
                	return new Result(bestMatch, bestResponse);
                }
            }
    	}
    	
    	// by tail
    	log.debug("matching response '" + replayResponse + "' by tail");
    	{
            for(Responses.Response recordResponse : recordResponses) {
                if(instanceOf(recordResponse, replayResponse, Responses.Appears.class)) {
                    Responses.Appears appears1 = (Responses.Appears) recordResponse;
                    Responses.Appears appears2 = (Responses.Appears) replayResponse;
                    
                    Level currentMatch = matchByTail(appears1, appears2);
                    if(currentMatch.isSevere(bestMatch)) {
                    	bestMatch = currentMatch;
                    	bestResponse = replayResponse;
                    }
                }
                
                if(instanceOf(recordResponse, replayResponse, Responses.Disappears.class)) {
                    Responses.Disappears disappears1 = (Responses.Disappears) recordResponse;
                    Responses.Disappears disappears2 = (Responses.Disappears) replayResponse;
                    
                    Level currentMatch = matchByTail(disappears1, disappears2);
                    if(currentMatch.isSevere(bestMatch)) {
                    	bestMatch = currentMatch;
                    	bestResponse = replayResponse;
                    }
                }
                
                if(instanceOf(recordResponse, replayResponse, Responses.Update.class)) {
                    Responses.Update update1 = (Responses.Update) recordResponse;
                    Responses.Update update2 = (Responses.Update) replayResponse;
                    
                    Level currentMatch = matchByTail(update1, update2);
                    if(currentMatch.isSevere(bestMatch)) {
                    	bestMatch = currentMatch;
                    	bestResponse = replayResponse;
                    }
                }
                
                if(bestMatch == Replay.Matcher.Level.SUCCESS) {
                	return new Result(bestMatch, bestResponse);
                }
            }
    	}
    	
    	// by scan
    	log.debug("matching response '" + replayResponse + "' by scan");
    	{
            if(replayResponse instanceof Responses.Appears) {
                Responses.Appears appears1 = (Responses.Appears) replayResponse;
                
                Level currentMatch = matchByScan(appears1.path, replayAfterRaw, recordAfterRaw);
                if(currentMatch.isSevere(bestMatch)) {
                	bestMatch = currentMatch;
                	bestResponse = replayResponse;
                }
            }
                
            if(replayResponse instanceof Responses.Disappears) {
                Responses.Disappears disappears1 = (Responses.Disappears) replayResponse;
                
                Level currentMatch = matchByScan(disappears1.path, replayBeforeRaw, recordBeforeRaw);
                if(currentMatch.isSevere(bestMatch)) {
                	bestMatch = currentMatch;
                	bestResponse = replayResponse;
                }
            }
            
            if(replayResponse instanceof Responses.Update) {
            	// TODO implement update (en)
            	throw new UnsupportedOperationException();
            }
    	}
    	
    	return new Result(bestMatch, bestResponse);
	}
    
    private Level matchByScan(final Locator.Qualifier locator, final Image.Int raw1, final Image.Int raw2) {
    	Resolver.CreateDescriptorId createDescriptorId = new Resolver.CreateDescriptorId() {
    		
    		int id = 0;

			@Override
			public int createId() {
				return id++;
			}
    	};
    	
    	ResolveByScan scan = new Resolver.ResolveByScan(raw1, raw2, createDescriptorId, ignorePosition);
    	
		double match = scan.resolve(Locator.Node.Factory.none(), locator).probability;
		
		return toMatch(match);
	}
    
    private boolean instanceOf(Response response1, Response response2, Class<? extends Response> cls) {
        return response1.getClass().equals(cls) && response2.getClass().equals(cls);
    }
    
    private Replay.Matcher.Level matchByPath(Responses.Appears appears1, Responses.Appears appears2) {
        return matchByPath(appears1.path, appears2.path);
    }

	private Replay.Matcher.Level matchByPath(Responses.Disappears disappears1, Responses.Disappears disappears2) {
        return matchByPath(disappears1.path, disappears2.path);
    }
    
    private Replay.Matcher.Level matchByPath(Responses.Update update1, Responses.Update update2) {
    	// FIXME implement (en)
    	throw new IllegalStateException("implement");
    }
    
    private Replay.Matcher.Level matchByTail(Responses.Appears appears1, Responses.Appears appears2) {
        return matchByTail(appears1.path, appears2.path);
    }

	private Replay.Matcher.Level matchByTail(Responses.Disappears disappears1, Responses.Disappears disappears2) {
        return matchByTail(disappears1.path, disappears2.path);
    }
    
    private Replay.Matcher.Level matchByTail(Responses.Update update1, Responses.Update update2) {
    	// FIXME implement (en)
    	throw new IllegalStateException("implement");
    }

	private Replay.Matcher.Level matchByTail(Locator.Qualifier qualifier1, Locator.Qualifier qualifier2) {
		Locator.Descriptor tail1 = qualifier1.getPath().getLast();
		Locator.Descriptor tail2 = qualifier2.getPath().getLast();
		
		double match = matching.match(tail1, tail2);
		
		return toMatch(match);
	}

	private Replay.Matcher.Level matchByPath(Locator.Qualifier qualifier1, Locator.Qualifier qualifier2) {
		
		if(qualifier1.getPath().size() != qualifier2.getPath().size()) {
		
			return Replay.Matcher.Level.FAILURE;
		
		} else {
			
			Replay.Matcher.Level bestMatch = Replay.Matcher.Level.SUCCESS;
			
		    for(int i = 0; i < qualifier1.getPath().size(); i++) {
		    	double match = matching.match(qualifier1.getPath().get(i), qualifier2.getPath().get(i));

		    	// fail
				if(match < Thresholds.Fail) {
		        	return Replay.Matcher.Level.FAILURE;
		        }
		        
		        // fuzzy
		        if(match < Thresholds.Fuzzy) {
		        	bestMatch = Replay.Matcher.Level.FUZZY;
		        }
		    }
		    
		    return bestMatch;
		}
	}

	private static Level toMatch(double match) {
		if(match < Thresholds.Fail) {
			return Replay.Matcher.Level.FAILURE;
		}
		
		if(match < Thresholds.Fuzzy) {
			return Replay.Matcher.Level.FUZZY;
		} 
		
		return Replay.Matcher.Level.SUCCESS;
	}

}