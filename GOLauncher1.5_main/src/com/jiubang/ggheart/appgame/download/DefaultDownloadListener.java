/*
 * 文 件 名:  DefaultDownloadListener1.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-8-23
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.download;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.RemoteException;
import android.widget.RemoteViews;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.jiubang.ggheart.appgame.base.component.AppInstallActivity;
import com.jiubang.ggheart.appgame.base.downloadmanager.AppsDownloadActivity;
import com.jiubang.ggheart.appgame.gostore.base.component.AppsThemeDetailActivity;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-8-23]
 */
public class DefaultDownloadListener extends IAidlDownloadListener.Stub {

	public static final String DOWNLOAD_INFOR_FORMAT_STRING = "%1$s--%2$s(%3$s)"; // 下载信息格式化字符串
	public static final String DOWNLOAD_TICKER_FORMAT_STRING = "%1$s--%2$s"; // 下载信息格式化字符串
	public static final String NOTIFY_TAG = "GO LauncherEX notify tag"; // 本应用发送通知的标识
	private NotificationManager mNotificationManager = null;
	private Notification mNotification = null;
	//	private PendingIntent mStopPendingIntent = null;
	//	private PendingIntent mDeletePendingIntent = null;
	//	private PendingIntent mRestartPendingIntent = null;
	private PendingIntent mPendingIntent = null;
	private int mPercentCount = 0;
	private final int mPercentStep = 1;
	private int mDisplayPercent = 0;
	private Context mContext = null;
	private String mConnectingText = null;
	private String mLoadingText = null;
	private String mStopText = null;
	private String mFailText = null;
	private String mStartTickerText = null;
	private String mStopTickerText = null;
	private String mFailTickerText = null;
	private String mCompleteTickerText = null;
	private RemoteViews mNotificationRemoteViews = null;
	private static long sData = 0;
	private int mRestarCount = 0;

	private synchronized static long getData() {
		sData++;
		return sData;
	}

	public DefaultDownloadListener(Context context) {
		if (context != null) {
			mContext = context;
			mNotificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationRemoteViews = new RemoteViews(context.getPackageName(),
					R.layout.download_notification_layout);
			//			extractColors();
		}
	}

	@Override
	public void onStart(DownloadTask downloadTask) throws RemoteException {
		// TODO Auto-generated method stub
		if (mContext != null && downloadTask != null && mNotificationRemoteViews != null
				&& mNotificationManager != null) {
			// 初始化下载文本信息
			String downloadName = downloadTask.getDownloadName();
			mLoadingText = String.format(DOWNLOAD_INFOR_FORMAT_STRING, downloadName,
					mContext.getString(R.string.themestore_downloading),
					mContext.getString(R.string.themestore_download_management));
			//			mStopText = String.format(DOWNLOAD_INFOR_FORMAT_STRING, downloadName,
			//					mContext.getString(R.string.themestore_download_stop),
			//					mContext.getString(R.string.themestore_download_management));
			mStopText = downloadName;
			mFailText = String.format(DOWNLOAD_INFOR_FORMAT_STRING, downloadName,
					mContext.getString(R.string.themestore_download_fail),
					mContext.getString(R.string.themestore_download_management));
			mConnectingText = String.format(DOWNLOAD_TICKER_FORMAT_STRING, downloadName,
					mContext.getString(R.string.themestore_download_connecting));
			mStartTickerText = String.format(DOWNLOAD_TICKER_FORMAT_STRING, downloadName,
					mContext.getString(R.string.themestore_begin_download));
			mStopTickerText = String.format(DOWNLOAD_TICKER_FORMAT_STRING, downloadName,
					mContext.getString(R.string.themestore_download_stop));
			mFailTickerText = String.format(DOWNLOAD_TICKER_FORMAT_STRING, downloadName,
					mContext.getString(R.string.themestore_download_fail));
			if (downloadTask.getIsApkFile()) {
				mCompleteTickerText = String.format(DOWNLOAD_TICKER_FORMAT_STRING, downloadName,
						mContext.getString(R.string.themestore_download_finish));
			} else {
				mCompleteTickerText = String.format(DOWNLOAD_TICKER_FORMAT_STRING, downloadName,
						mContext.getString(R.string.apps_management_download_completed));
			}

			// 下载ID
			long taskId = downloadTask.getId();
			//			// 初始化停止PendingIntent
			//			Intent stopIntent = new Intent();
			//			stopIntent.setAction(DownloadBrocastReceiver.ACTION_DOWNLOAD_STOP);
			//			stopIntent.setData(Uri.parse("download://" + getData()));
			//			stopIntent.putExtra(DownloadManager.DOWNLOAD_TASK_ID_KEY, taskId);
			//			mStopPendingIntent = PendingIntent.getBroadcast(mContext, 0, stopIntent,
			//					PendingIntent.FLAG_CANCEL_CURRENT);
			//			// 初始化删除PendingIntent
			//			Intent deleteIntent = new Intent();
			//			deleteIntent.setAction(DownloadBrocastReceiver.ACTION_DOWNLOAD_DELETE);
			//			deleteIntent.setData(Uri.parse("download://" + getData()));
			//			deleteIntent.putExtra(DownloadManager.DOWNLOAD_TASK_ID_KEY, taskId);
			//			mDeletePendingIntent = PendingIntent.getBroadcast(mContext, 0, deleteIntent,
			//					PendingIntent.FLAG_CANCEL_CURRENT);
			//			// 初始化重新开始PendingIntent
			//			Intent restartIntent = new Intent();
			//			restartIntent.setAction(DownloadBrocastReceiver.ACTION_DOWNLOAD_RESTART);
			//			restartIntent.setData(Uri.parse("download://" + getData()));
			//			restartIntent.putExtra(DownloadManager.DOWNLOAD_TASK_ID_KEY, taskId);
			//			mRestartPendingIntent = PendingIntent.getBroadcast(mContext, 0, restartIntent,
			//					PendingIntent.FLAG_CANCEL_CURRENT);
			// 创建新的通知，跳回GO Store
			Intent intent = new Intent();
			intent.setClass(mContext, AppsDownloadActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mPendingIntent = PendingIntent.getActivity(mContext, 0, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			mNotification = new Notification(R.drawable.notification_download_icon,
					mStartTickerText, System.currentTimeMillis());
			mNotification.contentIntent = mPendingIntent;
			mNotification.flags = Notification.FLAG_AUTO_CANCEL;
			//			mNotification.deleteIntent = mDeletePendingIntent;
			// 下载信息
			mNotificationRemoteViews.setCharSequence(R.id.downloadTextView, "setText",
					mConnectingText);
			// 下载百分比
			mNotificationRemoteViews.setCharSequence(R.id.downloadProgressTextView, "setText",
					mDisplayPercent + "%");
			//			mNotificationRemoteViews.setTextColor(R.id.downloadTextView, mNotificationTextColor);
			//			mNotificationRemoteViews.setTextColor(R.id.downloadProgressTextView, mNotificationTextColor);
			mNotification.contentView = mNotificationRemoteViews;
			mNotificationManager.notify(NOTIFY_TAG, (int) downloadTask.getId(), mNotification);

			// 发送广播到gostore
			Intent gostoreIntent = new Intent(
					ICustomAction.ACTION_UPDATE_DOWNLOAD_PERCENT_FOR_ITEM_DETAIL);
			gostoreIntent.putExtra(AppsThemeDetailActivity.PERSENT_KEY, mDisplayPercent);
			gostoreIntent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_PACKAGE_NAME,
					downloadTask.getDownloadApkPkgName());
			gostoreIntent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_ID,
					(int) downloadTask.getId());
			gostoreIntent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_NAME,
					downloadTask.getDownloadName());
			mContext.sendBroadcast(gostoreIntent);
		}
	}

	@Override
	public void onWait(DownloadTask downloadTask) throws RemoteException {
		// TODO Auto-generated method stub
		if (downloadTask != null && mNotificationManager != null && mContext != null) {
			Notification notification = new Notification(
					R.drawable.notification_download_icon,
					String.format(DOWNLOAD_TICKER_FORMAT_STRING, downloadTask.getDownloadName(),
							mContext.getString(R.string.themestore_download_add_to_download_queue)),
					System.currentTimeMillis());
			Intent intent = new Intent();
			intent.setClass(mContext, /*GoStore*/AppsDownloadActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			notification.setLatestEventInfo(mContext, downloadTask.getDownloadName(),
					mContext.getString(R.string.themestore_download_waiting), pendingIntent);
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			mNotificationManager.notify(NOTIFY_TAG, (int) downloadTask.getId(), notification);
		}
	}

	@Override
	public void onUpdate(DownloadTask downloadTask) throws RemoteException {
		// TODO Auto-generated method stub
		if (mContext != null && downloadTask != null && mNotificationRemoteViews != null
				&& mNotificationManager != null && mNotification != null) {
			if (downloadTask.getAlreadyDownloadPercent() >= mPercentCount && mPercentCount <= 100) {
				mPercentCount += mPercentStep;
				mDisplayPercent = downloadTask.getAlreadyDownloadPercent();
				//				mNotification.contentIntent = mStopPendingIntent
				mNotification.flags = Notification.FLAG_ONGOING_EVENT;
				mNotification.contentIntent = mPendingIntent;
				// 更新下载信息
				mNotificationRemoteViews.setCharSequence(R.id.downloadTextView, "setText",
						mLoadingText);
				// 更新进度条
				mNotificationRemoteViews.setInt(R.id.downloadProgressBar, "setProgress",
						mDisplayPercent);
				// 更新百分比
				mNotificationRemoteViews.setCharSequence(R.id.downloadProgressTextView, "setText",
						mDisplayPercent + "%");
				mNotification.contentView = mNotificationRemoteViews;
				mNotificationManager.notify(NOTIFY_TAG, (int) downloadTask.getId(), mNotification);

				// 发送广播到gostore
				Intent intent = new Intent(
						ICustomAction.ACTION_UPDATE_DOWNLOAD_PERCENT_FOR_ITEM_DETAIL);
				intent.putExtra(AppsThemeDetailActivity.PERSENT_KEY, mDisplayPercent);
				intent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_PACKAGE_NAME,
						downloadTask.getDownloadApkPkgName());
				intent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_ID, (int) downloadTask.getId());
				intent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_NAME,
						downloadTask.getDownloadName());
				mContext.sendBroadcast(intent);
			}
		}
	}

	@Override
	public void onComplete(DownloadTask downloadTask) throws RemoteException {
		// TODO Auto-generated method stub
		if (mContext != null && mNotificationManager != null && downloadTask != null) {

			// 首先移除之前的通知
			mNotificationManager.cancel(NOTIFY_TAG, (int) downloadTask.getId());

			if (downloadTask.getIsApkFile()) // 判断下载的是否APK
			{
				// 创建新的通知，该通知可清除，点击后自动消失，并且进行安装
				Intent intent = new Intent();
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				//				intent.setAction(android.content.Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(new File(downloadTask.getSaveFilePath())),
						"application/vnd.android.package-archive");
				intent.setClass(mContext, AppInstallActivity.class);
				PendingIntent downCompletedIntent = PendingIntent.getActivity(mContext, 0, intent,
						0);
				Notification finishNotification = new Notification(
						R.drawable.notification_download_complete_icon, mCompleteTickerText,
						System.currentTimeMillis());
				finishNotification.setLatestEventInfo(mContext, downloadTask.getDownloadName(),
						mCompleteTickerText, downCompletedIntent);
				// 该通知可清除，点击后自动消失
				finishNotification.flags = Notification.FLAG_AUTO_CANCEL;
				mNotificationManager.notify(NOTIFY_TAG, (int) downloadTask.getId(),
						finishNotification);
				// 统计更新完成
				AppManagementStatisticsUtil.getInstance().saveUpdataComplete(mContext,
						downloadTask.getDownloadApkPkgName(), String.valueOf(downloadTask.getId()),
						1);
				// 统计:应用推荐--下载/更新完成
				AppRecommendedStatisticsUtil.getInstance().saveDownloadComplete(mContext,
						downloadTask.getDownloadApkPkgName(), String.valueOf(downloadTask.getId()),
						1);

				// 打开文件进行安装
				openFile(downloadTask.getSaveFilePath());
			} else {
				Intent intent = new Intent();
				PendingIntent downCompletedIntent = PendingIntent.getActivity(mContext, 0, intent,
						0);
				Notification finishNotification = new Notification(
						R.drawable.notification_download_complete_icon, mCompleteTickerText,
						System.currentTimeMillis());
				finishNotification.setLatestEventInfo(mContext, downloadTask.getDownloadName(),
						mCompleteTickerText, downCompletedIntent);
				// 该通知可清除，点击后自动消失
				finishNotification.flags = Notification.FLAG_AUTO_CANCEL;
				mNotificationManager.notify(NOTIFY_TAG, (int) downloadTask.getId(),
						finishNotification);

			}
			// 发送广播到gostore
			Intent gostoreIntent = new Intent(ICustomAction.ACTION_DOWNLOAD_COMPLETE);
			gostoreIntent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_ID,
					(int) downloadTask.getId());
			gostoreIntent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_NAME,
					downloadTask.getDownloadName());
			mContext.sendBroadcast(gostoreIntent);
		}
	}

	@Override
	public void onFail(DownloadTask downloadTask) throws RemoteException {
		// TODO Auto-generated method stub
		++mRestarCount;
		if (mRestarCount <= 5) {
			// 自动重连5次
			if (Machine.isNetworkOK(mContext)) {
				IDownloadService downloadController = GOLauncherApp.getApplication()
						.getDownloadController();
				if (downloadController != null) {
					downloadController.restartDownloadById(downloadTask.getId());
				}
			}
		} else {
			// 首先移除之前的通知
			if (mContext != null && downloadTask != null && mNotificationManager != null
					&& mNotificationRemoteViews != null && mNotification != null) {
				// 移除之前的通知
				mNotificationManager.cancel(NOTIFY_TAG, (int) downloadTask.getId());
				// 更新下载信息
				mNotification.tickerText = mFailTickerText;
				// mNotification.contentIntent = mRestartPendingIntent;
				mNotification.contentIntent = mPendingIntent;
				mNotificationRemoteViews.setCharSequence(R.id.downloadTextView, "setText",
						mFailText);
				mNotification.contentView = mNotificationRemoteViews;
				mNotification.flags = Notification.FLAG_AUTO_CANCEL;
				mNotificationManager.notify(NOTIFY_TAG, (int) downloadTask.getId(), mNotification);
			}
			if (downloadTask != null) {
				// 发送广播到gostore
				Intent gostoreIntent = new Intent(ICustomAction.ACTION_UPDATE_DOWNLOAD_FAILED);
				gostoreIntent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_ID,
						(int) downloadTask.getId());
				gostoreIntent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_NAME,
						downloadTask.getDownloadName());
				mContext.sendBroadcast(gostoreIntent);
			}
		}
	}

	@Override
	public void onReset(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub
		mPercentCount = 0;
		mDisplayPercent = 0;
	}

	@Override
	public void onStop(DownloadTask downloadTask) throws RemoteException {
		// TODO Auto-generated method stub
		if (mContext != null && mNotificationManager != null && downloadTask != null
				&& mNotificationRemoteViews != null && mNotification != null) {
			if (downloadTask.getState() != DownloadTask.STATE_DELETE) {
				// 如果不是删除的停止
				// 设置下载信息
				// 移除之前的通知
				mNotificationManager.cancel(NOTIFY_TAG, (int) downloadTask.getId());
				// 更新下载信息
				mNotification.tickerText = mStopTickerText;
				//				mNotification.contentIntent = mRestartPendingIntent;
				mNotification.contentIntent = mPendingIntent;
				mNotificationRemoteViews.setCharSequence(R.id.downloadTextView, "setText",
						mStopText);
				// --------------------
				mNotificationRemoteViews.setTextViewText(R.id.downloadProgressTextView,
						mContext.getString(R.string.download_state_paused));
				mNotification.contentView = mNotificationRemoteViews;
				mNotification.flags = Notification.FLAG_AUTO_CANCEL;
				mNotificationManager.notify(NOTIFY_TAG, (int) downloadTask.getId(), mNotification);

				// 发送广播到gostore
				Intent gostoreIntent = new Intent(ICustomAction.ACTION_DOWNLOAD_STOP);
				gostoreIntent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_ID,
						(int) downloadTask.getId());
				gostoreIntent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_NAME,
						downloadTask.getDownloadName());
				mContext.sendBroadcast(gostoreIntent);
			}
		}

		if (downloadTask != null) {
			// 发送广播到gostore
			Intent gostoreIntent = new Intent(ICustomAction.ACTION_UPDATE_DOWNLOAD_STOP);
			gostoreIntent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_ID,
					(int) downloadTask.getId());
			gostoreIntent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_NAME,
					downloadTask.getDownloadName());
			mContext.sendBroadcast(gostoreIntent);
		}
	}

	@Override
	public void onCancel(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDestroy(DownloadTask downloadTask) throws RemoteException {
		// TODO Auto-generated method stub
		if (mNotificationManager != null && downloadTask != null
				&& downloadTask.getAlreadyDownloadPercent() < 100) {
			// 如果任务还没有下载完成，就把通知移除
			mNotificationManager.cancel(NOTIFY_TAG, (int) downloadTask.getId());
			mNotificationManager = null;

			// 发送广播到gostore
			Intent gostoreIntent = new Intent(ICustomAction.ACTION_DOWNLOAD_DESTROY);
			gostoreIntent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_ID,
					(int) downloadTask.getId());
			gostoreIntent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_NAME,
					downloadTask.getDownloadName());
			mContext.sendBroadcast(gostoreIntent);

			downloadTask = null;
		}
		mNotification = null;
		//		mStopPendingIntent = null;
		//		mDeletePendingIntent = null;
		//		mRestartPendingIntent = null;
		mContext = null;
		mConnectingText = null;
		mLoadingText = null;
		mStopText = null;
		mFailText = null;
		mStartTickerText = null;
		mStopTickerText = null;
		mFailTickerText = null;
		mCompleteTickerText = null;
		mNotificationRemoteViews = null;
	}

	@Override
	public void onConnectionSuccess(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onException(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub

	}

	/**
	 * 打开APK文件进行安装的方法
	 * 
	 * @param file
	 */
	private void openFile(String filepath) {
		File file = new File(filepath);
		if (!file.exists()) {
			return;
		}
		String[] token = file.getName().split("\\.");
		String pf = token[token.length - 1];
		if (!pf.equals("apk")) {
			return;
		}
		// DefaultDownloadListener是通过ClassName在DownloadService的进程实例化的对象
		// 所以这里可以直接取得下载控制接口
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
