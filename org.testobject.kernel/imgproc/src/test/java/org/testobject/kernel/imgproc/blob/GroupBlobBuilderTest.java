package org.testobject.kernel.imgproc.blob;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.junit.Test;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.kernel.imgproc.blob.BlobUtils;
import org.testobject.kernel.imgproc.blob.GraphBlobBuilder;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

public class GroupBlobBuilderTest {

	@Test
	public void segmentSingleRectangle() {
		final int width = 50, height = 50;
		final BufferedImage test = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		{
			final Graphics g = test.getGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, width, height);
		}

		final GraphBlobBuilder builder = new GraphBlobBuilder(width, height);

		final Blob root = builder.build(ImageUtil.toImage(test))[0];
		assertThat(root.id, is(0));
		assertThat(root.bbox, is(new Rectangle(0, 0, 50, 50)));
		assertThat(root.children.size(), is(1));

		final Blob outer = root.children.get(0);
		assertThat(outer.bbox, is(new Rectangle(0, 0, 50, 50)));
		assertThat(outer.children.size(), is(0));
	}

	@Test
	public void segmentTwoNestedRectangles() {
		final int width = 50, height = 50;
		final BufferedImage test = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		{
			final Graphics g = test.getGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, width, height);
			g.setColor(Color.RED);
			g.fillRect(10, 10, 30, 30);
		}

		final GraphBlobBuilder builder = new GraphBlobBuilder(width, height);

		final Blob root = builder.build(ImageUtil.toImage(test))[0];
		assertThat(root.children.size(), is(1));
		assertThat(root.bbox, is(new Rectangle(0, 0, 50, 50)));

		final Blob outer = root.children.get(0);
		assertThat(outer.children.size(), is(1));
		assertThat(outer.bbox, is(new Rectangle(0, 0, 50, 50)));

		final Blob inner = outer.children.get(0);
		assertThat(inner.children.size(), is(0));
		assertThat(inner.bbox, is(new Rectangle(10, 10, 30, 30)));
	}

	@Test
	public void segmentFiveNestedRectangles() {
		final int width = 100, height = 100;
		final BufferedImage test = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		{
			final Graphics g = test.getGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, width, height);
			g.setColor(Color.RED);
			g.fillRect(10, 10, 40, 40);
			g.setColor(Color.GREEN);
			g.fillRect(10, 50, 40, 40);
			g.setColor(Color.BLUE);
			g.fillRect(50, 10, 40, 40);
			g.setColor(Color.YELLOW);
			g.fillRect(50, 50, 40, 40);
		}

		final GraphBlobBuilder builder = new GraphBlobBuilder(width, height);

		final Blob root = builder.build(ImageUtil.toImage(test))[0];
		assertThat(root.children.size(), is(1));

		//VisualizerUtil.show("blah", test, 1f);
		BlobUtils.printBlob(root, 0);

		assertThat(root.bbox, is(new Rectangle(0, 0, 100, 100)));

		final Blob outer = root.children.get(0);
		assertThat(outer.children.size(), is(4));
		assertThat(root.bbox, is(new Rectangle(0, 0, 100, 100)));

		final Blob inner0 = outer.children.get(0);
		assertThat(inner0.children.size(), is(0));

		final Blob inner1 = outer.children.get(1);
		assertThat(inner1.children.size(), is(0));

		final Blob inner2 = outer.children.get(2);
		assertThat(inner2.children.size(), is(0));

		final Blob inner3 = outer.children.get(3);
		assertThat(inner3.children.size(), is(0));
	}

}
