package com.jiubang.go.backup.pro;

import java.io.File;
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
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.MessageReceiver;
import com.jiubang.go.backup.pro.data.ResultBean;
import com.jiubang.go.backup.pro.model.AsyncWorkEngine;
import com.jiubang.go.backup.pro.model.AsyncWorkEngine.WorkDetailBean;
import com.jiubang.go.backup.pro.model.DownloadBackupService;
import com.jiubang.go.backup.pro.model.ForegroundWorkerService;
import com.jiubang.go.backup.pro.model.ForegroundWorkerService.LocalBinder;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.model.NetSyncTaskDbHelper;
import com.jiubang.go.backup.pro.ui.WorkProcessDetailAdapter;
import com.jiubang.go.backup.pro.ui.WorkProcessDetailAdapter.OnViewUpdateListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 云端恢复进度页面
 *
 * @author maiyongshen
 */
public class RestoreOnlineBackupProcessAcitvity extends BaseActivity implements IAsyncTaskListener {
	public static final String EXTRA_TASK_DB_FILE = "extra_task_db_file";

	private static final String KEY_START_RESTORE = "key_start";
	private static final String KEY_COMPLETE_RESTORE = "key_complete";

	private static final int DOWNLOAD_RESULT_NOTIFICATION_ID = 0x00ff40ff;
	private static final int DIALOG_STOP_ONLINE_BACKUP = 0x2001;

	private NotificationManager mNotificationManager;
	private ForegroundWorkerService mService;
	private ServiceConnection mServiceConnection;
	private boolean mServiceBound;

	private Object mLock = new byte[0];
	private static final int MAX_STOP_WAIT_TIME = 45 * 1000;

	private boolean mActivityVisible;
	private boolean mStarted = false;
	private boolean mCompleted = false;
	private boolean mSuccessful;
	private ResultBean[] mResults;
	private boolean mShouldReboot = false;
	private File mSourceTaskDbFile = null;
	private NetSyncTaskDbHelper mTaskDbHelper = null;

	private ProgressBar mProgressBar;
	private TextView mProgress;
	private ListView mDetailListView;
	private int mDetailListViewScrollState;
	private Button mPauseButton;
	private Button mCancelButton;
	private WorkProcessDetailAdapter mWorksDetailAdapter;

	private Dialog mStopProgressDialog;
	private Dialog mStopAlertDialog = null;

	private boolean mIsNeedShowResult = true;
	private boolean mIsCancelRestore = false;
	
	private MessageReceiver mReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initViews();
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mSourceTaskDbFile = (File) getIntent().getSerializableExtra(EXTRA_TASK_DB_FILE);
		if (mSourceTaskDbFile == null || !mSourceTaskDbFile.exists()) {
			Toast.makeText(this, "cannot find task database file!", Toast.LENGTH_LONG).show();
			finish();
		}
		mTaskDbHelper = new NetSyncTaskDbHelper(this, mSourceTaskDbFile.getAbsolutePath(),
				NetSyncTaskDbHelper.getDbVersion());
		init(savedInstanceState);
		startDownloadAndRestore(mTaskDbHelper);
	}

	private void init(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mStarted = savedInstanceState.getBoolean(KEY_START_RESTORE, false);
			mCompleted = savedInstanceState.getBoolean(KEY_COMPLETE_RESTORE, false);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_START_RESTORE, mStarted);
		outState.putBoolean(KEY_COMPLETE_RESTORE, mCompleted);
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
		if (mTaskDbHelper != null) {
			mTaskDbHelper.close();
		}
	}

	@Override
	public void onBackPressed() {
		if (mService != null && !mService.isWorkPaused()) {
			// 恢复过程中，如果不是暂停状态，不允许相应返回键
			return;
		}
		if (mService != null && mService.isWorkPaused()) {
			mService.stopWork();
			mIsNeedShowResult = false;
			mHandler.sendEmptyMessage(MSG_SHOW_STOP_PROGRESS_DIALOG);
			return;
		}
		super.onBackPressed();
	}

	private void initViews() {
		setContentView(R.layout.layout_work_process);
		getWindow().setFormat(PixelFormat.RGBA_8888);

		TextView title = (TextView) findViewById(R.id.title);
		title.setText(R.string.title_restoring);

		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
		mProgress = (TextView) findViewById(R.id.progress);

		findViewById(R.id.single_button).setVisibility(View.GONE);

		mPauseButton = (Button) findViewById(R.id.positive_btn);
		mPauseButton.setVisibility(View.VISIBLE);
		mPauseButton.setText(R.string.pause);
		mPauseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mService != null) {
					if (mService.isWorkPaused()) {
						mService.resumeWork();
						updatePauseButton(true);
					} else {
						mService.pauseWork();
						updatePauseButton(false);
					}
				}
			}
		});

		mCancelButton = (Button) findViewById(R.id.negative_btn);
		mCancelButton.setVisibility(View.VISIBLE);
		mCancelButton.setText(R.string.cancel);
		mCancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showStopAlertDialog();
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

	private void updatePauseButton(boolean isPause) {
		String str = isPause ? getString(R.string.pause) : getString(R.string.resume);
		mPauseButton.setText(str);
	}

	private void showResultNotification() {
		if (mNotificationManager != null) {
			mNotificationManager.notify(DOWNLOAD_RESULT_NOTIFICATION_ID,
					getAdaptiveResultNotification());
		}
	}

	private void clearNotification() {
		if (mNotificationManager != null) {
			mNotificationManager.cancel(DOWNLOAD_RESULT_NOTIFICATION_ID);
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
		contentView.setTextViewText(R.id.title,
				getString(R.string.msg_restore_online_backup_finished));
		contentView.setViewVisibility(R.id.result, View.GONE);
		notification.contentView = contentView;
		notification.contentIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				RestoreOnlineBackupProcessAcitvity.class), 0);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.defaults &= ~Notification.DEFAULT_VIBRATE;
		notification.vibrate = null;
		return notification;
	}

	private Notification getThemeHoloNotification() {
		return new Notification.Builder(this)
				.setSmallIcon(R.drawable.notification_icon)
				.setContentTitle(getString(R.string.msg_restore_online_backup_finished))
				.setContentIntent(
						PendingIntent.getActivity(this, 0, new Intent(this,
								RestoreOnlineBackupProcessAcitvity.class), 0)).setAutoCancel(true)
				.getNotification();
	}

	private void startDownloadAndRestore(final NetSyncTaskDbHelper taskDbHelper) {
		if (mStarted) {
			return;
		}

		mStarted = true;
		mServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mService = ((LocalBinder) service).getService();
				mReceiver = (MessageReceiver) mService;
				mService.startWork(RestoreOnlineBackupProcessAcitvity.this, taskDbHelper, null,
						RestoreOnlineBackupProcessAcitvity.this);
				mServiceBound = true;
			}
		};
		Intent intent = new Intent(this, DownloadBackupService.class);
		bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	private void startReportActivity() {
		if (mActivityVisible) {
			Intent intent = new Intent(this, ReportActivity.class);
			intent.putExtra(ReportActivity.EXTRA_TITLE,
					getString(R.string.title_restore_result_report));
			// TODO 强制设置为false 结果页才会显示列表样式
			intent.putExtra(ReportActivity.EXTRA_RESULT, false);
			intent.putExtra(ReportActivity.EXTRA_ENABLE_BACK_KEY, true);
			intent.putExtra(ReportActivity.EXTRA_SHOULD_REBOOT, mShouldReboot);
			intent.putExtra(ReportActivity.EXTRA_DATE, System.currentTimeMillis());
			intent.putExtra(ReportActivity.EXTRA_POSITIVE_BUTTON_TEXT, mShouldReboot
					? getString(R.string.btn_reboot)
					: getString(R.string.finish));
			intent.putExtra(ReportActivity.EXTRA_MESSAGES, mResults);
			startActivity(intent);
			finish();
		}
	}

	private void unbindRestoreService() {
		if (!mServiceBound) {
			return;
		}
		try {
			unbindService(mServiceConnection);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mServiceBound = false;
	}

	@Override
	public void onStart(Object arg1, Object arg2) {
		if (arg1 instanceof List<?>) {
			Message.obtain(mHandler, MSG_INIT_WORK_DETAIL_LISTVIEW, arg1).sendToTarget();
		}
		Message.obtain(mHandler, MSG_UPDATE_OVERALL_PROGRESS_BAR, 0, -1).sendToTarget();
	}

	@Override
	public void onProceeding(final Object progress, final Object arg2, Object arg3, Object arg4) {
		Message.obtain(mHandler, MSG_UPDATE_OVERALL_PROGRESS_BAR, ((Integer) progress).intValue(),
				-1).sendToTarget();
		if (arg2 instanceof WorkDetailBean) {
			Message.obtain(mHandler, MSG_UPDATE_WORK_DETAIL, arg2).sendToTarget();
		}
		try {
			// 异步线程休眠一段时间，让主线程有机会更新UI
			final int threadSleepTime = 50;
			Thread.sleep(threadSleepTime);
		} catch (InterruptedException e) {
		};
	}

	@Override
	public void onEnd(boolean success, Object arg1, Object arg2) {
		if (arg1 instanceof ResultBean[]) {
			mResults = (ResultBean[]) arg1;
		}
		if (arg2 instanceof Boolean) {
			mShouldReboot = (Boolean) arg2;
		}

		synchronized (mLock) {
			mHandler.removeCallbacks(mForceStopRunnable);
		}

		mHandler.sendEmptyMessage(MSG_SHOW_NOTIFICATION);
		Message.obtain(mHandler, MSG_DISMISS_DIALOG, mStopProgressDialog).sendToTarget();
		mHandler.sendEmptyMessage(MSG_UNBIND_SERVICE);

		if (success || mIsCancelRestore) {
			if (mSourceTaskDbFile != null && mSourceTaskDbFile.exists()) {
				mSourceTaskDbFile.delete();
			}
		} else {
			File tempTaskDbFile = getDatabasePath(NetSyncTaskDbHelper.getDbName());
			boolean ret = Util.copyFile(tempTaskDbFile.getAbsolutePath(),
					mSourceTaskDbFile.getAbsolutePath());
			if (ret) {
				tempTaskDbFile.delete();
			}
		}

		if (mIsNeedShowResult) {
			Message.obtain(mHandler, MSG_SHOW_RESULT, success ? 1 : 0, 1).sendToTarget();
		} else {
			mCompleted = true;
			finish();
		}
	}

	private Runnable mForceStopRunnable = new Runnable() {
		@Override
		public void run() {
			synchronized (mLock) {
				mSuccessful = false;
				Message.obtain(mHandler, MSG_DISMISS_DIALOG, mStopProgressDialog).sendToTarget();
				Message.obtain(mHandler, MSG_UNBIND_SERVICE).sendToTarget();
				Message.obtain(mHandler, MSG_EXIT).sendToTarget();
			}
		}
	};

	private void exit() {
		finish();
	}

	private void stopRestore() {
		if (mService != null) {
			mService.stopWork();
		}
		mHandler.sendEmptyMessage(MSG_SHOW_STOP_PROGRESS_DIALOG);
		// 最多等待45秒
		mHandler.postDelayed(mForceStopRunnable, MAX_STOP_WAIT_TIME);
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

	private void showStopAlertDialog() {
		if (mStopAlertDialog == null) {
			mStopAlertDialog = new AlertDialog.Builder(this).setTitle(R.string.title_stop_backup)
					.setCancelable(true).setMessage(R.string.download_choose_to_stop)
					.setPositiveButton(R.string.ok, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							stopRestore();
							mIsCancelRestore = true;
						}
					}).setNegativeButton(R.string.cancel, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).create();
		}
		showDialog(mStopAlertDialog);
	}

	private void updateOverallProgressBar(int progress) {
		mProgressBar.setProgress(progress);
		mProgress.setText(getString(R.string.progress_format, String.valueOf(progress)));
	}

	private void updateWorkDetail(WorkDetailBean bean) {
		if (mWorksDetailAdapter != null) {
			mWorksDetailAdapter.update(bean);
		}
	}

	private static final int MSG_UPDATE_OVERALL_PROGRESS_BAR = 0x1001;
	private static final int MSG_UPDATE_WORK_DETAIL = 0x1002;
	private static final int MSG_UNBIND_SERVICE = 0x1003;
	private static final int MSG_SHOW_NOTIFICATION = 0x1004;
	private static final int MSG_DISMISS_DIALOG = 0x1005;
	private static final int MSG_SHOW_RESULT = 0x1006;
	private static final int MSG_INIT_WORK_DETAIL_LISTVIEW = 0x1007;
	private static final int MSG_SHOW_STOP_PROGRESS_DIALOG = 0x1008;
	private static final int MSG_EXIT = 0x1009;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_UPDATE_OVERALL_PROGRESS_BAR :
					updateOverallProgressBar(msg.arg1);
					break;
				case MSG_UPDATE_WORK_DETAIL :
					updateWorkDetail((WorkDetailBean) msg.obj);
					break;
				case MSG_UNBIND_SERVICE :
					unbindRestoreService();
					break;
				case MSG_SHOW_NOTIFICATION :
					showResultNotification();
					break;
				case MSG_DISMISS_DIALOG :
					dismissDialog((Dialog) msg.obj);
					break;
				case MSG_SHOW_RESULT :
					mSuccessful = msg.arg1 > 0;
					mCompleted = true;
					startReportActivity();
					break;
				case MSG_INIT_WORK_DETAIL_LISTVIEW :
					mWorksDetailAdapter.init((List<AsyncWorkEngine.WorkDetailBean>) msg.obj);
					break;
				case MSG_SHOW_STOP_PROGRESS_DIALOG :
					showStopProgressDialog();
					break;
				case MSG_EXIT :
					exit();
					break;
				default :
					break;
			}
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mReceiver != null) {
			mReceiver.handleMessage(requestCode, resultCode, data);
		}
	}
}
