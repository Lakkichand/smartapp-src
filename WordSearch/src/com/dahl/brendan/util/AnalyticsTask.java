package com.dahl.brendan.util;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class AnalyticsTask extends AsyncTask<String, Integer, Boolean> {
	public final static String GA_CODE = "UA-146333-5";
	final private Context context;
	final private boolean pageView;
	public AnalyticsTask(Context context, boolean pageView) {
		this.context = context;
		this.pageView = pageView;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		String appVer = "unknown";
		String category = "unknown";
		String action = "unknown";
		String page = "unknown";
		int value = 0;
		try {
			appVer  = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			appVer = "unknown";
		}
		GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();
	    tracker.start(GA_CODE, context);
	    if (pageView) {
			if (params != null) {
				if (params.length > 0) {
					page = params[0];
				}
			}
		    tracker.trackPageView("/app/"+appVer+"/"+page);
	    } else {
			if (params != null) {
				if (params.length > 0) {
					category = params[0];
				}
				if (params.length > 1) {
					action = params[1];
				}
				if (params.length > 2) {
					try {
						value = Integer.parseInt(params[2]);
					} catch (Exception e) {
						value = 0;
					}
				}
			}
		    tracker.trackEvent(
		    		category,  	// Category
		    		action,  	// Action
		            appVer, 	// Label
		            value);     // Value
	    }
	    Boolean result = tracker.dispatch();
	    tracker.stop();
		return result;
	}

}
