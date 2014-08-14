package org.testobject.kernel.ocr;

import static org.testobject.kernel.classification.parser.Parser.Builder.classify;
import static org.testobject.kernel.classification.parser.Parser.Builder.group;
import static org.testobject.kernel.classification.parser.Parser.Builder.locators;
import static org.testobject.kernel.classification.parser.Parser.Builder.plan;
import static org.testobject.kernel.classification.parser.Parser.Builder.prune;
import static org.testobject.kernel.classification.parser.Parser.Builder.segment;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.math.algebra.Size;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.classification.graph.Locator.Descriptor;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.api.util.VariableUtil;
import org.testobject.kernel.classification.classifiers.advanced.MaskSegment;
import org.testobject.kernel.classification.parser.Operations;
import org.testobject.kernel.classification.parser.Parser;
import org.testobject.kernel.classification.parser.Plan;
import org.testobject.kernel.ocr.tesseract.OptimizedTesseractOCR;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 
 * @author enijkamp
 *
 */
public class RandomLayoutTest {
	
	public static void main(String[] args) {
		final BufferedImage image = readImageApps();
		final OCR ocr = createOcr();
		final Parser.Executor<Locator.Node> parser = createParser();
		
		List<Locator.Node> nodes = parser.execute(ImageUtil.Convert.toImageInt(image));
		List<Locator.Node> childs = nodes.get(0).getChildren();
		List<TextGroup> texts = ocr(ocr, image, childs, 1);
		
		Map<Integer, List<TextGroup>> hierarchy = Maps.newHashMap();
		int max = 0;
		for(TextGroup text : texts) {
			if(hierarchy.containsKey(text.level) == false) {
				hierarchy.put(text.level, Lists.<TextGroup>newArrayList());
			}
			hierarchy.get(text.level).add(text);
			max = Math.max(max, text.level);
		}
		
		for(int level = 0; level < max; level++) {
			if(hierarchy.containsKey(level)) {
				System.out.println(level);
				List<TextGroup> groups = hierarchy.get(level);
				for(TextGroup group : groups) {
					for(OCR.Result text : group.texts) {
						System.out.println("\t" + text);
					}
				}
			}
		}
		
	}
	
	private static class TextGroup {
		public final List<OCR.Result> texts;
		public final int level;

		public TextGroup(List<OCR.Result> texts, int level) {
			this.texts = texts;
			this.level = level;
		}
		
		@Override
		public String toString() {
			String string = "level: " + level + "\t";
			for(OCR.Result text : texts) {
				string += "\n\t" + text;
			}
			
			return string;
		}
	}
	
	private static List<TextGroup> ocr(OCR ocr, BufferedImage image, List<Locator.Node> nodes, int level) {
		
		List<TextGroup> result = Lists.newArrayList();
		for(Locator.Node node : nodes) {
			
			// ocr
			Rectangle.Int box = toBox(node.getDescriptor());
			List<OCR.Result> texts = ocr.getText(image, box, 92, 1);
			result.add(new TextGroup(texts, level));
			
			// go on
			result.addAll(ocr(ocr, image, node.getChildren(), level + 1));
		}
		
		return result;
	}

	private static Rectangle.Int toBox(Descriptor descriptor) {
		
		Point.Int position = VariableUtil.getPosition(descriptor.getFeatures());
		Size.Int size = VariableUtil.getSize(descriptor.getFeatures());
		
		return new Rectangle.Int(position, size);
	}

	private static BufferedImage readImageApps() {
		try(InputStream stream = SegFaultOptimizedTest.class.getResourceAsStream("amazon.png")) {
			return ImageIO.read(stream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static OCR createOcr() {
		return new OptimizedTesseractOCR();
	}
	
	public static Parser.Executor<Locator.Node> createParser() {
		Parser.Builder.Naming naming = Parser.Builder.Naming.create();

		Operations.Input input = segment(0.0f, 50000d, 30);
		Operations.Map<Node, Node> group = group();
		Operations.Map<Node, Node> prune = prune();
		Operations.Map<Node, Node> lowpass = classify(new MaskSegment.Classifier());
		Operations.Map<Node, Locator.Node> locators = locators();

		Plan plan =
				plan(naming.stage())
						.input(input)
						.map(group)
						.map(prune)
						.map(lowpass)
						.map(locators)
				.build();

		return new Parser.Executor<Locator.Node>(Parser.Executor.Progress.Builder.sysout(), plan);
	}

}
