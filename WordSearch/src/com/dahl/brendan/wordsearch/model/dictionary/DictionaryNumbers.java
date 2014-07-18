//    This file is part of Open WordSearch.
//
//    Open WordSearch is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Open WordSearch is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Open WordSearch.  If not, see <http://www.gnu.org/licenses/>.
//
//	  Copyright 2009, 2010 Brendan Dahl <dahl.brendan@brendandahl.com>
//	  	http://www.brendandahl.com

package com.dahl.brendan.wordsearch.model.dictionary;

import java.util.Random;

/**
 * 
 * @author Brendan Dahl
 *
 */
public class DictionaryNumbers implements IDictionary {
	private final Random random = new Random();
	
	public String getNextWord(int minLength, int maxLength) {
		int length = minLength;
		int diff = maxLength-minLength;
		if (diff > 0) {
			length += random.nextInt(diff);
		}
		String str = "";
		for (int index = 0; index < length; index++) {
			str += random.nextInt(10);
		}
		return str;
	}

}
