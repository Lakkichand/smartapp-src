/**
 * 
 */
package com.jiubang.go.backup.pro.track.ga;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.analytics.tracking.android.AnalyticsReceiver;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GAServiceManager;

/**
 * @author liguoliang
 *
 */
public class TrackerReceiver extends BroadcastReceiver {
	private static final String GA_INSTALL_ACTION = "com.android.vending.INSTALL_REFERRER";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("ABEN", "TrackerReceiver");
		if (intent == null || intent.getAction() == null) {
			return ;
		}
		try {
			 String campaign = intent.getStringExtra("referrer");
			 if (campaign != null) {
				 Log.i("ABEN", "campaign = " + campaign);
			 } else {
				 Log.i("ABEN", "campaign is null");
			 }
			if (GA_INSTALL_ACTION.equals(intent.getAction())) {
				Log.i("ABEN", "TrackerReceiver action match");
				Uri uri = intent.getData();
				if (uri != null) {
					if (uri.getQueryParameter("utm_source") != null) {    // Use campaign parameters if avaialble.
						Log.i("ABEN", "TrackerReceiver utm_source = " + uri.getPath());
						EasyTracker.getTracker().setCampaign(uri.getPath());
					} else if (uri.getQueryParameter("referrer") != null) {    // Otherwise, try to find a referrer parameter.
						Log.i("ABEN", "TrackerReceiver referrer = " + uri.getQueryParameter("referrer"));
						EasyTracker.getTracker().setReferrer(uri.getQueryParameter("referrer"));					
					}
				}
			}
			Log.i("ABEN", "TrackerReceiver new AnalyticsReceiver");
			AnalyticsReceiver receiver = new AnalyticsReceiver();
			receiver.onReceive(context, intent);
			Log.i("ABEN", "TrackerReceiver dispatch");
			GAServiceManager.getInstance().dispatch();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

}
