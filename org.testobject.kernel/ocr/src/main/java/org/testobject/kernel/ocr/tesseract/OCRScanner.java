package org.testobject.kernel.ocr.tesseract;

import java.awt.Insets;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import net.sourceforge.tess4j.TessAPI.TessPageIteratorLevel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.kernel.imaging.segmentation.Group;
import org.testobject.kernel.imaging.segmentation.GroupBuilder;
import org.testobject.kernel.ocr.OCR;
import org.testobject.kernel.ocr.OCR.Result;
import org.testobject.kernel.ocr.OCR.TextGroup;
import org.testobject.kernel.ocr.OCR.TextPosition;
import org.testobject.kernel.ocr.tesseract.TesseractCopyRectOCR.Tess;

public class OCRScanner implements Closeable {

	private static final Log log = LogFactory.getLog(OCRScanner.class);

	private final int threadCount;
	private final AtomicBoolean closed = new AtomicBoolean();
	private final WeakHashMap<BufferedImage, BufferedImage> scaledImages = new WeakHashMap<>();
	private final BlockingQueue<TesseractCopyRectOCR.Tess> tessQueue = new LinkedBlockingQueue<TesseractCopyRectOCR.Tess>();

	public OCRScanner(int threadCount) {
		this.threadCount = threadCount;
		for (int i = 0; i < threadCount; i++) {
			tessQueue.add(new TesseractCopyRectOCR.Tess());
		}
	}

	public List<TextPosition> getTextPosition(BufferedImage image, int dpi, double deviceScalingFactor) {
		Tess tess = tessQueue.poll();

		double scalingFactor = (double) 320 * deviceScalingFactor / (double) dpi;

		image = scale(image, scalingFactor);

		try {
			tess.setImage(image, null);
			tess.recognize();
			tess.parseResults(TessPageIteratorLevel.RIL_WORD);

			List<TextPosition> scaledtextPositions = tess.getTextPositions();
			List<TextPosition> textPositions = scaleTextPositionsInverse(scaledtextPositions, scalingFactor);

			GroupBuilder<TextPosition> groupBuilder = new GroupBuilder<>();
			List<Group<TextPosition>> groups = groupBuilder.buildGroups(textPositions, new Insets(10, 10, 10, 10));

			Set<TextPosition> mergedTextPositions = new HashSet<>(textPositions);
			for (Group<OCR.TextPosition> group : groups) {
				mergedTextPositions.add(new TextGroup(group));
			}
			
			return new ArrayList<OCR.TextPosition>(mergedTextPositions);

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			tessQueue.add(tess);
		}
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

	public List<Result> recognize(BufferedImage screenshot, Rectangle.Int textPosition, int dpi, double deviceScalingFactor)
			throws IOException {
		if (closed.get()) {
			throw new IllegalStateException("scanner was already closed");
		}
		if (textPosition.isEmpty()) {
			return null;
		}
		Tess tess = tessQueue.poll();

		double scalingFactor = (double) 320 * deviceScalingFactor / (double) dpi;

		try {
			BufferedImage scaledImage = scale(screenshot, scalingFactor);
			Rectangle.Int scaledRectangle = scale(textPosition, scalingFactor);

			tess.setImage(scaledImage, scaledRectangle);
			tess.recognize();

			return scaleResultsInverse(scaledRectangle.x, scaledRectangle.y, tess.parseResults(TessPageIteratorLevel.RIL_WORD),
					scalingFactor);
		} finally {
			tessQueue.add(tess);
		}
	}

	@Override
	public void close() throws IOException {
		closed.set(true);

		for (int i = 0; i < threadCount; i++) {
			try {
				Tess tess = tessQueue.take();
				tess.close();
			} catch (Throwable e) {
				log.error("error while closing ocr scanner", e);
			}
		}
	}

	private Rectangle.Int scale(Rectangle.Int in, double scale) {
		return in != null ? new Rectangle.Int(scale(in.x, scale), scale(in.y, scale), scale(in.w, scale), scale(in.h, scale)) : null;
	}

	private int scale(int value, double scale) {
		return (int) Math.floor(value * scale);
	}

	private BufferedImage scale(BufferedImage image, double scale) {
		BufferedImage scaledImage = scaledImages.get(image);
		if (scaledImage == null) {
			AffineTransform at = new AffineTransform();
			at.scale(scale, scale);

			scaledImage = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC).filter(image, null);
			scaledImages.put(image, scaledImage);
		}
		return scaledImage;
	}

	private List<Result> scaleResultsInverse(int xOffset, int yOffset, List<Result> results, double scale) {
		List<Result> scaledResults = new ArrayList<>(results.size());
		for (Result result : results) {
			Rectangle.Int boundingBox = result.getBoundingBox();
			Rectangle.Int scaledBoundingBox = new Rectangle.Int(
					scaleInverse(xOffset + boundingBox.x, scale),
					scaleInverse(yOffset + boundingBox.y, scale),
					scaleInverse(boundingBox.w, scale),
					scaleInverse(boundingBox.h, scale)
					);
			scaledResults.add(new Result(result.getText(), result.getProbability(), scaledBoundingBox));
		}
		return scaledResults;
	}

	private static int scaleInverse(double value, double scale) {
		return (int) Math.floor(value / scale);
	}
}
