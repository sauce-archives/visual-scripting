package org.testobject.commons.util.lang.mutable;

/**
 * 
 * @author enijkamp
 *
 */
public class MutableBoolean {
	
	public boolean value;
	
	public MutableBoolean() {
		this(false);
	}

	public MutableBoolean(boolean value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		return value ? 1 : 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MutableBoolean other = (MutableBoolean) obj;
		if (value != other.value)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return value ? "true" : "false";
	}
}
