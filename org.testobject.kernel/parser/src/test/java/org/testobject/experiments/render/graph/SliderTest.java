package org.testobject.experiments.render.graph;

import static org.testobject.kernel.imaging.procedural.Element.Builder.circle;
import static org.testobject.kernel.imaging.procedural.Style.Builder.fill;
import static org.testobject.kernel.imaging.procedural.Graph.Builder.graph;
import static org.testobject.kernel.imaging.procedural.Node.Builder.node;
import static org.testobject.kernel.imaging.procedural.Element.Builder.rect;
import static org.testobject.kernel.imaging.procedural.Element.Builder.text;
import static org.testobject.kernel.imaging.procedural.Layout.Builder.layout;
import static org.testobject.kernel.imaging.procedural.Style.Builder.rgb;
import static org.testobject.kernel.imaging.procedural.Style.Builder.rgba;
import static org.testobject.kernel.imaging.procedural.Transform.Builder.translate;
import static org.testobject.kernel.imaging.procedural.Style.Builder.style;

import java.io.IOException;

import org.junit.Test;
import org.testobject.commons.util.config.Debug;
import org.testobject.kernel.imaging.procedural.Color;
import org.testobject.kernel.imaging.procedural.Node;
import org.testobject.kernel.imaging.procedural.Printer;
import org.testobject.kernel.imaging.procedural.Graph.Builder;
import org.testobject.kernel.imaging.procedural.Graph;
import org.testobject.kernel.imaging.procedural.Element.Circle;
import org.testobject.kernel.imaging.procedural.Element.Rect;
import org.testobject.kernel.imaging.procedural.Element.Text;
import org.testobject.kernel.imaging.procedural.Util;
import org.testobject.commons.tools.plot.VisualizerUtil;

public class SliderTest {

    public static final boolean debug = Debug.toDebugMode(false);

	@Test
	public void drawSlider() throws IOException {

		// template
		Node node = slider(50).build();
		Printer.print(node);

		// instance
		render(node);

		if(debug) System.in.read();
	}
	
	@Test
	public void drawButton() throws IOException {

		// template
		Node node = button().build();
		Printer.print(node);

		// instance
		render(node);

        if(debug) System.in.read();
	}
	
	private Graph.Builder slider(double x) {
		Color gray = rgb(100, 100, 100);
		Color lightblue = rgb(0, 0, 200);
		Color alphablue = rgba(0, 0, 150, 120);
		Color blue = rgb(0, 0, 255);

		Rect bar = rect(300, 3, 3, 3, style(fill(gray)));
		Rect filled = rect(x, 3, 3, 3, style(fill(lightblue)));
		Circle bigcircle = circle(30, style(fill(alphablue)));
		Circle smallcircle = circle(6, style(fill(blue)));
		
		Graph.Builder template = graph();

		template
			.node(translate(100, 100))
				.element(bar)
				.element(filled)
				.child(node(translate(x, 2))
					.element(bigcircle)
					.element(smallcircle));
		
		return template;
	}
	
	private Graph.Builder button() {
		Color gray = rgb(100, 100, 100);

		Rect frame = rect(400, 100, 15, 15, style(fill(gray)));
		Text label = text("hallo", style(fill(gray)));
		
		Graph.Builder template = graph();

		template
			.node(translate(100, 100))
				.element(frame)
				.child(node()
					.element(label, layout().centered()));
		
		return template;
	}

	private static void render(Node node) {
        if(debug) VisualizerUtil.show("slider", Util.render(node));
	}
}
