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

import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

/**
 * 
 * @author Brendan Dahl
 *
 */
public class DictionaryStringArray implements IDictionary {
//	private final static String LOG_TAG = "DictionaryXML"; 
	private String[] words;
	private final static Random random = new Random();
	private final LinkedList<String> remainingWords = new LinkedList<String>();

	public DictionaryStringArray(String[] words) {
		this.words = words;
	}

	public String getNextWord(int minLength, int maxLength) {
		String str = null;
		int tries = 0;
		try {
			do {
				if (remainingWords.size() == 0) {
					Collections.addAll(remainingWords, words);
				}
				str = remainingWords.remove(random.nextInt(remainingWords.size()));
				tries++;
			} while ((str.length() < minLength || str.length() > maxLength) && tries < DictionaryFactory.MAX_TRIES);
			str = str.toUpperCase();
		} catch (Exception e) {
			// Log.v();
		}
		return str;
	}

}
