package org.testobject.kernel.api.events.input;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jettison.json.JSONArray;

public class MouseDragEvent extends MouseEvent {

	public final int[][] points;

	@JsonCreator
	public MouseDragEvent(@JsonProperty("points") int[][] points) {
		super(Type.DRAG, null);
		this.points = points;
	}

	@Override
	public String toString() {
		return "Drag(" + points.toString() + ")";
	}
	
	public static String pointsAsString(int[][] points) {
		ArrayList<JSONArray> jsonPoints = new ArrayList<JSONArray>(points.length);
		for (int[] point : points) {
			JSONArray p = new JSONArray();
			for (int i : point) {
				p.put(i);
			}
			jsonPoints.add(p);
		}
		return (new JSONArray(jsonPoints).toString());
	}
	
}
