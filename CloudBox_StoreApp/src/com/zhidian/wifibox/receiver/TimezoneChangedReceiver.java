package com.zhidian.wifibox.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zhidian.wifibox.service.ADTService;
import com.zhidian.wifibox.util.AppUtils;

public class TimezoneChangedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!AppUtils.isServiceRunning(context, ADTService.class.getName())) {
			Intent i = new Intent(context, ADTService.class);
			context.startService(i);
		}
	}

}
