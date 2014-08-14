package org.testobject.kernel.replay;

import java.awt.Point;
import java.util.List;
import java.util.Map;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.locator.api.Locator;
import org.testobject.kernel.pipeline.Classification;
import org.testobject.kernel.platform.Grab;
import org.testobject.kernel.platform.Robot;
import org.testobject.kernel.platform.Window;
import org.testobject.kernel.replay.Playback.Replay.DebugHandler;
import org.testobject.kernel.replay.impl.BusAwaitPipelineEventAdapter;
import org.testobject.kernel.replay.impl.ReplayException;
import org.testobject.kernel.replay.impl.SimpleMatcher;
import org.testobject.kernel.replay.impl.SimpleReplay;
import org.testobject.kernel.script.api.Script;

/**
 * 
 * @author nijkamp
 * 
 */
public interface Playback {
	// FIXME replace with guice -> osgi cannot access impl package (en)
	class Factory {
		public static Matcher newMatcher(List<Script.Responses.Response> response) {
			return new SimpleMatcher(response);
		}

		public static Replay newReplay(Classification classification, AwaitPipelineEvent await, Window window, Robot robot, Grab grab,
				DebugHandler debug) {
			return new SimpleReplay(classification, await, window, robot, grab, debug);
		}

		public static AwaitPipelineEvent newAwaitPipelineEvent() {
			return new BusAwaitPipelineEventAdapter();
		}

	}

	interface Query {
		Blob select(Locator locator);
	}

	class Delta {
		public interface Meta {
			class Builder {
				public static Meta mock() {
					return new Meta() {

					};
				}
			}
		}

		public static class ClickMeta implements Meta {
			public final Point location; // FIXME redundant -> location in ClickOn.Click and here (en)
			public final Blob blob; // FIXME should be locator or add locator (en)

			public ClickMeta(Point location, Blob blob) {
				this.location = location;
				this.blob = blob;
			}
		}

		public static class TypeMeta implements Meta {
		}

		public static class State {
			public final long timestamp;
			public final Image.Int image;
			public final Blob blobs;
			public final Locator locators;
			public final Map<Locator, Blob> locatorToBlob;

			public State(long timestamp, Image.Int image, Blob blobs, Locator locators, Map<Locator, Blob> locatorToBlob) {
				this.timestamp = timestamp;
				this.image = image;
				this.blobs = blobs;
				this.locators = locators;
				this.locatorToBlob = locatorToBlob;
			}
		}

		public final Script.Requests.Request request;
		public final List<Script.Responses.Response> response;
		public final State before, after;
		public final List<Rectangle.Int> diffs;
		public final Meta meta;

		public Delta(Script.Requests.Request request, List<Script.Responses.Response> response, State before, State after,
				List<Rectangle.Int> diffs, Meta meta) {
			this.request = request;
			this.response = response;
			this.before = before;
			this.after = after;
			this.diffs = diffs;
			this.meta = meta;
		}
	}

	@SuppressWarnings("serial")
	class RequestException extends RuntimeException {
		public final Image.Int image;

		public RequestException(Image.Int image) {
			this.image = image;
		}
	}

	interface Replay {
		interface Factory {
			Replay createReplay(Robot robot, Grab grab, AwaitPipelineEvent await, DebugHandler handler);
		}

		interface DebugHandler {
			
			class Builder {
				public static DebugHandler mock() {
					return new DebugHandler()
					{
						@Override
						public void handle(DebugHandler.DebugInfo info)
						{

						}
					};
				}
			}
			
			interface DebugInfo {

			}

			class Locators implements DebugHandler.DebugInfo {
				public final Locator locator;

				public Locators(Locator locator) {
					this.locator = locator;
				}
			}

			class Target implements DebugHandler.DebugInfo {
				public final Blob blob;

				public Target(Blob blob) {
					this.blob = blob;
				}
			}
			
			class Click implements DebugHandler.DebugInfo {
				public final Point point;

				public Click(Point point) {
					this.point = point;
				}
			}

			void handle(DebugHandler.DebugInfo info);
		}
		
		void setDebugHandler(DebugHandler handler);

		Delta replay(Script.Requests.Request request) throws RequestException, ReplayException;
	}

	interface Matcher {
		enum Match {
			UNKNOWN(-1), SUCCESS(0), FUZZY(1), FAILURE(2);
			
			private final int level;
			
			private Match(int level) {
				this.level = level;
			}
			
			public boolean isSevere(Matcher.Match other) {
				return other.level > this.level;
			}
		}

		Match match(Script.Responses.Response response);
	}
}
