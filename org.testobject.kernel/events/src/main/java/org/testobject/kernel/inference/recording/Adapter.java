package org.testobject.kernel.inference.recording;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.events.output.Events;
import org.testobject.kernel.classification.parser.Parser;
import org.testobject.kernel.inference.input.Framebuffer;
import org.testobject.kernel.inference.input.InputTransition;
import org.testobject.kernel.inference.interpreter.Interpreter;

/**
 * 
 * @author enijkamp
 *
 */
public interface Adapter {

	class InputToParser {

		public static final Log log = LogFactory.getLog(Adapter.ParserToInterpreter.class);

		private final ExecutorService executor;
		private final Parser.Executor<Locator.Node> parserBefore, parserAfter;

		public InputToParser(ExecutorService executor, Parser.Executor.Factory<Locator.Node> factory) {
			this.executor = executor;
			this.parserBefore = factory.create();
			this.parserAfter = factory.create();

			if (System.identityHashCode(parserBefore) == System.identityHashCode(parserAfter)) {
				throw new IllegalStateException("parser instances point to the same object reference");
			}
		}

		public ParserResult parse(InputTransition transition) {
			try {
				Events.Event event = transition.getEvent();

				final Image.Int rawBefore = transition.getBefore().framebuffer;
				final Image.Int rawAfter = transition.getAfter().framebuffer;

				Callable<Locator.Node> before = new Callable<Locator.Node>() {
					@Override
					public Locator.Node call() throws Exception {
						return parserBefore.execute(rawBefore).get(0);
					}
				};

				Callable<Locator.Node> after = new Callable<Locator.Node>() {
					@Override
					public Locator.Node call() throws Exception {
						return parserAfter.execute(rawAfter).get(0);
					}
				};

				Future<Locator.Node> futureBefore = executor.submit(before);
				Future<Locator.Node> futureAfter = executor.submit(after);

				Locator.Node locatorBefore = futureBefore.get();
				Locator.Node locatorAfter = futureAfter.get();

				return new ParserResult(event, transition.getBefore().timestamp, transition.getAfter().timestamp, rawBefore, rawAfter,
						locatorBefore, locatorAfter);

			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		}

		public ParserResult.Partial parse(Framebuffer beforeFramebuffer) {
			final Image.Int rawBefore = beforeFramebuffer.framebuffer;

			Locator.Node locatorsBefore = parserBefore.execute(rawBefore).get(0);

			return new ParserResult.Partial(beforeFramebuffer.timestamp, locatorsBefore, beforeFramebuffer.framebuffer);
		}
	}

	class ParserToInterpreter {

		public interface Factory {

			ParserToInterpreter create();

		}

		public static final Log log = LogFactory.getLog(Adapter.ParserToInterpreter.class);

		private final Interpreter interpreter;

		@Inject
		public ParserToInterpreter(Interpreter interpreter) {
			this.interpreter = interpreter;
		}

		public TransitionResult interpret(ParserResult parserResult) {

			Interpreter.Input before = Interpreter.Input.Factory.create(parserResult.before.raw, parserResult.before.locator,
					parserResult.before.time);
			Interpreter.Input after = Interpreter.Input.Factory.create(parserResult.after.raw, parserResult.after.locator,
					parserResult.after.time);

			Interpreter.Result result = interpreter.interpret(parserResult.input, before, after);

			Interpreter.ResponseResult responses = result.getResponseResult();
			Interpreter.RequestResult request = result.getRequestResult();

			return new TransitionResult(request.getRequest(), responses.getResponses(), request.getTimeoutMs(), request.getTimeoutMs(),
					responses.getBefore().getRaw(), responses.getAfter().getRaw(), responses.getBefore().getLocators(), responses
							.getAfter().getLocators());
		}

		public RequestResult interpretRequest(Events.Event input, ParserResult.Partial beforeParserResult) {

			Interpreter.Input before = Interpreter.Input.Factory.create(beforeParserResult.raw, beforeParserResult.locator,
					beforeParserResult.time);

			Interpreter.RequestResult requestResult = interpreter.interpretRequest(input, before);

			return new RequestResult(requestResult.getRequest(), requestResult.getBefore().getRaw(), requestResult.getBefore()
					.getLocators());
		}

		public ResponseResult interpretResponse(Events.Event input, ParserResult.Partial beforeParserResult,
				ParserResult.Partial afterParserResult) {

			Interpreter.Input before = Interpreter.Input.Factory.create(beforeParserResult.raw, beforeParserResult.locator,
					beforeParserResult.time);

			Interpreter.Input after = Interpreter.Input.Factory.create(afterParserResult.raw, afterParserResult.locator,
					afterParserResult.time);

			Interpreter.ResponseResult responseResult = interpreter.interpretResponses(input, before, after);

			return new ResponseResult(responseResult.getResponses(), after.getRaw(), after.getLocators());
		}
	}
}
