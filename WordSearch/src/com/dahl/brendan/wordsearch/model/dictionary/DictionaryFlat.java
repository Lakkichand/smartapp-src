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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import android.content.Context;
import android.util.Log;

/**
 * 
 * @author Brendan Dahl
 *
 */
public class DictionaryFlat implements IDictionary {
	private final static String LOG_TAG = "DictionaryFlat";
	private final static Random random = new Random();
	private final String fileName;
	private final int fileSize;
	private final Context ctx;
	
	public DictionaryFlat(Context ctx, String fileName2, int fileSize2) {
		this.fileName = fileName2;
		this.fileSize = fileSize2;
		this.ctx = ctx;
	}

	public String getNextWord(int minLength, int maxLength) {
		String str = null;
		int tries = 0;
		do {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(ctx.getAssets().open(fileName)), 10000);
				br.skip(random.nextInt(fileSize));
				br.readLine();
				str = br.readLine();
				br.close();
			} catch (IOException e) {
				str = null;
				Log.e(LOG_TAG,"reading file failed", e);
			}
			tries++;
		} while (str != null && (str.length() < minLength || str.length() > maxLength) && tries < DictionaryFactory.MAX_TRIES);
		return str;
	}
}
