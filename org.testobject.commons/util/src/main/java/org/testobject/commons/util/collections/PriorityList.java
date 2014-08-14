package org.testobject.commons.util.collections;

/**
 * @author enijkamp
 *
 * Rewrite of https://github.com/aled/jsi/blob/master/src/main/java/com/infomatiq/jsi/rtree/SortedList.java
 */
public interface PriorityList {

    class Int {

        private static final int DEFAULT_PREFERRED_MAXIMUM_SIZE = 10;

        private int preferredMaximumSize = 1;
        private ArrayList.Int ids = null;
        private ArrayList.Float priorities = null;

        public Int(int preferredMaximumSize) {
            ids = new ArrayList.Int(preferredMaximumSize);
            priorities = new ArrayList.Float(preferredMaximumSize);
        }

        public void clear() {
            ids.clear();
            priorities.clear();
        }

        public void add(int id, float priority) {
            float lowestPriority = Float.NEGATIVE_INFINITY;

            if (priorities.size() > 0) {
                lowestPriority = priorities.get(priorities.size() - 1);
            }

            if ((priority == lowestPriority) ||
                    (priority < lowestPriority && ids.size() < preferredMaximumSize)) {
                // simply add the new entry at the lowest priority end
                ids.add(id);
                priorities.add(priority);
            } else if (priority > lowestPriority) {
                if (ids.size() >= preferredMaximumSize) {
                    int lowestPriorityIndex = ids.size() - 1;
                    while ((lowestPriorityIndex - 1 >= 0) &&
                            (priorities.get(lowestPriorityIndex - 1) == lowestPriority)) {
                        lowestPriorityIndex--;
                    }

                    if (lowestPriorityIndex >= preferredMaximumSize - 1) {
                        ids.removeRange(lowestPriorityIndex, ids.size() - lowestPriorityIndex);
                        priorities.removeRange(lowestPriorityIndex, priorities.size() - lowestPriorityIndex);
                    }
                }

                // put the new entry in the correct position. Could do a binary search here if the
                // preferredMaximumSize was large.
                int insertPosition = ids.size();
                while (insertPosition - 1 >= 0 && priority > priorities.get(insertPosition - 1)) {
                    insertPosition--;
                }

                ids.add(insertPosition, id);
                priorities.add(insertPosition, priority);
            }
        }

        public float getLowestPriority() {
            float lowestPriority = Float.NEGATIVE_INFINITY;
            if (priorities.size() >= preferredMaximumSize) {
                lowestPriority = priorities.get(priorities.size() - 1);
            }
            return lowestPriority;
        }

        public int[] toArray() {
            return ids.toArray();
        }

    }

}
