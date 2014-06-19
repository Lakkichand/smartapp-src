package com.jiubang.go.backup.pro.model;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.NopActivity;
import com.jiubang.go.backup.pro.model.AsyncWorkEngine.WorkDetailBean;
import com.jiubang.go.backup.pro.schedules.WakeLockManager;
import com.jiubang.go.backup.pro.util.Util;

/**
 * @author maiyongshen
 */
public abstract class ForegroundWorkerService extends Service implements IAsyncTaskListener {
	private NotificationManager mNotificationManager = null;
	private IAsyncTaskListener mListener;
	private IBinder mBinder = new LocalBinder();
	private Activity mAssociatedActivity;
	private int mCurrentProgress;
	private String mCurrentMessage;
	private boolean mServiceRunning = false;

	/**
	 * @author maiyongshen
	 */
	public class LocalBinder extends Binder {
		public ForegroundWorkerService getService() {
			return ForegroundWorkerService.this;
		}
	}

	@Override
	public void onCreate() {
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		startForeground(getForegroundNotificationId(), getAdaptiveNotification(0, null));
	}

	@Override
	public IBinder onBind(Intent intent) {
		//		mServiceRunning = true;
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		//		mServiceRunning = false;
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopForeground(true);
	}

	protected void updateProgressNotification(int progress, String message) {
		if (progress < 0) {
			return;
		}
		if (!mServiceRunning) {
			return;
		}

		if (mNotificationManager != null) {
			mNotificationManager.notify(getForegroundNotificationId(),
					getAdaptiveNotification(progress, message));
		}
	}

	private Notification getAdaptiveNotification(int progress, String info) {
		return Util.getAndroidSystemVersion() >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
				? getThemeHoloNotification(progress, info)
				: getThemeNomalNotification(progress, info);
	}

	private Notification getThemeNomalNotification(int progress, String info) {
		Notification notification = new Notification();
		notification.icon = R.drawable.icon;
		RemoteViews contentView = new RemoteViews(getPackageName(),
				R.layout.layout_backup_restore_progress_notification);
		final int progressBarMax = 100;
		contentView.setProgressBar(R.id.progress_bar, progressBarMax, progress, progress > 0
				? false
				: true);
		contentView.setTextViewText(R.id.progress, String.valueOf(progress) + "%，" + info);
		notification.contentView = contentView;
		notification.contentIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				NopActivity.class), 0);
		notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_FOREGROUND_SERVICE;
		notification.defaults &= ~Notification.DEFAULT_VIBRATE;
		notification.vibrate = null;
		return notification;
	}

	private Notification getThemeHoloNotification(int progress, String info) {
		final int progressBarMax = 100;
		return new Notification.Builder(this)
				.setSmallIcon(R.drawable.notification_icon)
				.setContentTitle(String.valueOf(progress) + "%，" + info)
				.setProgress(progressBarMax, progress, progress > 0 ? false : true)
				.setContentIntent(
						PendingIntent.getActivity(this, 0, new Intent(this, NopActivity.class), 0))
				.setOngoing(true).getNotification();
	}

	@Override
	public void onStart(Object arg1, Object arg2) {
		if (mListener != null) {
			mListener.onStart(arg1, arg2);
		}
	}

	@Override
	public void onProceeding(Object progress, Object arg2, Object arg3, Object arg4) {
		final int curProgress = ((Integer) progress).intValue();
		String message = getForegroundNotificationMessage();
		if (mCurrentProgress != curProgress /*|| !TextUtils.equals(mCurrentMessage, message)*/) {
			updateProgressNotification(curProgress, message);
			mCurrentMessage = message;
			mCurrentProgress = curProgress;
		}
		WorkDetailBean workDetailBean = null;
		if (arg3 instanceof WorkDetailBean) {
			workDetailBean = (WorkDetailBean) arg3;
		}
		if (mListener != null) {
			mListener.onProceeding(progress, workDetailBean, message, null);
		}
	}

	@Override
	public void onEnd(boolean success, Object arg1, Object arg2) {
		if (!shouldContinueToWork(arg1, success)) {
			onWorkFinish(success, arg1, arg2);
			return;
		}
		doWork(arg1, arg2);
	}

	public void startWork(Context context, Object arg1, Object arg2, IAsyncTaskListener listener) {
		if (context instanceof Activity) {
			mAssociatedActivity = (Activity) context;
		}
		mListener = listener;
		onWorkStart();
		doWork(arg1, arg2);
		mServiceRunning = true;
	}
	
	public boolean isRunning() {
		return mServiceRunning;
	}

	protected void onWorkStart() {
		WakeLockManager.acquireCpuWakeLock(this);
		updateProgressNotification(0, getString(R.string.msg_preparing));
	}

	protected void onWorkFinish(boolean success, Object arg1, Object arg2) {
		WakeLockManager.releaseCpuLock();
		if (mListener != null) {
			mListener.onEnd(success, arg1, arg2);
		}
		mAssociatedActivity = null;
		mServiceRunning = false;
	}

	protected boolean shouldContinueToWork(Object finishedWork, boolean result) {
		return false;
	}

	public Context getAssociatedContext() {
		return mAssociatedActivity != null ? mAssociatedActivity : this;
	}

	public abstract int getForegroundNotificationId();

	public abstract String getForegroundNotificationMessage();

	public abstract void stopWork();

	protected abstract void doWork(Object arg1, Object arg2);

	public abstract void pauseWork();

	public abstract void resumeWork();

	public abstract boolean isWorkPaused();
}
