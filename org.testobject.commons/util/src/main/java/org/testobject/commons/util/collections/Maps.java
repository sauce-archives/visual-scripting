package org.testobject.commons.util.collections;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author enijkamp
 *
 */
public class Maps {
	
	public static <T1, T2> Map<T1, T2> newIdentityMap() {
		return new IdentityHashMap<T1, T2>();
	}

	public static Map<String, String> toHashMap(String[][] values) {
		Map<String, String> map = new HashMap<String, String>();
		for (String[] keys : values) {
			if (keys.length > 1) {
				map.put(keys[0], keys[1]);
			}
		}
		return map;
	}
	
	public static Map<Integer, Integer> toHashMap(int[][] values) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int[] keys : values) {
			if (keys.length > 1) {
				map.put(keys[0], keys[1]);
			}
		}
		return map;
	}
	
	public static <K, V> Map<K, V> empty() {
		return Collections.emptyMap();
	}
	
    public static <K, V> String prettyPrint(Map<K, V> map) {
    	if(map == null) {
    		return "";
    	}
    	
        StringBuilder sb = new StringBuilder();
        Iterator<Entry<K, V>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<K, V> entry = iter.next();
            sb.append(entry.getKey());
            sb.append('=').append('"');
            sb.append(entry.getValue());
            sb.append('"');
            if (iter.hasNext()) {
                sb.append(',').append(' ');
            }
        }
        return sb.toString();

    }

}
