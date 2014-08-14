package org.testobject.kernel.classification.shapecontext;

import org.testobject.commons.math.algebra.Matrix;

public class Histogram extends Matrix.Int {

	public Histogram(int rows, int columns) {
		super(rows, columns);
	}

	public void increment(int row, int column) {
		data[row][column]++;
	}
}
