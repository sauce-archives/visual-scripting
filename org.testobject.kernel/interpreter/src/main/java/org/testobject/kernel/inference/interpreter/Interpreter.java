package org.testobject.kernel.inference.interpreter;

import java.util.List;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.classification.graph.Locator.Node;
import org.testobject.kernel.api.events.output.Events;
import org.testobject.kernel.api.events.output.Requests.Request;
import org.testobject.kernel.api.events.output.Responses.Response;

/**
 * 
 * @author enijkamp
 *
 */
public interface Interpreter {

	interface Factory {

		Interpreter create();

	}

	interface Input {

		long getTimestamp();

		Image.Int getRaw();

		Locator.Node getLocators();

		class Factory {

			public static Input create(final Image.Int raw, final Locator.Node locators, final long timestamp) {
				return new Input() {
					@Override
					public org.testobject.commons.util.image.Image.Int getRaw() {
						return raw;
					}

					@Override
					public Node getLocators() {
						return locators;
					}

					@Override
					public long getTimestamp() {
						return timestamp;
					}
				};
			}
		}
	}

	interface Tie {

		void handle(Damages damages);

		class Factory {

			private final static Tie mock = new Tie() {
				@Override
				public void handle(Damages damages) {

				}
			};

			public static Tie stub() {
				return mock;
			}
		}

	}

	interface RequestResult {

		long getTimeoutMs();

		Request getRequest();

		Pass getBefore();

		class Factory {

			public static RequestResult create(final Pass before, final Request request, final long timeout) {
				return new RequestResult() {

					@Override
					public Pass getBefore() {
						return before;
					}

					@Override
					public long getTimeoutMs() {
						return timeout;
					}

					@Override
					public Request getRequest() {
						return request;
					}
				};
			}
		}
	}

	interface ResponseResult {

		long getTimeoutMs();

		List<Response> getResponses();

		Damages getDamages();

		Pass getBefore();

		Pass getAfter();

		class Factory {

			public static ResponseResult create(final Pass before, final Pass after, final List<Response> responses, final Damages damages,
					final long timeout) {
				return new ResponseResult() {

					@Override
					public Pass getBefore() {
						return before;
					}

					@Override
					public Pass getAfter() {
						return after;
					}

					@Override
					public long getTimeoutMs() {
						return timeout;
					}

					@Override
					public List<Response> getResponses() {
						return responses;
					}

					@Override
					public Damages getDamages() {
						return damages;
					}
				};
			}
		}
	}

	interface Result {

		RequestResult getRequestResult();

		ResponseResult getResponseResult();

		class Factory {

			private final static Result none = new Result() {

				@Override
				public RequestResult getRequestResult() {
					throw new IllegalStateException();
				}

				@Override
				public ResponseResult getResponseResult() {
					throw new IllegalStateException();
				}
			};

			public static Result none() {
				return none;
			}

			public static Result create(final Damages damages, final Request request, final List<Response> responses,
					final long requestTimeout, final long responseTimeout, final Pass before, final Pass after) {
				return new Result() {

					final RequestResult requestResult = RequestResult.Factory.create(before, request, requestTimeout);
					final ResponseResult responseResult = ResponseResult.Factory.create(before, after, responses, damages, responseTimeout);

					@Override
					public RequestResult getRequestResult() {
						return requestResult;
					}

					@Override
					public ResponseResult getResponseResult() {
						return responseResult;
					}
				};
			}
		}
	}

	interface Damages {

		List<Rectangle.Int> getDamages();

		class Factory {
			public static Damages create(final List<Rectangle.Int> damages) {
				return new Damages() {
					@Override
					public List<Rectangle.Int> getDamages() {
						return damages;
					}
				};
			}

			public static Damages empty() {
				return new Damages() {
					@Override
					public List<Rectangle.Int> getDamages() {
						return Lists.empty();
					}
				};
			}
		}

	}

	interface Pass {

		Image.Int getRaw();

		Locator.Node getLocators();

		class Factory {
			public static Pass create(final Image.Int raw, final Locator.Node locators) {
				return new Pass() {

					@Override
					public org.testobject.commons.util.image.Image.Int getRaw() {
						return raw;
					}

					@Override
					public Node getLocators() {
						return locators;
					}
				};
			}
		}
	}

	Result interpret(Events.Event event, Input before, Input after);

	RequestResult interpretRequest(Events.Event event, Input before);

	ResponseResult interpretResponses(Events.Event event, Input before, Input after);

}
