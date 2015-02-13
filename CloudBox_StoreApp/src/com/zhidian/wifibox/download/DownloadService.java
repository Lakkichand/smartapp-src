package com.zhidian.wifibox.download;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.text.TextUtils;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.tencent.stat.StatService;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.controller.DownloadController;
import com.zhidian.wifibox.controller.ModeManager;
import com.zhidian.wifibox.data.AppDownloadCount;
import com.zhidian.wifibox.util.BoxIdManager;
import com.zhidian.wifibox.util.CheckNetwork;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.InstallingValidator;

/**
 * 下载服务，要与Activity交互
 * 
 * @author xiedezhi
 * 
 */
public class DownloadService extends Service {

	public static boolean sIsRunning = false;

	// TODO 前台进程？保证服务不会被杀死
	// TODO 下载完所有任务后停止服务
	/**
	 * 当前模式的下载管理器
	 */
	private IDownloadInterface mDownloadManager;

	private String mRapName;
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
			String src = intent.getStringExtra("src");
			String page = intent.getStringExtra("page") + "";
			if (command.equals(IDownloadInterface.REQUEST_COMMAND_ADD)) {
				// 上传下载数据
				unloadAppDownload(appId, packName, version, src, context);
				mtaDownloadStatistics(name, appId, packName, version, src, page);
				// 添加
				mDownloadManager.addTask(url, iconUrl, name, size, packName,
						appId, version, src);
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
						packName, appId, version, src);
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
				InstallingValidator.getInstance().onAppAction(
						TAApplication.getApplication(), packageName);
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
			boolean isRap = ModeManager.checkRapidly();
			if (TextUtils.isEmpty(mRapName) && isRap) {
				// 普通-->>极速
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
				// 记录极速名字
				ModeManager.getInstance().recordRapName();
				mRapName = ModeManager.getInstance().getRapName();
			} else if (!TextUtils.isEmpty(mRapName) && !isRap) {
				// 极速-->>普通
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
				// 记录极速名字
				ModeManager.getInstance().recordRapName();
				mRapName = ModeManager.getInstance().getRapName();
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

	/**
	 * 腾讯云统计下载点击
	 */
	private void mtaDownloadStatistics(String name, long appId,
			String packName, String version, String src, String page) {
		Properties prop = new Properties();
		prop.setProperty("name", name);
		prop.setProperty("uuId",
				InfoUtil.getUUID(TAApplication.getApplication()));
		prop.setProperty("boxNum", BoxIdManager.getInstance().getBoxId());
		prop.setProperty("appId", appId + "");
		prop.setProperty("packageName", packName);
		prop.setProperty("version", version);
		if (!TextUtils.isEmpty(src)) {// true表示为急速模式
			prop.setProperty("downloadModel", "超速下载");
		} else {
			prop.setProperty("downloadModel", "普通下载");
		}
		if (ModeManager.checkRapidly()) {
			prop.setProperty("downloadSource", "盒子WIFI");
		} else {
			prop.setProperty("downloadSource", "普通WIFI");
		}
		prop.setProperty("networkWay",
				CheckNetwork.getAPNType(TAApplication.getApplication()));
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());
		prop.setProperty("downloadTime", formatter.format(curDate));
		prop.setProperty("fromPage", page);
		StatService.trackCustomKVEvent(TAApplication.getApplication(),
				"downloadclickstatistics", prop);
	}

	/**
	 * 上传下载app数据到插件
	 * 
	 * @param appId
	 * @param packName
	 * @param version
	 */
	@SuppressLint("SimpleDateFormat")
	private void unloadAppDownload(long appId, String packName, String version,
			String src, Context context) {
		AppDownloadCount bean = new AppDownloadCount();
		bean.uuId = InfoUtil.getUUID(TAApplication.getApplication());
		bean.boxNum = BoxIdManager.getInstance().getBoxId();
		bean.appId = String.valueOf(appId);
		bean.packageName = packName;
		bean.version = version;
		if (!TextUtils.isEmpty(src)) {// true表示为急速模式
			bean.downloadModel = "0";
		} else {
			bean.downloadModel = "1";
		}
		if (ModeManager.checkRapidly()) {
			bean.downloadSource = "0";// 表示是在门店下载（无论是否是急速模式）
		} else {
			bean.downloadSource = "1";
		}
		bean.networkWay = CheckNetwork.getAPNType(context);// 联网方式
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		bean.downloadTime = formatter.format(curDate);

		TAApplication.getApplication().doCommand(
				context.getString(R.string.downloadcontroller),
				new TARequest(DownloadController.DOWNLOAD, bean),
				new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {

					}

					@Override
					public void onStart() {

					}

					@Override
					public void onRuning(TAResponse response) {

					}

					@Override
					public void onFinish() {

					}

					@Override
					public void onFailure(TAResponse response) {

					}
				}, true, false);
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
		ModeManager.getInstance().recordRapName();
		mRapName = ModeManager.getInstance().getRapName();
		sIsRunning = true;
		registerReceiver();
		// 初始化时调用DownloadManager的initSavedTask方法，把保存在本地的task读出来，如果task为running状态，则开始下载应用
		mDownloadManager = new CDownloadManager(this);
		mDownloadManager.initSavedTask();
		mDownloadManager.checkDownloadingTask();
	}

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
