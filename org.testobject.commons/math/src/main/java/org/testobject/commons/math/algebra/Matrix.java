package org.testobject.commons.math.algebra;

import java.util.Arrays;

public interface Matrix<T> {

	int getRows();

	int getColumns();

	T get(int row, int column);

	void set(int row, int column, T value);

	class Builder {
		public static <T> Matrix<T> create(final int rows, final int columns) {
			return new Matrix<T>() {
				private final Object[][] data = new Object[rows][columns];

				public int getRows() {
					return rows;
				}

				public int getColumns() {
					return columns;
				}

				public void set(int row, int column, T value) {
					data[row][column] = value;
				}

				@SuppressWarnings("unchecked")
				public T get(int row, int column) {
					return (T) data[row][column];
				}
			};
		}
	}

	class Int implements Matrix<java.lang.Integer> {

		public final int rows, columns;
		public final int[][] data;

		public Int(int rows, int columns) {
			this.rows = rows;
			this.columns = columns;
			this.data = new int[rows][columns];
		}

		public int getRows() {
			return rows;
		}

		public int getColumns() {
			return columns;
		}

		public java.lang.Integer get(int row, int column) {
			return data[row][column];
		}

		public void set(int row, int column, java.lang.Integer value) {
			data[row][column] = value;
		}

		public String toString() {
			StringBuilder b = new StringBuilder();
			for (int i = 0; i < data.length; i++) {
				b.append("\n").append(Arrays.toString(data[i]));
			}
			return b.toString();
		}
	}

	class Double implements Matrix<java.lang.Double> {

		public final int rows, columns;
		public final double[][] data;

		public Double(int rows, int columns) {
			this.rows = rows;
			this.columns = columns;
			this.data = new double[rows][columns];
		}

		public int getRows() {
			return rows;
		}

		public int getColumns() {
			return columns;
		}

		public java.lang.Double get(int row, int column) {
			return data[row][column];
		}

		public void set(int row, int column, java.lang.Double value) {
			data[row][column] = value;
		}

		public String toString() {
			StringBuilder b = new StringBuilder();
			for (int i = 0; i < data.length; i++) {
				b.append("\n").append(Arrays.toString(data[i]));
			}
			return b.toString();
		}
	}
}
