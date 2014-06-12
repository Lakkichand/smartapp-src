/*
 * 文 件 名:  WifiTimerTask.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-12-27
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.utils;

import java.util.ArrayList;
import java.util.Map;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.RemoteException;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.base.downloadmanager.AppsDownloadActivity;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.ServiceCallbackDownload;
import com.jiubang.ggheart.appgame.download.ServiceCallbackDownload.ServiceCallbackRunnable;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-12-27]
 */
public class WifiTimerTask extends TimerTask {

	private Context mContext = null;

	public final static int NOTIFICATIONID = 0x01010101;

	public final static String TAG = "WifiTimerTaskTags";

	private ServiceCallbackRunnable mRunnable = null;

	/**
	 * 3分钟监控
	 */
	public static final long WATCH_TIME_STEP = 3 * 60 * 1000;
	/**
	 * 2天的间隔时间
	 */
	private static final long TIME_STEP = 1000 * 60 * 60 * 24 * 2;
	
	private static final String SHAREDPREFERENCES_NAME = "notification_sharedpreferences";

	private static final String NOTIFICATION_TIME = "notification_time";
	
	public WifiTimerTask(Context context) {
		mContext = context;
		mRunnable = new ServiceCallbackRunnable() {
			@Override
			public void run() {
				try {
					if (mDownloadController == null) {
						return;
					}
					Map<Long, DownloadTask> map = mDownloadController
							.getDownloadConcurrentHashMap();
					ArrayList<DownloadTask> downloadTaskList = new ArrayList<DownloadTask>();
					for (DownloadTask dt : map.values()) {
						downloadTaskList.add(dt);
					}
					showNotification(downloadTaskList);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		};
	}

	/** {@inheritDoc} */

	@Override
	public void run() {
		// 判断两天之内是否已经弹出过Notification提示
		SharedPreferences pref = mContext.getSharedPreferences(SHAREDPREFERENCES_NAME,
				Context.MODE_PRIVATE);
		long timeStep = pref.getLong(NOTIFICATION_TIME, 0);
		if (System.currentTimeMillis() - timeStep >= TIME_STEP) {
			Editor editor = pref.edit();
			editor.putLong(NOTIFICATION_TIME, System.currentTimeMillis()).commit();
			ServiceCallbackDownload.callbackDownload(mContext, mRunnable);
		}
	}

	/**
	 * <br>功能简述:进入应用中心之后，假如有“已暂停”的任务，就在通知栏提示用户
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param list
	 */
	public void showNotification(ArrayList<DownloadTask> list) {
		int count = 0;
		for (int i = 0; i < list.size(); i++) {
			DownloadTask task = list.get(i);
			if (task.getState() == DownloadTask.STATE_STOP) {
				count++;
			}
		}
		if (count == 0) {
			return;
		}
		NotificationManager notificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClass(mContext, AppsDownloadActivity.class);
		intent.putExtra(AppsDownloadActivity.QUITSTYLE, AppsDownloadActivity.QUIT_TO_APPCENTER);
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		String tickerText = count
				+ mContext.getString(R.string.app_center_uncomplete_download_notification);
		Notification notification = new Notification(
				R.drawable.notification_download_uncomplete_icon, tickerText,
				System.currentTimeMillis());
		notification.setLatestEventInfo(mContext, tickerText,
				mContext.getString(R.string.app_center_uncomplete_download_text), pendingIntent);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(TAG, NOTIFICATIONID, notification);
	}
}
