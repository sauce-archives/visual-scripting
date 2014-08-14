package org.testobject.kernel.classification.parser;

import java.awt.Color;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.lang.mutable.MutableInt;
import org.testobject.commons.util.tree.r.CartesianLookupIndex;
import org.testobject.commons.util.tree.r.SpatialIndex;
import org.testobject.kernel.api.classification.classifiers.Classifier.Qualifier;
import org.testobject.kernel.api.classification.graph.Element;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.classification.parser.Operations.Map;
import org.testobject.kernel.imaging.segmentation.Blob;
import org.testobject.kernel.imaging.segmentation.BlobUtils;
import org.testobject.kernel.imaging.segmentation.Group;
import org.testobject.kernel.imaging.segmentation.GroupBuilder;
import org.testobject.kernel.imaging.segmentation.Mask;

/**
 * 
 * @author enijkamp
 *
 */
public interface Grouping {
	
	class Fat implements Map<Node, Node> {
		
		private final static int FAT_X = 3, FAT_Y = 3;
		
		private final static int SMALL_MAX_W = 100, SMALL_MAX_H = 100;
		
		public enum Filter { ROOT, SMALL }
		
		private final Filter filter;
		
		public Fat() {
			this.filter = Filter.ROOT;
		}
		
		public Fat(Filter filter) {
			this.filter = filter;
		}

		@Override
		public Node apply(Node node, Context context) {
			if(filter == Filter.ROOT) {
				return Node.Factory.create(groupFilterRoot(node.getChildren(), new MutableInt(0), 0), node.getElement());
			} else {
				return Node.Factory.create(groupFilterSmall(node.getChildren(), new MutableInt(0), 0), node.getElement());
			}
		}
		
		private static List<Node> groupFilterRoot(List<Node> source, MutableInt id, int level) {
			
			// skip root level of segments
			if(level == 0) {
				
				List<Node> result = Lists.newArrayList(source.size());
				for(Node child : source) {
					result.add(Node.Factory.create(groupFilterRoot(child.getChildren(), id, level + 1), child.getElement()));
				}
				
				return result;
				
			} else {
			
				List<Group<Node>> groups = new GroupBuilder<Node>(createNodeAdapter()).buildGroups(source, new Insets(FAT_X, FAT_Y, FAT_X, FAT_Y));
				
				List<Node> result = Lists.newArrayList(groups.size());
				for(Group<Node> group : groups) {
					
					// single node
					if(group.getContent().size() == 1) {
						Node child = group.getContent().get(0);
						Element element = child.getElement();
						Node node = Node.Factory.create(groupFilterRoot(child.getChildren(), id, level + 1), element);
						result.add(node);	
					}
					
					// multiple nodes
					if(group.getContent().size() > 1) {
						List<Mask> masks = Lists.newArrayList(group.size());
						List<Node> childs = Lists.newArrayList(group.size());
						for(Node child : group) {
							childs.add(Node.Factory.create(groupFilterRoot(child.getChildren(), id, level + 1), child.getElement()));
							masks.addAll(child.getElement().getMasks());
						}
						Element element = Element.Builder
								.element(masks)
								.qualifier(Qualifier.Factory.Class.group(increment(id)))
								.build();
						Node node = Node.Factory.create(childs, element);
						result.add(node);
					}
				}
				
				return result;
			}
		}
		
		private static List<Node> groupFilterSmall(List<Node> source, MutableInt id, int level) {
			
			List<Node> result = Lists.newLinkedList();
			List<Node> small = Lists.newLinkedList();
			List<Node> large = Lists.newLinkedList();
			
			// filter
			{
				for(Node node : source) {
					if(node.getElement().getBoundingBox().w > SMALL_MAX_W || node.getElement().getBoundingBox().h > SMALL_MAX_H) {
						large.add(node);
					} else {
						small.add(node);
					}
				}
			}
			
			// small
			{
				List<Group<Node>> groups = new GroupBuilder<Node>(createNodeAdapter()).buildGroups(small, new Insets(FAT_X, FAT_Y, FAT_X, FAT_Y));
				
				for(Group<Node> group : groups) {
					
					// single node
					if(group.getContent().size() == 1) {
						Node child = group.getContent().get(0);
						Element element = child.getElement();
						Node node = Node.Factory.create(groupFilterSmall(child.getChildren(), id, level + 1), element);
						result.add(node);	
					}
					
					// multiple nodes
					if(group.getContent().size() > 1) {
						List<Mask> masks = Lists.newArrayList(group.size());
						List<Node> childs = Lists.newArrayList(group.size());
						for(Node child : group) {
							childs.add(Node.Factory.create(groupFilterSmall(child.getChildren(), id, level + 1), child.getElement()));
							masks.addAll(child.getElement().getMasks());
						}
						Element element = Element.Builder
								.element(masks)
								.qualifier(Qualifier.Factory.Class.group(increment(id)))
								.build();
						Node node = Node.Factory.create(childs, element);
						result.add(node);
					}
				}
			}
			
			// large
			{
				for(Node child : large) {
					Element element = child.getElement();
					Node node = Node.Factory.create(groupFilterSmall(child.getChildren(), id, level + 1), element);
					result.add(node);	
				}
			}
			
			return result;
		}
		
		private static int increment(MutableInt id) {
			int value = id.value;
			id.increment();
			return value;
		}

		private static Group.Adapter<Node> createNodeAdapter() {
			return new Group.Adapter<Node>() {
				@Override
				public Rectangle.Int getBoundingBox(Node node) {
					return node.getElement().getBoundingBox();
				}
			};
		}
		
		@Override
		public String toString() {
			return "group(" + getClass().getSimpleName().toLowerCase() + "())";
		}
	}
	
	class Intersect {
		
		private class MutableGroup {
			private List<Blob> blobs = new LinkedList<>();
			private Rectangle.Int bbox = new Rectangle.Int(0, 0, 0, 0);

			public MutableGroup(List<MutableGroup> groups) {
				int x1 = Integer.MAX_VALUE;
				int y1 = Integer.MAX_VALUE;
				int x2 = Integer.MIN_VALUE;
				int y2 = Integer.MIN_VALUE;
				for (MutableGroup group : groups) {
					x1 = Math.min(x1, group.bbox.x);
					y1 = Math.min(y1, group.bbox.y);
					x2 = Math.max(x2, group.bbox.x + group.bbox.w);
					y2 = Math.max(y2, group.bbox.y + group.bbox.h);
					blobs.addAll(group.blobs);
				}

				bbox.x = x1;
				bbox.y = y1;
				bbox.w = x2 - x1;
				bbox.h = y2 - y1;
			}

			public MutableGroup(Blob blob) {
				bbox.x = blob.bbox.x;
				bbox.y = blob.bbox.y;
				bbox.w = blob.bbox.w;
				bbox.h = blob.bbox.h;
				blobs.add(blob);
			}
		}

		public void groupIntersect(List<Blob> children, List<Mask> masks, BufferedImage image) {

			// build spatial index
			SpatialIndex<MutableGroup> index = new CartesianLookupIndex.Factory<MutableGroup>().create(createSpatialAdapter());
			for (Blob blob : children) {
				index.put(new MutableGroup(blob));
			}

			boolean merge = true;
			while (merge) {
				merge = false;
				for (MutableGroup group : index.entries()) {
					List<MutableGroup> intersects = new LinkedList<>();
					index.intersects(toDoubleRect(group.bbox), collect(intersects));

					if (intersects.size() > 1) {
						MutableGroup mergedGroup = new MutableGroup(intersects);

						for (MutableGroup g : intersects) {
							index.remove(g);
						}

						index.put(mergedGroup);

						merge = true;
						break;
					}
				}
			}

			Color[] colors = BlobUtils.Draw.generateColors();

			for (MutableGroup group : index.entries()) {
				Mask union = Mask.Builder.create(group.blobs);
				masks.add(union);

				int position = union.getBoundingBox().x + union.getBoundingBox().w + union.getBoundingBox().y
						+ union.getBoundingBox().h;
				Color color = colors[position % colors.length];

				for (int y = 0; y < union.getBoundingBox().h; y++) {
					for (int x = 0; x < union.getBoundingBox().w; x++) {
						if (union.get(x, y)) {
							image.setRGB(union.getBoundingBox().x + x, union.getBoundingBox().y + y, color.getRGB());
						}
					}
				}
			}

			for (Blob child : children) {
				groupIntersect(child.children, masks, image);
			}
		}

		private <T> SpatialIndex.Visitor<T> collect(final List<T> result) {
			return new SpatialIndex.Visitor<T>() {
				@Override
				public boolean visit(T payload) {
					result.add(payload);
					return true;
				}
			};
		}

		private SpatialIndex.Adapter<MutableGroup> createSpatialAdapter() {
			return new SpatialIndex.Adapter<MutableGroup>() {
				@Override
				public Rectangle.Double getBoundingBox(MutableGroup payload) {
					return toDoubleRect(payload.bbox);
				}
			};
		}

		private org.testobject.commons.math.algebra.Rectangle.Double toDoubleRect(org.testobject.commons.math.algebra.Rectangle.Int rect) {
			return new org.testobject.commons.math.algebra.Rectangle.Double(rect.x, rect.y, rect.w, rect.h);
		}
	}
}