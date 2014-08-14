package org.testobject.kernel.api.classification.graph;

import org.testobject.kernel.imaging.procedural.Graph;
import org.testobject.kernel.imaging.procedural.Node;
import org.testobject.kernel.imaging.procedural.Transform;
import org.testobject.kernel.imaging.procedural.Transform.Scale;
import org.testobject.kernel.imaging.procedural.Transform.Translate;

/**
 * 
 * @author enijkamp
 *
 */
public interface Depiction {
	
	Transform.Translate getTranslate();
	
	Transform.Scale getScale();
	
	Node getGraph();
	
	class Builder {
		
		private static final class Impl implements Depiction {
			
			private final Translate translate;
			private final Scale scale;
			private final Node graph;
			
			public Impl(Translate translate, Scale scale, Node graph) {
				this.translate = translate;
				this.scale = scale;
				this.graph = graph;
			}

			@Override
			public Translate getTranslate() {
				return translate;
			}

			@Override
			public Scale getScale() {
				return scale;
			}

			@Override
			public Node getGraph() {
				return graph;
			}
			
			@Override
			public String toString() {
				return "{" + translate + ", " + scale + ", graph={" + graph.toString() + "}}";
			}
		};
	
		public static Depiction create(final Graph.Builder builderGraph) {
			final Node graph = builderGraph.build();
			
			return new Impl(Transform.Translate.Builder.identity(), Transform.Scale.Builder.identity(), graph);
		}
		
		public static Depiction create(final org.testobject.kernel.imaging.procedural.Node graph) {
			return new Impl(Transform.Translate.Builder.identity(), Transform.Scale.Builder.identity(), graph);
		}
		
		public static Depiction create(final Node graph, final Translate translate, final Scale scale) {
			return new Impl(translate, scale, graph);
		}
	}
}
