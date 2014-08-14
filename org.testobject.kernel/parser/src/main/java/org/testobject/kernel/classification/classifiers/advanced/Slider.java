package org.testobject.kernel.classification.classifiers.advanced;

import static org.testobject.kernel.imaging.procedural.Element.Builder.circle;
import static org.testobject.kernel.imaging.procedural.Style.Builder.fill;
import static org.testobject.kernel.imaging.procedural.Graph.Builder.graph;
import static org.testobject.kernel.imaging.procedural.Node.Builder.node;
import static org.testobject.kernel.imaging.procedural.Element.Builder.rect;
import static org.testobject.kernel.imaging.procedural.Style.Builder.rgb;
import static org.testobject.kernel.imaging.procedural.Style.Builder.rgba;
import static org.testobject.kernel.imaging.procedural.Transform.Builder.translate;
import static org.testobject.kernel.imaging.procedural.Style.Builder.style;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.math.algebra.Size;
import org.testobject.kernel.imaging.contours.PolyMatch;
import org.testobject.kernel.imaging.contours.Trace;
import org.testobject.kernel.imaging.procedural.Color;
import org.testobject.kernel.imaging.procedural.ContourExtractor;
import org.testobject.kernel.imaging.procedural.Element;
import org.testobject.kernel.imaging.procedural.Util;
import org.testobject.kernel.imaging.segmentation.Blob;
import org.testobject.kernel.imaging.segmentation.BlobUtils;
import org.testobject.kernel.imaging.segmentation.Mask;
import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.commons.util.image.Image.Int.Type;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.classification.graph.Context;
import org.testobject.kernel.imaging.color.models.YCrCb;
import org.testobject.commons.tools.plot.VisualizerUtil;
import org.testobject.kernel.imaging.procedural.Graph;

/**
 * 
 * @author enijkamp
 *
 */
public class Slider {

    public static final boolean debug = Debug.toDebugMode(true);

    // FIXME belongs to template (en)
	private static final int HEIGHT_HANDLE = 6;
	
	public static class Rewrite {
		
		// TODO variables (locator replacement) (en)
		
		// TODO keep graph transformation here (en)
		
	}
	
	public static class Label {
		
		public final double likelihood; // TODO geometric / photometric mismatch (en)
		public final Synthesization synthesization; // graph.render.template
		public final Rewrite rewrite; // graph.node.rewrite

		public Label(Synthesization synthesization, Rewrite rewrite, double likelihood) {
			this.synthesization = synthesization;
			this.rewrite = rewrite;
			this.likelihood = likelihood;
		}
		
	}
	
	public static class Synthesization {
		
		public final Graph.Builder graph;

		public Synthesization(Graph.Builder graph) {
			this.graph = graph;
		}
	}
	
	
	public static class Templates {
		
		public interface SliderTemplate {
			
			Graph.Builder create(Size.Int size, double position);
			
			Element getBar();
			
			Element getHandle();
			
		}
		
		public static class Native implements SliderTemplate {
			
			// FIXME use luma model or so (color independent) (en)
			Color gray = rgb(100, 100, 100);
			Color lightblue = rgb(0, 0, 200);
			Color alphablue = rgba(0, 0, 150, 120);
			Color blue = rgb(0, 0, 255);
		
			@Override
			public Graph.Builder create(Size.Int size, double position) {
				
				double x = size.getWidth() * position;
	
				Element.Rect bar = rect(size.getWidth(), 3, 3, 3, style(fill(gray)));
				Element.Rect filled = rect(x, 3, 3, 3, style(fill(lightblue)));
				Element.Circle bigcircle = circle(size.getHeight(), style(fill(alphablue)));
				Element.Circle smallcircle = circle(6, style(fill(blue)));
				
				Graph.Builder graph = graph();
	
				graph
					.node(translate(0, size.h / 2 + 20))
						.element(bar)
						.element(filled)
						.child(node(translate(x, 2))
							.element(bigcircle)
							.element(smallcircle));
				
				return graph;
			}

			@Override
			public Element getBar() {
				return rect(300, 3, 3, 3, style(fill(gray)));
			}

			@Override
			public Element getHandle() {
				return circle(30, style(fill(alphablue)));
			}
		}
	}
	
	private final static Templates.SliderTemplate[] templates = { new Templates.Native() };
	
	private static Label failed() {
		// TODO impelement (en)
		return null;
	}
	
	public Label classify(Context context, Node node) {
		
		// FIXME iterate over templates (en)
		Templates.SliderTemplate template = templates[0];
		
		// FIXME work on element hierarchy only (en)
		Blob blob = context.mapping.getBlob(node);
		
		// TODO hold variables somewhere (en)
		double position = 0;
		
		// TODO keep geometric and photometric mismatch in label (en)
		double geometricMismatch = 0d;
		double photometricMismatch = 0d;
		
		// discriminative classification
		Mask element, bar, handle;
		{
			
			// 1. identify elements
			{
				element = joinBlobs(context.neighbours, blob);

                if(debug) VisualizerUtil.show("element", ImageUtil.Convert.toBufferedImage(BlobUtils.Cut.cutByMask(context.image, element)));
				
				Histogram histogram = getHistogram(element);
				
				print(histogram.bins);
				
				int[] splits = locateSplits(histogram);
				
				print(splits);
				
				bar = locateBar(element, splits, histogram);
				handle = locateHandle(element, splits, histogram);

                if(debug) {
                    VisualizerUtil.show("bar", ImageUtil.Convert.toBufferedImage(BlobUtils.Cut.cutByMask(context.image, bar)));
                    VisualizerUtil.show("handle", ImageUtil.Convert.toBufferedImage(BlobUtils.Cut.cutByMask(context.image, handle)));
                }
			}
			
			// 2. classify
			double distanceBar, distanceHandle;
			{
				{
					List<Point.Double> trainContour = extractContour(template.getBar());
					List<Point.Double> testContour = extractContour(bar);

					/*
                    if(debug) {
                        PolyMatchPoints.showRelative("trainContour", trainContour);
                        PolyMatchPoints.showRelative("testContour", testContour);
                    }
                    */
					
					distanceBar = compareContours(trainContour, testContour);
				}
				{
					List<Point.Double> trainContour = extractContour(template.getHandle());
					List<Point.Double> testContour = extractContour(handle);
					
					distanceHandle = compareContours(trainContour, testContour);
				}
			}
			
			// 3. pruning
			{
				if(distanceBar > 100f) {
					return failed();
				}
				
				if(distanceHandle > 100f) {
					return failed();
				}
			}
			
			// 4. variable binding
			{
				// FIXME if handle (big circle) is either on the left or right, the xOffset is computed incorrectly (center of handle marks position) (en)
				// FIXME normalize to interval [0,1] (en)
				position = ((handle.getBoundingBox().x + (handle.getSize().w / 2d)) - element.getBoundingBox().x) / element.getSize().w;
			}
			
			// 5. geometric mismatch
			{
				// TODO extract outer contour, compare with outer contour of bound template (en)
			}
		}
		
		// generative classification
		{
			Graph.Builder graph;
			
			// 1. parsing
			{
				graph = template.create(element.getSize(), position);
			}
			
			// 2. photometric mismatch
			{
				Image.Int realImage = BlobUtils.Cut.cutByMask(context.image, element);
				Image.Int synthImage = ImageUtil.Cut.trim(Util.render(graph.build()), new java.awt.Color(0, 0, 0, 0).getRGB());

                if(debug) {
                    VisualizerUtil.show("realImage", realImage);
                    VisualizerUtil.show("synthImage", synthImage);
                }
				
				int width = 100;
				int height = 100;
				
				Image.Int lumaRealImage = ImageUtil.Convert.toImageInt(YCrCb.y(realImage));
				Image.Int lumaSynthImage = ImageUtil.Convert.toImageInt(YCrCb.y(synthImage));

                if(debug) {
                    VisualizerUtil.show("realImage (luma)", lumaRealImage);
                    VisualizerUtil.show("synthImage (luma)", lumaSynthImage);
                }
				
				Image.Int grayRealImage = normalizeIntensity(lumaRealImage);
				Image.Int graySynthImage = normalizeIntensity(lumaSynthImage);
				
				BufferedImage mismatchImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				BufferedImage scaledRealImage = scale(ImageUtil.Convert.toBufferedImage(grayRealImage), width, height);
				BufferedImage scaledSynthImage = scale(ImageUtil.Convert.toBufferedImage(graySynthImage), width, height);

                if(debug) {
                    VisualizerUtil.show("realImage (scaled)", scaledRealImage);
                    VisualizerUtil.show("synthImage (scaled)", scaledSynthImage);
                }

				photometricMismatch = computeError(scaledSynthImage, scaledRealImage, mismatchImage);
			}
		}
		
		System.out.println(geometricMismatch + " " + photometricMismatch);
		
		// TODO impelement (en)
		return null;
	}
	
	private static Image.Int normalizeIntensity(Image.Int in) {
		Image.Int out = new Image.Int(in.w, in.h, Image.Int.Type.ARGB);
		// interval
		int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		for (int y = 0; y < in.h; y++) {
			for (int x = 0; x < in.w; x++) {
				int pixel = in.get(x, y);
				if (in.type == Type.RGB || getAlpha(pixel) != 0) {
					int intensity = toIntensity(pixel);
					min = Math.min(min, intensity);
					max = Math.max(max, intensity);
				}
			}
		}
		// normalize
		for (int y = 0; y < in.h; y++) {
			for (int x = 0; x < in.w; x++) {
				int pixel = in.get(x, y);
				if (in.type == Type.RGB || getAlpha(pixel) != 0) {
					int intensity = toIntensity(pixel);
					int scaled = toInt((intensity - min) * (255f / (max - min)));
					out.pixels[y * in.w + x] = ImageUtil.toInt(getAlpha(pixel), scaled, scaled, scaled);
				}
			}
		}
		return out;
	}

	private static final int getAlpha(int argb) {
		return (argb >> 24) & 0xff;
	}

	private final static int toInt(float value) {
		return (int) value;
	}

	private final static int toIntensity(int rgb) {
		int intensity = (rgb >> 16) & 0xff;
		return intensity;
	}
	
	private static void print(int[] values) {
		for(int value : values) {
			System.out.print(value + " ");
		}
		System.out.println();
	}
	
	private static float computeError(BufferedImage train,
	        BufferedImage test, BufferedImage mismatch) {
		float sum = 0f;
		int pixels = 0;
		for (int x = 0; x < train.getWidth(); x++) {
			for (int y = 0; y < train.getHeight(); y++) {
				if (isTransparent(test.getRGB(x, y)) == false) {
					int sampleRgb = train.getRGB(x, y);
					int candidateRgb = test.getRGB(x, y);
					float distance = distance(sampleRgb, candidateRgb);
					sum += distance;
					pixels++;
					mismatch.setRGB(x, y, new java.awt.Color(distance, distance, distance, 1f).getRGB());
				} else {
					mismatch.setRGB(x, y, java.awt.Color.blue.getRGB());
				}
			}
		}
		return (sum / pixels) * 100;
	}

	private static boolean isTransparent(int rgba) {
		int a = (rgba >> 24) & 0xff;
		return a != 255;
	}

	private static float distance(int rgb1, int rgb2) {
		int r1 = (rgb1 >> 16) & 0xff;
		int g1 = (rgb1 >> 8) & 0xff;
		int b1 = (rgb1 >> 0) & 0xff;

		int r2 = (rgb2 >> 16) & 0xff;
		int g2 = (rgb2 >> 8) & 0xff;
		int b2 = (rgb2 >> 0) & 0xff;

		return (Math.abs(r1 - r2) / 256f + Math.abs(g1 - g2) / 256f + Math
		        .abs(b1 - b2) / 256f) / 3f;
	}

	private static BufferedImage scale(BufferedImage src, int w, int h) {
		BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = dest.createGraphics();
		AffineTransform at = AffineTransform.getScaleInstance((double) w / src.getWidth(), (double) h / src.getHeight());
		g.drawRenderedImage(src, at);
		g.dispose();
		return dest;
	}
	
	private static double compareContours(List<Point.Double> trainContour, List<Point.Double> testContour) {
		return PolyMatch.match(trainContour, testContour);
	}
	
	private static List<Point.Double> extractContour(Mask element) {
		return Trace.contourTrace(element);
	}
	
	private static List<Point.Double> extractContour(Element element) {
		return ContourExtractor.getContour(element);
	}

	private static Mask locateHandle(final Mask element, final int[] splits, final Histogram histogram) {
		
		final int width = splits[1] - splits[0];
		final int height = element.getSize().h;
		
		final int x = splits[0];
		final int y = 0;
		
		return Mask.Builder.create(element, new Rectangle.Int(x, y, width, height));
	}
	
	private static Mask locateBar(final Mask element, final int[] splits, final Histogram histogram) {
		
		final int width = element.getSize().w;
		final int height = getHandleHeight(element, splits);
		
		final int x = 0;
		final int y = getHandleY(element, splits);
		
		return Mask.Builder.create(element, new Rectangle.Int(x, y, width, height));
	}
	
	private static int getHandleY(Mask element, int[] splits) {
		if(splits[0] > 1) {
			return getHandleY(element, 1);
		} else {
			return getHandleY(element, element.getSize().w - 2);
		}
	}
	
	private static int getHandleY(Mask element, int x) {
		int minY = Integer.MAX_VALUE;
		for(int y = 0; y < element.getSize().h; y++) {
			if(element.get(x, y)) {
				minY = Math.min(y, minY);
			}
		}
		return minY;
	}

	private static int getHandleHeight(Mask element, int[] splits) {
		if(splits[0] > 1) {
			return getHandleHeight(element, 1);
		} else {
			return getHandleHeight(element, element.getSize().w - 2);
		}
	}

	private static int getHandleHeight(Mask element, int x) {
		int height = 0;
		for(int y = 0; y < element.getSize().h; y++) {
			if(element.get(x, y)) {
				height++;
			}
		}
		return height;
	}

	private static int[] locateSplits(Histogram histogram) {
		final int UNSET = -1;

		int left = UNSET, right = UNSET;
		
		for(int x = 0; x < histogram.bins.length; x++) {
			if(left == UNSET && histogram.bins[x] > HEIGHT_HANDLE) {
				left = x;
			}
			
			if(left != UNSET && right == UNSET && histogram.bins[x] < HEIGHT_HANDLE) {
				right = x;
			}
		}
		
		return new int[] { left, right };
	}

	private static class Histogram {
		
		public final int[] bins;

		public Histogram(int[] bins) {
			this.bins = bins;
		}
	}

	private static Histogram getHistogram(Mask element) {
		int[] bins = new int[element.getSize().w];
		for(int x = 0; x < bins.length; x++) {
			int sum = 0;
			for(int y = 0; y < element.getSize().h; y++) {
				if(element.get(x, y)) {
					sum++;
				}
			}
			bins[x] = sum;
		}
		
		return new Histogram(bins);
	}

	private static Mask joinBlobs(Map<Blob, List<Blob>> neighbours, Blob blob) {
		
		List<Blob> blobs = Lists.toLinkedList(blob);
		
		if(neighbours.containsKey(blob)) {
			blobs.addAll(neighbours.get(blob));
		}
		
		return toElement(blobs);
	}

	private static Mask toElement(List<Blob> blobs) {
		return Mask.Builder.create(blobs);
	}
}
