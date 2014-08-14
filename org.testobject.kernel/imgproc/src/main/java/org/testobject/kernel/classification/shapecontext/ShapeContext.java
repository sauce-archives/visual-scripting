package org.testobject.kernel.classification.shapecontext;

import org.testobject.commons.math.algebra.Matrix;
import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Polar;

public class ShapeContext {

	private static final int LOG_BIN_SIZE = 5;
	private static final int TETA_BIN_SIZE = 12;

	private static final double LOWER_BOUNDERY = Math.log10(0.125);
	private static final double HIGHER_BOUNDERY = Math.log10(2);

	private static final double[] logSpace = createLogSpace();

	private static double[] createLogSpace() {
		double[] logSpace = new double[LOG_BIN_SIZE];
		double distance = HIGHER_BOUNDERY - LOWER_BOUNDERY;

		for (short i = 0; i < LOG_BIN_SIZE; ++i) {
			logSpace[i] = Math.pow(10, LOWER_BOUNDERY + i * distance / LOG_BIN_SIZE);
		}

		return logSpace;
	}

	public static Histogram[] calculateShapeContextHistogram(final Point.Double[] contour) {
		final Matrix<Polar> polarMatrix = Matrix.Builder.create(contour.length, contour.length);
		double distanceSum = 0d;
		for (int r = 0; r < polarMatrix.getRows(); r++) {
			for (int c = 0; c < polarMatrix.getColumns(); c++) {
				Polar polar = new Polar(contour[r], contour[c]);
				polarMatrix.set(r, c, polar);
				distanceSum += polar.distance;
			}
		}
		double distanceAverage = distanceSum / (contour.length * contour.length);
		return createShapeContextHistogram(polarMatrix, distanceAverage);
	}

	private static Histogram[] createShapeContextHistogram(final Matrix<Polar> polarMatrix, final double distanceAverage) {
		Histogram[] histograms = new Histogram[polarMatrix.getRows()];
		for (int i = 0; i < histograms.length; i++) {
			histograms[i] = new Histogram(LOG_BIN_SIZE, TETA_BIN_SIZE);
		}

		for (int r = 0; r < polarMatrix.getRows(); r++) {
			for (int c = 0; c < polarMatrix.getColumns(); c++) {
				Polar polar = polarMatrix.get(r, c);
				int logBin = logBin(polar.distance / distanceAverage);
				int tetaBin = tetaBin(polar.angle);
				if (logBin > -1) {
					histograms[r].increment(logBin, tetaBin);
				}
			}
		}

		return histograms;
	}

	private static int tetaBin(double angle) {
		return (int) Math.floor((angle + Math.PI) / (2 * Math.PI / (TETA_BIN_SIZE - 1)));
	}

	private static int logBin(double normalizedDistance) {
		for (int k = 0; k < logSpace.length; ++k) {
			if (normalizedDistance < logSpace[k]) {
				return k;
			}
		}

		return -1;
	}

	public static Matrix.Double calculateCostMatrix(final Histogram[] experimentHistograms, final Histogram[] trainingHistograms) {
		Matrix.Double costs = new Matrix.Double(experimentHistograms.length, trainingHistograms.length);
		for (int r = 0; r < costs.rows; r++) {
			for (int c = 0; c < costs.columns; c++) {
				costs.data[r][c] = chiSquared(experimentHistograms[r], trainingHistograms[c]);
			}
		}
		return costs;
	}

	public static double calculateMatchingCosts(int[][] assignments, double[][] costMatrix) {
		int sumCounter = 0;
		double matchingCosts = 0;
		for (int i = 0; i < assignments.length; i++) {
			int[] assigment = assignments[i];
			if (assigment == null) {
				continue;
			}
			sumCounter++;
			matchingCosts += costMatrix[assigment[0]][assigment[1]];
		}

		return matchingCosts / sumCounter;
	}

	private static double chiSquared(final Matrix.Int m1, final Matrix.Int m2) {
		double chi = 0d;
		for (int r = 0; r < m1.rows; r++) {
			for (int c = 0; c < m2.columns; c++) {
				int hi = m1.data[r][c];
				int hj = m2.data[r][c];
				if (hi + hj != 0) {
					chi += Math.pow(hi - hj, 2) / (hi + hj);
				}
			}
		}
		return chi * .5d;
	}
}
