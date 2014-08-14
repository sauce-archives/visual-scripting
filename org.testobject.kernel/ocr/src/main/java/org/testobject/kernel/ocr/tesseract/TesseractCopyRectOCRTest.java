package org.testobject.kernel.ocr.tesseract;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import net.sourceforge.tess4j.TessAPI.TessPageIteratorLevel;

import org.junit.Test;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.distances.StringDistances;
import org.testobject.commons.util.distances.StringDistances.Distance;
import org.testobject.kernel.imaging.segmentation.Group;
import org.testobject.kernel.imaging.segmentation.GroupBuilder;
import org.testobject.kernel.imaging.segmentation.HasBoundingBox;
import org.testobject.kernel.ocr.OCR;
import org.testobject.kernel.ocr.OCR.Result;
import org.testobject.kernel.ocr.OCR.TextPosition;
import org.testobject.kernel.ocr.tesseract.TesseractCopyRectOCR.Tess;

import com.google.common.base.Stopwatch;

public class TesseractCopyRectOCRTest {

	private static final String QUERY_STRING = "Tap below to select seats";

	private static final int THREADS = 2;
	private static final double SCALE = 1;
	private static final String FILE = "/home/aluedeke/Desktop/before.png";
	private static final String FILE_TEXTS = "/home/aluedeke/Desktop/before.texts.png";
	private static final String FILE_BOXES = "/home/aluedeke/Desktop/before.boxes.png";

	private final ExecutorService executor = Executors.newFixedThreadPool(THREADS);

	Stopwatch w = new Stopwatch();

	@Test
	public void test() throws IOException, InterruptedException, ExecutionException {
		TesseractCopyRectOCR.Tess ocr = new TesseractCopyRectOCR.Tess();

		w.start();
		Stopwatch w6 = new Stopwatch().start();
		BufferedImage image = ImageIO.read(new File(FILE));
		final BufferedImage scaledImage = scale(image, SCALE);
//		final BufferedImage scaledImage = ImageUtil.scale(image, (int) (image.getWidth() * SCALE), (int) (image.getHeight() * SCALE));
		System.out.println("timing scaling: " + w6.stop().elapsedMillis());

		Stopwatch w5 = new Stopwatch().start();
		ocr.setImage(scaledImage, new Rectangle.Int(scaledImage.getWidth(), scaledImage.getHeight()));
		List<OCR.TextPosition> textPositions = ocr.getTextPositions();
		System.out.println("timing getTextPositions: " + w5.stop().elapsedMillis());

		Stopwatch w4 = new Stopwatch().start();
		GroupBuilder<OCR.TextPosition> groupBuilder = new GroupBuilder<>();
		List<Group<OCR.TextPosition>> groups = groupBuilder.buildGroups(textPositions, new Insets(10, 10, 10, 10));
		System.out.println("timing grouping: " + w4.stop().elapsedMillis());

		Set<OCR.TextPosition> mergedTextPositions = new HashSet<>(textPositions);
		for (Group<OCR.TextPosition> group : groups) {
			mergedTextPositions.add(new OCR.TextGroup(group));
		}

		Stopwatch w3 = new Stopwatch().start();

		final ConcurrentLinkedQueue<Tess> tessQueue = new ConcurrentLinkedQueue<TesseractCopyRectOCR.Tess>();
		for (int i = 0; i < THREADS; i++) {
			executor.submit(new Runnable() {
				@Override
				public void run() {
					try {
						TesseractCopyRectOCR.Tess tess = new TesseractCopyRectOCR.Tess();
						tess.setImage(scaledImage, null);
						tessQueue.add(tess);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			});
		}

		System.out.println("timing setup tess: " + w3.stop().elapsedMillis());

		System.out.println("merge textpositions: " + mergedTextPositions.size() + " testpositions: " + textPositions.size() + " groups: "
				+ groups.size());

		Stopwatch w2 = new Stopwatch().start();
		Map<OCR.TextPosition, Future<List<Result>>> textFutures = new HashMap<>();
		for (final TextPosition textPosition : mergedTextPositions) {
			if (textPosition.getBoundingBox().w < 10 || textPosition.getBoundingBox().h < 10) {
				continue;
			}
			Future<List<Result>> future = executor.submit(new Callable<List<Result>>() {
				@Override
				public List<Result> call() throws Exception {
					Tess tess = tessQueue.poll();

					try {
						tess.setRectangle(textPosition.getBoundingBox());
						tess.recognize();

						return tess.parseResults(TessPageIteratorLevel.RIL_WORD);
					} finally {
						tessQueue.add(tess);
					}
				}
			});

			textFutures.put(textPosition, future);
		}
		System.out.println("timing queuing: " + w2.stop().elapsedMillis());

		Stopwatch w1 = new Stopwatch().start();
		OCR.Result bestResult = new OCR.Result("", 0f, null);
		for (Entry<TextPosition, Future<List<Result>>> textFuture : textFutures.entrySet()) {
			{
				List<Result> results = textFuture.getValue().get();

				Result result = toResult(results, QUERY_STRING.length());
				if (result != null && result.getProbability() > bestResult.getProbability()) {
					bestResult = result;
				}
			}

			if (textFuture instanceof OCR.TextGroup) {
				OCR.TextGroup textGroup = (OCR.TextGroup) textFuture;
				List<Result> results = toResults(textGroup.getTextElements(), textFutures);

				Result result = toResult(results, QUERY_STRING.length());
				if (result != null && result.getProbability() > bestResult.getProbability()) {
					bestResult = result;
				}
			}
		}

		System.out.println("timing results: " + w1.stop().elapsedMillis());

		System.out.println("BestResult: " + bestResult);

		w.stop();

		render(textPositions, FILE_TEXTS);
		render(groups, FILE_BOXES);

		System.out.println(w.elapsedTime(TimeUnit.MILLISECONDS));
	}

	private List<Result> toResults(List<TextPosition> textElements, Map<TextPosition, Future<List<Result>>> textFutures)
			throws InterruptedException, ExecutionException {
		List<Result> results = new LinkedList<OCR.Result>();
		for (TextPosition textElement : textElements) {
			results.addAll(textFutures.get(textElement).get());
		}

		return results;
	}

	private void render(List<? extends HasBoundingBox> textPositions, String file) throws IOException {
		BufferedImage image = scale(ImageIO.read(new File(FILE)), SCALE);
		Graphics g = image.getGraphics();
		g.setColor(Color.RED);
		for (HasBoundingBox hasBoundingBox : textPositions) {
			Rectangle.Int bbox = hasBoundingBox.getBoundingBox();
			g.drawRect(bbox.x, bbox.y, bbox.w, bbox.h);
		}

		ImageIO.write(image, "PNG", new File(file));
	}

	private BufferedImage scale(BufferedImage image, double scale) {
		AffineTransform at = new AffineTransform();
		at.scale(scale, scale);

		return new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC).filter(image, null);
	}

	private static String toString(Iterable<OCR.Result> group) {
		String resultString = "";
		for (OCR.Result result : group) {
			if (result.getText().trim().isEmpty()) {
				continue;
			}
			resultString += result.getText() + " ";
		}
		return resultString.isEmpty() ? resultString : resultString.substring(0, resultString.length() - 1);
	}

	private OCR.Result toResult(List<Result> results, int length) {
		String text = toString(results);
		Result[] resultMap = toResultMap(results, text);
		Distance distance = StringDistances.getSubstringNormalizedDistance(QUERY_STRING, text);
		if (distance.propability == 0 || distance.position == -1) {
			return null;
		}

		Set<OCR.Result> resultSet = new LinkedHashSet<OCR.Result>();
		for (int i = distance.position; i < distance.position + length; i++) {
			resultSet.add(resultMap[i]);
		}

		Rectangle.Int bbox = resultSet.iterator().next().getBoundingBox();
		for (OCR.Result result : resultSet) {
			bbox = bbox.union(result.getBoundingBox());
		}

		String matchingText = toString(resultSet);

		return new OCR.Result(matchingText, distance.propability, bbox);
	}

	private static OCR.Result[] toResultMap(Iterable<OCR.Result> group, String grouptext) {
		OCR.Result[] resultMap = new OCR.Result[grouptext.length() + 1];
		int position = 0;
		for (OCR.Result result : group) {
			if (result.getText().trim().isEmpty()) {
				continue;
			}
			int fromIndex = position;
			int toIndex = position + result.getText().length() + 1;
			Arrays.fill(resultMap, fromIndex, toIndex, result);
			position = toIndex;
		}

		return resultMap;
	}

}
