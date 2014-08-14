package org.testobject.kernel.imaging.procedural;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.testobject.kernel.imaging.procedural.Graph.Builder.graph;
import static org.testobject.kernel.imaging.procedural.Style.Builder.fill;
import static org.testobject.kernel.imaging.procedural.Style.Builder.rgb;
import static org.testobject.kernel.imaging.procedural.Style.Builder.style;
import static org.testobject.kernel.imaging.procedural.Transform.Builder.translate;

import org.junit.Test;
import org.testobject.kernel.imaging.procedural.Element.Rect;

/**
 * 
 * @author enijkamp
 *
 */
public class RendererTest {
	
	private static Graph.Builder rect(double translateX, double translateY, double w, double h) {
		Color blue = rgb(0, 0, 255);

		Rect bar = Element.Builder.rect(w, h, 0, 0, style(fill(blue)));
		
		Graph.Builder template = graph();

		template
			.node(translate(translateX, translateY))
				.element(bar);
				
		return template;
	}
	
	@Test
	public void size() {
		
		Node node = rect(0, 0, 400, 800).build();
		
		Dimension.Double size = Renderer.Size.getSize(node);
		
		assertThat(size.w, is(400d));
		assertThat(size.h, is(800d));
	}
	
	@Test
	public void sizeTranslate() {
		
		Node node = rect(50, 100, 400, 800).build();
		
		Dimension.Double size = Renderer.Size.getSize(node);
		
		assertThat(size.w, is(400d + 50d));
		assertThat(size.h, is(800d + 100d));
	}

}
