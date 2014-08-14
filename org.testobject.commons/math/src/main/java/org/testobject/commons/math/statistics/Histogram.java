package org.testobject.commons.math.statistics;

/**
 * 
 * @author enijkamp
 *
 */
public interface Histogram {
	
	static class Byte {
	
		public final int[] bins;
		
		public final double min;
		public final double max;
		public final double mean;
		public final double stddev;
		public final double skewness;
		public final double kurtosis;
		
		public Byte(int[] bins, double min, double max, double mean,
				double stddev, double skewness,
				double kurtosis) {
			this.bins = bins;
			this.min = min;
			this.max = max;
			this.mean = mean;
			this.stddev = stddev;
			this.skewness = skewness;
			this.kurtosis = kurtosis;
		}
		
		public static class Builder {
	
			public static Byte compute(byte[] values) {
	
				int[] bins = new int[256];
				double min = Double.MAX_VALUE;
				double max = Double.MIN_VALUE;
				double[] moments = new double[5];
	
				for (byte signed : values) {
					int x = (int) (signed & 0xff);
					int x2 = x * x;
					
					bins[x]++;
	
					min = Math.min(min, x);
					max = Math.max(max, x);
					
					moments[0] += 1;
					moments[1] += x;
					moments[2] += x2;
					moments[3] += x2 * x;
					moments[4] += x2 * x2;
				}
				
				// convert power sums to central moments
				double n = moments[0];
				double sum = moments[1];
				
				// mean = 1/n * sum
			    double mean = sum / n;
			    
			    // mean^2 = mean * mean
			    double mean_sq = mean * mean;
				
			    double m2 = moments[2]/n;
			    double cm2 = m2 - mean * mean;
			    double m3 = moments[3]/n;
			    double cm3 = 2.0 * mean * mean_sq - 3.0 * mean * m2 + m3;
			    double m4 = moments[4]/n;
			    double cm4 = -3.0 * mean_sq * mean_sq + 6.0 * mean_sq * m2 -4.0 * mean * m3 + m4;
	
				// variance = n / (n-1) * cm2
			    double variance = cm2 * (n/ (n-1.0));
			    
			    // std dev = sqrt (variance)
			    double stddev = Math.sqrt(variance);
				
				// skewness = n^2/ (n-1) (n-2) * cm3/s^3
			    double skewness = (n * ( (n-1) * (n-2))) * n * cm3/(variance * stddev);
	
			    // kurtosis = n(n+1)/(n-1)(n-2)(n-3) * cm4/s^4 - 3(n-1)^2/(n-2)(n-3)
			    double factor1 = (n * (n+1.0))/ ((n-1.0) * (n-2.0) * (n-3.0));
			    double factor2 = (3.0 * (n-1.0) * (n-1.0) )/ ( (n-2.0) * (n-3.0));
			    double kurtosis = factor1 * cm4 * n / (variance*variance) - factor2;
	
				return new Byte(bins, min, max, mean, stddev, skewness, kurtosis);
			}
		}
	}
}
