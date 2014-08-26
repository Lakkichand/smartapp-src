package com.zhidian.wifibox.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ta.TAApplication;
import com.zhidian.wifibox.service.ADTService;

public class BootReceiver extends BroadcastReceiver {

	private static final String ACTION = "android.intent.action.BOOT_COMPLETED";

	@Override
	public void onReceive(Context context, Intent intent) {
		// if (intent.getAction().equals(ACTION)) {
		String action = intent.getAction();
		if (action.equals(ACTION)) {
			// Toast.makeText(context, "重新启动", Toast.LENGTH_SHORT).show();
			Intent i = new Intent(context, ADTService.class);
			context.startService(i);

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

}
