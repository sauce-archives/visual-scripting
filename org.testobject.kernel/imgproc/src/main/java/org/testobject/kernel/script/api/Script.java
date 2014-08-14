package org.testobject.kernel.script.api;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.testobject.kernel.inputs.InputStateMachine;
import org.testobject.kernel.locator.api.Locator;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.script.api.Script.Requests.Request;
import org.testobject.kernel.script.api.Script.Responses.Response;

/**
 * 
 * @author enijkamp
 * 
 */
public interface Script {
	interface Event {
	}

	// FIXME rename to input (en)
	class Action {
		public final Image.Int before, after;
		public final Request request;
		public final List<Response> response;

		public Action(Image.Int before, Image.Int after, Request request, List<Response> response) {
			this.before = before;
			this.after = after;
			this.request = request;
			this.response = response;
		}
	}

	interface Requests {
		interface Request extends Event {

		}

		class ClickOn implements Request {
			// FIXME coordinate is not required (for replay) (en)
			public final Events.Click click;

			public final LinkedList<Locator> path;

			public ClickOn(Events.Click click, LinkedList<Locator> path) {
				this.click = click;
				this.path = path;
			}

			@Override
			public String toString() {
				return getClass().getSimpleName() + "(" + Script.Util.toString(path) + ")";
			}
		}

		class Type implements Request {
			public final Events.Type type;

			public Type(Events.Type type) {
				this.type = type;
			}

			@Override
			public String toString() {
				return getClass().getSimpleName() + "('" + getInput() + "')";
			}

			public String getInput() {
				StringBuffer b = new StringBuffer();
				for (InputStateMachine.State.Key key : type.keys) {
					b.append((char) key.key);
				}

				return b.toString();
			}
		}

		class WaitFor implements Request {
			@Override
			public String toString() {
				return getClass().getSimpleName();
			}
		}
	}

	interface Responses {
		interface Response extends Event {

		}

		class Appears implements Response {
			public final LinkedList<Locator> path;

			public Appears(LinkedList<Locator> path) {
				this.path = path;
			}

			@Override
			public String toString() {
				return getClass().getSimpleName() + "(" + Script.Util.toString(path) + ")";
			}
		}

		class Disappears implements Response {
			public final LinkedList<Locator> path;

			public Disappears(LinkedList<Locator> path) {
				this.path = path;
			}

			@Override
			public String toString() {
				return getClass().getSimpleName() + "(" + Script.Util.toString(path) + ")";
			}
		}

		class Update implements Response {
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

			public final List<Property> properties;
			public final LinkedList<Locator> afterPath;
			public final LinkedList<Locator> beforePath;

			public Update(LinkedList<Locator> beforePath, LinkedList<Locator> afterPath, List<Property> properties) {
				this.afterPath = afterPath;
				this.beforePath = beforePath;
				this.properties = properties;
			}

			@Override
			public String toString() {
				return getClass().getSimpleName() + "(" + Script.Util.toString(beforePath) + ", {" + toString(properties) + "})";
			}

			private String toString(List<Property> properties) {
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

	class Util {
		public static String toString(List<Locator> widgets) {
			String path = "";
			if (widgets.isEmpty()) {
				return path;
			} else {
				Iterator<Locator> iter = widgets.iterator();
				for (int i = 0; i < widgets.size() - 1; i++) {
					Locator widget = iter.next();
					path += widget + "->";
				}
				return path + iter.next().toString();
			}
		}
	}
}
