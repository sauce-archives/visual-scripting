package org.testobject.kernel.api.events.output;

import java.util.List;

import org.testobject.kernel.api.classification.graph.Locator;

/**
 * 
 * @author enijkamp
 *
 */
public interface Responses {
	
	interface Response extends Event {

	}

	class Appears implements Responses.Response {
		public final Locator.Qualifier path;

		public Appears(Locator.Qualifier path) {
			this.path = path;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "(" + Util.toString(path) + ")";
		}
	}

	class Disappears implements Responses.Response {
		public final Locator.Qualifier path;

		public Disappears(Locator.Qualifier path) {
			this.path = path;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "(" + Util.toString(path) + ")";
		}
	}

	class Update implements Responses.Response {
		public static class Property {
			public final String name;
			public final Object oldValue, newValue;

			public Property(String name, Object oldValue, Object newValue) {
				this.name = name;
				this.oldValue = oldValue;
				this.newValue = newValue;
			}

			@Override
			public String toString() {
				return name + "(" + oldValue + "->" + newValue + ")";
			}
		}

		public final List<Update.Property> properties;
		public final Locator.Qualifier afterPath;
		public final Locator.Qualifier beforePath;

		public Update(Locator.Qualifier beforePath, Locator.Qualifier afterPath, List<Update.Property> properties) {
			this.afterPath = afterPath;
			this.beforePath = beforePath;
			this.properties = properties;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "(" + Util.toString(beforePath) + ", {" + toString(properties) + "})";
		}

		private String toString(List<Update.Property> properties) {
			StringBuilder string = new StringBuilder();
			for (int i = 0; i < properties.size(); i++) {
				string.append(properties.get(i));
				if (i != properties.size() - 1) {
					string.append(", ");
				}
			}
			return string.toString();
		}
	}
}