package org.testobject.kernel.pipeline;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.Meta;
import org.testobject.kernel.imgproc.classifier.Classes;
import org.testobject.kernel.locator.api.Locator;
import org.testobject.kernel.locator.api.Root;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.tuple.Pair;
import org.testobject.kernel.locator.api.Container;

// FIXME rewrite (en)
public class Locate {
	
	final static Log log = LogFactory.getLog(Locate.class);

	// FIXME messy (en)
	public static LinkedList<Locator> locate(Blob rootBlob, Root root, Map<Locator, Blob> locatorToBlob, Map<Blob, Locator> blobToLocator, Point location) {

		// FIXME always returns "Root" or other container locator, if no specific locator can be found (en)

		// TODO todo:
		// 1. if clicked on locator, return path to locator
		// 2. else if clicked on blob without locator, check children locators using grown bounding boxes (of click-able child screen
		// regions)
		// 3. >> what happens if we clicked on icon within button locator? -> use button locator, not icon locator <<
		// 3. annotate click-able screen regions (set of segments) with locator
		
		
		// locate by blob
		{
			Blob[] blobsWithRealId = BoundingBoxes.getBlobs(rootBlob);
			Map<Blob, Blob> parentBlobs = BoundingBoxes.getParents(rootBlob);
			
			Blob blob = BoundingBoxes.locateBlob(blobsWithRealId, location);
			
			// case 1: blob is associated with a locator
			{
				if(isClickable(blob.meta)) {
					List<Blob> blobPath = BoundingBoxes.getBlobParentPath(parentBlobs, blob);
					LinkedList<Locator> locatorPath = check(BoundingBoxes.getLocatorPath(blobPath, blobToLocator));
					return locatorPath;
				}
			}
			
			// case 2: blob childrens are associated with a locator
			{
				List<Blob> childs = new LinkedList<>();
				BoundingBoxes.filterChildBlobsWithLocator(blob.children, childs);
				if(childs.isEmpty() == false) {
					Blob target = BoundingBoxes.locate(childs, location);
					List<Blob> blobPath = BoundingBoxes.getBlobParentPath(parentBlobs, target);
					LinkedList<Locator> locatorPath = BoundingBoxes.getLocatorPath(blobPath, blobToLocator);
					if(locatorPath.isEmpty() == false) {
						return locatorPath;
					}
				}
			}
			
			// case 3: check for blobs on same hierarchy level
			{
				// TODO (en)
			}
			
			// case 4: blob parents are associated with a locator
			{
				Blob target = BoundingBoxes.findParentBlobWithLocator(blobsWithRealId, parentBlobs, blob);
				List<Blob> blobPath = BoundingBoxes.getBlobParentPath(parentBlobs, target);
				LinkedList<Locator> locatorPath = BoundingBoxes.getLocatorPath(blobPath, blobToLocator);
				if(locatorPath.isEmpty() == false) {
					return locatorPath;
				}
			}
			
			// case 5: nearby blobs regardless of hierarchical relation
			{
				List<Blob> blobsWithLocator = new LinkedList<>();
				BoundingBoxes.filterChildBlobsWithLocator(rootBlob.children, blobsWithLocator);
				if(blobsWithLocator.isEmpty() == false) {
					Blob target = BoundingBoxes.locate(blobsWithLocator, location);
					List<Blob> blobPath = BoundingBoxes.getBlobParentPath(parentBlobs, target);
					LinkedList<Locator> locatorPath = BoundingBoxes.getLocatorPath(blobPath, blobToLocator);
					if(locatorPath.isEmpty() == false) {
						return locatorPath;
					}
				}
			}
			
			return new LinkedList<Locator>();
		}

		
		/*
		// locate by hierarchical locators
		{
			LinkedList<Locator> path = new LinkedList<>();
			// add head
			path.add(root);
			// add tail
			if (Hierarchical.locate(root, locatorToBlob, location, path)) {
				return path;
			}
		}

		// locate by extended bounding boxes
		{
			Blob target = BoundingBoxes.locate(blobs, location);
			double cx = target.bbox.getCenterX();
			double cy = target.bbox.getCenterY();
			Point targetLocation = new Point((int) cx, (int) cy);

			LinkedList<Locator> path = new LinkedList<>();
			// add head
			path.add(root);
			// add tail
			if (Hierarchical.locate(root, locatorToBlob, targetLocation, path)) {
				return path;
			}
		}
		
		throw new IllegalStateException();
		
		*/
	}
	
	private static LinkedList<Locator> check(LinkedList<Locator> locators) {
		if(locators.isEmpty()) {
			log.warn("Returning empty locator path, cannot recognize a single click-able locator");
		}
		return locators;
	}
	
	// FIXME introduce click-able property (en)
	private static boolean isClickable(Meta meta) {
		return meta instanceof Classes.Button || meta instanceof Classes.TextWord || meta instanceof Classes.Icon || meta instanceof Classes.TextBox || meta instanceof Classes.Image || meta instanceof Classes.TextParagraph;
	}

	private static class BoundingBoxes {
		
		// FIXME this is required since classifiers mutate blob hierarchy, thus Blob[] is incomplete (en)
		public static Blob[] getBlobs(Blob root) {
			int maxId = getMaxId(root, 0);
			Blob[] blobs = new Blob[maxId+1];
			setBlobs(blobs, root);
			return blobs;
		}
		
		private static int getMaxId(Blob blob, int id) {
			int max = Math.max(blob.id, id);
			for(Blob child : blob.children) {
				int childId = getMaxId(child, max);		
				max = Math.max(max, childId);
			}
			return max;
		}

		public static Map<Blob, Blob> getParents(Blob rootBlob) {
			Map<Blob, Blob> parents = new IdentityHashMap<>();
			for(Blob child : rootBlob.children) {
				addParent(parents, rootBlob, child);
			}
			
			return parents;
		}

		private static void addParent(Map<Blob, Blob> parents, Blob parent, Blob child) {
			parents.put(child, parent);
			for(Blob childchild : child.children) {
				addParent(parents, child, childchild);
			}
		}

		public static void setBlobs(Blob[] blobs, Blob blob) {
			blobs[blob.id] = blob;
			for(Blob child : blob.children) {
				setBlobs(blobs, child);
			}
		}
		
		public static LinkedList<Locator> getLocatorPath(List<Blob> blobs, Map<Blob, Locator> blobToLocator) {
			LinkedList<Locator> locators = new LinkedList<>();
			for(Blob blob : blobs) {
				if(blobToLocator.containsKey(blob)) {
					locators.add(blobToLocator.get(blob));
				}
			}
			if(locators.size() == 1 && isRoot(locators.get(0))) {
				//FIXME do callback in this situation al
				return Lists.newLinkedList();
			} else {
				return locators;
			}
		}
		
		private static boolean isRoot(Locator locator) {
			return locator.getClass().equals(Root.class);
		}

		public static List<Blob> getBlobParentPath(Map<Blob, Blob> parents, final Blob blob) {
			LinkedList<Blob> path = new LinkedList<>();
			path.add(blob);
			Blob current = blob;
			while(parents.containsKey(current)) {
				Blob parent = parents.get(current);
				path.addFirst(parent);
				current = parent;
			}
			return path;
		}
		
		public static Blob findParentBlobWithLocator(Blob[] blobs, Map<Blob, Blob> parents, Blob blob) {
			Blob parent = parents.containsKey(blob) ? parents.get(blob) : blob;
			while(isClickable(parent.meta) == false && parents.containsKey(parent)) {
				parent = parents.get(parent);
			}
			// FIXME (en)
			if(isClickable(parent.meta)) {
				log.warn("Parent blob '"+blob.id+"' with meta '"+parent.meta+"' is not clickable");
			}
			return parent;
		}
		
		public static Blob locate(List<Blob> blobs, Point location) {
			double[] scales = fit(blobs, location);
			int index = best(scales);
			Blob target = blobs.get(index);
			return target;
		}

		/*
		public static Blob locate(Blob[] blobs, Point location) {
			Blob blob = locateBlob(blobs, location);
			List<Blob> leaves = new LinkedList<>();
			filterChildBlobsWithLocator(blob.children, leaves);
			if (leaves.isEmpty() == false) {
				double[] scales = fit(leaves, location);
				int index = best(scales);
				Blob target = leaves.get(index);
				return target;
			} else {
				// FIXME messy (en)
				throw new IllegalStateException();
			}
		}
		*/

		private static int best(double[] values) {
			double min = Double.MAX_VALUE;
			int index = 0;
			for (int i = 0; i < values.length; i++) {
				if (values[i] < min) {
					index = i;
					min = values[i];
				}
			}
			return index;
		}

		private static double[] fit(List<Blob> boxes, Point click) {
			double[] scale = new double[boxes.size()];
			for (int i = 0; i < boxes.size(); i++) {
				Rectangle box = boxes.get(i).bbox;

				double cx = box.getCenterX();
				double w = box.getWidth();
				double dx = Math.abs(click.x - cx) - (w / 2);
				double sx = 1 + (dx * 2 / w);

				double cy = box.getCenterY();
				double h = box.getHeight();
				double dy = Math.abs(click.y - cy) - (h / 2);
				double sy = 1 + (dy * 2 / h);

				scale[i] = Math.max(sx, sy);
			}
			return scale;
		}

		public static Blob locateBlob(Blob[] blobs, Point location) {
			int id = blobs[0].ids[location.y][location.x];
			return blobs[id];
		}

		public static void filterChildBlobsWithLocator(List<Blob> blobs, List<Blob> leaves) {
			for (Blob blob : blobs) {
				if (isClickable(blob.meta)) {
					leaves.add(blob);
				} else {
					filterChildBlobsWithLocator(blob.children, leaves);
				}
			}
		}
	}

	private static class Hierarchical {

		public static boolean locate(Locator locator, Map<Locator, Blob> locatorToBlob, Point point, LinkedList<Locator> path) {
			Blob blob = locatorToBlob.get(locator);
			if (blob.bbox.contains(point)) {
				// find matches
				LinkedList<Pair<Locator, Blob>> matches = new LinkedList<>();
				if (locator instanceof Container) {
					Container container = (Container) locator;
					for (Locator childLocator : container.getChilds()) {
						Blob childBlob = locatorToBlob.get(childLocator);
						if (childBlob.bbox.contains(point)) {
							matches.add(new Pair<Locator, Blob>(childLocator, childBlob));
						}
					}
				}

				// matches
				if (matches.isEmpty()) {
					return false;
				}

				// determine best match
				if (matches.isEmpty() == false) {
					Pair<Locator, Blob> best = matches.getFirst();
					for (int i = 1; i < matches.size(); i++) {
						Pair<Locator, Blob> current = matches.get(i);
						if (distance(current.second, point) < distance(best.second, point)) {
							best = current;
						}
					}
					path.add(best.first);
					locate(best.first, locatorToBlob, point, path);
				}

				return true;
			}

			return false;
		}

		public static Blob getLastWidget(List<Blob> path) {
			for (int i = path.size() - 1; i > 0; i--) {
				Blob blob = path.get(i);
				if (blob.meta instanceof Meta.Blob == false
						&& blob.meta instanceof Classes.Group == false
						&& blob.meta instanceof Classes.TextChar == false) {
					return blob;
				}
			}
			return path.get(0);
		}

		public static List<Blob> locate(Blob root, Point point) {
			List<Blob> path = new LinkedList<Blob>();
			{
				// add head
				path.add(root);
				// add tail
				locate(root, point, path);
			}
			return path;
		}

		private static void locate(Blob blob, Point point, List<Blob> path) {
			if (blob.bbox.contains(point)) {
				// find matches
				List<Blob> matches = new LinkedList<Blob>();
				for (Blob child : blob.children) {
					if (child.bbox.contains(point)) {
						matches.add(child);
					}
				}
				// determine best match
				if (matches.isEmpty() == false) {
					Blob best = matches.get(0);
					for (int i = 1; i < matches.size(); i++) {
						Blob current = matches.get(i);
						if (distance(current, point) < distance(best, point)) {
							best = current;
						}
					}
					path.add(best);
					locate(best, point, path);
				}
			}
		}

		private static double distance(Blob blob, Point point) {
			return blob.bbox.getLocation().distance(point);
		}
	}
}