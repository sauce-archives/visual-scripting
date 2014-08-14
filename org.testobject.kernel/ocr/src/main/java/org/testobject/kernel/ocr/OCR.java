package org.testobject.kernel.ocr;

import java.awt.image.BufferedImage;
import java.util.List;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.kernel.imaging.segmentation.Group;
import org.testobject.kernel.imaging.segmentation.HasBoundingBox;

public interface OCR {
	
	class TextPosition implements HasBoundingBox {
		
		private final Rectangle.Int boundingBox;

		public TextPosition(Rectangle.Int boundingBox) {
			this.boundingBox = boundingBox;
		}
		
		public Rectangle.Int getBoundingBox() {
			return boundingBox;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((boundingBox == null) ? 0 : boundingBox.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if(obj instanceof TextPosition == false){
				return false;
			}
			TextPosition other = (TextPosition) obj;
			if (boundingBox == null) {
				if (other.boundingBox != null)
					return false;
			} else if (!boundingBox.equals(other.boundingBox))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "boundingBox: " + boundingBox;
		}
		
	}
	
	public static class TextGroup extends TextPosition {

		private Group<TextPosition> group;

		public TextGroup(Group<OCR.TextPosition> group) {
			super(group.getBoundingBox());
			this.group = group;
		}

		public List<OCR.TextPosition> getTextElements() {
			return group.getContent();
		}

	}
	
	class Result implements HasBoundingBox {
		
		private final String text;
		private final float probability;
		private final Rectangle.Int boundingBox;

		public Result(String text, float probability, Rectangle.Int boundingBox) {
			this.text = text;
			this.probability = probability;
			this.boundingBox = boundingBox;
		}
		
		public String getText() {
			return text;
		}
		
		public float getProbability() {
			return probability;
		}
		
		public Rectangle.Int getBoundingBox() {
			return boundingBox;
		}
		
		@Override
		public String toString() {
			return "text: " + text + "\t probability: " + probability + "\t boundingBox: " + boundingBox;
		}
		
	}
	
	List<Result> getText(BufferedImage image, int dpi, double scalingFactor);
	
	List<Result> getText(BufferedImage image, Rectangle.Int region, int dpi, double scalingFactor);

	List<TextPosition> getTextPosition(BufferedImage image, int dpi, double scalingFactor);
	
}
