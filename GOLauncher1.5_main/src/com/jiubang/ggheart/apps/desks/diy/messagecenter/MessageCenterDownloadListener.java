/*
 * 文 件 名:  MessageCenterDownloadListener1.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-8-23
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.apps.desks.diy.messagecenter;

import java.io.File;
import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.RemoteViews;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.appcenter.help.AppsManagementConstants;
import com.jiubang.ggheart.appgame.base.component.AppInstallActivity;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.appgame.base.data.AppGameNetLogControll;
import com.jiubang.ggheart.appgame.base.utils.ApkInstallUtils;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.IAidlDownloadListener;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-8-23]
 */
public class MessageCenterDownloadListener extends IAidlDownloadListener.Stub {

	public static final String DOWNLOAD_INFO_FORMAT_STRING = "%1$s %2$s"; // 下载信息格式化字符串
	public static final String DOWNLOAD_TICKER_FORMAT_STRING = "%1$s %2$s"; // 下载信息格式化字符串
	public static final String NOTIFY_TAG = "AppDownloadListener notify tag"; // 本应用发送通知的标识

	private static final long REFRESH_INTERVAL = 500;

	private NotificationManager mNotificationManager = null;
	private Notification mNotification = null;
	private RemoteViews mNotificationRemoteViews = null;
	private PendingIntent mPendingIntent = null;
	private String mConnectingText = null;
	private String mLoadingText = null;
	private String mFailText = null;
	private String mCancelText = null;
	private String mStartTickerText = null;
	private String mFailTickerText = null;
	private String mCompleteTickerText = null;
	private String mCancelTickerText = null;
	private String mStopTickerText = null;
	private int mDisplayPercent = 0;

	private long mPrevRefreshTime;
	private int mRestarCount;
	private boolean mShowNotification = true;
	private Context mContext = null;
	//	public static final String ACTION_MESSAGECENTER_DOWNLOAD = "MessageCenter_Action_Download";

	public static final String UPDATE_DOWNLOAD_TASKID_KEY = "UPDATE_DOWNLOAD_TASKID_KEY";
	public static final String UPDATE_DOWNLOAD_MSGID_KEY = "UPDATE_DOWNLOAD_MSGID_KEY";
	public static final String UPDATE_DOWNLOAD_PARAM_KEY = "UPDATE_DOWNLOAD_PARAM_KEY";
	public static final String UPDATE_DOWNLOAD_OBJECT_KEY = "UPDATE_DOWNLOAD_OBJECT_KEY";
	public static final String UPDATE_DOWNLOAD_LISTOBJECT_KEY = "UPDATE_DOWNLOAD_LISTOBJECT_KEY";

	public static final String UPDATE_DOWNLOAD_INFO = "UPDATE_DOWNLOAD_INFO";

	private long mStartConntion = 0;
	private long mStartDonwload = 0;
	private long mAlreadyDownloadSize = 0;
	private int mChildThreadCode = 0;

	public MessageCenterDownloadListener(Context context) {
		mContext = context;
		mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationRemoteViews = new RemoteViews(context.getPackageName(),
				R.layout.download_notification_layout);
//		extractColors();
	}

	@Override
	public void onStart(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub
		if (task == null) {
			return;
		}

		mChildThreadCode = Thread.currentThread().hashCode();
		mStartConntion = System.currentTimeMillis();

		sendBroadcastingToMessageCenter(task);
		if (mContext != null && mNotificationRemoteViews != null && mNotificationManager != null) {
			String downloadName = task.getDownloadName();
			if (downloadName != null) {
				mLoadingText = String.format(DOWNLOAD_INFO_FORMAT_STRING, downloadName,
						mContext.getString(R.string.themestore_downloading));
				mFailText = String.format(DOWNLOAD_INFO_FORMAT_STRING, downloadName,
						mContext.getString(R.string.apps_management_download_failed));
				mCancelText = String.format(DOWNLOAD_INFO_FORMAT_STRING, downloadName,
						mContext.getString(R.string.apps_management_download_canceled));
				mConnectingText = String.format(DOWNLOAD_TICKER_FORMAT_STRING, downloadName,
						mContext.getString(R.string.themestore_download_connecting));
				mStartTickerText = String.format(DOWNLOAD_TICKER_FORMAT_STRING, downloadName,
						mContext.getString(R.string.themestore_begin_download));
				mFailTickerText = String.format(DOWNLOAD_TICKER_FORMAT_STRING, downloadName,
						mContext.getString(R.string.themestore_download_fail));
				mCompleteTickerText = String.format(DOWNLOAD_TICKER_FORMAT_STRING, downloadName,
						mContext.getString(R.string.themestore_download_finish));
				mCancelTickerText = String.format(DOWNLOAD_TICKER_FORMAT_STRING, downloadName,
						mContext.getString(R.string.apps_management_download_canceled));
				mStopTickerText = String.format(DOWNLOAD_TICKER_FORMAT_STRING, downloadName,
						mContext.getString(R.string.themestore_download_stop));

				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString("msgid", String.valueOf(task.getId()));
				intent.putExtras(bundle);
				intent.setClass(mContext, MessageContentActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mPendingIntent = PendingIntent.getActivity(mContext, 0, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				mNotification = new Notification(R.drawable.notification_download_icon,
						mStartTickerText, System.currentTimeMillis());
				mNotification.contentIntent = mPendingIntent;
				mNotification.flags = Notification.FLAG_AUTO_CANCEL;
				mNotificationRemoteViews.setTextViewText(R.id.downloadTextView, mConnectingText);
//				mNotificationRemoteViews.setTextColor(R.id.downloadTextView, mNotificationTextColor);
//				mNotificationRemoteViews.setTextColor(R.id.downloadProgressTextView, mNotificationTextColor);
				mNotification.contentView = mNotificationRemoteViews;
				mNotificationManager.notify(NOTIFY_TAG, (int) task.getId(), mNotification);
			} else {
				mShowNotification = false;
			}
		}
	}

	@Override
	public void onWait(DownloadTask task) throws RemoteException {
		if (task != null && mNotificationManager != null && mContext != null) {
			Notification notification = new Notification(
					R.drawable.notification_download_icon,
					String.format(DOWNLOAD_TICKER_FORMAT_STRING, task.getDownloadName(),
							mContext.getString(R.string.themestore_download_add_to_download_queue)),
					System.currentTimeMillis());
			Intent intent = new Intent(mContext, AppsManagementActivity.class);
			intent.putExtra(
					AppsManagementConstants.APPS_MANAGEMENT_ENTRANCE_KEY,
					MainViewGroup.ACCESS_FOR_APPCENTER_THEME);
			intent.putExtra(AppsManagementConstants.APPS_MANAGEMENT_SHOW_FRONTCOVER, false);
//			Intent intent = new Intent();
//			intent.setClass(mContext, GoStore.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent,
					PendingIntent.FLAG_CANCEL_CURRENT);
			notification.setLatestEventInfo(mContext, task.getDownloadName(),
					mContext.getString(R.string.themestore_download_waiting), pendingIntent);
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			mNotificationManager.notify(NOTIFY_TAG, (int) task.getId(), notification);
		}
	}

	@Override
	public void onUpdate(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub
		if (task != null) {
			long currentTime = System.currentTimeMillis();

			if (mStartDonwload == 0) {
				mStartDonwload = currentTime;
				mAlreadyDownloadSize = task.getAlreadyDownloadSize();
			}

			if (currentTime - mPrevRefreshTime > REFRESH_INTERVAL) {
				mPrevRefreshTime = currentTime;
				sendBroadcastingToMessageCenter(task);
				if (mShowNotification && mNotificationRemoteViews != null
						&& mNotificationManager != null && mNotification != null) {
					mDisplayPercent = task.getAlreadyDownloadPercent();
					mNotification.contentIntent = mPendingIntent;
					mNotification.contentView = mNotificationRemoteViews;
					mNotification.flags = Notification.FLAG_ONGOING_EVENT;
					// 更新下载信息
					mNotificationRemoteViews.setTextViewText(R.id.downloadTextView, mLoadingText);
					// 更新进度条
					mNotificationRemoteViews.setProgressBar(R.id.downloadProgressBar, 100,
							mDisplayPercent, false);
					// 更新百分比
					mNotificationRemoteViews.setTextViewText(R.id.downloadProgressTextView,
							mDisplayPercent + "%");

					mNotificationManager.notify(NOTIFY_TAG, (int) task.getId(), mNotification);
				}
			}
		}
	}

	@Override
	public void onComplete(DownloadTask downloadTask) throws RemoteException {
		// TODO Auto-generated method stub
		if (downloadTask != null) {
			//如果要打开file路径，安装spk 。必须路径中有.apk完整地址
			String filePath = downloadTask.getSaveFilePath();
			File saveFile = new File(filePath);
			if (saveFile.exists() && saveFile.isFile()) {
				sendBroadcastingToMessageCenter(downloadTask);
			}

			if (mContext != null && mNotificationManager != null) {
				// 首先移除之前的通知
				mNotificationManager.cancel(NOTIFY_TAG, (int) downloadTask.getId());
				if (mShowNotification) {
					// 创建新的通知，该通知可清除，点击后自动消失，并且进行安装
					Intent intent = new Intent();
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					//					intent.setAction(android.content.Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(new File(filePath)),
							"application/vnd.android.package-archive");
					intent.setClass(mContext, AppInstallActivity.class);
					PendingIntent downCompletedIntent = PendingIntent.getActivity(mContext, 0,
							intent, 0);
					Notification finishNotification = new Notification(
							R.drawable.notification_download_icon, mCompleteTickerText,
							System.currentTimeMillis());
					finishNotification.setLatestEventInfo(mContext, downloadTask.getDownloadName(),
							mCompleteTickerText, downCompletedIntent);
					// 该通知可清除，点击后自动消失
					finishNotification.flags = Notification.FLAG_AUTO_CANCEL;
					mNotificationManager.notify(NOTIFY_TAG, (int) downloadTask.getId(),
							finishNotification);
				}
			}
			//			File file = new File(downloadTask.getSaveFilePath());
			//			openFile(file);
			ApkInstallUtils.installApk(downloadTask.getSaveFilePath());
		}
	}

	@Override
	public void onFail(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub
		if (mContext != null && task != null) {
			++mRestarCount;
			if (mRestarCount <= 5) {
				if (Machine.isNetworkOK(mContext)) {
					IDownloadService downloadController = GOLauncherApp.getApplication()
							.getDownloadController();
					downloadController.restartDownloadById(task.getId());
				}
			} else {
				handleDownloadFail(task);
			}
		}
	}

	@Override
	public void onReset(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStop(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub
		if (task != null) {
			mNotification.tickerText = mStopTickerText;
			mNotification.contentIntent = mPendingIntent;
			mNotificationRemoteViews.setCharSequence(R.id.downloadTextView, "setText",
					mStopTickerText);
			mNotification.contentView = mNotificationRemoteViews;
			mNotification.flags = Notification.FLAG_AUTO_CANCEL;
			mNotificationManager.notify(NOTIFY_TAG, (int) task.getId(), mNotification);

			sendBroadcastingToMessageCenter(task);
		}
	}

	@Override
	public void onCancel(DownloadTask downloadTask) throws RemoteException {
		// TODO Auto-generated method stub
		if (downloadTask != null) {
			String filePath = downloadTask.getSaveFilePath();
			File saveFile = new File(filePath);
			if (saveFile.exists()) {
				saveFile.delete();
			}
			sendBroadcastingToMessageCenter(downloadTask);
			if (mNotificationManager != null) {
				mNotificationManager.cancel(NOTIFY_TAG, (int) downloadTask.getId());
				if (mShowNotification && mNotification != null) {
					// 更新下载信息
					mNotification.tickerText = mCancelTickerText;
					mNotification.contentIntent = mPendingIntent;
					mNotificationRemoteViews.setTextViewText(R.id.downloadTextView, mCancelText);
					mNotification.contentView = mNotificationRemoteViews;
					mNotification.flags = Notification.FLAG_AUTO_CANCEL;
					mNotificationManager.notify(NOTIFY_TAG, (int) downloadTask.getId(),
							mNotification);
				}
			}
		}
	}

	@Override
	public void onDestroy(DownloadTask downloadTask) throws RemoteException {
		// TODO Auto-generated method stub
		if (downloadTask != null && downloadTask.getAlreadyDownloadPercent() < 100) {
			// 如果任务还没有下载完成
			if (mNotificationManager != null) {
				// 把通知移除
				mNotificationManager.cancel(NOTIFY_TAG, (int) downloadTask.getId());
			}
		}
	}

	@Override
	public void onConnectionSuccess(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub
		long time = System.currentTimeMillis() - mStartConntion;
		AppGameNetLogControll.getInstance().setConnectionTime(
				AppGameNetLogControll.DEFAULT_CURRENT_THREAD_CODE, time);
	}

	@Override
	public void onException(DownloadTask downloadTask) throws RemoteException {
		// TODO Auto-generated method stub
		ArrayList<Exception> list = downloadTask.getExceptionList();
		if (mStartDonwload != 0) {
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
		sendBroadcastingToMessageCenter(downloadTask);
		// 首先移除之前的通知
		if (mNotificationManager != null) {
			// 移除之前的通知
			mNotificationManager.cancel(NOTIFY_TAG, (int) downloadTask.getId());
			// 更新下载信息
			if (mShowNotification && mNotificationRemoteViews != null && mNotification != null) {
				mNotification.tickerText = mFailTickerText;
				mNotification.contentIntent = mPendingIntent;
				mNotificationRemoteViews.setTextViewText(R.id.downloadTextView, mFailText);
				mNotification.contentView = mNotificationRemoteViews;
				mNotification.flags = Notification.FLAG_AUTO_CANCEL;
				mNotificationManager.notify(NOTIFY_TAG, (int) downloadTask.getId(), mNotification);
			}
		}
	}

	/**
	 * 打开APK文件进行安装的方法
	 * @param file
	 */
	//	private void openFile(File file) {
	//		if (mContext != null && file != null && file.exists()) {
	//			Intent intent = new Intent();
	//			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	//			//TODO ADT-4131 可能就是因为少了Category ，无法预知影响这个是否修改好
	//			intent.addCategory(Intent.CATEGORY_DEFAULT);
	//			intent.setAction(android.content.Intent.ACTION_VIEW);
	//			intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
	//			mContext.startActivity(intent);
	//		}
	//	}

	/**
	 * 把下载的消息广播到消息中心
	 * @param msgId
	 * @param param
	 * @param object
	 * @param objects
	 */
	private void sendBroadcastingToMessageCenter(DownloadTask downloadTask) {
		Intent intent = new Intent(ICustomAction.ACTION_MESSAGECENTER_DOWNLOAD);
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
