package com.zhidian.wifibox.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ta.TAApplication;
import com.zhidian.wifibox.service.ADTService;
import com.zhidian.wifibox.util.AppUtils;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!AppUtils.isServiceRunning(context, ADTService.class.getName())) {
			Intent i = new Intent(context, ADTService.class);
			context.startService(i);
		}

		// 检查更新
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent scanIntent = new Intent(TAApplication.getApplication(),
				ScanReceiver.class);
		scanIntent.setAction("alarm.scan.action");
		PendingIntent scanIntentPi = PendingIntent.getBroadcast(
				TAApplication.getApplication(), 0, scanIntent, 0);
		am.cancel(scanIntentPi);
		am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(),
				4 * 60 * 60 * 1000, scanIntentPi);// 一小时检查一次
	}

}
