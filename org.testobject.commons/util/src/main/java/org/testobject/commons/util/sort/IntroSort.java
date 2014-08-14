package org.testobject.commons.util.sort;

/**
 * 
 * @author enijkamp
 *
 */
public class IntroSort {

	private static final int size_threshold = 16;

	public static void sort(int[] a, int[] b, double[] w) {
		introsort_loop(a, b, w, 0, a.length, 2 * floor_lg(a.length));
	}

	public static void sort(int[] a, int[] b, double[] w, int begin, int end) {
		if (begin < end) {
			introsort_loop(a, b, w, begin, end, 2 * floor_lg(end - begin));
		}
	}

	private final static void introsort_loop(int[] a, int[] b, double[] w, int lo, int hi, int depth_limit) {
		while (hi - lo > size_threshold) {
			if (depth_limit == 0) {
				heapsort(a, b, w, lo, hi);
				return;
			}
			depth_limit--;
			int p = partition(a, b, w, lo, hi, medianof3(w, lo, lo + ((hi - lo) / 2) + 1, hi - 1));
			introsort_loop(a, b, w, p, hi, depth_limit);
			hi = p;
		}
		insertionsort(w, lo, hi);
	}

	private final static int partition(int[] a, int[] b, double[] w, int lo, int hi, double x) {
		int i = lo, j = hi;
		while (true) {
			while (w[i] < x)
				i++;
			j = j - 1;
			while (x < w[j])
				j = j - 1;
			if (!(i < j))
				return i;
			exchange(a, b, w, i, j);
			i++;
		}
	}

	private final static double medianof3(double[] w, int lo, int mid, int hi) {
		if (w[mid] < w[lo]) {
			if (w[hi] < w[mid])
				return w[mid];
			else {
				if (w[hi] < w[lo])
					return w[hi];
				else
					return w[lo];
			}
		} else {
			if (w[hi] < w[mid]) {
				if (w[hi] < w[lo])
					return w[lo];
				else
					return w[hi];
			} else
				return w[mid];
		}
	}

	/*
	 * Heapsort algorithm
	 */
	private final static void heapsort(int[] a, int[] b, double[] w, int lo, int hi) {
		int n = hi - lo;
		for (int i = n / 2; i >= 1; i--) {
			downheap(w, i, n, lo);
		}
		for (int i = n; i > 1; i--) {
			exchange(a, b, w, lo, lo + i - 1);
			downheap(w, 1, i - 1, lo);
		}
	}

	private final static void downheap(double[] w, int i, int n, int lo) {
		double d = w[lo + i - 1];
		int child;
		while (i <= n / 2) {
			child = 2 * i;
			if (child < n && w[lo + child - 1] < w[lo + child]) {
				child++;
			}
			if (d >= w[lo + child - 1])
				break;
			w[lo + i - 1] = w[lo + child - 1];
			i = child;
		}
		w[lo + i - 1] = d;
	}

	private final static void insertionsort(double[] w, int lo, int hi) {
		int i, j;
		double t;
		for (i = lo; i < hi; i++) {
			j = i;
			t = w[i];
			while (j != lo && t < w[j - 1]) {
				w[j] = w[j - 1];
				j--;
			}
			w[j] = t;
		}
	}

	private final static void exchange(int[] a, int[] b, double[] w, int i, int j) {
		int ta = a[i];
		a[i] = a[j];
		a[j] = ta;
		int tb = b[i];
		b[i] = b[j];
		b[j] = tb;
		double tw = w[i];
		w[i] = w[j];
		w[j] = tw;
	}

	private final static int floor_lg(int a) {
		return (int) (Math.floor(Math.log(a) / Math.log(2)));
	}

}