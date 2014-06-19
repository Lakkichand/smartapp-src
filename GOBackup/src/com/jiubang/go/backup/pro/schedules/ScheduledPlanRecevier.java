package com.jiubang.go.backup.pro.schedules;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.MainActivity;
import com.jiubang.go.backup.pro.PreferenceManager;
import com.jiubang.go.backup.pro.ScheduledPlanListActivity;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 定时备份receive
 *
 * @author maiyongshen
 */
public class ScheduledPlanRecevier extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, final Intent intent) {
		// Log.d("GoBackup", "ScheduledPlanRecevier onReceive");
		final String action = intent.getAction();
		// Log.d("GoBackup", "action = " + action);

		WakeLockManager.acquireCpuWakeLock(context);
		if (Scheduler.ACTION_SCHEDULED_PLAN_ADVANCE_NOTICE.equals(action)
				|| Scheduler.ACTION_SCHEDULED_BACKUP.equals(action)) {
			final BackupPlan plan = intent.getParcelableExtra(Scheduler.EXTRA_PLAN_DATA);
			if (plan == null) {
				return;
			}
			handleScheduledBackupPlan(context, action, plan);
		} else if (ContactCheckerSchedule.ACTION_CONTACT_CHECK.equals(action)) {
			// 联系人变更检查
			handleScheduledContactCheck(context);
		}
		WakeLockManager.releaseCpuLock();
	}

	private void handleScheduledBackupPlan(Context context, String action, BackupPlan plan) {
		if (Scheduler.ACTION_SCHEDULED_PLAN_ADVANCE_NOTICE.equals(action)) {
			showScheduledBackupPlanAdvanceNotification(context, plan);
		} else if (Scheduler.ACTION_SCHEDULED_BACKUP.equals(action)) {
			SchedulesExecutor.getInstance().handleScheduleTask(context, plan);
		}
	}

	/**
	 * 联系人变更检查
	 *
	 * @param context
	 */
	private void handleScheduledContactCheck(Context context) {
		ContactCheckerSchedule schedule = ContactCheckerSchedule.getInstance(context
				.getApplicationContext());
		if (schedule.isContactChange()) {
			if (PreferenceManager.getInstance().getBoolean(context,
					PreferenceManager.KEY_CONTACT_CHANGE_PUSH, true)) {
				showScheduledContactCheckNotification(context);
			}
			schedule.reflashContactToPreference();
		}
		schedule.scheduleNextCheck();
	}

	private void showScheduledBackupPlanAdvanceNotification(Context context, BackupPlan plan) {
		if (context == null || plan == null) {
			return;
		}
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(plan.id, getAdaptiveAdvanceNotification(context, plan));
	}

	private void showScheduledContactCheckNotification(Context context) {
		Notification notification = null;
		final PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(
				context, MainActivity.class), 0);

		if (Util.getAndroidSystemVersion() >= Build.VERSION_CODES.HONEYCOMB) {
			notification = new Notification.Builder(context)
					.setSmallIcon(R.drawable.notification_icon)
					.setDefaults(Notification.DEFAULT_VIBRATE).setAutoCancel(true)
					.setContentTitle(context.getString(R.string.notification_contacts_changed_title))
					.setContentText(context.getString(R.string.notification_contacts_changed_message))
					.setContentIntent(contentIntent).getNotification();
		} else {
			notification = new Notification();
			notification.icon = R.drawable.icon;
			RemoteViews contentView = new RemoteViews(context.getPackageName(),
					R.layout.layout_backup_restore_result_notification);
			contentView.setTextViewText(R.id.title, context.getString(R.string.notification_contacts_changed_title));
			contentView.setTextViewText(R.id.result,
					context.getString(R.string.notification_contacts_changed_message));
			notification.contentView = contentView;
			notification.contentIntent = contentIntent;
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			notification.defaults &= ~Notification.DEFAULT_VIBRATE;
			notification.vibrate = null;
		}

		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(ContactCheckerSchedule.CONTACT_CHECK_NOTIFICATION_ID, notification);
	}

	private Notification getAdaptiveAdvanceNotification(Context context, BackupPlan plan) {
		Notification notification = null;
		// final String[] backupContents =
		// context.getResources().getStringArray(R.array.schduled_backup_content);
		final String planTime = context.getString(R.string.scheduled_plan_time_format, plan.hour,
				Util.pad(plan.minutes));
		final PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(
				context, ScheduledPlanListActivity.class), 0);

		if (Util.getAndroidSystemVersion() >= Build.VERSION_CODES.HONEYCOMB) {
			notification = new Notification.Builder(context)
					.setSmallIcon(R.drawable.notification_icon)
					.setDefaults(Notification.DEFAULT_VIBRATE)
					.setAutoCancel(true)
					.setContentTitle(
							/* backupContents[plan.type.ordinal()] */BackupPlan
									.getBackupContentText(context, plan.type))
					.setContentText(planTime).setContentIntent(contentIntent).getNotification();
		} else {
			notification = new Notification();
			notification.icon = R.drawable.icon;
			RemoteViews contentView = new RemoteViews(context.getPackageName(),
					R.layout.layout_backup_restore_result_notification);
			contentView.setTextViewText(R.id.title,
					BackupPlan.getBackupContentText(context, plan.type));
			contentView.setTextViewText(R.id.result, planTime);
			notification.contentView = contentView;
			notification.contentIntent = contentIntent;
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			notification.defaults &= ~Notification.DEFAULT_VIBRATE;
			notification.vibrate = null;
		}
		return notification;
	}
}
