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

package com.dahl.brendan.wordsearch.view.controller;

import java.util.List;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.dahl.brendan.wordsearch.model.Grid;
import com.dahl.brendan.wordsearch.model.Theme;
import com.dahl.brendan.wordsearch.view.R;

/**
 * 
 * @author Brendan Dahl
 *
 * handles the logic of displaying the words that the user is to hunt in the grid
 */
public class WordBoxController implements OnClickListener, IWordBoxController, Callback {
	final private Button next;
	final private Button prev;
	final private TextView wordBox;
	final private TextView letterBox;
	final private Handler handler;
	private List<String> words;
	private int wordsIndex = 0;

	protected WordBoxController(Button prev, Button next, TextView wordBox, TextView letterBox) {
		this.letterBox = letterBox;
		this.prev = prev;
		this.prev.setOnClickListener(this);
		this.next = next;
		this.next.setOnClickListener(this);
		this.wordBox = wordBox;
		this.handler = new Handler(this);
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.prev:
			if (wordsIndex > 0) {
				wordsIndex--;
			}
			break;
		case R.id.next:
			if (wordsIndex < words.size() - 1) {
				wordsIndex++;
			}
			break;
		default:
			return;
		}
		Message.obtain(handler, MSG_UPDATE_WORD_BOX).sendToTarget();
	}

	public void resetWords(Grid grid) {
		this.words = grid.getWordList();
		this.wordsIndex = 0;
		Message.obtain(handler, MSG_UPDATE_WORD_BOX).sendToTarget();
	}

	public void setLetter(CharSequence charSequence) {
		if (charSequence != null && charSequence.length() > 1) {
			charSequence = String.valueOf(charSequence.charAt(charSequence.length()-1));
		}
		Message.obtain(handler, MSG_SET_LETTER_BOX, charSequence).sendToTarget();
	}
	
	public void wordFound(String str) {
		words.remove(str);
		wordsIndex = 0;
		Message.obtain(handler, MSG_UPDATE_WORD_BOX).sendToTarget();
	}

	public void updateTheme(Theme theme) {
		Message.obtain(handler, MSG_UPDATE_THEME, theme).sendToTarget();
	}

	final static private int MSG_SET_LETTER_BOX = 0;
	final static private int MSG_UPDATE_WORD_BOX = 1;
	final static private int MSG_UPDATE_THEME = 2;
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_SET_LETTER_BOX: {
			CharSequence letterBoxText = (CharSequence)msg.obj;
			if (letterBoxText == null) {
				prev.setVisibility(Button.VISIBLE);
				letterBox.setVisibility(TextView.INVISIBLE);
			} else {
				prev.setVisibility(Button.INVISIBLE);
				letterBox.setText(letterBoxText);
				letterBox.setVisibility(TextView.VISIBLE);
			}
			break;
		}
		case MSG_UPDATE_WORD_BOX: {
			if (wordsIndex < 0 || wordsIndex > words.size()) {
				wordsIndex = 0;
			}
			CharSequence text = "";
			if (words.size() != 0) {
				text = words.get(wordsIndex);
			}
			next.setEnabled(wordsIndex < words.size()-1);
			prev.setEnabled(wordsIndex > 0);
			wordBox.setText(text);
			break;
		}
		case MSG_UPDATE_THEME: { 
			Theme theme = (Theme)msg.obj;
			this.letterBox.setTextColor(theme.picked);
			this.wordBox.setTextColor(theme.normal);
			break;
		}
		default: {
			return false;
		}
		}
		return true;
	}
}
