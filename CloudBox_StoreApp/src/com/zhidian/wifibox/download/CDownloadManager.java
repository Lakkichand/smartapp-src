package com.zhidian.wifibox.download;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

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
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.Toast;
import cn.trinea.android.common.util.PackageUtils;

import com.ta.TAApplication;
import com.ta.util.download.DownLoadCallback;
import com.ta.util.download.DownloadManager;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.stat.StatService;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.controller.ModeManager;
import com.zhidian.wifibox.data.DownloadSpeed;
import com.zhidian.wifibox.db.dao.AppDownloadSpeedDao;
import com.zhidian.wifibox.root.RootShell;
import com.zhidian.wifibox.util.BoxIdManager;
import com.zhidian.wifibox.util.CheckNetwork;
import com.zhidian.wifibox.util.DownloadUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.GetTaskStateUtil;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.InstallingValidator;
import com.zhidian.wifibox.util.PathConstant;
import com.zhidian.wifibox.util.Setting;
import com.zhidian.wifibox.util.ThreadPoolExecutorUtil;
import com.zhidian.wifibox.view.dialog.OfflineDownloadDialog;
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
public class CDownloadManager implements IDownloadInterface {

	private static final int GET_ROOT = 100;// 获取Root权限
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
	private Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GET_ROOT:
				DownloadTask task = (DownloadTask) msg.obj;
				if (msg.arg1 == 1) {// 有Root权限
					mNotificationManager.cancel(START_ID);
					silentInstall(task.copyObj());
				} else {

					File file = new File(
							DownloadUtil.getCApkFileFromUrl(task.url));

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
	/**
	 * toast
	 */
	private Toast mToast = Toast.makeText(TAApplication.getApplication(),
			"玩命下载ing...", Toast.LENGTH_SHORT);

	private Toast mNoNetworkToast = Toast.makeText(
			TAApplication.getApplication(), "网络连接失败", Toast.LENGTH_SHORT);
	/**
	 * 每个任务都有一个重试的Toast提示
	 */
	private Map<DownloadTask, Toast> mToasts = new HashMap<DownloadTask, Toast>();

	private Setting mSetting = new Setting(TAApplication.getApplication());

	/**
	 * 下载回调
	 */
	private DownLoadCallback mCallback = new DownLoadCallback() {
		public void onStart() {
		}

		public void onAdd(String url, Boolean isInterrupt) {
			mSetting.putInt(url + Setting.APP_DOWNLOAD_FAIL_COUNT, 0);
			DownloadTask task = mAllTask.get(url);
			if (task != null) {
				task.state = DownloadTask.WAITING;
				saveAllTask();
				sendBroadcast(task);
			}
		}

		public void onLoading(String url, long totalSize, long currentSize,
				long speed, long time, long aveSpeed) {
			mSetting.putInt(url + Setting.APP_DOWNLOAD_FAIL_COUNT, 0);
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
				task.state = DownloadTask.DOWNLOADING;
				int newPercent = (int) (currentSize * 100.0 / totalSize + 0.5);
				// 下载进度的差超过2%时才保存任务和发送广播
				task.alreadyDownloadPercent = newPercent;
				task.speed = aveSpeed;
				saveAllTask();
				sendBroadcast(task);
				refreshTaskState();
				if (downloadingCount == 1 && pauseCount == 0
						&& completeCount == 0) {
					showNotification(task.name, CDownloadManager.DOWNLOADING,
							newPercent, task.iconUrl);
				}
				if (!TextUtils.isEmpty(task.src)) {// 只在超速模式下保存
					saveDownloadSpeedData(task, speed, currentSize, totalSize,
							time);
				}
			}
		}

		public void onSuccess(String url) {
			mSetting.remove(url + Setting.APP_DOWNLOAD_FAIL_COUNT);
			// 发广播
			DownloadTask task = mAllTask.get(url);
			if (task != null) {
				task.state = DownloadTask.COMPLETE;
				task.speed = 0;
				sendBroadcast(task);
				saveAllTask();
				File file = new File(DownloadUtil.getCApkFileFromUrl(url));
				if (!FileUtil.isSDCardAvaiable()) {
					DownloadUtil.chmod(file.getParentFile().getParentFile());
					DownloadUtil.chmod(file.getParentFile());
					DownloadUtil.chmod(file);
				}
				if (new Setting(mContext)
						.getBoolean(Setting.INSTALL_AFTER_DOWNLOAD)) {
					getRoot(task);
					showNotification();
				} else {
					showSuccessNotification(task);
				}
				Setting setting = new Setting(TAApplication.getApplication());
				int count = setting.getInt(Setting.DOWNLOAD_SUCCESS_COUNT);
				count = count + 1;
				setting.putInt(Setting.DOWNLOAD_SUCCESS_COUNT, count);
				if (count >= 10) {
					XGPushManager.deleteTag(TAApplication.getApplication(),
							"有效用户x20");
					XGPushManager.setTag(TAApplication.getApplication(),
							"有效用户x10");
				} else if (count >= 20) {
					XGPushManager.deleteTag(TAApplication.getApplication(),
							"有效用户x10");
					XGPushManager.setTag(TAApplication.getApplication(),
							"有效用户x20");
				}
			}
		}

		public void onFailure(final String url, String strMsg) {
			int count = mSetting.getInt(url + Setting.APP_DOWNLOAD_FAIL_COUNT);
			count = count + 1;
			mSetting.putInt(url + Setting.APP_DOWNLOAD_FAIL_COUNT, count);
			mtaDownloadFailStatistics(mAllTask.get(url), strMsg);
			if (count <= 3) {
				DownloadTask task = mAllTask.get(url);
				if (task != null
						&& (task.state == DownloadTask.DOWNLOADING || task.state == DownloadTask.WAITING)) {
					if (mToasts.containsKey(task)) {
						mToasts.get(task).show();
					} else {
						Toast toast = Toast.makeText(mContext, task.name
								+ "下载失败，正重新下载", Toast.LENGTH_SHORT);
						toast.show();
						mToasts.put(task, toast);
					}
					postDelayed(new Runnable() {

						@Override
						public void run() {
							// 下载失败后自动重试
							mDownloadManager.addHandler(url);
						}
					}, 2000);
				}
				return;
			} else if (strMsg != null
					&& (strMsg.contains("ENOSPC") || strMsg.contains("space"))) {
				// 暂停所有任务
				for (DownloadTask task : mAllTask.values()) {
					pauseTask(task.url, false);
				}
				// 提示存储空间已满
				if (SpaceDialog.sDialog != null) {
					SpaceDialog.sDialog.show();
				}
				return;
			} else if (strMsg != null && strMsg.contains("找不到文件")) {
				// 文件不存在
				Toast.makeText(mContext, "下载失败，文件不存在", Toast.LENGTH_SHORT)
						.show();
				deleteTask(url);
				return;
			} else if (strMsg != null
					&& strMsg.toLowerCase().contains("filenotfoundexception")) {
				// 文件不存在
				Toast.makeText(mContext, "下载失败，文件不存在", Toast.LENGTH_SHORT)
						.show();
				deleteTask(url);
				return;
			} else if (strMsg != null && strMsg.contains("服务器内部错误")) {
				// 文件不存在
				Toast.makeText(mContext, "下载失败，文件不存在", Toast.LENGTH_SHORT)
						.show();
				deleteTask(url);
				return;
			} else {
				Toast.makeText(mContext, "网络异常，任务暂停", Toast.LENGTH_SHORT)
						.show();
				pauseTask(url, true);
				return;
			}
		}

		public void onFinish(String url) {
			mSetting.remove(url + Setting.APP_DOWNLOAD_FAIL_COUNT);
		}

		public void onStop() {
		}
	};

	private void getRoot(final DownloadTask task) {
		new Thread() {

			@Override
			public void run() {
				super.run();
				boolean isHave = RootShell.isRootValid();
				Message msg = new Message();
				msg.what = GET_ROOT;
				msg.obj = task;
				msg.arg1 = isHave ? 1 : 0;
				mHandler.sendMessage(msg);
			}
		}.start();
	}

	private NotificationManager mNotificationManager;

	private Notification notification;
	private String currentIcon = "";

	private int START_ID = 100223;
	private int APPS_STATE_ID = 100224;
	private int DOWNLOADING_SUCCESS_ID = 100225;
	private int notStartCount;
	private int downloadingCount;
	private int completeCount;
	private int completeInstalled;
	private int pauseCount;

	public CDownloadManager(Context context) {
		mContext = context;
		mNotificationManager = (NotificationManager) mContext
				.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		notification = new Notification();
		if (FileUtil.isSDCardAvaiable()) {
			mDownloadManager = DownloadManager
					.getDownloadManager(PathConstant.C_APK_ROOTPATH);
		} else {
			mDownloadManager = DownloadManager
					.getDownloadManager(PathConstant.C_APK_ROOTPATH_CACHE);
		}
		mDownloadManager.setDownLoadCallback(mCallback);
	}

	/*****************************
	 * 保存下载速度统计数据到数据库
	 * 
	 * @param task
	 * @param speed
	 * @param totalSize
	 * @param currentSize
	 *****************************/
	@SuppressLint("SimpleDateFormat")
	private void saveDownloadSpeedData(final DownloadTask task,
			final long speed, final long currentSize, final long totalSize,
			final long time) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				SimpleDateFormat formatter = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				Date curDate = new Date(time);// 获取当前时间
				String nowTime = formatter.format(curDate);
				AppDownloadSpeedDao dao = new AppDownloadSpeedDao(
						TAApplication.getApplication());
				DownloadSpeed bean = new DownloadSpeed();
				bean.unique = task.unique;
				bean.uuId = InfoUtil.getUUID(TAApplication.getApplication());
				bean.boxNum = BoxIdManager.getInstance().getBoxId();
				bean.appId = String.valueOf(task.appId);
				bean.time = nowTime;
				bean.speed = String.valueOf(speed);
				bean.downloadSource = "0"; // 一定是门店下载
				bean.downloadModel = "0";// 0表示急速模式
				bean.version = task.version;
				bean.packageName = task.packName;
				bean.appName = task.name;
				bean.networkWay = CheckNetwork.getAPNType(TAApplication
						.getApplication());// 联网方式
				bean.currentSize = String.valueOf(currentSize / 1024);// 把单位转化为kb
				bean.totalSize = String.valueOf(totalSize / 1024);// 把单位转化为kb
				dao.saveData(bean);
			}
		};
		ThreadPoolExecutorUtil.getInstance().execute(runnable);
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
		byte[] b = FileUtil.getByteFromFile(PathConstant.C_TASK_PATH);
		if (b == null) {
			return;
		}
		try {
			JSONArray array = new JSONArray(new String(b));
			for (int i = 0; i < array.length(); i++) {
				JSONObject json = array.getJSONObject(i);
				if (json != null) {
					DownloadTask task = new DownloadTask();
					task.unique = json.optString("unique",
							"" + System.currentTimeMillis());
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
					task.src = json.optString("src", "");
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
		notification.icon = R.drawable.icon_notify;
		// 设置通知在状态栏上显示的滚动信息
		// notification.tickerText = "你有 " + mAllTask.size() + "个应用下载";
		// 设置通知的时间
		// notification.when = System.currentTimeMillis();

		notification.contentView = new RemoteViews(mContext.getPackageName(),
				R.layout.notification);

		if (!currentIcon.equals(iconurl)) {
			Bitmap bm = AsyncImageManager.getInstance().loadImgFromSD(
					PathConstant.ICON_ROOT_PATH, iconurl.hashCode() + "",
					iconurl, true);
			currentIcon = iconurl;
			if (bm == null) {
				bm = ((BitmapDrawable) mContext.getResources().getDrawable(
						R.drawable.icon)).getBitmap();
				currentIcon = "";
			}
			notification.contentView.setImageViewBitmap(R.id.myicon, bm);
		}
		notification.contentView.setTextViewText(R.id.down_tv, text);
		notification.contentView.setTextViewText(R.id.app_name, name);

		notification.contentView
				.setTextViewText(R.id.percent_tv, percent + "%");
		notification.contentView.setProgressBar(R.id.pb, 100, percent, false);

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
		Notification notification = new Notification();
		notification.icon = R.drawable.icon_notify;
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
		File file = new File(DownloadUtil.getCApkFileFromUrl(url));
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
		if (downloadingCount + pauseCount + completeCount > 1) {
			showAppsNotification();
		} else if (downloadingCount == 1 && pauseCount == 0
				&& completeCount == 0) {
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
		}

	}

	/********************
	 * 显示多个应用通知
	 ********************/
	private void showAppsNotification() {
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
				&& downloadingCount > 1) {
			textState = downloadingCount + "个下载中";
		} else if (completeCount > 0 && pauseCount > 0 && downloadingCount == 0) {
			textState = pauseCount + "个已暂停, " + completeCount + "个已下载未安装";
		} else if (completeCount == 0 && pauseCount > 1
				&& downloadingCount == 0) {
			textState = pauseCount + "个已暂停";
		} else if (completeCount == 0 && pauseCount > 0 && downloadingCount > 0) {
			textState = downloadingCount + "个下载中," + pauseCount + "个已暂停";
		} else if (completeCount > 0 && pauseCount == 0
				&& downloadingCount == 0) {
			textState = completeCount + "个已下载未安装";
		} else {
			return;
		}
		Notification notification = new Notification();
		notification.icon = R.drawable.icon_notify;
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
			case DownloadTask.WAITING:
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

	@Override
	public void checkDownloadingTask() {
		for (String url : mAllTask.keySet()) {
			DownloadTask task = mAllTask.get(url);
			if (task.state == DownloadTask.DOWNLOADING) {
				mDownloadManager.addHandler(url);
			}
		}
		for (String url : mAllTask.keySet()) {
			DownloadTask task = mAllTask.get(url);
			if (task.state == DownloadTask.WAITING) {
				mDownloadManager.addHandler(url);
			}
		}
	}

	@Override
	public void addTask(String url, String iconUrl, String name, int size,
			String packName, long appId, String version, String src) {
		DownloadTask task = new DownloadTask();
		task.unique = System.currentTimeMillis() + "";
		task.state = DownloadTask.WAITING;
		task.url = url;
		task.alreadyDownloadPercent = 0;
		task.iconUrl = iconUrl;
		task.name = name;
		task.size = size;
		task.packName = packName;
		task.appId = appId;
		task.version = version;
		task.src = src;
		mAllTask.put(url, task);
		saveAllTask();
		sendBroadcast(task);
		if (!InfoUtil.hasNetWorkConnection(mContext)) {
			deleteTask(url);
			mNoNetworkToast.show();
			return;
		}
		// 如果当前连接盒子，同时盒子不能连外网，弹框提示
		if (ModeManager.checkRapidly()
				&& !ModeManager.getInstance().isExtranet()
				&& TextUtils.isEmpty(src)) {
			if (MainActivity.sIsOpen) {
				// 弹框
				OfflineDownloadDialog sDialog = new OfflineDownloadDialog(
						TAApplication.getApplication(), task);
				sDialog.getWindow().setType(
						WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				sDialog.setCancelable(true);
				sDialog.show();
				return;
			}
		}
		mDownloadManager.addHandler(url);
		// Notification
		mHandler.post(new Runnable() {
			public void run() {
				mToast.show();
			}
		});
		showNotification();
		// 设置标签
		if (!TextUtils.isEmpty(src)) {
			Setting setting = new Setting(TAApplication.getApplication());
			int count = setting.getInt(Setting.X_DOWNLOAD_COUNT);
			count = count + 1;
			setting.putInt(Setting.X_DOWNLOAD_COUNT, count);
			if (count >= 3) {
				XGPushManager.setTag(TAApplication.getApplication(), "超版用户x3");
			}
		}
	}

	@Override
	public void pauseTask(String url, boolean showNotifycation) {
		if (mAllTask.containsKey(url)) {
			DownloadTask task = mAllTask.get(url);
			task.speed = 0;
			task.state = DownloadTask.PAUSING;
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
			task.state = DownloadTask.WAITING;
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
		mSetting.remove(url + Setting.APP_DOWNLOAD_FAIL_COUNT);
		mDownloadManager.deleteHandler(url);
		// 要删除任务和文件
		if (mAllTask.containsKey(url)) {
			DownloadTask task = mAllTask.get(url);
			task.state = DownloadTask.NOT_START;
			task.alreadyDownloadPercent = 0;
			mAllTask.remove(url);
			File file = new File(DownloadUtil.getCApkFileFromUrl(task.url));
			file.delete();
			file = new File(DownloadUtil.getCTempApkFileFromUrl(task.url));
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
			final long appId, final String version, final String src) {
		// 重新下载任务
		deleteTask(url);
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				addTask(url, iconUrl, name, size, packName, appId, version, src);
			}
		}, 1000);
	}

	private void saveAllTask() {
		// 更新任务记录器的任务
		DownloadTaskRecorder.getInstance().recordDownloadTaskList(mAllTask);
		// 先把旧数据删除
		File file = new File(PathConstant.C_TASK_PATH);
		file.delete();
		if (mAllTask.size() <= 0) {
			return;
		}
		JSONArray array = new JSONArray();
		Collection<DownloadTask> collection = mAllTask.values();
		for (DownloadTask task : collection) {
			try {
				JSONObject json = new JSONObject();
				json.put("unique", task.unique);
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
				json.put("src", task.src);
				array.put(json);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		FileUtil.saveByteToFile(array.toString().getBytes(),
				PathConstant.C_TASK_PATH);
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
							.getCTempApkFileFromUrl(task.url);
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
				String fileName = DownloadUtil.getCApkFileFromUrl(task.url);
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
				File file = new File(DownloadUtil.getCApkFileFromUrl(task.url));
				file.delete();
				file = new File(DownloadUtil.getCTempApkFileFromUrl(task.url));
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
				String apkFileName = DownloadUtil.getCApkFileFromUrl(task.url);
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
							Toast.makeText(mContext,
									"yeah！" + task.name + "安装成功~~",
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
	 * 腾讯云统计下载失败
	 */
	private void mtaDownloadFailStatistics(DownloadTask task, String strMsg) {
		if (task == null) {
			return;
		}
		Properties prop = new Properties();
		prop.setProperty("name", task.name);
		prop.setProperty("packName", task.packName);
		prop.setProperty("url", task.url);
		prop.setProperty("appId", task.appId + "");
		prop.setProperty("version", task.version);
		prop.setProperty("size", task.size + "");
		prop.setProperty("reason", strMsg + "");
		prop.setProperty("uuId",
				InfoUtil.getUUID(TAApplication.getApplication()));
		prop.setProperty("boxNum", BoxIdManager.getInstance().getBoxId());
		prop.setProperty("operators",
				InfoUtil.getSimOperatorName(TAApplication.getApplication()));
		if (!InfoUtil.hasNetWorkConnection(TAApplication.getApplication())) {
			prop.setProperty("networkWay", "NO NETWORK");
		} else if (ModeManager.checkRapidly()) {
			if (ModeManager.getInstance().isExtranet()) {
				prop.setProperty("networkWay", "MIBAO WIFI ONLINE");
			} else {
				prop.setProperty("networkWay", "MIBAO WIFI OFFLINE");
			}
		} else {
			prop.setProperty("networkWay",
					CheckNetwork.getAPNType(TAApplication.getApplication()));
		}
		int count = mSetting.getInt(task.url + Setting.APP_DOWNLOAD_FAIL_COUNT);
		if (count <= 3) {
			prop.setProperty("results", "第" + count + "失败，2秒后继续下载任务");
		} else if (strMsg != null
				&& (strMsg.contains("ENOSPC") || strMsg.contains("space"))) {
			prop.setProperty("results", "空间不足，暂停任务");
		} else if (strMsg != null && strMsg.contains("找不到文件")) {
			prop.setProperty("results", "文件不存在，删除任务");
		} else if (strMsg != null
				&& strMsg.toLowerCase().contains("filenotfoundexception")) {
			prop.setProperty("results", "文件不存在，删除任务");
		} else if (strMsg != null && strMsg.contains("服务器内部错误")) {
			prop.setProperty("results", "服务器内部错误，删除任务");
		} else {
			prop.setProperty("results", "网络异常，任务暂停");
		}
		StatService.trackCustomKVEvent(TAApplication.getApplication(),
				"downloadfailstatistics", prop);
	}

}
