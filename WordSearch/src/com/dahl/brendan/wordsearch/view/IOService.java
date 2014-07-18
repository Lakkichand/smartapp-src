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
//	  Copyright 2010 Brendan Dahl <dahl.brendan@brendandahl.com>
//	  	http://www.brendandahl.com

package com.dahl.brendan.wordsearch.view;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.dahl.brendan.wordsearch.view.WordDictionaryProvider.Word;

public class IOService extends Service {
	class Command implements Runnable {
		final private Intent intent;
		final private Context context;
		public Command(Context context, Intent intent) {
			this.intent = intent;
			this.context = context;
		}
		public void run() {
			String filename = intent.getStringExtra(IOService.EXTRA_FILENAME);
			if (TextUtils.isEmpty(filename)) {
				Log.v(LOGTAG, "missing filename");
				return;
			} else {
				Log.v(LOGTAG, String.format("filename: %s", filename));
			}
			File file = new File(filename);
			boolean overwrite = intent.getBooleanExtra(IOService.EXTRA_OVERWRITE, true);
			int action = intent.getIntExtra(IOService.EXTRA_ACTION_TYPE, -1);
			boolean mExternalStorageAvailable = false;
			boolean mExternalStorageWriteable = false;
			String state = Environment.getExternalStorageState();

			if (Environment.MEDIA_MOUNTED.equals(state)) {
			    // We can read and write the media
			    mExternalStorageAvailable = mExternalStorageWriteable = true;
			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			    // We can only read the media
			    mExternalStorageAvailable = true;
			    mExternalStorageWriteable = false;
			} else {
			    // Something else is wrong. It may be one of many other states, but all we need
			    //  to know is we can neither read nor write
			    mExternalStorageAvailable = mExternalStorageWriteable = false;
			}
			switch (action) {
			case ACTION_READ:
				Log.v(LOGTAG, "Action type: read");
				if (mExternalStorageAvailable) {
					IOService.readFile(context, file, overwrite);
				} else {
					Log.e(LOGTAG, "storage not available");
				}
				break;
			case ACTION_WRITE:
				Log.v(LOGTAG, "Action type: write");
				if (mExternalStorageWriteable) {
					IOService.writeFile(context, file, overwrite);
				} else {
					Log.e(LOGTAG, "storage not available");
				}
				break;
			default:
				Log.e(LOGTAG, String.format("unknown action received %s", action));
				break;
			}
			stopSelf();
		}
	}

	final static private String LOGTAG = IOService.class.getSimpleName();
	public static final int ACTION_READ = 0;
	public static final int ACTION_WRITE = 1;
	public static final String EXTRA_ACTION_TYPE = "actionType";
	public static final String EXTRA_FILENAME = "filename";
	
	public static final String EXTRA_OVERWRITE = "overwrite";

	private static void readFile(Context context, File file, boolean overwrite) {
		if (file.canRead()) {
			if (overwrite) {
				context.getContentResolver().delete(Word.CONTENT_URI, "1", null);
			}
			try {
				byte[] buffer = new byte[(int) file.length()];
			    BufferedInputStream f = new BufferedInputStream(new FileInputStream(file));
			    f.read(buffer);
			    JSONArray array = new JSONArray(new String(buffer));
			    List<ContentValues> values = new LinkedList<ContentValues>();
			    for (int i = 0; i < array.length(); i++) {
			    	Log.v(LOGTAG, array.getString(i));
					ContentValues value = new ContentValues();
					value.put(Word.WORD, array.getString(i));
					values.add(value);
			    }
				context.getContentResolver().bulkInsert(Word.CONTENT_URI, values.toArray(new ContentValues[0]));
			} catch (IOException e) {
				Log.e(LOGTAG, "IO error", e);
			} catch (JSONException e) {
				Log.e(LOGTAG, "bad input", e);
			}
		}
	}

	private static void writeFile(Context context, File file, boolean overwrite) {
		if (!file.exists() || overwrite){
			Cursor cursor = context.getContentResolver().query(Word.CONTENT_URI, new String[] { Word.WORD }, null, null, null);
			JSONArray array = new JSONArray();
			if (cursor.moveToFirst()) {
				while(!cursor.isAfterLast()) {
					array.put(cursor.getString(0));
					cursor.moveToNext();
				}
			}
			try {
				file.getParentFile().mkdirs();
				file.createNewFile();
			    BufferedWriter out = new BufferedWriter(new FileWriter(file));
			    out.write(array.toString());
			    out.close();
			} catch (IOException e) {
				Log.e(LOGTAG, "write failed", e);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	// This is the old onStart method that will be called on the pre-2.0
	// platform.  On 2.0 or later we override onStartCommand() so this
	// method will not be called.
	@Override
	public void onStart(Intent intent, int startId) {
		new Thread(new Command(this.getApplicationContext(), intent)).start();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new Thread(new Command(this.getApplicationContext(), intent)).start();
	    // We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
	    return START_STICKY;
	}

}
