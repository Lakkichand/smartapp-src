package com.jiubang.go.backup.pro.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author maiyongshen
 */
public class PackageEventReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		final String data = intent.getData().getEncodedSchemeSpecificPart();
		if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
			BackupManager.getInstance().onPackageAdded(context.getApplicationContext(), data);
		} else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
			BackupManager.getInstance().onPackageRemoved(context.getApplicationContext(), data);
		}
	}

}
