package org.testobject.kernel.ocr.tesseract;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.tess4j.TessAPI;
import net.sourceforge.tess4j.TessAPI.TessBaseAPI;
import net.sourceforge.tess4j.TessAPI.TessPageIterator;
import net.sourceforge.tess4j.TessAPI.TessPageIteratorLevel;
import net.sourceforge.tess4j.TessAPI.TessResultIterator;
import net.sourceforge.vietocr.ImageIOHelper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.kernel.ocr.OCR;

import com.google.common.base.Preconditions;
import com.sun.jna.Pointer;

/**
 * enijkamp changes:
 * 
 *   - fixed shutdown sequence for tesseract handle
 *   - fixed reference handling for image data (GC was collecting unreferenced image data)
 *   - factored out tess low-level access
 *   - replaced potentially dangerous tess setRectandle() by java crop-image code
 * 
 */
public class TesseractCopyRectOCR implements OCR {
	
	public static class Tess implements Closeable {

		private static final TessAPI api = TessAPI.INSTANCE;
		
		private final TessBaseAPI handle;
		
		// do not delete me
		// this keeps track of references so that the GC will not collect references which are passed out of the JVM lifecycle
		// (i.e. references which are passed to native code)
		private final List<ByteBuffer> bufferRefsAvoidGarbageCollector = new LinkedList<>();
		
		public Tess() {
			debug("tesseract version is '" + version() + "'");
			this.handle = api.TessBaseAPICreate();
			api.TessBaseAPIInit3(handle, "tessdata", "deu+eng");
			api.TessBaseAPISetPageSegMode(handle, TessAPI.TessPageSegMode.PSM_SINGLE_BLOCK);
			api.TessBaseAPISetVariable(handle, "tessedit_char_whitelist",
					"0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ?!.,:-");
		}
		
		@Override
		public void close() throws IOException {
			api.TessBaseAPIClear(handle);
			api.TessBaseAPIEnd(handle);
			api.TessBaseAPIDelete(handle);
		}
		
		public void recognize() {
			trace(Thread.currentThread().getId() + " TessBaseAPIRecognize");
			int ret = api.TessBaseAPIRecognize(handle, null);
			if(ret != 0) {
				log.error("recognize() failed");
				throw new IllegalStateException("tesseract recognize() failed");
			}
		}
		
		public BufferedImage setImage(BufferedImage image, Rectangle.Int region) throws IOException {

			trace(Thread.currentThread().getId() + " setImage");

			if (region != null) {
				if (isValidRectangle(region, image) == false) {
					throw new IllegalArgumentException("region invalid " + region.toString());
				}
				
				BufferedImage subimage = crop(image, region);
				setImage(subimage);
				return subimage;
				
			} else {
				setImage(image);
				return image;
			}
		}
		
		public void setRectangle(Rectangle.Int region) throws IOException {
			trace(Thread.currentThread().getId() + " setRectangle");
			api.TessBaseAPISetRectangle(handle, region.x, region.y, region.w, region.h);
		}
		
		private void setImage(BufferedImage image) throws IOException {
			
			int width = image.getWidth();
			int height = image.getHeight();

			int bitsPerPixel = image.getColorModel().getPixelSize();
			ByteBuffer imageByteBuffer = ImageIOHelper.getImageByteBuffer(image);

			int bytesPerPixel = bitsPerPixel / 8;
			int bytesPerLine = width * bytesPerPixel;
			
			bufferRefsAvoidGarbageCollector.add(imageByteBuffer);
			
			setImage(width, height, imageByteBuffer, bytesPerPixel, bytesPerLine);
		}

		private void setImage(int width, int height, ByteBuffer imageByteBuffer, int bytesPerPixel, int bytesPerLine) {
			debug(Thread.currentThread().getId() + " TessBaseApiSetImage");
			api.TessBaseAPISetImage(handle, imageByteBuffer, width, height, bytesPerPixel, bytesPerLine);
		}
		
		private BufferedImage crop(BufferedImage src, Rectangle.Int rect) {
		      BufferedImage dest = src.getSubimage(rect.x, rect.y, rect.w, rect.h);
		      return dest;
		   }

		private boolean isValidRectangle(Rectangle.Int textPosition, BufferedImage image) {
			if (textPosition.isEmpty()) {
				return false;
			}

			Rectangle.Int imageRectangle = new Rectangle.Int(image.getWidth(), image.getHeight());
			return imageRectangle.contains(textPosition);
		}

		public List<TextPosition> getTextPositions() {
			List<TextPosition> textPositions = new LinkedList<>();
			TessPageIterator pi = analyseLayout(handle);
			if (pi == null) {
				return textPositions;
			}
			try {
				api.TessPageIteratorBegin(pi);

				do {
					Rectangle.Int boundingBox = getIteratorBoundingBox(TessPageIteratorLevel.RIL_WORD, pi);
					textPositions.add(new TextPosition(boundingBox));
				} while (api.TessPageIteratorNext(pi, TessPageIteratorLevel.RIL_WORD) == TessAPI.TRUE);
			} finally {
				api.TessPageIteratorDelete(pi);
			}

			return textPositions;
		}
		
		private TessPageIterator analyseLayout(TessBaseAPI handle) {
			trace(Thread.currentThread().getId() + " TessBaseAPIAnalyseLayout");
			return api.TessBaseAPIAnalyseLayout(handle);
		}

		public List<Result> parseResults(int iteratorLevel) {
			TessResultIterator ri = api.TessBaseAPIGetIterator(handle);
			TessPageIterator pi = api.TessResultIteratorGetPageIterator(ri);

			List<OCR.Result> results = new LinkedList<>();

			try {
				iteratorBegin(pi);

				do {
					String text = getIteratorText(iteratorLevel, ri);
					float propability = getIteratorPropability(iteratorLevel, ri);
					Rectangle.Int boundingBox = getIteratorBoundingBox(iteratorLevel, pi);

					OCR.Result result = new OCR.Result(text, propability, boundingBox);
					debug("found result " + result);
					results.add(result);
				} while (api.TessPageIteratorNext(pi, iteratorLevel) == TessAPI.TRUE);
			} finally {
				api.TessPageIteratorDelete(pi);
			}

			return results;
		}

		public void iteratorBegin(TessPageIterator pi) {
			trace(Thread.currentThread().getId() + " TessPageIteratorBegin");
			api.TessPageIteratorBegin(pi);
		}

		public int iteratorNext(TessPageIterator pi) {
			trace(Thread.currentThread().getId() + " TessPageIteratorNext");
			return api.TessPageIteratorNext(pi, TessPageIteratorLevel.RIL_WORD);
		}

		public void iteratorDelete(TessPageIterator pi) {
			trace(Thread.currentThread().getId() + " TessPageIteratorDelete");
			api.TessPageIteratorDelete(pi);
		}

		public float getIteratorPropability(int iteratorLevel, TessResultIterator ri) {
			trace(Thread.currentThread().getId() + " getIteratorPropability");
			return api.TessResultIteratorConfidence(ri, iteratorLevel);
		}

		public Rectangle.Int getIteratorBoundingBox(int iteratorLevel, TessPageIterator pi) {
			
			IntBuffer leftB = IntBuffer.allocate(1);
			IntBuffer topB = IntBuffer.allocate(1);
			IntBuffer rightB = IntBuffer.allocate(1);
			IntBuffer bottomB = IntBuffer.allocate(1);

			trace(Thread.currentThread().getId() + " TessPageIteratorBoundingBox");
			api.TessPageIteratorBoundingBox(pi, iteratorLevel, leftB, topB, rightB, bottomB);

			int left = leftB.get();
			int top = topB.get();
			int right = rightB.get();
			int bottom = bottomB.get();
			
			return new Rectangle.Int(left, top, right - left, bottom - top);
		}

		public String getIteratorText(int iteratorLevel, TessResultIterator ri) {
			Pointer ptr = api.TessResultIteratorGetUTF8Text(ri, iteratorLevel);
			try {
				return ptr != null ? ptr.getString(0) : "";
			} finally {
				api.TessDeleteText(ptr);
			}
		}
		
		public String version() {
			return api.TessVersion();
		}
	}

	private static final boolean DEBUG = false;
	private static int invokes = 0;
	
	private static final Log log = LogFactory.getLog(TesseractCopyRectOCR.class);

	@Override
	public List<Result> getText(BufferedImage image, int dpi, double deviceScalingFactor) {
		return getText(image, null, dpi, deviceScalingFactor);
	}

	@Override
	public List<Result> getText(BufferedImage image, Rectangle.Int region, int dpi, double deviceScalingFactor) {
		if(DEBUG) {
			invokes++;
			System.out.println("invoke " + invokes);
		}

		try(Tess tess = new Tess()) {
			long start = System.currentTimeMillis();
			debug("searching text in region '" + region + "'");
			Rectangle.Int targetRegion = (region != null ? region : new Rectangle.Int(0, 0, image.getWidth(), image.getHeight()));
			List<Result> result = recognizeTextWithRegion(tess, image, targetRegion);
			debug("searching text in region '" + region + "' took " + (System.currentTimeMillis() - start) + "ms");
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public List<TextPosition> getTextPosition(BufferedImage image, int dpi, double scalingFactor) {
		try(Tess tess = new Tess()) {
			tess.setImage(image);
			
			return tess.getTextPositions();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private List<Result> recognizeTextWithRegion(Tess tess, BufferedImage image, Rectangle.Int targetRegion) throws IOException {
		
		Preconditions.checkNotNull(targetRegion);

		trace(Thread.currentThread().getId() + " recognizeTextWithRegion -> Region = " + targetRegion.toString() + ", Image = (" + image.getWidth() + ", " + image.getHeight() + ")");

		Rectangle.Int imageBox = new Rectangle.Int(0, 0, image.getWidth(), image.getHeight());
		BufferedImage subImage = tess.setImage(image, targetRegion);

		List<Result> results = new LinkedList<>();
		List<TextPosition> textRegions = tess.getTextPositions();

		trace(Thread.currentThread().getId() + " textPositions");
		for (TextPosition textPosition : textRegions) {
			trace(Thread.currentThread().getId() + "   " + textPosition.toString());
		}

		for (TextPosition textRegion : textRegions) {
			if (tess.isValidRectangle(textRegion.getBoundingBox(), subImage) == false) {
				log.error("invalid text position recognized '" + textRegion + "' for image '" + new Rectangle.Int(subImage.getWidth(), subImage.getHeight()) + "'");
				continue;
			}
			
			List<Result> localTextResults = recognize(tess, subImage, textRegion.getBoundingBox());
			List<Result> globalTextResults = translateResults(localTextResults, imageBox, targetRegion, textRegion.getBoundingBox());
			results.addAll(globalTextResults);
		}

		return results;
	}

	private List<Result> translateResults(List<Result> resultsIn, Rectangle.Int imageBox, Rectangle.Int imageRegion, Rectangle.Int textRegion) {
		List<Result> translatedOut = new ArrayList<Result>();
		
		for(Result result : resultsIn) {
			translatedOut.add(translate(result, imageBox, imageRegion, textRegion));
		}
		
		return translatedOut;
	}

	private Result translate(Result resultIn, Rectangle.Int imageBox, Rectangle.Int imageRegion, Rectangle.Int textRegion) {
		
		Rectangle.Int box = new Rectangle.Int();
		{
			box.x += (resultIn.getBoundingBox().x + imageRegion.x + textRegion.x);
			box.y += (resultIn.getBoundingBox().y + imageRegion.y + textRegion.y);
			box.w += (textRegion.w);
			box.h += (textRegion.h);
		}
		
		return new Result(resultIn.getText(), resultIn.getProbability(), box);
	}

	private List<Result> recognize(Tess tess, BufferedImage image, Rectangle.Int region) throws IOException {

		trace(Thread.currentThread().getId() + " recognize BEGIN " + region.toString());
		tess.setImage(image, region);
		tess.recognize();
		trace(Thread.currentThread().getId() + " recognize END " + region.toString());
		
		trace(Thread.currentThread().getId() + " parseResults BEGIN " + region.toString());
		List<Result> results = tess.parseResults(TessPageIteratorLevel.RIL_WORD);
		trace(Thread.currentThread().getId() + " parseResults END " + region.toString());
		
		return results;
	}

	private static void debug(String msg) {
		if(log.isDebugEnabled()) {
			log.debug(msg);
		}
	}
	
	private static void trace(String msg) {
		if(log.isTraceEnabled()) {
			log.trace(msg);
		}
	}

}
