package com.zhidian.wifibox.download;

import java.util.Map;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.zhidian.wifibox.controller.ModeManager;

/**
 * 下载服务，要与Activity交互
 * 
 * @author xiedezhi
 * 
 */
public class DownloadService extends Service {

	// TODO 前台进程？保证服务不会被杀死
	// TODO 下载完所有任务后停止服务
	/**
	 * 网络状态
	 */
	private int mNetMode = ModeManager.COMMON_MODE;
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
			String iconUrl = intent.getStringExtra("iconUrl");
			String name = intent.getStringExtra("name");
			int size = intent.getIntExtra("size", 0);
			String packName = intent.getStringExtra("packName");
			long appId = intent.getLongExtra("appId", 0);
			String version = intent.getStringExtra("version");
			if (command.equals(IDownloadInterface.REQUEST_COMMAND_ADD)) {
				// 添加
				mDownloadManager.addTask(url, iconUrl, name, size, packName,
						appId, version);
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
				mDownloadManager.redownloadTask(url, iconUrl, name, size,
						packName, appId, version);
			} else if (command
					.equals(IDownloadInterface.REQUEST_COMMAND_CHECKTASK)) {
				// 检查下载任务
				mDownloadManager.checkTask();
			}
		}
	};
	/**
	 * 网络模式改变广播接收器
	 */
	private BroadcastReceiver mNetModeListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 判断网络模式是否改变
			int netMode = ModeManager.getInstance().isRapidly() ? ModeManager.X_MODE
					: ModeManager.COMMON_MODE;
			if (netMode != mNetMode) {
				// 网络模式发生改变
				// 当网络模式发生改变，先停掉当前所有下载任务，再切换下载管理器，再初始化新的下载管理器，再开始状态为DOWNLOADING的任务
				Map<String, DownloadTask> map = mDownloadManager
						.getAllDownloadTasks();
				for (String key : map.keySet()) {
					DownloadTask task = map.get(key);
					if (task != null && task.state == DownloadTask.DOWNLOADING) {
						mDownloadManager.pauseTask(task.url, true);
					}
				}
				// 清空任务记录器的任务
				DownloadTaskRecorder.getInstance().destory();
				// 清除下载进入通知栏
				mDownloadManager.clearNotification();
				if (ModeManager.getInstance().isRapidly()) {
					mDownloadManager = new XDownloadManager(
							DownloadService.this);
					mNetMode = ModeManager.X_MODE;
				} else {
					mDownloadManager = new CDownloadManager(
							DownloadService.this);
					mNetMode = ModeManager.COMMON_MODE;
				}
				mDownloadManager.initSavedTask();
				mDownloadManager.checkTask();
				mDownloadManager.checkDownloadingTask();
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
	 * 注册广播接收器
	 */
	private void registerReceiver() {
		// 注册下载广播事件
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
		registerReceiver(mDownloadRequestListener, intentFilter);
		// 注册网络状态监听器
		intentFilter = new IntentFilter();
		intentFilter.addAction(IDownloadInterface.DOWNLOAD_MODECHANGE_ACTION);
		registerReceiver(mNetModeListener, intentFilter);
		// 注册应用安装卸载事件
		intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentFilter.addDataScheme("package");
		registerReceiver(mAppInstallListener, intentFilter);
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
		registerReceiver();
		// 根据网络模式初始化mDownloadManager
		// 初始化时调用DownloadManager的initSavedTask方法，把保存在本地的task读出来，如果task为running状态，则开始下载应用
		if (ModeManager.getInstance().isRapidly()) {
			// 把另一个downloadmanager的所有任务暂停
			CDownloadManager manager = new CDownloadManager(this);
			manager.initSavedTask();
			Map<String, DownloadTask> map = manager.getAllDownloadTasks();
			for (String key : map.keySet()) {
				DownloadTask task = map.get(key);
				if (task != null && task.state == DownloadTask.DOWNLOADING) {
					manager.pauseTask(task.url, false);
				}
			}
			mDownloadManager = new XDownloadManager(this);
			mDownloadManager.initSavedTask();
			mDownloadManager.checkDownloadingTask();
			mNetMode = ModeManager.X_MODE;
		} else {
			// 把另一个downloadmanager的所有任务暂停
			XDownloadManager manager = new XDownloadManager(this);
			manager.initSavedTask();
			Map<String, DownloadTask> map = manager.getAllDownloadTasks();
			for (String key : map.keySet()) {
				DownloadTask task = map.get(key);
				if (task != null) {
					manager.pauseTask(task.url, false);
				}
			}
			mDownloadManager = new CDownloadManager(this);
			mDownloadManager.initSavedTask();
			mDownloadManager.checkDownloadingTask();
			mNetMode = ModeManager.COMMON_MODE;
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mDownloadRequestListener);
		unregisterReceiver(mNetModeListener);
		unregisterReceiver(mAppInstallListener);
	}

}
