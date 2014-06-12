package com.jiubang.ggheart.analytic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.apps.analytics.AnalyticsReceiver;
import com.jiubang.ggheart.launcher.ICustomAction;

public class ReferrerInfoReceiver extends BroadcastReceiver {

	private static final String tag = "ReferrerInfoReceiver";
//	private static final String ACTION_INSTALL_REFERRER = "com.android.vending.INSTALL_REFERRER";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent != null && ICustomAction.ACTION_INSTALL_REFERRER.equals(intent.getAction())) {
			String referrer = intent.getStringExtra("referrer");
			Log.i(tag, "Referrer Info: " + referrer);
			if (referrer != null) {
				InstallationUtils.needStoreRefInfo(context);
				AnalyticsReceiver receiver = new AnalyticsReceiver();
				Log.i(tag, "Pass intent to AnalyticsReceiver");
				try {
					receiver.onReceive(context, intent);
				} catch (Exception e) {
					Log.e(tag, "AnalyticsReceiver Error", e);
				}
			}
		} else {
			Log.i(tag, "Invalid intent: " + (intent == null ? intent : intent.getAction()));
		}
	}

}
