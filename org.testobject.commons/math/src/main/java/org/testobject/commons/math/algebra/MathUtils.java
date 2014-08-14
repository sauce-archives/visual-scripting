package org.testobject.commons.math.algebra;

public class MathUtils {

	public static long lround(double number) {
		return (long) (Math.round(number) + 0.5);
	}

	public static int round(double number) {
		return (int) (Math.round(number) + 0.5);
	}

	public static int round(float number) {
		return (int) (Math.round(number) + 0.5);
	}

	public static int signum(long number) {
		if (number < 0)
			return -1;
		else if (number == 0)
			return 0;
		else
			return 1;
	}

	public static int compare(long n1, long n2) {
		if (n1 < n2)
			return -1;
		else if (n1 == n2)
			return 0;
		else
			return 1;
	}

	public static int floor(float x) {
		return (int) (Math.floor(x) + 0.5f);
	}

	public static double sqr(double x) {
		return x * x;
	}

	public static double cub(double x) {
		return x * x * x;
	}

	public static int ceil(float x) {
		return (int) (Math.ceil(x) + (x > 0 ? 0.5f : -0.5f));
	}

	public static int floor(double x) {
		return (int) (Math.floor(x) + (x > 0 ? 0.5 : -0.5));
	}

	public static int ceil(double x) {
		return (int) (Math.ceil(x) + (x > 0 ? 0.5 : -0.5));
	}

	public static int div(int a, int b) {
		return (a + b / 2) / b;
	}

	public static long div(long a, long b) {
		return (a + b / 2) / b;
	}

	public static int trim(int value, int from, int to) {
		if (value < from)
			value = from;
		if (value >= to)
			value = to - 1;
		return value;
	}

	public static double max(double n1, double n2, double n3) {
		return Math.max(n1, Math.max(n2, n3));
	}

	public static double max(double... values) {
		double max = -Double.MAX_VALUE;
		for (double value : values)
			max = Math.max(max, value);
		return max;
	}

	public static double min(double n1, double n2, double n3) {
		return Math.min(n1, Math.min(n2, n3));
	}

	/**
	 * Rounds <code>x</code> up to a lowest number that is greater or equal to
	 * <code>x</code> and is divisible by <code>mod</code>.
	 * 
	 * @param x
	 *            the number to round, must be >= 0
	 * @param mod
	 *            the modulo, must be >= 1
	 * 
	 * @return the rounded value
	 */
	public static int modCeil(int x, int mod) {
		return x + (mod - x % mod) % mod;
	}

	/**
	 * Rounds <code>x</code> up to a highest number that is less or equal to
	 * <code>x</code> and is divisible by <code>mod</code>.
	 * 
	 * @param x
	 *            the number to round, must be >= 0
	 * @param mod
	 *            the modulo, must be >= 1
	 * 
	 * @return the rounded value
	 */
	public static int modFloor(int x, int mod) {
		return x - (x % mod);
	}

	public static int signum(double d) {
		return (int) Math.signum(d);
	}

	public static int safeParseInt(String text, int def) {
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	public static int fix(int value, int min, int max) {
		assert min <= max;
		if (value < min)
			return min;
		else if (value > max)
			return max;
		else
			return value;

	}

	public static double truncate(double x, double gran) {
		return Math.round(x / gran) * gran;
	}

	public static int hashCode(Object value) {
		return value != null ? value.hashCode() : 0;
	}

	public static int hashCode(double value) {
		long bits = Double.doubleToLongBits(value);
		return (int) (bits ^ (bits >>> 32));
	}

	public static int hashCode(long value) {
		return (int) (value ^ (value >>> 32));
	}

	public static int hashCode(boolean value) {
		return value ? 1231 : 1237;
	}

	public static double parseDimension(String spec) {
		return Double.parseDouble(spec.substring(0, spec.length() - 2))
		        * getScale(spec.substring(spec.length() - 2));
	}

	private static double getScale(String unit) {
		if (unit.equals("mm"))
			return 72 / 25.4;
		else if (unit.equals("pt"))
			return 1;
		else if (unit.equals("in"))
			return 72;
		else
			throw new IllegalArgumentException("unknown scaling factor " + unit);
	}

	public static double mod(double value, double mod) {
		if (value < 0 || mod <= 0)
			throw new IllegalArgumentException("value must be >= 0");
		int index = (int) (value / mod);
		value -= mod * index;
		if (value < 0)
			return 0;
		else if (value >= mod)
			return 0;
		else
			return value;
	}

	public static boolean equals(double x1, double x2, double epsilon) {
		double abs = Math.max(Math.abs(x1), Math.abs(x2)) * epsilon;
		return Math.abs(x1 - x2) < abs;
	}

	public static boolean isIn(int current, int low, int high) {
		return low <= current && current < high;
	}

}