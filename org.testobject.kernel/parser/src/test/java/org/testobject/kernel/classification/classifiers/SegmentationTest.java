package org.testobject.kernel.classification.classifiers;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.junit.Test;
import org.testobject.commons.tools.plot.VisualizerUtil;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.commons.util.io.FileUtil;
import org.testobject.kernel.imaging.color.contrast.Quantize;
import org.testobject.kernel.imaging.segmentation.Blob;
import org.testobject.kernel.imaging.segmentation.BlobUtils;
import org.testobject.kernel.imaging.segmentation.GraphBlobBuilder;

/**
 * 
 * @author enijkamp
 *
 */
public class SegmentationTest {

    public static final boolean debug = Debug.toDebugMode(false);
	
	@Test
	public void lowPass() throws IOException {
		Image.Int image = readImage("screenshots/device/kaufda/angebote2.png");
		
		Image.Int quantized = Quantize.quantize(image, 4);	
		
		if(debug) VisualizerUtil.show("quantize", quantized);
		
		{
			final float sigma = 0f;
			final double threshold = 20000d;
			
			GraphBlobBuilder builder = new GraphBlobBuilder(image.w, image.h, sigma, threshold);
			
			Blob[] blobs = builder.build(image);
	
			// render
            if(debug)
            {
				Image.Int hierarchy = new Image.Int(image.w, image.h);
				BlobUtils.Draw.drawHierarchy(blobs, hierarchy);
				VisualizerUtil.show("graph (0, 20000)", ImageUtil.Convert.toBufferedImage(hierarchy), 1f);
			}
		}
		
		{
			final float sigma = 0.1f;
			final double threshold = 20000d;
			
			long start = System.currentTimeMillis();
			GraphBlobBuilder builder = new GraphBlobBuilder(image.w, image.h, sigma, threshold);
			Blob[] blobs = builder.build(image);
			System.out.println(System.currentTimeMillis() - start);
	
			// render
            if(debug)
            {
				Image.Int hierarchy = new Image.Int(image.w, image.h);
				BlobUtils.Draw.drawHierarchy(blobs, hierarchy);
				VisualizerUtil.show("graph (0.1, 20000)", ImageUtil.Convert.toBufferedImage(hierarchy), 1f);
			}
		}
		
		{
			Image.Int scaled = ImageUtil.Convert.toImage(scale(ImageUtil.Convert.toBufferedImage(image), image.w / 2, image.h / 2));
			
			final float sigma = 0.1f;
			final double threshold = 10000d;
			
			
			long start = System.currentTimeMillis();
			GraphBlobBuilder builder = new GraphBlobBuilder(scaled.w, scaled.h, sigma, threshold);
			Blob[] blobs = builder.build(scaled);
			System.out.println(System.currentTimeMillis() - start);
	
			// render
            if(debug)
            {
				Image.Int hierarchy = new Image.Int(scaled.w, scaled.h);
				BlobUtils.Draw.drawHierarchy(blobs, hierarchy);
				VisualizerUtil.show("graph (scaled)", ImageUtil.Convert.toBufferedImage(hierarchy), 1f);
			}
		}

        if(debug) System.in.read();
	}
	
	private static BufferedImage scale(BufferedImage src, int w, int h) {
		BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = dest.createGraphics();
		AffineTransform at = AffineTransform.getScaleInstance((double) w / src.getWidth(), (double) h / src.getHeight());
		g.drawRenderedImage(src, at);
		g.dispose();
		return dest;
	}
	
	@Test
	public void performance() throws IOException {
		Image.Int image = readImage("screenshots/device/kaufda/angebote2.png");
		
		long timeA = 0, timeB = 0;
		
		for(int i = 0; i < 100; i++) {
			
			final float sigma = 0.5f;
			final double threshold = 40000d;
			
			long start = System.currentTimeMillis();
			GraphBlobBuilder builder = new GraphBlobBuilder(image.w, image.h, sigma, threshold);
			builder.build(image);
			long end = System.currentTimeMillis();
			timeA += end - start;
		}
		
		for(int i = 0; i < 100; i++) {
			
			final float sigma = 0.5f;
			final double threshold = 20000d;
			
			long start = System.currentTimeMillis();
			GraphBlobBuilder builder = new GraphBlobBuilder(image.w, image.h, sigma, threshold);
			builder.build(image);
			long end = System.currentTimeMillis();
			timeB += end - start;
		}
		
		System.out.println(timeA);
		System.out.println(timeB);
	}
	
	private static org.testobject.commons.util.image.Image.Int readImage(String file) throws IOException {
		return ImageUtil.Read.read(FileUtil.readFileFromClassPath("android/4_0_3/" + file));
	}

}
