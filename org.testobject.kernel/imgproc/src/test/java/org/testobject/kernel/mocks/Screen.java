package org.testobject.kernel.mocks;

import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;

import org.testobject.kernel.classification.procedural.ProceduralRenderer;
import org.testobject.commons.util.image.Image;

import static org.testobject.kernel.classification.procedural.ProceduralRenderer.Builder.rect;

/**
 * 
 * @author enijkamp
 *
 */
public class Screen {

	public static class Builder {

		public final int w, h;
		public final List<ProceduralRenderer.Procedure> procedures = new LinkedList<>();

		public Builder(int w, int h) {
			this.w = w;
			this.h = h;
		}

		public Screen.Builder button(int x, int y, int w, int h, String title) {
			procedures.add(Screen.button(x, y, w, h, title));
			return this;
		}

		public Screen.Builder button(Rectangle r, String title) {
			procedures.add(Screen.button(r.x, r.y, r.width, r.height, title));
			return this;
		}

		public Screen.Builder textbox(Rectangle r) {
			procedures.add(Screen.textbox(r.x, r.y, r.width, r.height));
			return this;
		}

		public Screen.Builder textbox(int x, int y, int w, int h) {
			procedures.add(Screen.textbox(x, y, w, h));
			return this;
		}

		public Screen.Builder icon(int x, int y, double[] contour) {
			procedures.add(Screen.icon(x, y, contour));
			return this;
		}

		public Image.Int build() {
			return new ProceduralRenderer().render(w, h, procedures);
		}
	}

	public static Screen.Builder screen(int w, int h) {
		return new Builder(w, h);
	}

	public static ProceduralRenderer.Procedure button(int x, int y, int w, int h, String title) {
		return ProceduralRenderer.Builder.describe()
		        .shape(ProceduralRenderer.Builder.rect(x, y, w, h)
		                .round(12, 12)
		                .stroke(ProceduralRenderer.Builder.Color.darkGray())
		                .gradient(ProceduralRenderer.Builder.Color.white(), ProceduralRenderer.Builder.Color.lightGray())
		                .text(title)
		        )
		        .build();
	}

	public static ProceduralRenderer.Procedure textbox(int x, int y, int w, int h) {
		return ProceduralRenderer.Builder.describe()
		        .shape(ProceduralRenderer.Builder.rect(x, y, w, h)
		                .round(12, 12)
		                .stroke(ProceduralRenderer.Builder.Color.darkGray())
		                .fill(ProceduralRenderer.Builder.Color.white())
		        )
		        .build();
	}

	public static ProceduralRenderer.Procedure icon(int x, int y, double[] contour) {
		double[] shape = translate(contour, x, y);
		return ProceduralRenderer.Builder.describe()
		        .shape(ProceduralRenderer.Builder.polygon()
		                .points(shape)
		                .stroke(ProceduralRenderer.Builder.Color.blue())
		                .fill(ProceduralRenderer.Builder.Color.blue()))
		        .build();
	}

	private static double[] translate(double[] contour, int x, int y) {
		double[] translated = new double[contour.length];
		for (int i = 0; i < contour.length; i += 2) {
			translated[i + 0] = contour[i + 0] + x;
			translated[i + 1] = contour[i + 1] + y;
		}
		return translated;
	}
}