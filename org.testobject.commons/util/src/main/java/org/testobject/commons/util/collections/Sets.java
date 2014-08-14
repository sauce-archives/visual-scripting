package org.testobject.commons.util.collections;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author enijkamp
 * 
 */
public class Sets
{
	public static <T> Set<T> newIdentitySet() {
		return Collections.newSetFromMap(new IdentityHashMap<T, Boolean>());
	}

	public static <T> Set<T> concat(Set<T> set1, Set<T> set2) {
		Set<T> result = newIdentitySet();
		result.addAll(set1);
		result.addAll(set2);
		return result;
	}

	public static int[] toArray(Set<Integer> set) {
		int[] result = new int[set.size()];
		int counter = 0;
		for (int i : set) {
			result[counter++] = i;
		}

		return result;
	}
	
	public static <T> List<T> toList(Set<T> set) {
		List<T> list = new java.util.ArrayList<>();
		for(T t : set) {
			list.add(t);
		}
		
		return list;
	}

	public static Set<Integer> fromArray(int ... in) {
		Set<Integer> result = new HashSet<>();
		for (Integer integer : in) {
			result.add(integer);
		}

		return Collections.unmodifiableSet(result);
	}

	public static Set<Character> fromArray(char ... in) {
		Set<Character> result = new HashSet<>();
		for (Character integer : in) {
			result.add(integer);
		}

		return result;
	}

	public static Set<String> from(String ... strings) {
		Set<String> result = new HashSet<>();
		for (String string : strings) {
			result.add(string);
		}

		return result;
	}

	public static <T> Set<T> empty() {
		return new HashSet<T>();
	}

	public static <T> Set<T> toSet(T t1) {
		Set<T> set = new HashSet<T>();
		set.add(t1);
		return set;
	}

}
