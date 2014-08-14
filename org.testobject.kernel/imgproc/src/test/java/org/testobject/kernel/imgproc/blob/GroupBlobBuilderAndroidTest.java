package org.testobject.kernel.imgproc.blob;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Ignore;
import org.junit.Test;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.plot.Visualizer;
import org.testobject.kernel.imgproc.segmentation.Segmentation;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.commons.util.file.FileUtil;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.plot.VisualizerUtil;
import org.testobject.kernel.imgproc.blob.BlobUtils;
import org.testobject.kernel.imgproc.blob.GraphBlobBuilder;

public class GroupBlobBuilderAndroidTest {

	private static final String IMAGE = "android/4_0_3/pipeline/twitter/tweet_after.png";

	@Test
	@Ignore
	public void test() throws IOException {

		final BufferedImage input = ImageIO.read(FileUtil.readFileFromClassPath(IMAGE));
		final GraphBlobBuilder builder = new GraphBlobBuilder(input.getWidth(), input.getHeight());

		final long start = System.currentTimeMillis();
		final Blob[] blobs = builder.build(ImageUtil.toImage(input));
		final long end = System.currentTimeMillis();
		System.out.println(end - start);

		VisualizerUtil.show("Input", input, 1f);
		Visualizer.Renderable image = VisualizerUtil.toRenderable(ImageUtil.toBufferedImage(BlobUtils.drawHierarchy(blobs)));
		Visualizer.Renderable boxes = new Visualizer.Renderable() {
			@Override
			public void render(Visualizer.Graphics graphics) {
				for (Blob blob : blobs) {
					graphics.drawRect(blob.bbox.x, blob.bbox.y, blob.bbox.width, blob.bbox.height);
				}
			}
		};
		VisualizerUtil.show("Result", 1f, image, boxes);

		System.in.read();
	}

	@Test
	@Ignore
	public void segmentationTest() throws IOException {
		System.in.read();

		segment();

		//VisualizerUtil.show("Result", ImageUtil.toBufferedImage(result), 0.5f);
		System.in.read();
	}

	@Test
	@Ignore
	public void segmentationtest100() throws IOException {
		System.in.read();
		for (int i = 0; i < 100; i++) {
			segment();
		}
		System.in.read();
	}

	public static Image.Int segment() throws IOException {
		BufferedImage input = ImageIO.read(new File(IMAGE));

		long start = System.currentTimeMillis();

		Image.Int output = Segmentation.visualizeSegmentation(input);

		System.out.println("Segmentation: " + (System.currentTimeMillis() - start) + "ms");

		return output;
	}

}
