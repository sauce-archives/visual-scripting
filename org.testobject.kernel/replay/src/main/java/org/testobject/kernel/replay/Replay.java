package org.testobject.kernel.replay;

import java.util.Map;
import java.util.Set;

import org.testobject.commons.util.image.Image;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.classification.graph.Locator.Node;
import org.testobject.kernel.api.classification.graph.Locator.Qualifier;
import org.testobject.kernel.api.events.output.Responses;
import org.testobject.kernel.api.events.output.Responses.Response;
import org.testobject.kernel.classification.matching.Resolver.CreateDescriptorId;
import org.testobject.kernel.inference.input.Framebuffer;
import org.testobject.kernel.replay.Replay.Matcher.Level;

/**
 * 
 * @author enijkamp
 * 
 */
public interface Replay {

	class Phase {
		
		public final long timestamp;
		public final Image.Int image;
		public final Locator.Node locator;
		
		public Phase(long timestamp,  Image.Int image, Node locator) {
			this.timestamp = timestamp;
			this.image = image;
			this.locator = locator;
		}
	}
	
	class Match {
		
		public final Replay.Matcher.Level level;
		public final Locator.Qualifier qualifier;
		public final double probability;
		public final double scale;
		public final boolean found;
		public final Map<String, Double> factors;
		
		public Match(Level level, Qualifier qualifier, double probability, double scale, Map<String, Double> factors, boolean found) {
			this.level = level;
			this.qualifier = qualifier;
			this.probability = probability;
			this.scale = scale;
			this.factors = factors;
			this.found = found;
		}
	}
	
	class Resolution {
		
		public final Framebuffer replayBeforeFramebuffer;
		public final Locator.Node beforeLocators;
		public final Match match;
		
		public Resolution(Framebuffer replayBeforeFramebuffer, Node beforeLocators, Match match) {
			this.replayBeforeFramebuffer = replayBeforeFramebuffer;
			this.beforeLocators = beforeLocators;
			this.match = match;
		}
	}

	interface Resolver {
		
		interface Factory {
			
			Resolver create();
			
		}
		
		interface Context {
			
			Image.Int getBeforeImage();
			
			class Factory {
				public static Context create(final Image.Int beforeImage) {
					return new Context() {
						@Override
						public Image.Int getBeforeImage() {
							return beforeImage;
						}
					};
				}
			}
			
		}

		Resolution resolve(Locator.Qualifier locator, Image.Int recordImage, Image.Int replayImage, CreateDescriptorId createDescriptorId, double minSimilarity, double fuzzySimilarity, boolean ignorePosition);
		
		Set<Match> match(Locator.Qualifier locator, Image.Int recordImage, Image.Int replayImage, CreateDescriptorId createDescriptorId, double minSimilarity, double fuzzySimilarity, boolean ignorePosition);
	}

	interface Matcher {
		
		enum Level {
			SUCCESS(0), FUZZY(1), FAILURE(2);
			
			private final int level;
			
			private Level(int level) {
				this.level = level;
			}
			
			public boolean isSevere(Matcher.Level other) {
				return other.level > this.level;
			}
		}
		
		class Result {
			public final Level level;
			public final Responses.Response response;
			
			public Result(Level level, Responses.Response response) {
				this.level = level;
				this.response = response;
			}
		}

		Result match(Response response, Image.Int before, Image.Int after, CreateDescriptorId createDescriptorId);
	}
}
