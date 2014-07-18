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
//	  Copyright 2009, 2010, 2011 Brendan Dahl <dahl.brendan@brendandahl.com>
//	  	http://www.brendandahl.com

package com.dahl.brendan.wordsearch.view;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dahl.brendan.util.AnalyticsTask;
import com.dahl.brendan.wordsearch.Constants;
import com.dahl.brendan.wordsearch.model.HighScore;
import com.dahl.brendan.wordsearch.util.AndroidHttpClient;
import com.dahl.brendan.wordsearch.util.ConversionUtil;
import com.dahl.brendan.wordsearch.view.WordDictionaryProvider.Word;
import com.dahl.brendan.wordsearch.view.controller.TextViewGridController;
import com.dahl.brendan.wordsearch.view.controller.WordSearchActivityController;

/**
 * 
 * @author Brendan Dahl
 *
 * Activity for the word search game itself
 */
public class WordSearchActivity extends Activity {
	class StartHighScoreGlobalTask extends AsyncTask<Integer, Integer, List<HighScore>> {
		final private ProgressDialog pd = new ProgressDialog(WordSearchActivity.this);

		@Override
		protected void onPostExecute(List<HighScore> highScores) {
			if (!this.isCancelled() && pd.isShowing() && pd.getWindow() != null) {
				try {
					pd.dismiss();
				} catch (IllegalArgumentException e) {
					return;
				}
				if (highScores != null) {
					StringBuilder str = new StringBuilder();
					if (highScores.size() == 0) {
						str.append(getString(R.string.no_high_scores));
					} else {
						Collections.sort(highScores);
						for (int index = 0; index < highScores.size(); index++) {
							str.append(Integer.toString(index+1)+": "+highScores.get(index).getName()+" " + highScores.get(index).getScore() + " ( " + ConversionUtil.formatTime.format(new Date(highScores.get(index).getTime())) + " )\n");
						}
					}
					final DialogHighScoresGlobalShowListener DIALOG_LISTENER_HIGH_SCORES_SHOW = new DialogHighScoresGlobalShowListener();
					AlertDialog.Builder builder = new AlertDialog.Builder(WordSearchActivity.this);
					builder.setMessage(str);
					builder.setTitle(R.string.GLOBAL_HIGH_SCORES);
					builder.setNeutralButton(android.R.string.ok, DIALOG_LISTENER_HIGH_SCORES_SHOW);
					builder.setPositiveButton(R.string.LOCAL, DIALOG_LISTENER_HIGH_SCORES_SHOW);
					builder.show();
				} else {
					final DialogHighScoresGlobalShowListener DIALOG_LISTENER_HIGH_SCORES_SHOW = new DialogHighScoresGlobalShowListener();
					AlertDialog.Builder builder = new AlertDialog.Builder(WordSearchActivity.this);
					builder.setMessage(getString(R.string.SCORE_GLOBAL_ERROR));
					builder.setTitle(R.string.GLOBAL_HIGH_SCORES);
					builder.setNeutralButton(android.R.string.ok, DIALOG_LISTENER_HIGH_SCORES_SHOW);
					builder.show();
				}
			}
		}

		@Override
		protected void onPreExecute() {
			pd.setMessage(getString(R.string.LOADING));
			pd.setIndeterminate(true);
			pd.show();
		}

		@Override
		protected List<HighScore> doInBackground(Integer... res) {
			List<HighScore> results = new LinkedList<HighScore>();
//			Debug.startMethodTracing("globalHS");
			try {
				HttpPost httpPost = new HttpPost(Constants.API_URL_SCORE_TOP10);
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair(Constants.SECURITY_TOKEN, Constants.VALUE_SECRET));
				nvps.add(new BasicNameValuePair(Constants.KEY_HIGH_SCORE_THEME, getControl().getCurrentTheme()));
				httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
				HttpResponse response = null;
				try {
					response = httpClient.execute(httpPost);
				} catch (IllegalStateException ise) {
					httpClient = AndroidHttpClient.newInstance("wordsearch");
					response = httpClient.execute(httpPost);
				}
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				response.getEntity().writeTo(baos);
				JSONArray json = new JSONArray(baos.toString());
				for (int i = 0; i < json.length(); i++) {
					results.add(new HighScore(json.getJSONObject(i)));
				}
			} catch (UnsupportedEncodingException e) {
				results = null;
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				results = null;
				e.printStackTrace();
			} catch (IOException e) {
				results = null;
				e.printStackTrace();
			} catch (JSONException e) {
				results = null;
				e.printStackTrace();
			}
//			Debug.stopMethodTracing();
			return results;
		}
	}
	class HighScoreSubmitTask extends AsyncTask<Integer, Integer, Boolean> {
		final private HighScore hs;
		public HighScoreSubmitTask(HighScore hs) {
			this.hs = hs;
		}
		@Override
		protected Boolean doInBackground(Integer... params) {
//			Debug.startMethodTracing("submit");
			try {
				HttpPost httpPost = new HttpPost(Constants.API_URL_SCORE_SUBMIT);
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair(Constants.SECURITY_TOKEN, Constants.VALUE_SECRET));
				nvps.add(new BasicNameValuePair(Constants.KEY_PAYLOAD, hs.toJSON().toString()));
				httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
				try {
					httpClient.execute(httpPost);
				} catch (IllegalStateException ise) {
					httpClient = AndroidHttpClient.newInstance("wordsearch");
					httpClient.execute(httpPost);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
//			Debug.stopMethodTracing();
			return true;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			getControl().clearCurrentHighScore();
		}
	}
	class DialogGameNewListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				getControl().newWordSearch();
				break;
			case DialogInterface.BUTTON_NEUTRAL:
				getControl().resetGrid();
				break;
			}
		}
	}
	class DialogGameOverListener implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
		public void onClick(DialogInterface dialog, int which) {
			String name = ((EditText)((AlertDialog)dialog).findViewById(android.R.id.input)).getText().toString();
			HighScore hs = getControl().getCurrentHighScore();
			if (!TextUtils.isEmpty(name)) {
				hs.setName(name);
				getControl().getPrefs().setDetaultName(name);
			} else {
				hs.setName("?");
			}
			switch(which) {
			case DialogInterface.BUTTON_POSITIVE: {
				if (!getControl().isReplaying()) {
					new HighScoreSubmitTask(hs).execute(new Integer[0]);
				}
			}
			case DialogInterface.BUTTON_NEUTRAL: {
				LinkedList<HighScore> scores = getControl().getPrefs().getTopScores();
				scores.add(hs);
				getControl().getPrefs().setTopScores(scores);
				showDialog(WordSearchActivity.DIALOG_ID_GAME_NEW);
				break;
			}
			}
			removeDialog(DIALOG_ID_GAME_OVER);
		}

		public void onCancel(DialogInterface dialog) {
			removeDialog(DIALOG_ID_GAME_OVER);
		}
	}
	class DialogHighScoresGlobalShowListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			if (which == DialogInterface.BUTTON_NEGATIVE) {
				control.getPrefs().resetTopScores();
			}
			if (which == DialogInterface.BUTTON_POSITIVE) {
				showDialog(WordSearchActivity.DIALOG_ID_HIGH_SCORES_LOCAL_SHOW);
			} else if (!getControl().isGameRunning()) {
				showDialog(WordSearchActivity.DIALOG_ID_GAME_NEW);
			}
		}
	}
	class DialogHighScoresLocalShowListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			if (which == DialogInterface.BUTTON_NEGATIVE) {
				control.getPrefs().resetTopScores();
			}
			if (which == DialogInterface.BUTTON_POSITIVE) {
				new StartHighScoreGlobalTask().execute(new Integer[0]);
			} else if (!getControl().isGameRunning()) {
				showDialog(WordSearchActivity.DIALOG_ID_GAME_NEW);
			}
		}
	}
	class DialogNoWordsCustomListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				Intent intent = new Intent(Intent.ACTION_EDIT, com.dahl.brendan.wordsearch.view.WordDictionaryProvider.Word.CONTENT_URI);
				intent.setType(com.dahl.brendan.wordsearch.view.WordDictionaryProvider.Word.CONTENT_TYPE);
				startActivity(intent);
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				startActivity(new Intent(WordSearchActivity.this, WordSearchPreferences.class));
				break;
			default:
				break;
			}
		}
	}
	class DialogNoWordsListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				getControl().newWordSearch();
				break;
			case DialogInterface.BUTTON_NEUTRAL:
				startActivity(new Intent(WordSearchActivity.this, WordSearchPreferences.class));
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				startActivity(new Intent(WordSearchActivity.this, WordSearchPreferences.class));
				break;
			default:
				break;
			}
		}
	}
	class DialogIntroListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE: {
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(WordSearchActivity.this);
				sp.edit().putString(getString(R.string.prefs_touch_mode), getString(R.string.TAP)).commit();
				break;
			}
			case DialogInterface.BUTTON_NEUTRAL:
				break;
			case DialogInterface.BUTTON_NEGATIVE: {
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(WordSearchActivity.this);
				sp.edit().putString(getString(R.string.prefs_touch_mode), getString(R.string.DRAG)).commit();
				break;
			}
			default:
				break;
			}
		}
	}
	class DialogDonateListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE: {
				String url = "market://details?id=com.dahl.brendan.donate";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
				break;
			}
			case DialogInterface.BUTTON_NEUTRAL: {
				String url = "http://www.brendandahl.com/wordsearch/donate";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
				break;
			}
			case DialogInterface.BUTTON_NEGATIVE: {
				break;
			}
			default:
				break;
			}
			control.getPrefs().setDonateIgnore();
		}
	}
	class DialogIntroDonateListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE: {
				showDialog(DIALOG_ID_DONATE);
				break;
			}
			case DialogInterface.BUTTON_NEGATIVE: {
				control.getPrefs().setDonateIgnore();
				break;
			}
			default:
				break;
			}
		}
	}
	final public static int DIALOG_ID_NO_WORDS = 0;
	final public static int DIALOG_ID_NO_WORDS_CUSTOM = 1;
	final public static int DIALOG_ID_GAME_OVER = 2;
	final public static int DIALOG_ID_HIGH_SCORES_LOCAL_SHOW = 3;
	final public static int DIALOG_ID_GAME_NEW = 5;
	final public static int DIALOG_ID_INTRO_INPUT_TYPE = 6;
	final public static int DIALOG_ID_INTRO_DONATE = 7;
	final public static int DIALOG_ID_DONATE = 8;

	final private static String LOG_TAG = "WordSearchActivity";
	/**
	 * control classes were made to segment the complex game logic away from the display logic
	 */
	private WordSearchActivityController control;
	private String appVer;

	public WordSearchActivityController getControl() {
		return control;
	}

	public static AndroidHttpClient httpClient = null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			appVer = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			appVer = "unknown";
		}
		try {
			AnalyticsTask analytics = new AnalyticsTask(this, true);
			analytics.execute(new String[] {"/WordSearchActivity"});
		} catch (RuntimeException re) {
			Log.e(LOG_TAG, "tracker failed!");
		} catch (Exception e) {
			Log.e(LOG_TAG, "tracker failed!");
		}
		setContentView(R.layout.wordsearch_main);
		control = new WordSearchActivityController(this);
		control.restoreState(savedInstanceState);
		{
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			if (!appVer.equals(sp.getString(Constants.KEY_INTRO_VER, null)) && sp.getString(getString(R.string.prefs_touch_mode), null) == null) {
				this.showDialog(DIALOG_ID_INTRO_INPUT_TYPE);
				sp.edit().putString(Constants.KEY_INTRO_VER, appVer).commit();
			} else if (control.getPrefs().getGamePlayCount() >= Constants.DONATE_GAME_PLAY_COUNT && !control.getPrefs().isDonateIngored()) {
				this.showDialog(DIALOG_ID_INTRO_DONATE);
			}
		}
		httpClient = AndroidHttpClient.newInstance("wordsearch");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (httpClient != null) {
			httpClient.close();
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch(id) {
		case DIALOG_ID_NO_WORDS: {
			final DialogNoWordsListener DIALOG_LISTENER_NO_WORDS = new DialogNoWordsListener();
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.no_words);
			builder.setNegativeButton(R.string.category, DIALOG_LISTENER_NO_WORDS);
			builder.setPositiveButton(R.string.new_game, DIALOG_LISTENER_NO_WORDS);
			builder.setNeutralButton(R.string.size, DIALOG_LISTENER_NO_WORDS);
			dialog = builder.create();
			break;
		}
		case DIALOG_ID_NO_WORDS_CUSTOM: {
			final DialogNoWordsCustomListener DIALOG_LISTENER_NO_WORDS_CUSTOM = new DialogNoWordsCustomListener();
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.no_words_custom);
			builder.setNegativeButton(R.string.category, DIALOG_LISTENER_NO_WORDS_CUSTOM);
			builder.setPositiveButton(R.string.custom_editor, DIALOG_LISTENER_NO_WORDS_CUSTOM);
			dialog = builder.create();
			break;
		}
		case DIALOG_ID_GAME_OVER: {
			final DialogGameOverListener DIALOG_LISTENER_GAME_OVER = new DialogGameOverListener();
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("blank");
			EditText text = new EditText(this);
			text.setSingleLine();
			text.setId(android.R.id.input);
			builder.setView(text);
			builder.setPositiveButton(R.string.SAVE_SUBMIT, DIALOG_LISTENER_GAME_OVER);
			builder.setNeutralButton(R.string.SAVE, DIALOG_LISTENER_GAME_OVER);
			builder.setOnCancelListener(DIALOG_LISTENER_GAME_OVER);
			dialog = builder.create();
			break;
		}
		case DIALOG_ID_HIGH_SCORES_LOCAL_SHOW: {
			final DialogHighScoresLocalShowListener DIALOG_LISTENER_HIGH_SCORES_SHOW = new DialogHighScoresLocalShowListener();
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("blank");
			builder.setTitle(R.string.LOCAL_HIGH_SCORES);
			builder.setNegativeButton(R.string.reset, DIALOG_LISTENER_HIGH_SCORES_SHOW);
			builder.setNeutralButton(android.R.string.ok, DIALOG_LISTENER_HIGH_SCORES_SHOW);
			builder.setPositiveButton(R.string.GLOBAL, DIALOG_LISTENER_HIGH_SCORES_SHOW);
			dialog = builder.create();
			break;
		}
		case DIALOG_ID_GAME_NEW: {
			final DialogGameNewListener DIALOG_LISTENER_GAME_NEW = new DialogGameNewListener();
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(this.getString(R.string.game_over));
			builder.setPositiveButton(R.string.new_game, DIALOG_LISTENER_GAME_NEW);
			builder.setNeutralButton(R.string.REPLAY, DIALOG_LISTENER_GAME_NEW);
			builder.setNegativeButton(android.R.string.cancel, DIALOG_LISTENER_GAME_NEW);
			dialog = builder.create();
			break;
		}
		case DIALOG_ID_INTRO_INPUT_TYPE: {
			final DialogIntroListener DIALOG_LISTENER_INTRO = new DialogIntroListener();
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.INTRO);
			builder.setPositiveButton(R.string.tap, DIALOG_LISTENER_INTRO);
			builder.setNeutralButton(android.R.string.cancel, DIALOG_LISTENER_INTRO);
			builder.setNegativeButton(R.string.drag, DIALOG_LISTENER_INTRO);
			dialog = builder.create();
			break;
		}
		case DIALOG_ID_INTRO_DONATE: {
			final DialogInterface.OnClickListener LISTENER = new DialogIntroDonateListener();
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.INTRO_DONATE_MSG);
			builder.setPositiveButton(R.string.DONATE, LISTENER);
			builder.setNegativeButton(R.string.DONATE_NO, LISTENER);
			dialog = builder.create();
			break;
		}
		case DIALOG_ID_DONATE: {
			final DialogInterface.OnClickListener LISTENER = new DialogDonateListener();
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.DONATE_MSG);
			builder.setPositiveButton(R.string.DONATE_MARKET, LISTENER);
			builder.setNeutralButton(R.string.DONATE_WEB, LISTENER);
			builder.setNegativeButton(R.string.DONATE_NO, LISTENER);
			dialog = builder.create();
			break;
		}
		default:
			dialog = super.onCreateDialog(id);
			break;
		}
		return dialog;
	}

	/** hook into menu button for activity */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.wordsearch_options, menu);
		menu.findItem(R.id.menu_new).setIcon(android.R.drawable.ic_menu_add);
		menu.findItem(R.id.menu_options).setIcon(android.R.drawable.ic_menu_preferences);
		menu.findItem(R.id.menu_custom).setIcon(android.R.drawable.ic_menu_edit);
		menu.findItem(R.id.menu_tutorial).setIcon(android.R.drawable.ic_menu_help);
		menu.findItem(R.id.menu_scores).setIcon(android.R.drawable.ic_menu_gallery);
		menu.findItem(R.id.menu_donate).setIcon(R.drawable.love);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch(id) {
		case DIALOG_ID_GAME_OVER: {
			HighScore hs = control.getCurrentHighScore();
			TextView label = (TextView)((AlertDialog)dialog).findViewById(android.R.id.message);
			String msg = this.getString(R.string.SCORE_CONGRATULATIONS).replace("%replaceme", hs.getScore().toString()+" ("+ConversionUtil.formatTime.format(new Date(hs.getTime()))+")");
			if (hs.isHighScore()) {
				msg += this.getString(R.string.SCORE_LOCAL_HIGH).replace("%replaceme", Integer.toString(hs.getRank()+1));
			}
			if (hs.isGlobalError()) {
				msg += this.getString(R.string.SCORE_GLOBAL_ERROR);
			} else {
				String global = "";
				if (hs.isGlobalHighScore()) {
					global = this.getString(R.string.SCORE_GLOBAL_HIGH);
				} else {
					global = this.getString(R.string.SCORE_GLOBAL_PERCENT);
				}
				msg += global.replace("%replaceme", Integer.toString(hs.getGlobalRank()));
			}
			EditText edit = (EditText)((AlertDialog)dialog).findViewById(android.R.id.input);
			Button save = (Button)((AlertDialog)dialog).findViewById(android.R.id.button3);
			edit.setText(getControl().getPrefs().getDefaultName());
			if (hs.isHighScore()) {
				save.setVisibility(EditText.VISIBLE);
			} else {
				save.setVisibility(EditText.GONE);
			}
			if (hs.isHighScore() || !hs.isGlobalError()) {
				edit.setVisibility(EditText.VISIBLE);
				msg += this.getString(R.string.SCORE_INITIALS);
			} else {
				edit.setVisibility(EditText.GONE);
			}
			label.setText(msg);
			break;
		}
		case DIALOG_ID_HIGH_SCORES_LOCAL_SHOW: {
//			Debug.startMethodTracing("localHS");
			List<HighScore> highScores = this.getControl().getHighScores();
			StringBuilder str = new StringBuilder();
			if (highScores.size() == 0) {
				str.append(this.getString(R.string.no_high_scores));
			} else {
				Collections.sort(highScores);
				for (int index = 0; index < highScores.size(); index++) {
					str.append(Integer.toString(index+1)+": "+highScores.get(index).getName()+" " + highScores.get(index).getScore() + " ( " + ConversionUtil.formatTime.format(new Date(highScores.get(index).getTime())) + " )\n");
				}
			}
			TextView label = (TextView)((AlertDialog)dialog).findViewById(android.R.id.message);
			label.setText(str);
//			Debug.stopMethodTracing();
			break;
		}
		default:
			break;
		}
	}

	/** when menu button option selected */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_scores:
			this.showDialog(DIALOG_ID_HIGH_SCORES_LOCAL_SHOW);
			return true;
		case R.id.menu_options:
			startActivity(new Intent(this, WordSearchPreferences.class));
			return true;
		case R.id.menu_new:
			control.newWordSearch();
			return true;
		case R.id.menu_custom:
		{
			Intent intent = new Intent(Intent.ACTION_EDIT, Word.CONTENT_URI);
			intent.setType(Word.CONTENT_TYPE);
			this.startActivity(intent);
			return true;
		}
		case R.id.menu_tutorial:
		{
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setClass(this, TutorialActivity.class);
			startActivity(intent);
			return true;
		}
		case R.id.menu_donate:
		{
			this.showDialog(DIALOG_ID_DONATE);
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
//		Log.v(LOG_TAG, "onPause");
		control.timePause();
	}

	@Override
	protected void onResume() {
		super.onResume();
//		Log.v(LOG_TAG, "onResume");
		if (control.isGameRunning()) {
			control.timeResume();
		} else if (control.getCurrentHighScore() != null) {
			showDialog(WordSearchActivity.DIALOG_ID_GAME_OVER);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
//		Log.v(LOG_TAG, "onSaveInstanceState");
		control.saveState(outState);
		this.removeDialog(DIALOG_ID_GAME_OVER);
	}

	/**
	 * creates a grid of textViews from layout files based on the gridSize
	 *  and sets the new textViews to use the controller as their listener
	 * 
	 * @param gridSize square size of the new grid to make
	 * @param controller the onkeyListener used for the grid's textViews, also holds the gridView an array of the new textView's in the grid
	 */
	public void setupViewGrid() {
		control.setLetter(null);
		int gridSize = control.getGridSize();
		TextViewGridController controller = control.getGridManager();
		ViewGroup gridTable = (ViewGroup) this.findViewById(R.id.gridTable);
		if (gridTable.getChildCount() != gridSize) {
			if (gridTable.getChildCount() == 0) {
				gridTable.setKeepScreenOn(true);
				gridTable.setOnTouchListener(controller);
			}
			controller.clearPointDemension();
			gridTable.removeAllViews();
			Point point = new Point();
			controller.setGridView(new TextView[gridSize][]);
			TextView[][] gridView = controller.getGridView();
			for (point.y = 0; point.y < gridSize; point.y++) {
				this.getLayoutInflater().inflate(R.layout.grid_row, gridTable, true);
				ViewGroup row = (ViewGroup)gridTable.getChildAt(point.y);
				TextView[] rowText = new TextView[gridSize];
				for (point.x = 0; point.x < gridSize; point.x++) {
					this.getLayoutInflater().inflate(R.layout.grid_text_view, row, true);
					TextView view = (TextView)row.getChildAt(point.x);
					view.setId(ConversionUtil.convertPointToID(point, control.getGridSize()));
					view.setOnKeyListener(controller);

					rowText[point.x] = view;
				}
				gridView[point.y] = rowText;
			}
			gridTable.requestLayout();
		}
	}

	public void trackGame() {
		try {
			String category = control.getPrefs().getCategory();
			String input = "Tap";
			if (control.getPrefs().getTouchMode()) {
				input = "Drag";
			}
			AnalyticsTask analytics = new AnalyticsTask(this, false);
			analytics.execute(new String[] {category, input, Integer.toString(control.getGridSize())});
		} catch (RuntimeException re) {
			Log.e(LOG_TAG, "tracker failed!");
		} catch (Exception e) {
			Log.e(LOG_TAG, "tracker failed!");
		}
	}

	public void trackReplay() {
		try {
			String category = control.getPrefs().getCategory();
			AnalyticsTask analytics = new AnalyticsTask(this, false);
			analytics.execute(new String[] {category, "replay", Integer.toString(control.getGridSize())});
		} catch (RuntimeException re) {
			Log.e(LOG_TAG, "tracker failed!");
		} catch (Exception e) {
			Log.e(LOG_TAG, "tracker failed!");
		}
	}
}
