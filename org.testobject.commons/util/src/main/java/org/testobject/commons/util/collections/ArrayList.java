package org.testobject.commons.util.collections;

/**
 * @author enijkamp
 */
public interface ArrayList {

    class Int {

        private int[] array;
        private int n;

        public static int INITIAL_CAPACITY = 10;

        public Int() {
            this(INITIAL_CAPACITY);
        }

        public Int(int initialCapacity) {
            if (initialCapacity < 0) {
                throw new IllegalArgumentException("Capacity can't be negative: " + initialCapacity);
            }
            this.array = new int[initialCapacity];
            this.n = 0;
        }

        public Int(int[] data) {
            this.array = new int[(int) (data.length * 1.1) + 1];
            this. n = data.length;
            System.arraycopy(data, 0, array, 0, n);
        }

        public int[] toArray() {
            int[] result = new int[this.n];
            System.arraycopy(this.array, 0, result, 0, n);
            return result;
        }

        public int get(int index) {
            checkRange(index);
            return this.array[index];
        }

        public int size() {
            return this.n;
        }

        public int remove(int index) {
            checkRange(index);
            int oldValue = this.array[index];
            int numToMove = this.n - index - 1;
            if (numToMove > 0) {
                System.arraycopy(this.array, index + 1, this.array, index, numToMove);
            }
            n--;
            return oldValue;
        }

        public void removeRange(int fromIndex, int toIndex) {
            checkRange(fromIndex);
            checkRange(toIndex);
            if (fromIndex >= toIndex) {
                return;
            }
            int numToMove = n - toIndex;
            if (numToMove > 0) {
                System.arraycopy(array, toIndex, array, fromIndex, numToMove);
            }
            n -= (toIndex - fromIndex);
        }

        public int set(int index, int element) {
            checkRange(index);
            int oldval = array[index];
            array[index] = element;
            return oldval;
        }

        public void add(int element) {
            ensureCapacity(n + 1);
            array[n++] = element;
        }

        public void add(int index, int element) {
            checkRangeIncludingEndpoint(index);
            ensureCapacity(n + 1);
            int numtomove = n - index;
            System.arraycopy(array, index, array, index + 1, numtomove);
            array[index] = element;
            n++;
        }

        public void addAll(int[] data) {
            int dataLen = data.length;
            if (dataLen == 0) {
                return;
            }
            int newcap = n + (int) (dataLen * 1.1) + 1;
            ensureCapacity(newcap);
            System.arraycopy(data, 0, array, n, dataLen);
            n += dataLen;
        }

        public void addAll(int index, int[] data) {
            int dataLen = data.length;
            if (dataLen == 0) {
                return;
            }
            int newcap = n + (int) (dataLen * 1.1) + 1;
            ensureCapacity(newcap);
            System.arraycopy(array, index, array, index + dataLen, n - index);
            System.arraycopy(data, 0, array, index, dataLen);
            n += dataLen;
        }

        public void clear() {
            n = 0;
        }

        public boolean contains(int data) {
            for (int i = 0; i < n; i++) {
                if (array[i] == data) {
                    return true;
                }
            }
            return false;
        }

        public int indexOf(int data) {
            for (int i = 0; i < n; i++) {
                if (array[i] == data) {
                    return i;
                }
            }
            return -1;
        }

        public int lastIndexOf(int data) {
            for (int i = n - 1; i >= 0; i--) {
                if (array[i] == data) {
                    return i;
                }
            }
            return -1;
        }

        public boolean isEmpty() {
            return n == 0;
        }

        public void ensureCapacity(int mincap) {
            if (mincap > array.length) {
                int newcap = ((array.length * 3) >> 1) + 1;
                int[] olddata = array;
                array = new int[newcap < mincap ? mincap : newcap];
                System.arraycopy(olddata, 0, array, 0, n);
            }
        }

        public void trimToSize() {
            if (n < array.length) {
                int[] olddata = array;
                array = new int[n];
                System.arraycopy(olddata, 0, array, 0, n);
            }
        }

        private void checkRange(int index) {
            if (index < 0 || index >= n) {
                throw new IndexOutOfBoundsException("Index should be at least 0 and less than " + n + ", found " + index);
            }
        }

        private void checkRangeIncludingEndpoint(int index) {
            if (index < 0 || index > n) {
                throw new IndexOutOfBoundsException("Index should be at least 0 and at most " + n + ", found " + index);
            }
        }
    }

    class Float {

        private float[] array;
        private int n;

        public static int INITIAL_CAPACITY = 10;

        public Float() {
            this(INITIAL_CAPACITY);
        }

        public Float(int initialCapacity) {
            if (initialCapacity < 0) {
                throw new IllegalArgumentException("Capacity can't be negative: " + initialCapacity);
            }
            this.array = new float[initialCapacity];
            this.n = 0;
        }

        public Float(int[] data) {
            this.array = new float[(int) (data.length * 1.1) + 1];
            this. n = data.length;
            System.arraycopy(data, 0, array, 0, n);
        }

        public int[] toArray() {
            int[] result = new int[this.n];
            System.arraycopy(this.array, 0, result, 0, n);
            return result;
        }

        public float get(int index) {
            checkRange(index);
            return this.array[index];
        }

        public int size() {
            return this.n;
        }

        public float remove(int index) {
            checkRange(index);
            float oldValue = this.array[index];
            int numToMove = this.n - index - 1;
            if (numToMove > 0) {
                System.arraycopy(this.array, index + 1, this.array, index, numToMove);
            }
            n--;
            return oldValue;
        }

        public void removeRange(int fromIndex, int toIndex) {
            checkRange(fromIndex);
            checkRange(toIndex);
            if (fromIndex >= toIndex) {
                return;
            }
            int numToMove = n - toIndex;
            if (numToMove > 0) {
                System.arraycopy(array, toIndex, array, fromIndex, numToMove);
            }
            n -= (toIndex - fromIndex);
        }

        public float set(int index, float element) {
            checkRange(index);
            float oldval = array[index];
            array[index] = element;
            return oldval;
        }

        public void add(float element) {
            ensureCapacity(n + 1);
            array[n++] = element;
        }

        public void add(int index, float element) {
            checkRangeIncludingEndpoint(index);
            ensureCapacity(n + 1);
            int numtomove = n - index;
            System.arraycopy(array, index, array, index + 1, numtomove);
            array[index] = element;
            n++;
        }

        public void addAll(float[] data) {
            int dataLen = data.length;
            if (dataLen == 0) {
                return;
            }
            int newcap = n + (int) (dataLen * 1.1) + 1;
            ensureCapacity(newcap);
            System.arraycopy(data, 0, array, n, dataLen);
            n += dataLen;
        }

        public void addAll(int index, float[] data) {
            int dataLen = data.length;
            if (dataLen == 0) {
                return;
            }
            int newcap = n + (int) (dataLen * 1.1) + 1;
            ensureCapacity(newcap);
            System.arraycopy(array, index, array, index + dataLen, n - index);
            System.arraycopy(data, 0, array, index, dataLen);
            n += dataLen;
        }

        public void clear() {
            n = 0;
        }

        public boolean contains(float data) {
            for (int i = 0; i < n; i++) {
                if (array[i] == data) {
                    return true;
                }
            }
            return false;
        }

        public int indexOf(float data) {
            for (int i = 0; i < n; i++) {
                if (array[i] == data) {
                    return i;
                }
            }
            return -1;
        }

        public int lastIndexOf(float data) {
            for (int i = n - 1; i >= 0; i--) {
                if (array[i] == data) {
                    return i;
                }
            }
            return -1;
        }

        public boolean isEmpty() {
            return n == 0;
        }

        public void ensureCapacity(int mincap) {
            if (mincap > array.length) {
                int newcap = ((array.length * 3) >> 1) + 1;
                float[] olddata = array;
                array = new float[newcap < mincap ? mincap : newcap];
                System.arraycopy(olddata, 0, array, 0, n);
            }
        }

        public void trimToSize() {
            if (n < array.length) {
                float[] olddata = array;
                array = new float[n];
                System.arraycopy(olddata, 0, array, 0, n);
            }
        }

        private void checkRange(int index) {
            if (index < 0 || index >= n) {
                throw new IndexOutOfBoundsException("Index should be at least 0 and less than " + n + ", found " + index);
            }
        }

        private void checkRangeIncludingEndpoint(int index) {
            if (index < 0 || index > n) {
                throw new IndexOutOfBoundsException("Index should be at least 0 and at most " + n + ", found " + index);
            }
        }
    }

    class Double {

        private double[] array;
        private int n;

        public static int INITIAL_CAPACITY = 10;

        public Double() {
            this(INITIAL_CAPACITY);
        }

        public Double(int initialCapacity) {
            if (initialCapacity < 0) {
                throw new IllegalArgumentException("Capacity can't be negative: " + initialCapacity);
            }
            this.array = new double[initialCapacity];
            this.n = 0;
        }

        public Double(int[] data) {
            this.array = new double[(int) (data.length * 1.1) + 1];
            this. n = data.length;
            System.arraycopy(data, 0, array, 0, n);
        }

        public int[] toArray() {
            int[] result = new int[this.n];
            System.arraycopy(this.array, 0, result, 0, n);
            return result;
        }

        public double get(int index) {
            checkRange(index);
            return this.array[index];
        }

        public int size() {
            return this.n;
        }

        public double remove(int index) {
            checkRange(index);
            double oldValue = this.array[index];
            int numToMove = this.n - index - 1;
            if (numToMove > 0) {
                System.arraycopy(this.array, index + 1, this.array, index, numToMove);
            }
            n--;
            return oldValue;
        }

        public void removeRange(int fromIndex, int toIndex) {
            checkRange(fromIndex);
            checkRange(toIndex);
            if (fromIndex >= toIndex) {
                return;
            }
            int numToMove = n - toIndex;
            if (numToMove > 0) {
                System.arraycopy(array, toIndex, array, fromIndex, numToMove);
            }
            n -= (toIndex - fromIndex);
        }

        public double set(int index, double element) {
            checkRange(index);
            double oldval = array[index];
            array[index] = element;
            return oldval;
        }

        public void add(double element) {
            ensureCapacity(n + 1);
            array[n++] = element;
        }

        public void add(int index, double element) {
            checkRangeIncludingEndpoint(index);
            ensureCapacity(n + 1);
            int numtomove = n - index;
            System.arraycopy(array, index, array, index + 1, numtomove);
            array[index] = element;
            n++;
        }

        public void addAll(double[] data) {
            int dataLen = data.length;
            if (dataLen == 0) {
                return;
            }
            int newcap = n + (int) (dataLen * 1.1) + 1;
            ensureCapacity(newcap);
            System.arraycopy(data, 0, array, n, dataLen);
            n += dataLen;
        }

        public void addAll(int index, double[] data) {
            int dataLen = data.length;
            if (dataLen == 0) {
                return;
            }
            int newcap = n + (int) (dataLen * 1.1) + 1;
            ensureCapacity(newcap);
            System.arraycopy(array, index, array, index + dataLen, n - index);
            System.arraycopy(data, 0, array, index, dataLen);
            n += dataLen;
        }

        public void clear() {
            n = 0;
        }

        public boolean contains(double data) {
            for (int i = 0; i < n; i++) {
                if (array[i] == data) {
                    return true;
                }
            }
            return false;
        }

        public int indexOf(double data) {
            for (int i = 0; i < n; i++) {
                if (array[i] == data) {
                    return i;
                }
            }
            return -1;
        }

        public int lastIndexOf(double data) {
            for (int i = n - 1; i >= 0; i--) {
                if (array[i] == data) {
                    return i;
                }
            }
            return -1;
        }

        public boolean isEmpty() {
            return n == 0;
        }

        public void ensureCapacity(int mincap) {
            if (mincap > array.length) {
                int newcap = ((array.length * 3) >> 1) + 1;
                double[] olddata = array;
                array = new double[newcap < mincap ? mincap : newcap];
                System.arraycopy(olddata, 0, array, 0, n);
            }
        }

        public void trimToSize() {
            if (n < array.length) {
                double[] olddata = array;
                array = new double[n];
                System.arraycopy(olddata, 0, array, 0, n);
            }
        }

        private void checkRange(int index) {
            if (index < 0 || index >= n) {
                throw new IndexOutOfBoundsException("Index should be at least 0 and less than " + n + ", found " + index);
            }
        }

        private void checkRangeIncludingEndpoint(int index) {
            if (index < 0 || index > n) {
                throw new IndexOutOfBoundsException("Index should be at least 0 and at most " + n + ", found " + index);
            }
        }
    }

}
