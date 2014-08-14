package org.testobject.kernel.classification.matching;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;
import static org.testobject.kernel.classification.parser.Parser.Builder.classify;
import static org.testobject.kernel.classification.parser.Parser.Builder.group;
import static org.testobject.kernel.classification.parser.Parser.Builder.locators;
import static org.testobject.kernel.classification.parser.Parser.Builder.plan;
import static org.testobject.kernel.classification.parser.Parser.Builder.prune;
import static org.testobject.kernel.classification.parser.Parser.Builder.segment;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.tools.plot.VisualizerUtil;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.commons.util.io.FileUtil;
import org.testobject.kernel.api.classification.classifiers.Classifier;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.api.util.DescriptorUtil;
import org.testobject.kernel.api.util.LocatorUtil;
import org.testobject.kernel.classification.classifiers.advanced.MaskSegment;
import org.testobject.kernel.classification.matching.Resolver.CreateDescriptorId;
import org.testobject.kernel.classification.matching.Resolver.Resolution;
import org.testobject.kernel.classification.parser.Operations;
import org.testobject.kernel.classification.parser.Parser;
import org.testobject.kernel.classification.parser.Plan;
import org.testobject.kernel.classification.util.Find;
import org.testobject.kernel.classification.util.Find.Adapter;
import org.testobject.kernel.imaging.segmentation.Mask;

/**
 * 
 * @author enijkamp
 *
 */
public class ResolverTest {
	
	private static final boolean debug = Debug.toDebugMode(false);
	
	public static final Log log = LogFactory.getLog(ResolverTest.class);
	
	public static final double thresholdFail = 0.75d;
	public static final double thresholdFuzzy = 0.9d;
	
	@Test
	public void testFuzzy1() throws IOException {
		
		Image.Int raw1 = readImage("matching/framebuffers/wiki/record.raw.png");
		Image.Int raw2 = readImage("matching/framebuffers/wiki/replay.raw.png");
		
		Parser.Executor<Locator.Node> parser = parser();
		
		Locator.Node locators1 = parser.execute(raw1).get(0);
		Locator.Node locators2 = parser.execute(raw2).get(0);
		
		Point.Int lesen = Point.Int.from(200, 676);
		
		LinkedList<Locator.Descriptor> path = find(locators1, lesen.x, lesen.y);
		
		Resolver resolver = new Resolver(thresholdFail, thresholdFuzzy, false, new Matching(matchers(), raw1, raw1), raw1, raw2, createDescriptorId());
		Resolution resolution = resolver.resolve(locators2, Locator.Qualifier.Factory.create(path));
		
		show(resolution.qualifier, raw2);
		
		assertThat(resolution.probability, is(closeTo(0.63d, 1e-2)));
		assertThat(DescriptorUtil.getPosition(resolution.qualifier.getPath().getLast()).x, is(122));
		assertThat(DescriptorUtil.getPosition(resolution.qualifier.getPath().getLast()).y, is(586));
	}

	@Test
	public void testFuzzy2() throws IOException {
		
		Image.Int raw1 = readImage("matching/framebuffers/wiki/record.raw.png");
		Image.Int raw2 = readImage("matching/framebuffers/wiki/replay.raw.png");
		
		Parser.Executor<Locator.Node> parser = parser();
		
		Locator.Node locators1 = parser.execute(raw1).get(0);
		Locator.Node locators2 = parser.execute(raw2).get(0);
		
		Point.Int Sprache = Point.Int.from(80, 676);
		
		LinkedList<Locator.Descriptor> path = find(locators1, Sprache.x, Sprache.y);
		
		Resolver resolver = new Resolver(thresholdFail, thresholdFuzzy, false, new Matching(matchers(), raw1, raw1), raw1, raw2, createDescriptorId());
		Resolution resolution = resolver.resolve(locators2, Locator.Qualifier.Factory.create(path));
		
		show(resolution.qualifier, raw2);
		
		assertThat(resolution.probability, is(closeTo(0.71d, 1e-2)));
		assertThat(DescriptorUtil.getPosition(resolution.qualifier.getPath().getLast()).x, is(2));
		assertThat(DescriptorUtil.getPosition(resolution.qualifier.getPath().getLast()).y, is(704));
	}
	
	@Test
	public void testFuzzy3() throws IOException {
		
		Image.Int raw1 = readImage("matching/framebuffers/adac/spaeter_4_0_3.png");
		Image.Int raw2 = readImage("matching/framebuffers/adac/spaeter_2_3_3.png");
		
		Parser.Executor<Locator.Node> parser = parser();
		
		Locator.Node locators1 = parser.execute(raw1).get(0);
		Locator.Node locators2 = parser.execute(raw2).get(0);
		
		Point.Int spaeter = Point.Int.from(133, 521);
		
		LinkedList<Locator.Descriptor> path = find(locators1, spaeter.x, spaeter.y);
		
		Resolver resolver = new Resolver(thresholdFail, thresholdFuzzy, false, new Matching(matchers(), raw1, raw1), raw1, raw2, createDescriptorId());
		Resolution resolution = resolver.resolve(locators2, Locator.Qualifier.Factory.create(path));
		
		show(resolution.qualifier, raw2);
		
		assertThat(resolution.probability, is(closeTo(0.84d, 1e-2)));
		assertThat(DescriptorUtil.getPosition(resolution.qualifier.getPath().getLast()).x, is(101));
		assertThat(DescriptorUtil.getPosition(resolution.qualifier.getPath().getLast()).y, is(566));
	}
	
	
	@Test
	public void testFail1() throws IOException {
		
		Image.Int raw1 = readImage("matching/framebuffers/wiki/record.raw.png");
		Image.Int raw2 = readImage("matching/framebuffers/wiki/replay.raw.png");
		
		Parser.Executor<Locator.Node> parser = parser();
		
		Locator.Node locators1 = parser.execute(raw1).get(0);
		Locator.Node locators2 = parser.execute(raw2).get(0);
		
		Point.Int Wortherkunft = Point.Int.from(155, 280);
		
		LinkedList<Locator.Descriptor> path = find(locators1, Wortherkunft.x, Wortherkunft.y);
		
		Resolver resolver = new Resolver(thresholdFail, thresholdFuzzy, false, new Matching(matchers(), raw1, raw1), raw1, raw2, createDescriptorId());
		Resolution resolution = resolver.resolve(locators2, Locator.Qualifier.Factory.create(path));
		
		show(resolution.qualifier, raw2);
		
		assertThat(resolution.probability, is(closeTo(0.77d, 1e-2)));
	}
	
	@Test
	public void testResolveByTail() throws IOException {
		
		Image.Int raw1 = readImage("matching/framebuffers/adac/spaeter_4_0_3.png");
		Image.Int raw2 = readImage("matching/framebuffers/adac/spaeter_2_3_3.png");
		
		Parser.Executor<Locator.Node> parser = parser();
		
		Locator.Node locators1 = parser.execute(raw1).get(0);
		Locator.Node locators2 = parser.execute(raw2).get(0);
		
		Point.Int spaeter = Point.Int.from(133, 521);
		
		LinkedList<Locator.Descriptor> path = find(locators1, spaeter.x, spaeter.y);
		
		Resolver resolver = new Resolver(thresholdFail, thresholdFuzzy, false, new Matching(matchers(), raw1, raw1), raw1, raw2, createDescriptorId());
		Resolution resolution = resolver.resolve(locators2, Locator.Qualifier.Factory.create(path));
		
		assertThat(resolution.probability, is(closeTo(0.84d, 1e-2)));
	}
	
	@Test
	public void testResolveByScan() throws IOException {
		
		Image.Int raw1 = readImage("matching/framebuffers/adac/spaeter_4_0_3.png");
		Image.Int raw2 = readImage("matching/framebuffers/adac/spaeter_2_3_3.png");
		
		Parser.Executor<Locator.Node> parser = parser();
		
		Locator.Node locators1 = parser.execute(raw1).get(0);
		Locator.Node locators2 = parser.execute(raw2).get(0);
		
		Point.Int spaeter = Point.Int.from(133, 521);
		
		LinkedList<Locator.Descriptor> path = find(locators1, spaeter.x, spaeter.y);
		
		show(Locator.Qualifier.Factory.create(path), raw1);
		
		Resolver resolver = new Resolver(thresholdFail, thresholdFuzzy, false, new Matching(matchers(), raw1, raw1), raw1, raw2, createDescriptorId());
		Resolution resolution = resolver.resolve(locators2, Locator.Qualifier.Factory.create(path));
		
		show(resolution.qualifier, raw2);
		
		assertThat(resolution.probability, is(closeTo(0.84d, 1e-2)));
		assertThat(DescriptorUtil.getPosition(resolution.qualifier.getPath().getLast()).x, is(101));
		assertThat(DescriptorUtil.getPosition(resolution.qualifier.getPath().getLast()).y, is(566));
	}
	
	private CreateDescriptorId createDescriptorId() {
		return new CreateDescriptorId() {
			@Override
			public int createId() {
				return 0;
			}
		};
	}
	
	public static Parser.Executor<Locator.Node> parser() {
		Parser.Builder.Naming naming = Parser.Builder.Naming.create();

		Operations.Input input1 = segment(0.0f, 50000d, 30);
		Operations.Map<Node, Node> group1 = group();
		Operations.Map<Node, Node> prune = prune();
		Operations.Map<Node, Node> lowpass = classify(new MaskSegment.Classifier());
		Operations.Map<Node, Locator.Node> locators3 = locators();

		Plan plan =
				plan(naming.stage())
						.input(input1)
						.map(group1)
						.map(prune)
						.map(lowpass)
						.map(locators3)
				.build();

		return new Parser.Executor<Locator.Node>(Parser.Executor.Progress.Builder.sysout(), plan);
	}
	
	public Map<String, org.testobject.kernel.classification.matching.Matcher> matchers() {
		Map<String, Matcher> matchers = new HashMap<>();
		{
			matchers.put(Classifier.Qualifier.Factory.Class.node, new org.testobject.kernel.classification.classifiers.advanced.Node.Matcher());
			matchers.put(Classifier.Qualifier.Factory.Class.segment, new org.testobject.kernel.classification.classifiers.advanced.MaskSegment.Matcher());
		}
		return matchers;
	}

	private static org.testobject.commons.util.image.Image.Int readImage(String file) throws IOException {
		return ImageUtil.Read.read(FileUtil.readFileFromClassPath("android/4_0_3/" + file));
	}
	
	private static LinkedList<Locator.Descriptor> find(Locator.Node locators, int x, int y) {
		LinkedList<Locator.Node> path = new Find<Locator.Node>(createAdapter(locators)).at(x, y).path;
		return LocatorUtil.toDescriptors(path);
	}
	
	private static Adapter<Locator.Node> createAdapter(final Locator.Node node) {

		return new Adapter<Locator.Node>() {
			@Override
			public LinkedList<Locator.Node> at(Point.Int location) {

				LinkedList<Locator.Node> nodes = LocatorUtil.locate(node, location.x, location.y);

				log.debug("located '" + LocatorUtil.toPathString(nodes) + "' at position (" + location.x + ", " + location.y + ")");

				return nodes;
			}

			@Override
			public List<Locator.Node> childs(Locator.Node parent) {
				return parent.getChildren();
			}

			@Override
			public Rectangle.Double toBox(Locator.Node node) {
				return toDoubleRect(LocatorUtil.union(LocatorUtil.toRectangles(DescriptorUtil.getMasks(node.getDescriptor()))));
			}

			@Override
			public Mask toMask(Locator.Node node) {
				return Mask.Builder.create(DescriptorUtil.getMasks(node.getDescriptor()));
			}

			private Rectangle.Double toDoubleRect(Rectangle.Int box) {
				return new Rectangle.Double(box.x, box.y, box.w, box.h);
			}

			@Override
			public boolean isGroup(Locator.Node node) {
				return DescriptorUtil.getMasks(node.getDescriptor()).size() > 1;
			}
		};
	}
	
	private void show(Locator.Qualifier qualifier, Image.Int raw) {
		if(debug) {
			Mask mask = Mask.Builder.create(DescriptorUtil.getMasks(qualifier.getPath().getLast()));
			VisualizerUtil.show(ImageUtil.Cut.crop(raw, mask.getBoundingBox()));
		}
	}

}
