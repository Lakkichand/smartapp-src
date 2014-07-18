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

package com.dahl.brendan.wordsearch.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 
 * @author Brendan Dahl
 *
 * Activity to display the tutorial to users
 */
public class TutorialActivity extends Activity {
//	private static String LOG_TAG = "TutorialActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.tutorial_main);
		ViewGroup mainView = (ViewGroup)this.findViewById(R.id.tutorial_main);
		TextView text = (TextView)this.getLayoutInflater().inflate(R.layout.tutorial_text_view_message, mainView, false);
		text.setText(R.string.tutorial_message);
		mainView.addView(text);
		this.addSection(mainView, R.string.tutorial_heading_trackball, R.array.tutorial_messages_trackball);
		TextView text2 = (TextView)this.getLayoutInflater().inflate(R.layout.tutorial_text_view_heading, mainView, false);
		text2.setText(R.string.tutorial_heading_touch);
		mainView.addView(text2);
		TextView text3 = (TextView)this.getLayoutInflater().inflate(R.layout.tutorial_text_view_message, mainView, false);
		text3.setText(R.string.tutorial_message2);
		mainView.addView(text3);
		this.addSection(mainView, R.string.tutorial_heading_drag, R.array.tutorial_messages_drag);
		this.addSection(mainView, R.string.tutorial_heading_tap, R.array.tutorial_messages_tap);
		this.addSection(mainView, R.string.tutorial_heading_tips, R.array.tutorial_messages_tips);
	}
	
	/**
	 * 
	 * @param mainView the viewgroup to add a tutorial section into 
	 * @param headingId the resourceid of a string to use for the section header
	 * @param messagesId the resourceid of an array of strings to use for the section tips
	 */
	private void addSection(ViewGroup mainView, int headingId, int messagesId) {
		TextView text = (TextView)this.getLayoutInflater().inflate(R.layout.tutorial_text_view_heading, mainView, false);
		text.setText(headingId);
		mainView.addView(text);
		int count = 1;
		for (String message : this.getResources().getStringArray(messagesId)) {
			text = (TextView)this.getLayoutInflater().inflate(R.layout.tutorial_text_view_message, mainView, false);
			text.setText(count+") "+message);
			mainView.addView(text);
			count++;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.tutorial_options, menu);
		menu.findItem(R.id.menu_quit).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_quit:
			this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
