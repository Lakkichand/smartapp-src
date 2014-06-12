/*
 * 文 件 名:  DownloadService.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-8-16
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.download;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;

import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * <br>
 * 类描述: <br>
 * 功能详细描述:
 * 
 * @author liuxinyang
 * @date [2012-8-16]
 */
public class DownloadService extends Service {
	
	public final static String SERVICE_BROADCAST = "com.jiubang.downloadservice.stop";
	
	private DownloadService mStaticSelf = null;

	//下载管理类
	private DownloadManager mDownloadManager = DownloadManager.getInstance(GOLauncherApp
			.getContext());
	
	//安装管理类
	private InstallManager mInstallManager = InstallManager.getInstance(GOLauncherApp.getContext());

	//广播接收器
	private BroadcastReceiver mReceiver = null;
	
	//计时器
	private Timer mTimer = null;

	//检查下载任务是否要停止的定时任务
	private MyTimerTask mTimerTask = null;

	//下载服务是否存活的判断实例
	private RunngingDownloadServiceConstance mRunningCondition = new RunngingDownloadServiceConstance();

	//下载服务通过AIDL对外提供的接口实现
	private IDownloadService.Stub mBinder = new IDownloadService.Stub() {

		@Override
		public void stopDownloadById(long taskId) throws RemoteException {
			if (mDownloadManager == null) {
				mDownloadManager = DownloadManager.getInstance(GOLauncherApp.getContext());
			}
			mDownloadManager.stopDownloadById(taskId);
		}

		@Override
		public void restartDownloadById(long taskId) throws RemoteException {
			if (mDownloadManager == null) {
				mDownloadManager = DownloadManager.getInstance(GOLauncherApp.getContext());
			}
			mDownloadManager.restartDownload(taskId);
		}

		@Override
		public void removeTaskIdFromDownloading(long taskId) throws RemoteException {
			if (mDownloadManager == null) {
				mDownloadManager = DownloadManager.getInstance(GOLauncherApp.getContext());
			}
			mDownloadManager.removeTaskIdFromDownloading(taskId, true);
		}

		@Override
		public void removeListener(long id) throws RemoteException {
			if (mDownloadManager == null) {
				mDownloadManager = DownloadManager.getInstance(GOLauncherApp.getContext());
			}
			mDownloadManager.removeDownloadManagerListener(id);
		}

		@Override
		public void removeDownloadTaskById(long taskId) throws RemoteException {
			if (mDownloadManager == null) {
				mDownloadManager = DownloadManager.getInstance(GOLauncherApp.getContext());
			}
			mDownloadManager.removeDownloadTaskById(taskId, true);

			//从下载服务的失败队列中删除
			if (mDownloadManager.getFailTaskIdList().contains(taskId)) {
				mDownloadManager.getFailTaskIdList().remove(taskId);
			}
		}

		@Override
		public DownloadTask getDownloadTaskById(long taskId) throws RemoteException {
			if (mDownloadManager == null) {
				mDownloadManager = DownloadManager.getInstance(GOLauncherApp.getContext());
			}
			return mDownloadManager.getDownloadTaskById(taskId);
		}

		@Override
		public Map<Long, DownloadTask> getDownloadConcurrentHashMap() throws RemoteException {
			if (mDownloadManager == null) {
				mDownloadManager = DownloadManager.getInstance(GOLauncherApp.getContext());
			}
			return mDownloadManager.getDownloadConcurrentHashMap();
		}

		/**
		 * 直接传入IAidlDownloadListener实例对象
		 * 由于aidl通信模式的原因，传入的IAidlDownloadListener实例对象会随着产生该对象的进程的销毁而销毁
		 * 适用于局部更新下载状态的listener
		 */
		@Override
		public long addDownloadTaskListener(long taskId, IAidlDownloadListener listener)
				throws RemoteException {
			DownloadTask task = getDownloadTaskById(taskId);
			long listenerId = -1;
			if (task != null) {
				listenerId = task.addDownloadListener(listener);
			}
			return listenerId;
		}

		@Override
		public void removeDownloadTaskListener(long taskId, long listenerId) throws RemoteException {
			DownloadTask task = getDownloadTaskById(taskId);
			if (task != null) {
				task.removeDownloadListener(listenerId);
			}
		}

		@Override
		public void removAllDownloadTaskListeners(long taskId) throws RemoteException {
			DownloadTask task = getDownloadTaskById(taskId);
			if (task != null) {
				task.removeAllDownloadListener();
			}
		}

		@Override
		public void startDownload(long taskId) throws RemoteException {
			if (mDownloadManager == null) {
				mDownloadManager = DownloadManager.getInstance(GOLauncherApp.getContext());
			}
			mDownloadManager.startDownload(taskId);
		}

		@Override
		public void removeDownloadTasksById(List list) throws RemoteException {
			if (mDownloadManager == null) {
				mDownloadManager = DownloadManager.getInstance(GOLauncherApp.getContext());
			}
			if (list != null) {
				ArrayList<Long> al = (ArrayList<Long>) list;
				mDownloadManager.removeDownloadTaskById(al);

				if (mDownloadManager.getFailTaskIdList() != null) {
					// 从下载服务的失败队列中删除下载任务
					for (int i = 0; i < list.size(); i++) {
						if (mDownloadManager.getFailTaskIdList().contains(list.get(i))) {
							mDownloadManager.getFailTaskIdList().remove(list.get(i));
						}
					}
				}
			}
		}

		@Override
		public long addDownloadTask(DownloadTask task) throws RemoteException {
			if (mDownloadManager == null) {
				mDownloadManager = DownloadManager.getInstance(GOLauncherApp.getContext());
			}
			long taskId = mDownloadManager.addDownloadTask(task);
			return taskId;
		}

		@Override
		public List getCompleteIdsByPkgName(String packageName) throws RemoteException {
			if (mDownloadManager == null) {
				mDownloadManager = DownloadManager.getInstance(GOLauncherApp.getContext());
			}
			ArrayList<Long> list = mDownloadManager.getDownloadCompleteManager()
					.getCompleteIdsByPkgName(packageName);
			return list;
		}

		/**
		 * 根据listener的类名生成listener的实例
		 * 由于aidl通信模式的原因，当下载状态无论在哪个进程都需要监听的时候（例如通知栏的更新），必须使用此方法
		 * 由这个方法添加的下载监听全局可用
		 */
		@Override
		public long addDownloadTaskListenerByName(long taskId, String name) throws RemoteException {
			long id = -1;
			if (name != null && !name.trim().equals("")) {
				try {
					IAidlDownloadListener downloadListener = (IAidlDownloadListener) Class
							.forName(name).getConstructor(Context.class)
							.newInstance(GOLauncherApp.getContext());
					DownloadTask task = getDownloadTaskById(taskId);
					if (downloadListener != null && task != null) {
						id = task.addDownloadListener(downloadListener);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return id;
		}

		@Override
		public List getDownloadCompleteList() throws RemoteException {
			if (mDownloadManager == null) {
				mDownloadManager = DownloadManager.getInstance(GOLauncherApp.getContext());
			}
			return mDownloadManager.getDownloadCompleteManager().getDownloadCompleteList();
		}

		@Override
		public void removeDownloadCompleteItem(long taskId) throws RemoteException {
			if (mDownloadManager == null) {
				mDownloadManager = DownloadManager.getInstance(GOLauncherApp.getContext());
			}
			mDownloadManager.getDownloadCompleteManager().removeDownloadCompleteTask(taskId);
		}

		@Override
		public List getDownloadingTaskSortByTime() throws RemoteException {
			ArrayList<DownloadTask> taskList = mDownloadManager.getDownloadTaskSortByTime();
			return taskList;
		}

		@Override
		public void addInstalledPackage(String packageName) throws RemoteException {
			if (mDownloadManager == null) {
				mDownloadManager = DownloadManager.getInstance(GOLauncherApp.getContext());
			}
			mDownloadManager.getDownloadCompleteManager().addInstalledTask(packageName);
		}

		@Override
		public List getInstalledTaskList() throws RemoteException {
			if (mDownloadManager == null) {
				mDownloadManager = DownloadManager.getInstance(GOLauncherApp.getContext());
			}
			return mDownloadManager.getDownloadCompleteManager().getInstalledTaskList();
		}

		@Override
		public void addRunningActivityClassName(String className) throws RemoteException {
			if (mRunningCondition != null) {
				mRunningCondition.addActivityClassName(className);
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mStaticSelf = this;
		//注册广播接收器
		register();
		//加载之前保存的下载任务信息
		mDownloadManager.getAllTaskInfo();
		// 创建定时器，每隔一分钟检测下载服务处于工作状态，否则停止下载服务
		mTimer = new Timer();
		mTimerTask = new MyTimerTask();
		mTimer.schedule(mTimerTask, 1 * 60 * 1000, 1 * 60 * 1000);
	}

	/**
	 * 功能简述:注册DownloadService的广播接收器，用于接收桌面发送的保存信息的广播和网络状态改变的广播
	 * 功能详细描述:桌面退出，接收广播后，停止下载任务，进行保存后退出。网络状态改变，对之前下载失败的任务进行重新连接
	 * 注意:
	 */
	private void register() {
		if (mReceiver == null) {
			mReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();
					if (action.equals(ICustomAction.ACTION_SERVICE_BROADCAST)) {
						// 通知下载服务退出
						notifyQuit();
					} else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
						NetworkInfo ni = intent
								.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
						if (ni != null && ni.getState() == State.CONNECTED) {
							restartFailedTask(ni.getType());
						}
					}
				}
			};
		}

		IntentFilter filter = new IntentFilter();
		filter.addAction(ICustomAction.ACTION_SERVICE_BROADCAST);
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mReceiver, filter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//TODO:下载wangzhuobin 这里为什么不用停止全部的下载任务才保存呢？
		mDownloadManager.saveAllTaskInfo();
		cleanData();
	}

	/**
	 * 功能简述:服务退出调用
	 * 功能详细描述:
	 * 注意:
	 */
	private void notifyQuit() {
		mDownloadManager.stopAllDownloadTask();
		onQuit();
	}

	/**
	 * <br>功能简述:退出之后的数据清除
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void cleanData() {
		if (mReceiver != null) {
			if (mReceiver != null) {
				unregisterReceiver(mReceiver);
				mReceiver = null;
			}
			mReceiver = null;
		}
		mDownloadManager = null;
		mInstallManager = null;
		mTimer.cancel();
		mTimerTask.cancel();
		mTimer = null;
		mTimerTask = null;
		mStaticSelf = null;
		android.os.Process.killProcess(Process.myPid());
	}

	/**
	 * <br>功能简述:退出之前的相关保存工作 
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void onQuit() {
		// 第一阶段：保存信息
		mDownloadManager.saveAllTaskInfo();
		// 第二阶段：stopSelf
		stopSelf();
	}

	/**
	 * <br>功能简述:根据网络状态，对之前下载失败的任务进行重新下载的方法
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param type
	 */
	private void restartFailedTask(int type) {
		if (mDownloadManager == null || mDownloadManager.getFailTaskIdList() == null
				|| mDownloadManager.getFailTaskIdList().size() <= 0) {
			return;
		}
		//wifi环境下，将在wifi下载失败的任务重新连接
		if (type == ConnectivityManager.TYPE_WIFI) {
			Iterator<Long> iter = mDownloadManager.getFailTaskIdList().iterator();
			while (iter.hasNext()) {
				long id = iter.next();
				mDownloadManager.startDownload(id);
				StatisticsData.countStatData(GOLauncherApp.getContext(),
						StatisticsData.KEY_AUTO_DOWNLOAD);
				iter.remove();
			}
		} else {
			//其它数据网络下，将在数据网络连接失败的任务重新连接
			Iterator<Long> iter = mDownloadManager.getFailTaskIdList().iterator();
			while (iter.hasNext()) {
				long id = iter.next();
				DownloadTask task = mDownloadManager.getDownloadTaskById(id);
				if (task == null) {
					continue;
				}
				if (task.getDownloadNetWorkType() == DownloadTask.NETWORK_TYPE_OTHER) {
					mDownloadManager.startDownload(id);
					StatisticsData.countStatData(GOLauncherApp.getContext(),
							StatisticsData.KEY_AUTO_DOWNLOAD);
					iter.remove();
				}
			}
		}
	}

	/**
	 * 
	 * <br>类描述:DownloadService的计时退出任务
	 * <br>功能详细描述:
	 * 
	 * @author  liuxinyang
	 * @date  [2012-10-24]
	 */
	private class MyTimerTask extends TimerTask {
		@Override
		public void run() {
			// 是否继续运行DownloadService
			ActivityManager activityManager = (ActivityManager) mStaticSelf
					.getSystemService(ACTIVITY_SERVICE);
			// 先判断是否还有正在下载和等待下载的任务
			for (DownloadTask task : mDownloadManager.getDownloadConcurrentHashMap().values()) {
				int state = task.getState();
				if (state == DownloadTask.STATE_DOWNLOADING || state == DownloadTask.STATE_RESTART
						|| state == DownloadTask.STATE_START || state == DownloadTask.STATE_WAIT) {
					return;
				}
			}
			// 判断桌面的进程
			List<RunningAppProcessInfo> mRunningAppProcessInfo = activityManager
					.getRunningAppProcesses();
			for (RunningAppProcessInfo runningAppProcess : mRunningAppProcessInfo) {
				if (runningAppProcess != null) {
					if (mRunningCondition.isProcessKeepAlive(runningAppProcess.processName)) {
						return;
					}
				}
			}
			// 判断桌面的Top Activity
			List<RunningTaskInfo> mRunningTaskInfo = activityManager
					.getRunningTasks(Integer.MAX_VALUE);
			for (RunningTaskInfo runningTask : mRunningTaskInfo) {
				if (runningTask != null) {
					if (mRunningCondition.isActivityKeepAlive(runningTask.topActivity
							.getClassName())) {
						return;
					}
				}
			}
			onQuit();
		}
	};
}
