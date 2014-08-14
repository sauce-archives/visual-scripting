package org.testobject.commons.util.distances;

import java.util.HashMap;
import java.util.Map;

import org.testobject.commons.util.tuple.Pair;

public class CharSimilarity {

	public static Map<CharPair, Float> singleCharSimilarityTable = getSingleCharTable();
	public static Map<Pair<String, String>, Float> doubleCharSimilarityTable = gettDoubleCharTable();

	public static float singleCharacterSimilarity(char ch1, char ch2) {

		Float similarity = singleCharSimilarityTable.get(CharPair.newInstance(ch1, ch2));
		if (similarity == null) {
			similarity = singleCharSimilarityTable.get(CharPair.newInstance(ch2, ch1));
		} 
		
		if (similarity == null) {
			if (ch1 == ch2){
				similarity = 1f;
			} else if (Character.toLowerCase(ch1) == Character.toLowerCase(ch2)){
				similarity = 0.8f;
			} else {
				similarity = 0f;
			}
		} 
		return similarity;

	}
	
	public static float doubleCharacterSimilarity(String str1, String str2) {

		Float similarity = doubleCharSimilarityTable.get(new Pair<String,String>(str1, str2));
		if (similarity == null) {
			similarity = doubleCharSimilarityTable.get(new Pair<String,String>(str2, str1));
		} 
		
		if (similarity == null) {
			if (str1 == str2){
				similarity = 1f;
			} else if (str1.equalsIgnoreCase(str2)){
				similarity = 0.8f;
			} else {
				similarity = 0f;
			}
		} 
		return similarity;

	}
	
	private static HashMap<CharPair, Float> getSingleCharTable() {
		HashMap<CharPair, Float> result = new HashMap<CharPair, Float>();
		result.put(CharPair.newInstance('0', 'O'), new Float(0.9));
		result.put(CharPair.newInstance('1', '7'), new Float(0.5));
		result.put(CharPair.newInstance('1', 'i'), new Float(0.9));
		result.put(CharPair.newInstance('1', 'l'), new Float(1));
		result.put(CharPair.newInstance('2', 'Z'), new Float(0.1));
		result.put(CharPair.newInstance('2', 'z'), new Float(0.1));
		result.put(CharPair.newInstance('3', 'E'), new Float(0.1));
		result.put(CharPair.newInstance('4', 'A'), new Float(0.1));
		result.put(CharPair.newInstance('4', 'H'), new Float(0.1));
		result.put(CharPair.newInstance('4', '9'), new Float(0.2));
		result.put(CharPair.newInstance('5', 'S'), new Float(0.2));
		result.put(CharPair.newInstance('6', 'b'), new Float(0.3));
		result.put(CharPair.newInstance('8', 'B'), new Float(0.3));
		result.put(CharPair.newInstance('9', 'P'), new Float(0.1));
		result.put(CharPair.newInstance('a', 'c'), new Float(0.2));
		result.put(CharPair.newInstance('a', 'd'), new Float(0.2));
		result.put(CharPair.newInstance('a', 'e'), new Float(0.2));
		result.put(CharPair.newInstance('a', 'o'), new Float(0.3));
		result.put(CharPair.newInstance('A', 'H'), new Float(0.1));
		result.put(CharPair.newInstance('b', 'd'), new Float(0.2));
		result.put(CharPair.newInstance('b', 'h'), new Float(0.2));
		result.put(CharPair.newInstance('B', 'E'), new Float(0.1));
		result.put(CharPair.newInstance('B', 'P'), new Float(0.2));
		result.put(CharPair.newInstance('B', 'R'), new Float(0.2));
		result.put(CharPair.newInstance('c', 'e'), new Float(0.2));
		result.put(CharPair.newInstance('c', 'o'), new Float(0.3));
		result.put(CharPair.newInstance('C', 'G'), new Float(0.2));
		result.put(CharPair.newInstance('d', 'o'), new Float(0.2));
		result.put(CharPair.newInstance('e', 'o'), new Float(0.2));
		result.put(CharPair.newInstance('E', 'F'), new Float(0.2));
		result.put(CharPair.newInstance('F', 'P'), new Float(0.1));
		result.put(CharPair.newInstance('f', 't'), new Float(0.3));
		result.put(CharPair.newInstance('g', 'q'), new Float(0.2));
		result.put(CharPair.newInstance('G', 'O'), new Float(0.1));
		result.put(CharPair.newInstance('h', 'k'), new Float(0.1));
		result.put(CharPair.newInstance('h', 'n'), new Float(0.4));
		result.put(CharPair.newInstance('i', 'j'), new Float(0.5));
		result.put(CharPair.newInstance('I', 'l'), new Float(1));
		result.put(CharPair.newInstance('I', '!'), new Float(0.6));
		result.put(CharPair.newInstance('i', '!'), new Float(0.6));
		result.put(CharPair.newInstance('K', 'X'), new Float(0.1));
		result.put(CharPair.newInstance('m', 'n'), new Float(0.1));
		result.put(CharPair.newInstance('n', 'r'), new Float(0.1));
		result.put(CharPair.newInstance('o', 'p'), new Float(0.1));
		result.put(CharPair.newInstance('o', 'q'), new Float(0.7));
		result.put(CharPair.newInstance('O', 'Q'), new Float(0.4));
		result.put(CharPair.newInstance('p', 'q'), new Float(0.1));
		result.put(CharPair.newInstance('P', 'R'), new Float(0.2));
		result.put(CharPair.newInstance('u', 'v'), new Float(0.1));
		result.put(CharPair.newInstance('v', 'w'), new Float(0.1));
		result.put(CharPair.newInstance('v', 'x'), new Float(0.1));
		result.put(CharPair.newInstance('v', 'y'), new Float(0.2));
		result.put(CharPair.newInstance('x', 'y'), new Float(0.1));
		

		result.put(CharPair.newInstance(',', '.'), new Float(0.4));
		result.put(CharPair.newInstance(',', ' '), new Float(0.4));
		result.put(CharPair.newInstance('.', ' '), new Float(0.4));
		
		
		return result;
	}
	
	private static HashMap<Pair<String, String>, Float> gettDoubleCharTable() {
		HashMap<Pair<String, String>, Float> result = new HashMap<Pair<String, String>, Float>();
		result.put(new Pair<String, String>("cl", "d"), new Float(0.4));	
		result.put(new Pair<String, String>("fl", "f"), new Float(0.5));	
		result.put(new Pair<String, String>("mn", "nm"), new Float(0.5));	
		result.put(new Pair<String, String>("nn", "m"), new Float(0.5));	
		result.put(new Pair<String, String>("AA", "M"), new Float(0.2));
		result.put(new Pair<String, String>("rn", "m"), new Float(1));	
		result.put(new Pair<String, String>("VV", "W"), new Float(0.8));	
		result.put(new Pair<String, String>("ti", "u"), new Float(0.8));
		
		return result;
	}



}
