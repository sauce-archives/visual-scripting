package org.testobject.commons.util.collections;

/**
 * @author enijkamp
 */
public interface Stack {

    class Int {

        private final static int DEFAULT_SIZE = 100;

        private int[] array;
        private int n;

        public Int() {
            this(DEFAULT_SIZE);
        }

        public Int(int size) {
            this.array = new int[size];
            this.n = 0;
        }

        public void push(int element) {
            if (n == array.length) {
                int[] newArray = new int[2*array.length];
                for (int i = 0; i < array.length; i++) {
                    newArray[i] = array[i];
                }
                array = newArray;
            }

            array[n++] = element;
        }

        public int pop() {
            return array[--n];
        }

        public boolean empty() {
            return (n == 0);
        }

        public int peek() {
            return array[n - 1];
        }

        public void clear() {
            this.n = 0;
        }

        public int size() {
            return this.n;
        }
    }

}
