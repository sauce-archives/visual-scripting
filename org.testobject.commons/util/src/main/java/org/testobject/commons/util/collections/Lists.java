package org.testobject.commons.util.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author enijkamp
 * 
 */
public class Lists {
	
	public static <T> ArrayList<T> copy(Collection<T> list) {
		return new ArrayList<T>(list);
	}
	
	public static <T> ArrayList<T> newArrayList() {
		return new ArrayList<T>();
	}
	
	public static <T> ArrayList<T> newArrayList(int size) {
		return new ArrayList<T>(size);
	}
	
	public static <T> ArrayList<T> newArrayList(Collection<T> list) {
		return new ArrayList<T>(list);
	}
	
	public static <T> LinkedList<T> newLinkedList() {
		return new LinkedList<T>();
	}

	public static <T> LinkedList<T> newLinkedList(Collection<T> list) {
		return new LinkedList<T>(list);
	}
	
	public static <T> LinkedList<T> newLinkedList(T[] array) {
		LinkedList<T> list = new LinkedList<T>();
		for(T entry : array) {
			list.add(entry);
		}
		return list;
	}
	
	public static <T> LinkedList<T> newLinkedList(Iterable<T> iterable) {
		LinkedList<T> list = new LinkedList<T>();
		for(T entry : iterable) {
			list.add(entry);
		}
		return list;
	}

	public static <T> LinkedList<T> toLinkedList(T t1)
	{
		LinkedList<T> list = new LinkedList<T>();
		list.add(t1);
		return list;
	}

	public static <T> LinkedList<T> toLinkedList(T t1, T t2) {
		LinkedList<T> list = toLinkedList(t1);
		list.add(t2);
		return list;
	}

	public static <T> LinkedList<T> toLinkedList(T t1, T t2, T t3) {
		LinkedList<T> list = toLinkedList(t1, t2);
		list.add(t3);
		return list;
	}

	public static <T> List<T> toList(Collection<T> collection) {
		return new LinkedList<T>(collection);
	}

	public static <T> List<T> toList(T t1) {
		List<T> list = new ArrayList<T>();
		list.add(t1);
		return list;
	}

	public static <T> List<T> toList(T t1, T t2) {
		List<T> list = toList(t1);
		list.add(t2);
		return list;
	}

	public static <T> List<T> toList(T t1, T t2, T t3) {
		List<T> list = toList(t1, t2);
		list.add(t3);
		return list;
	}

	public static <T> List<T> toList(T t1, T t2, T t3, T t4) {
		List<T> list = toList(t1, t2, t3);
		list.add(t4);
		return list;
	}

	public static <T> LinkedList<T> concat(T t1, LinkedList<T> list) {
		LinkedList<T> result = toLinkedList(t1);
		result.addAll(list);
		return result;
	}
	
	public static <T> List<T> concat(List<T> list, T t1) {
		LinkedList<T> result = new LinkedList<T>(list);
		result.add(t1);
		return result;
	}

	public static <T> LinkedList<T> concat(LinkedList<T> list, T t1) {
		LinkedList<T> result = new LinkedList<T>(list);
		result.add(t1);
		return result;
	}

	public static <T> LinkedList<T> concat(List<T> list1, List<T> list2) {
		LinkedList<T> result = new LinkedList<T>();
		result.addAll(list1);
		result.addAll(list2);
		return result;
	}

	public static <T> LinkedList<T> concat(LinkedList<T> list, T t1, T t2) {
		LinkedList<T> result = concat(list, t1);
		result.add(t2);
		return result;
	}

	public static <T> LinkedList<T> concat(LinkedList<T> list, T t1, T t2, T t3) {
		LinkedList<T> result = concat(list, t1, t2);
		result.add(t3);
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> castList(List<?> list) {
		return (List<T>) (Object) list;
	}
	
	public static <T> List<T> reverse(List<T> list) {
		List<T> reverse = new LinkedList<T>(list);
		Collections.reverse(reverse);
		return reverse;
	}

	public static <T> List<T> empty() {
		return Collections.<T>emptyList();
	}

	public static <T> List<T> immutable(List<T> mutable) {
		return Collections.unmodifiableList(mutable);
	}

}
