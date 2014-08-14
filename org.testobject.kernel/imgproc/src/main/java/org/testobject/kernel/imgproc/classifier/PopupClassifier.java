package org.testobject.kernel.imgproc.classifier;

import java.awt.Color;

import org.testobject.kernel.classification.contours.Contour;
import org.testobject.kernel.classification.contours.Contours;
import org.testobject.kernel.classification.polymatch.PolyMatch;
import org.testobject.kernel.classification.procedural.ProceduralRenderer;
import org.testobject.kernel.imgproc.blob.Blob;

import static org.testobject.kernel.classification.procedural.ProceduralRenderer.Builder.rect;

/**
 * 
 * @author enijkamp
 *
 */
public class PopupClassifier extends ClassifierBase
{
	private final static int MIN_HEIGHT = 700, MIN_WIDTH = 400;

	@Override
	public Specification getSpec()
	{
		return spec().requires(none()).returns(Classes.Popup.class).build();
	}

	@Override
	public Match match(Context context, Blob blob)
	{
		// constraint: dimensions
		{
			if (blob.bbox.height < MIN_HEIGHT || blob.bbox.width < MIN_WIDTH)
				return failed;
		}

		// constraint: childs
		{
			if (blob.children.isEmpty())
				return failed;
		}

		// constraint: round rect
		{
			// VisualizerUtil.show(ImageUtil.crop(context.before, blob.bbox));

			Contour testContour = new Contour(Contours.contourTrace(blob));
			if (testContour.isEmpty())
				return failed;

			// TODO precompute contours of blobs (en)
			ProceduralRenderer.Procedure popup = popup(blob.bbox.width, blob.bbox.height);
			Contour trainContour = ProceduralRenderer.getContour(popup.shapes.get(0)); // FIXME (en)
			double geometricMismatch = compareContours(trainContour, testContour);
			if (geometricMismatch > 1d)
				return failed;
		}

		return ClassifierBase.setClass(1.0f, blob, new Classes.Popup(blob.children));
	}

	private ProceduralRenderer.Procedure popup(int w, int h) {
		return ProceduralRenderer.Builder.describe()
		        .shape(ProceduralRenderer.Builder.rect(w, h)
		                .round(24, 24)
		                .stroke(Color.darkGray)
		                .fill(Color.white))
		        .build();
	}

	private double compareContours(Contour trainContour, Contour testContour) {
		return PolyMatch.match(trainContour, testContour);
	}
}