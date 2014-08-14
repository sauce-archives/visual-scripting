//package org.testobject.kernel.ocr.tesseract;
//
//import java.awt.image.BufferedImage;
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.nio.IntBuffer;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.concurrent.locks.ReentrantLock;
//
//import net.sourceforge.tess4j.TessAPI;
//import net.sourceforge.tess4j.TessAPI.TessBaseAPI;
//import net.sourceforge.tess4j.TessAPI.TessPageIterator;
//import net.sourceforge.tess4j.TessAPI.TessPageIteratorLevel;
//import net.sourceforge.tess4j.TessAPI.TessResultIterator;
//import net.sourceforge.vietocr.ImageIOHelper;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.testobject.commons.math.algebra.Rectangle;
//import org.testobject.kernel.ocr.OCR;
//
//import com.sun.jna.Pointer;
//
///**
// * DO NOT use this class in production. It's has issues with the image reference being garbage collected while tesseract is still operating on it.
// * 
// * @author enijkamp
// *
// */
//public class TesseractOCR implements OCR {
//
//	private static final ReentrantLock lock = new ReentrantLock();
//
//	private static final Log log = LogFactory.getLog(TesseractOCR.class);
//	
//	private static final boolean DEBUG = false;
//
//	private final TessAPI api = TessAPI.INSTANCE;
//	
//	public TesseractOCR() {
//		log.info("tesseract version is '" + api.TessVersion() + "'");
//	}
//
//	@Override
//	public List<Result> getText(BufferedImage image, int dpi, double deviceScalingFactor) {
//		return getText(image, null, dpi, deviceScalingFactor);
//	}
//
//	@Override
//	public List<Result> getText(BufferedImage image, Rectangle.Int region, int dpi, double deviceScalingFactor) {
//		
//		lock.lock();
//		try {
//			final TessBaseAPI handle = api.TessBaseAPICreate();
//			api.TessBaseAPIInit3(handle, "tessdata", "deu+eng");
//			api.TessBaseAPISetPageSegMode(handle, TessAPI.TessPageSegMode.PSM_SINGLE_BLOCK);
//			api.TessBaseAPISetVariable(handle, "tessedit_char_whitelist",
//					"0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ?!.,:-");
//			
//			try {
//				long start = System.currentTimeMillis();
//				if (region != null) {
//					log.debug("searching text in region: " + region);
//					List<Result> result = recognizeTextWithRegion(image, region, handle);
//					log.debug("searching text in region: " + region + " took " + (System.currentTimeMillis() - start) + "ms");
//					return result;
//				} else {
//					return recognizeText(image, handle);
//				}
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			} finally {
//				api.TessBaseAPIClear(handle);
//				api.TessBaseAPIDelete(handle);
//			}
//		} finally {
//			lock.unlock();
//		}
//	}
//
//	private List<Result> parseResults(int iteratorLevel, TessBaseAPI handle) {
//		TessResultIterator ri = api.TessBaseAPIGetIterator(handle);
//		TessPageIterator pi = api.TessResultIteratorGetPageIterator(ri);
//
//		List<OCR.Result> results = new LinkedList<>();
//
//		try {
//			api.TessPageIteratorBegin(pi);
//
//			do {
//				String text = getIteratorText(iteratorLevel, ri);
//				float propability = getIteratorPropability(iteratorLevel, ri);
//				Rectangle.Int boundingBox = getIteratorBoundingBox(iteratorLevel, pi);
//
//				OCR.Result result = new OCR.Result(text, propability, boundingBox);
//				log.debug("found result " + result);
//				results.add(result);
//			} while (api.TessPageIteratorNext(pi, iteratorLevel) == TessAPI.TRUE);
//		} finally {
//			//			api.TessPageIteratorDelete(pi);
//			api.TessResultIteratorDelete(ri);
//		}
//
//		return results;
//	}
//
//	private Rectangle.Int getIteratorBoundingBox(int iteratorLevel, TessPageIterator pi) {
//		IntBuffer leftB = IntBuffer.allocate(1);
//		IntBuffer topB = IntBuffer.allocate(1);
//		IntBuffer rightB = IntBuffer.allocate(1);
//		IntBuffer bottomB = IntBuffer.allocate(1);
//
//		api.TessPageIteratorBoundingBox(pi, iteratorLevel, leftB, topB, rightB, bottomB);
//
//		int left = leftB.get();
//		int top = topB.get();
//		int right = rightB.get();
//		int bottom = bottomB.get();
//		return new Rectangle.Int(left, top, right - left, bottom - top);
//	}
//
//	private float getIteratorPropability(int iteratorLevel, TessResultIterator ri) {
//		return api.TessResultIteratorConfidence(ri, iteratorLevel);
//	}
//
//	private String getIteratorText(int iteratorLevel, TessResultIterator ri) {
//		Pointer ptr = api.TessResultIteratorGetUTF8Text(ri, iteratorLevel);
//		try {
//			return ptr != null ? ptr.getString(0) : "";
//		} finally {
//			api.TessDeleteText(ptr);
//		}
//	}
//
//	private List<Result> recognizeText(BufferedImage image, TessBaseAPI handle) throws IOException {
//		int width = image.getWidth();
//		int height = image.getHeight();
//
//		int bitsPerPixel = image.getColorModel().getPixelSize();
//		ByteBuffer imageByteBuffer = ImageIOHelper.getImageByteBuffer(image);
//
//		int bytesPerPixel = bitsPerPixel / 8;
//		int bytesPerLine = width * bytesPerPixel;
//
//		TessBaseApiSetImage(handle, width, height, imageByteBuffer, bytesPerPixel, bytesPerLine);
//
//		List<Result> results = new LinkedList<>();
//		List<Rectangle.Int> textPositions = getTextPositions(handle);
//		for (Rectangle.Int textPosition : textPositions) {
//			if (isValidRectangle(textPosition, image) == false) {
//				log.error("invalid text position recognized: " + textPosition + " for image: "
//						+ new Rectangle.Int(image.getWidth(), image.getHeight()));
//				continue;
//			}
//			recognize(handle, textPosition);
//
//			List<Result> textResults = parseResults(TessPageIteratorLevel.RIL_WORD, handle);
//			results.addAll(textResults);
//		}
//
//		return results;
//	}
//
//	private List<Result> recognizeTextWithRegion(BufferedImage image, Rectangle.Int region, TessBaseAPI handle) throws IOException {
//		
//		int width = image.getWidth();
//		int height = image.getHeight();
//
//		int bitsPerPixel = image.getColorModel().getPixelSize();
//		ByteBuffer imageByteBuffer = ImageIOHelper.getImageByteBuffer(image);
//
//		int bytesPerPixel = bitsPerPixel / 8;
//		int bytesPerLine = width * bytesPerPixel;
//
//		if(DEBUG) {
//			System.out.println(Thread.currentThread().getId() + " Region = " + region.toString() + ", Image = (" + width + ", " + height + ")");
//		}
//
//		setImage(image, region, handle, width, height, imageByteBuffer, bytesPerPixel, bytesPerLine);
//
//		List<Result> results = new LinkedList<>();
//		List<Rectangle.Int> textPositions = getTextPositions(handle);
//
//		if(DEBUG) {
//			System.out.println(Thread.currentThread().getId() + " textPositions");
//			for (Rectangle.Int textPosition : textPositions) {
//				System.out.println(Thread.currentThread().getId() + "   " + textPosition.toString());
//			}
//		}
//
//		for (Rectangle.Int textPosition : textPositions) {
//			if (isValidRectangle(textPosition, image) == false) {
//				log.error("invalid text position recognized: " + textPosition + " for image: "
//						+ new Rectangle.Int(image.getWidth(), image.getHeight()));
//				continue;
//			}
//			recognize(handle, textPosition);
//
//			List<Result> textResults = parseResults(TessPageIteratorLevel.RIL_WORD, handle);
//			results.addAll(textResults);
//		}
//
//		return results;
//	}
//
//	private void recognize(TessBaseAPI handle, Rectangle.Int textPosition) {
//		if(DEBUG) {
//			System.out.println(Thread.currentThread().getId() + " recognize BEGIN " + textPosition.toString());
//		}
//		TessBaseAPISetRectangle(textPosition, handle);
//		TessBaseAPIRecognize(handle);
//		if(DEBUG) {
//			System.out.println(Thread.currentThread().getId() + " recognize END " + textPosition.toString());
//		}
//	}
//
//	private void setImage(BufferedImage image, Rectangle.Int region, TessBaseAPI handle, int width, int height, ByteBuffer imageByteBuffer,
//			int bytesPerPixel, int bytesPerLine) {
//
//		if(DEBUG) {
//			System.out.println(Thread.currentThread().getId() + " setImage");
//		}
//		TessBaseApiSetImage(handle, width, height, imageByteBuffer, bytesPerPixel, bytesPerLine);
//
//		if (region != null) {
//			if (isValidRectangle(region, image) == false) {
//				throw new IllegalArgumentException("region invalid " + region.toString());
//			}
//			TessBaseAPISetRectangle(region, handle);
//		}
//	}
//
//	private void TessBaseAPIRecognize(TessBaseAPI handle) {
//		if(DEBUG) {
//			System.out.println(Thread.currentThread().getId() + " TessBaseAPIRecognize");
//		}
//		api.TessBaseAPIRecognize(handle, null);
//	}
//
//	private void TessBaseAPISetRectangle(Rectangle.Int region, TessBaseAPI handle) {
//		if(DEBUG) {
//			System.out.println(Thread.currentThread().getId() + " TessBaseAPISetRectangle");
//		}
//		api.TessBaseAPISetRectangle(handle, region.x, region.y, region.w, region.h);
//	}
//
//	private void TessBaseApiSetImage(TessBaseAPI handle, int width, int height, ByteBuffer imageByteBuffer, int bytesPerPixel,
//			int bytesPerLine) {
//		if(DEBUG) {
//			System.out.println(Thread.currentThread().getId() + " TessBaseApiSetImage");
//		}
//		api.TessBaseAPISetImage(handle, imageByteBuffer, width, height, bytesPerPixel, bytesPerLine);
//	}
//
//	private boolean isValidRectangle(Rectangle.Int textPosition, BufferedImage image) {
//		if (textPosition.isEmpty()) {
//			return false;
//		}
//
//		Rectangle.Int imageRectangle = new Rectangle.Int(image.getWidth(), image.getHeight());
//		return imageRectangle.contains(textPosition);
//	}
//
//	private List<Rectangle.Int> getTextPositions(TessBaseAPI handle) {
//		List<Rectangle.Int> textPositions = new LinkedList<>();
//		TessPageIterator pi = api.TessBaseAPIAnalyseLayout(handle);
//		if (pi == null) {
//			return textPositions;
//		}
//		try {
//			api.TessPageIteratorBegin(pi);
//
//			do {
//				Rectangle.Int boundingBox = getIteratorBoundingBox(TessPageIteratorLevel.RIL_WORD, pi);
//				textPositions.add(boundingBox);
//				log.debug("found layout " + boundingBox);
//			} while (api.TessPageIteratorNext(pi, TessPageIteratorLevel.RIL_WORD) == TessAPI.TRUE);
//		} finally {
//			api.TessPageIteratorDelete(pi);
//		}
//
//		return textPositions;
//	}
//
//}
