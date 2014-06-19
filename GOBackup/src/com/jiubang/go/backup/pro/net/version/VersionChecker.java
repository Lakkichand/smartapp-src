package com.jiubang.go.backup.pro.net.version;

import java.lang.reflect.Field;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.BaseActivity;
import com.jiubang.go.backup.pro.PreferenceManager;
import com.jiubang.go.backup.pro.net.version.VersionManager.VersionUpdateListener;

/**
 * 检查更新工具类
 *
 * @author maiyongshen
 */

public class VersionChecker implements VersionUpdateListener {
	public static final String ACTION_NEW_UPDATE = "com.jiubang.go.backup.action.new_update";
	public static final String ACTION_FORCE_UPDATE = "com.jiubang.go.backup.action.force_update";
	public static final String ACTION_SHOW_UPDATE_TIP = "com.jiubang.go.backup.action.show_update_tip";
	public static final String EXTRA_VERSION_INFO = "extra_version_info";
	public static final String EXTRA_MESSAGE = "extra_message";

	private static final String MARKET_APP_DETAIL = "market://details?id=";
	private static final String DOWNLOAD_URL_SEPARATOR = "\\|\\|";

	// private static final int MSG_SHOW_CHECKING_UPDATE_DIALOG = 0x1001;
	// private static final int MSG_DISMISS_CHECKING_UPDATE_DIALOG = 0x1002;
	// private static final int MSG_SHOW_UPDATE_INFO_DIALOG = 0x1003;
	// private static final int MSG_DISMISS_UPDATE_INFO_DIALOG = 0x1004;
	// private static final int MSG_SHOW_TIPS_DIALOG = 0x1005;
	// private static final int MSG_DISMISS_TIPS_DIALOG = 0x1006;
	// private static final int MSG_SHOW_FORCE_UPADTE_DIALOG = 0x1007;
	// private static final int MSG_DISMISS_FORCE_UPADTE_DIALOG = 0x1008;

	// 下次检查更新的间隔时间为7天
	private static final int CHECK_UPDATE_INTERVAL = 1000 * 60 * 60 * 24 * 7;

	private static VersionChecker sInstance = null;
	// private Context mContext;
	private boolean mCancelUpdate;
	private boolean mSilently;

	private Dialog mCheckUpdateDialog = null;

	// private Dialog mUpdateInfoDialog = null;
	// private Dialog mTipDialog = null;
	// private Dialog mForceUpdateDialog = null;

	private VersionChecker() {

	}

	public static synchronized VersionChecker getInstance() {
		if (sInstance == null) {
			sInstance = new VersionChecker();
		}
		return sInstance;
	}

	public void checkUpdate(Context context, boolean silently) {
		if (context == null) {
			throw new IllegalArgumentException("context is null!");
		}
		// mContext = context;
		mCancelUpdate = false;
		mSilently = silently;
		int type = VersionManager.TYPE_UPDATE_AUTOMATICALLY;
		if (!silently) {
			// mHandler.sendEmptyMessage(MSG_SHOW_CHECKING_UPDATE_DIALOG);
			type = VersionManager.TYPE_UPDATE_MANUALLY;
			showCheckUpdateDialog(context);
		}
		new VersionManager().checkVersion(context, type, this);
	}

	private void showCheckUpdateDialog(Context context) {
		if (context == null) {
			return;
		}
		final ProgressDialog dialog = new ProgressDialog(context);
		dialog.setTitle(R.string.title_update);
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setIndeterminate(true);
		dialog.setMessage(context.getString(R.string.msg_checking_update));
		dialog.setButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mCancelUpdate = true;
				dialog.dismiss();
			}
		});

		mCheckUpdateDialog = dialog;
		mCheckUpdateDialog.show();
	}

	private void dismissCheckUpdateDialog() {
		if (mCheckUpdateDialog != null && mCheckUpdateDialog.isShowing()) {
			mCheckUpdateDialog.dismiss();
		}
	}

	public static void showUpdateInfoDialog(final Context context, final VersionInfo versionInfo) {
		if (context == null || versionInfo == null) {
			return;
		}

		if (context instanceof Activity && ((Activity) context).isFinishing()) {
			return;
		}

		new AlertDialog.Builder(context)
				.setTitle(R.string.title_update_info)
				.setMessage(versionInfo.mTipInfo)
				.setPositiveButton(R.string.btn_update_now, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String[] downloadUrls = parseDownloadUrl(versionInfo.mDownloadUrl);
						String marketUrl = null;
						String ftpUrl = null;
						if (downloadUrls != null) {
							for (String url : downloadUrls) {
								if (url.startsWith(MARKET_APP_DETAIL)) {
									marketUrl = url;
								} else {
									ftpUrl = url;
								}
							}
						}
						if (!gotoMarket(context, marketUrl)) {
							downloadNewVersionByUrl(context, ftpUrl);
						}
						dialog.dismiss();
					}
				})
				.setNegativeButton(R.string.btn_update_later,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								scheduleNextCheckUpdateTime(context);
								dialog.dismiss();
							}
						}).setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						scheduleNextCheckUpdateTime(context);
					}
				}).show();
	}

	public static void showTipDialog(Context context, String message) {
		if (context == null) {
			return;
		}
		new AlertDialog.Builder(context).setTitle(R.string.alert_dialog_title).setMessage(message)
				.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}

	public static void showForceUpdateDialog(final Context context, final VersionInfo versionInfo) {
		if (context == null || versionInfo == null) {
			return;
		}
		/*
		 * if (context instanceof MainActivity) { // Log.d("GoBackup",
		 * "showForceUpdateDialog"); if (((MainActivity)
		 * context).isActivityStopped()) { return; } }
		 */
		new AlertDialog.Builder(context).setCancelable(false).setTitle(R.string.title_update_info)
				.setMessage(versionInfo.mTipInfo)
				.setPositiveButton(R.string.btn_update_now, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String[] downloadUrls = parseDownloadUrl(versionInfo.mDownloadUrl);
						String marketUrl = null;
						String ftpUrl = null;
						if (downloadUrls != null) {
							for (String url : downloadUrls) {
								if (url.startsWith(MARKET_APP_DETAIL)) {
									marketUrl = url;
								} else {
									ftpUrl = url;
								}
							}
						}
						if (!gotoMarket(context, marketUrl)) {
							downloadNewVersionByUrl(context, ftpUrl);
						}
						planToCheckUpdateNextTime(context);

						// 欺骗系统，不让对话框消失
						try {
							Field field = dialog.getClass().getSuperclass()
									.getDeclaredField("mShowing");
							field.setAccessible(true);
							// 将mShowing变量设为false，表示对话框已关闭
							field.set(dialog, false);
							dialog.dismiss();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).setNegativeButton(R.string.btn_exit_app, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent logoutIntent = new Intent(BaseActivity.ACTION_LOGOUT);
						logoutIntent.setPackage(context.getPackageName());
						context.sendBroadcast(logoutIntent);
						planToCheckUpdateNextTime(context);
						dialog.dismiss();
					}
				}).show();
	}

	private static boolean downloadNewVersionByUrl(Context context, String url) {
		if (url == null || url.equals("")) {
			return false;
		}
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static String[] parseDownloadUrl(String downloadUrl) {
		if (downloadUrl == null) {
			return null;
		}
		return downloadUrl.split(DOWNLOAD_URL_SEPARATOR);
	}

	public static boolean gotoMarket(Context context, String alternateMarketUrl) {
		if (context == null) {
			return false;
		}
		// Context appContext = context.getApplicationContext();
		Uri uri = null;
		if (alternateMarketUrl != null && alternateMarketUrl.startsWith(MARKET_APP_DETAIL)) {
			uri = Uri.parse(alternateMarketUrl);
		} else {
			final String packageName = context.getPackageName();
			uri = Uri.parse(MARKET_APP_DETAIL + packageName);
		}
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/*
	 * public void planToCheckUpdateNextTime(Context context) { if (context ==
	 * null) { return; } PreferenceManager pm = PreferenceManager.getInstance();
	 * pm.putBoolean(context.getApplicationContext(),
	 * PreferenceManager.KEY_SHOULD_CHECK_UPDATE, true); } public void
	 * cancelCheckUpdateNextTime(Context context) { if (context == null) {
	 * return; } PreferenceManager pm = PreferenceManager.getInstance();
	 * pm.putBoolean(context.getApplicationContext(),
	 * PreferenceManager.KEY_SHOULD_CHECK_UPDATE, false); }
	 */

	public static void planToCheckUpdateNextTime(Context context) {
		if (context == null) {
			return;
		}
		PreferenceManager pm = PreferenceManager.getInstance();
		pm.putLong(context, PreferenceManager.KEY_CHECK_UPDATE_TIME, 0);
	}

	public static void scheduleNextCheckUpdateTime(Context context) {
		if (context == null) {
			return;
		}
		PreferenceManager.getInstance().putLong(context, PreferenceManager.KEY_CHECK_UPDATE_TIME,
				System.currentTimeMillis() + CHECK_UPDATE_INTERVAL);
	}

	public boolean shouldCheckUpdateNow(Context context) {
		if (context == null) {
			return true;
		}
		PreferenceManager pm = PreferenceManager.getInstance();
		long shouldCheckTime = pm.getLong(context, PreferenceManager.KEY_CHECK_UPDATE_TIME, 0);
		long now = System.currentTimeMillis();
		return now >= shouldCheckTime;
	}

	private void sendBroadcast(Context context, String action, VersionInfo versionInfo,
			String message) {
		if (context == null) {
			return;
		}
		Intent intent = new Intent(action);
		intent.setPackage(context.getPackageName());
		intent.putExtra(EXTRA_VERSION_INFO, versionInfo);
		intent.putExtra(EXTRA_MESSAGE, message);
		context.getApplicationContext().sendBroadcast(intent);
	}

	@Override
	public void onVersionUpdate(Context context, VersionInfo versionInfo) {
		if (!mCancelUpdate) {
			// mHandler.sendEmptyMessage(MSG_DISMISS_CHECKING_UPDATE_DIALOG);
			// Message.obtain(mHandler, MSG_SHOW_UPDATE_INFO_DIALOG,
			// versionInfo).sendToTarget();
			dismissCheckUpdateDialog();
			sendBroadcast(context, ACTION_NEW_UPDATE, versionInfo, null);
		}
	}

	@Override
	public void onVersionForceUpdate(Context context, VersionInfo versionInfo) {
		// mHandler.sendEmptyMessage(MSG_DISMISS_CHECKING_UPDATE_DIALOG);
		// Message.obtain(mHandler, MSG_SHOW_FORCE_UPADTE_DIALOG,
		// versionInfo).sendToTarget();
		Log.d("GoBackup", "onVersionForceUpdate");
		dismissCheckUpdateDialog();
		sendBroadcast(context, ACTION_FORCE_UPDATE, versionInfo, null);
	}

	@Override
	public void onNoNewVersion(Context context) {
		if (!mCancelUpdate && !mSilently) {
			// mHandler.sendEmptyMessage(MSG_DISMISS_CHECKING_UPDATE_DIALOG);
			// Message.obtain(mHandler, MSG_SHOW_TIPS_DIALOG,
			// R.string.msg_update_latest_version, -1).sendToTarget();
			dismissCheckUpdateDialog();
			String message = context != null ? context
					.getString(R.string.msg_update_latest_version) : "";
			sendBroadcast(context, ACTION_SHOW_UPDATE_TIP, null, message);
		}
	}

	@Override
	public void onSystemPrompt(Context context, VersionInfo versionInfo) {

	}

	@Override
	public void onError(Context context, int errCode, String errMsg, Object data) {
		if (!mCancelUpdate && !mSilently) {
			// mHandler.sendEmptyMessage(MSG_DISMISS_CHECKING_UPDATE_DIALOG);
			// Message.obtain(mHandler, MSG_SHOW_TIPS_DIALOG,
			// R.string.msg_update_net_error, -1).sendToTarget();
			dismissCheckUpdateDialog();
			String message = context != null
					? context.getString(R.string.msg_update_net_error)
					: "";
			sendBroadcast(context, ACTION_SHOW_UPDATE_TIP, null, message);
		}
	}

	/*
	 * private Handler mHandler = new Handler() {
	 *
	 * @Override public void handleMessage(Message msg) { switch (msg.what) {
	 * case MSG_SHOW_CHECKING_UPDATE_DIALOG: showCheckUpdateDialog(mContext);
	 * break; case MSG_DISMISS_CHECKING_UPDATE_DIALOG:
	 * dismissCheckUpdateDialog(); break; case MSG_SHOW_TIPS_DIALOG:
	 * showTipDialog(mContext, mContext.getString(msg.arg1)); break; case
	 * MSG_SHOW_UPDATE_INFO_DIALOG: showUpdateInfoDialog(mContext, (VersionInfo)
	 * msg.obj); break; case MSG_DISMISS_UPDATE_INFO_DIALOG: if
	 * (mUpdateInfoDialog != null && mUpdateInfoDialog.isShowing()) {
	 * mUpdateInfoDialog.dismiss(); } break; case MSG_DISMISS_TIPS_DIALOG: if
	 * (mTipDialog != null && mTipDialog.isShowing()) { mTipDialog.dismiss(); }
	 * break; case MSG_SHOW_FORCE_UPADTE_DIALOG: showForceUpdateDialog(mContext,
	 * (VersionInfo) msg.obj); break; case MSG_DISMISS_FORCE_UPADTE_DIALOG: if
	 * (mForceUpdateDialog != null && mForceUpdateDialog.isShowing()) {
	 * mForceUpdateDialog.dismiss(); } break; default: break; } } };
	 */

}
