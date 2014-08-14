package org.testobject.commons.distance;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Ignore;
import org.junit.Test;
import org.testobject.commons.util.distances.StringDistances;
import org.testobject.commons.util.distances.StringDistances.Distance;
import org.testobject.commons.util.tuple.Pair;

public class StringDistanceTest {

	class StringTestPair {

		public String str1;
		public String str2;
		public float expectedResult;

		public StringTestPair(String str1, String str2, float expectedResult) {
			this.str1 = str1;
			this.str2 = str2;
			this.expectedResult = expectedResult;
		}

	}
	
	@Test
	@Ignore
	public void testSubstringDistance(){
		Map<Pair<String, String>, Distance> testData = new LinkedHashMap<>();
		testData.put(new Pair<String, String>("abc123abc", "123"), new Distance(1/3f, 0, 3));
		testData.put(new Pair<String, String>("123", "abc123abc"), new Distance(1, 3, 3));
		testData.put(new Pair<String, String>("123", "123abc"), new Distance(1, 0, 3));
		testData.put(new Pair<String, String>("Elvc", "Create New Project"), new Distance(0.25f, 13, 4));
		testData.put(new Pair<String, String>("Create New Project", "Elvc"), new Distance(17f/18f, 0, 4));
		testData.put(new Pair<String, String>("Create New Project", "Create New Projec1"), new Distance(18f/17f, 0, 18));

		for (Entry<Pair<String, String>, Distance> entry : testData.entrySet()) {
			Distance distance = StringDistances.getSubstringNormalizedDistance(entry.getKey().first, entry.getKey().second);
			assertThat(entry.getValue(), equalTo(distance));
		}
	}

}
