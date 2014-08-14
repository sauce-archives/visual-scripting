package org.testobject.kernel.app.modules;

import static org.testobject.kernel.classification.parser.Parser.Builder.classify;
import static org.testobject.kernel.classification.parser.Parser.Builder.group;
import static org.testobject.kernel.classification.parser.Parser.Builder.locators;
import static org.testobject.kernel.classification.parser.Parser.Builder.plan;
import static org.testobject.kernel.classification.parser.Parser.Builder.prune;
import static org.testobject.kernel.classification.parser.Parser.Builder.segment;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.inject.Named;

import org.testobject.commons.bus.AsyncEventBus;
import org.testobject.commons.bus.EventEmitter;
import org.testobject.commons.guice.AbstractModule;
import org.testobject.kernel.api.classification.classifiers.Classifier;
import org.testobject.kernel.api.classification.classifiers.Classifier.Qualifier;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.classification.classifiers.advanced.Registry;
import org.testobject.kernel.classification.classifiers.advanced.MaskSegment;
import org.testobject.kernel.classification.classifiers.advanced.Registry.Sample;
import org.testobject.kernel.classification.parser.Operations;
import org.testobject.kernel.classification.parser.Parser;
import org.testobject.kernel.classification.parser.Plan;
import org.testobject.kernel.inference.input.DelayingAndOrderingQueue;
import org.testobject.kernel.inference.input.GetOrientation;
import org.testobject.kernel.inference.input.InputStateMachine;
import org.testobject.kernel.inference.input.TimestampComparator;
import org.testobject.kernel.inference.input.InputStateMachine.Callback;
import org.testobject.kernel.inference.interpreter.Interpreter;
import org.testobject.kernel.inference.interpreter.Interpreter.Tie;
import org.testobject.kernel.inference.interpreter.SimpleInterpreter;
import org.testobject.kernel.inference.interpreter.Stages;
import org.testobject.kernel.inference.recording.Adapter;
import org.testobject.kernel.platform.robot.Window;
import org.testobject.kernel.replay.Replay;
import org.testobject.kernel.replay.impl.ReplayResolver;

import com.google.inject.Provides;

/**
 * 
 * @author enijkamp
 *
 */
public class KernelModule extends AbstractModule {

	//	private final Registry registry;
	//
	//	public KernelModule(Registry registry) {
	//		this.registry = registry;
	//	}
	//	
	//	public KernelModule() {
	//		this(new Registry.Mutable());
	//	}
	//	
	@Override
	protected void configure() {
		//		install(factory().implement(Replay.Executor.class, ResolveReplayExecutor.class).build(Replay.Executor.Factory.class));
		//		install(factory().implement(Interpreter.class, SimpleInterpreter.class).build(Interpreter.Factory.class));
	}

	//	
	@Provides
	@Named("locator.matchers")
	public Map<String, org.testobject.kernel.classification.matching.Matcher> matchers() {
		Map<String, org.testobject.kernel.classification.matching.Matcher> matchers = new HashMap<>();
		{
			matchers.put(Classifier.Qualifier.Factory.Class.node,
					new org.testobject.kernel.classification.classifiers.advanced.Node.Matcher());
			matchers.put(Classifier.Qualifier.Factory.Class.segment,
					new org.testobject.kernel.classification.classifiers.advanced.MaskSegment.Matcher());
		}
		return matchers;
	}
	//	
	//	@Provides
	//	public Window window() {
	//		return Window.Builder.mock();
	//	}
	//
	//	@Provides
	//	public InputStateMachine.Factory inputStateMachine(final UncaughtExceptionHandler handler) {
	//		return new InputStateMachine.Factory() {
	//			@Override
	//			public InputStateMachine create(GetOrientation getOrientation, InputStateMachine.TransitionSequence sequence, Callback callback) {
	//				AsyncEventBus bus = new AsyncEventBus(handler);
	//				bus.open();
	//				return new InputStateMachine(bus, getOrientation, sequence, callback);
	//			}
	//		};
	//	}
	//
	//	@Provides
	//	public DelayingAndOrderingQueue createDelayingAndOrderingQueue() {
	//		DelayingAndOrderingQueue queue = new DelayingAndOrderingQueue(new TimestampComparator(), 200);
	//		queue.open();
	//		return queue;
	//	}
	//	
	//	@Provides
	//	public Adapter.ParserToInterpreter parserToInterpreter(EventEmitter emitter, Interpreter interpreter) {
	//		return new Adapter.ParserToInterpreter(emitter, interpreter);
	//	}
	//
	//	@Provides
	//	public Interpreter interpreter() {
	//		return new SimpleInterpreter(Stages.Builder.stub(), Tie.Factory.stub());
	//	}
	//
	//	@Provides
	//	public Adapter.InputToParser inputToParser(Parser.Executor.Factory<Locator.Node> factory, EventEmitter emitter) {
	//		// return new Adapter.InputToParser(Executors.newSingleThreadExecutor(), emitter, parser);
	//		return new Adapter.InputToParser(Executors.newFixedThreadPool(2), emitter, factory);
	//	}
	//
	//	@Provides
	//	public Parser.Executor<Locator.Node> parser(Parser.Executor.Factory<Locator.Node> factory) {
	//		return factory.create();
	//	}
	//	
	//	@Provides
	//	public Parser.Executor.Factory<Locator.Node> parserFactory() {
	//		return new Parser.Executor.Factory<Locator.Node>() {
	//			@Override
	//			public Parser.Executor<Locator.Node> create() {
	//				Parser.Builder.Naming naming = Parser.Builder.Naming.create();
	//				
	//				Operations.Input input = segment(0.0f, 50000d, 30);
	//				Operations.Map<Node, Node> group = group();
	//				Operations.Map<Node, Node> prune = prune();
	//				Operations.Map<Node, Node> lowpass = classify(new MaskSegment.Classifier());
	//				Operations.Map<Node, Locator.Node> locators = locators();
	//
	//				Plan plan =
	//						plan(naming.stage())
	//								.input(input)
	//								.map(group)
	//								.map(prune)
	//								.map(lowpass)
	//								.map(locators)
	//						.build();
	//
	//				return new Parser.Executor<Locator.Node>(Parser.Executor.Progress.Builder.sysout(), plan);
	//
	////				Operations.Map<Node, Node> lowpass = cache(classify(new Segment.Classifier()));
	////				Operations.Map<Node, Node> prune = prune();
	////
	////				Operations.Input input1 = cache(segment(0.0f, 50000d, 30));
	////				Operations.Map<Node, Node> group1 = cache(group());
	////				Operations.Map<Node, Node> classify1 = classify(images());
	////				Operations.Reduce<Node, Node> optimize1 = optimize(group1, flat);
	////
	////				Operations.Reduce<Node, Node> optimize3 = optimize(lowpass(loosers), lowpass, hierarchical);
	////				Operations.Map<Node, Locator.Node> locators3 = locators();
	////
	////				Plan plan1 =
	////						plan(naming.stage())
	////								.input(input1)
	////								.map(group1)
	////								.map(prune)
	////								.map(lowpass)
	////						.build();
	////
	////				Plan plan2 =
	////						plan(naming.stage())
	////								.map(plan(naming.pass())
	////										.input(input1)
	////										.map(group1)
	////										.map(classify1)
	////										.reduce(optimize1))
	////								.reduce(optimize3)
	////								.map(locators3)
	////						.build();
	////
	////				return new Parser.Executor<Locator.Node>(Parser.Executor.Progress.Builder.sysout(), plan1, plan2);
	//			}
	//		};
	//	}
	//	
	//	public Classifier images() {
	//		return images(registry);
	//	}
	//	
	//	public static Classifier images(Registry registry) {
	//		List<Sample> images = registry.get(Qualifier.Factory.Class.image);
	//		return new org.testobject.kernel.classification.classifiers.advanced.Image.Classifier(images);
	//	}

}
