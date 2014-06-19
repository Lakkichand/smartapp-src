/*
 *  Copyright 2010 Emmanuel Astier & Kevin Gaudin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.jiubang.go.backup.pro.errorreport;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.errorreport.ErrorReporter.ReportsSenderWorker;

import android.app.Activity;
import android.app.NotificationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

/**
 * This is the dialog Activity used by ACRA to get authorization from the user
 * to send reports. Requires android:theme="@android:style/Theme.Dialog" and
 * android:launchMode="singleInstance" in your AndroidManifest to work properly.
 * 
 * @author Kevin Gaudin
 */
public class CrashReportDialog extends Activity {
	private final static String TAG = "zyp";
	/**
	 * Default left title icon.
	 */

	String mReportFileName = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Log.i("GOPHOTO","CrashReportDialog on create");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.report);
		mReportFileName = getIntent().getStringExtra(ErrorReporter.EXTRA_REPORT_FILE_NAME);
		if (mReportFileName == null) {
			// Log.i("====","CrashReportDialog return");
			finish();
		}
		// Log.i("====","CrashReportDialog before button");
		Button btnYes = (Button) findViewById(R.id.sure_report);
		Button btnNo = (Button) findViewById(R.id.cancel_report);
		btnYes.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onYes();
			}
		});
		btnNo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				onNo();
			}
		});

		cancelNotification();

	}

	/**
	 * Disable the notification in the Status Bar.
	 */
	protected void cancelNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(CrashReport.NOTIF_CRASH_ID);
	}

	private void onYes() {
		try {
			ErrorReporter err = ErrorReporter.getInstance();
			ReportsSenderWorker worker = err.new ReportsSenderWorker();
			worker.setCommentReportFileName(mReportFileName, getApplicationContext());
			worker.start();
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
		finish();
	}

	private void onNo() {
		finish();
		System.exit(0);

	}
}
