package org.testobject.kernel.imgproc.classifier;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.kernel.classification.contours.Contour;
import org.testobject.kernel.classification.contours.Contours;
import org.testobject.kernel.classification.polymatch.PolyMatch;
import org.testobject.kernel.classification.procedural.ProceduralRenderer;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.GraphBlobBuilder;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.classifier.Classes.Icon;
import org.testobject.kernel.imgproc.plot.VisualizerUtil;
import org.testobject.commons.math.algebra.Point;

/**
 * 
 * @author enijkamp
 *
 */
public class IconClassifier extends ClassifierBase {

	private static final boolean DEBUG = Debug.toDebugMode(false);

	private static final int MIN_HEIGHT = 20, MIN_WIDTH = 20;
	private static final int MAX_HEIGHT = 100, MAX_WIDTH = 100;

	private final Log log = LogFactory.getLog(IconClassifier.class);

	private static class NamedProcedure {

		public final String name;
		public final ProceduralRenderer.Procedure procedure;

		public NamedProcedure(String name, ProceduralRenderer.Procedure procedure) {
			this.name = name;
			this.procedure = procedure;
		}
	}

	private final NamedProcedure[] icons = {
//			cameraKomoot(),
//			magnifier(),
//			meinKomoot(),
//			meinePosition(),
//			regionen(),
//			mapKomoot(),
//			tourPlanen()//,
			bird(),
	        camera(),
	        marker(),
	        at(),
	        write(),
	        house(),
	        pin()
	};

	@Override
	public Specification getSpec() {
		return spec().requires(Classes.Group.class).returns(Classes.Icon.class).build();
	}

	@Override
	public Classifier.Match match(Context context, Blob blob) {

		// constraint 1: dimensions
		{
			if (blob.bbox.height > MAX_HEIGHT || blob.bbox.height > MAX_WIDTH)
				return failed;
			
			if (blob.bbox.height < MIN_HEIGHT || blob.bbox.height < MIN_WIDTH)
				return failed;
		}

		// FIXME cleanup (en)
		if (isGroup(blob)) {

			Match top = failed;
			for (NamedProcedure icon : icons) {

				if (icon.procedure.shapes.size() != blob.children.size())
					continue;

				float certaintySum = 0f;

				// FIXME blob should contain all (hierarchical) contours of trained icon, this code leads to wrong results (en)
				
				// FIXME this works for multiple blob on the same level, but not for nested blobs (en)
				for (Blob child : blob.children) {

					Contour testContour = new Contour(Contours.contourTrace(child));
					if (testContour.isEmpty())
						continue;

					float best = Float.MIN_VALUE;
					for (ProceduralRenderer.Shape shape : icon.procedure.shapes) {
						Contour trainContour = ProceduralRenderer.getContour(shape);
						float certainty = toCertainty(compareContours(trainContour, testContour));
						best = Math.max(certainty, best);
					}
					certaintySum += best;
				}
				
				if(certaintySum > top.certainty) {
					top = setClass(certaintySum, blob, new Icon(icon.name));
				}
			}

			if (top.certainty < .14d)
				return failed;

			if(DEBUG) {
				VisualizerUtil.show(ImageUtil.crop(context.before, blob.bbox));
			}

			return top;

		} else {

			Contour testContour = new Contour(Contours.contourTrace(blob));
			if (testContour.isEmpty())
				return failed;

			// TODO precompute contours of blobs (en)
			Contour topTrainContour = null;

			Match top = failed;
			for (NamedProcedure icon : icons) {
				Contour trainContour = ProceduralRenderer.getContour(icon.procedure.shapes.get(0));
				float certainty = toCertainty(compareContours(trainContour, testContour));
				if (certainty > top.certainty) {
					top = setClass(certainty, blob, new Icon(icon.name));
					topTrainContour = trainContour;
				}
				log.trace("icon: " + icon.name + " -> " + certainty);
			}

			if (DEBUG) {
				plotContour(testContour, "test contour");
				plotContour(topTrainContour, "train contour");
			}

			if (top.certainty < .14d)
				return failed;

			return top;

			// TODO match inner contours, if train icon has more nested contours (en)
			/*
			List<Contour> testContours = extractContours(blob);
			if (testContours.isEmpty())
				return failed;
			
			// TODO precompute contours of blobs (en)
			Match top = failed;
			for (Procedure icon : icons) {
				List<Contour> trainContours = ProceduralRenderer.getContours(icon.shapes);
				float certainty = toCertainty(compareContours(trainContours, testContours));
				if (certainty > top.certainty) {
					top = setWidget(certainty, blob, new Icon());
				}
			}
			
			if (top.certainty < .2d)
				return failed;
			
			return top;
			*/
		}
	}

	private void plotContour(Contour contour, String title) {
		ProceduralRenderer.Procedure proc = ProceduralRenderer.Builder.describe()
		        .shape(ProceduralRenderer.Builder.polygon()
		                .points(toXY(contour))
		                .stroke(Color.black)
		                .fill(Color.white))
		        .build();

		BufferedImage output = new ProceduralRenderer().render(proc);
		VisualizerUtil.show(title, output);
	}

	private float toCertainty(double mismatch) {
		// TODO find way to normalize mismatch (en)
		if (mismatch == 0d) {
			return 1f;
		}
		return (float) (1f / mismatch);
	}

	private static double compareContours(Contour trainContour, Contour testContour) {
		return PolyMatch.match(trainContour, testContour);
	}

	private static double compareContours(List<Contour> trainContour, List<Contour> testContour) {
		return PolyMatch.match(trainContour, testContour);
	}

	private static final double[] twitter_bird_points = { 29, 1, 27, 3, 27, 4, 26, 5, 26, 6, 25, 7, 25, 8, 24, 9, 24, 10, 23, 11, 21, 11,
	        19, 9,
	        18, 9, 17, 8, 16, 8, 15, 7, 14, 7, 13, 6, 11, 6, 10, 5, 9, 5, 8, 4, 6, 4, 6, 5, 10, 9, 9, 10, 8, 10, 8, 11, 12, 15, 11, 16, 10,
	        16, 10, 17, 11, 18, 12, 18, 13, 19, 14, 19, 15, 20, 14, 21, 14, 22, 15, 23, 16, 23, 17, 24, 16, 25, 15, 25, 13, 27, 3, 27, 2,
	        26, 1, 26, 4, 29, 5, 29, 7, 31, 8, 31, 9, 32, 9, 33, 28, 33, 29, 32, 30, 32, 32, 30, 33, 30, 40, 23, 40, 22, 41, 21, 41, 20,
	        43, 18, 47, 18, 47, 16, 45, 16, 44, 15, 45, 14, 46, 14, 47, 13, 47, 12, 46, 12, 45, 13, 43, 13, 42, 12, 42, 9, 37, 4, 36, 4,
	        35, 3, 35, 1 };

	private static final Color twitter_bird_color = new Color(46, 119, 171);

	private NamedProcedure bird() {
		return new NamedProcedure("bird",
		        ProceduralRenderer.Builder.describe()
		                .shape(ProceduralRenderer.Builder.polygon()
		                        .points(twitter_bird_points)
		                        .stroke(twitter_bird_color)
		                        .fill(twitter_bird_color))
		                .build());
	}

	private NamedProcedure camera() {
		return extractContour("classifier/icon/camera.png", "camera"); // FIXME define database and use global strings for "camera" etc. (en)
	}
	
	private NamedProcedure house() {
		return extractContour("classifier/icon/house.png", "house");
	}

	private NamedProcedure marker() {
		return extractContour("classifier/icon/marker.png", "marker");
	}

	private NamedProcedure at() {
		return extractContour("classifier/icon/at.png", "at");
	}
	
	private NamedProcedure pin() {
		return extractContour("classifier/icon/pin.png", "pin");
	}
	
	private NamedProcedure meinKomoot() {
		return extractContour("classifier/icon/komoot/MeinKomoot.png", "Mein Komoot");
	}
	
	private NamedProcedure tourPlanen() {
		return extractContour("classifier/icon/komoot/MeinePosition.png", "Meine Position");
	}
	
	private NamedProcedure meinePosition() {
		return extractContour("classifier/icon/komoot/Regionen.png", "Regionen");
	}
	
	private NamedProcedure regionen() {
		return extractContour("classifier/icon/komoot/TourPlanen.png", "Tour Planen");
	}
	
	private NamedProcedure magnifier() {
		return extractContour("classifier/icon/komoot/magnifier.png", "Lupe");
	}
	
	private NamedProcedure cameraKomoot() {
		return extractContour("classifier/icon/komoot/camera.png", "Kamera");
	}
	
	private NamedProcedure mapKomoot() {
		return extractContour("classifier/icon/komoot/map.png", "Map");
	}


	private NamedProcedure write() {
		ProceduralRenderer.Builder proc = ProceduralRenderer.Builder.describe();
		extractContour(proc, "classifier/icon/write_frame.png");
		extractContour(proc, "classifier/icon/write_feather.png");
		return new NamedProcedure("write", proc.build());
	}

	// FIXME duplicate code (en)
	private void extractContour(ProceduralRenderer.Builder proc, String file) {
		try {
			// blobs
			Image.Int trainImage = ImageUtil.grow(ImageUtil.read(ClassLoader.getSystemResource(file)), 2, 2);
			GraphBlobBuilder trainBuilder = new GraphBlobBuilder(trainImage.w, trainImage.h);
			Blob[] trainBlobs = trainBuilder.build(trainImage);

			// contours
			Blob trainBlob = trainBlobs[0].children.get(0).children.get(0); // FIXME (en)
			List<Contour> train = extractContours(trainBlob);

			// procedure
			for (Contour contour : train) {
				proc.shape(ProceduralRenderer.Builder.polygon()
				        .points(toXY(contour))
				        .stroke(ProceduralRenderer.Builder.Color.blue())
				        .fill(ProceduralRenderer.Builder.Color.blue()));
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private NamedProcedure extractContour(String file, String type) {
		try {
			// blobs
			Image.Int trainImage = ImageUtil.grow(ImageUtil.read(ClassLoader.getSystemResource(file)), 2, 2);
			GraphBlobBuilder trainBuilder = new GraphBlobBuilder(trainImage.w, trainImage.h);
			Blob[] trainBlobs = trainBuilder.build(trainImage);

			// contours
			Blob trainBlob = trainBlobs[0].children.get(0).children.get(0); // FIXME (en)
			List<Contour> train = extractContours(trainBlob);

			// procedure
			ProceduralRenderer.Builder proc = ProceduralRenderer.Builder.describe();
			for (Contour contour : train) {
				proc.shape(ProceduralRenderer.Builder.polygon()
				        .points(toXY(contour))
				        .stroke(ProceduralRenderer.Builder.Color.blue())
				        .fill(ProceduralRenderer.Builder.Color.blue()));
			}
			return new NamedProcedure(type, proc.build());
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static List<Contour> extractContours(Blob blob) {
		List<Contour> contours = new LinkedList<>();
		contours.add(new Contour(Contours.contourTrace(blob)));
		for (Blob child : blob.children) {
			extractContours(child, child.bbox.x - blob.bbox.x, child.bbox.y - blob.bbox.y, contours);
		}
		return contours;
	}

	private static void extractContours(Blob blob, int offsetX, int offsetY, List<Contour> contours) {
		contours.add(new Contour(offset(Contours.contourTrace(blob), offsetX, offsetY)));
		for (Blob child : blob.children) {
			extractContours(child, offsetX + child.bbox.x - blob.bbox.x, offsetY + child.bbox.y - blob.bbox.y, contours);
		}
	}

	private static List<Point.Double> offset(List<Point.Double> points, int x, int y) {
		for (Point.Double point : points) {
			point.x += x;
			point.y += y;
		}
		return points;
	}

	private static double[] toXY(Contour points) {
		double[] xy = new double[points.npoints * 2];
		for (int i = 0; i < points.npoints; i++) {
			xy[i * 2 + 0] = points.xpoints[i];
			xy[i * 2 + 1] = points.ypoints[i];
		}
		return xy;
	}
}
