package org.testobject.kernel.imaging.segmentation;

import java.util.Arrays;
import java.util.LinkedList;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.kernel.imaging.filters.GaussianFilter;
import org.testobject.commons.util.sort.IntroSort;

/**
 * http://www.cs.brown.edu/~pff/papers/seg-ijcv.pdf
 * 
 */
public class GraphBlobBuilder {

	public static final int ROOT = 0, UNDEFINED = -1;
	public static final float SIGMA = 0.5f;
	public static final double THRESHOLD = 4500d;
	public static final int MIN_BLOB_SIZE = 20;

	private final int min_size;
	private final float sigma;
	private final double threshold;

	private final int width, height;

	private final double[][] r_original, g_original, b_original;
	private final double[][] r_gauss, g_gauss, b_gauss;

	private final GaussianFilter gaussianFilter;

	private final int[] aPoints, bPoints;
	private final double[] distances;

	private final int[] ps, ranks, sizes;
	private final double[] thresholds;
	private int num_head, num_tail;
	
	public GraphBlobBuilder(int width, int height) {
		this(width, height, SIGMA, THRESHOLD, MIN_BLOB_SIZE);
	}
	
	public GraphBlobBuilder(int width, int height, float sigma, double threshold) {
		this(width, height, sigma, threshold, MIN_BLOB_SIZE);
	}
	
	public GraphBlobBuilder(int width, int height, float sigma, double threshold, int min_size) {
		this.sigma = sigma;
		this.threshold = threshold;
		this.min_size = min_size;
		
		this.width = width;
		this.height = height;

		this.r_original = new double[height][width];
		this.g_original = new double[height][width];
		this.b_original = new double[height][width];

		this.r_gauss = new double[height][width];
		this.g_gauss = new double[height][width];
		this.b_gauss = new double[height][width];

		// FIXME factor out gaussian filter -> pre-processing step (en)
		this.gaussianFilter = new GaussianFilter(width, height);

		this.aPoints = new int[width * height * 4];
		this.bPoints = new int[width * height * 4];
		this.distances = new double[width * height * 4];

		this.ps = new int[height * width];
		this.ranks = new int[height * width];
		this.sizes = new int[height * width];
		this.thresholds = new double[height * width];
	}

	public Blob[] build(org.testobject.commons.util.image.Image.Int input) {
		
		readColorChannels(input);
		
		smoothChannels();

		num_head = 0;
		num_tail = (width * height * 4) - 1;

		computeDiffForInnerPixels();
		computeDiffForFirstRowPixels();
		computeDiffForLastRowPixels();
		computeDiffForLastColumnPixels();

		IntroSort.sort(aPoints, bPoints, distances, 0, num_head - 1);

		int numberOfSegments = ps.length;

		resetComponents();

		numberOfSegments = connectComponents(numberOfSegments, num_head, num_tail);

		numberOfSegments = connectSmallComponents(numberOfSegments, num_head);
		
		return createHierarchy(ps, numberOfSegments);
	}

	private void computeDiffForInnerPixels() {
		int a_point, b_point;
		double o_distance, g_distance;

		for (int y = 1; y < height - 1; y++) {
			final double[] r_g_row_0 = r_gauss[y - 1], g_g_row_0 = g_gauss[y - 1], b_g_row_0 = b_gauss[y - 1];
			final double[] r_g_row_1 = r_gauss[y + 0], g_g_row_1 = g_gauss[y + 0], b_g_row_1 = b_gauss[y + 0];
			final double[] r_g_row_2 = r_gauss[y + 1], g_g_row_2 = g_gauss[y + 1], b_g_row_2 = b_gauss[y + 1];

			final double[] r_o_row_0 = r_original[y - 1], g_o_row_0 = g_original[y - 1], b_o_row_0 = b_original[y - 1];
			final double[] r_o_row_1 = r_original[y + 0], g_o_row_1 = g_original[y + 0], b_o_row_1 = b_original[y + 0];
			final double[] r_o_row_2 = r_original[y + 1], g_o_row_2 = g_original[y + 1], b_o_row_2 = b_original[y + 1];

			for (int x = 0; x < width - 1; x++) {
				final double r_g_0 = r_g_row_1[x], r_o_0 = r_o_row_1[x];
				final double g_g_0 = g_g_row_1[x], g_o_0 = g_o_row_1[x];
				final double b_g_0 = b_g_row_1[x], b_o_0 = b_o_row_1[x];

				// compute diff for pixel (x + 1,y + 0)
				{
					a_point = y * width + x;
					b_point = y * width + (x + 1);
					o_distance = diff(r_o_0, r_o_row_1[x + 1], g_o_0, g_o_row_1[x + 1], b_o_0, b_o_row_1[x + 1]);
					g_distance = diff(r_g_0, r_g_row_1[x + 1], g_g_0, g_g_row_1[x + 1], b_g_0, b_g_row_1[x + 1]);
					setDistance(a_point, b_point, o_distance, g_distance);
				}

				// compute diff for pixel (x + 0,y + 1)
				{
					a_point = y * width + x;
					b_point = (y + 1) * width + x;
					o_distance = diff(r_o_0, r_o_row_2[x], g_o_0, g_o_row_2[x], b_o_0, b_o_row_2[x]);
					g_distance = diff(r_g_0, r_g_row_2[x], g_g_0, g_g_row_2[x], b_g_0, b_g_row_2[x]);
					setDistance(a_point, b_point, o_distance, g_distance);
				}

				// compute diff for pixel (x + 1,y + 1)
				{
					a_point = y * width + x;
					b_point = (y + 1) * width + (x + 1);
					o_distance = diff(r_o_0, r_o_row_2[x + 1], g_o_0, g_o_row_2[x + 1], b_o_0, b_o_row_2[x + 1]);
					g_distance = diff(r_g_0, r_g_row_2[x + 1], g_g_0, g_g_row_2[x + 1], b_g_0, b_g_row_2[x + 1]);
					setDistance(a_point, b_point, o_distance, g_distance);
				}

				// compute diff for pixel (x + 1,y - 1)
				{
					a_point = y * width + x;
					b_point = (y - 1) * width + (x + 1);
					o_distance = diff(r_o_0, r_o_row_0[x + 1], g_o_0, g_o_row_0[x + 1], b_o_0, b_o_row_0[x + 1]);
					g_distance = diff(r_g_0, r_g_row_0[x + 1], g_g_0, g_g_row_0[x + 1], b_g_0, b_g_row_0[x + 1]);
					setDistance(a_point, b_point, o_distance, g_distance);
				}
			}
		}
	}

	private void computeDiffForFirstRowPixels() {
		int a_point, b_point;
		double o_distance, g_distance;

		final int y = 0;

		final double[] r_g_row_1 = r_gauss[y + 0], g_g_row_1 = g_gauss[y + 0], b_g_row_1 = b_gauss[y + 0];
		final double[] r_g_row_2 = r_gauss[y + 1], g_g_row_2 = g_gauss[y + 1], b_g_row_2 = b_gauss[y + 1];

		final double[] r_o_row_1 = r_original[y + 0], g_o_row_1 = g_original[y + 0], b_o_row_1 = b_original[y + 0];
		final double[] r_o_row_2 = r_original[y + 1], g_o_row_2 = g_original[y + 1], b_o_row_2 = b_original[y + 1];

		for (int x = 0; x < width - 1; x++) {
			final double r_g_0 = r_g_row_1[x], r_o_0 = r_o_row_1[x];
			final double g_g_0 = g_g_row_1[x], g_o_0 = r_o_row_1[x];
			final double b_g_0 = b_g_row_1[x], b_o_0 = r_o_row_1[x];

			// compute diff for pixel (x + 1,y + 0)
			{
				a_point = y * width + x;
				b_point = y * width + (x + 1);
				o_distance = diff(r_o_0, r_o_row_1[x + 1], g_o_0, g_o_row_1[x + 1], b_o_0, b_o_row_1[x + 1]);
				g_distance = diff(r_g_0, r_g_row_1[x + 1], g_g_0, g_g_row_1[x + 1], b_g_0, b_g_row_1[x + 1]);
				setDistance(a_point, b_point, o_distance, g_distance);
			}

			// compute diff for pixel (x + 0,y + 1)
			{
				a_point = y * width + x;
				b_point = (y + 1) * width + x;
				o_distance = diff(r_o_0, r_o_row_2[x], g_o_0, g_o_row_2[x], b_o_0, b_o_row_2[x]);
				g_distance = diff(r_g_0, r_g_row_2[x], g_g_0, g_g_row_2[x], b_g_0, b_g_row_2[x]);
				setDistance(a_point, b_point, o_distance, g_distance);
			}

			// compute diff for pixel (x + 1,y + 1)
			{
				a_point = y * width + x;
				b_point = (y + 1) * width + (x + 1);
				o_distance = diff(r_o_0, r_o_row_2[x + 1], g_o_0, g_o_row_2[x + 1], b_o_0, b_o_row_2[x + 1]);
				g_distance = diff(r_g_0, r_g_row_2[x + 1], g_g_0, g_g_row_2[x + 1], b_g_0, b_g_row_2[x + 1]);
				setDistance(a_point, b_point, o_distance, g_distance);
			}
		}
	}

	private void computeDiffForLastRowPixels() {
		int a_point, b_point;
		double o_distance, g_distance;

		final int y = height - 1;

		final double[] r_g_row_0 = r_gauss[y - 1], g_g_row_0 = g_gauss[y - 1], b_g_row_0 = b_gauss[y - 1];
		final double[] r_g_row_1 = r_gauss[y + 0], g_g_row_1 = g_gauss[y + 0], b_g_row_1 = b_gauss[y + 0];

		final double[] r_o_row_0 = r_original[y - 1], g_o_row_0 = g_original[y - 1], b_o_row_0 = b_original[y - 1];
		final double[] r_o_row_1 = r_original[y + 0], g_o_row_1 = g_original[y + 0], b_o_row_1 = b_original[y + 0];

		for (int x = 0; x < width - 1; x++) {
			final double r_g_0 = r_g_row_1[x], r_o_0 = r_o_row_1[x];
			final double g_g_0 = g_g_row_1[x], g_o_0 = r_o_row_1[x];
			final double b_g_0 = b_g_row_1[x], b_o_0 = r_o_row_1[x];

			// compute diff for pixel (x + 1,y + 0)
			{
				a_point = y * width + x;
				b_point = y * width + (x + 1);
				o_distance = diff(r_o_0, r_o_row_1[x + 1], g_o_0, g_o_row_1[x + 1], b_o_0, b_o_row_1[x + 1]);
				g_distance = diff(r_g_0, r_g_row_1[x + 1], g_g_0, g_g_row_1[x + 1], b_g_0, b_g_row_1[x + 1]);
				setDistance(a_point, b_point, o_distance, g_distance);
			}

			// compute diff for pixel (x + 1,y - 1)
			{
				a_point = y * width + x;
				b_point = (y - 1) * width + (x + 1);
				o_distance = diff(r_o_0, r_o_row_0[x + 1], g_o_0, g_o_row_0[x + 1], b_o_0, b_o_row_0[x + 1]);
				g_distance = diff(r_g_0, r_g_row_0[x + 1], g_g_0, g_g_row_0[x + 1], b_g_0, b_g_row_0[x + 1]);
				setDistance(a_point, b_point, o_distance, g_distance);
			}
		}
	}

	private void computeDiffForLastColumnPixels() {
		int a_point, b_point;
		double o_distance, g_distance;

		final int x = width - 1;

		for (int y = 1; y < height - 1; y++) {
			final double[] r_g_row_1 = r_gauss[y + 0], g_g_row_1 = g_gauss[y + 0], b_g_row_1 = b_gauss[y + 0];
			final double[] r_g_row_2 = r_gauss[y + 1], g_g_row_2 = g_gauss[y + 1], b_g_row_2 = b_gauss[y + 1];

			final double[] r_o_row_1 = r_original[y + 0];
			final double[] r_o_row_2 = r_original[y + 1], g_o_row_2 = g_original[y + 1], b_o_row_2 = b_original[y + 1];

			final double r_g_0 = r_g_row_1[x], r_o_0 = r_o_row_1[x];
			final double g_g_0 = g_g_row_1[x], g_o_0 = r_o_row_1[x];
			final double b_g_0 = b_g_row_1[x], b_o_0 = r_o_row_1[x];

			// compute diff for pixel (x + 0,y + 1)
			{
				a_point = y * width + x;
				b_point = (y + 1) * width + x;
				o_distance = diff(r_o_0, r_o_row_2[x], g_o_0, g_o_row_2[x], b_o_0, b_o_row_2[x]);
				g_distance = diff(r_g_0, r_g_row_2[x], g_g_0, g_g_row_2[x], b_g_0, b_g_row_2[x]);
				setDistance(a_point, b_point, o_distance, g_distance);
			}

		}
	}

	private final void setDistance(int a_point, int b_point, double o_distance, double g_distance) {
		final double distance = o_distance <= g_distance ? o_distance : g_distance;
		if (distance != 0) {
			setDistance(a_point, b_point, distance, num_head++);
		} else {
			setDistance(a_point, b_point, distance, num_tail--);
		}
	}

	private Blob[] createHierarchy(int[] pixel, int numberOfSegments) {

		// TODO normalize segmentation ids in previous code (en)
		int counter = 1;
		final int[] segments = new int[pixel.length];
		for (int i = 0; i < pixel.length; i++) {
			if (pixel[i] == i) {
				segments[i] = counter++;
			}
		}

		final int[] segment_size = new int[counter];

		// compute the bounding boxes of blobs
		final int[] segment_top = new int[counter];
		Arrays.fill(segment_top, height);
		final int[] segment_bottom = new int[counter];
		final int[] segment_left = new int[counter];
		Arrays.fill(segment_left, width);
		final int[] segment_right = new int[counter];

		final int[][] ids = new int[height][width];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final int i = segments[find(pixel, y * width + x)];
				ids[y][x] = i;
				segment_size[i]++;
				if (y < segment_top[i]) {
					segment_top[i] = y;
				}
				if (y > segment_bottom[i]) {
					segment_bottom[i] = y;
				}
				if (x < segment_left[i]) {
					segment_left[i] = x;
				}
				if (x > segment_right[i]) {
					segment_right[i] = x;
				}
			}
		}

		final int[] parents = new int[counter];
		Arrays.fill(parents, UNDEFINED);

		// iterate over left and right columns (root blob contains these)
		for (int y = 0; y < height; y++) {
			final int rightChild = ids[y][0];
			parents[rightChild] = GraphBlobBuilder.ROOT;

			final int leftChild = ids[y][width - 1];
			parents[leftChild] = GraphBlobBuilder.ROOT;
		}

		// iterate over top and bottom rows (root blob contains these)
		for (int x = 0; x < width; x++) {
			final int bottomChild = ids[0][x];
			parents[bottomChild] = GraphBlobBuilder.ROOT;

			final int topChild = ids[height - 1][x];
			parents[topChild] = GraphBlobBuilder.ROOT;
		}

		// iterate over remaining pixels
		for (int y = 0; y < height - 1; y++) {
			final int[] row_y0 = ids[y + 0];
			final int[] row_y1 = ids[y + 1];
			for (int x = 0; x < width - 1; x++) {
				final int parent = row_y0[x];
				// row y+0
				{
					final int child = row_y0[x + 1];
					if (parent != child) {
						setParent(child, parent, parents);
					}
				}
				// row y+1
				{
					final int child = row_y1[x + 0];
					if (parent != child) {
						setParent(child, parent, parents);
					}
				}
			}
		}

		// extract blobs
		final Blob[] blobs = new Blob[counter];
		blobs[0] = new Blob(ROOT, new Rectangle.Int(0, 0, width, height), 0, new LinkedList<Blob>(), ids);

		for (int i = 1; i < blobs.length; i++) {
			final int width = segment_right[i] - segment_left[i] + 1;
			final int height = segment_bottom[i] - segment_top[i] + 1;
			final Rectangle.Int bbox = new Rectangle.Int(segment_left[i], segment_top[i], width, height);
			blobs[i] = new Blob(i, bbox, segment_size[i], new LinkedList<Blob>(), ids);
		}
		for (int i = 1; i < blobs.length; i++) {
			blobs[parents[i]].children.add(blobs[i]);
		}

		return blobs;
	}

	private void setParent(int child, int parent, int[] parents) {
		// initialize parent
		if (parents[child] == UNDEFINED) {
			parents[child] = parent;
			return;
		}

		// same parents tells child multiple times "I'm your father"
		if (parents[child] == parent) {
			return;
		}
		
		// child is the parent
		for(int parentParent = parent; parentParent != UNDEFINED; parentParent = parents[parentParent]) {
			if(parentParent == child) {
				parents[parent] = child;
				return;
			}
		}

		// second parent 'tells' that he is the childs father, thus look for convergence of parent paths
		for (int childParent = child; childParent != UNDEFINED; childParent = parents[childParent]) {
			for (int parentParent = parent; parentParent != UNDEFINED; parentParent = parents[parentParent]) {
				if (childParent == parentParent && parents[parent] != child && child != parentParent) {
					parents[child] = parentParent;
					return;
				}
			}
		}
	}

	private int connectSmallComponents(int numberOfSegments, int num) {
		// post process small components
		for (int i = 0; i < num; i++) {
			final int a_tmp = find(ps, aPoints[i]);
			final int b_tmp = find(ps, bPoints[i]);
			if ((a_tmp != b_tmp) && ((sizes[a_tmp] < min_size) || (sizes[b_tmp] < min_size))) {
				join(ranks, sizes, ps, a_tmp, b_tmp);
				numberOfSegments--;
			}
		}
		return numberOfSegments;
	}

	private void resetComponents() {
		for (int i = 0; i < ps.length; i++) {
			ps[i] = i;
			ranks[i] = 0;
			sizes[i] = 1;
			thresholds[i] = threshold;
		}
	}

	private int connectComponents(int numberOfSegments, int num, int numEnd) {
		for (int i = (width * height * 4) - 1; i >= numEnd; i--) {
			if (connectComponent(i)) {
				numberOfSegments--;
			}
		}

		// for each edge, in non-decreasing weight order...
		for (int i = 0; i < num; i++) {
			if (connectComponent(i)) {
				numberOfSegments--;
			}
		}

		return numberOfSegments;
	}

	private boolean connectComponent(int i) {
		int a_point = find(ps, aPoints[i]);
		int b_point = find(ps, bPoints[i]);
		double distance = distances[i];
		
		if (a_point != b_point) {
			if ((distance <= thresholds[a_point]) && (distance <= thresholds[b_point])) {
				join(ranks, sizes, ps, a_point, b_point);
				a_point = find(ps, a_point);
				thresholds[a_point] = distance + threshold(sizes[a_point], threshold);
				return true;
			}
		}

		return false;
	}

	private void setDistance(int a_point, int b_point, double distance, int num) {
		aPoints[num] = a_point;
		bPoints[num] = b_point;
		distances[num] = distance;
	}

	private void readColorChannels(org.testobject.commons.util.image.Image.Int input) {
		// smooth each color channel
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final int rgb = input.get(x, y);
				r_original[y][x] = r_gauss[y][x] = ((rgb >> 16) & 0xff);
				g_original[y][x] = g_gauss[y][x] = ((rgb >> 8) & 0xff);
				b_original[y][x] = b_gauss[y][x] = ((rgb >> 0) & 0xff);
			}
		}
	}

	private void smoothChannels() {
		gaussianFilter.smooth(r_gauss, sigma);
		gaussianFilter.smooth(g_gauss, sigma);
		gaussianFilter.smooth(b_gauss, sigma);
	}

	private static final double diff(double r0, double r1, double g0, double g1, double b0, double b1) {
		return Math.sqrt((r0 - r1) * (r0 - r1) + (g0 - g1) * (g0 - g1) + (b0 - b1) * (b0 - b1));
	}

	private static final double threshold(int size, double c) {
		return c / size;
	}

	private static final void join(int[] rank, int[] size, int[] p, int x, int y) {
		if (rank[x] > rank[y]) {
			mergeYintoX(size, p, x, y);
		} else {
			mergeXintoY(rank, size, p, x, y);
		}
	}

	private static final void mergeYintoX(int[] size, int[] p, int x, int y) {
		p[y] = x;
		size[x] += size[y];
	}

	private static final void mergeXintoY(int[] rank, int[] size, int[] p, int x, int y) {
		p[x] = y;
		size[y] += size[x];
		if (rank[x] == rank[y]) {
			rank[y]++;
		}
	}

	private static final int find(int[] p, int x) {
		int y = x;
		while (y != p[y]) {
			y = p[y];
		}
		p[x] = y;
		return y;
	}

}
