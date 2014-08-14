package org.testobject.experiments.render.graph;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.testobject.kernel.api.classification.graph.Element.Builder.depict;
import static org.testobject.kernel.api.classification.graph.Element.Builder.element;
import static org.testobject.kernel.api.classification.graph.Element.Builder.position;
import static org.testobject.kernel.api.classification.graph.Element.Builder.size;
import static org.testobject.kernel.api.classification.graph.Element.Builder.value;
import static org.testobject.kernel.imaging.procedural.Element.Builder.circle;
import static org.testobject.kernel.imaging.procedural.Element.Builder.rect;
import static org.testobject.kernel.imaging.procedural.Node.Builder.node;
import static org.testobject.kernel.imaging.procedural.Style.Builder.fill;
import static org.testobject.kernel.imaging.procedural.Style.Builder.rgb;
import static org.testobject.kernel.imaging.procedural.Style.Builder.rgba;
import static org.testobject.kernel.imaging.procedural.Style.Builder.style;
import static org.testobject.kernel.imaging.procedural.Transform.Builder.scale;
import static org.testobject.kernel.imaging.procedural.Transform.Builder.translate;

import org.junit.Test;
import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Size;
import org.testobject.kernel.api.classification.classifiers.Classifier;
import org.testobject.kernel.api.classification.graph.Depiction;
import org.testobject.kernel.api.classification.graph.Variable;
import org.testobject.kernel.imaging.procedural.Color;
import org.testobject.kernel.imaging.procedural.Element;
import org.testobject.kernel.imaging.procedural.Graph;
import org.testobject.kernel.imaging.segmentation.Mask;

/**
 * 
 * @author enijkamp
 *
 */
public class ElementTest {
	
	static class Slider {
	
		public static org.testobject.kernel.api.classification.graph.Element create(Point.Int position, Size.Int size, double progress) {
			
			Graph.Builder graph = Graph.Builder.graph();
			{
				Color gray = rgb(100, 100, 100);
				Color lightblue = rgb(0, 0, 200);
				Color alphablue = rgba(0, 0, 150, 120);
				Color blue = rgb(0, 0, 255);
				
				double x = size.getWidth() * progress;
	
				Element.Rect bar = rect(size.getWidth(), 3, 3, 3, style(fill(gray)));
				Element.Rect filled = rect(x, 3, 3, 3, style(fill(lightblue)));
				Element.Circle bigcircle = circle(size.getHeight(), style(fill(alphablue)));
				Element.Circle smallcircle = circle(6, style(fill(blue)));
	
				graph
					.node(translate(0, size.h / 2 + 20))
						.element(bar)
						.element(filled)
						.child(node(translate(x, 2))
							.element(bigcircle)
							.element(smallcircle));
			}
			
			Depiction depiction = Depiction.Builder.create(graph.build(), translate(0d, 0d), scale(1d, 1d));
			
			return element(Mask.Builder.none())
					.qualifier(Classifier.Qualifier.Factory.create("foo", "bar"))
					.feature(depict(depiction))
					.feature(position(new Point.Int(10, 20)))
					.feature(size(new Size.Int(30, 40)))
					.feature(value("element.progress", progress))
				.build();
		}

	}
	
	@Test
	public void testVariables() {
		
		org.testobject.kernel.api.classification.graph.Element element = Slider.create(new Point.Int(10, 20), new Size.Int(30, 40), 0.5d);
		
		{
			Variable<Point.Int> var = getVariable(element, "geometric.position");
			assertThat(var.getValue().x, is(10));
			assertThat(var.getValue().y, is(20));
		}
		
		{
			Variable<Size.Int> var = getVariable(element, "geometric.size");
			assertThat(var.getValue().w, is(30));
			assertThat(var.getValue().h, is(40));
		}
		
		{
			Variable<Double> var = getVariable(element, "element.progress");
			assertThat(var.getValue(), is(0.5d));
		}
		
	}

	@SuppressWarnings("unchecked")
	private <T> Variable<T> getVariable(org.testobject.kernel.api.classification.graph.Element element, String name) {
		for(Variable<?> feature : element.getLabel().getFeatures()) {
			if(feature.getName().equals(name)) {
				return (Variable<T>) feature;
			}
		}
		
		throw new IllegalArgumentException(name);
	}

}
