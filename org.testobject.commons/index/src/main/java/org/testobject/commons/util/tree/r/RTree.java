package org.testobject.commons.util.tree.r;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.collections.ArrayList;
import org.testobject.commons.util.collections.PriorityQueue;
import org.testobject.commons.util.collections.Stack;

/**
 * @author enijkamp
 *
 * Rewrite of https://github.com/aled/jsi/blob/master/src/main/java/com/infomatiq/jsi/rtree/RTree.java
 */
// FIXME get rid of ids (en)
// FIXME get rid of rectangle with minX, minY, maxX, maxY instead of x,y,w,h (en)
public class RTree<T> implements SpatialIndex<T> {

    public static class Factory<T> implements SpatialIndex.Factory<T> {
        @Override
        public SpatialIndex<T> create(Adapter<T> adapter) {
            return new RTree<>(adapter);
        }
    }

    private class Node {
        int nodeId = 0;
        double mbrMinX = +Double.MAX_VALUE;
        double mbrMinY = +Double.MAX_VALUE;
        double mbrMaxX = -Double.MAX_VALUE;
        double mbrMaxY = -Double.MAX_VALUE;

        double[] entriesMinX = null;
        double[] entriesMinY = null;
        double[] entriesMaxX = null;
        double[] entriesMaxY = null;

        int[] ids = null;
        int level;
        int entryCount;

        Node(int nodeId, int level, int maxNodeEntries) {
            this.nodeId = nodeId;
            this.level = level;
            this.entriesMinX = new double[maxNodeEntries];
            this.entriesMinY = new double[maxNodeEntries];
            this.entriesMaxX = new double[maxNodeEntries];
            this.entriesMaxY = new double[maxNodeEntries];
            this.ids = new int[maxNodeEntries];
        }

        void addEntry(double minX, double minY, double maxX, double maxY, int id) {
            this.ids[entryCount] = id;
            this.entriesMinX[entryCount] = minX;
            this.entriesMinY[entryCount] = minY;
            this.entriesMaxX[entryCount] = maxX;
            this.entriesMaxY[entryCount] = maxY;

            if (minX <this. mbrMinX) this.mbrMinX = minX;
            if (minY < this.mbrMinY)this. mbrMinY = minY;
            if (maxX > this.mbrMaxX)this. mbrMaxX = maxX;
            if (maxY > this.mbrMaxY) this.mbrMaxY = maxY;

            entryCount++;
        }

        int findEntry(double minX, double minY, double maxX, double maxY, int id) {
            for (int i = 0; i < entryCount; i++) {
                if (id == ids[i] &&
                        entriesMinX[i] == minX && entriesMinY[i] == minY &&
                        entriesMaxX[i] == maxX && entriesMaxY[i] == maxY) {
                    return i;
                }
            }
            return -1;
        }

        void deleteEntry(int i) {
            int lastIndex = entryCount - 1;
            double deletedMinX = entriesMinX[i];
            double deletedMinY = entriesMinY[i];
            double deletedMaxX = entriesMaxX[i];
            double deletedMaxY = entriesMaxY[i];

            if (i != lastIndex) {
                entriesMinX[i] = entriesMinX[lastIndex];
                entriesMinY[i] = entriesMinY[lastIndex];
                entriesMaxX[i] = entriesMaxX[lastIndex];
                entriesMaxY[i] = entriesMaxY[lastIndex];
                ids[i] = ids[lastIndex];
            }
            entryCount--;

            // adjust the MBR
            recalculateMBRIfInfluencedBy(deletedMinX, deletedMinY, deletedMaxX, deletedMaxY);
        }

        void recalculateMBRIfInfluencedBy(double deletedMinX, double deletedMinY, double deletedMaxX, double deletedMaxY) {
            if (mbrMinX == deletedMinX || mbrMinY == deletedMinY || mbrMaxX == deletedMaxX || mbrMaxY == deletedMaxY) {
                recalculateMBR();
            }
        }

        void recalculateMBR() {
            mbrMinX = entriesMinX[0];
            mbrMinY = entriesMinY[0];
            mbrMaxX = entriesMaxX[0];
            mbrMaxY = entriesMaxY[0];

            for (int i = 1; i < entryCount; i++) {
                if (entriesMinX[i] < mbrMinX) mbrMinX = entriesMinX[i];
                if (entriesMinY[i] < mbrMinY) mbrMinY = entriesMinY[i];
                if (entriesMaxX[i] > mbrMaxX) mbrMaxX = entriesMaxX[i];
                if (entriesMaxY[i] > mbrMaxY) mbrMaxY = entriesMaxY[i];
            }
        }

        void reorganize(RTree rtree) {
            int countdownIndex = rtree.maxNodeEntries - 1;
            for (int index = 0; index < entryCount; index++) {
                if (ids[index] == -1) {
                    while (ids[countdownIndex] == -1 && countdownIndex > index) {
                        countdownIndex--;
                    }
                    entriesMinX[index] = entriesMinX[countdownIndex];
                    entriesMinY[index] = entriesMinY[countdownIndex];
                    entriesMaxX[index] = entriesMaxX[countdownIndex];
                    entriesMaxY[index] = entriesMaxY[countdownIndex];
                    ids[index] = ids[countdownIndex];
                    ids[countdownIndex] = -1;
                }
            }
        }

        public int getEntryCount() {
            return entryCount;
        }

        public int getId(int index) {
            if (index < entryCount) {
                return ids[index];
            }
            return -1;
        }

        boolean isLeaf() {
            return (level == 1);
        }

        public int getLevel() {
            return level;
        }
    }

    private static final Log log = LogFactory.getLog(RTree.class);

    private final static int DEFAULT_MAX_NODE_ENTRIES = 50;
    private final static int DEFAULT_MIN_NODE_ENTRIES = 20;

    private final Adapter<T> adapter;

    private final int maxNodeEntries;
    private final int minNodeEntries;

    private Map<Integer, Node> nodeMap = new HashMap<>();
    private Map<Integer, T> objectMap = new HashMap<>();

    // internal consistency checking - set to true if debugging tree corruption
    private final static boolean INTERNAL_CONSISTENCY_CHECKING = true;

    // used to mark the status of entries during a node split
    private final static int ENTRY_STATUS_ASSIGNED = 0;
    private final static int ENTRY_STATUS_UNASSIGNED = 1;
    private byte[] entryStatus = null;
    private byte[] initialEntryStatus = null;

    // stacks used to store nodeId and entry index of each node
    // from the root down to the leaf. Enables fast lookup
    // of nodes when a split is propagated up the tree.
    private Stack.Int parents = new Stack.Int();
    private Stack.Int parentsEntry = new Stack.Int();

    // initialisation
    private int treeHeight = 1; // leaves are always level 1
    private int rootNodeId = 0;
    private int size = 0;

    // Enables creation of new nodes
    private int highestUsedNodeId = rootNodeId;

    // Deleted node objects are retained in the nodeMap,
    // so that they can be reused. Store the IDs of nodes
    // which can be reused.
    private Stack.Int deletedNodeIds = new Stack.Int();

    private ArrayList.Int savedValues = new ArrayList.Int();
    private double savedPriority = 0;

    public RTree(Adapter<T> adapter) {
        this.adapter = adapter;
        this.maxNodeEntries = DEFAULT_MAX_NODE_ENTRIES;
        this.minNodeEntries = DEFAULT_MIN_NODE_ENTRIES;

        this.entryStatus = new byte[maxNodeEntries];
        this.initialEntryStatus = new byte[maxNodeEntries];

        for (int i = 0; i < maxNodeEntries; i++) {
            this.initialEntryStatus[i] = ENTRY_STATUS_UNASSIGNED;
        }

        Node root = new Node(rootNodeId, 1, maxNodeEntries);
        this.nodeMap.put(rootNodeId, root);
    }


    @Override
    public void put(T element) {
        Rectangle.Double r = adapter.getBoundingBox(element);

        int id = getNextNodeId();
        objectMap.put(id, element);
        add(r.x, r.y, r.x + r.w, r.y + r.h, id, 1);

        size++;

        if (INTERNAL_CONSISTENCY_CHECKING) {
            checkConsistency();
        }
    }

    private void add(double minX, double minY, double maxX, double maxY, int id, int level) {
        // I1 [Find position for new record] Invoke ChooseLeaf to select a
        // leaf node L in which to place r
        Node n = chooseNode(minX, minY, maxX, maxY, level);
        Node newLeaf = null;

        // I2 [Add record to leaf node] If L has room for another entry,
        // install E. Otherwise invoke SplitNode to obtain L and LL containing
        // E and all the old entries of L
        if (n.entryCount < maxNodeEntries) {
            n.addEntry(minX, minY, maxX, maxY, id);
        } else {
            newLeaf = splitNode(n, minX, minY, maxX, maxY, id);
        }

        // I3 [Propagate changes upwards] Invoke AdjustTree on L, also passing LL
        // if a split was performed
        Node newNode = adjustTree(n, newLeaf);

        // I4 [Grow tree taller] If node split propagation caused the root to
        // split, create a new root whose children are the two resulting nodes.
        if (newNode != null) {
            int oldRootNodeId = rootNodeId;
            Node oldRoot = getNode(oldRootNodeId);

            rootNodeId = getNextNodeId();
            treeHeight++;
            Node root = new Node(rootNodeId, treeHeight, maxNodeEntries);
            root.addEntry(newNode.mbrMinX, newNode.mbrMinY, newNode.mbrMaxX, newNode.mbrMaxY, newNode.nodeId);
            root.addEntry(oldRoot.mbrMinX, oldRoot.mbrMinY, oldRoot.mbrMaxX, oldRoot.mbrMaxY, oldRoot.nodeId);
            nodeMap.put(rootNodeId, root);
        }
    }

    @Override
    public boolean remove(T element) {
        Rectangle.Double r = adapter.getBoundingBox(element);
        int id = -1;
        for(Map.Entry<Integer, T> entry : objectMap.entrySet()) {
            if(entry.getValue() == element) {
                id = entry.getKey();
            }
        }

        if(id == -1) {
            throw new IllegalStateException();
        }

        // FindLeaf algorithm inlined here. Note the "official" algorithm
        // searches all overlapping entries. This seems inefficient to me,
        // as an entry is only worth searching if it contains (NOT overlaps)
        // the rectangle we are searching for.
        //
        // Also the algorithm has been changed so that it is not recursive.

        // FL1 [Search subtrees] If root is not a leaf, check each entry
        // to determine if it contains r. For each entry found, invoke
        // findLeaf on the node pointed to by the entry, until r is found or
        // all entries have been checked.
        parents.clear();
        parents.push(rootNodeId);

        parentsEntry.clear();
        parentsEntry.push(-1);
        Node n = null;
        int foundIndex = -1;  // index of entry to be deleted in leaf

        while (foundIndex == -1 && parents.size() > 0) {
            n = getNode(parents.peek());
            int startIndex = parentsEntry.peek() + 1;

            if (!n.isLeaf()) {
                log.trace("searching node " + n.nodeId + ", from index " + startIndex);
                boolean contains = false;
                for (int i = startIndex; i < n.entryCount; i++) {
                    if (Rectangle.Double.contains(n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i],
                            r.x, r.y, r.x + r.w, r.y + r.h)) {
                        parents.push(n.ids[i]);
                        parentsEntry.pop();
                        parentsEntry.push(i); // this becomes the start index when the child has been searched
                        parentsEntry.push(-1);
                        contains = true;
                        break; // ie go to next iteration of while()
                    }
                }
                if (contains) {
                    continue;
                }
            } else {
                foundIndex = n.findEntry(r.x, r.y, r.x + r.w, r.y + r.h, id);
            }

            parents.pop();
            parentsEntry.pop();
        } // while not found

        if (foundIndex != -1) {
            n.deleteEntry(foundIndex);
            condenseTree(n);
            size--;
        }

        // shrink the tree if possible (i.e. if root node has exactly one entry,and that
        // entry is not a leaf node, delete the root (it's entry becomes the new root)
        Node root = getNode(rootNodeId);
        while (root.entryCount == 1 && treeHeight > 1)
        {
            deletedNodeIds.push(rootNodeId);
            root.entryCount = 0;
            rootNodeId = root.ids[0];
            treeHeight--;
            root = getNode(rootNodeId);
        }

        // if the tree is now empty, then set the MBR of the root node back to it's original state
        // (this is only needed when the tree is empty, as this is the only state where an empty node
        // is not eliminated)
        if (size == 0) {
            root.mbrMinX = +Double.MAX_VALUE;
            root.mbrMinY = +Double.MAX_VALUE;
            root.mbrMaxX = -Double.MAX_VALUE;
            root.mbrMaxY = -Double.MAX_VALUE;
        }

        if (INTERNAL_CONSISTENCY_CHECKING) {
            checkConsistency();
        }

        if(foundIndex != -1) {
            objectMap.remove(element);
        }

        return (foundIndex != -1);
    }

    @Override
    public void nearest(Point.Double point, double furthestDistance, Ordering ordering, Visitor<T> visitor) {
        nearestSorted(point, furthestDistance, visitor);
    }

    @Override
    public void nearest(Point.Double point, double furthestDistance, int n, Ordering ordering, Visitor<T> visitor) {
        if(ordering == Ordering.Sorted) {
            nearestNSorted(point, visitor, n, furthestDistance);
        } else {
            nearestNUnsorted(point, visitor,  n, furthestDistance);
        }
    }

    @Override
    public Iterable<T> entries() {
        return objectMap.values();
    }

    private void nearestSorted(Point.Double point, double furthestDistance, Visitor<T> visitor) {
        Node rootNode = getNode(rootNodeId);
        double furthestDistanceSq = furthestDistance * furthestDistance;
        ArrayList.Int nearestIds = new ArrayList.Int();
        nearest(point, rootNode, furthestDistanceSq, nearestIds);
        for(int id : nearestIds.toArray()) {
            visitor.visit(getObject(id));
        }
    }

    private void nearestNUnsorted(Point.Double p, Visitor<T> visitor, int count, double furthestDistance) {
        // This implementation is designed to give good performance
        // where
        //   o N is high (100+)
        //   o The results do not need to be sorted by distance.
        //
        // Uses a priority queue as the underlying data structure.
        //
        // The behaviour of this algorithm has been carefully designed to
        // return exactly the same items as the the original version (nearestN_orig), in particular,
        // more than N items will be returned if items N and N+x have the
        // same priority.
        PriorityQueue.Int distanceQueue = createNearestNDistanceQueue(p, count, furthestDistance);
        for(int id : distanceQueue.toArray()) {
            visitor.visit(getObject(id));
        }
    }

    private PriorityQueue.Int createNearestNDistanceQueue(Point.Double p, int count, double furthestDistance) {

        PriorityQueue.Int distanceQueue = new PriorityQueue.Int(PriorityQueue.Int.SORT_ORDER_DESCENDING);

        //  return immediately if given an invalid "count" parameter
        if (count <= 0) {
            return distanceQueue;
        }

        parents.clear();
        parents.push(rootNodeId);

        parentsEntry.clear();
        parentsEntry.push(-1);

        double furthestDistanceSq = furthestDistance * furthestDistance;

        while (parents.size() > 0) {
            Node n = getNode(parents.peek());
            int startIndex = parentsEntry.peek() + 1;

            if (!n.isLeaf()) {
                // go through every entry in the index node to check
                // if it could contain an entry closer than the farthest entry
                // currently stored.
                boolean near = false;
                for (int i = startIndex; i < n.entryCount; i++) {
                    if (Rectangle.Double.distanceSq(n.entriesMinX[i], n.entriesMinY[i],
                            n.entriesMaxX[i], n.entriesMaxY[i],
                            p.x, p.y) <= furthestDistanceSq) {
                        parents.push(n.ids[i]);
                        parentsEntry.pop();
                        parentsEntry.push(i); // this becomes the start index when the child has been searched
                        parentsEntry.push(-1);
                        near = true;
                        break; // ie go to next iteration of while()
                    }
                }
                if (near) {
                    continue;
                }
            } else {
                // go through every entry in the leaf to check if
                // it is currently one of the nearest N entries.
                for (int i = 0; i < n.entryCount; i++) {
                    double entryDistanceSq = Rectangle.Double.distanceSq(n.entriesMinX[i], n.entriesMinY[i],
                            n.entriesMaxX[i], n.entriesMaxY[i],
                            p.x, p.y);
                    int entryId = n.ids[i];

                    if (entryDistanceSq <= furthestDistanceSq) {
                        distanceQueue.insert(entryId, entryDistanceSq);

                        while (distanceQueue.size() > count) {
                            // normal case - we can simply remove the lowest priority (highest distance) entry
                            int value = distanceQueue.getValue();
                            double distanceSq = distanceQueue.getPriority();
                            distanceQueue.pop();

                            // rare case - multiple items of the same priority (distance)
                            if (distanceSq == distanceQueue.getPriority()) {
                                savedValues.add(value);
                                savedPriority = distanceSq;
                            } else {
                                savedValues.clear();
                            }
                        }

                        // if the saved values have the same distance as the
                        // next one in the tree, add them back in.
                        if (savedValues.size() > 0 && savedPriority == distanceQueue.getPriority()) {
                            for (int svi = 0; svi < savedValues.size(); svi++) {
                                distanceQueue.insert(savedValues.get(svi), savedPriority);
                            }
                            savedValues.clear();
                        }

                        // narrow the search, if we have already found N items
                        if (distanceQueue.getPriority() < furthestDistanceSq && distanceQueue.size() >= count) {
                            furthestDistanceSq = distanceQueue.getPriority();
                        }
                    }
                }
            }
            parents.pop();
            parentsEntry.pop();
        }

        return distanceQueue;
    }

    public void nearestNSorted(Point.Double p, Visitor<T> visitor, int count, double furthestDistance) {
        PriorityQueue.Int distanceQueue = createNearestNDistanceQueue(p, count, furthestDistance);
        distanceQueue.setSortOrder(PriorityQueue.Int.SORT_ORDER_ASCENDING);
        for(int id : distanceQueue.toArray()) {
            visitor.visit(getObject(id));
        }
    }

    @Override
    public void intersects(Rectangle.Double r, Visitor<T> v) {
        Node rootNode = getNode(rootNodeId);
        intersects(r, v, rootNode);
    }

    @Override
    public void contains(Rectangle.Double r, Visitor<T> v) {
        // find all rectangles in the tree that are contained by the passed rectangle
        // written to be non-recursive (should model other searches on this?)
        parents.clear();
        parents.push(rootNodeId);

        parentsEntry.clear();
        parentsEntry.push(-1);

        while (parents.size() > 0) {
            Node n = getNode(parents.peek());
            int startIndex = parentsEntry.peek() + 1;

            if (!n.isLeaf()) {
                // go through every entry in the index node to check
                // if it intersects the passed rectangle. If so, it
                // could contain entries that are contained.
                boolean intersects = false;
                for (int i = startIndex; i < n.entryCount; i++) {
                    if (Rectangle.Double.intersects(r.x, r.y, r.x + r.w, r.y + r.h,
                            n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i])) {
                        parents.push(n.ids[i]);
                        parentsEntry.pop();
                        parentsEntry.push(i); // this becomes the start index when the child has been searched
                        parentsEntry.push(-1);
                        intersects = true;
                        break; // ie go to next iteration of while()
                    }
                }
                if (intersects) {
                    continue;
                }
            } else {
                // go through every entry in the leaf to check if
                // it is contained by the passed rectangle
                for (int i = 0; i < n.entryCount; i++) {
                    if (Rectangle.Double.contains(r.x, r.y, r.x + r.w, r.y + r.h,
                            n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i])) {
                        if (!v.visit(getObject(n.ids[i]))) {
                            return;
                        }
                    }
                }
            }
            parents.pop();
            parentsEntry.pop();
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Rectangle.Double getBounds() {
        Rectangle.Double bounds = null;

        Node n = getNode(getRootNodeId());
        if (n != null && n.entryCount > 0) {
            bounds = new Rectangle.Double(n.mbrMinX, n.mbrMinY, n.mbrMaxX - n.mbrMinX, n.mbrMaxY - n.mbrMinY);
        }
        return bounds;
    }

    /**
     * Get the next available node ID. Reuse deleted node IDs if
     * possible
     */
    private int getNextNodeId() {
        int nextNodeId = 0;
        if (deletedNodeIds.size() > 0) {
            nextNodeId = deletedNodeIds.pop();
        } else {
            nextNodeId = 1 + highestUsedNodeId++;
        }
        return nextNodeId;
    }

    public Node getNode(int id) {
        return nodeMap.get(id);
    }

    public T getObject(int id) {
        return objectMap.get(id);
    }

    /**
     * Get the highest used node ID
     */
    public int getHighestUsedNodeId() {
        return highestUsedNodeId;
    }

    /**
     * Get the root node ID
     */
    public int getRootNodeId() {
        return rootNodeId;
    }

    /**
     * Split a node. Algorithm is taken pretty much verbatim from
     * Guttman's original paper.
     */
    private Node splitNode(Node n, double newRectMinX, double newRectMinY, double newRectMaxX, double newRectMaxY, int newId) {
        // [Pick first entry for each group] Apply algorithm pickSeeds to
        // choose two entries to be the first elements of the groups. Assign
        // each to a group.

        // debug code
        double initialArea = 0;
        if (log.isDebugEnabled()) {
            double unionMinX = Math.min(n.mbrMinX, newRectMinX);
            double unionMinY = Math.min(n.mbrMinY, newRectMinY);
            double unionMaxX = Math.max(n.mbrMaxX, newRectMaxX);
            double unionMaxY = Math.max(n.mbrMaxY, newRectMaxY);

            initialArea = (unionMaxX - unionMinX) * (unionMaxY - unionMinY);
        }

        System.arraycopy(initialEntryStatus, 0, entryStatus, 0, maxNodeEntries);

        Node newNode = new Node(getNextNodeId(), n.level, maxNodeEntries);
        nodeMap.put(newNode.nodeId, newNode);

        pickSeeds(n, newRectMinX, newRectMinY, newRectMaxX, newRectMaxY, newId, newNode); // this also sets the entryCount to 1

        // [Check if done] If all entries have been assigned, stop. If one
        // group has so few entries that all the rest must be assigned to it in
        // order for it to have the minimum number m, assign them and stop.
        while (n.entryCount + newNode.entryCount < maxNodeEntries + 1) {
            if (maxNodeEntries + 1 - newNode.entryCount == minNodeEntries) {
                // assign all remaining entries to original node
                for (int i = 0; i < maxNodeEntries; i++) {
                    if (entryStatus[i] == ENTRY_STATUS_UNASSIGNED) {
                        entryStatus[i] = ENTRY_STATUS_ASSIGNED;

                        if (n.entriesMinX[i] < n.mbrMinX) n.mbrMinX = n.entriesMinX[i];
                        if (n.entriesMinY[i] < n.mbrMinY) n.mbrMinY = n.entriesMinY[i];
                        if (n.entriesMaxX[i] > n.mbrMaxX) n.mbrMaxX = n.entriesMaxX[i];
                        if (n.entriesMaxY[i] > n.mbrMaxY) n.mbrMaxY = n.entriesMaxY[i];

                        n.entryCount++;
                    }
                }
                break;
            }
            if (maxNodeEntries + 1 - n.entryCount == minNodeEntries) {
                // assign all remaining entries to new node
                for (int i = 0; i < maxNodeEntries; i++) {
                    if (entryStatus[i] == ENTRY_STATUS_UNASSIGNED) {
                        entryStatus[i] = ENTRY_STATUS_ASSIGNED;
                        newNode.addEntry(n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i], n.ids[i]);
                        n.ids[i] = -1; // an id of -1 indicates the entry is not in use
                    }
                }
                break;
            }

            // [Select entry to assign] Invoke algorithm pickNext to choose the
            // next entry to assign. Add it to the group whose covering rectangle
            // will have to be enlarged least to accommodate it. Resolve ties
            // by adding the entry to the group with smaller area, then to the
            // the one with fewer entries, then to either. Repeat from S2
            pickNext(n, newNode);
        }

        n.reorganize(this);

        // check that the MBR stored for each node is correct.
        if (INTERNAL_CONSISTENCY_CHECKING) {
            Rectangle.Double nMBR = toRectangle(n.mbrMinX, n.mbrMinY, n.mbrMaxX, n.mbrMaxY);
            if (!nMBR.equals(calculateMBR(n))) {
                log.error("Error: splitNode old node MBR wrong");
            }
            Rectangle.Double newNodeMBR = toRectangle(newNode.mbrMinX, newNode.mbrMinY, newNode.mbrMaxX, newNode.mbrMaxY);
            if (!newNodeMBR.equals(calculateMBR(newNode))) {
                log.error("Error: splitNode new node MBR wrong");
            }
        }

        // debug code
        if (log.isTraceEnabled()) {
            double newArea = Rectangle.Double.area(n.mbrMinX, n.mbrMinY, n.mbrMaxX, n.mbrMaxY) +
                    Rectangle.Double.area(newNode.mbrMinX, newNode.mbrMinY, newNode.mbrMaxX, newNode.mbrMaxY);
            double percentageIncrease = (100 * (newArea - initialArea)) / initialArea;
            log.trace("Node " + n.nodeId + " split. New area increased by " + percentageIncrease + "%");
        }

        return newNode;
    }

    /**
     * Pick the seeds used to split a node.
     * Select two entries to be the first elements of the groups
     */
    private void pickSeeds(Node n, double newRectMinX, double newRectMinY, double newRectMaxX, double newRectMaxY, int newId, Node newNode) {
        // Find extreme rectangles along all dimension. Along each dimension,
        // find the entry whose rectangle has the highest low side, and the one
        // with the lowest high side. Record the separation.
        double maxNormalizedSeparation = -1; // initialize to -1 so that even overlapping rectangles will be considered for the seeds
        int highestLowIndex = -1;
        int lowestHighIndex = -1;

        // for the purposes of picking seeds, take the MBR of the node to include
        // the new rectangle aswell.
        if (newRectMinX < n.mbrMinX) n.mbrMinX = newRectMinX;
        if (newRectMinY < n.mbrMinY) n.mbrMinY = newRectMinY;
        if (newRectMaxX > n.mbrMaxX) n.mbrMaxX = newRectMaxX;
        if (newRectMaxY > n.mbrMaxY) n.mbrMaxY = newRectMaxY;

        double mbrLenX = n.mbrMaxX - n.mbrMinX;
        double mbrLenY = n.mbrMaxY - n.mbrMinY;

        if (log.isTraceEnabled()) {
            log.trace("pickSeeds(): NodeId = " + n.nodeId);
        }

        double tempHighestLow = newRectMinX;
        int tempHighestLowIndex = -1; // -1 indicates the new rectangle is the seed

        double tempLowestHigh = newRectMaxX;
        int tempLowestHighIndex = -1; // -1 indicates the new rectangle is the seed

        for (int i = 0; i < n.entryCount; i++) {
            double tempLow = n.entriesMinX[i];
            if (tempLow >= tempHighestLow) {
                tempHighestLow = tempLow;
                tempHighestLowIndex = i;
            } else {  // ensure that the same index cannot be both lowestHigh and highestLow
                double tempHigh = n.entriesMaxX[i];
                if (tempHigh <= tempLowestHigh) {
                    tempLowestHigh = tempHigh;
                    tempLowestHighIndex = i;
                }
            }

            // PS2 [Adjust for shape of the rectangle cluster] Normalize the separations
            // by dividing by the widths of the entire set along the corresponding
            // dimension
            double normalizedSeparation = mbrLenX == 0 ? 1 : (tempHighestLow - tempLowestHigh) / mbrLenX;
            if (normalizedSeparation > 1 || normalizedSeparation < -1) {
                log.error("Invalid normalized separation X");
            }

            if (log.isTraceEnabled()) {
                log.trace("Entry " + i + ", dimension X: HighestLow = " + tempHighestLow +
                        " (index " + tempHighestLowIndex + ")" + ", LowestHigh = " +
                        tempLowestHigh + " (index " + tempLowestHighIndex + ", NormalizedSeparation = " + normalizedSeparation);
            }

            // PS3 [Select the most extreme pair] Choose the pair with the greatest
            // normalized separation along any dimension.
            // Note that if negative it means the rectangles overlapped. However still include
            // overlapping rectangles if that is the only choice available.
            if (normalizedSeparation >= maxNormalizedSeparation) {
                highestLowIndex = tempHighestLowIndex;
                lowestHighIndex = tempLowestHighIndex;
                maxNormalizedSeparation = normalizedSeparation;
            }
        }

        // Repeat for the Y dimension
        tempHighestLow = newRectMinY;
        tempHighestLowIndex = -1; // -1 indicates the new rectangle is the seed

        tempLowestHigh = newRectMaxY;
        tempLowestHighIndex = -1; // -1 indicates the new rectangle is the seed

        for (int i = 0; i < n.entryCount; i++) {
            double tempLow = n.entriesMinY[i];
            if (tempLow >= tempHighestLow) {
                tempHighestLow = tempLow;
                tempHighestLowIndex = i;
            } else {  // ensure that the same index cannot be both lowestHigh and highestLow
                double tempHigh = n.entriesMaxY[i];
                if (tempHigh <= tempLowestHigh) {
                    tempLowestHigh = tempHigh;
                    tempLowestHighIndex = i;
                }
            }

            // PS2 [Adjust for shape of the rectangle cluster] Normalize the separations
            // by dividing by the widths of the entire set along the corresponding
            // dimension
            double normalizedSeparation = mbrLenY == 0 ? 1 : (tempHighestLow - tempLowestHigh) / mbrLenY;
            if (normalizedSeparation > 1 || normalizedSeparation < -1) {
                log.error("Invalid normalized separation Y");
            }

            if (log.isTraceEnabled()) {
                log.trace("Entry " + i + ", dimension Y: HighestLow = " + tempHighestLow +
                        " (index " + tempHighestLowIndex + ")" + ", LowestHigh = " +
                        tempLowestHigh + " (index " + tempLowestHighIndex + ", NormalizedSeparation = " + normalizedSeparation);
            }

            // PS3 [Select the most extreme pair] Choose the pair with the greatest
            // normalized separation along any dimension.
            // Note that if negative it means the rectangles overlapped. However still include
            // overlapping rectangles if that is the only choice available.
            if (normalizedSeparation >= maxNormalizedSeparation) {
                highestLowIndex = tempHighestLowIndex;
                lowestHighIndex = tempLowestHighIndex;
                maxNormalizedSeparation = normalizedSeparation;
            }
        }

        // At this point it is possible that the new rectangle is both highestLow and lowestHigh.
        // This can happen if all rectangles in the node overlap the new rectangle.
        // Resolve this by declaring that the highestLowIndex is the lowest Y and,
        // the lowestHighIndex is the largest X (but always a different rectangle)
        if (highestLowIndex == lowestHighIndex) {
            highestLowIndex = -1;
            double tempMinY = newRectMinY;
            lowestHighIndex = 0;
            double tempMaxX = n.entriesMaxX[0];

            for (int i = 1; i < n.entryCount; i++) {
                if (n.entriesMinY[i] < tempMinY) {
                    tempMinY = n.entriesMinY[i];
                    highestLowIndex = i;
                }
                else if (n.entriesMaxX[i] > tempMaxX) {
                    tempMaxX = n.entriesMaxX[i];
                    lowestHighIndex = i;
                }
            }
        }

        // highestLowIndex is the seed for the new node.
        if (highestLowIndex == -1) {
            newNode.addEntry(newRectMinX, newRectMinY, newRectMaxX, newRectMaxY, newId);
        } else {
            newNode.addEntry(n.entriesMinX[highestLowIndex], n.entriesMinY[highestLowIndex],
                    n.entriesMaxX[highestLowIndex], n.entriesMaxY[highestLowIndex],
                    n.ids[highestLowIndex]);
            n.ids[highestLowIndex] = -1;

            // move the new rectangle into the space vacated by the seed for the new node
            n.entriesMinX[highestLowIndex] = newRectMinX;
            n.entriesMinY[highestLowIndex] = newRectMinY;
            n.entriesMaxX[highestLowIndex] = newRectMaxX;
            n.entriesMaxY[highestLowIndex] = newRectMaxY;

            n.ids[highestLowIndex] = newId;
        }

        // lowestHighIndex is the seed for the original node.
        if (lowestHighIndex == -1) {
            lowestHighIndex = highestLowIndex;
        }

        entryStatus[lowestHighIndex] = ENTRY_STATUS_ASSIGNED;
        n.entryCount = 1;
        n.mbrMinX = n.entriesMinX[lowestHighIndex];
        n.mbrMinY = n.entriesMinY[lowestHighIndex];
        n.mbrMaxX = n.entriesMaxX[lowestHighIndex];
        n.mbrMaxY = n.entriesMaxY[lowestHighIndex];
    }

    /**
     * Pick the next entry to be assigned to a group during a node split.
     *
     * [Determine cost of putting each entry in each group] For each
     * entry not yet in a group, calculate the area increase required
     * in the covering rectangles of each group
     */
    private int pickNext(Node n, Node newNode) {
        double maxDifference = Float.NEGATIVE_INFINITY;
        int next = 0;
        int nextGroup = 0;

        maxDifference = Double.NEGATIVE_INFINITY;

        if (log.isTraceEnabled()) {
            log.trace("pickNext()");
        }

        for (int i = 0; i < maxNodeEntries; i++) {
            if (entryStatus[i] == ENTRY_STATUS_UNASSIGNED) {

                if (n.ids[i] == -1) {
                    log.error("Error: Node " + n.nodeId + ", entry " + i + " is null");
                }

                double nIncrease = Rectangle.Double.enlargement(n.mbrMinX, n.mbrMinY, n.mbrMaxX, n.mbrMaxY,
                        n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i]);
                double newNodeIncrease = Rectangle.Double.enlargement(newNode.mbrMinX, newNode.mbrMinY, newNode.mbrMaxX, newNode.mbrMaxY,
                        n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i]);

                double difference = Math.abs(nIncrease - newNodeIncrease);

                if (difference > maxDifference) {
                    next = i;

                    if (nIncrease < newNodeIncrease) {
                        nextGroup = 0;
                    } else if (newNodeIncrease < nIncrease) {
                        nextGroup = 1;
                    } else if (Rectangle.Double.area(n.mbrMinX, n.mbrMinY, n.mbrMaxX, n.mbrMaxY) < Rectangle.Double.area(newNode.mbrMinX, newNode.mbrMinY, newNode.mbrMaxX, newNode.mbrMaxY)) {
                        nextGroup = 0;
                    } else if (Rectangle.Double.area(newNode.mbrMinX, newNode.mbrMinY, newNode.mbrMaxX, newNode.mbrMaxY) < Rectangle.Double.area(n.mbrMinX, n.mbrMinY, n.mbrMaxX, n.mbrMaxY)) {
                        nextGroup = 1;
                    } else if (newNode.entryCount < maxNodeEntries / 2) {
                        nextGroup = 0;
                    } else {
                        nextGroup = 1;
                    }
                    maxDifference = difference;
                }
                if (log.isTraceEnabled()) {
                    log.trace("Entry " + i + " group0 increase = " + nIncrease + ", group1 increase = " + newNodeIncrease +
                            ", diff = " + difference + ", MaxDiff = " + maxDifference + " (entry " + next + ")");
                }
            }
        }

        entryStatus[next] = ENTRY_STATUS_ASSIGNED;

        if (nextGroup == 0) {
            if (n.entriesMinX[next] < n.mbrMinX) n.mbrMinX = n.entriesMinX[next];
            if (n.entriesMinY[next] < n.mbrMinY) n.mbrMinY = n.entriesMinY[next];
            if (n.entriesMaxX[next] > n.mbrMaxX) n.mbrMaxX = n.entriesMaxX[next];
            if (n.entriesMaxY[next] > n.mbrMaxY) n.mbrMaxY = n.entriesMaxY[next];
            n.entryCount++;
        } else {
            // move to new node.
            newNode.addEntry(n.entriesMinX[next], n.entriesMinY[next], n.entriesMaxX[next], n.entriesMaxY[next], n.ids[next]);
            n.ids[next] = -1;
        }

        return next;
    }

    /**
     * Recursively searches the tree for the nearest entry. Other queries
     * call execute() on an IntProcedure when a matching entry is found;
     * however nearest() must store the entry Ids as it searches the tree,
     * in case a nearer entry is found.
     * Uses the member variable nearestIds to store the nearest
     * entry IDs.
     */
    private double nearest(Point.Double p, Node n, double furthestDistanceSq, ArrayList.Int nearestIds) {
        for (int i = 0; i < n.entryCount; i++) {
            double tempDistanceSq = Rectangle.Double.distanceSq(n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i], p.x, p.y);
            if (n.isLeaf()) { // for leaves, the distance is an actual nearest distance
                if (tempDistanceSq < furthestDistanceSq) {
                    furthestDistanceSq = tempDistanceSq;
                    nearestIds.clear();
                }
                if (tempDistanceSq <= furthestDistanceSq) {
                    nearestIds.add(n.ids[i]);
                }
            } else { // for index nodes, only go into them if they potentially could have
                // a rectangle nearer than actualNearest
                if (tempDistanceSq <= furthestDistanceSq) {
                    // search the child node
                    furthestDistanceSq = nearest(p, getNode(n.ids[i]), furthestDistanceSq, nearestIds);
                }
            }
        }
        return furthestDistanceSq;
    }

    private boolean intersects(Rectangle.Double r, Visitor<T> v, Node n) {
        for (int i = 0; i < n.entryCount; i++) {
            if (Rectangle.Double.intersects(r.x, r.y, r.x + r.w, r.y + r.h, n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i])) {
                if (n.isLeaf()) {
                    if (!v.visit(getObject(n.ids[i]))) {
                        return false;
                    }
                } else {
                    Node childNode = getNode(n.ids[i]);
                    if (!intersects(r, v, childNode)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Used by delete(). Ensures that all nodes from the passed node
     * up to the root have the minimum number of entries.
     *
     * Note that the parent and parentEntry stacks are expected to
     * contain the nodeIds of all parents up to the root.
     */
    private void condenseTree(Node l) {
        // CT1 [Initialize] Set n=l. Set the list of eliminated
        // nodes to be empty.
        Node n = l;
        Node parent = null;
        int parentEntry = 0;

        Stack.Int eliminatedNodeIds = new Stack.Int();

        // CT2 [Find parent entry] If N is the root, go to CT6. Otherwise
        // let P be the parent of N, and let En be N's entry in P
        while (n.level != treeHeight) {
            parent = getNode(parents.pop());
            parentEntry = parentsEntry.pop();

            // CT3 [Eliminiate under-full node] If N has too few entries,
            // delete En from P and add N to the list of eliminated nodes
            if (n.entryCount < minNodeEntries) {
                parent.deleteEntry(parentEntry);
                eliminatedNodeIds.push(n.nodeId);
            } else {
                // CT4 [Adjust covering rectangle] If N has not been eliminated,
                // adjust EnI to tightly contain all entries in N
                if (n.mbrMinX != parent.entriesMinX[parentEntry] ||
                        n.mbrMinY != parent.entriesMinY[parentEntry] ||
                        n.mbrMaxX != parent.entriesMaxX[parentEntry] ||
                        n.mbrMaxY != parent.entriesMaxY[parentEntry]) {
                    double deletedMinX = parent.entriesMinX[parentEntry];
                    double deletedMinY = parent.entriesMinY[parentEntry];
                    double deletedMaxX = parent.entriesMaxX[parentEntry];
                    double deletedMaxY = parent.entriesMaxY[parentEntry];
                    parent.entriesMinX[parentEntry] = n.mbrMinX;
                    parent.entriesMinY[parentEntry] = n.mbrMinY;
                    parent.entriesMaxX[parentEntry] = n.mbrMaxX;
                    parent.entriesMaxY[parentEntry] = n.mbrMaxY;
                    parent.recalculateMBRIfInfluencedBy(deletedMinX, deletedMinY, deletedMaxX, deletedMaxY);
                }
            }
            // CT5 [Move up one level in tree] Set N=P and repeat from CT2
            n = parent;
        }

        // CT6 [Reinsert orphaned entries] Reinsert all entries of nodes in set Q.
        // Entries from eliminated leaf nodes are reinserted in tree leaves as in
        // Insert(), but entries from higher level nodes must be placed higher in
        // the tree, so that leaves of their dependent subtrees will be on the same
        // level as leaves of the main tree
        while (eliminatedNodeIds.size() > 0) {
            Node e = getNode(eliminatedNodeIds.pop());
            for (int j = 0; j < e.entryCount; j++) {
                add(e.entriesMinX[j], e.entriesMinY[j], e.entriesMaxX[j], e.entriesMaxY[j], e.ids[j], e.level);
                e.ids[j] = -1;
            }
            e.entryCount = 0;
            deletedNodeIds.push(e.nodeId);
        }
    }

    /**
     *  Used by add(). Chooses a leaf to add the rectangle to.
     */
    private Node chooseNode(double minX, double minY, double maxX, double maxY, int level) {
        // CL1 [Initialize] Set N to be the root node
        Node n = getNode(rootNodeId);
        parents.clear();
        parentsEntry.clear();

        // CL2 [Leaf check] If N is a leaf, return N
        while (true) {
            if (n == null) {
                log.error("Could not get root node (" + rootNodeId + ")");
            }

            if (n.level == level) {
                return n;
            }

            // CL3 [Choose subtree] If N is not at the desired level, let F be the entry in N
            // whose rectangle FI needs least enlargement to include EI. Resolve
            // ties by choosing the entry with the rectangle of smaller area.
            double leastEnlargement = Rectangle.Double.enlargement(n.entriesMinX[0], n.entriesMinY[0], n.entriesMaxX[0], n.entriesMaxY[0],
                    minX, minY, maxX, maxY);
            int index = 0; // index of rectangle in subtree
            for (int i = 1; i < n.entryCount; i++) {
                double tempMinX = n.entriesMinX[i];
                double tempMinY = n.entriesMinY[i];
                double tempMaxX = n.entriesMaxX[i];
                double tempMaxY = n.entriesMaxY[i];
                double tempEnlargement = Rectangle.Double.enlargement(tempMinX, tempMinY, tempMaxX, tempMaxY,
                        minX, minY, maxX, maxY);
                if ((tempEnlargement < leastEnlargement) ||
                        ((tempEnlargement == leastEnlargement) &&
                                (Rectangle.Double.area(tempMinX, tempMinY, tempMaxX, tempMaxY) <
                                        Rectangle.Double.area(n.entriesMinX[index], n.entriesMinY[index], n.entriesMaxX[index], n.entriesMaxY[index])))) {
                    index = i;
                    leastEnlargement = tempEnlargement;
                }
            }

            parents.push(n.nodeId);
            parentsEntry.push(index);

            // CL4 [Descend until a leaf is reached] Set N to be the child node
            // pointed to by Fp and repeat from CL2
            n = getNode(n.ids[index]);
        }
    }

    /**
     * Ascend from a leaf node L to the root, adjusting covering rectangles and
     * propagating node splits as necessary.
     */
    private Node adjustTree(Node n, Node nn) {
        // AT1 [Initialize] Set N=L. If L was split previously, set NN to be
        // the resulting second node.

        // AT2 [Check if done] If N is the root, stop
        while (n.level != treeHeight) {

            // AT3 [Adjust covering rectangle in parent entry] Let P be the parent
            // node of N, and let En be N's entry in P. Adjust EnI so that it tightly
            // encloses all entry rectangles in N.
            Node parent = getNode(parents.pop());
            int entry = parentsEntry.pop();

            if (parent.ids[entry] != n.nodeId) {
                log.error("Error: entry " + entry + " in node " +
                        parent.nodeId + " should point to node " +
                        n.nodeId + "; actually points to node " + parent.ids[entry]);
            }

            if (parent.entriesMinX[entry] != n.mbrMinX ||
                    parent.entriesMinY[entry] != n.mbrMinY ||
                    parent.entriesMaxX[entry] != n.mbrMaxX ||
                    parent.entriesMaxY[entry] != n.mbrMaxY) {

                parent.entriesMinX[entry] = n.mbrMinX;
                parent.entriesMinY[entry] = n.mbrMinY;
                parent.entriesMaxX[entry] = n.mbrMaxX;
                parent.entriesMaxY[entry] = n.mbrMaxY;

                parent.recalculateMBR();
            }

            // AT4 [Propagate node split upward] If N has a partner NN resulting from
            // an earlier split, create a new entry Enn with Ennp pointing to NN and
            // Enni enclosing all rectangles in NN. Add Enn to P if there is room.
            // Otherwise, invoke splitNode to produce P and PP containing Enn and
            // all P's old entries.
            Node newNode = null;
            if (nn != null) {
                if (parent.entryCount < maxNodeEntries) {
                    parent.addEntry(nn.mbrMinX, nn.mbrMinY, nn.mbrMaxX, nn.mbrMaxY, nn.nodeId);
                } else {
                    newNode = splitNode(parent, nn.mbrMinX, nn.mbrMinY, nn.mbrMaxX, nn.mbrMaxY, nn.nodeId);
                }
            }

            // AT5 [Move up to next level] Set N = P and set NN = PP if a split
            // occurred. Repeat from AT2
            n = parent;
            nn = newNode;

            parent = null;
            newNode = null;
        }

        return nn;
    }


    /**
     * Check the consistency of the tree.
     *
     * @return false if an inconsistency is detected, true otherwise.
     */
    public boolean checkConsistency() {
        return checkConsistency(rootNodeId, treeHeight, null);
    }

    private boolean checkConsistency(int nodeId, int expectedLevel, Rectangle.Double expectedMBR) {
        // go through the tree, and check that the internal data structures of
        // the tree are not corrupted.
        Node n = getNode(nodeId);

        if (n == null) {
            log.error("Error: Could not read node " + nodeId);
            return false;
        }

        // if tree is empty, then there should be exactly one node, at level 1
        if (nodeId == rootNodeId && size() == 0) {
            if (n.level != 1) {
                log.error("Error: tree is empty but root node is not at level 1");
                return false;
            }
        }

        if (n.level != expectedLevel) {
            log.error("Error: Node " + nodeId + ", expected level " + expectedLevel + ", actual level " + n.level);
            return false;
        }

        Rectangle.Double calculatedMBR = calculateMBR(n);
        Rectangle.Double actualMBR = toRectangle(n.mbrMinX, n.mbrMinY, n.mbrMaxX, n.mbrMaxY);
        if (!actualMBR.equals(calculatedMBR)) {
            log.error("Error: Node " + nodeId + ", calculated MBR does not equal stored MBR");
            if (actualMBR.x != n.mbrMinX) log.error("  actualMinX=" + actualMBR.x + ", calc=" + calculatedMBR.x);
            if (actualMBR.y != n.mbrMinY) log.error("  actualMinY=" + actualMBR.y + ", calc=" + calculatedMBR.y);
            if ((actualMBR.x + actualMBR.w) != n.mbrMaxX) log.error("  actualMaxX=" + (actualMBR.x + actualMBR.w) + ", calc=" + (calculatedMBR.x + calculatedMBR.w));
            if ((actualMBR.y + actualMBR.h) != n.mbrMaxY) log.error("  actualMaxY=" + (actualMBR.y + actualMBR.h) + ", calc=" + (calculatedMBR.y + calculatedMBR.h));
            return false;
        }

        if (expectedMBR != null && !actualMBR.equals(expectedMBR)) {
            log.error("Error: Node " + nodeId + ", expected MBR (from parent) does not equal stored MBR");
            return false;
        }

        // Check for corruption where a parent entry is the same object as the child MBR
        if (expectedMBR != null && actualMBR == expectedMBR) {
            log.error("Error: Node " + nodeId + " MBR using same rectangle object as parent's entry");
            return false;
        }

        for (int i = 0; i < n.entryCount; i++) {
            if (n.ids[i] == -1) {
                log.error("Error: Node " + nodeId + ", Entry " + i + " is null");
                return false;
            }

            if (n.level > 1) { // if not a leaf
                if (!checkConsistency(n.ids[i], n.level - 1, toRectangle(n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i]))) {
                    return false;
                }
            }
        }
        return true;
    }

    private Rectangle.Double calculateMBR(Node n) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (int i = 0; i < n.entryCount; i++) {
            if (n.entriesMinX[i] < minX) minX = n.entriesMinX[i];
            if (n.entriesMinY[i] < minY) minY = n.entriesMinY[i];
            if (n.entriesMaxX[i] > maxX) maxX = n.entriesMaxX[i];
            if (n.entriesMaxY[i] > maxY) maxY = n.entriesMaxY[i];
        }
        return toRectangle(minX,  minY,  maxX, maxY);
    }

    private Rectangle.Double toRectangle(double minX, double minY, double maxX, double maxY) {
        return new Rectangle.Double(minX,  minY,  maxX - minX, maxY - minY);
    }
}
