package org.testobject.kernel.imaging.procedural;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;

/**
 * 
 * @author enijkamp
 *
 */
public class Util {

	public static Image.Int render(Node node) {
		
		Dimension.Double size = Renderer.Size.getSize(node);
		BufferedImage image = new BufferedImage((int) Math.ceil(size.w) + 1, (int) Math.ceil(size.h) + 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		{
			Renderer.render(graphics, node);
		}
		graphics.dispose();
		
		return ImageUtil.Convert.toImage(image);
	}
	
	public static void render(Node node, Point.Double offset, BufferedImage image) {
		
		// translate
		Node translate = Node.Builder.node(Transform.Builder.translate(offset.x, offset.y)).child(node).build();
		
		// render
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		{
			Renderer.render(graphics, translate);
		}
		graphics.dispose();
	}
	
}
