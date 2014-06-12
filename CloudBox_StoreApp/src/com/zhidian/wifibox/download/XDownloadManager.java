package com.zhidian.wifibox.download;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import cn.trinea.android.common.util.PackageUtils;

import com.ta.TAApplication;
import com.ta.util.download.DownLoadCallback;
import com.ta.util.download.DownloadManager;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.controller.ModeManager;
import com.zhidian.wifibox.root.RootShell;
import com.zhidian.wifibox.util.DownloadUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.GetTaskStateUtil;
import com.zhidian.wifibox.util.InstallingValidator;
import com.zhidian.wifibox.util.PathConstant;
import com.zhidian.wifibox.util.Setting;
import com.zhidian.wifibox.view.dialog.SpaceDialog;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;

/**
 * 普通模式下的下载管理器
 * 
 * 任务的保存和读取由自己管理，对外只负责管理所有下载任务，添加、暂停、继续、删除下载任务
 * 
 * @author xiedezhi
 * 
 */
public class XDownloadManager implements IDownloadInterface {

	private static final int GET_ROOT = 100;
	/**
	 * 正在下载
	 */
	public static final String DOWNLOADING = "正在下载";
	/**
	 * 暂停下载
	 */
	public static final String PAUSE_DOWNLOADING = "暂停下载";
	/**
	 * 这个是DownloadService实例
	 */
	private Context mContext;

	/**
	 * 所有下载任务
	 */
	private Map<String, DownloadTask> mAllTask = new HashMap<String, DownloadTask>();
	/**
	 * 普通模式下的下载管理器，CDownloadManager依赖于它
	 */
	private DownloadManager mDownloadManager;
	/**
	 * 主线程handler
	 */
	private Handler mHandler = new Handler(Looper.getMainLooper());
	/**
	 * toast
	 */
	private Toast mToast = Toast.makeText(TAApplication.getApplication(),
			"正在下载", Toast.LENGTH_SHORT);
	/**
	 * 上次更新下载任务的时间
	 */
	private long mLastUpdateTime = System.currentTimeMillis();
	/**
	 * 下载回调
	 */
	private DownLoadCallback mCallback = new DownLoadCallback() {
		public void onStart() {
		}

		public void onAdd(String url, Boolean isInterrupt) {
			DownloadTask task = mAllTask.get(url);
			if (task != null) {
				task.state = DownloadTask.DOWNLOADING;
				saveAllTask();
				sendBroadcast(task);
			}
		}

		public void onLoading(String url, long totalSize, long currentSize,
				long speed) {
			if (currentSize <= 0) {
				return;
			}
			// 发广播
			DownloadTask task = mAllTask.get(url);
			if (task != null) {
				if (task.state == DownloadTask.PAUSING) {
					pauseTask(url, true);
					return;
				}
				int oldPercent = task.alreadyDownloadPercent;
				int newPercent = (int) (currentSize * 100.0 / totalSize + 0.5);
				long now = System.currentTimeMillis();
				// 下载进度的差超过2%时才保存任务和发送广播
				if (newPercent - oldPercent >= 1 || newPercent < oldPercent
						|| (now - mLastUpdateTime > 1000)) {
					mLastUpdateTime = now;
					task.alreadyDownloadPercent = newPercent;
					task.speed = speed;
					saveAllTask();
					sendBroadcast(task);
					refreshTaskState();
					if (downloadingCount == 1 && pauseCount == 0
							&& completeCount == 0) {
						showNotification(task.name,
								CDownloadManager.DOWNLOADING, newPercent,
								task.iconUrl);
					}
				}

			}
		}

		public void onSuccess(String url) {
			// 发广播
			DownloadTask task = mAllTask.get(url);
			if (task != null) {
				task.state = DownloadTask.COMPLETE;
				task.speed = 0;
				// 把完成的任务复制到普通模式的任务列表
				copyTaskToCommonMode(task);
				sendBroadcast(task);
				saveAllTask();
				File file = new File(DownloadUtil.getXApkFileFromUrl(url));
				if (!FileUtil.isSDCardAvaiable()) {
					DownloadUtil.chmod(file.getParentFile().getParentFile());
					DownloadUtil.chmod(file.getParentFile());
					DownloadUtil.chmod(file);
				}
				if (new Setting(mContext)
						.getBoolean(Setting.INSTALL_AFTER_DOWNLOAD)) {
					getRoot(task);

					// if (RootShell.isRootValid()) {// TODO
					// // 复制一个新的任务，不要改变真正任务的状态
					// mNotificationManager.cancel(START_ID);
					// silentInstall(task.copyObj());
					// } else {
					// mNotificationManager.cancel(START_ID);
					// Intent intent = new Intent();
					// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					// intent.setAction(android.content.Intent.ACTION_VIEW);
					// intent.setDataAndType(Uri.fromFile(file),
					// "application/vnd.android.package-archive");
					// mContext.startActivity(intent);
					// }
					showNotification();
				} else {
					showSuccessNotification(task);
				}
			}
		}

		public void onFailure(String url, String strMsg) {
			if (strMsg != null && strMsg.contains("ENOSPC")) {
				// 暂停所有任务
				for (DownloadTask task : mAllTask.values()) {
					pauseTask(task.url, false);
				}
				// 提示存储空间已满
				if (SpaceDialog.sDialog != null) {
					SpaceDialog.sDialog.show();
				}
				return;
			}
			DownloadTask task = mAllTask.get(url);
			if (task != null && task.state == DownloadTask.DOWNLOADING) {
				if (ModeManager.checkRapidly()) {
					// 下载失败后自动重试
					mDownloadManager.addHandler(url);
				} else {
					pauseTask(url, true);
				}
			}
		}

		public void onFinish(String url) {
		}

		public void onStop() {
		}
	};

	@SuppressLint("HandlerLeak")
	private Handler mHandler2 = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GET_ROOT:
				// TODO
				DownloadTask task = (DownloadTask) msg.obj;
				if (isHave) {// 有Root权限
					mNotificationManager.cancel(START_ID);
					silentInstall(task.copyObj());
				} else {

					File file = new File(
							DownloadUtil.getXApkFileFromUrl(task.url));

					mNotificationManager.cancel(START_ID);
					Intent intent = new Intent();
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setAction(android.content.Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(file),
							"application/vnd.android.package-archive");
					mContext.startActivity(intent);
				}
				break;

			default:
				break;
			}
		}

	};

	/***********************
	 * 开条线程去获取Root权限
	 **********************/
	private boolean isHave = false;

	private void getRoot(final DownloadTask task) {
		new Thread() {

			@Override
			public void run() {
				super.run();
				// TODO
				isHave = RootShell.isRootValid();
				Message msg = new Message();
				msg.what = GET_ROOT;
				msg.obj = task;
				mHandler2.sendMessage(msg);
			}

		}.start();
	}

	private NotificationManager mNotificationManager;

	private Notification notification;

	private int START_ID = 100223;
	private int APPS_STATE_ID = 100224;
	private int DOWNLOADING_SUCCESS_ID = 100225;
	private int notStartCount;
	private int downloadingCount;
	private int completeCount;
	private int pauseCount;
	private int completeInstalled;

	public XDownloadManager(Context context) {
		mContext = context;
		mNotificationManager = (NotificationManager) mContext
				.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		notification = new Notification();
		if (FileUtil.isSDCardAvaiable()) {
			mDownloadManager = DownloadManager
					.getDownloadManager(PathConstant.X_APK_ROOTPATH);
		} else {
			mDownloadManager = DownloadManager
					.getDownloadManager(PathConstant.X_APK_ROOTPATH_CACHE);
		}
		mDownloadManager.setDownLoadCallback(mCallback);
	}

	/**
	 * 把下载任务发送到Activity
	 */
	private void sendBroadcast(DownloadTask task) {
		Intent intent = new Intent(DOWNLOAD_BROADCAST_ACTION);
		intent.putExtra("task", task);
		mContext.sendBroadcast(intent);
	}

	@Override
	public void initSavedTask() {
		// 对于已经开始下载和下载完成的任务，要判断APK文件是否存在，如果不存在，则删除该任务
		byte[] b = FileUtil.getByteFromFile(PathConstant.X_TASK_PATH);
		if (b == null) {
			return;
		}
		try {
			JSONArray array = new JSONArray(new String(b));
			for (int i = 0; i < array.length(); i++) {
				JSONObject json = array.getJSONObject(i);
				if (json != null) {
					DownloadTask task = new DownloadTask();
					task.state = json.getInt("state");
					task.url = json.getString("url");
					task.alreadyDownloadPercent = json
							.getInt("alreadyDownloadPercent");
					task.iconUrl = json.getString("iconUrl");
					task.name = json.getString("name");
					task.size = json.getInt("size");
					task.packName = json.getString("packName");
					task.appId = json.getLong("appId");
					task.version = json.getString("version");
					task.speed = json.getLong("speed");
					mAllTask.put(task.url, task);
				}
			}

			// 更新任务记录器的任务
			DownloadTaskRecorder.getInstance().recordDownloadTaskList(mAllTask);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/********************
	 * 显示单个应用通知
	 ** 
	 * @param name
	 *            应用名称
	 * @param text
	 *            是否在下载
	 * @param percent
	 *            下载进度
	 ********************/

	private void showNotification(String name, String text, int percent,
			String iconurl) {

		// 设置通知的icon
		notification.icon = R.drawable.icon;
		// 设置通知在状态栏上显示的滚动信息
		notification.tickerText = "你有 " + mAllTask.size() + "个应用下载";
		// 设置通知的时间
		notification.when = System.currentTimeMillis();
		/*
		 * notification.contentView = new
		 * RemoteViews(mContext.getPackageName(),R
		 * .layout.notification_downloading);
		 * notification.contentView.setProgressBar
		 * (R.id.detail_app_down_progress, 100,5, false);
		 */
		notification.contentView = new RemoteViews(mContext.getPackageName(),
				R.layout.notification);
		notification.contentView.setTextViewText(R.id.down_tv, text);
		notification.contentView.setTextViewText(R.id.app_name, name);
		notification.contentView
				.setTextViewText(R.id.percent_tv, percent + "%");
		notification.contentView.setProgressBar(R.id.pb, 100, percent, false);
		Bitmap bm = AsyncImageManager.getInstance().loadImgFromSD(
				PathConstant.ICON_ROOT_PATH, iconurl.hashCode() + "", iconurl,
				true);
		if (bm == null) {
			bm = ((BitmapDrawable) mContext.getResources().getDrawable(
					R.drawable.icon)).getBitmap();
		}
		notification.contentView.setImageViewBitmap(R.id.myicon, bm);
		// 3.设置通知的显示参数
		Intent intent = new Intent(mContext, MainActivity.class);
		intent.putExtra(MainActivity.JUMP_TO_DOWNLOADMANAGER, true);
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);
		notification.contentIntent = pendingIntent;
		/*
		 * notification .setLatestEventInfo(mContext, "WIFI盒子", "你有 " +
		 * mAllTask.size() + "个应用下载", pendingIntent);
		 */
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		// 4.发送通知
		mNotificationManager.notify(START_ID, notification);

	}

	/**********************
	 * 显示应用下载成功通知
	 **********************/
	private void showSuccessNotification(DownloadTask task) {
		String text = task.name;
		String url = task.url;
		Setting setting = new Setting(mContext);
		Notification notification = new Notification();
		notification.icon = R.drawable.icon;
		notification.when = System.currentTimeMillis();
		notification.tickerText = "应用下载完成";
		notification.contentView = new RemoteViews(mContext.getPackageName(),
				R.layout.notification_apps);
		notification.contentView
				.setTextViewText(R.id.decription_state_tv, text);
		notification.contentView.setTextViewText(R.id.apps_state_tv,
				"下载完成，点击安装");
		Bitmap bm = AsyncImageManager.getInstance().loadImgFromSD(
				PathConstant.ICON_ROOT_PATH, task.iconUrl.hashCode() + "",
				task.iconUrl, true);
		if (bm == null) {
			bm = ((BitmapDrawable) mContext.getResources().getDrawable(
					R.drawable.icon)).getBitmap();
		}
		notification.contentView.setImageViewBitmap(R.id.myicon, bm);

		File file = new File(DownloadUtil.getXApkFileFromUrl(url));
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);
		notification.contentIntent = pendingIntent;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// 发送通知
		refreshTaskState();
		mNotificationManager.cancel(START_ID);
		mNotificationManager.notify(DOWNLOADING_SUCCESS_ID, notification);
		if (downloadingCount + pauseCount > 1) {
			showAppsNotification();
		} else if (downloadingCount == 1 && pauseCount == 0) {
			DownloadTask currentTask = GetTaskStateUtil
					.getDownloadingTask(mAllTask);
			showNotification(currentTask.name, DOWNLOADING,
					currentTask.alreadyDownloadPercent, currentTask.iconUrl);
			mNotificationManager.cancel(APPS_STATE_ID);
		} else if (downloadingCount == 0 && pauseCount == 1) {
			DownloadTask currentTask = GetTaskStateUtil.getPauseTask(mAllTask);
			showNotification(currentTask.name, PAUSE_DOWNLOADING,
					currentTask.alreadyDownloadPercent, currentTask.iconUrl);
			mNotificationManager.cancel(APPS_STATE_ID);
		}
	}

	/********************
	 * 显示多个应用通知
	 ********************/
	private void showAppsNotification() {
		Log.e("", "showAppsNotification()");
		mNotificationManager.cancel(START_ID);
		String textState = null;
		if (downloadingCount == 0 && pauseCount == 0 && completeCount == 0) {
			mNotificationManager.cancel(START_ID);
			mNotificationManager.cancel(APPS_STATE_ID);
			return;
		} else if (downloadingCount > 0 && pauseCount > 0 && completeCount > 0) {
			textState = downloadingCount + "个下载中，" + pauseCount + "个已暂停, "
					+ completeCount + "已下载未安装";
		} else if (completeCount > 0 && pauseCount == 0 && downloadingCount > 0) {
			textState = downloadingCount + "个下载中," + completeCount + "已下载未安装";
		} else if (completeCount == 0 && pauseCount == 0
				&& downloadingCount > 0) {
			textState = downloadingCount + "个下载中";
		} else if (completeCount > 0 && pauseCount > 0 && downloadingCount == 0) {
			textState = pauseCount + "个已暂停, " + completeCount + "个已下载未安装";
		} else if (completeCount == 0 && pauseCount > 0
				&& downloadingCount == 0) {
			textState = pauseCount + "个已暂停";
		} else if (completeCount == 0 && pauseCount > 0 && downloadingCount > 0) {
			textState = downloadingCount + "个下载中," + pauseCount + "个已暂停";
		} else {
			return;
		}
		Notification notification = new Notification();
		notification.icon = R.drawable.icon;
		notification.when = System.currentTimeMillis();
		notification.tickerText = "应用下载情况";

		notification.contentView = new RemoteViews(mContext.getPackageName(),
				R.layout.notification_apps);
		notification.contentView.setTextViewText(R.id.decription_state_tv,
				(completeCount + downloadingCount + pauseCount) + "个下载任务");
		notification.contentView.setTextViewText(R.id.apps_state_tv, textState);
		Intent intent = new Intent(mContext, MainActivity.class);
		intent.putExtra(MainActivity.JUMP_TO_DOWNLOADMANAGER, true);
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);
		notification.contentIntent = pendingIntent;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		// 4.发送通知
		mNotificationManager.notify(APPS_STATE_ID, notification);
		mNotificationManager.cancel(START_ID);

	}

	@Override
	public void checkDownloadingTask() {
		for (String url : mAllTask.keySet()) {
			DownloadTask task = mAllTask.get(url);
			if (task.state == DownloadTask.DOWNLOADING) {
				mDownloadManager.addHandler(url);
			}
		}
	}

	@Override
	public void addTask(String url, String iconUrl, String name, int size,
			String packName, long appId, String version) {
		DownloadTask task = new DownloadTask();
		task.state = DownloadTask.DOWNLOADING;
		task.url = url;
		task.alreadyDownloadPercent = 0;
		task.iconUrl = iconUrl;
		task.name = name;
		task.size = size;
		task.packName = packName;
		task.appId = appId;
		task.version = version;
		mAllTask.put(url, task);
		mDownloadManager.addHandler(url);
		saveAllTask();
		sendBroadcast(task);

		// Notification
		mHandler.post(new Runnable() {
			public void run() {
				mToast.show();
			}
		});
		showNotification();
	}

	@Override
	public void pauseTask(String url, boolean showNotifycation) {
		if (mAllTask.containsKey(url)) {
			DownloadTask task = mAllTask.get(url);
			task.state = DownloadTask.PAUSING;
			task.speed = 0;
			mDownloadManager.pauseHandler(url);
			saveAllTask();
			sendBroadcast(task);
			if (showNotifycation) {
				showNotification();
			}
		}
	}

	@Override
	public void continueTask(String url) {
		if (mAllTask.containsKey(url)) {
			DownloadTask task = mAllTask.get(url);
			task.state = DownloadTask.DOWNLOADING;
			if (mDownloadManager.hasHandler(url)) {
				mDownloadManager.continueHandler(url);
			} else {
				mDownloadManager.addHandler(url);
			}
			saveAllTask();
			sendBroadcast(task);
			// Notification
			mHandler.post(new Runnable() {
				public void run() {
					mToast.show();
				}
			});
			showNotification();
		}
	}

	@Override
	public void deleteTask(String url) {
		mDownloadManager.deleteHandler(url);
		// 要删除任务和文件
		if (mAllTask.containsKey(url)) {
			DownloadTask task = mAllTask.get(url);
			task.state = DownloadTask.NOT_START;
			task.alreadyDownloadPercent = 0;
			mAllTask.remove(url);
			File file = new File(DownloadUtil.getXApkFileFromUrl(task.url));
			file.delete();
			file = new File(DownloadUtil.getXTempApkFileFromUrl(task.url));
			file.delete();
			saveAllTask();
			sendBroadcast(task);
			showNotification();
		}
	}

	private void showNotification() {
		refreshTaskState();
		if (downloadingCount == 1 && pauseCount == 0 && completeCount == 0) {
			DownloadTask currentTask = GetTaskStateUtil
					.getDownloadingTask(mAllTask);
			showNotification(currentTask.name, DOWNLOADING,
					currentTask.alreadyDownloadPercent, currentTask.iconUrl);
			mNotificationManager.cancel(APPS_STATE_ID);

		} else if (downloadingCount == 0 && pauseCount == 1
				&& completeCount == 0) {
			DownloadTask currentTask = GetTaskStateUtil.getPauseTask(mAllTask);
			showNotification(currentTask.name, PAUSE_DOWNLOADING,
					currentTask.alreadyDownloadPercent, currentTask.iconUrl);
			mNotificationManager.cancel(APPS_STATE_ID);
		} else {

			showAppsNotification();

		}
	}

	@Override
	public void redownloadTask(final String url, final String iconUrl,
			final String name, final int size, final String packName,
			final long appId, final String version) {
		// 重新下载任务
		deleteTask(url);
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				addTask(url, iconUrl, name, size, packName, appId, version);
			}
		}, 1000);
	}

	/***************************
	 * 更新任务列表中的任务状态，以便判断推送，在推送前调用
	 ***************************/
	private void refreshTaskState() {
		notStartCount = 0;
		downloadingCount = 0;
		completeCount = 0;
		completeInstalled = 0;
		pauseCount = 0;

		Iterator<DownloadTask> iterator = mAllTask.values().iterator();
		while (iterator.hasNext()) {
			DownloadTask task = iterator.next();
			switch (task.state) {
			case DownloadTask.NOT_START:
				notStartCount++;
				break;
			case DownloadTask.DOWNLOADING:
				downloadingCount++;
				break;
			case DownloadTask.PAUSING:
				pauseCount++;
				break;
			case DownloadTask.COMPLETE:
				if (InstallingValidator.getInstance().isAppExist(mContext,
						task.packName)) {
					completeInstalled++;
				} else {
					completeCount++;
				}
				break;

			default:
				break;
			}
		}
	}

	private void saveAllTask() {
		// 更新任务记录器的任务
		DownloadTaskRecorder.getInstance().recordDownloadTaskList(mAllTask);
		// 先把旧数据删除
		File file = new File(PathConstant.X_TASK_PATH);
		file.delete();
		if (mAllTask.size() <= 0) {
			return;
		}
		JSONArray array = new JSONArray();
		Collection<DownloadTask> collection = mAllTask.values();
		for (DownloadTask task : collection) {
			try {
				JSONObject json = new JSONObject();
				json.put("state", task.state);
				json.put("url", task.url);
				json.put("alreadyDownloadPercent", task.alreadyDownloadPercent);
				json.put("iconUrl", task.iconUrl);
				json.put("name", task.name);
				json.put("size", task.size);
				json.put("packName", task.packName);
				json.put("appId", task.appId);
				json.put("version", task.version);
				json.put("speed", task.speed);
				array.put(json);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		FileUtil.saveByteToFile(array.toString().getBytes(),
				PathConstant.X_TASK_PATH);
	}

	@Override
	public Map<String, DownloadTask> getAllDownloadTasks() {
		return mAllTask;
	}

	@Override
	public void checkTask() {
		Map<String, DownloadTask> newMap = new HashMap<String, DownloadTask>();
		for (String url : mAllTask.keySet()) {
			DownloadTask task = mAllTask.get(url);
			if (task.state == DownloadTask.DOWNLOADING
					|| task.state == DownloadTask.PAUSING) {
				if (task.alreadyDownloadPercent > 0) {
					String fileName = DownloadUtil
							.getXTempApkFileFromUrl(task.url);
					File file = new File(fileName);
					if (!file.exists()) {
						mDownloadManager.deleteHandler(task.url);
						task.state = DownloadTask.NOT_START;
						task.alreadyDownloadPercent = 0;
						sendBroadcast(task);
						continue;
					}
				}
			} else if (task.state == DownloadTask.COMPLETE) {
				String fileName = DownloadUtil.getXApkFileFromUrl(task.url);
				File file = new File(fileName);
				if (!file.exists()
						&& !InstallingValidator.getInstance().isAppExist(
								mContext, task.packName)) {
					mDownloadManager.deleteHandler(task.url);
					task.state = DownloadTask.NOT_START;
					task.alreadyDownloadPercent = 0;
					sendBroadcast(task);
					continue;
				}
			} else if (task.state == DownloadTask.FAIL) {
				File file = new File(DownloadUtil.getXApkFileFromUrl(task.url));
				file.delete();
				file = new File(DownloadUtil.getXTempApkFileFromUrl(task.url));
				file.delete();
				mDownloadManager.deleteHandler(task.url);
				task.state = DownloadTask.NOT_START;
				task.alreadyDownloadPercent = 0;
				sendBroadcast(task);
				continue;
			}
			newMap.put(url, task);
		}
		mAllTask = newMap;
		saveAllTask();
	}

	@Override
	public void clearNotification() {
		if (mNotificationManager != null) {
			mNotificationManager.cancel(START_ID);
			mNotificationManager.cancel(APPS_STATE_ID);
			mNotificationManager.cancel(DOWNLOADING_SUCCESS_ID);
		}
	}

	/**
	 * 静默安装
	 */
	private void silentInstall(final DownloadTask task) {
		new Thread("silentInstall") {
			public void run() {
				String apkFileName = DownloadUtil.getXApkFileFromUrl(task.url);
				task.state = DownloadTask.INSTALLING;
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						sendBroadcast(task);
					}
				});
				final int status = PackageUtils.installSilent(mContext,
						apkFileName);
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						switch (status) {
						case PackageUtils.INSTALL_SUCCEEDED:
							// 安装成功
							Toast.makeText(mContext, task.name + "安装成功",
									Toast.LENGTH_SHORT).show();
							break;
						case PackageUtils.INSTALL_FAILED_ALREADY_EXISTS:
							Toast.makeText(mContext, task.name + "安装失败，应用已经存在",
									Toast.LENGTH_SHORT).show();
							task.state = DownloadTask.NOT_START;
							sendBroadcast(task);
							break;
						case PackageUtils.INSTALL_FAILED_INVALID_APK:
							Toast.makeText(mContext,
									task.name + "安装失败，安装包无效，请重新下载",
									Toast.LENGTH_SHORT).show();
							task.state = DownloadTask.NOT_START;
							sendBroadcast(task);
							break;
						case PackageUtils.INSTALL_FAILED_INSUFFICIENT_STORAGE:
							Toast.makeText(mContext,
									task.name + "安装失败，没有足够的存储空间",
									Toast.LENGTH_SHORT).show();
							task.state = DownloadTask.NOT_START;
							sendBroadcast(task);
							break;
						default:
							Toast.makeText(mContext, task.name + "安装失败，未知原因",
									Toast.LENGTH_SHORT).show();
							task.state = DownloadTask.NOT_START;
							sendBroadcast(task);
							break;
						}
					}
				});
			};
		}.start();
	}

	@Override
	public void onAppAction(String packName) {
		showNotification();
	}

	/**
	 * 把完成的任务复制一份到普通模式
	 */
	private void copyTaskToCommonMode(DownloadTask task) {
		byte[] b = FileUtil.getByteFromFile(PathConstant.C_TASK_PATH);
		if (b == null) {
			b = new JSONArray().toString().getBytes();
		}
		// 复制APK
		FileUtil.copyFile(DownloadUtil.getXApkFileFromUrl(task.url),
				DownloadUtil.getCApkFileFromUrl(task.url));
		try {
			JSONArray array = new JSONArray(new String(b));
			JSONObject json = new JSONObject();
			json.put("state", task.state);
			json.put("url", task.url);
			json.put("alreadyDownloadPercent", task.alreadyDownloadPercent);
			json.put("iconUrl", task.iconUrl);
			json.put("name", task.name);
			json.put("size", task.size);
			json.put("packName", task.packName);
			json.put("appId", task.appId);
			json.put("version", task.version);
			json.put("speed", task.speed);
			array.put(json);
			FileUtil.saveByteToFile(array.toString().getBytes(),
					PathConstant.C_TASK_PATH);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
