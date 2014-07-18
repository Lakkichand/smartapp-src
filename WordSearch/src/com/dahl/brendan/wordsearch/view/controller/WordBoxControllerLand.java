package com.dahl.brendan.wordsearch.view.controller;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.dahl.brendan.wordsearch.model.Grid;
import com.dahl.brendan.wordsearch.model.Theme;
import com.dahl.brendan.wordsearch.view.R;

public class WordBoxControllerLand extends ArrayAdapter<String> implements IWordBoxController, Callback {
	final private Set<String> wordsFound = new HashSet<String>();
	final private ListView wordListView;
	final private TextView letterBox;
	final private Handler handler;
	private Theme theme;
	
	public WordBoxControllerLand(Context context, ListView wordList, TextView letterBox) {
		super(context, R.layout.wordlist_text_view);
		this.wordListView = wordList;
		this.wordListView.setClickable(false);
		this.wordListView.setEnabled(false);
		this.wordListView.setAdapter(this);
		this.letterBox = letterBox;
		this.handler = new Handler(this);
	}

	public void setLetter(CharSequence charSequence) {
		Message.obtain(handler, MSG_SET_LETTER_BOX, charSequence).sendToTarget();
	}
	
	public void wordFound(String str) {
		this.wordsFound.add(str);
		Message.obtain(handler, MSG_FOUND_WORD, str).sendToTarget();
	}

	public void resetWords(Grid grid) {
		this.wordsFound.clear();
		this.wordsFound.addAll(grid.getWordFound());
		Message.obtain(handler, MSG_RESET_WORDS, grid).sendToTarget();
	}

	public void updateTheme(Theme theme) {
		this.theme = theme;
		Message.obtain(handler, MSG_UPDATE_THEME).sendToTarget();
	}

	public long getItemId(int position) {
		long id = this.getItem(position).hashCode();
		if (this.wordsFound.contains(getItem(position))) {
			id++;
		}
		if (theme != null) {
			id += theme.hashCode();
		}
		return id;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		TextView view = (TextView)super.getView(position, convertView, parent);
		if (this.wordsFound.contains(this.getItem(position))) {
			view.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
		} else {
			view.setPaintFlags(0);
		}
		if (theme != null) {
			view.setTextColor(theme.normal);
		}
		return view;
	}

	final static private int MSG_SET_LETTER_BOX = 0;
	final static private int MSG_FOUND_WORD = 1;
	final static private int MSG_RESET_WORDS = 2;
	final static private int MSG_UPDATE_THEME = 3;
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_SET_LETTER_BOX: {
			letterBox.setText((CharSequence)msg.obj);
			break;
		}
		case MSG_FOUND_WORD: {
			this.remove((String)msg.obj);
			this.add((String)msg.obj);
			break;
		}
		case MSG_RESET_WORDS: {
			Grid grid = (Grid)msg.obj;
			this.clear();
			for (String str : grid.getWordList()) {
				this.add(str);
			}
			for (String str : grid.getWordFound()) {
				this.add(str);
			}
			break;
		}
		case MSG_UPDATE_THEME: {
			this.letterBox.setTextColor(theme.picked);
			this.notifyDataSetChanged();
			break;
		}
		default: {
			return false;
		}
		}
		return true;
	}
}
