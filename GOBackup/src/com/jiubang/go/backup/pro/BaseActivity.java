package com.jiubang.go.backup.pro;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.net.version.VersionChecker;
import com.jiubang.go.backup.pro.net.version.VersionInfo;
import com.jiubang.go.backup.pro.util.Util;

/**
 * GO备份中大多数Activity的基类，主要增加监听SD卡状态及升级广播的方法，并提供相应的处理接口让有需要的子类实现
 *
 * @author maiyongshen
 */
public class BaseActivity extends Activity {
	public static final String ACTION_LOGOUT = "com.jiubang.go.backup.logout";

	private boolean mStopped;

	public BaseActivity() {
		super();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerLogOutEventRecevier();
	}

	@Override
	protected void onStart() {
//		long t = System.currentTimeMillis();
		super.onStart();
		
		registerSdCardStateReceiver();
		registerVersionUpdateEventRecevier();
		
		if (!Util.checkSdCardReady(this)) {
			// 没有sd卡挂载
			BackupManager.getInstance().releaseRestoreRecords();
			resetBackupRootPath();
			return;
		}
		if (!checkBackupRootPathValid()) {
			// 通过设置项设置的备份路径所在的sd卡无效，重新切换有效的存储路径
			resetBackupRootPath();
		}
		
		ensureRecordsValid();

		mStopped = false;
		
//		t = System.currentTimeMillis() - t;
//		LogUtil.d("BaseActivity onStart time = " + t);
	}
	
	private void ensureRecordsValid() {
//		final Dialog progressDialog = createSpinnerProgressDialog(false);
//		showDialog(progressDialog);
//		postAsyncTask(new Runnable() {
//			@Override
//			public void run() {
//				BackupManager.getInstance().updateRestoreRecords(BaseActivity.this);
//				dismissDialog(progressDialog);
//			}
//		});
		BackupManager.getInstance().updateRestoreRecords(BaseActivity.this);
	}

	@Override
	protected void onStop() {
		// Log.d("GoBackup", "BaseActivity onStop");
		super.onStop();
		unregisterVersionUpdateEventReceiver();
		unregisterSdCardStateReceiver();
		mStopped = true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterLogOutEventRecevier();
	}

	public boolean isStopped() {
		return mStopped;
	}

	private boolean checkBackupRootPathValid() {
		return Util.isPathValid(Util.getSdRootPathOnPreference(this));
	}

	private BroadcastReceiver mSDCardStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			Log.d("GoBackup", "sd card acion = " + action);
			if (Intent.ACTION_MEDIA_EJECT.equals(action)
					|| Intent.ACTION_MEDIA_REMOVED.equals(action)) {

			} else if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
				PreferenceManager.getInstance().enableShowNoneSdCardAlert(context, true);
				onSdCardMounted();
			} else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
				onSdCardUnmounted();
			}
		}
	};

	private BroadcastReceiver mLogoutReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (ACTION_LOGOUT.equals(action)) {
				onLogOut();
			}
		}
	};

	private BroadcastReceiver mVersionUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			VersionInfo versionInfo = intent.getParcelableExtra(VersionChecker.EXTRA_VERSION_INFO);
			String message = intent.getStringExtra(VersionChecker.EXTRA_MESSAGE);
			if (VersionChecker.ACTION_NEW_UPDATE.equals(action)) {
				VersionChecker.showUpdateInfoDialog(BaseActivity.this, versionInfo);
			} else if (VersionChecker.ACTION_FORCE_UPDATE.equals(action)) {
				VersionChecker.showForceUpdateDialog(BaseActivity.this, versionInfo);
			} else if (VersionChecker.ACTION_SHOW_UPDATE_TIP.equals(action)) {
				VersionChecker.showTipDialog(BaseActivity.this, message);
			}
		}
	};

	public IntentFilter getSdCardReceiverIntentFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_REMOVED);
		filter.addAction(Intent.ACTION_MEDIA_EJECT);
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		filter.addDataScheme("file");
		return filter;
	}

	public void registerSdCardStateReceiver() {
		registerReceiver(mSDCardStateReceiver, getSdCardReceiverIntentFilter());
	}

	public void unregisterSdCardStateReceiver() {
		try {
			unregisterReceiver(mSDCardStateReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void registerLogOutEventRecevier() {
		registerReceiver(mLogoutReceiver, new IntentFilter(ACTION_LOGOUT));
	}

	public void unregisterLogOutEventRecevier() {
		try {
			unregisterReceiver(mLogoutReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void registerVersionUpdateEventRecevier() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(VersionChecker.ACTION_NEW_UPDATE);
		filter.addAction(VersionChecker.ACTION_FORCE_UPDATE);
		filter.addAction(VersionChecker.ACTION_SHOW_UPDATE_TIP);
		registerReceiver(mVersionUpdateReceiver, filter);
	}

	public void unregisterVersionUpdateEventReceiver() {
		try {
			unregisterReceiver(mVersionUpdateReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void reInitBackupManager() {
		final Dialog progressDialog = createSpinnerProgressDialog(false);
		showDialog(progressDialog);
		postAsyncTask(new Runnable() {
			@Override
			public void run() {
				BackupManager.getInstance().init(BaseActivity.this);
				dismissDialog(progressDialog);
			}
		});
	}

	protected void onSdCardMounted() {
		// 更新GO备份根目录到preference中
		// Util.updateGOBackupRootPathToPreference(this);
		resetBackupRootPath();
		reInitBackupManager();
	}

	protected void onSdCardUnmounted() {
		// 更新GO备份根目录到preference中
		// Util.updateGOBackupRootPathToPreference(this);
		BackupManager.getInstance().releaseRestoreRecords();
		resetBackupRootPath();
	}

	private void resetBackupRootPath() {
		String preferenceSdRootPath = Util.getSdRootPathOnPreference(this);
		String validSdRootPath = Util.getDefalutValidSdPath(this);

		if (validSdRootPath == null) {
			PreferenceManager pm = PreferenceManager.getInstance();
			// 没有sd卡
			if (pm.isNoneSdCardAlertEnabled(this)) {
				showDialog(createBackupPathChangeDialog(getString(R.string.msg_no_sd)));
				pm.enableShowNoneSdCardAlert(this, false);
			}
			PreferenceManager.getInstance().putString(this, PreferenceManager.KEY_BACKUP_SD_PATH,
					"");
			return;
		}

		if (TextUtils.equals(preferenceSdRootPath, validSdRootPath)) {
			// 选择的路径存在
			return;
		}

		// 选择的路径的sd被移除
		if (TextUtils.isEmpty(preferenceSdRootPath)) {
			if (Util.isInternalSdPath(validSdRootPath)) {
				showDialog(createBackupPathChangeDialog(getString(
						R.string.msg_reset_backup_path_tips,
						getString(R.string.msg_internal_storage))));
			} else {
				showDialog(createBackupPathChangeDialog(getString(
						R.string.msg_reset_backup_path_tips,
						getString(R.string.msg_external_storage))));
			}
			PreferenceManager.getInstance().putString(this, PreferenceManager.KEY_BACKUP_SD_PATH,
					validSdRootPath);
			return;
		}

		// 选择的路径的sd被移除
		if (Util.isInternalSdPath(preferenceSdRootPath)) {
			// 内置sd卡被移除
			showDialog(createBackupPathChangeDialog(getString(R.string.msg_backup_path_change_tips,
					getString(R.string.msg_internal_storage),
					getString(R.string.msg_external_storage))));
		} else {
			// 外置sd卡被移除
			showDialog(createBackupPathChangeDialog(getString(R.string.msg_backup_path_change_tips,
					getString(R.string.msg_external_storage),
					getString(R.string.msg_internal_storage))));
		}
		PreferenceManager.getInstance().putString(this, PreferenceManager.KEY_BACKUP_SD_PATH,
				validSdRootPath);
	}
	protected void onLogOut() {
		finish();
	}

	private Dialog createBackupPathChangeDialog(String message) {
		Dialog dialog = new AlertDialog.Builder(this).setTitle(R.string.attention)
				.setMessage(message).setPositiveButton(R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).create();
		return dialog;

	}

	public static ProgressDialog createSpinnerProgressDialog(Context context, boolean cancelable) {
		if (context == null) {
			return null;
		}
		ProgressDialog dialog = new ProgressDialog(context);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setIndeterminate(true);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(cancelable);
		return dialog;
	}

	public ProgressDialog createSpinnerProgressDialog(boolean cancelable) {
		return createSpinnerProgressDialog(this, cancelable);
	}

	public void dismissDialog(Dialog dialog) {
		if (dialog == null || isFinishing()) {
			return;
		}
		if (dialog.isShowing()) {
			dialog.dismiss();
		}
	}

	public void showDialog(Dialog dialog) {
		if (dialog == null || isStopped() || isFinishing()) {
			return;
		}
		if (!dialog.isShowing()) {
			dialog.show();
		}
	}
	
	public void postAsyncTask(Runnable runnable) {
		GoBackupApplication.postRunnable(runnable);
	}
}
