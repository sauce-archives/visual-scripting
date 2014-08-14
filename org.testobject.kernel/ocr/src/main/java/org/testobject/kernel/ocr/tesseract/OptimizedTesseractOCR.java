package org.testobject.kernel.ocr.tesseract;

import java.awt.Insets;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.kernel.imaging.segmentation.Group;
import org.testobject.kernel.imaging.segmentation.GroupBuilder;
import org.testobject.kernel.ocr.OCR;

public class OptimizedTesseractOCR implements OCR {
	
	private final Map<BufferedImage, BufferedImage> scaledImages = new WeakHashMap<>();
	
	private final OCR ocr = new TesseractCopyRectOCR();

	//	UnsharpMaskFilter filter = new UnsharpMaskFilter(2.5f, 15, 20);

	@Override
	public List<Result> getText(BufferedImage image, int dpi, double deviceScalingFactor) {
		return getText(image, null, dpi, deviceScalingFactor);
	}

	@Override
	public List<Result> getText(BufferedImage image, Rectangle.Int region, int dpi, double deviceScalingFactor) {
		double scalingFactor = (double) 320 * deviceScalingFactor / (double) dpi;

		image = scale(image, scalingFactor);
		Rectangle.Int regionScaled = scale(region, scalingFactor);

		List<Result> text = ocr.getText(image, regionScaled, dpi, deviceScalingFactor);
		return scaleResultsInverse(text, scalingFactor);
	}
	
	@Override
	public List<TextPosition> getTextPosition(BufferedImage image, int dpi, double deviceScalingFactor) {
		double scalingFactor = (double) 320 * deviceScalingFactor / (double) dpi;

		image = scale(image, scalingFactor);

		List<TextPosition> scaledtextPositions = ocr.getTextPosition(image, dpi, scalingFactor);
		List<TextPosition> textPositions = scaleTextPositionsInverse(scaledtextPositions, scalingFactor);

		GroupBuilder<TextPosition> groupBuilder = new GroupBuilder<>();
		List<Group<TextPosition>> groups = groupBuilder.buildGroups(textPositions, new Insets(10, 10, 10, 10));

		Set<TextPosition> mergedTextPositions = new HashSet<>(textPositions);
		for (Group<OCR.TextPosition> group : groups) {
			mergedTextPositions.add(new TextGroup(group));
		}
		
		return new ArrayList<OCR.TextPosition>(mergedTextPositions);
	}

	private BufferedImage scale(BufferedImage image, double scale) {
		BufferedImage scaledImage = scaledImages.get(image);
		if(scaledImage == null){
			AffineTransform at = new AffineTransform();
			at.scale(scale, scale);
			
			scaledImage = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC).filter(image, null);
			scaledImages.put(image, scaledImage);
		}
		return scaledImage;
	}

	private Rectangle.Int scale(Rectangle.Int in, double scale) {
		return in != null ? new Rectangle.Int(scale(in.x, scale), scale(in.y, scale), scale(in.w, scale), scale(in.h, scale)) : null;
	}

	private int scale(int value, double scale) {
		return (int) Math.floor(value * scale);
	}

	private List<Result> scaleResultsInverse(List<Result> results, double scale) {
		List<Result> scaledResults = new ArrayList<>(results.size());
		for (Result result : results) {
			Rectangle.Int boundingBox = result.getBoundingBox();
			Rectangle.Int scaledBoundingBox = new Rectangle.Int(scaleInverse(boundingBox.x, scale), scaleInverse(boundingBox.y, scale),
					scaleInverse(boundingBox.w, scale), scaleInverse(boundingBox.h, scale));
			scaledResults.add(new Result(result.getText(), result.getProbability(), scaledBoundingBox));
		}
		return scaledResults;
	}
	
	private List<TextPosition> scaleTextPositionsInverse(Collection<TextPosition> textPositions, double scale) {
		List<TextPosition> scaledResults = new ArrayList<>(textPositions.size());
		for (TextPosition textPosition : textPositions) {
			Rectangle.Int boundingBox = textPosition.getBoundingBox();
			Rectangle.Int scaledBoundingBox = new Rectangle.Int(scaleInverse(boundingBox.x, scale), scaleInverse(boundingBox.y, scale),
					scaleInverse(boundingBox.w, scale), scaleInverse(boundingBox.h, scale));
			scaledResults.add(new TextPosition(scaledBoundingBox));
		}
		return scaledResults;
	}

	private static int scaleInverse(double value, double scale) {
		return (int) Math.floor(value / scale);
	}

}
