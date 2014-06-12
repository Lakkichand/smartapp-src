/*
 * 文 件 名:  ApplicationDownloadListener1.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-8-23
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.apps.appmanagement.download;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import android.widget.RemoteViews;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.jiubang.ggheart.appgame.base.component.AppInstallActivity;
import com.jiubang.ggheart.appgame.base.downloadmanager.AppsDownloadActivity;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.IAidlDownloadListener;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.apps.appmanagement.component.AppsManageView;
import com.jiubang.ggheart.apps.appmanagement.help.AppsManagementConstants;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.GOLauncherApp;
/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-8-23]
 */
public class ApplicationDownloadListener extends IAidlDownloadListener.Stub {

	private static final String TAG = "ApplicationDownloadListener1";

	public static final String DOWNLOAD_INFO_FORMAT_STRING = "%1$s %2$s"; // 下载信息格式化字符串
	public static final String DOWNLOAD_TICKER_FORMAT_STRING = "%1$s %2$s"; // 下载信息格式化字符串
	public static final String NOTIFY_TAG = "ApplicationDownloadListener1 notify tag"; // 本应用发送通知的标识

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
	private int mDisplayPercent = 0;

	private long mPrevRefreshTime;
	private int mRestarCount;
	private boolean mShowNotification = true;

	private Context mContext = null;

//	public static final String ACTION_APP_DOWNLOAD = "appsmanagement_download_info";
	public static final String ACTION_APP_DOWNLOAD_STATE = "appsmanagement_download_state";
	public static final String ACTION_APP_DOWNLOAD_TASK = "appsmanagement_download_task";

	public ApplicationDownloadListener(Context context) {
		mContext = context;
		mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationRemoteViews = new RemoteViews(context.getPackageName(),
				R.layout.download_notification_layout);
	}

	@Override
	public void onStart(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub
		//		Log.i(TAG, "--------------onStartDownload");
		if (task == null) { // add by zhaojunjie
			return;
		}

		//		int taskId = Long.valueOf(task.getId()).intValue();

		//		GoLauncher.sendHandler(this, IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
		//				IDiyMsgIds.APPS_MANAGEMENT_START_DOWNLOAD, taskId, null, null);
		//		GoLauncher.sendHandler(this, IDiyFrameIds.APPS_MANAGEMENT_RECOMMENDED_APP_FRAME,
		//				IDiyMsgIds.APPS_MANAGEMENT_START_DOWNLOAD, taskId, null, null);

		sendBroadcastingToAppCenter(IDiyMsgIds.APPS_MANAGEMENT_START_DOWNLOAD, task);

		if (mContext != null // && downloadTask != null //del by zhaojunjie
								// (findbugs报这是多余的判断，提前判断)
				&& mNotificationRemoteViews != null && mNotificationManager != null) {
			String downloadName = task.getDownloadName();
			if (downloadName != null) {
				mLoadingText = String.format(
						ApplicationDownloadListener.DOWNLOAD_INFO_FORMAT_STRING, downloadName,
						mContext.getString(R.string.themestore_downloading));
				mFailText = String.format(ApplicationDownloadListener.DOWNLOAD_INFO_FORMAT_STRING,
						downloadName, mContext.getString(R.string.apps_management_download_failed));
				mCancelText = String.format(
						ApplicationDownloadListener.DOWNLOAD_INFO_FORMAT_STRING, downloadName,
						mContext.getString(R.string.apps_management_download_canceled));

				mConnectingText = String.format(
						ApplicationDownloadListener.DOWNLOAD_TICKER_FORMAT_STRING, downloadName,
						mContext.getString(R.string.themestore_download_connecting));
				mStartTickerText = String.format(
						ApplicationDownloadListener.DOWNLOAD_TICKER_FORMAT_STRING, downloadName,
						mContext.getString(R.string.themestore_begin_download));
				mFailTickerText = String.format(
						ApplicationDownloadListener.DOWNLOAD_TICKER_FORMAT_STRING, downloadName,
						mContext.getString(R.string.themestore_download_fail));
				mCompleteTickerText = String.format(
						ApplicationDownloadListener.DOWNLOAD_TICKER_FORMAT_STRING, downloadName,
						mContext.getString(R.string.themestore_download_finish));
				mCancelTickerText = String.format(
						ApplicationDownloadListener.DOWNLOAD_TICKER_FORMAT_STRING, downloadName,
						mContext.getString(R.string.apps_management_download_canceled));

				Intent intent = new Intent();
				intent.setClass(mContext, AppsDownloadActivity.class);
				int appsTabType = AppsManageView.APPS_UPDATE_VIEW_ID;
				if (task.getmDownloadType() == DownloadTask.DOWNLOAD_TYPE_FOR_APPS_RECOMMEND) {
					appsTabType = AppsManageView.APPS_RECOMMEND_VIEW_ID;
				}
				intent.putExtra(AppsManagementConstants.APPS_MANAGEMENT_VIEW_KEY, appsTabType);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mPendingIntent = PendingIntent.getActivity(mContext, 0, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				mNotification = new Notification(R.drawable.notification_download_icon,
						mStartTickerText, System.currentTimeMillis());
				mNotification.contentIntent = mPendingIntent;
				mNotification.flags = Notification.FLAG_AUTO_CANCEL;
				mNotificationRemoteViews.setTextViewText(R.id.downloadTextView, mConnectingText);
				mNotification.contentView = mNotificationRemoteViews;
				mNotificationManager.notify(ApplicationDownloadListener.NOTIFY_TAG,
						(int) task.getId(), mNotification);
			} else {
				mShowNotification = false;
			}
		}
	}

	@Override
	public void onWait(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub
		Log.i(TAG, "--------------onWaitDownload");
		//		int taskId = Long.valueOf(task.getId()).intValue();
		//		GoLauncher.sendHandler(this, IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
		//				IDiyMsgIds.APPS_MANAGEMENT_WAIT_FOR_DOWNLOAD, taskId, null, null);
		//		GoLauncher.sendHandler(this, IDiyFrameIds.APPS_MANAGEMENT_RECOMMENDED_APP_FRAME,
		//				IDiyMsgIds.APPS_MANAGEMENT_WAIT_FOR_DOWNLOAD, taskId, null, null);

		sendBroadcastingToAppCenter(IDiyMsgIds.APPS_MANAGEMENT_WAIT_FOR_DOWNLOAD, task);
	}

	@Override
	public void onUpdate(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub
		if (task != null) {
			long currentTime = System.currentTimeMillis();
			if (currentTime - mPrevRefreshTime > REFRESH_INTERVAL) {
				mPrevRefreshTime = currentTime;

				//				int taskId = Long.valueOf(task.getId()).intValue();
				//				long downSize = task.getAlreadyDownloadSize();
				//				int percent = task.getAlreadyDownloadPercent();
				//				List<Object> list = new ArrayList<Object>(2);
				//				list.add(Long.valueOf(downSize));
				//				list.add(Integer.valueOf(percent));
				//
				//				GoLauncher.sendHandler(this, IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
				//						IDiyMsgIds.APPS_MANAGEMENT_DOWNLOADING, taskId, null, list);
				//				GoLauncher.sendHandler(this, IDiyFrameIds.APPS_MANAGEMENT_RECOMMENDED_APP_FRAME,
				//						IDiyMsgIds.APPS_MANAGEMENT_DOWNLOADING, taskId, null, list);

				sendBroadcastingToAppCenter(IDiyMsgIds.APPS_MANAGEMENT_DOWNLOADING, task);

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
	public void onComplete(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub
		if (task != null) {
			// 统计:应用更新--更新完成
			AppManagementStatisticsUtil.getInstance().saveUpdataComplete(mContext,
					task.getDownloadApkPkgName(), String.valueOf(task.getId()), 1);
			// 统计:应用推荐--下载/更新完成
			AppRecommendedStatisticsUtil.getInstance().saveDownloadComplete(mContext,
					task.getDownloadApkPkgName(), String.valueOf(task.getId()), 1);

			sendBroadcastingToAppCenter(IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_COMPLETED, task);
			//			int taskId = Long.valueOf(task.getId()).intValue();
			String filePath = task.getSaveFilePath();
			//			File saveFile = new File(filePath);
			//			if (saveFile.exists() && saveFile.isFile()) {
			//				if (filePath.endsWith(".tmp")) {
			//					filePath = filePath.replace(".tmp", ".apk");
			//				}
			//				Log.i(TAG, "------------------filePath: " + filePath);
			//				if (saveFile.renameTo(new File(filePath))) {
			////					long downSize = task.getAlreadyDownloadSize();
			////					int percent = task.getAlreadyDownloadPercent();
			////					List<Object> list = new ArrayList<Object>(2);
			////					list.add(Long.valueOf(downSize));
			////					list.add(Integer.valueOf(percent));
			////					GoLauncher.sendHandler(this, IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
			////							IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_COMPLETED, taskId, filePath, list);
			////					GoLauncher.sendHandler(this,
			////							IDiyFrameIds.APPS_MANAGEMENT_RECOMMENDED_APP_FRAME,
			////							IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_COMPLETED, taskId, filePath, list);
			//					
			//					sendBroadcastingToAppCenter(IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_COMPLETED,task);
			//				}
			//			}

			if (mContext != null && mNotificationManager != null) {
				// 首先移除之前的通知
				mNotificationManager.cancel(NOTIFY_TAG, (int) task.getId());

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
					finishNotification.setLatestEventInfo(mContext, task.getDownloadName(),
							mCompleteTickerText, downCompletedIntent);
					// 该通知可清除，点击后自动消失
					finishNotification.flags = Notification.FLAG_AUTO_CANCEL;
					mNotificationManager.notify(NOTIFY_TAG, (int) task.getId(), finishNotification);
				}
			}
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
					if (downloadController != null) {
						downloadController.restartDownloadById(task.getId());
					}
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

	}

	@Override
	public void onCancel(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub
		if (task != null) {
			//			int taskId = Long.valueOf(task.getId()).intValue();
			String filePath = task.getSaveFilePath();
			File saveFile = new File(filePath);
			if (saveFile.exists()) {
				saveFile.delete();
			}
			//			GoLauncher.sendHandler(this, IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
			//					IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_CANCELED, taskId, null, null);
			//			GoLauncher.sendHandler(this, IDiyFrameIds.APPS_MANAGEMENT_RECOMMENDED_APP_FRAME,
			//					IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_CANCELED, taskId, null, null);

			sendBroadcastingToAppCenter(IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_CANCELED, task);

			if (mNotificationManager != null) {
				mNotificationManager.cancel(NOTIFY_TAG, (int) task.getId());
				if (mShowNotification && mNotification != null) {
					// 更新下载信息
					mNotification.tickerText = mCancelTickerText;
					mNotification.contentIntent = mPendingIntent;
					mNotificationRemoteViews.setTextViewText(R.id.downloadTextView, mCancelText);
					mNotification.contentView = mNotificationRemoteViews;
					mNotification.flags = Notification.FLAG_AUTO_CANCEL;
					mNotificationManager.notify(NOTIFY_TAG, (int) task.getId(), mNotification);
				}
			}
		}
	}

	@Override
	public void onDestroy(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub
		if (task != null && task.getAlreadyDownloadPercent() < 100) {
			// 如果任务还没有下载完成
			if (mNotificationManager != null) {
				// 把通知移除
				mNotificationManager.cancel(NOTIFY_TAG, (int) task.getId());
			}
		}
	}

	@Override
	public void onConnectionSuccess(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onException(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub

	}

	private void handleDownloadFail(DownloadTask downloadTask) {
		int taskId = Long.valueOf(downloadTask.getId()).intValue();
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
		//		GoLauncher.sendHandler(this, IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
		//				IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_FAILED, taskId, null, null);
		//		GoLauncher.sendHandler(this, IDiyFrameIds.APPS_MANAGEMENT_RECOMMENDED_APP_FRAME,
		//				IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_FAILED, taskId, null, null);

		sendBroadcastingToAppCenter(IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_FAILED, downloadTask);

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
	 * 把下载的消息广播到应用管理
	 * 
	 * @param msgId
	 * @param param
	 * @param object
	 * @param objects
	 */
	private void sendBroadcastingToAppCenter(int downloadState, DownloadTask task) {
		Intent intent = new Intent(ICustomAction.ACTION_APP_DOWNLOAD_FOR_APPS);

		intent.putExtra(ACTION_APP_DOWNLOAD_STATE, downloadState);
		intent.putExtra(ACTION_APP_DOWNLOAD_TASK, task);

		mContext.sendBroadcast(intent);
	}
}
