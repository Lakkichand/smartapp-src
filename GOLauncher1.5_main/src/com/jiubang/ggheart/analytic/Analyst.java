package com.jiubang.ggheart.analytic;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

import com.gau.go.launcherex.R;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
 * Analyst
 * 
 * @author kuangsunny
 * @version 1.0
 */
public class Analyst {
	private final static boolean DEBUG = false;
	private final static String DUMMY_PAGE_INSTALL = "/GOLauncherEXIntalled";
	private final static int INTERVAL = 30 * 10;
	private GoogleAnalyticsTracker mTracker;
	private String mUaNumber;
	private Context mContext;

	public Analyst(Context context) {
		this.mContext = context;
		initUANumber();
	}

	private void initUANumber() {
		InputStream is = mContext.getResources().openRawResource(R.raw.ua_number);
		try {
			byte[] buffer = new byte[64];
			int len = is.read(buffer);
			if (len > 0) {
				mUaNumber = new String(buffer, 0, len).trim();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void startAnalysation() {
		if (mTracker == null) {
			mTracker = GoogleAnalyticsTracker.getInstance();
			mTracker.setDebug(DEBUG);
			mTracker.startNewSession(mUaNumber, INTERVAL, mContext);
		}
	}

	public void uploadReferrerInfo() {
		if (mTracker != null) {
			if (!InstallationUtils.isRefInfoStored(mContext)) {
				mTracker.trackPageView(DUMMY_PAGE_INSTALL);
				InstallationUtils.refInfoStored(mContext);
			}
		}
	}
	
	public void stopAnalysation() {
		if (mTracker != null) {
			mTracker.stopSession();
			mTracker = null;
		}
	}
}
