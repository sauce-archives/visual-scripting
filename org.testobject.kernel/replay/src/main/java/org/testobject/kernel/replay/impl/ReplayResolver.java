package org.testobject.kernel.replay.impl;

import java.util.Map;
import java.util.Set;

import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.classification.graph.Locator.Node;
import org.testobject.kernel.classification.matching.Matching;
import org.testobject.kernel.classification.matching.Resolver;
import org.testobject.kernel.classification.matching.Resolver.CreateDescriptorId;
import org.testobject.kernel.classification.matching.Resolver.Resolution;
import org.testobject.kernel.inference.input.Framebuffer;
import org.testobject.kernel.replay.Replay;

import com.google.common.collect.Sets;
import com.google.inject.assistedinject.AssistedInject;

/**
 * 
 * @author enijkamp
 * 
 */
public class ReplayResolver implements Replay.Resolver {
	
	public static final Log log = LogFactory.getLog(ReplayResolver.class);

	private final Map<String, org.testobject.kernel.classification.matching.Matcher> matchers;

	@AssistedInject
	public ReplayResolver(@Named("locator.matchers") Map<String, org.testobject.kernel.classification.matching.Matcher> matchers) {
		this.matchers = matchers;
	}

	@Override
	public Replay.Resolution resolve(Locator.Qualifier locator, Image.Int recordImage, Image.Int replayImage, CreateDescriptorId createDescriptorId, double minSimilarity, double fuzzySimilarity, boolean ignorePosition) {
		
		Matching matching = new Matching(matchers, recordImage, replayImage);
		Resolver resolver = new Resolver(minSimilarity, fuzzySimilarity, ignorePosition, matching, recordImage, replayImage, createDescriptorId);
		
		Resolution resolution = resolver.resolve(Locator.Node.Factory.none(), locator);
		Replay.Matcher.Level level = toMatchLevel(resolution, minSimilarity, fuzzySimilarity, ignorePosition);
		Replay.Match match = new Replay.Match(level, resolution.qualifier, resolution.probability, resolution.scale, resolution.factors, resolution.resolved);
		
		return new Replay.Resolution(new Framebuffer(now(), replayImage), createBeforeLocators(resolution), match);
	}

	private Node createBeforeLocators(Resolution resolution) {
		if (resolution.resolved) {
			return Locator.Node.Factory.create(resolution.qualifier.getPath().getLast(), Lists.<Locator.Node>empty());
		} else {
			return Locator.Node.Factory.none();
		}
	}

	@Override
	public Set<Replay.Match> match(Locator.Qualifier locator, Image.Int recordImage, Image.Int replayImage, CreateDescriptorId createDescriptorId, double minSimilarity, double fuzzySimilarity, boolean ignorePosition) {
		
		Matching matching = new Matching(matchers, recordImage, replayImage);
		
		Resolver resolver = new Resolver(minSimilarity, fuzzySimilarity, ignorePosition, matching, recordImage, replayImage, createDescriptorId);
		
		Set<Resolution> resolutions = resolver.resolveAll(Locator.Node.Factory.none(), locator);
		
		Set<Replay.Match> matches = Sets.newHashSet();
		
		for(Resolution resolution : resolutions) {
			Replay.Match match = new Replay.Match(toMatchLevel(resolution, minSimilarity, fuzzySimilarity, ignorePosition), resolution.qualifier, resolution.probability, resolution.scale, resolution.factors, resolution.resolved);
			matches.add(match);
		}

		return matches;
	}

	private Replay.Matcher.Level toMatchLevel(Resolution resolution, double minSimilarity, double fuzzySimilarity, boolean ignorePosition) {
		if (resolution.probability <= minSimilarity) {
			return Replay.Matcher.Level.FAILURE;
		}

		if (resolution.probability <= fuzzySimilarity) {
			return Replay.Matcher.Level.FUZZY;
		}
		
		if (ignorePosition == false) {		
			for(Map.Entry<String, Double> probability : resolution.factors.entrySet()) {
				if(probability.getValue() <= fuzzySimilarity) {
					return Replay.Matcher.Level.FUZZY;
				}
			}
		}

		return Replay.Matcher.Level.SUCCESS;
	}

	private static long now() {
		return System.currentTimeMillis();
	}

}
