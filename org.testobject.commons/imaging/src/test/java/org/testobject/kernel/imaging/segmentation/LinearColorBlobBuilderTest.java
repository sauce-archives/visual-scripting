package org.testobject.kernel.imaging.segmentation;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import org.junit.Test;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.collections.Maps;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.commons.util.io.FileUtil;
import org.testobject.kernel.imaging.procedural.Color;

/**
 * 
 * @author enijkamp
 *
 */
public class LinearColorBlobBuilderTest {
	
    public static final boolean debug = Debug.toDebugMode(false);
	
	@Test
	public void segmentSingleRectangle() {
		final int width = 50, height = 50;
		final BufferedImage test = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		{
			final Graphics g = test.getGraphics();
			g.setColor(java.awt.Color.WHITE);
			g.fillRect(0, 0, width, height);
		}

		LinearColorBlobBuilder builder = new LinearColorBlobBuilder();

		Blob root = builder.build(ImageUtil.Convert.toImage(test));
		
		BlobUtils.Print.printBlobs(root);
		
		assertThat(root.id, is(1));
		assertThat(root.bbox, is(new Rectangle.Int(0, 0, 50, 50)));
		assertThat(root.children.size(), is(0));
	}
	
	@Test
	public void segmentNestedRectangles() {
		final int width = 50, height = 50;
		final BufferedImage test = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		{
			final Graphics g = test.getGraphics();
			g.setColor(java.awt.Color.WHITE);
			g.fillRect(0, 0, width, height);
			g.setColor(java.awt.Color.RED);
			g.fillRect(10, 10, 30, 30);
		}

		LinearColorBlobBuilder builder = new LinearColorBlobBuilder();

		Blob root = builder.build(ImageUtil.Convert.toImage(test));
		
		BlobUtils.Print.printBlobs(root);
		
		assertThat(root.id, is(1));
		assertThat(root.children.size(), is(1));
		assertThat(root.bbox, is(new Rectangle.Int(0, 0, 50, 50)));

		Blob inner = root.children.get(0);
		assertThat(inner.children.size(), is(0));
		assertThat(inner.bbox, is(new Rectangle.Int(10, 10, 30, 30)));
	}
	
	private static class Segment {
		public List<Blob> blobs = new LinkedList<>();
		public Color color;
		public int size;
		
		public Segment(Blob blob, Color color) {
			this.blobs.add(blob);
			this.size = blob.area;
			this.color = color;
		}
		
		public void add(Blob blob, Color color) {
			this.blobs.add(blob);
			this.size += blob.area;
			
			int r = ((this.color.r) * (this.size - blob.area) + (color.r * blob.area)) / this.size;	
			int g = ((this.color.g) * (this.size - blob.area) + (color.g * blob.area)) / this.size;
			int b = ((this.color.b) * (this.size - blob.area) + (color.b * blob.area)) / this.size;
			
			this.color = new Color(r, g, b, 255);
		}
	}
	
	private static class Component {
		public final Segment a, b;

		public Component(Segment a, Segment b) {
			this.a = a;
			this.b = b;
		}

		public double distance() {
			return (a.size + b.size) * l2(a.color, b.color);
		}
		
		private double l2(Color a, Color b) {
			double l2 = 0d;
			l2 += (a.r - b.r) * (a.r - b.r);
			l2 += (a.g - b.g) * (a.g - b.g);
			l2 += (a.b - b.b) * (a.b - b.b);
			return Math.sqrt(l2);
		}
	}
	
	public static Color extractColor(Image.Int image, Blob blob) {
		int sum_r = 0, sum_g = 0, sum_b = 0, sum_a = 0, sum = 0;
		for (int x = 0; x < blob.bbox.w; x++) {
			for (int y = 0; y < blob.bbox.h; y++) {
				if (blob.get(x, y)) {
					int rgba = image.pixels[(blob.bbox.y + y) * image.w + blob.bbox.x + x];
					{
						int a = (rgba >> 24) & 0xff;
						int r = (rgba >> 16) & 0xff;
						int g = (rgba >> 8) & 0xff;
						int b = (rgba >> 0) & 0xff;

						sum_a += a;
						sum_r += r;
						sum_g += g;
						sum_b += b;
						sum++;
					}
				}
			}
		}
		return new Color((sum_r / sum), (sum_g / sum), (sum_b / sum), (sum_a / sum));
	}
	
	@Test
	public void segment() throws IOException {
		Image.Int image = ImageUtil.Read.read(FileUtil.readFileFromClassPath("small.png"));
		
		LinearColorBlobBuilder builder = new LinearColorBlobBuilder();

		Blob blob = builder.build(image);
		
		BlobUtils.Print.printBlobs(blob);
		
//		if(debug) VisualizerUtil.show("0", image);
//		
//		if(debug) VisualizerUtil.show("1", BlobUtils.Draw.drawHierarchyByLevel(blob));
//		
//		if(debug)
//		{
//			GraphBlobBuilder b = new GraphBlobBuilder(image.w, image.h, 0, 0);
//			VisualizerUtil.show("1", BlobUtils.Draw.drawHierarchyByLevel(b.build(image)[0]));
//		}
		
		List<Segment> segments = Lists.newArrayList(BlobUtils.Auxiliary.countBlobs(blob));
		Map<Segment, Segment> neighbours = Maps.newIdentityMap();
		toSegment(blob, image, segments, neighbours);
		
		neighbours.clear();
		findNeighbours(segments, neighbours);
		
		Queue<Component> components = new PriorityQueue<>(BlobUtils.Auxiliary.countBlobs(blob) << 2, new Comparator<Component>() {
			@Override
			public int compare(Component c1, Component c2) {
				return Double.compare(c1.distance(), c2.distance());
			}
		});
		components.addAll(toComponents(neighbours));
		
		System.out.println(segments.size());
		
		while(components.isEmpty() == false) {
			Component component = components.poll();
			
			System.out.println(component.distance());
			
			addTo(component.a, component.b, image);
			
			segments.remove(component.b);
			neighbours.remove(component.b);
		}
		
		System.out.println(segments.size());
		
		if(debug) draw("2", segments, image.w, image.h);
		
		if(debug) System.in.read();
	}

	private void findNeighbours(List<Segment> segments, Map<Segment, Segment> neighbours) {
		for(Segment a : segments) {
			for(Segment b : segments) {
				if(a != b && touches(a, b)) {
					neighbours.put(a, b);
				}
			}
		}
	}

	private void draw(String title, List<Segment> segments, int width, int height) {
		int[] pixels = new int[width * height];
		Image.Int image = new Image.Int(pixels, 0, 0, width, height, width, Image.Int.Type.RGB);
		
		java.awt.Color[] colors = BlobUtils.Draw.generateColors();
		
		for(Segment segment : segments) {
			Blob first = segment.blobs.get(0);
			int position = first.bbox.y * image.w + first.bbox.x + first.bbox.w + first.bbox.h;
			java.awt.Color c = colors[position % colors.length];
			for(Blob blob : segment.blobs) {
				for(int y = 0; y < blob.bbox.h; y++) {
					for(int x = 0; x < blob.bbox.w; x++) {
						if(blob.get(x, y)) {
							pixels[((blob.bbox.y + y) * width) + blob.bbox.x + x] = c.getRGB();
						}
					}
				}
			}
		}
		
//		VisualizerUtil.show(title, image);
	}

	private void addTo(Segment a, Segment b, Image.Int image) {
		for(Blob blob : b.blobs) {
			a.add(blob, extractColor(image, blob));
		}
	}

	private Collection<? extends Component> toComponents(Map<Segment, Segment> neighbours) {
		List<Component> components = Lists.newArrayList(neighbours.size());
		for(Map.Entry<Segment, Segment> entry : neighbours.entrySet()) {
			components.add(new Component(entry.getKey(), entry.getValue()));
		}
		return components;
	}

	private Segment toSegment(Blob blob, Image.Int image, List<Segment> segments, Map<Segment, Segment> neighbours) {
		Segment parentSegment = new Segment(blob, extractColor(image, blob));
		segments.add(parentSegment);
		
		List<Segment> childSegments = Lists.newArrayList(blob.children.size());
		
		for(Blob childBlob : blob.children) {
			Segment childSegment = toSegment(childBlob, image, segments, neighbours);
			childSegments.add(childSegment);
			neighbours.put(parentSegment, childSegment);
		}
		
		for(int i = 0; i < childSegments.size(); i++) {
			for(int j = i + 1; j < childSegments.size(); j++) {
				Segment a = childSegments.get(i);
				Segment b = childSegments.get(j);
				if(touches(a, b)) {
					neighbours.put(a, b);
				}
			}
		}
		
		return parentSegment;
	}
	
	public static boolean touches(Segment a, Segment b) {
		
		Blob child1 = a.blobs.get(0);
		Blob child2 = b.blobs.get(0);
		
		//  a b c
		//  d   e
		//  f g h
		
		Rectangle.Int box1 = child1.getBoundingBox();
		
		for(int y = box1.y; y < box1.y + box1.h; y++) {
			for(int x = box1.x; x < box1.x + box1.w; x++) {
				
				// a
				if(y > 0 && x > 0) {
					if(child1.ids[y-1][x-1] == child2.id) {
						return true;
					}
				}
				
				// b
				if(y > 0) {
					if(child1.ids[y-1][x] == child2.id) {
						return true;
					}
				}
				
				// c
				if(y > 0 && x+1 < child1.ids[0].length) {
					if(child1.ids[y-1][x+1] == child2.id) {
						return true;
					}
				}
				
				// d
				if(x > 0) {
					if(child1.ids[y][x-1] == child2.id) {
						return true;
					}
				}
				
				// e
				if(x+1 < child1.ids[0].length) {
					if(child1.ids[y][x+1] == child2.id) {
						return true;
					}
				}
				
				// f
				if(y+1 < child1.ids.length && x > 0) {
					if(child1.ids[y+1][x-1] == child2.id) {
						return true;
					}
				}
				
				// g
				if(y+1 < child1.ids.length) {
					if(child1.ids[y+1][x] == child2.id) {
						return true;
					}
				}
				
				// h
				if(y+1 < child1.ids.length && x+1 < child1.ids[0].length) {
					if(child1.ids[y+1][x+1] == child2.id) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
}