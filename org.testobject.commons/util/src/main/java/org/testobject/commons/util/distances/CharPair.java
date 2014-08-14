package org.testobject.commons.util.distances;

import org.testobject.commons.util.tuple.Pair;

class CharPair extends Pair<Character, Character>{
	
	public CharPair(Character first, Character second) {
		super(first, second);
	}
	
	public static CharPair newInstance(char first, char second){
		return new CharPair(new Character(first), new Character(second));
	}

}