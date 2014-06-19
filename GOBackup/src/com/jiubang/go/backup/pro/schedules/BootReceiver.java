package com.jiubang.go.backup.pro.schedules;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BootReceiver
 *
 * @author maiyongshen
 */
public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		// Log.d("GoBackup", "BootReceiver onReceive");
		// final String action = intent.getAction();
		WakeLockManager.acquireCpuWakeLock(context);
		AsyncHandler.post(new Runnable() {
			@Override
			public void run() {
				scheduleBackupPlan(context);
				scheduleContactCheckPlan(context);
				WakeLockManager.releaseCpuLock();
			}
		});
	}

	private void scheduleBackupPlan(Context context) {
		final Scheduler planScheduler = Scheduler.getInstance(context);
		planScheduler.disableExpiredPlan();
		// planScheduler.scheduleNextPlan();
		planScheduler.scheduleAllActivePlans();
	}

	private void scheduleContactCheckPlan(Context context) {
		ContactCheckerSchedule schedule = ContactCheckerSchedule.getInstance(context
				.getApplicationContext());
		schedule.scheduleNextCheck();
	}
}
