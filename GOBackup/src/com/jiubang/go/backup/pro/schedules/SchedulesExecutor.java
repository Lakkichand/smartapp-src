package com.jiubang.go.backup.pro.schedules;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.MainActivity;
import com.jiubang.go.backup.pro.RecordsListActivity;
import com.jiubang.go.backup.pro.ScheduleBackupAlertActivity;
import com.jiubang.go.backup.pro.data.AppBackupEntry.AppBackupType;
import com.jiubang.go.backup.pro.data.BackupableRecord;
import com.jiubang.go.backup.pro.data.BackupableRecord.RecordBackupArgs;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.model.BackupService;
import com.jiubang.go.backup.pro.model.ForegroundWorkerService;
import com.jiubang.go.backup.pro.model.ForegroundWorkerService.LocalBinder;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.model.RootShell;
import com.jiubang.go.backup.pro.model.UploadBackupService;
import com.jiubang.go.backup.pro.product.manage.ProductManager;
import com.jiubang.go.backup.pro.product.manage.ProductPayInfo;
import com.jiubang.go.backup.pro.schedules.BackupPlan.RepeatType;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 备份执行
 *
 * @author maiyongshen
 */
public class SchedulesExecutor {
	private List<BackupPlan> mWaitingQueue = new ArrayList<BackupPlan>();
	private boolean mIdle = true;
	private ForegroundWorkerService mWorkerService;
	private ServiceConnection mServiceConnection;

	private static SchedulesExecutor sInstance = null;
	
	private Runnable mPendingIntent = null;

	private SchedulesExecutor() {

	}

	public synchronized static SchedulesExecutor getInstance() {
		if (sInstance == null) {
			sInstance = new SchedulesExecutor();
		}
		return sInstance;
	}

	public void handleScheduleTask(final Context context, final BackupPlan plan) {
		if (context == null || plan == null) {
			return;
		}
		if (mIdle) {
			WakeLockManager.acquireCpuWakeLock(context);
			executeSchedule(context, plan);
		} else {
			// Log.d("GoBackup", "enqueue");
			// Log.d("GoBackup", "waiting queue size = " +
			// mWaitingQueue.size());
			mWaitingQueue.add(plan);
		}
	}

	private void executeSchedule(final Context context, final BackupPlan plan) {
		AsyncHandler.post(new Runnable() {
			@Override
			public void run() {
				handleBackupInternal(context, plan);
			}
		});
	}

	private void handleBackupInternal(final Context context, final BackupPlan plan) {
		if (context == null || plan == null) {
			return;
		}

		if (!plan.type.isCloudBackup()) {
			handleLocalBackup(context, plan);
		} else {
			handleCloudBackup(context, plan);
		}
	}
	
	private void handleLocalBackup(final Context context, final BackupPlan plan) {
		final Context appContext = context.getApplicationContext();
		String backupRootPath = Util.getDefalutValidBackupRootPath(context);
		if (TextUtils.isEmpty(backupRootPath)) {
			// SD卡不可用
			showExceptionNotification(appContext, plan);
			onFinish(appContext, plan);
			return;
		}

		mIdle = false;
		boolean isRootUser = RootShell.isRootValid();

		final RecordBackupArgs args = new RecordBackupArgs();
		args.mIsRoot = isRootUser;
		args.mAppBackupType = args.mIsRoot ? AppBackupType.APK_DATA : AppBackupType.APK;

		if (mWorkerService == null) {
			mServiceConnection = new ServiceConnection() {
				@Override
				public void onServiceDisconnected(ComponentName name) {
				}

				@Override
				public void onServiceConnected(ComponentName name, IBinder service) {
					mWorkerService = ((LocalBinder) service).getService();
					doLocalBackupTask(appContext, plan, args);
				}
			};
			Intent intent = new Intent(appContext, BackupService.class);
			appContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
			return;
		}
		doLocalBackupTask(appContext, plan, args);
	}
	
	private void handleCloudBackup(final Context context, final BackupPlan plan) {
		if (!Util.isNetworkValid(context)) {
			// TODO 弹出取消原因提示
			return;
		}
		
		if (!Util.isWifiEnable(context)) {
			// TODO 弹倒计时对话框
			Intent intent = new Intent(context, ScheduleBackupAlertActivity.class);
			intent.putExtra(ScheduleBackupAlertActivity.EXTRA_MESSAGE_TYPE,
					ScheduleBackupAlertActivity.MESSAGE_TYPE_CLOUD_BACKUP_COUNTDOWN);
			if (!(context instanceof Activity)) {
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			}
			context.startActivity(intent);
			return;
		}
	}
	
	private Runnable buildCloudBackupPendingIntent() {
		return new Runnable() {
			@Override
			public void run() {
//				doCloudBackupTask();
			}
		};
	}
	
	private void doCloudBackupTask(final Context context) {
		mServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mWorkerService = ((LocalBinder) service).getService();
				mWorkerService.startWork(context, null,
						null, new IAsyncTaskListener() {
							@Override
							public void onStart(Object arg1, Object arg2) {
								
							}
							
							@Override
							public void onProceeding(Object progress, Object arg2, Object arg3, Object arg4) {
								
							}
							
							@Override
							public void onEnd(boolean success, Object arg1, Object arg2) {
								
							}
						});
			}
		};
		Intent intent = new Intent(context, UploadBackupService.class);
		context.getApplicationContext().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	private void doLocalBackupTask(final Context context, final BackupPlan plan,
			RecordBackupArgs backupArgs) {
		final BackupableRecord record = BackupManager.getInstance().createScheduleBackupableRecord(
				context, plan.type);
		if (record == null) {
			onFinish(context, plan);
			return;
		}

		record.selectAllEntries(true);

		if (!isMeetConditionToBackup(plan, record, context)) {
			Intent it = new Intent(context, ScheduleBackupAlertActivity.class);
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			it.putExtra(ScheduleBackupAlertActivity.EXTRA_MESSAGE_TYPE,
					ScheduleBackupAlertActivity.MESSAGE_TYPE_BACKUP_SIZE_LIMIT);
			it.putExtra(ScheduleBackupAlertActivity.KEY_PLAN, plan);
//			it.putExtra(ScheduleBackupAlertActivity.KEY_SIZE_LIMIT,
//					record.getSelectedEntriesSpaceUsed() > maxBackupSize);
			context.startActivity(it);
			onFinish(context, plan);
			return;
		}

		backupArgs.mBackupPath = record.getBackupPath();
		if (mWorkerService == null) {
			return;
		}

		// 删除原来目录的内容
		Util.deleteFile(record.getBackupPath());

		mWorkerService.startWork(context, record, backupArgs, new IAsyncTaskListener() {
			@Override
			public void onStart(Object arg1, Object arg2) {
			}

			@Override
			public void onProceeding(Object progress, Object arg2, Object arg3, Object arg4) {
			}

			@Override
			public void onEnd(boolean success, Object arg1, Object arg2) {
				Context context = mWorkerService.getApplication().getApplicationContext();
//				if (success) {
					showResultNotification(context, plan);
//				}
				RestorableRecord newRecord = new RestorableRecord(context, record.getBackupPath());
				BackupManager.getInstance().addRestoreRecord(newRecord);
				onFinish(context, plan);
			}
		});
	}

	private boolean isMeetConditionToBackup(final BackupPlan plan, BackupableRecord record,
			Context context) {
		if (plan == null || record == null || context == null) {
			return false;
		}

		boolean needAdvanceFunction = false;

		final long totalSize = record.getSelectedEntriesSpaceUsed();
		final long maxBackupSize = BackupManager.getInstance().getMaxBackupSizeLimit(context);

		if (totalSize > maxBackupSize) {
			needAdvanceFunction = true;
		}

		// 根据新需求，定时备份时不对内容做限制，直接屏蔽
		// if (plan.type.isBackupSystemData() || plan.type.isBackupSystemApp())
		// {
		// needAdvanceFunction = true;
		// }

		if (!needAdvanceFunction) {
			return true;
		}

		// 需要使用高级功能，判断是否是付费用户
		if (ProductManager.getProductPayInfo(context, ProductPayInfo.PRODUCT_ID).isAlreadyPaid()) {
			return true;
		}
		return false;
	}

	private void unbindBackupService(Context context) {
		if (context == null) {
			return;
		}
		try {
			context.unbindService(mServiceConnection);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mWorkerService = null;
	}

	private void onFinish(Context context, BackupPlan finishedPlan) {
		final Scheduler planScheduler = Scheduler.getInstance(context);
		if (finishedPlan != null && finishedPlan.repeatType == RepeatType.ONE_OFF) {
			planScheduler.enablePlan(finishedPlan, false);
		} else if (finishedPlan != null) {
			// 安排下一次启动计划
			planScheduler.schedulePlan(finishedPlan);
		}
		if (hasPlanQueuing()) {
			executeNextPlan(context);
			return;
		}
		mIdle = true;
		unbindBackupService(context);
		WakeLockManager.releaseCpuLock();
	}

	private boolean hasPlanQueuing() {
		if (mWaitingQueue == null || mWaitingQueue.size() <= 0) {
			return false;
		}
		return true;
	}

	private void executeNextPlan(Context context) {
		// Log.d("GoBackup", "execute queuing plan");
		executeSchedule(context, mWaitingQueue.remove(0));
	}

	private void showExceptionNotification(Context context, BackupPlan plan) {
		if (context == null || plan == null) {
			return;
		}
		final String[] backupContents = context.getResources().getStringArray(
				getContentArrayId(isPaidUser(context)));
		final String title = BackupPlan.getBackupContentText(context, plan.type);
		final String message = context.getString(R.string.sd_card_unavailable);
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent intent = new Intent(context, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		nm.notify(
				(int) System.currentTimeMillis(),
				getAdaptiveResultNotification(context, title, message,
						PendingIntent.getActivity(context, 0, intent, 0)));
	}

	private void showResultNotification(Context context, BackupPlan plan) {
		if (context == null || plan == null) {
			return;
		}
		// Log.d("GoBackup", "showResultNotification");
		final String[] backupContents = context.getResources().getStringArray(
				getContentArrayId(isPaidUser(context)));
		final String title = BackupPlan.getBackupContentText(context, plan.type);
		final String message = context.getString(R.string.msg_backup_finished);
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(
				(int) System.currentTimeMillis(),
				getAdaptiveResultNotification(context, title, message, PendingIntent.getActivity(
						context, 0, new Intent(context, RecordsListActivity.class), 0)));
	}

	private boolean isPaidUser(Context context) {
		return ProductManager.getProductPayInfo(context, ProductPayInfo.PRODUCT_ID).isAlreadyPaid();
	}

	private int getContentArrayId(boolean isPaid) {
		return isPaid ? R.array.schduled_backup_content_paid : R.array.schduled_backup_content_free;
	}

	private Notification getAdaptiveResultNotification(Context context, String title,
			String message, PendingIntent contentIntent) {
		Notification notification = null;
		if (Util.getAndroidSystemVersion() >= Build.VERSION_CODES.HONEYCOMB) {
			notification = new Notification.Builder(context)
					.setSmallIcon(R.drawable.notification_icon).setContentTitle(title)
					.setContentText(message).setContentIntent(contentIntent).setAutoCancel(true)
					.getNotification();
		} else {
			notification = new Notification();
			notification.icon = R.drawable.icon;
			RemoteViews contentView = new RemoteViews(context.getPackageName(),
					R.layout.layout_backup_restore_result_notification);
			contentView.setTextViewText(R.id.title, title);
			contentView.setTextViewText(R.id.result, message);
			notification.contentView = contentView;
			notification.contentIntent = contentIntent;
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			notification.defaults &= ~Notification.DEFAULT_VIBRATE;
			notification.vibrate = null;
		}
		return notification;
	}
}
