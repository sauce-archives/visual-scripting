package org.testobject.kernel.inference.interpreter;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.log.LogUtil;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.classification.graph.Locator.Node;
import org.testobject.kernel.api.classification.graph.Variable;
import org.testobject.kernel.api.events.output.Events.Event;
import org.testobject.kernel.api.events.output.Requests;
import org.testobject.kernel.api.events.output.Responses;
import org.testobject.kernel.api.events.output.Responses.Response;

/**
 * 
 * @author enijkamp
 *
 */
public class SimpleInterpreter implements Interpreter {

	public static final Log log = LogFactory.getLog(SimpleInterpreter.class);

	private final Stages stages;

	@Inject
	public SimpleInterpreter() {
		this.stages = Stages.Builder.stub();
	}

	public SimpleInterpreter(Stages stages) {
		this.stages = stages;
	}

	@Override
	public RequestResult interpretRequest(Event event, Input before) {

		stages.start();

		// 1. script: infer user interactions (on before screen)
		Requests.Request request = InputInference.inferRequest(event, before);
		long requestTimeout = InputInference.inferRequestTimeoutMs(event, before);
		stages.done("requests");

		return RequestResult.Factory.create(toPass(before), request, requestTimeout);
	}

	@Override
	public ResponseResult interpretResponses(Event event, Input before, Input after) {

		stages.start();

		List<Responses.Response> response = Lists.newLinkedList();
		long responseTimeout = InputInference.inferResponseTimeoutMs(event, before, after);

		// 2. damages
		List<Rectangle.Int> damages = Lists.toList(new Rectangle.Int(after.getRaw().w, after.getRaw().h));
		stages.done("damages");

		// 3. disappear
		fillDisappears(before.getLocators(), Lists.toLinkedList(before.getLocators().getDescriptor()), response);
		stages.done("disappear");

		// 4. appear
		fillAppears(after.getLocators(), Lists.toLinkedList(after.getLocators().getDescriptor()), response);
		stages.done("appear");

		return ResponseResult.Factory.create(toPass(before), toPass(after), response, Damages.Factory.create(damages), responseTimeout);
	}

	@Override
	public Result interpret(Event event, Input before, Input after) {

		List<Responses.Response> response = Lists.newLinkedList();

		stages.start();

		// debug
		{
			log.trace("before");
			print(before.getLocators());
			log.trace("after");
			print(after.getLocators());
		}

		// 1. script: infer user interactions (on before screen)

		Requests.Request request = InputInference.inferRequest(event, before);
		stages.done("requests");

		// 2. damages: regions of change

		// int tolerance = 5;
		// List<Rectangle.Int> damages = ImageComparator.compare(before.getRaw(), after.getRaw(), tolerance);
		// tie.handle(Damages.Factory.create(damages));

		List<Rectangle.Int> damages = Lists.toList(new Rectangle.Int(after.getRaw().w, after.getRaw().h));
		stages.done("damages");

		// 3. disappear
		fillDisappears(before.getLocators(), Lists.toLinkedList(before.getLocators().getDescriptor()), response);
		stages.done("disappear");

		// 4. appear
		fillAppears(after.getLocators(), Lists.toLinkedList(after.getLocators().getDescriptor()), response);
		stages.done("appear");

		long requestTimeout = InputInference.inferRequestTimeoutMs(event, before);
		long responseTimeout = InputInference.inferResponseTimeoutMs(event, before, after);

		return Result.Factory.create(Damages.Factory.create(damages), request, response, requestTimeout, responseTimeout, toPass(before),
				toPass(after));
	}

	private void fillDisappears(Node node, LinkedList<Locator.Descriptor> path, List<Response> response) {
		for (Locator.Node child : node.getChildren()) {
			LinkedList<Locator.Descriptor> childPath = Lists.concat(path, child.getDescriptor());
			response.add(new Responses.Disappears(Locator.Qualifier.Factory.create(childPath)));
			fillDisappears(child, childPath, response);
		}
	}

	private void fillAppears(Node node, LinkedList<Locator.Descriptor> path, List<Response> response) {
		for (Locator.Node child : node.getChildren()) {
			LinkedList<Locator.Descriptor> childPath = Lists.concat(path, child.getDescriptor());
			response.add(new Responses.Appears(Locator.Qualifier.Factory.create(childPath)));
			fillAppears(child, childPath, response);
		}
	}

	private static void print(Locator.Node node) {
		Locator.Printer.print(node, 1, LogUtil.getTraceStream(log), Variable.Names.Geometric.position, Variable.Names.Geometric.size);
	}

	private Pass toPass(Input input) {
		return Pass.Factory.create(input.getRaw(), input.getLocators());
	}
}
