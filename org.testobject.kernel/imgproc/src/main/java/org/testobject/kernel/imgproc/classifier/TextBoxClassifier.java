package org.testobject.kernel.imgproc.classifier;

import static org.testobject.kernel.classification.procedural.ProceduralRenderer.Builder.rect;
import static org.testobject.kernel.imgproc.classifier.Utils.findRecursive;

import java.awt.Color;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.testobject.kernel.classification.contours.Contour;
import org.testobject.kernel.classification.contours.Contours;
import org.testobject.kernel.classification.polymatch.PolyMatch;
import org.testobject.kernel.classification.procedural.ProceduralRenderer;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.classifier.Classes.TextChar;
import org.testobject.kernel.imgproc.classifier.Classes.TextBox;
import org.testobject.kernel.imgproc.classifier.Classes.TextWord;

/**
 * 
 * @author enijkamp
 *
 */
public class TextBoxClassifier extends ClassifierBase
{
	private final static int MIN_HEIGHT = 200, MAX_HEIGHT = 600;

	private static class LeftToRightSorter implements Comparator<Blob> {
		@Override
		public int compare(Blob b1, Blob b2) {
			return Integer.compare(b1.bbox.x, b2.bbox.x);
		}
	}
	
	@Override
	public Specification getSpec()
	{
		return spec().requires(TextWord.class).returns(TextBox.class).build();
	}

	@Override
	public Match match(Context context, Blob blob)
	{
		// constraint 1: dimensions
		{
			if (blob.bbox.height < MIN_HEIGHT || blob.bbox.height > MAX_HEIGHT)
				return failed;
		}

		// constraint 2: round rect
		{
			Contour testContour = new Contour(Contours.contourTrace(blob));
			if (testContour.isEmpty())
				return failed;

			// TODO precompute contours of blobs (en)
			ProceduralRenderer.Procedure textbox = textbox(blob.bbox.width, blob.bbox.height);
			Contour trainContour = ProceduralRenderer.getContour(textbox.shapes.get(0)); // FIXME (en)
			double geometricMismatch = compareContours(trainContour, testContour);
			if (geometricMismatch > 1d)
				return failed;
		}

		// constraint 3: white
		{
			int pixels = 0;
			for (int y = 0; y < blob.bbox.height; y++) {
				for (int x = 0; x < blob.bbox.width; x++) {
					int rgb = context.after.get(blob.bbox.x + x, blob.bbox.y + y);
					int r = (rgb >> 16) & 0xff;
					int g = (rgb >> 8) & 0xff;
					int b = (rgb >> 0) & 0xff;
					if (r > 245 && g > 245 && b > 245) {
						pixels++;
					}
				}
			}
			float coverage = toFloat(pixels) / (blob.bbox.height * blob.bbox.width);
			if (coverage < .85f)
				return failed;
		}

		// text
		{
			List<Blob> texts = Utils.findRecursive(blob, filter(TextChar.class));
			Collections.sort(texts, new LeftToRightSorter());
	
			return setClass(1.0f, blob, new TextBox(texts));
		}
	}

	private static final float toFloat(int value) {
		return (float) value;
	}

	private ProceduralRenderer.Procedure textbox(int w, int h) {
		return ProceduralRenderer.Builder.describe()
		        .shape(ProceduralRenderer.Builder.rect(w, h)
		                .round(12, 12)
		                .stroke(Color.darkGray)
		                .gradient(Color.white, Color.lightGray))
		        .build();
	}

	private double compareContours(Contour trainContour, Contour testContour) {
		return PolyMatch.match(trainContour, testContour);
	}
}