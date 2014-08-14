package org.testobject.kernel.ocr;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testobject.kernel.imgproc.blob.BlobUtils;
import org.testobject.kernel.imgproc.blob.BoundingBox;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.collections.Sets;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.BooleanRaster;
import org.testobject.kernel.imgproc.blob.GraphBlobBuilder;
import org.testobject.kernel.imgproc.plot.VisualizerUtil;

/**
 * 
 * @author enijkamp
 * 
 */
public class MaskClusterer {

	private static final boolean DEBUG = Debug.toDebugMode(false);

	private final class Match {
		public final Set<CharacterMask> cluster;
		public final double fit;

		public Match(Set<CharacterMask> cluster, double fit) {
			this.cluster = cluster;
			this.fit = fit;
		}
	}

	private final FontRenderer fontRenderer;
	private final int histogramN;
	private final float maskThresholdUpperCase;
	private final float maskThresholdLowerCase;

	private final Decolorizer decolorizer = new Decolorizer();

	public MaskClusterer(FontRenderer fontRenderer, int histogramN, float maskThresholdUpperCase, float maskThresholdLowerCase) {
		this.fontRenderer = fontRenderer;
		this.histogramN = histogramN;
		this.maskThresholdUpperCase = maskThresholdUpperCase;
		this.maskThresholdLowerCase = maskThresholdLowerCase;
	}

	public Map<java.lang.Character, List<AdditiveMask>> generateMasksMap(final List<List<File>> fonts, final int fontSize) throws Exception {
		List<AdditiveMask> maskList = generateMasksFast(fonts, fontSize);
		Map<java.lang.Character, List<AdditiveMask>> maskMap = new HashMap<java.lang.Character, List<AdditiveMask>>();
		for (java.lang.Character character : Character.getAllChars()) {
			maskMap.put(character, new LinkedList<AdditiveMask>());
		}
		for (AdditiveMask additiveMask : maskList) {
			maskMap.get(additiveMask.chr).add(additiveMask);
		}
		return maskMap;
	}

	public List<AdditiveMask> generateMasksFast(final List<List<File>> fonts, final int... fontSize) {
		List<AdditiveMask> masks = new LinkedList<>();
		for (int size : fontSize) {
			masks.addAll(generateMasksFast(fonts, size));
		}

		return masks;
	}

	public List<AdditiveMask> generateMasksFast(final List<List<File>> fonts, final int fontSize) {
		return generateMasksFast(fonts, fontSize, Character.getAllChars());
	}

	public List<AdditiveMask> generateMasksFast(final List<List<File>> fonts, final int[] fontSizes, char[] characters) {
		List<AdditiveMask> additiveMasks = new LinkedList<AdditiveMask>();
		for (int fontSize : fontSizes) {
			additiveMasks.addAll(generateMasksFast(fonts, fontSize, characters));
		}
		return additiveMasks;
	}

	public List<AdditiveMask> generateMasksFast(final List<List<File>> fonts, final int fontSize, char[] characters) {
		List<AdditiveMask> additiveMasks = new LinkedList<AdditiveMask>();

		// chars
		int countInputMasks = 0, countOutputMasks = 0;
		for (List<File> family : fonts) {
			for (char chr : characters) {
				// log
				System.out.print(chr + " ");

				// cluster
				List<CharacterMask> masks = new LinkedList<CharacterMask>();
				for (File font : family) {
					try {
						BufferedImage characterImage = ImageUtil.trim(fontRenderer.drawChar(font, fontSize, chr),
                                Color.WHITE.getRGB());

						if (DEBUG) {
							VisualizerUtil.show("maskImage", characterImage, 20f);
						}

						Image.Int coloredImage = ImageUtil.toImage(characterImage);
						// FIXME may be done for all chars
						if (chr == 'i' || chr == 'j') {
							Image.Int image = new Image.Int(coloredImage.w + 20, coloredImage.h + 20);
							image = ImageUtil.fill(image, Color.WHITE);

							ImageUtil.paint(coloredImage, image, 10, 10);
							if (DEBUG) {
								VisualizerUtil.show("large " + chr, image, 20f);
							}

							GraphBlobBuilder blobBuilder = new GraphBlobBuilder(image.w, image.h);
							Blob[] blobs = blobBuilder.build(image);
							if (DEBUG) {
								VisualizerUtil.show("blobs " + chr, BlobUtils.drawHierarchy(blobs), 20f);
							}
							if (blobs.length != 3) {
								throw new IllegalStateException("character " + chr + " must have exactly 2 blobs");
							}

							for (Blob blob : blobs) {
								if (blob.children.isEmpty()) {
									coloredImage = ImageUtil.crop(coloredImage, new Rectangle(blob.bbox.x - 10, blob.bbox.y - 10,
											blob.bbox.width, blob.bbox.height));
									if (DEBUG) {
										VisualizerUtil.show("segfmented " + chr, coloredImage, 20f);
									}
								}
							}
						}

						Image.Int decolorizedImage = decolorizer.decolorize(coloredImage, toRaster(coloredImage));
						CharacterMask mask = MaskExtractor.getMask(decolorizedImage, chr, (int) fontSize, font.getAbsolutePath());

						mask.histogram = HistogramFeature.computeHistogram(ImageUtil.toImageByte(decolorizedImage), histogramN);
						masks.add(mask);
						countInputMasks++;
					} catch (Exception ex) {
						System.out.println(font);
					}
				}
				float threshold = java.lang.Character.isLowerCase(chr) ? maskThresholdLowerCase : maskThresholdUpperCase;
				// FIXME shouldn't be required anymore
				if (chr == 'r') {
					threshold = 0;
				}
				List<AdditiveMask> clusterMasks = clusterMasks(masks, threshold);
				countOutputMasks += clusterMasks.size();

				// store
				additiveMasks.addAll(clusterMasks);
			}
			System.out.println();
		}
		System.out.println(countInputMasks + " input masks -> " + countOutputMasks + " additive masks");

		return additiveMasks;
	}

	@SuppressWarnings("unchecked")
	public static <T extends BooleanRaster & BoundingBox> T toRaster(final Image.Int image) {
		class RasterWithBoundingBox implements BooleanRaster, BoundingBox {
			@Override
			public boolean get(int x, int y) {
				return (255 << 24 | image.get(x, y)) != Color.WHITE.getRGB();
			}

			@Override
			public Rectangle getBoundingBox() {
				return new Rectangle(getSize());
			}

			@Override
			public Dimension getSize() {
				return new Dimension(image.w, image.h);
			}

			@Override
			public void set(int x, int y, boolean what) {
				throw new UnsupportedOperationException();
			}
		}
		return (T) new RasterWithBoundingBox();
	}

	private List<AdditiveMask> clusterMasks(List<CharacterMask> masks, float threshold) {
		// start with single mask
		List<Set<CharacterMask>> clusters = new LinkedList<Set<CharacterMask>>();
		for (CharacterMask mask : masks) {
			Set<CharacterMask> set = Sets.newIdentitySet();
			set.add(mask);
			clusters.add(set);
		}

		// merge
		List<Set<CharacterMask>> merges = new LinkedList<Set<CharacterMask>>(clusters);
		// FIXME rewrite (en)
		int oldSize = merges.size();
		do {
			oldSize = merges.size();
			if (merges.size() > 1) {
				merges = mergeClusters(merges, threshold);
			}
		} while (oldSize != merges.size());

		// result
		List<AdditiveMask> result = new LinkedList<AdditiveMask>();
		for (Set<CharacterMask> cluster : merges) {
			result.add(new AdditiveMask(masks.get(0).chr, masks.get(0).fontSize, Lists.toList(cluster)));
		}
		return result;
	}

	private List<Set<CharacterMask>> mergeClusters(List<Set<CharacterMask>> clusters, float threshold) {
		List<Set<CharacterMask>> merges = new LinkedList<Set<CharacterMask>>();
		Set<Set<CharacterMask>> clustered = new HashSet<Set<CharacterMask>>();
		// build pairs of best fitting characters
		for (int i = 0; i < clusters.size(); i++) {
			Set<CharacterMask> firstCluster = clusters.get(i);
			boolean noMatch = true, lastMask = (i == clusters.size() - 1);

			// match
			if (lastMask == false) {
				// cartesian product without duplicates
				List<Set<CharacterMask>> secondClusters = clusters.subList(i, clusters.size() - 1);
				Match second = getBestMatchSameSize(secondClusters, firstCluster);
				if (second != null && second.fit < threshold) {
					// pair
					merges.add(Sets.concat(firstCluster, second.cluster));
					clustered.add(second.cluster);
					noMatch = false;
				}
			}

			// outlier
			if (lastMask || noMatch) {
				if (clustered.contains(firstCluster) == false) {
					merges.add(firstCluster);
				}
			}
		}
		return merges;
	}

	private Match getBestMatchSameSize(List<Set<CharacterMask>> clusters, Set<CharacterMask> first) {
		LinkedList<Set<CharacterMask>> candidates = new LinkedList<Set<CharacterMask>>();
		double[][] mask1 = getAverageMask(first);
		for (Set<CharacterMask> second : clusters) {
			double[][] mask2 = getAverageMask(second);
			if (getWidth(mask1) == getWidth(mask2) && getHeight(mask1) == getHeight(mask2)) {
				candidates.add(second);
			}
		}

		if (candidates.isEmpty()) {
			return null;
		}

		double bestDist = Float.MAX_VALUE;
		Set<CharacterMask> bestCluster = candidates.getFirst();
		for (Set<CharacterMask> second : candidates) {
			if (first != second) {
				double[][] mask2 = getAverageMask(second);
				int maxW = Math.max(getWidth(mask1), getWidth(mask2));
				int maxH = Math.max(getHeight(mask1), getHeight(mask2));
				double dist = l2(flatten(resize(mask1, maxW, maxH)), flatten(resize(mask2, maxW, maxH)));
				if (dist < bestDist) {
					bestDist = dist;
					bestCluster = second;
				}
			}
		}
		return new Match(bestCluster, bestDist);
	}

	private static double[] flatten(double[][] array) {
		double[] flat = new double[array[0].length * array.length];
		for (int i = 0; i < array.length; i++) {
			System.arraycopy(array[i], 0, flat, i * array[0].length, array[0].length);
		}
		return flat;
	}

	private static double l2(double[] v1, double[] v2) {
		assert (v1.length == v2.length);
		double diff = 0d;
		for (int i = 0; i < v1.length; i++) {
			diff += Math.pow(v1[i] - v2[i], 2);
		}
		return Math.sqrt(diff);
	}

	private double[][] getAverageMask(Collection<CharacterMask> characterMasks) {
		// size
		int maxHeight = 0, maxWidth = 0;
		for (CharacterMask mask : characterMasks) {
			maxHeight = Math.max(mask.height, maxHeight);
			maxWidth = Math.max(mask.width, maxWidth);
		}

		// avg
		double[][] average = new double[maxHeight][maxWidth];
		for (CharacterMask mask : characterMasks) {
			for (int x = 0; x < mask.width; x++) {
				for (int y = 0; y < mask.height; y++) {
					if (mask.get(x, y)) {
						average[y][x] += (1f / characterMasks.size()) * 255f;
					}
				}
			}
		}

		return average;
	}

	private double[][] resize(double[][] mask, int w, int h) {
		double[][] larger = new double[h][w];
		for (int y = 0; y < getHeight(mask); y++) {
			System.arraycopy(mask[y], 0, larger[y], 0, getWidth(mask));
		}
		return larger;
	}

	private int getWidth(double[][] mask) {
		return mask[0].length;
	}

	private int getHeight(double[][] mask) {
		return mask.length;
	}

}
