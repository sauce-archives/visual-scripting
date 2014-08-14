package org.testobject.kernel.classification.parser;

import org.testobject.commons.util.image.Image;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.classification.parser.Operations.Operation;

/**
 * 
 * @author enijkamp
 *
 */
public interface Cache extends Operation {
	
	interface IsCache<T> {
		
		void reset();
		
		T getCache();
		
	}
	
	class Input implements Operations.Input, Cache.IsCache<Node> {
		
		private final Operations.Input proxee;
		private Node cache;
		
		public Input(Operations.Input proxee) {
			this.proxee = proxee;
		}

		@Override
		public void reset() {
			this.cache = null;
		}

		@Override
		public Node apply(Image.Int raw) {
			if(cache == null) {
				this.cache = proxee.apply(raw);
			}
			return this.cache ;
		}
		
		@Override
		public String toString() {
			return "cache(" + proxee.toString() + ")";
		}

		@Override
		public Node getCache() {
			if(cache == null) {
				throw new IllegalStateException();
			}
			return cache;
		}
	}
	
	class Map implements Operations.Map<Node, Node>, Cache.IsCache<Node> {
		
		private final Operations.Map<Node, Node> proxee;
		private Node cache;
		
		public Map(Operations.Map<Node, Node> proxee) {
			this.proxee = proxee;
		}

		@Override
		public void reset() {
			this.cache = null;
		}

		@Override
		public Node apply(Node node, Context context) {
			if(cache == null) {
				this.cache = proxee.apply(node,context);
			}
			return this.cache ;
		}
		
		@Override
		public String toString() {
			return "cache(" + proxee.toString() + ")";
		}
		
		@Override
		public Node getCache() {
			if(cache == null) {
				throw new IllegalStateException();
			}
			return cache;
		}
	}
}