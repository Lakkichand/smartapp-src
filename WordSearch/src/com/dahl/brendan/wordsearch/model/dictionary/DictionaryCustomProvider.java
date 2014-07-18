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

import android.content.Context;
import android.database.Cursor;

import com.dahl.brendan.wordsearch.view.WordDictionaryProvider.Word;

/**
 * 
 * @author Brendan Dahl
 *
 */
public class DictionaryCustomProvider implements IDictionary {
	
	final private Random random = new Random();
	final private Context ctx;

	protected DictionaryCustomProvider(Context ctx) {
		this.ctx = ctx;
	}

	public String getNextWord(int minLength, int maxLength) {
		Cursor cursor = ctx.getContentResolver().query(Word.CONTENT_URI, new String[] { Word.WORD }, null, null, null);
		String str = null;
		if (cursor!= null) {
			if (cursor.getCount() != 0) {
				cursor.moveToPosition(random.nextInt(cursor.getCount()));
				str = cursor.getString(0);
			}
			cursor.close();
		}
		return str;
	}

}
