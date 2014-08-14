package org.testobject.kernel.classification.gui;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.tools.plot.Visualizer.Graphics;
import org.testobject.commons.tools.plot.Visualizer.Renderable;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.kernel.classification.util.Find;
import org.testobject.kernel.classification.util.Find.Adapter;
import org.testobject.kernel.classification.util.MaskUtil;
import org.testobject.kernel.imaging.segmentation.Mask;

/**
 * 
 * @author enijkamp
 *
 */
public interface Layers {

	class Raw implements Renderable {

		public BufferedImage image;

		@Override
		public void render(Graphics graphics) {
			if (image != null) {
				graphics.drawImage(image, 0, 0, image.getWidth(), image.getHeight());
			}
		}

		public void setImage(BufferedImage buffer) {
			this.image = buffer;
		}
		
		public Color getColor(int x, int y) {
			if (image != null && x < image.getWidth() && y < image.getHeight()) {
				return new Color(image.getRGB(x, y));
			} else {
				return Color.BLACK;
			}
		}
	}

	class BoundingBoxes implements Renderable {

		private boolean enabled = false;
		private List<org.testobject.commons.math.algebra.Rectangle.Int> boxes;

		@Override
		public void render(Graphics graphics) {
			if (enabled == true && boxes != null) {
				graphics.setAlpha(0.6f);
				{
					graphics.setColor(Color.RED);
					for (org.testobject.commons.math.algebra.Rectangle.Int box : boxes) {
						graphics.drawRect(box.x, box.y, box.w, box.h);
					}
				}
				graphics.setAlpha(1.0f);
			}
		}

		public void setBoxes(List<org.testobject.commons.math.algebra.Rectangle.Int> boxes) {
			this.boxes = boxes;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}

	class Box implements Renderable {
		
		private boolean enabled = true;
		private org.testobject.commons.math.algebra.Rectangle.Int box;

		@Override
		public void render(Graphics graphics) {
			if (isEnabled() == true && box != null) {
				graphics.setAlpha(0.6f);
				{
					graphics.setColor(Color.RED);
					final int border = 10;
					for (int i = 0; i < 5; i++) {
						graphics.drawRect(box.x - border + i, box.y - border + i, box.w + 2 * border - 2 * i, box.h + 2 * border - 2 * i);
					}
				}
				{
					graphics.setColor(Color.GREEN);
					graphics.fillRect(box.x, box.y, box.w, box.h);
				}
				graphics.setAlpha(1f);
			}
		}

		public void setBox(org.testobject.commons.math.algebra.Rectangle.Int box) {
			this.box = box;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}
	
	class Touch<T> implements Renderable {
		
		private boolean enabled = false;
		
		private int w, h;
		private Find<T> find;
		private Find.Result<T> result;
		
		public void setNodes(final Adapter<T> adapter, final int w, final int h) {
			this.w = w;
			this.h = h;
			this.find = new Find<T>(adapter);
		}
		
		public Find.Result<T> move(double x, double y) {
			if(x < w && y < h && x >= 0 && y >= 0) {
				if(this.find != null) {
					this.result = this.find.at(x, y);
				}
			}
			return this.result;
		}

		@Override
		public void render(Graphics graphics) {
			if (isEnabled() == true && result != null) {
				if(result.assigned) {
						
					// over mask
					if(result.overMask != null) {
						graphics.setAlpha(0.5f);
						Mask mask = result.overMask;
						Rectangle.Int box = mask.getBoundingBox();
						BufferedImage image = ImageUtil.Convert.toBufferedImage(MaskUtil.draw(mask, Color.orange.getRGB()));
						graphics.drawImage(image, box.x, box.y, box.w, box.h);
						graphics.setAlpha(1.0f);
					}
					
					// boxes
					if(result.boxes != null) {
						graphics.setColor(Color.red);
						for (Rectangle.Double box : result.boxes) {
							graphics.drawRect((int) box.x, (int) box.y, (int) box.w, (int) box.h);
						}
					}
										
					// bounding box
					if(result.index != Find.unassigned) {
						graphics.setAlpha(0.7f);
						graphics.setColor(Color.yellow);
						Rectangle.Double box = result.boxes[result.index];
						graphics.fillRect((int) box.x, (int) box.y, (int) box.w, (int) box.h);
						graphics.setAlpha(1.0f);
					}
					
					// touch mask
					if(result.touchMask != null) {
						graphics.setAlpha(0.7f);
						Mask mask = result.touchMask;
						Rectangle.Int box = mask.getBoundingBox();
						BufferedImage image = ImageUtil.Convert.toBufferedImage(MaskUtil.draw(mask, Color.magenta.getRGB()));
						graphics.drawImage(image, box.x, box.y, box.w, box.h);
						graphics.setAlpha(1.0f);
					}
					
					// scaled box
					if(result.index != Find.unassigned) {
						Rectangle.Double box = result.scaled[result.index];
						graphics.setColor(Color.green);
						graphics.drawRect((int) box.x, (int) box.y, (int) box.w, (int) box.h);
					}
					
					// location
					if(result.location != null) {
						graphics.setColor(Color.BLUE);
						graphics.drawOval(result.location.x-4, result.location.y-4, 8, 8);
					}
				}
			}
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}
}