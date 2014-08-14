package org.testobject.kernel.classification.gui;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.testobject.commons.util.image.ImageUtil;
import org.testobject.kernel.api.classification.classifiers.Classifier.Qualifier;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.imaging.segmentation.BlobUtils;
import org.testobject.kernel.imaging.segmentation.Mask;

/**
 * 
 * @author enijkamp
 *
 */
public interface Draw {
	
	class Segments {
		
		public static BufferedImage draw(Node segments, int w, int h) {
			BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

			Color[] colors = BlobUtils.Draw.generateColors();
			draw(segments, colors, image);

			return image;
		}

		private static void draw(Node node, Color[] colors, BufferedImage image) {

			// draw
			for(Mask mask : node.getElement().getMasks()) {
				int position = mask.getBoundingBox().x + mask.getBoundingBox().w + mask.getBoundingBox().y + mask.getBoundingBox().h;
				Color color = colors[position % colors.length];
				for (int y = 0; y < mask.getBoundingBox().h; y++) {
					for (int x = 0; x < mask.getBoundingBox().w; x++) {
						if (mask.get(x, y)) {
							image.setRGB(mask.getBoundingBox().x + x, mask.getBoundingBox().y + y, color.getRGB());
						}
					}
				}
			}

			// recursion
			for (Node child : node.getChildren()) {
				draw(child, colors, image);
			}
		}
		
	}
	
	class Groups {
	
		public static BufferedImage draw(Node groups, int w, int h) {
			BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

			Color[] colors = BlobUtils.Draw.generateColors();
			draw(groups, colors, image);

			return image;
		}

		private static void draw(Node node, Color[] colors, BufferedImage image) {

			// draw
			if (Qualifier.Factory.Class.group.equals(node.getElement().getLabel().getQualifier().getType())) {

				for(Mask mask : node.getElement().getMasks()) {
					int position = mask.getBoundingBox().x + mask.getBoundingBox().w + mask.getBoundingBox().y + mask.getBoundingBox().h;
					Color color = colors[position % colors.length];
					for (int y = 0; y < mask.getBoundingBox().h; y++) {
						for (int x = 0; x < mask.getBoundingBox().w; x++) {
							if (mask.get(x, y)) {
								image.setRGB(mask.getBoundingBox().x + x, mask.getBoundingBox().y + y, color.getRGB());
							}
						}
					}
				}
			}

			// recursion
			for (Node child : node.getChildren()) {
				draw(child, colors, image);
			}
		}
	}
	
	class Synthetic {
		
		public static BufferedImage draw(org.testobject.kernel.imaging.procedural.Node graph) {
			return ImageUtil.Convert.toBufferedImage(org.testobject.kernel.imaging.procedural.Util.render(graph));
		}
	}
}