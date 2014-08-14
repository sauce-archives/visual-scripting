package org.testobject.commons.util.collections;

/**
 * 
 * @author nijkamp
 *
 */
public class ArrayUtils
{
	public static <T> boolean contains(T[] array, T object)
	{
		for(T element : array)
		{
			if(element.equals(object))
			{
				return true;
			}
		}
		return false;
	}
}
