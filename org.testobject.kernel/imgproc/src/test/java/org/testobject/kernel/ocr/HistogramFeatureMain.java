package org.testobject.kernel.ocr;

import static org.testobject.commons.util.collections.Lists.toList;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.BlobUtils;
import org.testobject.kernel.imgproc.blob.GraphBlobBuilder;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.kernel.ocr.freetype.FT2Library;
import org.testobject.commons.util.file.FileUtil;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.Image.Int;
import org.testobject.kernel.imgproc.plot.VisualizerUtil;
import org.testobject.kernel.ocr.*;
import org.testobject.kernel.ocr.freetype.FreeTypeGrayscaleFontRenderer;

public class HistogramFeatureMain {

	private static final FreeTypeGrayscaleFontRenderer fontRenderer = new FreeTypeGrayscaleFontRenderer(160, 240,
			FT2Library.FT_LCD_FILTER_DEFAULT);

	public static void main(String... args) throws IOException {

		debugHighLevel();
		debugLowLevel();

		System.in.read();
	}

	public static void debugHighLevel() throws IOException {
		char[] chars = "w".toCharArray();
		int[] fontSize = { 18 };

		File font = FileUtil.toFileFromSystem("android/4_0_3/fonts/sans-serif/Roboto-Bold.ttf");

		int histogramN = 7;

		double MAX_DIST = 1.75d; // 2d; // swing
		int MAX_TOLERANCE_X = 2;

		Image.Int image = ImageUtil.read(ClassLoader.getSystemResourceAsStream("classifier/text/positives/4_0_3/Tweet.png"));
		List<List<File>> fonts = Collections.singletonList(Collections.singletonList(font));
		List<AdditiveMask> additiveMasks = new MaskClusterer(fontRenderer, histogramN, 1400f, 800f).generateMasksFast(fonts, fontSize,
				chars);

		Blob[] blobs = new GraphBlobBuilder(image.w, image.h).build(image);
		// VisualizerUtil.show(BlobUtils.drawHierarchy(blobs));
		showBlobs(image, true, blobs);

		TextRecognizer.Match<Blob> match = new SlidingMaskTextRecognizer(additiveMasks, histogramN, MAX_DIST, MAX_TOLERANCE_X).recognize(image,
				toList(blobs[3]));

		System.out.println(match);
	}

	public static void debugLowLevel() throws IOException {

		Image.Int trainImage = ImageUtil.read("classifier/text/positives/4_0_3/Tweet.png");
		File font = FileUtil.toFileFromSystem("android/4_0_3/fonts/sans-serif/Roboto-Regular.ttf");

		Image.Int testImage = ImageUtil.toImage(fontRenderer.drawChar(font,
				16f,
				's'));

		Blob[] blobs = new GraphBlobBuilder(trainImage.w, trainImage.h).build(trainImage);
		VisualizerUtil.show(BlobUtils.drawHierarchy(blobs));
		showBlobs(trainImage, true, blobs);

		Blob blob = blobs[5];

		VisualizerUtil.show("test", testImage, 20f);
		VisualizerUtil.show("train", trainImage, 20f);

		Decolorizer decolorizer = new Decolorizer();

		Int testDecolorized = decolorizer.decolorize(testImage, MaskClusterer.toRaster(testImage));
		Int trainDecolorized = decolorizer.decolorize(trainImage, blob);
		trainDecolorized = ImageUtil.crop(trainDecolorized, new Rectangle(10, trainDecolorized.h));
		VisualizerUtil.show("test decolorized", testDecolorized, 20f);
		VisualizerUtil.show("train decolorized", trainDecolorized, 20f);

		double[][] trainHistogram = HistogramFeature.computeHistogram(ImageUtil.toImageByte(testDecolorized), 7);
		double[][] testHistogram = HistogramFeature.computeHistogram(ImageUtil.toImageByte(trainDecolorized), 7);

		HistogramFeature.plotHistograms("test", testHistogram);
		HistogramFeature.plotHistograms("train", trainHistogram);

		double distance = HistogramFeature.distance(trainHistogram, testHistogram);

		System.out.println(distance);
		System.in.read();
	}

	private static void showBlobs(Image.Int image, boolean enabled, Blob... blobs) {
		if (!enabled) {
			return;
		}

		for (int i = 0; i < blobs.length; i++) {
			VisualizerUtil.show(i + " index", ImageUtil.crop(image, blobs[i].bbox));
		}
	}
}
