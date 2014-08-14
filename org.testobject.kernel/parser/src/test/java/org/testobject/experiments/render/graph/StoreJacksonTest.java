package org.testobject.experiments.render.graph;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.testobject.kernel.classification.parser.Parser.Builder.cache;
import static org.testobject.kernel.classification.parser.Parser.Builder.classify;
import static org.testobject.kernel.classification.parser.Parser.Builder.group;
import static org.testobject.kernel.classification.parser.Parser.Builder.locators;
import static org.testobject.kernel.classification.parser.Parser.Builder.lowpass;
import static org.testobject.kernel.classification.parser.Parser.Builder.optimize;
import static org.testobject.kernel.classification.parser.Parser.Builder.plan;
import static org.testobject.kernel.classification.parser.Parser.Builder.prune;
import static org.testobject.kernel.classification.parser.Parser.Builder.segment;
import static org.testobject.kernel.classification.parser.Parser.Builder.Breeding.loosers;
import static org.testobject.kernel.classification.parser.Parser.Builder.Contestants.flat;
import static org.testobject.kernel.classification.parser.Parser.Builder.Contestants.hierarchical;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.junit.After;
import org.junit.Test;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.tools.plot.VisualizerUtil;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.commons.util.io.FileUtil;
import org.testobject.kernel.api.classification.classifiers.Classifier;
import org.testobject.kernel.api.classification.classifiers.Classifier.Lookups;
import org.testobject.kernel.api.classification.graph.Contour;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.api.classification.graph.Variable;
import org.testobject.kernel.api.classification.graph.Locator.Descriptor;
import org.testobject.kernel.api.util.VariableUtil;
import org.testobject.kernel.classification.classifiers.advanced.MaskSegment;
import org.testobject.kernel.classification.graph.Store;
import org.testobject.kernel.classification.matching.Matching;
import org.testobject.kernel.classification.parser.Operations;
import org.testobject.kernel.classification.parser.Parser;
import org.testobject.kernel.classification.parser.Plan;
import org.testobject.kernel.classification.util.MaskUtil;
import org.testobject.kernel.imaging.segmentation.BlobUtils;
import org.testobject.kernel.imaging.segmentation.Mask;

/**
 * 
 * @author enijkamp
 *
 */
//public class StoreJacksonTest {
//	
//	private static final String input = "android/4_0_3/replay/twitter/3.png";
//	private static final File output = new File(System.getProperty("java.io.tmpdir"), "test.json");
//
//	@After
//	public void cleanUp() {
////		if(output.exists()) {
////			output.delete();
////		}
//	}
//
//	@Test
//	public void testSerialization() throws IOException {
//
//		// input
//		Image.Int raw = ImageUtil.Convert.toImage(ImageIO.read(FileUtil.readFileFromClassPath(input)));
//		Locator.Node inputLocatorsTree = parse(raw);
//		
//		// put
//		Store.Serializer.Put put = new Store.Serializer.Put() {
//			@Override
//			public void put(Descriptor descriptor, List<Image.Int> masks) {
//				int id = 0;
//				for(Image.Int image : masks) {
//					try {
//						ImageIO.write(ImageUtil.Convert.toBufferedImage(image), "png", new FileOutputStream(new File(System.getProperty("java.io.tmpdir"), descriptor.getId() + "." + id++ + ".png")));
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		};
//		
//		// get
//		Store.Deserializer.Get get = new Store.Deserializer.Get() {
//			@Override
//			public Image.Int get(int descriptor, int maskId) {
//				
//				File file = new File(System.getProperty("java.io.tmpdir"), descriptor + "." + maskId + ".png");
//				BufferedImage image;
//				try {
//					image = ImageIO.read(new FileInputStream(file));
//					return ImageUtil.Convert.toImage(image);
//				} catch (IOException e) {
//					throw new RuntimeException(e);
//				}
//			}
//		};
//		
//		// serialize
//		Store.Serializer serializer = Store.Serializer.Factory.create(Lists.toList(new org.testobject.kernel.classification.classifiers.advanced.Node.Compression.Zip(), new org.testobject.kernel.classification.classifiers.advanced.MaskSegment.Compression.Zip()));
//		serializer.serialize(raw, inputLocatorsTree, new FileOutputStream(output), put);
//		
//		// deserialize
//		Store.Deserializer deserializer = Store.Deserializer.Factory.create(Lists.toList(new org.testobject.kernel.classification.classifiers.advanced.Node.Compression.Unzip(), new org.testobject.kernel.classification.classifiers.advanced.MaskSegment.Compression.Unzip(Lookups.Contour.Factory.create())));
//		Locator.Node outputLocatorsTree = deserializer.deserialize(get, new FileInputStream(output), Classifier.Images.Factory.create(raw));
//		
//		// maps
//		Map<Integer, Locator.Node> inputLocatorMap = toMap(toSet(inputLocatorsTree));
//		Map<Integer, Locator.Node> outputLocatorMap = toMap(toSet(outputLocatorsTree));
//			
//		// locators
//		{
//			assertThat(inputLocatorMap.size(), is(outputLocatorMap.size()));
//			for(Locator.Node input : inputLocatorMap.values()) {
//				Locator.Node output = outputLocatorMap.get(input.getDescriptor().getId());
//				
//				assertThat(input.getChildren().size(), is(output.getChildren().size()));
//			}
//		}
//		
//		// variables
//		{
//			for(Locator.Node input : inputLocatorMap.values()) {
//				Locator.Node output = outputLocatorMap.get(input.getDescriptor().getId());
//				List<Variable<?>> inputVariables = input.getDescriptor().getFeatures();
//				List<Variable<?>> outputVariables = output.getDescriptor().getFeatures();
//				
//				for(Variable<?> inputVariable : inputVariables) {
//					assertThat(output.getDescriptor().getLabel() + " " + inputVariable.getName(), VariableUtil.has(outputVariables, inputVariable.getName()), is(true));
//				}
//				
//				assertThat(output.getDescriptor().getLabel().toString(), inputVariables.size(), is(outputVariables.size()));
//			}
//		}
//
//		// masks
//		{
//			for(Locator.Node input : inputLocatorMap.values()) {
//				Locator.Node output = outputLocatorMap.get(input.getDescriptor().getId());
//			
//				List<Mask> inputMasks = VariableUtil.getMasks(input.getDescriptor().getFeatures());
//				List<Mask> outputMasks = VariableUtil.getMasks(output.getDescriptor().getFeatures());
//				
//				assertThat(inputMasks.size(), is(outputMasks.size()));
//				
//				for(int i = 0; i < inputMasks.size(); i++) {
//				
//					Mask inputMask = inputMasks.get(i);
//					Mask outputMask = outputMasks.get(i);
//
//					Rectangle.Int inputBox = inputMask.getBoundingBox();
//					Rectangle.Int outputBox = outputMask.getBoundingBox();
//					assertThat(inputBox, is(outputBox));
//					
//					for (int y = 0; y < inputBox.h; y++) {
//						for (int x = 0; x < inputBox.w; x++) {
//							
//							if(inputMask.get(x, y) != outputMask.get(x, y)) {
//								VisualizerUtil.show("in", MaskUtil.draw(inputMask, Color.red.getRGB()));
//								VisualizerUtil.show("in (raw)", BlobUtils.Cut.cutByMask(raw, inputMask));
//								VisualizerUtil.show("out", MaskUtil.draw(outputMask, Color.red.getRGB()));
//								System.out.println();
//							}
//							
//							assertThat(input.getDescriptor().getLabel().toString(), inputMask.get(x, y), is(outputMask.get(x, y)));
//						}
//					}
//				}
//			}
//		}
//		
//		// contours
//		{
//			assertThat(inputLocatorMap.size(), is(outputLocatorMap.size()));
//			for(Locator.Node input : inputLocatorMap.values()) {
//				Locator.Node output = outputLocatorMap.get(input.getDescriptor().getId());
//				
//				if(VariableUtil.has(input.getDescriptor().getFeatures(), Variable.Names.Depiction.contours)) {
//				
//					List<Contour> inputContours = VariableUtil.getContours(input.getDescriptor());
//					List<Contour> outputContours = VariableUtil.getContours(output.getDescriptor());
//					
//					assertThat(inputContours.size(), is(outputContours.size()));
//				}
//			}
//		}
//		
//		// matching
//		{
//			Map<String, org.testobject.kernel.classification.matching.Matcher> matchers = new HashMap<>();
//			{
//				matchers.put(Classifier.Qualifier.Factory.Class.node, new org.testobject.kernel.classification.classifiers.advanced.Node.Matcher());
//				matchers.put(Classifier.Qualifier.Factory.Class.segment, new org.testobject.kernel.classification.classifiers.advanced.MaskSegment.Matcher());
//			}
//			Matching matching = new Matching(matchers, raw, raw);
//			
//			for(Locator.Node input : inputLocatorMap.values()) {
//				Locator.Node output = outputLocatorMap.get(input.getDescriptor().getId());
//				
//				assertThat(matching.match(input.getDescriptor(), output.getDescriptor()), is(1d));
//			}
//		}
//	}
//
//	private Map<Integer, Locator.Node> toMap(Set<Locator.Node> locators) {
//		Map<Integer, Locator.Node> map = new HashMap<>();
//		for(Locator.Node locator : locators) {
//			map.put(locator.getDescriptor().getId(), locator);
//		}
//		
//		return map;
//	}
//	
//	private static Set<Locator.Node> toSet(Locator.Node node) {
//		Set<Locator.Node> nodes = new HashSet<>();
//		nodes.add(node);
//		for (Locator.Node child : node.getChildren()) {
//			nodes.addAll(toSet(child));
//		}
//
//		return nodes;
//	}
//
//	private static org.testobject.kernel.classification.graph.Locator.Node parse(Image.Int raw) {
//		Parser.Builder.Naming naming = Parser.Builder.Naming.create();
//
//		Operations.Map<Node, Node> lowpass = cache(classify(new MaskSegment.Classifier()));
//		Operations.Map<Node, Node> prune = prune();
//
//		Operations.Input input1 = cache(segment(0.0f, 50000d));
//		Operations.Map<Node, Node> group1 = cache(group());
//		Operations.Map<Node, Node> classify1 = classify(new MaskSegment.Classifier());
//		Operations.Reduce<Node, Node> optimize1 = optimize(group1, flat);
//
//		Operations.Reduce<Node, Node> optimize3 = optimize(lowpass(loosers), lowpass, flat);
//		Operations.Map<Node, org.testobject.kernel.classification.graph.Locator.Node> locators3 = locators();
//
//		Plan plan1 =
//				plan(naming.stage())
//						.caches(lowpass, input1, group1)
//						.input(input1)
//						.map(group1)
//						.map(prune)
//						.map(lowpass)
//				.build();
//
//		Plan plan2 =
//				plan(naming.stage())
//						.map(plan(naming.pass())
//								.input(input1)
//								.map(group1)
//								.map(classify1)
//								.reduce(optimize1))
//						.reduce(optimize3)
//						.map(locators3)
//				.build();
//
//		return new Parser.Executor<Locator.Node>(Parser.Executor.Progress.Builder.sysout(), plan1, plan2).execute(raw).get(0);
//	}
//}