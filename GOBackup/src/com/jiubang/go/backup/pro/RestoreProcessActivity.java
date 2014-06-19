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

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.AppRestoreEntry.AppRestoreType;
import com.jiubang.go.backup.pro.data.MessageReceiver;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.data.RestorableRecord.RecordRestoreArgs;
import com.jiubang.go.backup.pro.data.ResultBean;
import com.jiubang.go.backup.pro.model.AsyncWorkEngine;
import com.jiubang.go.backup.pro.model.AsyncWorkEngine.WorkDetailBean;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.model.ForegroundWorkerService;
import com.jiubang.go.backup.pro.model.ForegroundWorkerService.LocalBinder;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.model.RestoreService;
import com.jiubang.go.backup.pro.ui.WorkProcessDetailAdapter;
import com.jiubang.go.backup.pro.ui.WorkProcessDetailAdapter.OnViewUpdateListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 本地恢复进度页面
 *
 * @author maiyongshen
 */

public class RestoreProcessActivity extends BaseActivity implements IAsyncTaskListener {
	public static final String EXTRA_ROOT = "is_root";
	public static final String EXTRA_SHOULD_RESOTRE_SILENTLY = "should_restore_silently";
	public static final String EXTRA_APP_RESTORE_TYPE = "app_restore_type";

	private static final String KEY_START_RESTORE = "key_start";
	private static final String KEY_COMPLETE_RESTORE = "key_complete";
	private static final String KEY_RECORD_ID = "key_record_id";

	private static final int RESTORE_RESULT_NOTIFICATION_ID = 0x00ff2002;

	private static final int MAX_STOP_WAIT_TIME = 45 * 1000;

	private ProgressBar mProgressBar;
	private TextView mProgress;
	private TextView mDescription;
	private Button mButton;
	private Dialog mStopDialog;
	private Dialog mStopProgressDialog;
	private ListView mDetailListView;
	private int mDetailListViewScrollState;
	private WorkProcessDetailAdapter mWorksDetailAdapter;
	private Dialog mExceptionOcurredDialog = null;

	private BackupManager mBackupManager;
	private long mRecordId = -1;
	private RestorableRecord mRestoreRecord = null;
	private boolean mStarted;
	private boolean mCompleted;
	private boolean mSuccessful = false;
	private ForegroundWorkerService mRestoreService;
	private ServiceConnection mServiceConnection;
	private boolean mServiceBound = false;

	private NotificationManager mNotificationManager;

	private MessageReceiver mReceiver;

	private boolean mActivityVisible;
	private Object mLock = new byte[0];

	private ResultBean[] mResults;
	private boolean mShouldReboot;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Log.d("GoBackup", "RestoreProcessActivity onCreate");
		super.onCreate(savedInstanceState);
		initViews();
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		init(savedInstanceState);
		startRestore();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// Log.d("GoBackup", "RestoreProcessActivity onSaveInstanceState");
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_START_RESTORE, mStarted);
		outState.putBoolean(KEY_COMPLETE_RESTORE, mCompleted);
		outState.putLong(KEY_RECORD_ID, mRecordId);
	}

	@Override
	protected void onStart() {
		// Log.d("GoBackup", "RestoreProcessActivity onStart");
		super.onStart();
		mActivityVisible = true;
	}

	@Override
	protected void onResume() {
		// Log.d("GoBackup", "RestoreProcessActivity onResume");
		super.onResume();
		if (mStarted && mCompleted) {
			startReportActivity();
		}
	}

	@Override
	protected void onStop() {
		// Log.d("GoBackup", "BackupProcessActivity onStop");
		super.onStop();
		mActivityVisible = false;
	}

	@Override
	protected void onDestroy() {
		// Log.d("GoBackup", "RestoreProcessActivity onDestroy");
		super.onDestroy();
		clearNotification();
	}

	private void init(Bundle savedInstanceState) {
		mBackupManager = BackupManager.getInstance();
		if (savedInstanceState != null) {
			mStarted = savedInstanceState.getBoolean(KEY_START_RESTORE, false);
			mCompleted = savedInstanceState.getBoolean(KEY_COMPLETE_RESTORE, false);
			mRecordId = savedInstanceState.getLong(KEY_RECORD_ID);
		} else if (getIntent() != null) {
			mRecordId = getIntent().getLongExtra(RestoreBackupActivity.RECORD_ID, -1);
		}
		mRestoreRecord = mBackupManager.getRecordById(mRecordId);
		if (mRestoreRecord != null && !mRestoreRecord.dataAvailable()) {
			// TODO 如果同步比较缓慢，需要修改为异步
			mRestoreRecord.loadData(this);
		}
		if (mStarted && !mCompleted && mRestoreRecord == null) {
			showExceptionOcurredDialog();
		}
	}

	private void initViews() {
		setContentView(R.layout.layout_work_process);
		getWindow().setFormat(PixelFormat.RGBA_8888);

		TextView title = (TextView) findViewById(R.id.title);
		title.setText(getString(R.string.title_restoring));

		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
		mProgress = (TextView) findViewById(R.id.progress);
		mDescription = (TextView) findViewById(R.id.operation_desc);

		findViewById(R.id.optional_buttons).setVisibility(View.GONE);

		mButton = (Button) findViewById(R.id.single_button);
		mButton.setText(getString(R.string.btn_stop_restore));
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
		contentView.setTextViewText(R.id.title, getString(R.string.msg_restore_finished));
		contentView.setViewVisibility(R.id.result, View.GONE);
		notification.contentView = contentView;
		notification.contentIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				RestoreProcessActivity.class), 0);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.defaults &= ~Notification.DEFAULT_VIBRATE;
		notification.vibrate = null;
		return notification;
	}

	private Notification getThemeHoloNotification() {
		return new Notification.Builder(this)
				.setSmallIcon(R.drawable.notification_icon)
				.setContentTitle(getString(R.string.msg_restore_finished))
				.setContentIntent(
						PendingIntent.getActivity(this, 0, new Intent(this,
								RestoreProcessActivity.class), 0)).setAutoCancel(true)
				.getNotification();
	}

	private void showResultNotification() {
		if (mNotificationManager != null) {
			mNotificationManager.notify(RESTORE_RESULT_NOTIFICATION_ID,
					getAdaptiveResultNotification());
		}
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

	private void startRestore() {
		if (mStarted) {
			return;
		}
		if (mRestoreRecord == null) {
			showToast(getString(R.string.msg_record_corrupted));
			exit();
			return;
		}

		mStarted = true;
		final Intent intent = getIntent();
		final RecordRestoreArgs args = new RecordRestoreArgs();
		args.mRestorePath = mRestoreRecord.getRecordRootDir();
		args.mIsRoot = intent != null ? intent.getBooleanExtra(EXTRA_ROOT, false) : false;
		args.mAppRestoreType = intent != null ? (AppRestoreType) intent
				.getSerializableExtra(EXTRA_APP_RESTORE_TYPE) : AppRestoreType.APP_DATA;
		args.mSilentRestoreApp = intent != null ? intent.getBooleanExtra(
				EXTRA_SHOULD_RESOTRE_SILENTLY, false) : false;
		// args.mConfig = mRestoreRecord.getBackupPropertiesConfig();
//		args.mRecordDescribe = mRestoreRecord.getRecordDescribe();

		mServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {

			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mServiceBound = true;
				mRestoreService = ((LocalBinder) service).getService();
				mReceiver = (MessageReceiver) mRestoreService;
				mRestoreService.startWork(RestoreProcessActivity.this, mRestoreRecord, args,
						RestoreProcessActivity.this);
			}
		};
		Intent service = new Intent(this, RestoreService.class);
		bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	private Runnable mForceStopRunnable = new Runnable() {
		@Override
		public void run() {
			synchronized (mLock) {
				mSuccessful = false;
				dimissStopProgressDialog();
				unbindRestoreService();
				exit();
			}
		}
	};

	private void stopRestore() {
		if (mRestoreService != null) {
			mRestoreService.stopWork();
		}
		// 最多等待45秒
		mHandler.postDelayed(mForceStopRunnable, MAX_STOP_WAIT_TIME);
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

	private void stopAndExit() {
		if (mStarted && !mCompleted) {
			if (mRestoreService != null && !mRestoreService.isWorkPaused()) {
				mRestoreService.pauseWork();
			}
			showStopAlertDialog();
		} else if (!mStarted) {
			exit();
		}
	}

	private void startReportActivity() {
		if (mActivityVisible) {
			startActivity(getStartReportActivityIntent());
			exit();
		}
	}

	private void showToast(String toast) {
		Toast.makeText(this, toast, Toast.LENGTH_LONG);
	}

	private void clearNotification() {
		if (mNotificationManager != null) {
			mNotificationManager.cancel(RESTORE_RESULT_NOTIFICATION_ID);
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

	private void updateDescription(String desc) {
		if (mDescription != null) {
			mDescription.setText(desc);
		}
	}

	private void exit() {
		finish();
	}

	private void showExceptionOcurredDialog() {
		mExceptionOcurredDialog = new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.stat_notify_error)
				.setTitle(R.string.alert_dialog_title)
				.setMessage(R.string.msg_restore_exception_occurred)
				.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						exit();
					}
				}).create();
		showDialog(mExceptionOcurredDialog);
	}

	private Dialog createStopAlertDialog() {
		Dialog dialog = new AlertDialog.Builder(this).setIcon(android.R.drawable.stat_notify_error)
				.setTitle(R.string.title_stop_restore).setMessage(R.string.msg_discard_restoring)
				.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mHandler.sendEmptyMessage(MSG_SHOW_STOP_PROGRESS_DIALOG);
						stopRestore();
						dialog.dismiss();
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						resumeRestoreService();
						dialog.dismiss();
					}
				}).setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						resumeRestoreService();
					}
				}).create();
		dialog.setCanceledOnTouchOutside(false);
		return dialog;
	}

	private void showStopAlertDialog() {
		if (mStopDialog == null) {
			mStopDialog = createStopAlertDialog();
		}
		if (mStopDialog.isShowing()) {
			return;
		}
		showDialog(mStopDialog);
	}

	private synchronized void resumeRestoreService() {
		if (mRestoreService != null && mRestoreService.isWorkPaused()) {
			mRestoreService.resumeWork();
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

	private static final int MSG_READY_TO_START = 0x1002;
	private static final int MSG_UPDATE_PROGRESS = 0x1003;
	private static final int MSG_SHOW_NOTIFICATION = 0x1004;
	private static final int MSG_SHOW_RESULT = 0x1005;
	private static final int MSG_SHOW_STOP_PROGRESS_DIALOG = 0x1007;
	private static final int MSG_DISMISS_STOP_PROGRESS_DIALOG = 0x1008;
	private static final int MSG_UNBIND_SERVICE = 0X1009;
	private static final int MSG_UPDATE_WORK_DETAIL = 0X100a;
	private static final int MSG_INIT_WORK_DETAIL_LISTVIEW = 0x100b;

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_READY_TO_START :
					if (msg.obj != null) {
						updateDescription(msg.obj.toString());
					}
					break;
				case MSG_UPDATE_PROGRESS :
					updateProgress(msg.arg1, msg.obj);
					break;
				case MSG_SHOW_NOTIFICATION :
					showResultNotification();
					break;
				case MSG_SHOW_RESULT :
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
					unbindRestoreService();
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

	private Intent getStartReportActivityIntent() {
		Intent intent = new Intent(this, ReportActivity.class);
		intent.putExtra(ReportActivity.EXTRA_TITLE, getString(R.string.title_restore_result_report));
		intent.putExtra(ReportActivity.EXTRA_RESULT, mSuccessful);
		intent.putExtra(ReportActivity.EXTRA_ENABLE_BACK_KEY, true);
		intent.putExtra(ReportActivity.EXTRA_SHOULD_REBOOT, mShouldReboot);
		intent.putExtra(ReportActivity.EXTRA_DATE, mRestoreRecord.getDate().getTime());
		intent.putExtra(ReportActivity.EXTRA_POSITIVE_BUTTON_TEXT, mShouldReboot
				? getString(R.string.btn_reboot)
				: getString(R.string.finish));

		if (!mSuccessful) {
			intent.putExtra(ReportActivity.EXTRA_MESSAGES, mResults);
		} else if (mShouldReboot) {
			ResultBean resultBean = new ResultBean();
			resultBean.result = mSuccessful;
			resultBean.title = getString(R.string.msg_reboot_info);
			intent.putExtra(ReportActivity.EXTRA_MESSAGES, new ResultBean[] { resultBean });
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
			mHandler.sendEmptyMessage(MSG_UNBIND_SERVICE);
			return;
		}
		synchronized (mLock) {
			mHandler.removeCallbacks(mForceStopRunnable);
		}
		try {
			final int m1000 = 1000;
			Thread.sleep(m1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (arg1 instanceof ResultBean[]) {
			mResults = (ResultBean[]) arg1;
		}
		if (arg2 instanceof Boolean) {
			mShouldReboot = (Boolean) arg2;
		}
		mHandler.sendEmptyMessage(MSG_SHOW_NOTIFICATION);
		mHandler.sendEmptyMessage(MSG_DISMISS_STOP_PROGRESS_DIALOG);
		mHandler.sendEmptyMessage(MSG_UNBIND_SERVICE);
		Message.obtain(mHandler, MSG_SHOW_RESULT, success ? 1 : 0, 1).sendToTarget();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mReceiver != null) {
			mReceiver.handleMessage(requestCode, resultCode, data);
		}
	}

	private void updateWorkDetail(WorkDetailBean bean) {
		if (mWorksDetailAdapter != null) {
			mWorksDetailAdapter.update(bean);
		}
	}

}
