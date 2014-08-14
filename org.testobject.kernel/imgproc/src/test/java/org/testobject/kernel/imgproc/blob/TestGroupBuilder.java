package org.testobject.kernel.imgproc.blob;

import static org.junit.Assert.assertThat;
import static org.testobject.kernel.imgproc.blob.TestUtils.init;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.testobject.kernel.imgproc.blob.*;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.commons.util.file.FileUtil;

public class TestGroupBuilder {
	@Test
	public void testEmpty() {
		@SuppressWarnings("unchecked")
		List<Group<Blob>> groups = new GroupBuilder<Blob>().buildGroups(Collections.EMPTY_LIST, 0, 0);

		assertThat(groups.size(), CoreMatchers.equalTo(0));
	}

	@Test
	public void testTrivial() {
		ArrayRaster raster = init(new int[][] { { 1 } });

		Blob root = new LinearBlobBuilder().build(raster);

		List<Group<Blob>> groups = new GroupBuilder<Blob>().buildGroups(root.children, 0, 0);

		assertThat(groups.size(), CoreMatchers.equalTo(1));
	}

	@Test
	public void testXGroups() {
		ArrayRaster raster = init(new int[][] { { 1, 0, 1 } });

		Blob root = new LinearBlobBuilder().build(raster);

		List<Group<Blob>> groups = new GroupBuilder<Blob>().buildGroups(root.children, 0, 0);
		assertThat(groups.size(), CoreMatchers.equalTo(2));

		groups = new GroupBuilder<Blob>().buildGroups(root.children, 0, 1);
		assertThat(groups.size(), CoreMatchers.equalTo(2));

		groups = new GroupBuilder<Blob>().buildGroups(root.children, 2, 0);
		assertThat(groups.size(), CoreMatchers.equalTo(1));
	}

	@Test
	public void testBigXGroups() {
		ArrayRaster raster = init(new int[][] { { 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1 } });

		Blob root = new LinearBlobBuilder().build(raster);

		List<Group<Blob>> groups = new GroupBuilder<Blob>().buildGroups(root.children, 0, 0);
		assertThat(groups.size(), CoreMatchers.equalTo(7));

		groups = new GroupBuilder<Blob>().buildGroups(root.children, 0, 1);
		assertThat(groups.size(), CoreMatchers.equalTo(7));

		groups = new GroupBuilder<Blob>().buildGroups(root.children, 2, 0);
		assertThat(groups.size(), CoreMatchers.equalTo(2));
	}

	@Test
	public void testRealistic() {
		ArrayRaster raster = init(new int[][] { { 1, 1, 1, 0, 1, 1, 1, 0, 1, 0, 0, 1, 0, 1 }, { 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1 } });

		Blob root = new LinearBlobBuilder().build(raster);
		assertThat(root.children.size(), CoreMatchers.equalTo(5));

		List<Group<Blob>> groups = new GroupBuilder<Blob>().buildGroups(root.children, 0, 0);
		assertThat(groups.size(), CoreMatchers.equalTo(5));

		groups = new GroupBuilder<Blob>().buildGroups(root.children, 0, 1);
		assertThat(groups.size(), CoreMatchers.equalTo(5));

		groups = new GroupBuilder<Blob>().buildGroups(root.children, 2, 0);
		assertThat(groups.size(), CoreMatchers.equalTo(2));

		assertThat(groups.get(0).getBoundingBox().x, CoreMatchers.equalTo(0));
		assertThat(groups.get(0).getBoundingBox().y, CoreMatchers.equalTo(0));
		assertThat(groups.get(0).getBoundingBox().width, CoreMatchers.equalTo(9));
		assertThat(groups.get(0).getBoundingBox().height, CoreMatchers.equalTo(2));

		assertThat(groups.get(1).getBoundingBox().x, CoreMatchers.equalTo(11));
		assertThat(groups.get(1).getBoundingBox().y, CoreMatchers.equalTo(0));
		assertThat(groups.get(1).getBoundingBox().width, CoreMatchers.equalTo(3));
		assertThat(groups.get(1).getBoundingBox().height, CoreMatchers.equalTo(2));
	}

	static List<Blob> randomize(List<Blob> source, long seed) {
		ArrayList<Blob> out = new ArrayList<Blob>(source);

		Random random = new Random(seed);

		for (int i = 0; i < out.size() - 1; i++) {
			int target = i + 1 + random.nextInt(out.size() - i - 1);
			Blob t = out.get(target);
			out.set(target, out.get(i));
			out.set(i, t);
		}

		return out;
	}

	@Test
	public void testBug() {
		ArrayRaster raster = init(new int[][] {
				{ 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0 },
				{ 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1 } });

		Blob root = new LinearBlobBuilder().build(raster);
		assertThat(root.children.size(), CoreMatchers.equalTo(15));

		List<Group<Blob>> groups = new GroupBuilder<Blob>().buildGroups(randomize(root.children, 2), 0, 0);
		assertThat(groups.size(), CoreMatchers.equalTo(15));

		groups = new GroupBuilder<Blob>().buildGroups(root.children, 0, 1);
		assertThat(groups.size(), CoreMatchers.equalTo(15));

		groups = new GroupBuilder<Blob>().buildGroups(root.children, 2, 0);
		assertThat(groups.size(), CoreMatchers.equalTo(2));

		assertThat(groups.get(0).getBoundingBox().x, CoreMatchers.equalTo(0));
		assertThat(groups.get(0).getBoundingBox().y, CoreMatchers.equalTo(0));
		assertThat(groups.get(0).getBoundingBox().width, CoreMatchers.equalTo(13));
		assertThat(groups.get(0).getBoundingBox().height, CoreMatchers.equalTo(2));

		assertThat(groups.get(1).getBoundingBox().x, CoreMatchers.equalTo(15));
		assertThat(groups.get(1).getBoundingBox().y, CoreMatchers.equalTo(0));
		assertThat(groups.get(1).getBoundingBox().width, CoreMatchers.equalTo(15));
		assertThat(groups.get(1).getBoundingBox().height, CoreMatchers.equalTo(2));
	}

	@Test
	public void testLetterI() {
		ArrayRaster raster = init(new int[][] { { 1 }, { 0 }, { 1 }, { 1 }, { 1 }, });

		Blob root = new LinearBlobBuilder().build(raster);
		assertThat(root.children.size(), CoreMatchers.equalTo(2));

		List<Group<Blob>> groups = new GroupBuilder<Blob>().buildGroups(root.children, 0, 0);
		assertThat(groups.size(), CoreMatchers.equalTo(2));

		groups = new GroupBuilder<Blob>().buildGroups(root.children, new Insets(1, 0, 0, 0));
		assertThat(groups.size(), CoreMatchers.equalTo(2));

		groups = new GroupBuilder<Blob>().buildGroups(root.children, new Insets(0, 1, 0, 0));
		assertThat(groups.size(), CoreMatchers.equalTo(2));

		groups = new GroupBuilder<Blob>().buildGroups(root.children, new Insets(0, 1, 0, 1));
		assertThat(groups.size(), CoreMatchers.equalTo(2));

		groups = new GroupBuilder<Blob>().buildGroups(root.children, new Insets(1, 0, 1, 1));
		assertThat(groups.size(), CoreMatchers.equalTo(1));

		assertThat(groups.get(0).getBoundingBox().x, CoreMatchers.equalTo(0));
		assertThat(groups.get(0).getBoundingBox().y, CoreMatchers.equalTo(0));
		assertThat(groups.get(0).getBoundingBox().width, CoreMatchers.equalTo(1));
		assertThat(groups.get(0).getBoundingBox().height, CoreMatchers.equalTo(5));
	}

	private static void detectAllXGroups(GroupBuilder<Blob> bld, Blob root, int threshold) {
		for (Blob c : root.children) {
			detectAllXGroups(bld, c, threshold);
		}

		bld.buildGroups(root.children, threshold, 0);
	}

	public static ArrayRaster readRasterFromImage(String filename, int threshold) throws IOException, InterruptedException {
		BufferedImage image = ImageIO.read(FileUtil.readFileFromSystem(filename));
		final BufferedImage bwImage = ImageUtil.toBlackWhite(image, threshold);
		final Dimension size = new Dimension(image.getWidth(), image.getHeight());
		final int WHITE = Color.WHITE.getRGB();
		final boolean[][] bwRaster = new boolean[size.height][size.width];
		for (int x = 0; x < size.width; x++) {
			for (int y = 0; y < size.height; y++) {
				bwRaster[y][x] = bwImage.getRGB(x, y) == WHITE;
			}
		}

		return new ArrayRaster(bwRaster, size);
	}

	public static void main(String[] args) throws Throwable {
		final ArrayRaster raster = readRasterFromImage("android/4_0_3/screenshots/gdocs.png", 100);

		Blob root = new LinearBlobBuilder().build(raster);

		GroupBuilder<Blob> bld = new GroupBuilder<Blob>();

		long total = 0L;
		for (int i = 0; i < 150; i++) {
			long start = System.currentTimeMillis();
			detectAllXGroups(bld, root, 2);
			long time = System.currentTimeMillis() - start;
			total += time;
		}

		System.out.println(total / 150 + "ms");
		System.out.println("the biggest interval tree size was: " + bld.getMaxTreeSize());

	}
}
