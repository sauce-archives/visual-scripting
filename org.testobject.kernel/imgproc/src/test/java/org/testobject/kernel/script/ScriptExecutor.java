package org.testobject.kernel.script;

import org.testobject.kernel.replay.Playback.Matcher.Match;

public interface ScriptExecutor {

	Result replay(Script script);

	public class Result {

		private final Match match;
		private final String message;

		public Result(Match match, String message) {
			this.match = match;
			this.message = message;
		}

		public Result(Match match) {
			this(match, "");
		}

		public Match getMatch() {
			return match;
		}

		public String getMessage() {
			return message;
		}

	}
}