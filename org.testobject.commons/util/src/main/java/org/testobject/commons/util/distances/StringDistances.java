package org.testobject.commons.util.distances;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author nijkamp
 *
 */
public class StringDistances
{

	/** Main Function **/
	public static void main(String[] args) throws IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Wagner Fischer Test\n");

		/** Accept two strings **/
		System.out.println("\nEnter string 1 :");
		String str1 = br.readLine();
		System.out.println("\nEnter string 2 :");
		String str2 = br.readLine();

		Distance lDist = getSubstringNormalizedDistance(str1, str2);

		System.out.println("\nLevenshtein Distance = " + lDist);
	}

	/*
	 * returns a normalized distance between 1..0, 
	 * where 1 is a perfect match. (id)
	 * 
	 */

	public static Distance getSubstringNormalizedDistance(String query, String target) {
		query = query.trim();
		target = target.trim();
		int maxLength = query.length();
		
		int swapCosts = 0;
		if(query.length() > target.length()){
			swapCosts = query.length() - target.length();
			String tmp = query;
			query = target;
			target = tmp;
		}

 		Distance distance = getSubstringLevenshteinDistance(query, target);
		
		int position = Math.max(-1, swapCosts == 0 && query.length() != target.length() ? distance.position  - query.length() : 0);
		float score = position >= 0 ? 1 - ((distance.propability + swapCosts) / maxLength) : 0;

		return new Distance(score, position, query.length());
	}

	public static class Distance {
		
		public final float propability;
		public final int position;
		public final  int length;

		public Distance(float costs, int position, int length) {
			this.propability = costs;
			this.position = position;
			this.length = length;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + length;
			result = prime * result + position;
			result = prime * result + Float.floatToIntBits(propability);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Distance other = (Distance) obj;
			if (length != other.length)
				return false;
			if (position != other.position)
				return false;
			if (Float.floatToIntBits(propability) != Float.floatToIntBits(other.propability))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "propability: " + propability + ", position: " + position + ", length: " + length;
		}
	}

	public static Distance getSubstringLevenshteinDistance(String t, String s) {
		int n = s.length(); // length of s
		int m = t.length(); // length of t

		if (n == 0)
		{
			return new Distance(m, 0, 0);
		}
		else if (m == 0)
		{
			return new Distance(m, 0, 0);
		}

		int p[] = new int[n + 1]; //'previous' cost array, horizontally
		int d[] = new int[n + 1]; // cost array, horizontally
		int _d[]; //placeholder to assist in swapping p and d

		// indexes into strings s and t
		int i; // iterates through s
		int j; // iterates through t

		char t_j; // jth character of t

		int cost; // cost

		for (j = 1; j <= m; j++)
		{
			t_j = t.charAt(j - 1);
			d[0] = j;

			for (i = 1; i <= n; i++)
			{
				cost = s.charAt(i - 1) == t_j ? 0 : 1;
				// minimum of cell to the left+1, to the top+1, diagonally left and up +cost                
				d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
			}

			// copy current distance counts to 'previous row' distance counts
			_d = p;
			p = d;
			d = _d;
		}

		// our last action in the above loop was to switch d and p, so p now 
		// actually has the most recent cost counts
		return min(t.length(), p);
	}

	private static Distance min(int length, int... value) {
		int smallest = value[0];
		int position = 0;
		for (int i = 1; i < value.length; i++) {
			if (smallest > value[i]) {
				smallest = value[i];
				position = i;
			}
		}
		return new Distance(smallest, position, length);
	}

	/**
	 * http://www.merriampark.com/ldjava.htm
	 * @param s
	 * @param t
	 * @return
	 */
	public static int getLevenshteinDistance(String s, String t)
	{
		/*
		  The difference between this impl. and the previous is that, rather 
		   than creating and retaining a matrix of size s.length()+1 by t.length()+1, 
		   we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
		   is the 'current working' distance array that maintains the newest distance cost
		   counts as we iterate through the characters of String s.  Each time we increment
		   the index of String t we are comparing, d is copied to p, the second int[].  Doing so
		   allows us to retain the previous cost counts as required by the algorithm (taking 
		   the minimum of the cost count to the left, up one, and diagonally up and to the left
		   of the current cost count being calculated).  (Note that the arrays aren't really 
		   copied anymore, just switched...this is clearly much better than cloning an array 
		   or doing a System.arraycopy() each time  through the outer loop.)

		   Effectively, the difference between the two implementations is this one does not 
		   cause an out of memory condition when calculating the LD over two very large strings.          
		*/

		int n = s.length(); // length of s
		int m = t.length(); // length of t

		if (n == 0)
		{
			return m;
		}
		else if (m == 0)
		{
			return n;
		}

		int p[] = new int[n + 1]; //'previous' cost array, horizontally
		int d[] = new int[n + 1]; // cost array, horizontally
		int _d[]; //placeholder to assist in swapping p and d

		// indexes into strings s and t
		int i; // iterates through s
		int j; // iterates through t

		char t_j; // jth character of t

		int cost; // cost

		for (i = 0; i <= n; i++)
		{
			p[i] = i;
		}

		for (j = 1; j <= m; j++)
		{
			t_j = t.charAt(j - 1);
			d[0] = j;

			for (i = 1; i <= n; i++)
			{
				cost = s.charAt(i - 1) == t_j ? 0 : 1;
				// minimum of cell to the left+1, to the top+1, diagonally left and up +cost                
				d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
			}

			// copy current distance counts to 'previous row' distance counts
			_d = p;
			p = d;
			d = _d;
		}

		// our last action in the above loop was to switch d and p, so p now 
		// actually has the most recent cost counts
		return p[n];
	}

	public static int getDamerauLevenshteinDistance(String source, String target) {
		return getDamerauLevenshteinDistance(source, target, 1, 1, 1, 1);
	}

	public static int getDamerauLevenshteinDistanceForArrays(int[] source, int[] target, int deleteCost, int insertCost, int replaceCost,
			int swapCost) {

		if (source.length == 0) {
			return target.length * insertCost;
		}
		if (target.length == 0) {
			return source.length * deleteCost;
		}
		int[][] table = new int[source.length][target.length];
		Map<Integer, Integer> sourceIndexByCharacter = new HashMap<>();
		if (source[0] != target[0]) {
			table[0][0] = Math.min(replaceCost, deleteCost + insertCost);
		}
		sourceIndexByCharacter.put(source[0], 0);
		for (int i = 1; i < source.length; i++) {
			int deleteDistance = table[i - 1][0] + deleteCost;
			int insertDistance = (i + 1) * deleteCost + insertCost;
			int matchDistance = i * deleteCost
					+ (source[i] == target[i] ? 0 : replaceCost);
			table[i][0] = Math.min(Math.min(deleteDistance, insertDistance),
					matchDistance);
		}
		for (int j = 1; j < target.length; j++) {
			int deleteDistance = table[0][j - 1] + insertCost;
			int insertDistance = (j + 1) * insertCost + deleteCost;
			int matchDistance = j * insertCost
					+ (source[0] == target[j] ? 0 : replaceCost);
			table[0][j] = Math.min(Math.min(deleteDistance, insertDistance),
					matchDistance);
		}
		for (int i = 1; i < source.length; i++) {
			int maxSourceLetterMatchIndex = source[i] == target[0] ? 0 : -1;
			for (int j = 1; j < target.length; j++) {
				Integer candidateSwapIndex = sourceIndexByCharacter.get(target[j]);
				int jSwap = maxSourceLetterMatchIndex;
				int deleteDistance = table[i - 1][j] + deleteCost;
				int insertDistance = table[i][j - 1] + insertCost;
				int matchDistance = table[i - 1][j - 1];
				if (source[i] != target[j]) {
					matchDistance += replaceCost;
				} else {
					maxSourceLetterMatchIndex = j;
				}
				int swapDistance;
				if (candidateSwapIndex != null && jSwap != -1) {
					int iSwap = candidateSwapIndex;
					int preSwapCost;
					if (iSwap == 0 && jSwap == 0) {
						preSwapCost = 0;
					} else {
						preSwapCost = table[Math.max(0, iSwap - 1)][Math.max(0,
								jSwap - 1)];
					}
					swapDistance = preSwapCost + (i - iSwap - 1) * deleteCost
							+ (j - jSwap - 1) * insertCost + swapCost;
				} else {
					swapDistance = Integer.MAX_VALUE;
				}
				table[i][j] = Math.min(
						Math.min(Math.min(deleteDistance, insertDistance),
								matchDistance), swapDistance);
			}
			sourceIndexByCharacter.put(source[i], i);
		}
		return table[source.length - 1][target.length - 1];
	}

	public static int getDamerauLevenshteinDistance(String source, String target, int deleteCost, int insertCost,
			int replaceCost, int swapCost) {
		if (source.length() == 0) {
			return target.length() * insertCost;
		}
		if (target.length() == 0) {
			return source.length() * deleteCost;
		}
		int[][] table = new int[source.length()][target.length()];
		Map<Character, Integer> sourceIndexByCharacter = new HashMap<Character, Integer>();
		if (source.charAt(0) != target.charAt(0)) {
			table[0][0] = Math.min(replaceCost, deleteCost + insertCost);
		}
		sourceIndexByCharacter.put(source.charAt(0), 0);
		for (int i = 1; i < source.length(); i++) {
			int deleteDistance = table[i - 1][0] + deleteCost;
			int insertDistance = (i + 1) * deleteCost + insertCost;
			int matchDistance = i * deleteCost
					+ (source.charAt(i) == target.charAt(0) ? 0 : replaceCost);
			table[i][0] = Math.min(Math.min(deleteDistance, insertDistance),
					matchDistance);
		}
		for (int j = 1; j < target.length(); j++) {
			int deleteDistance = table[0][j - 1] + insertCost;
			int insertDistance = (j + 1) * insertCost + deleteCost;
			int matchDistance = j * insertCost
					+ (source.charAt(0) == target.charAt(j) ? 0 : replaceCost);
			table[0][j] = Math.min(Math.min(deleteDistance, insertDistance),
					matchDistance);
		}
		for (int i = 1; i < source.length(); i++) {
			int maxSourceLetterMatchIndex = source.charAt(i) == target
					.charAt(0) ? 0 : -1;
			for (int j = 1; j < target.length(); j++) {
				Integer candidateSwapIndex = sourceIndexByCharacter.get(target
						.charAt(j));
				int jSwap = maxSourceLetterMatchIndex;
				int deleteDistance = table[i - 1][j] + deleteCost;
				int insertDistance = table[i][j - 1] + insertCost;
				int matchDistance = table[i - 1][j - 1];
				if (source.charAt(i) != target.charAt(j)) {
					matchDistance += replaceCost;
				} else {
					maxSourceLetterMatchIndex = j;
				}
				int swapDistance;
				if (candidateSwapIndex != null && jSwap != -1) {
					int iSwap = candidateSwapIndex;
					int preSwapCost;
					if (iSwap == 0 && jSwap == 0) {
						preSwapCost = 0;
					} else {
						preSwapCost = table[Math.max(0, iSwap - 1)][Math.max(0,
								jSwap - 1)];
					}
					swapDistance = preSwapCost + (i - iSwap - 1) * deleteCost
							+ (j - jSwap - 1) * insertCost + swapCost;
				} else {
					swapDistance = Integer.MAX_VALUE;
				}
				table[i][j] = Math.min(
						Math.min(Math.min(deleteDistance, insertDistance),
								matchDistance), swapDistance);
			}
			sourceIndexByCharacter.put(source.charAt(i), i);
		}
		return table[source.length() - 1][target.length() - 1];
	}

	/*
	 * returns a normalized distance between 1..0, 
	 * where 1 is a perfect match. (id)
	 * 
	 */

	public static float getNormalizedDistance(String source, String target) {

		float distance = getCostDistance(source, target);
		int maxLength = Math.max(source.length(), target.length());

		if (maxLength == 0) {
			return 1f;
		}

		float score = 1 - distance / maxLength;

		return score;
	}

	/*
	 * generates Levenshtein distance with consideration of character simmilarity (id)
	 * 	
	 */
	public static float getCostDistance(String source, String target) {

		int effInfinity = 9999;

		int len_s = source.length(); // length of s
		int len_t = target.length(); // length of t

		ArrayList<ArrayList<Float>> d = new ArrayList<ArrayList<Float>>();
		d.add(new ArrayList<Float>());
		for (int i = 0; i <= len_t; i++) {
			d.get(0).add((float) i);
		}
		for (int i = 1; i <= len_s; i++) {
			ArrayList<Float> newArray = new ArrayList<Float>(1);
			newArray.add((float) i);
			d.add(newArray);
		}

		for (int i = 0; i < len_s; i++) {
			for (int j = 0; j < len_t; j++) {
				float minCost = effInfinity;

				// delete
				minCost = updateCost(i, j + 1, 1, minCost, d);

				// insert
				minCost = updateCost(i + 1, j, 1, minCost, d);

				float subsCost = 1 - CharSimilarity.singleCharacterSimilarity(source.charAt(i), target.charAt(j));

				minCost = updateCost(i, j, subsCost, minCost, d);

				if (i > 0) {
					float subs21Cost = 1 - CharSimilarity.doubleCharacterSimilarity(source.substring(i - 1, i + 1),
							String.valueOf(target.charAt(j)));
					subs21Cost = subs21Cost != 1 ? subs21Cost : 2;
					minCost = updateCost(i - 1, j, subs21Cost, minCost, d);
				}

				if (j > 0) {
					float subs12Cost = 1 - CharSimilarity.doubleCharacterSimilarity(String.valueOf(source.charAt(i)),
							target.substring(j - 1, j + 1));
					subs12Cost = subs12Cost != 1 ? subs12Cost : 2;
					minCost = updateCost(i, j - 1, subs12Cost, minCost, d);
				}

				if (i > 0 && j > 0) {
					float subs22Cost = 1 - CharSimilarity.doubleCharacterSimilarity(source.substring(i - 1, i + 1),
							target.substring(j - 1, j + 1));
					subs22Cost = subs22Cost != 1 ? subs22Cost : 2;
					minCost = updateCost(i - 1, j - 1, subs22Cost, minCost, d);

					if (source.charAt(i - 1) == target.charAt(j) && source.charAt(i) == target.charAt(j - 1)) {
						float transpCost = 1 - CharSimilarity.singleCharacterSimilarity(source.charAt(i), target.charAt(j));
						minCost = updateCost(i - 1, j - 1, transpCost, minCost, d);
					}
				}

				d.get(i + 1).add(minCost);
			}
		}
		return d.get(len_s).get(len_t);
	}

	private static float updateCost(int baseCostX, int baseCostY, float oprCost, float lowestCostSoFar, ArrayList<ArrayList<Float>> d) {
		if (baseCostX >= 0 && baseCostY >= 0) {
			float newCost = d.get(baseCostX).get(baseCostY) + oprCost;
			if (newCost < lowestCostSoFar) {
				return newCost;
			}
		}

		return lowestCostSoFar;
	}

}
