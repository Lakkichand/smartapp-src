package com.dahl.brendan.wordsearch;

import java.io.File;

import android.os.Environment;

public class Constants {
	public static final int DONATE_GAME_PLAY_COUNT = 10;
	public static final String GA_CODE = "UA-146333-5";

	public static final String API_URL_BASE = "http://wordsearchapp.brendandahl.com/app/";
	public static final String API_URL_CRASH = API_URL_BASE + "crash";
	private static final String API_URL_SCORE = API_URL_BASE + "score";
	public static final String API_URL_SCORE_RANK = API_URL_SCORE + "/rank";
	public static final String API_URL_SCORE_SUBMIT = API_URL_SCORE + "/submit";
	public static final String API_URL_SCORE_TOP10 = API_URL_SCORE + "/top10";
	
	public static final String KEY_GLOBAL_RANK = "KEY_GLOBAL_RANK";
	public static final String KEY_GLOBAL_HIGH_SCORE = "KEY_GLOBAL_HIGH_SCORE";
	public static final String KEY_HIGH_SCORE = "KEY_HIGH_SCORE";
	public static final String KEY_RANK = "KEY_RANK";
	public static final String KEY_WORD_COUNT = "KEY_WORDLIST_COUNT";
	public static final String KEY_HIGH_SCORE_TIME = "KEY_TIME";
	public static final String KEY_HIGH_SCORE_SIZE = "KEY_SIZE";
	public static final String KEY_HIGH_SCORE_THEME = "KEY_THEME";
	public static final String KEY_HIGH_SCORE_NAME = "KEY_NAME";
	public static final String KEY_PAYLOAD = "payload";
	public static final String KEY_DEVICE_ID = "KEY_DEVICE_ID";
	public static final String KEY_INTRO_VER = "intro_app_ver";
	public static final String KEY_DONATE_IGNORE = "di";
	public static final String KEY_GAME_PLAY_COUNT = "gpc";

	// intent params
	public final static String APPLICATION_VERSION = "APPLICATION_VERSION";
	public final static String APPLICATION_STACKTRACE = "APPLICATION_STACKTRACE";
	public final static String PHONE_MODEL = "PHONE_MODEL";
	public final static String ANDROID_VERSION = "ANDROID_VERSION";
	public final static String SECURITY_TOKEN = "SECURITY_TOKEN";
	public final static String ADDITIONAL_DATA = "ADDITIONAL_DATA";

	public static final String VALUE_SECRET = "wordsearchfreepw";
	
	public static final int GRID_SIZE_DEFAULT = 10;
	public static final int MAX_NAME_LENGTH = 30;
	public static final int MAX_TOP_SCORES = 10;
	
	private static final String DEFAULT_FILE_NAME = "wordsearch.json";
	public static final String DEFAULT_FILE_LOCATION = new File(Environment.getExternalStorageDirectory(),DEFAULT_FILE_NAME).getAbsolutePath();
	/**
	 * Activity Action: Pick a file through the file manager, or let user
	 * specify a custom file name.
	 * Data is the current file name or file name suggestion.
	 * Returns a new file name as file URI in data.
	 * 
	 * <p>Constant Value: "org.openintents.action.PICK_FILE"</p>
	 */
	public static final String ACTION_PICK_FILE = "org.openintents.action.PICK_FILE";

	/**
	 * Activity Action: Pick a directory through the file manager, or let user
	 * specify a custom file name.
	 * Data is the current directory name or directory name suggestion.
	 * Returns a new directory name as file URI in data.
	 * 
	 * <p>Constant Value: "org.openintents.action.PICK_DIRECTORY"</p>
	 */
	public static final String ACTION_PICK_DIRECTORY = "org.openintents.action.PICK_DIRECTORY";
	
	/**
	 * The title to display.
	 * 
	 * <p>This is shown in the title bar of the file manager.</p>
	 * 
	 * <p>Constant Value: "org.openintents.extra.TITLE"</p>
	 */
	public static final String EXTRA_TITLE = "org.openintents.extra.TITLE";

	/**
	 * The text on the button to display.
	 * 
	 * <p>Depending on the use, it makes sense to set this to "Open" or "Save".</p>
	 * 
	 * <p>Constant Value: "org.openintents.extra.BUTTON_TEXT"</p>
	 */
	public static final String EXTRA_BUTTON_TEXT = "org.openintents.extra.BUTTON_TEXT";
}
