package org.testobject.kernel.imgproc.classifier;

import static org.testobject.kernel.classification.procedural.ProceduralRenderer.Builder.rect;
import static org.testobject.kernel.imgproc.classifier.Utils.findRecursive;

import java.awt.Color;
import java.util.List;

import org.testobject.kernel.classification.contours.Contour;
import org.testobject.kernel.classification.contours.Contours;
import org.testobject.kernel.classification.polymatch.PolyMatch;
import org.testobject.kernel.classification.procedural.ProceduralRenderer;
import org.testobject.kernel.imgproc.plot.VisualizerUtil;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.commons.util.config.Debug;
import org.testobject.kernel.imgproc.blob.*;

/**
 * 
 * @author enijkamp
 *
 */
public class ButtonClassifier extends ClassifierBase
{
	private final static boolean DEBUG = Debug.toDebugMode(false);

	private final static int MIN_HEIGHT = 40;
    private final static int MIN_WIDTH = 40;
    
    private final static int MAX_HEIGHT = 80;

	@Override
	public Specification getSpec()
	{
		return spec().requires(Classes.TextChar.class, Classes.Icon.class).returns(Classes.Button.class).build();
	}

	@Override
	public Match match(Context context, Blob blob)
	{
		// constraint: dimensions
		{
			if (blob.bbox.height < MIN_HEIGHT || blob.bbox.height > MAX_HEIGHT)
				return failed;

            if (blob.bbox.width < MIN_WIDTH)
                return failed;
            
            //fixme doesn't work for wide buttons like komoot app (al)
            if(blob.bbox.width > 400){
            	return failed;
            }
		}

		// constraint: only contains text and icons
		{
			Utils.UnaryFilter<Blob> filter = new Utils.UnaryFilter<Blob>() {
				@Override
				public boolean pass(Blob blob) {
					return blob.meta.getClass() != Meta.Blob.class
							&& blob.meta.getClass() != Classes.Group.class
					        && blob.meta.getClass() != Classes.Icon.class
					        && blob.meta.getClass() != Classes.TextChar.class // FIXME messy, difficult to maintain (en)
					        && blob.meta.getClass() != Classes.TextWord.class
					        && blob.meta.getClass() != Classes.TextLine.class;
				}
			};

			List<Blob> widgets = Utils.findRecursive(blob, filter);

			if (widgets.isEmpty() == false)
				return failed;
		}
		
		if (DEBUG) {
			VisualizerUtil.show("blob=" + blob.id, ImageUtil.crop(context.after, blob.bbox));
			BlobUtils.printMeta(blob);
		}

		// constraint: round rect
		{
			Contour testContour = new Contour(Contours.contourTrace(blob));
			if (testContour.isEmpty())
				return failed;

			// TODO precompute contours of blobs (en)
			ProceduralRenderer.Procedure button = button(blob.bbox.width, blob.bbox.height);
			Contour trainContour = ProceduralRenderer.getContour(button.shapes.get(0)); // FIXME (en)
			double geometricMismatch = compareContours(trainContour, testContour);
			if (geometricMismatch > 1d)
				return failed;
		}

		// constraint: centered text or icon
		{
			// TODO is centered? (en)
			List<Blob> texts = Utils.findRecursive(blob, filter(Classes.TextChar.class));
			List<Blob> icons = Utils.findRecursive(blob, filter(Classes.Icon.class));

			if (texts.isEmpty() && icons.isEmpty())
				return failed;

			if (icons.isEmpty() == false) {
				return ClassifierBase.setClass(1.0f, blob, new Classes.Button(false, icons, ((Classes.Icon) icons.get(0).meta).name));
			} else {
				return ClassifierBase.setClass(1.0f, blob, new Classes.Button(true, texts));
			}

			// TODO certainty -> likelihood, see experimental code  (en)
		}
	}

	private ProceduralRenderer.Procedure button(int w, int h) {
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