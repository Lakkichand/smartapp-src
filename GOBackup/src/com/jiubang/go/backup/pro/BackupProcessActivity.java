package com.jiubang.go.backup.pro;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.ParcelableAction.DeleteRecordAction;
import com.jiubang.go.backup.pro.ParcelableAction.ReleaseBackupableRecordAction;
import com.jiubang.go.backup.pro.data.AppBackupEntry.AppBackupType;
import com.jiubang.go.backup.pro.data.BackupableRecord;
import com.jiubang.go.backup.pro.data.BackupableRecord.RecordBackupArgs;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.data.ResultBean;
import com.jiubang.go.backup.pro.model.AsyncWorkEngine;
import com.jiubang.go.backup.pro.model.AsyncWorkEngine.WorkDetailBean;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.model.BackupService;
import com.jiubang.go.backup.pro.model.ForegroundWorkerService;
import com.jiubang.go.backup.pro.model.ForegroundWorkerService.LocalBinder;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.track.ga.TrackerEvent;
import com.jiubang.go.backup.pro.track.ga.TrackerLog;
import com.jiubang.go.backup.pro.ui.WorkProcessDetailAdapter;
import com.jiubang.go.backup.pro.ui.WorkProcessDetailAdapter.OnViewUpdateListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 备份进度页面
 *
 * @author maiyongshen
 */

public class BackupProcessActivity extends BaseActivity implements IAsyncTaskListener {
	public static final String EXTRA_ROOT = "is_root";
	public static final String EXTRA_SHOULD_BACKUP_APP_DATA = "should_backup_app_data";
	public static final String EXTRA_APP_BACKUP_TYPE = "app_backup_type";

	private static final int BACKUP_RESULT_NOTIFICATION_ID = 0x00ff20ff;

	private static final String KEY_START_BACKUP = "key_start";
	private static final String KEY_COMPLETE_BACKUP = "key_complete";

	private static final int MAX_STOP_WAIT_TIME = 45 * 1000;
	private static final int ONE_SECOND_IN_MILLIS = 1000;

	private Dialog mStopDialog;
	private Dialog mStopProgressDialog;
	private ProgressBar mProgressBar;
	private TextView mProgress;
	private TextView mDescription;
	private Button mButton;
	private ListView mDetailListView;
	private int mDetailListViewScrollState;
	private WorkProcessDetailAdapter mWorksDetailAdapter;

	private NotificationManager mNotificationManager;

	private boolean mActivityVisible;
	private Object mLock = new byte[0];

	private ForegroundWorkerService mBackupService;
	private ServiceConnection mBackupServiceConnection;
	private boolean mServiceBound;
	private BackupableRecord mBackupRecord;
	private boolean mStarted;
	private boolean mCompleted;
	private boolean mSuccessful = false;
	private ResultBean[] mResults;
	private long mBackupedRecordActualSize;
	
	private Tracker mTracker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Log.d("GoBackup", "BackupProcessActivity onCreate");
		super.onCreate(savedInstanceState);
		initViews();
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		init(savedInstanceState);
		startBackup();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// Log.d("GoBackup", "BackupProcessActivity onSaveInstanceState");
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_START_BACKUP, mStarted);
		outState.putBoolean(KEY_COMPLETE_BACKUP, mCompleted);
	}

	private void init(Bundle savedInstanceState) {
		mBackupRecord = BackupManager.getInstance().getBackupRecord();
		if (savedInstanceState != null) {
			mStarted = savedInstanceState.getBoolean(KEY_START_BACKUP, false);
			mCompleted = savedInstanceState.getBoolean(KEY_COMPLETE_BACKUP, false);
		}

		if (mStarted && !mCompleted && mBackupRecord == null) {
			Log.d("GoBackup", "mBackupRecord is null!");
			showExceptionOcurredDialog();
		}
	}

	private void initViews() {
		setContentView(R.layout.layout_work_process);
		getWindow().setFormat(PixelFormat.RGBA_8888);

		TextView title = (TextView) findViewById(R.id.title);
		title.setText(getString(R.string.title_backing_up));

		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
		mProgress = (TextView) findViewById(R.id.progress);
		mDescription = (TextView) findViewById(R.id.operation_desc);

		findViewById(R.id.optional_buttons).setVisibility(View.GONE);

		mButton = (Button) findViewById(R.id.single_button);
		mButton.setText(getString(R.string.btn_stop_backup));
		mButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				stopAndExit();
			}
		});

		mDetailListView = (ListView) findViewById(R.id.work_detail);
		mDetailListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				mDetailListViewScrollState = scrollState;
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
					int totalItemCount) {

			}
		});
		mWorksDetailAdapter = new WorkProcessDetailAdapter(this);
		mWorksDetailAdapter.setOnViewUpdateListener(new OnViewUpdateListener() {
			@Override
			public void onChildViewUpdated(int position) {
				if (mDetailListViewScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					mDetailListView.setSelection(position);
				}
			}
		});
		mDetailListView.setAdapter(mWorksDetailAdapter);
	}

	private void updateWorkDetail(WorkDetailBean bean) {
		if (mWorksDetailAdapter != null) {
			mWorksDetailAdapter.update(bean);
		}
	}

	private Notification getAdaptiveResultNotification() {
		return Util.getAndroidSystemVersion() >= Build.VERSION_CODES.HONEYCOMB
				? getThemeHoloNotification()
				: getThemeNomalNotification();
	}

	private Notification getThemeNomalNotification() {
		Notification notification = new Notification();
		notification.icon = R.drawable.icon;
		RemoteViews contentView = new RemoteViews(getPackageName(),
				R.layout.layout_backup_restore_result_notification);
		contentView.setTextViewText(R.id.title, getString(R.string.msg_backup_finished));
		contentView.setViewVisibility(R.id.result, View.GONE);
		notification.contentView = contentView;
		notification.contentIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				BackupProcessActivity.class), 0);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.defaults &= ~Notification.DEFAULT_VIBRATE;
		notification.vibrate = null;
		return notification;
	}

	private Notification getThemeHoloNotification() {
		return new Notification.Builder(this)
				.setSmallIcon(R.drawable.notification_icon)
				.setContentTitle(getString(R.string.msg_backup_finished))
				.setContentIntent(
						PendingIntent.getActivity(this, 0, new Intent(this,
								BackupProcessActivity.class), 0)).setAutoCancel(true)
				.getNotification();
	}

	private void showResultNotification() {
		if (mNotificationManager != null) {
			mNotificationManager.notify(BACKUP_RESULT_NOTIFICATION_ID,
					getAdaptiveResultNotification());
		}
	}

	private void clearNotification() {
		if (mNotificationManager != null) {
			mNotificationManager.cancel(BACKUP_RESULT_NOTIFICATION_ID);
		}
	}

	@Override
	protected void onStart() {
		// Log.d("GoBackup", "BackupProcessActivity onStart");
		super.onStart();
		mActivityVisible = true;
		
		EasyTracker.getInstance().activityStart(this);
		mTracker = EasyTracker.getTracker();
	}

	@Override
	protected void onResume() {
		// Log.d("GoBackup", "BackupProcessActivity onResume");
		super.onResume();
		if (mStarted && mCompleted) {
			startReportActivity();
		}
	}

	@Override
	protected void onPause() {
		// Log.d("GoBackup", "BackupProcessActivity onPause");
		super.onPause();
	}

	@Override
	protected void onStop() {
		// Log.d("GoBackup", "BackupProcessActivity onStop");
		super.onStop();
		mActivityVisible = false;
		
		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	protected void onRestart() {
//		Log.d("GoBackup", "BackupProcessActivity onRestart");
		super.onRestart();
	}

	@Override
	protected void onDestroy() {
//		Log.d("GoBackup", "BackupProcessActivity onDestroy");
		super.onDestroy();
		clearNotification();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// Log.d("GoBackup", "BackupProcessActivity onRestoreInstanceState");
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking() && !event.isCanceled()) {
			return false;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		// 屏蔽掉用户的返回操作
	}

	private void stopAndExit() {
		if (mStarted && !mCompleted) {
			if (mBackupService != null && !mBackupService.isWorkPaused()) {
				mBackupService.pauseWork();
			}
			showStopAlertDialog();
		} else if (!mStarted) {
			exit();
		}
	}

	private void exit() {
		finish();
	}

	private void startBackup() {
		if (mStarted) {
			return;
		}
		if (mBackupRecord == null) {
			Message.obtain(mHandler, MSG_SHOW_TOAST, getString(R.string.msg_record_corrupted))
					.sendToTarget();
			exit();
			return;
		}
		mStarted = true;
		final Intent intent = getIntent();
		final RecordBackupArgs backupArgs = new RecordBackupArgs();
		backupArgs.mBackupPath = mBackupRecord.getBackupPath();
		backupArgs.mIsRoot = intent != null
				? intent.getBooleanExtra(EXTRA_ROOT, false)
				: false;
//		boolean needBackupData = intent != null ? intent.getBooleanExtra(
//				EXTRA_SHOULD_BACKUP_APP_DATA, true) : false;
//		backupArgs.mAppBackupType = needBackupData ? AppBackupType.APK_DATA : AppBackupType.APK;
		backupArgs.mAppBackupType = intent != null ? (AppBackupType) intent
				.getSerializableExtra(EXTRA_APP_BACKUP_TYPE) : backupArgs.mIsRoot
				? AppBackupType.APK_DATA
				: AppBackupType.APK;
		mBackupServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mServiceBound = true;
				mBackupService = ((LocalBinder) service).getService();
				mBackupService.startWork(BackupProcessActivity.this, mBackupRecord, backupArgs,
						BackupProcessActivity.this);
			}
		};
		Intent service = new Intent(this, BackupService.class);
		bindService(service, mBackupServiceConnection, Context.BIND_AUTO_CREATE);
	}

	private void unbindBackupService() {
		if (!mServiceBound) {
			return;
		}
		try {
			unbindService(mBackupServiceConnection);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mServiceBound = false;
	}

	private Runnable mForceStopRunnable = new Runnable() {
		@Override
		public void run() {
			synchronized (mLock) {
				mSuccessful = false;
				dimissStopProgressDialog();
				unbindBackupService();
				exit();
			}
		}
	};

	private void stopBackup() {
		if (mBackupService != null) {
			mBackupService.stopWork();
		}
		// 最多等待45秒，如果45秒内不通过onEnd返回，此时可能发生异常，强制结束，并且不进入结果报告页面
		mHandler.postDelayed(mForceStopRunnable, MAX_STOP_WAIT_TIME);
	}

	/*
	 * private boolean isTopActivity() { ActivityManager am = (ActivityManager)
	 * getSystemService(ACTIVITY_SERVICE); List<RunningTaskInfo> tasksInfo =
	 * am.getRunningTasks(1); if (tasksInfo != null && tasksInfo.size() > 0) {
	 * final String packageName = getPackageName(); if
	 * (packageName.equals(tasksInfo.get(0).topActivity.getPackageName())) {
	 * return true; } } return false; }
	 */

	private void startReportActivity() {
		if (mActivityVisible) {
			startActivity(getStartReportActivityIntent());
			exit();
		}
	}

	private Intent getStartReportActivityIntent() {
		Intent intent = new Intent(this, ReportActivity.class);
		intent.putExtra(ReportActivity.EXTRA_TITLE, getString(R.string.title_backup_result_report));
		intent.putExtra(ReportActivity.EXTRA_RESULT, mSuccessful);
		intent.putExtra(ReportActivity.EXTRA_ENABLE_BACK_KEY, mSuccessful);
		intent.putExtra(ReportActivity.EXTRA_DATE, mBackupRecord.getDate().getTime());
		if (mBackupedRecordActualSize > 0) {
			intent.putExtra(
					ReportActivity.EXTRA_TIP,
					getString(R.string.backup_size_explanation,
							Util.formatFileSize(mBackupedRecordActualSize)));
		}
		if (mSuccessful) {
			intent.putExtra(ReportActivity.EXTRA_POSITIVE_BUTTON_TEXT, getString(R.string.finish));
			intent.putExtra(ReportActivity.EXTRA_POSITIVE_ACTION,
					new ReleaseBackupableRecordAction());
		} else {
			intent.putExtra(ReportActivity.EXTRA_MESSAGES, mResults);
			// 全部失败只显示“完成”按钮，并删除记录
			if (areAllEntriesFailed(mResults)) {
				intent.putExtra(ReportActivity.EXTRA_POSITIVE_BUTTON_TEXT,
						getString(R.string.finish));
				intent.putExtra(ReportActivity.EXTRA_POSITIVE_ACTION, new DeleteRecordAction(
						mBackupRecord.getId()));
			} else {
				intent.putExtra(ReportActivity.EXTRA_POSITIVE_BUTTON_TEXT, getString(R.string.save));
				intent.putExtra(ReportActivity.EXTRA_POSITIVE_ACTION,
						new ReleaseBackupableRecordAction());
				intent.putExtra(ReportActivity.EXTRA_NEGATIVE_BUTTON_TEXT,
						getString(R.string.discard));
				intent.putExtra(ReportActivity.EXTRA_NEGATIVE_ACTION, new DeleteRecordAction(
						mBackupRecord.getId()));
			}
		}
		return intent;
	}

	@Override
	public void onStart(Object arg1, Object arg2) {
		// Message.obtain(mHandler, MSG_READY_TO_START,
		// getString(R.string.msg_preparing)).sendToTarget();
		if (arg1 instanceof List<?>) {
			Message.obtain(mHandler, MSG_INIT_WORK_DETAIL_LISTVIEW, arg1).sendToTarget();
		}
	}

	@Override
	public void onProceeding(Object progress, Object arg2, Object arg3, Object arg4) {
		Message.obtain(mHandler, MSG_UPDATE_PROGRESS, ((Integer) progress).intValue(), 0)
				.sendToTarget();
		if (arg2 instanceof WorkDetailBean) {
			Message.obtain(mHandler, MSG_UPDATE_WORK_DETAIL, arg2).sendToTarget();
		}
	}

	@Override
	public void onEnd(boolean success, Object arg1, Object arg2) {
		if (isFinishing()) {
			return;
		}
		synchronized (mLock) {
			mHandler.removeCallbacks(mForceStopRunnable);
		}
		if (success) {
			try {
				Thread.sleep(ONE_SECOND_IN_MILLIS);
			} catch (InterruptedException e) {

			}
		}
		if (arg1 instanceof ResultBean[]) {
			mResults = (ResultBean[]) arg1;
		}
		RestorableRecord newRecord = new RestorableRecord(this, mBackupRecord.getBackupPath());
		BackupManager.getInstance().addRestoreRecord(newRecord);
		mBackupedRecordActualSize = newRecord.getSpaceUsage();
		mHandler.sendEmptyMessage(MSG_SHOW_NOTIFICATION);
		mHandler.sendEmptyMessage(MSG_DISMISS_STOP_PROGRESS_DIALOG);
		mHandler.sendEmptyMessage(MSG_UNBIND_SERVICE);
		Message.obtain(mHandler, MSG_SHOW_RESULT, success ? 1 : 0, 1).sendToTarget();
	}

	private void showToast(String toast) {
		Toast.makeText(this, toast, Toast.LENGTH_LONG);
	}

	private void updateDescription(String desc) {
		if (mDescription != null) {
			mDescription.setText(desc);
		}
	}

	private void updateProgress(int progress, Object data) {
		mProgressBar.setProgress(progress);
		mProgress.setText(getString(R.string.progress_format, String.valueOf(progress)));
		String entryDesc = "";
		if (data != null) {
			entryDesc = data.toString();
		}
		updateDescription(entryDesc);
	}

	private void showExceptionOcurredDialog() {
		Dialog dialog = new AlertDialog.Builder(this).setIcon(android.R.drawable.stat_notify_error)
				.setTitle(R.string.alert_dialog_title)
				.setMessage(R.string.msg_backup_exception_occurred)
				.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						exit();
					}
				}).create();
		showDialog(dialog);
	}

	private Dialog createStopAlertDialog() {
		Dialog dialog = new AlertDialog.Builder(this).setIcon(android.R.drawable.stat_notify_error)
				.setTitle(R.string.title_stop_backup).setMessage(R.string.msg_discard_backuping)
				.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// GA统计
						TrackerLog.i("BackupProcessActivity stopDialog ok");
						mTracker.trackEvent(TrackerEvent.CATEGORY_UI_ACTION,
								TrackerEvent.ACTION_BUTTON_PRESS,
								TrackerEvent.LABEL_BACKUPPROCESS_STOP_DIALOG_OK_BUTTON,
								TrackerEvent.OPT_CLICK);
						
						mHandler.sendEmptyMessage(MSG_SHOW_STOP_PROGRESS_DIALOG);
						stopBackup();
						dialog.dismiss();
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						resumeBackupService();
						dialog.dismiss();
					}
				}).setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						resumeBackupService();
					}
				}).create();
		dialog.setCanceledOnTouchOutside(false);
		return dialog;
	}

	private void showStopAlertDialog() {
		if (mStopDialog == null) {
			mStopDialog = createStopAlertDialog();
		}
		showDialog(mStopDialog);
	}

	private synchronized void resumeBackupService() {
		if (mBackupService != null && mBackupService.isWorkPaused()) {
			mBackupService.resumeWork();
		}
	}

	private void initStopProgressDialog() {
		mStopProgressDialog = new ProgressDialog(this);
		((ProgressDialog) mStopProgressDialog).setProgressStyle(ProgressDialog.STYLE_SPINNER);
		((ProgressDialog) mStopProgressDialog).setCancelable(false);
		((ProgressDialog) mStopProgressDialog).setTitle(R.string.dialog_title_stopping);
		((ProgressDialog) mStopProgressDialog)
				.setMessage(getString(R.string.msg_stopping_operation));
		((ProgressDialog) mStopProgressDialog).setIndeterminate(true);
	}

	private void showStopProgressDialog() {
		if (mStopProgressDialog == null) {
			initStopProgressDialog();
		}
		showDialog(mStopProgressDialog);
	}

	private void dimissStopProgressDialog() {
		dismissDialog(mStopProgressDialog);
	}

	private boolean areAllEntriesFailed(ResultBean[] resultBeans) {
		if (resultBeans == null || resultBeans.length < 1) {
			return true;
		}
		final int count = resultBeans.length;
		for (int i = 0; i < count; i++) {
			if (resultBeans[i].result) {
				return false;
			}
		}
		return true;
	}

	private static final int MSG_READY_TO_START = 0x1002;
	private static final int MSG_UPDATE_PROGRESS = 0x1003;
	private static final int MSG_SHOW_NOTIFICATION = 0x1004;
	private static final int MSG_SHOW_TOAST = 0x1005;
	private static final int MSG_SHOW_RESULT = 0x1006;
	private static final int MSG_SHOW_STOP_PROGRESS_DIALOG = 0x1007;
	private static final int MSG_DISMISS_STOP_PROGRESS_DIALOG = 0x1008;
	private static final int MSG_UNBIND_SERVICE = 0X1009;
	private static final int MSG_UPDATE_WORK_DETAIL = 0X100a;
	private static final int MSG_INIT_WORK_DETAIL_LISTVIEW = 0x100b;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_READY_TO_START :
					if (msg.obj != null) {
						updateDescription(msg.obj.toString());
					}
					break;
				case MSG_SHOW_NOTIFICATION :
					showResultNotification();
					break;
				case MSG_UPDATE_PROGRESS :
					updateProgress(msg.arg1, msg.obj);
					break;
				case MSG_SHOW_TOAST :
					if (msg.obj != null) {
						showToast(msg.obj.toString());
					}
					break;
				case MSG_SHOW_RESULT :
					// GA统计
					TrackerLog.i("BackupProgressActivity showResult");
					mTracker.trackEvent(TrackerEvent.CATEGORY_SCHEDULE,
							TrackerEvent.ACTION_PROGRESS, TrackerEvent.LABEL_BACKUPPROGRESS_FINISH,
							-1l);
					
					mSuccessful = msg.arg1 > 0;
					mCompleted = true;
					startReportActivity();
					break;
				case MSG_SHOW_STOP_PROGRESS_DIALOG :
					showStopProgressDialog();
					break;
				case MSG_DISMISS_STOP_PROGRESS_DIALOG :
					dimissStopProgressDialog();
					break;
				case MSG_UNBIND_SERVICE :
					unbindBackupService();
					break;
				case MSG_UPDATE_WORK_DETAIL :
					updateWorkDetail((WorkDetailBean) msg.obj);
					break;
				case MSG_INIT_WORK_DETAIL_LISTVIEW :
					mWorksDetailAdapter.init((List<AsyncWorkEngine.WorkDetailBean>) msg.obj);
					break;
				default :
					break;
			}
		}
	};

}
