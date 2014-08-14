package org.testobject.kernel.replay.impl;

import static org.hamcrest.core.Is.is;
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
import org.junit.Ignore;
import org.junit.Test;
import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.commons.util.io.FileUtil;
import org.testobject.kernel.api.classification.classifiers.Classifier;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.api.events.output.Responses;
import org.testobject.kernel.api.util.DescriptorUtil;
import org.testobject.kernel.api.util.LocatorUtil;
import org.testobject.kernel.classification.classifiers.advanced.MaskSegment;
import org.testobject.kernel.classification.matching.Matcher;
import org.testobject.kernel.classification.matching.Resolver.CreateDescriptorId;
import org.testobject.kernel.classification.parser.Operations;
import org.testobject.kernel.classification.parser.Parser;
import org.testobject.kernel.classification.parser.Plan;
import org.testobject.kernel.classification.util.Find;
import org.testobject.kernel.classification.util.Find.Adapter;
import org.testobject.kernel.imaging.segmentation.Mask;
import org.testobject.kernel.replay.Replay;

/**
 * 
 * @author enijkamp
 *
 */
public class ResolveReplayMatcherTest {
	
	public static final Log log = LogFactory.getLog(ResolveReplayMatcherTest.class);
	
	@Test
	@Ignore
	public void failsBookmark() throws IOException {
		
		Image.Int raw1 = readImage("replay/android/apps_record.png");
		Image.Int raw2 = readImage("replay/android/apps_replay.png");
		
		Parser.Executor<Locator.Node> parser = parser();
		
		Locator.Node locators1 = parser.execute(raw1).get(0);
		Locator.Node locators2 = parser.execute(raw2).get(0);
		
		Point.Int bookmark = Point.Int.from(300, 420);
		
		LinkedList<Locator.Descriptor> path = find(locators1, bookmark.x, bookmark.y);
		
		Replay.Matcher matcher = new ResolveReplayMatcher(matchers(), raw1, raw1, locators1, locators2, false);
		
		Replay.Matcher.Level match = matcher.match(new Responses.Appears(Locator.Qualifier.Factory.create(path)), raw2, raw2, createRequestDescriptorId(0)).level;
		
		assertThat(match, is(Replay.Matcher.Level.FAILURE));
	}
	
	private CreateDescriptorId createRequestDescriptorId(final int seed) {
		return new CreateDescriptorId() {
			
			int id = seed;
			
			@Override
			public int createId() {
				return id++;
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

}
