package org.testobject.commons.util.lang.mutable;

/**
 * 
 * @author enijkamp
 *
 */
public class MutableInt {
	
	public int value;
	
	public MutableInt() {
		this(0);
	}

	public MutableInt(int value) {
		this.value = value;
	}
	
	public MutableInt increment() {
		this.value++;
		return this;
	}

	@Override
	public int hashCode() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MutableInt other = (MutableInt) obj;
		if (value != other.value)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return Integer.toString(value);
	}
}
