package org.testobject.kernel.ocr;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.testobject.kernel.imgproc.blob.BoundingBox;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.BooleanRaster;
import org.testobject.kernel.ocr.SlidingMaskTextRecognizer.MaskMatch;
import org.testobject.kernel.ocr.SlidingMaskTextRecognizer.Word;

/**
 * 
 * @author enijkamp
 * 
 */
public interface TextRecognizer<T extends BooleanRaster & BoundingBox> {

	class Builder {

		public static class Mocks {

			public static class TextDescriptor {

				public final Rectangle box;
				public final int area;

				public TextDescriptor(Rectangle box, int area) {
					this.box = box;
					this.area = area;
				}

				public TextDescriptor(Rectangle box) {
					this(box, -1);
				}

				public boolean hasArea() {
					return area != -1;
				}
			}

			public static Match<Blob> mockMatch(String text) {
				List<MaskMatch> matches = Collections.singletonList(new MaskMatch(0, text, null, null, 0, 0));
				return new Match<Blob>(Collections.singletonList(new Word(matches, 0, 0, 0)));
			}

			public static TextDescriptor text(Rectangle box, int area) {
				return new TextDescriptor(box, area);
			}

			public static TextDescriptor text(Rectangle box) {
				return new TextDescriptor(box);
			}

			public static Rectangle box(int x, int y, int w, int h) {
				return new Rectangle(x, y, w, h);
			}

			public static int area(int pixels) {
				return pixels;
			}
		}

		public static TextRecognizer<Blob> mock(final String[] texts) {
			return new TextRecognizer<Blob>() {
				@Override
				public TextRecognizer.Match<Blob> recognize(Image.Int image, List<Blob> rasters) {
					for (String text : texts) {
						if (rasters.size() == text.length()) {
							return Mocks.mockMatch(text);
						}
					}

					return Mocks.mockMatch("");
				}
			};
		}

		public static TextRecognizer<Blob> mock(final Map<Mocks.TextDescriptor, String> texts) {
			return new TextRecognizer<Blob>() {
				@Override
				public TextRecognizer.Match<Blob> recognize(Image.Int image, List<Blob> rasters) {
					sort(rasters);
					String text = "";
					for (Blob blob : rasters) {
						for (Entry<Mocks.TextDescriptor, String> candidate : texts.entrySet()) {
							Rectangle fat = fatRect(candidate.getKey().box);
							if (fat.contains(blob.bbox)) {
								if (candidate.getKey().hasArea()) {
									if (Math.abs(candidate.getKey().area - blob.area) < 5) {
										text += candidate.getValue();
									}
								} else {
									text += candidate.getValue();
								}
							}
						}
					}

					return Mocks.mockMatch(text);
				}
			};
		}

		private static void sort(List<Blob> blobs) {
			class LeftToRightSorter implements Comparator<Blob> {
				@Override
				public int compare(Blob b1, Blob b2) {
					return Integer.compare(b1.bbox.x, b2.bbox.x);
				}
			}
			Collections.sort(blobs, new LeftToRightSorter());
		}

		private static Rectangle fatRect(Rectangle rect) {
			return new Rectangle(Math.max(rect.x - 2, 0), Math.max(rect.y - 2, 0), rect.width + 4, rect.height + 4);
		}
	}

	class Match<T extends BooleanRaster & BoundingBox> {

		public final String text;
		public final List<Word> words;

		public Match(List<Word> words) {
			this.words = words;
			this.text = toString(words);
		}

		private final String toString(List<Word> words) {
			String text = "";
			for (Word word : words) {
				text += word.toString();
			}
			return text;
		}

		public String toString() {
			return text;
		}
	}

	Match<T> recognize(Image.Int image, List<T> rasters);
}
