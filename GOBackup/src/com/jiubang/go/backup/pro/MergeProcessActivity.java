package com.jiubang.go.backup.pro;

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
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.data.ResultBean;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.model.ForegroundWorkerService;
import com.jiubang.go.backup.pro.model.ForegroundWorkerService.LocalBinder;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.model.MergeService;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 整合备份进度页面
 *
 * @author maiyongshen
 */
public class MergeProcessActivity extends BaseActivity implements IAsyncTaskListener {
	private static final String KEY_START_MERGE = "key_start";
	private static final String KEY_COMPLETE_MERGE = "key_complete";
	private static final int RESULT_NOTIFICATION_ID = 0x00ff30ff;

	private static final int MAX_STOP_WAIT_TIME = 45 * 1000;

	private boolean mStarted;
	private boolean mCompleted;
	private boolean mSuccessful = false;
	private boolean mActivityVisible;

	private NotificationManager mNotificationManager;

	private ProgressBar mProgressBar;
	private TextView mProgress;
	private TextView mDescription;
	private Dialog mStopDialog;
	private Dialog mStopProgressDialog;

	private ForegroundWorkerService mMergeService;
	private ServiceConnection mMergeServiceConnection;
	private boolean mServiceBound;

	private Object mLock = new byte[0];

	private RestorableRecord mMergedRecord;
	private ResultBean[] mResults;

	private Runnable mForceStopRunnable = new Runnable() {
		@Override
		public void run() {
			synchronized (mLock) {
				mSuccessful = false;
				dismissStopProgressDialog();
				unbindMergeService();
				finish();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initViews();
		String selectedRecordIds = getIntent().getStringExtra("mergeRcords");

		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mStarted = false;
		mCompleted = false;
		init(savedInstanceState, selectedRecordIds);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mActivityVisible = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mStarted && mCompleted) {
			startReportActivity();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		mActivityVisible = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		clearNotification();
	}

	private void initViews() {
		setContentView(R.layout.layout_merge_process);
		getWindow().setFormat(PixelFormat.RGBA_8888);

		TextView title = (TextView) findViewById(R.id.title);
		title.setText(R.string.title_merging);

		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
		mProgress = (TextView) findViewById(R.id.progress);
		mDescription = (TextView) findViewById(R.id.operation_desc);

		Button stopButton = (Button) findViewById(R.id.operate_button);
		stopButton.setText(R.string.btn_stop_merge);
		stopButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				stopAndExit();
			}
		});
	}

	private void init(Bundle savedInstanceState, String selectedRecordIds) {
		if (savedInstanceState != null) {
			mStarted = savedInstanceState.getBoolean(KEY_START_MERGE, false);
			mCompleted = savedInstanceState.getBoolean(KEY_COMPLETE_MERGE, false);
		}
		if (mStarted) {
			return;
		}
		startToMerge(selectedRecordIds);
	}

	@Override
	public void onBackPressed() {
		// 屏蔽掉用户的返回操作
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_START_MERGE, mStarted);
		outState.putBoolean(KEY_COMPLETE_MERGE, mCompleted);
	}

	private void startToMerge(final String selectedRecordIds) {
		if (mMergeServiceConnection != null) {
			unbindService(mMergeServiceConnection);
			mMergeServiceConnection = null;
		}
		mStarted = true;
		mMergeServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mServiceBound = true;
				mMergeService = ((LocalBinder) service).getService();
				//在这里传一勾选的定时备份包list和普通备份包list
				mMergeService.startWork(MergeProcessActivity.this, selectedRecordIds, null,
						MergeProcessActivity.this);
			}
		};

		Intent intent = new Intent(this, MergeService.class);
		bindService(intent, mMergeServiceConnection, Context.BIND_AUTO_CREATE);
	}

	private void stopAndExit() {
		if (mStarted && !mCompleted) {
			if (mMergeService != null && !mMergeService.isWorkPaused()) {
				mMergeService.pauseWork();
			}
			showStopAlertDialog();
			return;
		}
		finish();
	}

	private Dialog createStopAlertDialog() {
		Dialog dialog = new AlertDialog.Builder(this).setIcon(android.R.drawable.stat_notify_error)
				.setTitle(R.string.title_stop_merge).setMessage(R.string.msg_discard_merging)
				.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						showStopProgressDialog();
						stopMerging();
						dialog.dismiss();
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						resumeMergeService();
						dialog.dismiss();
					}
				}).setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						resumeMergeService();
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

	private synchronized void resumeMergeService() {
		if (mMergeService != null && mMergeService.isWorkPaused()) {
			mMergeService.resumeWork();
		};
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

	private void dismissStopProgressDialog() {
		dismissDialog(mStopProgressDialog);
	}

	private void stopMerging() {
		if (mMergeService != null) {
			mMergeService.stopWork();
		}
		// 最多等待45秒，如果45秒内不通过onEnd返回，此时可能发生异常，强制结束，并且不进入结果报告页面
		mHandler.postDelayed(mForceStopRunnable, MAX_STOP_WAIT_TIME);
	}

	private void unbindMergeService() {
		if (!mServiceBound) {
			return;
		}
		try {
			unbindService(mMergeServiceConnection);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mServiceBound = false;
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
		contentView.setTextViewText(R.id.title, getString(R.string.msg_merge_finished));
		contentView.setViewVisibility(R.id.result, View.GONE);
		notification.contentView = contentView;
		notification.contentIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				MergeProcessActivity.class), 0);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.defaults &= ~Notification.DEFAULT_VIBRATE;
		notification.vibrate = null;
		return notification;
	}

	private Notification getThemeHoloNotification() {
		return new Notification.Builder(this)
				.setSmallIcon(R.drawable.notification_icon)
				.setContentTitle(getString(R.string.msg_merge_finished))
				.setContentIntent(
						PendingIntent.getActivity(this, 0, new Intent(this,
								MergeProcessActivity.class), 0)).setAutoCancel(true)
				.getNotification();
	}

	private void showResultNotification() {
		if (mNotificationManager != null) {
			mNotificationManager.notify(RESULT_NOTIFICATION_ID, getAdaptiveResultNotification());
		}
	}

	private void clearNotification() {
		if (mNotificationManager != null) {
			mNotificationManager.cancel(RESULT_NOTIFICATION_ID);
		}
	}

	private void startReportActivity() {
		if (mActivityVisible) {
			Intent intent = new Intent(this, ReportActivity.class);
			intent.putExtra(ReportActivity.EXTRA_RESULT, mSuccessful);
			intent.putExtra(ReportActivity.EXTRA_TITLE,
					getString(R.string.title_merge_result_report));
			intent.putExtra(ReportActivity.EXTRA_ENABLE_BACK_KEY, true);
			long date = System.currentTimeMillis();
			if (mMergedRecord != null && mMergedRecord.getDate() != null) {
				date = mMergedRecord.getDate().getTime();
			}
			intent.putExtra(ReportActivity.EXTRA_DATE, date);
			intent.putExtra(ReportActivity.EXTRA_POSITIVE_BUTTON_TEXT, getString(R.string.finish));
			if (!mSuccessful) {
				intent.putExtra(ReportActivity.EXTRA_MESSAGES, mResults);
			}
			startActivity(intent);
			finish();
		}
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

	@Override
	public void onStart(Object arg1, Object arg2) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				updateDescription(getString(R.string.msg_preparing));
			}
		});
	}

	@Override
	public void onProceeding(final Object progress, final Object arg2, final Object arg3,
			final Object arg4) {
		final int progressValue = ((Integer) progress).intValue();
		// Log.d("GoBackup", "progress = " + progressValue);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				updateProgress(progressValue, arg3);
			}
		});
	}

	@Override
	public void onEnd(boolean success, Object arg1, Object arg2) {
		synchronized (mLock) {
			mHandler.removeCallbacks(mForceStopRunnable);
		}

		if (arg1 instanceof RestorableRecord) {
			mMergedRecord = (RestorableRecord) arg1;
		}
		if (arg2 instanceof ResultBean[]) {
			mResults = (ResultBean[]) arg2;
		}

		BackupManager.getInstance().addRestoreRecord(mMergedRecord);
		mHandler.sendEmptyMessage(MSG_SHOW_NOTIFICATION);
		Message.obtain(mHandler, MSG_DISMISS_DIALOG, mStopProgressDialog).sendToTarget();
		mHandler.sendEmptyMessage(MSG_UNBIND_SERVICE);
		Message.obtain(mHandler, MSG_START_REPORT_ACTIVITY, success ? 1 : 0, 1).sendToTarget();
	}

	private static final int MSG_DISMISS_DIALOG = 0x1001;
	private static final int MSG_SHOW_NOTIFICATION = 0x1002;
	private static final int MSG_UNBIND_SERVICE = 0x1003;
	private static final int MSG_START_REPORT_ACTIVITY = 0x1004;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_DISMISS_DIALOG :
					dismissDialog((Dialog) msg.obj);
					break;
				case MSG_SHOW_NOTIFICATION :
					showResultNotification();
					break;
				case MSG_UNBIND_SERVICE :
					unbindMergeService();
					break;
				case MSG_START_REPORT_ACTIVITY :
					mSuccessful = msg.arg1 > 0;
					mCompleted = true;
					startReportActivity();
					break;
				default :
					break;
			}
		}
	};

}