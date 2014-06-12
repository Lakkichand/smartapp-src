/*
 * 文 件 名:  AppDownloadListener1.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-8-20
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.utils;

import java.io.File;
import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.base.component.AppInstallActivity;
import com.jiubang.ggheart.appgame.base.data.AppGameNetInfoLog;
import com.jiubang.ggheart.appgame.base.data.AppGameNetLogControll;
import com.jiubang.ggheart.appgame.base.downloadmanager.AppsDownloadActivity;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.IAidlDownloadListener;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.appgame.download.InstallManager;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * <br>
 * 类描述: <br>
 * 功能详细描述:
 * 
 * @author liuxinyang
 * @date [2012-8-20]
 */
public class AppDownloadListener extends IAidlDownloadListener.Stub {

	private static final String TAG = "AppDownloadListener";

	private static final boolean DEBUG = false;

	public static final String DOWNLOAD_INFO_FORMAT_STRING = "%1$s %2$s"; // 下载信息格式化字符串

	public static final String DOWNLOAD_TICKER_FORMAT_STRING = "%1$s %2$s"; // 下载信息格式化字符串

	public static final String NOTIFY_TAG = "AppDownloadListener notify tag"; // 本应用发送通知的标识

	private static final long REFRESH_INTERVAL = 1000;

	private NotificationManager mNotificationManager = null;

	private Notification mNotification = null;

	private PendingIntent mPendingIntent = null;

	private int mDisplayPercent = 0;

	private int mPercentCount = 0;

	private String mStartTickerText = null;

	private String mStopTickerText = null;

	private String mFailTickerText = null;

	private String mCompletedTickerText = null;

	private String mCancelTickerText = null;

	private String mConnectText = null;

	private String mDownloadText = null;

	private String mPauseText = null;

	private String mFailText = null;

	private String mCompleteText = null;

	private String mProgressText = null;

	private long mPrevRefreshTime;
	private boolean mShowNotification = true;
	private Context mContext = null;
	public static final String UPDATE_DOWNLOAD_INFO = "UPDATE_DOWNLOAD_INFO";

	private long mStartConntion = 0;
	private long mStartDonwload = 0;
	private long mAlreadyDownloadSize = 0;
	private int mChildThreadCode = 0;

	private final int mStep = 1;
	public AppDownloadListener(Context context) {
		mContext = context;
		mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override
	public void onStart(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub
		if (task == null) { // add by zhaojunjie
			return;
		}

		AppGameNetLogControll.getInstance().startRecord(mContext,
				AppGameNetLogControll.DEFAULT_CURRENT_THREAD_CODE,
				AppGameNetInfoLog.NETLOG_TYPE_FOR_DOWNLOAD_APK);
		AppGameNetLogControll.getInstance().setUrl(
				AppGameNetLogControll.DEFAULT_CURRENT_THREAD_CODE, task.getDownloadUrl());
		mChildThreadCode = Thread.currentThread().hashCode();
		mStartConntion = System.currentTimeMillis();

		sendBroadcastingToAppCenter(task);

		if (mContext != null && mNotificationManager != null) {
			String downloadName = task.getDownloadName();
			if (downloadName != null) {
				mStartTickerText = String.format(DOWNLOAD_TICKER_FORMAT_STRING, downloadName,
						mContext.getString(R.string.appgame_notification_ticker_start_text));
				mStopTickerText = String.format(DOWNLOAD_TICKER_FORMAT_STRING, downloadName,
						mContext.getString(R.string.appgame_notification_ticker_stop_text));
				mFailTickerText = String.format(DOWNLOAD_TICKER_FORMAT_STRING, downloadName,
						mContext.getString(R.string.appgame_notification_ticker_fail_text));
				mCompletedTickerText = String.format(DOWNLOAD_TICKER_FORMAT_STRING, downloadName,
						mContext.getString(R.string.appgame_notification_ticker_complete_text));
				mCancelTickerText = String.format(DOWNLOAD_TICKER_FORMAT_STRING, downloadName,
						mContext.getString(R.string.appgame_notification_ticker_cancel_text));

				mConnectText = String.format(DOWNLOAD_INFO_FORMAT_STRING, downloadName,
						mContext.getString(R.string.appgame_notification_connect_text));
				mDownloadText = String.format(DOWNLOAD_INFO_FORMAT_STRING, downloadName,
						mContext.getString(R.string.appgame_notification_download_text));
				mPauseText = String.format(DOWNLOAD_INFO_FORMAT_STRING, downloadName,
						mContext.getString(R.string.appgame_notification_pause_text));
				mFailText = String.format(DOWNLOAD_INFO_FORMAT_STRING, downloadName,
						mContext.getString(R.string.appgame_notification_fail_text));
				mCompleteText = mContext.getString(R.string.appgame_notification_complete_text);
				mProgressText = mContext.getString(R.string.appgame_notification_progress_text);

				Intent intent = new Intent();
				intent.setClass(mContext, AppsDownloadActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mPendingIntent = PendingIntent.getActivity(mContext, 0, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);

				mNotification = new Notification(R.drawable.notification_download_icon,
						mStartTickerText, System.currentTimeMillis());
				mNotification.contentIntent = mPendingIntent;
				mNotification.flags = Notification.FLAG_AUTO_CANCEL;
				mNotification.setLatestEventInfo(mContext, mConnectText,
						mProgressText + " " + task.getAlreadyDownloadPercent() + "%",
						mPendingIntent);
				if (mNotification.contentView != null) {
					mNotificationManager.notify(NOTIFY_TAG, (int) task.getId(), mNotification);
				}
			} else {
				mShowNotification = false;
			}
		}
	}
	@Override
	public void onWait(DownloadTask task) throws RemoteException {
		sendBroadcastingToAppCenter(task);
	}

	@Override
	public void onUpdate(DownloadTask task) throws RemoteException {
		if (task == null) {
			return;
		}
		if (task != null) {
			long currentTime = System.currentTimeMillis();

			if (mStartDonwload == 0) {
				mStartDonwload = currentTime;
				mAlreadyDownloadSize = task.getAlreadyDownloadSize();
			}

			if (currentTime - mPrevRefreshTime > REFRESH_INTERVAL) {
				mPrevRefreshTime = currentTime;

				if (mShowNotification && mNotificationManager != null && mNotification != null) {
					// 更新通知栏的step为1%
					if (task.getAlreadyDownloadPercent() < mPercentCount && mPercentCount > 100) {
						return;
					}
					sendBroadcastingToAppCenter(task);
					mPercentCount += mStep;
					mDisplayPercent = task.getAlreadyDownloadPercent();
					mNotification.flags = Notification.FLAG_ONGOING_EVENT;
					mNotification.contentIntent = mPendingIntent;
					mNotification.setLatestEventInfo(mContext, mDownloadText, mProgressText + " "
							+ task.getAlreadyDownloadPercent() + "%", mPendingIntent);
					//					mNotificationManager.notify(NOTIFY_TAG, (int) task.getId(), mNotification);
					if (mNotification.contentView != null) {
						mNotificationManager.notify(NOTIFY_TAG, (int) task.getId(), mNotification);
					}
				}
			}
		}
	}

	@Override
	public void onComplete(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub
		if (task != null) {

			stopAppGameNetLog(task);

			String filePath = task.getSaveFilePath();
			File saveFile = new File(filePath);
			if (saveFile.exists() && saveFile.isFile()) {
				sendBroadcastingToAppCenter(task);
			}

			if (mContext != null && mNotificationManager != null) {
				// 首先移除之前的通知
				mNotificationManager.cancel(NOTIFY_TAG, (int) task.getId());

				if (mShowNotification) {
					// 创建新的通知，该通知可清除，点击后自动消失，并且进行安装
					Intent intent = new Intent();
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setDataAndType(Uri.fromFile(new File(filePath)),
							"application/vnd.android.package-archive");
					intent.setClass(mContext, AppInstallActivity.class);
					PendingIntent downCompletedIntent = PendingIntent.getActivity(mContext, 0,
							intent, 0);
					Notification finishNotification = new Notification(
							R.drawable.notification_download_complete_icon, mCompletedTickerText,
							System.currentTimeMillis());
					finishNotification.setLatestEventInfo(mContext, task.getDownloadName(),
							mCompleteText, downCompletedIntent);
					// 该通知可清除，点击后自动消失
					finishNotification.flags = Notification.FLAG_AUTO_CANCEL;
					//					mNotificationManager.notify(NOTIFY_TAG, (int) task.getId(), finishNotification);
					if (mNotification.contentView != null) {
						mNotificationManager.notify(NOTIFY_TAG, (int) task.getId(), finishNotification);
					}
				}
			}
			openFile(task.getSaveFilePath());
		}
	}

	@Override
	public void onFail(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub
		if (mContext != null && task != null) {
			if (mNotificationManager != null) {
				// 移除之前的通知
				mNotificationManager.cancel(NOTIFY_TAG, (int) task.getId());
				// 更新下载信息
				if (mShowNotification && mNotification != null) {
					mNotification.tickerText = mFailTickerText;
					mNotification.contentIntent = mPendingIntent;
					//					mNotificationRemoteViews.setTextViewText(R.id.downloadTextView, mFailText);
					//					mNotification.contentView = mNotificationRemoteViews;
					mNotification.setLatestEventInfo(mContext, mFailText, mProgressText + " "
							+ task.getAlreadyDownloadPercent() + "%", mPendingIntent);
					mNotification.flags = Notification.FLAG_AUTO_CANCEL;
					//					mNotificationManager.notify(NOTIFY_TAG, (int) task.getId(), mNotification);
					if (mNotification.contentView != null) {
						mNotificationManager.notify(NOTIFY_TAG, (int) task.getId(), mNotification);
					}
				}
			}
		}
	}

	@Override
	public void onReset(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStop(DownloadTask task) throws RemoteException {
		if (mNotificationManager != null && mNotification != null) {
			mNotification.tickerText = mStopTickerText;
			mNotification.flags = Notification.FLAG_AUTO_CANCEL;
			mNotification.contentIntent = mPendingIntent;
			//			if (RecommAppsUtils.isZh()) {
			mNotification.setLatestEventInfo(mContext, mPauseText,
					mProgressText + " " + task.getAlreadyDownloadPercent() + "%", mPendingIntent);
			//			} else {
			//				mNotification.setLatestEventInfo(mContext, mPauseText,
			//						task.getAlreadyDownloadPercent() + "%",
			//						mPendingIntent);
			//			}
			//			mNotificationManager.notify(NOTIFY_TAG, (int) task.getId(), mNotification);
			if (mNotification.contentView != null) {
				mNotificationManager.notify(NOTIFY_TAG, (int) task.getId(), mNotification);
			}
		}
		if (task != null) {
			// add by xiedezhi
			sendBroadcastingToAppCenter(task);
		}
	}

	@Override
	public void onCancel(DownloadTask task) throws RemoteException {
		if (task != null) {
			sendBroadcastingToAppCenter(task);
			stopAppGameNetLog(task);
			if (mNotificationManager != null) {
				if (mShowNotification && mNotification != null) {
					// 更新下载信息
					mNotification.tickerText = mCancelTickerText;
					mNotification.contentIntent = mPendingIntent;
					mNotification.flags = Notification.FLAG_AUTO_CANCEL;
					//					mNotificationManager.notify(NOTIFY_TAG, (int) task.getId(), mNotification);
					if (mNotification.contentView != null) {
						mNotificationManager.notify(NOTIFY_TAG, (int) task.getId(), mNotification);
					}
				}
			}
			String filePath = task.getSaveFilePath();
			if (TextUtils.isEmpty(filePath)) {
				return;
			}
			File saveFile = new File(filePath);
			if (saveFile.exists()) {
				saveFile.delete();
			}
		}
	}

	@Override
	public void onDestroy(DownloadTask task) throws RemoteException {
		if (task != null) {
			// 如果任务还没有下载完成
			if (mNotificationManager != null) {
				// 把通知移除
				mNotificationManager.cancel(NOTIFY_TAG, (int) task.getId());
			}
			if (DEBUG) {
				Log.i("", "-----------------state: " + task.getState());
			}
		}
	}

	@Override
	public void onConnectionSuccess(DownloadTask task) throws RemoteException {
		long time = System.currentTimeMillis() - mStartConntion;
		AppGameNetLogControll.getInstance().setConnectionTime(
				AppGameNetLogControll.DEFAULT_CURRENT_THREAD_CODE, time);
	}

	@Override
	public void onException(DownloadTask task) throws RemoteException {
		ArrayList<Exception> list = task.getExceptionList();
		if (mStartDonwload != 0) {
			setDonwloadSpeed(task);
			for (Exception e : list) {
				AppGameNetLogControll.getInstance().setExceptionCode(mChildThreadCode, e);
			}
		} else {
			AppGameNetLogControll.getInstance().setDownloadSpeed(mChildThreadCode, "0");
			for (Exception e : list) {
				AppGameNetLogControll.getInstance().setExceptionCode(
						AppGameNetLogControll.DEFAULT_CURRENT_THREAD_CODE, e);
			}

		}
		mStartConntion = 0;
		mStartDonwload = 0;
	}

	private void handleDownloadFail(DownloadTask downloadTask) {
		long taskId = downloadTask.getId();
		String filePath = downloadTask.getSaveFilePath();
		if (filePath != null && filePath.length() > 0) {
			File saveFile = new File(filePath);
			if (saveFile.exists()) {
				saveFile.delete();
			}
		}
		IDownloadService mDownloadController = GOLauncherApp.getApplication()
				.getDownloadController();
		try {
			if (mDownloadController != null) {
				mDownloadController.removeDownloadTaskById(taskId);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		sendBroadcastingToAppCenter(downloadTask);

		// 首先移除之前的通知
		if (mNotificationManager != null) {
			// 移除之前的通知
			//			mNotificationManager.cancel(NOTIFY_TAG, (int) downloadTask.getId());
			mNotificationManager.cancel(NOTIFY_TAG, (int) downloadTask.getId());
			// 更新下载信息
			if (mShowNotification && mNotification != null) {
				mNotification.tickerText = mFailTickerText;
				mNotification.contentIntent = mPendingIntent;
				mNotification.flags = Notification.FLAG_AUTO_CANCEL;
				//				mNotificationManager.notify(NOTIFY_TAG, (int) downloadTask.getId(), mNotification);
				if (mNotification.contentView != null) {
					mNotificationManager.notify(NOTIFY_TAG, (int) downloadTask.getId(), mNotification);
				}
			}
		}
	}

	private void stopAppGameNetLog(DownloadTask downloadTask) {
		setDonwloadSpeed(downloadTask);
		AppGameNetLogControll.getInstance().stopRecord(mChildThreadCode, mContext);
	}

	private void setDonwloadSpeed(DownloadTask downloadTask) {
		long time = System.currentTimeMillis() - mStartDonwload;
		if (time > 0) {
			String speed = String.valueOf((downloadTask.getTotalSize() - mAlreadyDownloadSize)
					/ time);
			if (DEBUG) {
				Log.d(TAG, "download apk speed : " + speed);
			}
			AppGameNetLogControll.getInstance().setDownloadSpeed(mChildThreadCode, speed);
		}
	}

	/**
	 * 打开APK文件进行安装的方法
	 * 
	 * @param file
	 */
	private void openFile(final String filepath) {
		File file = new File(filepath);
		if (!file.exists()) {
			return;
		}
		String[] token = file.getName().split("\\.");
		String pf = token[token.length - 1];
		if (!pf.equals("apk")) {
			return;
		}
		//		// DefaultDownloadListener是通过ClassName在DownloadService的进程实例化的对象
		//		// 所以这里可以直接取得下载控制接口
		//		IDownloadService mDownloadController = IDownloadService.Stub.asInterface(DownloadService
		//				.getContext().getBinder());
		//		try {
		//			if (mDownloadController != null) {
		//				mDownloadController.addInstallPath(filepath);
		//			}
		//		} catch (RemoteException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		InstallManager mInstallManager = InstallManager.getInstance(GOLauncherApp.getContext());
		if (filepath != null) {
			mInstallManager.addPkgToArray(filepath);
		}
	}

	/**
	 * 把下载的消息广播到应用中心
	 * 
	 * @param msgId
	 * @param param
	 * @param object
	 * @param objects
	 */
	private void sendBroadcastingToAppCenter(DownloadTask downloadTask) {
		Intent intent = new Intent(ICustomAction.ACTION_APP_DOWNLOAD);
		intent.putExtra(UPDATE_DOWNLOAD_INFO, downloadTask);
		mContext.sendBroadcast(intent);
	}

	//-------------------以下代码用于获取系统通知栏的字体颜色--------------------------------------//

	//系统通知栏的字体颜色值
	//	private Integer mNotificationTextColor = null;
	//
	//	private final String mCOLOR_SEARCH_RECURSE_TIP = "Custom notification";
	//
	//	private boolean recurseGroup(ViewGroup gp) {
	//		final int count = gp.getChildCount();
	//		for (int i = 0; i < count; ++i) {
	//			if (gp.getChildAt(i) instanceof TextView) {
	//				final TextView text = (TextView) gp.getChildAt(i);
	//				final String szText = text.getText().toString();
	//				if (mCOLOR_SEARCH_RECURSE_TIP.equals(szText)) {
	//					mNotificationTextColor = text.getTextColors().getDefaultColor();
	//					return true;
	//				}
	//			} else if (gp.getChildAt(i) instanceof ViewGroup) {
	//				return recurseGroup((ViewGroup) gp.getChildAt(i));
	//			}
	//		}
	//		return false;
	//	}
	//
	//	private void extractColors() {
	//		if (mNotificationTextColor != null) {
	//			return;
	//		}
	//		try {
	//			Notification ntf = new Notification();
	//			ntf.setLatestEventInfo(mContext, mCOLOR_SEARCH_RECURSE_TIP, "Utest", null);
	//			LinearLayout group = new LinearLayout(mContext);
	//			ViewGroup event = (ViewGroup) ntf.contentView.apply(mContext, group);
	//			recurseGroup(event);
	//			group.removeAllViews();
	//		} catch (Exception e) {
	//			if (mNotificationTextColor == null) {
	//				mNotificationTextColor = mContext.getResources().getColor(android.R.color.white);
	//			}
	//		}
	//	}
}
