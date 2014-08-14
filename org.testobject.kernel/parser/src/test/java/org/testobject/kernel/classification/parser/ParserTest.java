package org.testobject.kernel.classification.parser;

import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.testobject.kernel.classification.parser.Parser.Builder.cache;
import static org.testobject.kernel.classification.parser.Parser.Builder.classify;
import static org.testobject.kernel.classification.parser.Parser.Builder.group;
import static org.testobject.kernel.classification.parser.Parser.Builder.locators;
import static org.testobject.kernel.classification.parser.Parser.Builder.lowpass;
import static org.testobject.kernel.classification.parser.Parser.Builder.optimize;
import static org.testobject.kernel.classification.parser.Parser.Builder.plan;
import static org.testobject.kernel.classification.parser.Parser.Builder.segment;
import static org.testobject.kernel.classification.parser.Parser.Builder.Breeding.loosers;
import static org.testobject.kernel.classification.parser.Parser.Builder.Breeding.winners;
import static org.testobject.kernel.classification.parser.Parser.Builder.Contestants.flat;
import static org.testobject.kernel.classification.parser.Parser.Builder.Contestants.hierarchical;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.debug.StopWatch;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.commons.util.io.FileUtil;
import org.testobject.kernel.api.classification.classifiers.Classifier;
import org.testobject.kernel.api.classification.classifiers.Classifier.Qualifier;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.api.classification.graph.Variable;
import org.testobject.kernel.classification.classifiers.advanced.Icon;
import org.testobject.kernel.classification.classifiers.advanced.Registry;
import org.testobject.kernel.classification.classifiers.advanced.MaskSegment;
import org.testobject.kernel.classification.classifiers.advanced.Registry.Sample;
import org.testobject.kernel.classification.parser.Cache.IsCache;
import org.testobject.kernel.classification.parser.Operations.Input;
import org.testobject.kernel.classification.parser.Operations.Map;
import org.testobject.kernel.classification.parser.Operations.Operation;
import org.testobject.kernel.classification.parser.Parser.Builder.Naming;

/**
 * 
 * @author enijkamp
 *
 */
public class ParserTest {
	
	@Test
	public void testPlan() {
		
		Naming naming = Naming.create();
		
		Input input1 = segment(0.5f, 4500d);
		Input input2 = segment(0.0f, 50000d);
		
		Map<Node, Node> group1 = cache(group());
		
		Plan plan = 
				plan(naming.stage())
					.caches(group1)
					.map(plan(naming.pass())
							.input(input2)
							.map(group())
							.map(classify(Komoot.image()))
							.reduce(optimize(group1, flat)),
						 plan(naming.pass())
							.input(input1)
							.map(group())
							.map(classify(Komoot.icon()))
							.reduce(optimize(group1, flat)))
					.reduce(optimize(group1, hierarchical))
					.map(locators())
				.build();
		
		assertThat(plan.getCaches().size(), is(1));
		assertThat(plan.getOperations().size(), is(3));
	}
	
	@Test
	public void testExecutorKomoot() throws IOException {
		
		Image.Int raw = ImageUtil.Read.read(FileUtil.readFileFromClassPath(toImagePathKomoot("framebuffer/menu.png")));
		
		print("parsing");
		List<Locator.Node> locators = Komoot.createParser().execute(raw);
		{
			assertThat(locators.size(), is(1));
		}

		System.out.println();
		
		print("result");
		print(locators.get(0));
		{
			String[][] childs = {
					{ Classifier.Qualifier.Factory.Class.image, "tour" },
					{ Classifier.Qualifier.Factory.Class.image, "region" },
					{ Classifier.Qualifier.Factory.Class.icon, "map" }
			};
			
			Locator.Node root = locators.get(0);
			assertThat(root.getDescriptor().getLabel().getType(), is(Classifier.Qualifier.Factory.Class.node));
			assertThat(root.getChildren().size(), is(childs.length));
			{
				for(String[] child : childs) {
					boolean found = false;
					for(Locator.Node locator : root.getChildren()) {
						Qualifier type = locator.getDescriptor().getLabel();
						if(child[0].equals(type.getType()) && child[1].equals(type.getId())) {
							found = true;
						}
					}
					assertThat(child[0] + "." +  child[1] + " missing", found, is(true));
				}
			}
		}
	}

	@Test
	public void testExecutorKomootIntermediate() throws IOException {
		
		Image.Int raw = ImageUtil.Read.read(FileUtil.readFileFromClassPath(toImagePathKomoot("framebuffer/menu.png")));
		
		Naming naming = Naming.create();
		
		final Operations.Map<Node, Node> lowpass = cache(classify(segments()));
		
		final Input input1 = cache(segment(0.0f, 50000d));
		final Input input2 = segment(0.5f, 4500d);
		
		final Map<Node, Node> group1 = cache(group());
		final Classification classify1 = classify(Komoot.image());		
		final Optimization optimize1 = optimize(group1, flat);
		
		final Map<Node, Node> group2 = group();
		final Classification classify2 = classify(Komoot.icon());
		final Optimization optimize2 = optimize(group1, flat);
		
		final Optimization optimize3 = optimize(lowpass(winners), lowpass, hierarchical);
		final Locators locators3 = locators();
		
		Plan plan1 =
				plan(naming.stage())
					.caches(lowpass, input1, group1)
					.input(input1)
					.map(group1)
					.map(prune())
					.map(lowpass)
				.build();
		
		Plan plan2 =
				plan(naming.stage())
					.map(plan(naming.pass())
							.input(input1)
							.map(group1)
							.map(classify1)
							.reduce(optimize1),
						 plan(naming.pass())
							.input(input2)
							.map(group2)
							.map(classify2)
							.reduce(optimize2))
					.reduce(optimize3)
					.map(locators3)
				.build();
		
		Parser.Executor.Progress progress = new Parser.Executor.Progress() {
			private int indentPlan = 0;

			@Override
			public void begin(List<IsCache<?>> caches) {
				System.out.println(spaces(indentPlan) + "caches(" + caches.size() + ")");
			}

			@Override
			public void end(List<IsCache<?>> caches) {
			}

			@Override
			public void begin(Plan plan) {
				System.out.println(spaces(indentPlan) + "plan(" + plan.getQualifier() + ")");
				indentPlan += 2;
			}

			@Override
			public void end(Plan plan, List<Object> result) {
				indentPlan -= 2;
			}

			@Override
			public void begin(Operation operation) {
				System.out.println(spaces(indentPlan) + operation.getClass().getSimpleName().toLowerCase() + "()");
				if(operation instanceof Parallel) {
					indentPlan += 2;
				}
			}

			@Override
			public void end(Operation operation, List<Object> result) {
				if(operation instanceof Parallel) {
					indentPlan -= 2;
				}
				
				// assert
				{
					if(operation == group1) {
						List<org.testobject.kernel.api.classification.graph.Node> groups = Lists.castList(result);
						assertThat(count(groups), is(107));
					}
					
					if(operation == group2) {
						List<org.testobject.kernel.api.classification.graph.Node> groups = Lists.castList(result);
						assertThat(count(groups), is(179));
					}
					
					if(operation == classify1) {
						List<org.testobject.kernel.api.classification.graph.Node> groups = Lists.castList(result);
						assertThat(count(groups), is(107));
					}
					
					if(operation == classify2) {
						List<org.testobject.kernel.api.classification.graph.Node> groups = Lists.castList(result);
						assertThat(count(groups), is(179));
					}
					
					if(operation == optimize1) {
						List<org.testobject.kernel.api.classification.graph.Node> groups = Lists.castList(result);
						assertThat(count(groups), is(3));
					}
					
					if(operation == optimize2) {
						List<org.testobject.kernel.api.classification.graph.Node> groups = Lists.castList(result);
						assertThat(count(groups), is(4));
					}
					
					if(operation == optimize3) {
						List<org.testobject.kernel.api.classification.graph.Node> groups = Lists.castList(result);
						assertThat(count(groups), is(4));
					}
				}
			}
			
			private int count(List<Node> nodes) {
				int count = 0;
				for(Node node : nodes) {
					count++;
					count += count(node.getChildren());
				}
				return count;
			}

			private String spaces(int indent) {
				StringBuilder builder = new StringBuilder();
				for(int i = 0; i < indent; i++) {
					builder.append(" ");
				}
				return builder.toString();
			}
		};
		
		print("parsing");
		List<Locator.Node> locators = new Parser.Executor<Locator.Node>(progress, plan1, plan2).execute(raw);
		{
			assertThat(locators.size(), is(1));
		}

		System.out.println();
		
		print("result");
		print(locators.get(0));
		{
			String[][] childs = {
					{ Classifier.Qualifier.Factory.Class.image, "tour" },
					{ Classifier.Qualifier.Factory.Class.image, "region" },
					{ Classifier.Qualifier.Factory.Class.icon, "map" }
			};
			
			Locator.Node root = locators.get(0);
			assertThat(root.getDescriptor().getLabel().getType(), is(Classifier.Qualifier.Factory.Class.node));
			assertThat(root.getChildren().size(), is(childs.length));
			{
				for(String[] child : childs) {
					boolean found = false;
					for(Locator.Node locator : root.getChildren()) {
						Qualifier type = locator.getDescriptor().getLabel();
						if(child[0].equals(type.getType()) && child[1].equals(type.getId())) {
							found = true;
						}
					}
					assertThat(child[0] + "." +  child[1] + " missing", found, is(true));
				}
			}
		}
	}
	
	@Test
	public void testExecutorTwitter() throws IOException {

		Image.Int raw = ImageUtil.Read.read(FileUtil.readFileFromClassPath(toImagePathTwitter("framebuffer/dialog.png")));
		
		print("parsing");
		List<Locator.Node> locators = Twitter.createParser().execute(raw);
		{
			assertThat(locators.size(), is(1));
		}

		System.out.println();
		
		print("result");
		print(locators.get(0));
		{
			String[][] childs = {
					{ Classifier.Qualifier.Factory.Class.image, "cancel" },
			};
			
			Locator.Node root = locators.get(0);
			assertThat(root.getDescriptor().getLabel().getType(), is(Classifier.Qualifier.Factory.Class.node));
			assertThat(root.getChildren().size(), is(11));
			{
				for(String[] child : childs) {
					assertThat(child[0] + "." +  child[1] + " missing", contains(root, child[0], child[1]), is(true));
				}
			}
		}
	}
	
	private static boolean contains(Locator.Node node, String type, String id) {
		for(Locator.Node locator : node.getChildren()) {
			Qualifier qualifier = locator.getDescriptor().getLabel();
			if(type.equals(qualifier.getType()) && id.equals(qualifier.getId())) {
				return true;
			}
			
			if(contains(locator, type, id)) {
				return true;
			}
		}
		
		return false;
	}
	
	@Test
	public void testExecutorPerformance() throws IOException {

		Image.Int raw = ImageUtil.Read.read(FileUtil.readFileFromClassPath(toImagePathTwitter("framebuffer/dialog.png")));
		
		StopWatch watch = new StopWatch(System.out);
		
		watch.start("parsing");
		{
			Twitter.createParser().execute(raw);
		}
		long ms =  watch.stop();
		
		assertThat("parsing took more than " + ms + " ms", ms, lessThan(15000l));
	}
	
	private static Classifier segments() {
		return new MaskSegment.Classifier();
	}
	
	public static Map<Node, Node> prune() {
		return new Pruning.LowPass();
	}
	
	private static void print(Locator.Node node) {
		Locator.Printer.print(node, 1, System.out, Variable.Names.Geometric.position, Variable.Names.Geometric.size);
	}
	
	public static Classifier images(Registry registry) {
		List<Sample> images = registry.get(Qualifier.Factory.Class.image);
		return new org.testobject.kernel.classification.classifiers.advanced.Image.Classifier(images);
	}
	
	private static void print(String string) {
		System.out.println(">>> " + string + " <<<");
		System.out.println();
	}
	
	private static String toImagePathTwitter(String file) {
		return "android/4_0_3/test/twitter/" + file;
	}
	
	private static class Twitter {
		
		private static final String[][] images = {
			{ "write.contour.png", "write" },
			{ "cancel.contour.png", "cancel" },
			{ "popup-empty.contour.png", "popup-empty" },
			{ "popup-hello.contour.png", "popup-hello" },
			{ "discard.contour.png", "discard" } };
		
		public static Classifier image() {
			org.testobject.kernel.classification.classifiers.advanced.Image.Trainer trainer = new org.testobject.kernel.classification.classifiers.advanced.Image.Trainer();
			
			List<String> names = Lists.newArrayList(images.length);
			String[] files = new String[images.length];
			
			for(int i = 0; i < images.length; i++) {
				names.add(images[i][1]);
				files[i] = images[i][0];
			}
			
			Classifier classifier = trainer.train(names, readImages(files));
			
			return classifier;
		}
		
		public static Parser.Executor<Locator.Node> createParser() {
			Parser.Builder.Naming naming = Parser.Builder.Naming.create();

			Operations.Map<Node, Node> lowpass = cache(classify(new MaskSegment.Classifier()));
			Operations.Map<Node, Node> prune = prune();

			Operations.Input input1 = cache(segment(0.0f, 50000d));
			Operations.Map<Node, Node> group1 = cache(group());
			Operations.Map<Node, Node> classify1 = classify(Twitter.image());
			Operations.Reduce<Node, Node> optimize1 = optimize(group1, flat);

			Operations.Reduce<Node, Node> optimize3 = optimize(lowpass(loosers), lowpass, hierarchical);
			Operations.Map<Node, org.testobject.kernel.api.classification.graph.Locator.Node> locators3 = locators();

			Plan plan1 =
					plan(naming.stage())
							.caches(lowpass, input1, group1)
							.input(input1)
							.map(group1)
							.map(prune)
							.map(lowpass)
					.build();

			Plan plan2 =
					plan(naming.stage())
							.map(plan(naming.pass())
									.input(input1)
									.map(group1)
									.map(classify1)
									.reduce(optimize1))
							.reduce(optimize3)
							.map(locators3)
					.build();

			return new Parser.Executor<Locator.Node>(Parser.Executor.Progress.Builder.sysout(), plan1, plan2);
		}
		
	    private static List<Image.Int> readImages(String ... files) {
			try {
				List<Image.Int> images = Lists.newArrayList(files.length);
				for(String file : files) {
					images.add(ImageUtil.Read.read(FileUtil.readFileFromClassPath(toImagePath(file))));
				}
				return images;
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
	    
	    private static String toImagePath(String file) {
	    	return toImagePathTwitter("train/images/" + file);
	    }
	}
	
	private static String toImagePathKomoot(String file) {
		return "android/4_0_3/test/komoot/" + file;
	}
	
	private static class Komoot {
	
		public static Classifier image() {
			org.testobject.kernel.classification.classifiers.advanced.Image.Trainer trainer = new org.testobject.kernel.classification.classifiers.advanced.Image.Trainer();
			Classifier classifier = trainer.train(Lists.toList("region", "tour"), readImages("regions.png", "tour.png"));
			return classifier;
		}
	
		public static Classifier icon() {
			Icon.Trainer trainer = new Icon.Trainer();
			Classifier classifier = trainer.train(Lists.toList("map"), readImages("map.png"));
			return classifier;
		}
		
		public static Parser.Executor<Locator.Node> createParser() {
			Naming naming = Naming.create();
			
			Operations.Map<Node, Node> lowpass = cache(classify(segments()));
			
			Input input1 = cache(segment(0.0f, 50000d));
			Input input2 = segment(0.5f, 4500d);
			
			Map<Node, Node> group1 = cache(group());
			
			Plan plan1 =
					plan(naming.stage())
						.caches(lowpass, input1, group1)
						.input(input1)
						.map(group1)
						.map(prune())
						.map(lowpass)
					.build();
			
			Plan plan2 =
					plan(naming.stage())
						.map(plan(naming.pass())
								.input(input1)
								.map(group1)
								.map(classify(Komoot.image()))
								.reduce(optimize(group1, flat)),
							 plan(naming.pass())
								.input(input2)
								.map(group())
								.map(classify(Komoot.icon()))
								.reduce(optimize(group1, flat)))
						.reduce(optimize(lowpass(winners), lowpass, hierarchical))
						.map(locators())
					.build();
			
			return new Parser.Executor<Locator.Node>(Parser.Executor.Progress.Builder.sysout(), plan1, plan2);
		}
		
	    private static List<Image.Int> readImages(String ... files) {
			try {
				List<Image.Int> images = Lists.newArrayList(files.length);
				for(String file : files) {
					images.add(ImageUtil.Read.read(FileUtil.readFileFromClassPath(toImagePath(file))));
				}
				return images;
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
	    
	    private static String toImagePath(String file) {
	    	return toImagePathKomoot("train/images/" + file);
	    }
	}
}
