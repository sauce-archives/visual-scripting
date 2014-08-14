package org.testobject.kernel.replay.impl;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.util.exceptions.Exceptions;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.events.output.Responses;
import org.testobject.kernel.api.events.output.Responses.Response;
import org.testobject.kernel.classification.matching.Matcher;
import org.testobject.kernel.classification.matching.Matching;
import org.testobject.kernel.classification.matching.Resolver;
import org.testobject.kernel.classification.matching.Resolver.CreateDescriptorId;
import org.testobject.kernel.classification.matching.Resolver.Resolution;
import org.testobject.kernel.replay.Replay;

/**
 * 
 * @author enijkamp
 *
 */
public class ResolveReplayMatcher implements Replay.Matcher {
	
	public static final Log log = LogFactory.getLog(ResolveReplayMatcher.class);
	
	private final double thresholdFail, thresholdFuzzy;
	private final Map<String, Matcher> matchers;
	private final Image.Int replayBeforeRaw, replayAfterRaw;
	private final Locator.Node replayBeforeLocator, replayAfterLocator;
	private final boolean ignorePosition;
    
    public ResolveReplayMatcher(Map<String, Matcher> matchers, Image.Int replayBeforeRaw, Image.Int replayAfterRaw,
    		Locator.Node replayBeforeLocator, Locator.Node replayAfterLocator, boolean ignorePosition) {
    	this.matchers = matchers;
    	this.replayBeforeRaw = replayBeforeRaw;
    	this.replayAfterRaw = replayAfterRaw;
    	this.replayBeforeLocator = replayBeforeLocator;
    	this.replayAfterLocator = replayAfterLocator;
		this.thresholdFail = Thresholds.Fail;
		this.thresholdFuzzy = Thresholds.Fuzzy;
		this.ignorePosition = ignorePosition;
    }

    @Override
    public Replay.Matcher.Result match(Response recordResponse, Image.Int recordBeforeRaw, Image.Int recordAfterRaw, CreateDescriptorId createDescriptorId) {
   
    	log.debug("matching response '" + recordResponse);
    	
    	if(recordResponse instanceof Responses.Appears) {
    		Responses.Appears appears = (Responses.Appears) recordResponse;
    		Resolution resolution = resolve(replayAfterLocator, appears.path, replayAfterRaw, recordAfterRaw, createDescriptorId);
    		return new Replay.Matcher.Result(toMatch(resolution), new Responses.Appears(resolution.qualifier));
    	}
    	
    	if(recordResponse instanceof Responses.Disappears) {
    		Responses.Disappears disappears = (Responses.Disappears) recordResponse;
    		Resolution resolution = resolve(replayBeforeLocator, disappears.path, replayBeforeRaw, recordBeforeRaw, createDescriptorId);
    		return new Replay.Matcher.Result(toMatch(resolution), new Responses.Disappears(resolution.qualifier));
    	}
    	
    	throw Exceptions.newUnsupportedTypeException("response", recordResponse.getClass());
	}
    
	private Resolution resolve(Locator.Node target, Locator.Qualifier path, Image.Int replayRaw, Image.Int recordRaw, CreateDescriptorId createDescriptorId) {
		
		Matching matching = new Matching(matchers, replayRaw, recordRaw);
		
		Resolver resolver = new Resolver(thresholdFail, thresholdFuzzy, ignorePosition, matching, recordRaw, replayRaw, createDescriptorId);

		Resolution resolution = resolver.resolve(target, path);
		
		return resolution;
	}

	private Replay.Matcher.Level toMatch(Resolution resolution) {
		if (resolution.probability <= thresholdFail) {
			return Replay.Matcher.Level.FAILURE;
		}

		if (resolution.probability <= thresholdFuzzy) {
			return Replay.Matcher.Level.FUZZY;
		}

		return Replay.Matcher.Level.SUCCESS;
	}
}