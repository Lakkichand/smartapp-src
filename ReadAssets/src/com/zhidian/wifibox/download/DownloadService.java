package com.zhidian.wifibox.download;

import java.util.Map;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;

/**
 * 下载服务，要与Activity交互
 * 
 * @author xiedezhi
 * 
 */
public class DownloadService extends Service {

	public static boolean sIsRunning = false;
	/**
	 * 当前模式的下载管理器
	 */
	private IDownloadInterface mDownloadManager;

	/**
	 * 下载命令广播接收器
	 */
	private BroadcastReceiver mDownloadRequestListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 处理下载请求
			String command = intent.getStringExtra("command");
			String url = intent.getStringExtra("url");
			String boxNum = intent.getStringExtra("boxNum");
			String code = intent.getStringExtra("code");
			int versionCode = intent.getIntExtra("versionCode", -1);
			int rank = intent.getIntExtra("rank", -1);
			String config = intent.getStringExtra("config");
			if (command.equals(IDownloadInterface.REQUEST_COMMAND_ADD)) {
				// 添加
				mDownloadManager.addTask(url, boxNum, code, versionCode, rank,
						config);
			} else if (command.equals(IDownloadInterface.REQUEST_COMMAND_PAUSE)) {
				// 暂停
				mDownloadManager.pauseTask(url, true);
			} else if (command
					.equals(IDownloadInterface.REQUEST_COMMAND_CONTINUE)) {
				// 继续
				mDownloadManager.continueTask(url);
			} else if (command
					.equals(IDownloadInterface.REQUEST_COMMAND_DELETE)) {
				// 删除
				mDownloadManager.deleteTask(url);
			} else if (command
					.equals(IDownloadInterface.REQUEST_COMMAND_REDOWNLOAD)) {
				// 重新下载
				mDownloadManager.redownloadTask(url, boxNum, code, versionCode,
						rank);
			} else if (command
					.equals(IDownloadInterface.REQUEST_COMMAND_CHECKTASK)) {
				// 检查下载任务
				mDownloadManager.checkTask();
			}
		}
	};
	/**
	 * 应用安装卸载监听器
	 */
	private final BroadcastReceiver mAppInstallListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
					|| Intent.ACTION_PACKAGE_REMOVED.equals(action)
					|| Intent.ACTION_PACKAGE_ADDED.equals(action)) {
				String packageName = intent.getData().getSchemeSpecificPart();
				mDownloadManager.onAppAction(packageName);
			}
		}
	};
	/**
	 * 暂停所有下载任务
	 */
	private final BroadcastReceiver mPauseAllListener = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			Map<String, DownloadTask> taskMap = mDownloadManager
					.getAllDownloadTasks();
			for (DownloadTask task : taskMap.values()) {
				if (task != null
						&& (task.state == DownloadTask.DOWNLOADING || task.state == DownloadTask.WAITING)) {
					mDownloadManager.pauseTask(task.url, false);
				}
			}
			Intent xi = new Intent(
					IDownloadInterface.PAUSE_ALL_TASK_ACTION_COMPLETE);
			sendBroadcast(xi);
		};
	};

	/**
	 * 网络状态监听器
	 */
	private final BroadcastReceiver mNetWorkListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 暂停所有下载任务
			Map<String, DownloadTask> taskMap = mDownloadManager
					.getAllDownloadTasks();
			for (DownloadTask task : taskMap.values()) {
				if (task != null
						&& (task.state == DownloadTask.DOWNLOADING || task.state == DownloadTask.WAITING)) {
					mDownloadManager.pauseTask(task.url, false);
				}
			}
			Intent xi = new Intent(
					IDownloadInterface.PAUSE_ALL_TASK_ACTION_COMPLETE);
			sendBroadcast(xi);

		}
	};

	/**
	 * 注册广播接收器
	 */
	private void registerReceiver() {
		// 注册下载广播事件
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
		registerReceiver(mDownloadRequestListener, intentFilter);
		// 注册应用安装卸载事件
		intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentFilter.addDataScheme("package");
		registerReceiver(mAppInstallListener, intentFilter);
		// 注册暂停全部任务的事件
		intentFilter = new IntentFilter();
		intentFilter.addAction(IDownloadInterface.PAUSE_ALL_TASK_ACTION);
		registerReceiver(mPauseAllListener, intentFilter);
		// 注册网络状态监听器
		intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mNetWorkListener, intentFilter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mDownloadManager.checkTask();
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		sIsRunning = true;
		registerReceiver();
		// 初始化时调用DownloadManager的initSavedTask方法，把保存在本地的task读出来，如果task为running状态，则开始下载应用
		mDownloadManager = new CDownloadManager(this);
		mDownloadManager.initSavedTask();
		mDownloadManager.checkDownloadingTask();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		sIsRunning = false;
		unregisterReceiver(mDownloadRequestListener);
		unregisterReceiver(mAppInstallListener);
		unregisterReceiver(mPauseAllListener);
		unregisterReceiver(mNetWorkListener);
	}

}
