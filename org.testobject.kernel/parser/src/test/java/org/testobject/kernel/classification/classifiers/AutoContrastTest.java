package org.testobject.kernel.classification.classifiers;

import static org.testobject.kernel.imaging.procedural.Element.Builder.circle;
import static org.testobject.kernel.imaging.procedural.Element.Builder.rect;
import static org.testobject.kernel.imaging.procedural.Graph.Builder.graph;
import static org.testobject.kernel.imaging.procedural.Node.Builder.node;
import static org.testobject.kernel.imaging.procedural.Style.Builder.fill;
import static org.testobject.kernel.imaging.procedural.Style.Builder.rgb;
import static org.testobject.kernel.imaging.procedural.Style.Builder.rgba;
import static org.testobject.kernel.imaging.procedural.Style.Builder.style;
import static org.testobject.kernel.imaging.procedural.Transform.Builder.translate;

import java.awt.Dimension;
import java.io.IOException;

import org.junit.Test;
import org.testobject.commons.tools.plot.VisualizerUtil;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.commons.util.io.FileUtil;
import org.testobject.kernel.imaging.color.contrast.Contrast;
import org.testobject.kernel.imaging.color.contrast.Quantize;
import org.testobject.kernel.imaging.procedural.Color;
import org.testobject.kernel.imaging.procedural.Element.Circle;
import org.testobject.kernel.imaging.procedural.Element.Rect;
import org.testobject.kernel.imaging.procedural.Graph;
import org.testobject.kernel.imaging.procedural.Util;

/**
 * 
 * @author enijkamp
 *
 */
public class AutoContrastTest {

    public static final boolean debug = Debug.toDebugMode(false);
	
	public static Graph.Builder slider(Dimension size, double position) {
		
		Color gray = rgb(100, 100, 100);
		Color lightblue = rgb(0, 0, 200);
		Color alphablue = rgba(0, 0, 150, 120);
		Color blue = rgb(0, 0, 255);
		
		double x = size.getWidth() * position;

		Rect bar = rect(size.getWidth(), 3, 3, 3, style(fill(gray)));
		Rect filled = rect(x, 3, 3, 3, style(fill(lightblue)));
		Circle bigcircle = circle(size.getHeight(), style(fill(alphablue)));
		Circle smallcircle = circle(6, style(fill(blue)));
		
		Graph.Builder graph = graph();

		graph
			.node(translate(0, size.height / 2 + 20))
				.element(bar)
				.element(filled)
				.child(node(translate(x, 2))
					.element(bigcircle)
					.element(smallcircle));
		
		return graph;
	}
	
	@Test
	public void autoContrast() throws IOException {
		
		Graph.Builder slider = slider(new Dimension(200, 40), 0.5d);
		Image.Int color = ImageUtil.Cut.trim(render(slider), new java.awt.Color(0, 0, 0, 0).getRGB());
		
		Image.Byte gray = ImageUtil.Convert.toImageByte(color);

        if(debug) VisualizerUtil.show("gray", ImageUtil.Convert.toImageInt(gray));
		
		Image.Byte auto = Contrast.autoContrast(gray);

        if(debug) VisualizerUtil.show("auto", ImageUtil.Convert.toImageInt(auto));
		
		if(debug) System.in.read();
	}
	
	@Test
	public void normalize() throws IOException {
		// synthetic
		{
			Graph.Builder slider = slider(new Dimension(200, 40), 0.5d);
			Image.Int color = render(slider);

            if(debug) VisualizerUtil.show("color (synth)", render(slider));
			
			color = ImageUtil.Cut.trim(color, new java.awt.Color(0, 0, 0, 0).getRGB());			
			Image.Int quantized = Quantize.quantize(color, 4);

            if(debug) VisualizerUtil.show("quantized (synth)", quantized);
			
			Image.Byte gray = ImageUtil.Convert.toImageByte(quantized);

            if(debug) VisualizerUtil.show("gray (synth)", ImageUtil.Convert.toImageInt(gray));
			
			Image.Byte auto = Contrast.autoContrast(gray);

            if(debug) VisualizerUtil.show("auto (synth)", ImageUtil.Convert.toImageInt(auto));
		}
		
		// real
		{
			Image.Int color = readImage("classifiers/slider/slider.png");

            if(debug) VisualizerUtil.show("color (real)", readImage("classifiers/slider/slider.png"));
			
			Image.Int quantized = Quantize.quantize(color, 5);

            if(debug) VisualizerUtil.show("quantized (real)", quantized);
			
			Image.Byte gray = ImageUtil.Convert.toImageByte(quantized);

            if(debug) VisualizerUtil.show("gray (real)", ImageUtil.Convert.toImageInt(gray));
			
			Image.Byte auto = Contrast.autoContrast(gray);

            if(debug) VisualizerUtil.show("auto (real)", ImageUtil.Convert.toImageInt(auto));
		}

        if(debug) System.in.read();
		
	}
	
	private static Image.Int render(Graph.Builder graph) {
		return Util.render(graph.build());
	}
	
	private static org.testobject.commons.util.image.Image.Int readImage(String file) throws IOException {
		return ImageUtil.Read.read(FileUtil.readFileFromClassPath("android/4_0_3/" + file));
	}

}
