package com.zhidian.wifibox.receiver;

import com.zhidian.wifibox.service.ADTService;
import com.zhidian.wifibox.util.AppUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NewOutgoingCallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!AppUtils.isServiceRunning(context, ADTService.class.getName())) {
			Intent i = new Intent(context, ADTService.class);
			context.startService(i);
		}
	}

}
