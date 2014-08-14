package org.testobject.kernel.classification.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.testobject.commons.math.algebra.Size;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.kernel.api.classification.classifiers.Classifier.Lookup;
import org.testobject.kernel.api.classification.graph.Element;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.imaging.segmentation.BlobUtils;
import org.testobject.kernel.imaging.segmentation.Mask;

/**
 * 
 * @author enijkamp
 *
 */
public class MaskUtil {

	public static List<Mask> toMasks(Node node) {
		List<Mask> masks = Lists.newLinkedList();
		toMasks(node, masks);
		return masks;
	}

	public static void toMasks(Node node, List<Mask> masks) {
		masks.addAll(node.getElement().getMasks());
		for(Node child : node.getChildren()) {
			toMasks(child, masks);
		}
	}
	
	public static Image.Int draw(Mask mask, int rgb) {
		Size.Int size = mask.getSize();

		Image.Int image = new Image.Int(size.w, size.h, Image.Int.Type.ARGB);
		for (int y = 0; y < size.h; y++) {
			for (int x = 0; x < size.w; x++) {
				if (mask.get(x, y)) {
					image.pixels[y * image.w + x] = rgb;
				}
			}
		}
		
		return image;
	}
	
    public static List<Mask> flatten(Mask mask, Node root) {
    	
    	return flatten(Lists.toList(mask), Lookup.Factory.node(root));
	}
	
    public static List<Mask> flatten(List<Mask> masks, Lookup lookup) {
    	List<Mask> result = Lists.newArrayList(masks);
		for (Mask mask : masks) {
			result.addAll(flatten(lookup.childs(mask), lookup));
		}
		
		return result;
	}
    
    public static List<Mask> flatten(Node node) {
    	List<Mask> result = Lists.newLinkedList();
    	flatten(node, result);
		
		return result;
	}
	
	private static void flatten(Node node, List<Mask> result) {
		result.addAll(node.getElement().getMasks());
		for(Node child : node.getChildren()) {
			flatten(child, result);
		}
	}
	
	public static Image.Int cutByContour(Image.Int raw, Node nodes, Mask mask) {
		Mask union = Mask.Builder.create(flatten(mask, nodes));
		
		return BlobUtils.Cut.cutByMask(raw, union);
	}

	public static void saveMasks(Image.Int raw, Node nodes, File path) throws IOException {
		
		List<Element> elements = ElementUtil.flatten(nodes);
		
		for (Element element : elements) {
			String id = element.getLabel().getQualifier().toString();
			{
				// FIXME n masks (en)
				BufferedImage bufferedImageContour = ImageUtil.Convert.toBufferedImage(MaskUtil.cutByContour(raw, nodes, Mask.Builder.create(element.getMasks())));
				ImageIO.write(bufferedImageContour, "png", new File(path, id + ".contour.png"));
			}
			{
				BufferedImage bufferedImageMask = ImageUtil.Convert.toBufferedImage(BlobUtils.Cut.cutByMask(raw, Mask.Builder.create(element.getMasks())));
				ImageIO.write(bufferedImageMask, "png", new File(path, id + ".mask.png"));
			}
		}
	}

}
