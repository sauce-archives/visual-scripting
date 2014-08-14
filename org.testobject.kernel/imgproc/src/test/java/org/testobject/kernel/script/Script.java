package org.testobject.kernel.script;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.testobject.commons.events.ButtonMask;
import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.locator.api.Button;
import org.testobject.kernel.locator.api.Locator;
import org.testobject.kernel.locator.api.Popup;
import org.testobject.kernel.locator.api.Root;
import org.testobject.kernel.locator.api.TextBox;
import org.testobject.kernel.script.api.Events;
import org.testobject.kernel.script.api.Script.Requests.ClickOn;
import org.testobject.kernel.script.api.Script.Requests.Request;
import org.testobject.kernel.script.api.Script.Responses.Appears;
import org.testobject.kernel.script.api.Script.Responses.Disappears;
import org.testobject.kernel.script.api.Script.Responses.Response;

public class Script {

	public static class Builder {

		private static int timestamp = 1;

		private List<Step> steps;

		public Builder() {
			steps = new ArrayList<>();
		}

		public Builder step(Image.Int[] images, Request request, Response... responses) {
			steps.add(new Step(images[0], images[1], request, Arrays.asList(responses)));
			return this;
		}

		public Script build() {
			return new Script(steps);
		}

		public static Image.Int[] images(Image.Int before, Image.Int after) {
			return new Image.Int[] { before, after };
		}

		public static Request click(Point.Int point, LinkedList<Locator> path) {
			return new ClickOn(new Events.Click(timestamp(), ButtonMask.ButtonLeft, point.getX(),
					point.getY()), path);
		}

		public static Point.Int at(int x, int y) {
			return new Point.Int(x, y);
		}

		public static Button button(String label) {
			return new Button(label);
		}

		public static TextBox textBox(String text) {
			return new TextBox(text);
		}

		public static Popup popup(Locator child) {
			return new Popup(new Locator[] { child });
		}

		public static LinkedList<Locator> path(Locator child) {
			// FIXME build the complete path recursivly (?) (tk)
			Root root = new Root(new Locator[] { child });
			return new LinkedList<>(Arrays.asList(new Locator[] { root, child }));
		}

		public static Response disappears(LinkedList<Locator> path) {
			return new Disappears(path);
		}

		public static Response appears(LinkedList<Locator> path) {
			return new Appears(path);
		}

		private static int timestamp() {
			return timestamp++;
		}

	}

	public static class Step {

		private Image.Int before, after;
		private Request request;
		private List<Response> responses = new ArrayList<>();

		public Step(Image.Int before, Image.Int after, Request request, List<Response> responses) {
			this.before = before;
			this.after = after;
			this.request = request;
			this.responses = responses;
		}

		public Image.Int getBefore() {
			return before;
		}

		public Image.Int getAfter() {
			return after;
		}

		public Request getRequest() {
			return request;
		}

		public List<Response> getResponses() {
			return responses;
		}

	}

	private final List<Step> steps;

	private Script(List<Step> steps) {
		this.steps = steps;
	}

	public List<Step> getSteps() {
		return steps;
	}

}
