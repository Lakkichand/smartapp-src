/**
 * 
 */
package com.jiubang.ggheart.appgame.base.downloadmanager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.widget.FrameLayout.LayoutParams;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.IAidlDownloadListener;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.components.DeskResourcesConfiguration;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * @author liuxinyang
 * 
 */
public class AppsDownloadActivity extends Activity {

	//当前正在显示下载控制界面
	private final int mShowControllView = 1;

	//当前正在显示批量删除界面
	private final int mShowDeleteView = 2;

	private int mShowView = mShowControllView;

	// 下载控制器
	private IDownloadService mDownloadController = null;

	// 正在下载的任务listener的ID， <taskID , listenerID>
	private HashMap<Long, Long> mListenerHashMap = new HashMap<Long, Long>();

	// 下载控制view
	private DownloadControllView mDownloadControllView = null;

	// 批量删除view
	private DownloadDeleteView mDownloadDeleteView = null;

	private LayoutInflater mInflater = null;

	/**
	 * 是否已经绑定下载服务的标记
	 */
	private boolean mHasBindService = false;

	public static AppsDownloadActivity sContext = null;
	
	public static final String QUITSTYLE = "quit";
	/**
	 * 回退到之前的界面
	 */
	public static final int QUIT_TO_OLD_ACTIVITY = 0;
	/**
	 * 回退到应用游戏中心
	 */
	public static final int QUIT_TO_APPCENTER = 1;
	/**
	 * 回退方式
	 */
	private int mQuit = QUIT_TO_OLD_ACTIVITY;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		registerInstalledBroadCast();
		if (getIntent() != null) {
			mQuit = getIntent().getIntExtra(QUITSTYLE, QUIT_TO_OLD_ACTIVITY);
		}
		mInflater = LayoutInflater.from(this);
		sContext = this;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mHandler.sendEmptyMessage(MSG_UPDATE_LIST);
	}

	@Override
	protected void onDestroy() {
		if (mInstalledReceiver != null) {
			unregisterReceiver(mInstalledReceiver);
		}
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// 先启动下载服务
		GOLauncherApp.getContext().startService(new Intent(ICustomAction.ACTION_DOWNLOAD_SERVICE));
		// 再bind服务
		if (!mHasBindService) {
			mHasBindService = GOLauncherApp.getContext().bindService(
					new Intent(ICustomAction.ACTION_DOWNLOAD_SERVICE), mConnenction,
					Context.BIND_AUTO_CREATE);
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		removeListeners();
		//解除绑定下载服务
		if (mHasBindService) {
			GOLauncherApp.getContext().unbindService(mConnenction);
			mHasBindService = false;
		}
	}

	private ServiceConnection mConnenction = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mDownloadController = IDownloadService.Stub.asInterface(service);
			try {
				// 添加保持DownloadService运行的Activity的className
				// 假如activity在Task的TopProject,那么DownloadService就不会被停止
				if (mDownloadController != null) {
					mDownloadController.addRunningActivityClassName(sContext.getClass().getName());
					addListenerToDownloadTask();
				}
				showControllerView();
				mHandler.sendEmptyMessage(MSG_UPDATE_LIST);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mDownloadController = null;
		}
	};

	public static AppsDownloadActivity getContext() {
		return sContext;
	}

	public IDownloadService getDownloadController() {
		return mDownloadController;
	}

	public int getQuitStyle() {
		return mQuit;
	}
	
	private void showControllerView() {
		if (mDownloadControllView == null) {
			mDownloadControllView = (DownloadControllView) mInflater.inflate(
					R.layout.appgame_download_control_view, null);
			mDownloadControllView.setHandler(mHandler);
		}
		setContentView(mDownloadControllView, new LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT));
		mDownloadDeleteView = null;
		mShowView = mShowControllView;
	}

	private void showDeleteView() {
		if (mDownloadDeleteView == null) {
			mDownloadDeleteView = (DownloadDeleteView) mInflater.inflate(
					R.layout.appgame_download_delete_view, null);
			mDownloadDeleteView.setHandler(mHandler);
		}
		setContentView(mDownloadDeleteView, new LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT));
		mDownloadControllView = null;
		mShowView = mShowDeleteView;
	}

	/**
	 * <br>功能简述:将下载listener的回调转移到UI线程执行，否则会报错
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param task
	 */
	private void notifyDownloadTask(DownloadTask task) {
		Message msg = new Message();
		msg.what = MSG_UPDATE_DOWNLOAD_TASK;
		msg.obj = task;
		mHandler.sendMessage(msg);
	}

	/**
	 * <br>功能简述:构建默认的下载listener
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	private IAidlDownloadListener createListener() {
		IAidlDownloadListener listener = new IAidlDownloadListener.Stub() {
			@Override
			public void onWait(DownloadTask task) throws RemoteException {
				notifyDownloadTask(task);
			}

			@Override
			public void onUpdate(DownloadTask task) throws RemoteException {
				notifyDownloadTask(task);
			}

			@Override
			public void onStop(DownloadTask task) throws RemoteException {
				notifyDownloadTask(task);
			}

			@Override
			public void onStart(DownloadTask task) throws RemoteException {
				notifyDownloadTask(task);
			}

			@Override
			public void onReset(DownloadTask task) throws RemoteException {
				notifyDownloadTask(task);
			}

			@Override
			public void onFail(DownloadTask task) throws RemoteException {
				notifyDownloadTask(task);
			}

			@Override
			public void onException(DownloadTask task) throws RemoteException {
				notifyDownloadTask(task);
			}

			@Override
			public void onDestroy(DownloadTask task) throws RemoteException {
				notifyDownloadTask(task);
			}

			@Override
			public void onConnectionSuccess(DownloadTask task) throws RemoteException {
				notifyDownloadTask(task);
			}

			@Override
			public void onComplete(DownloadTask task) throws RemoteException {
				notifyDownloadTask(task);
			}

			@Override
			public void onCancel(DownloadTask task) throws RemoteException {
				notifyDownloadTask(task);
			}
		};
		return listener;
	}

	/**
	 * <br>功能简述:为所有当前正在下载队列的下载任务添加listener
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void addListenerToDownloadTask() {
		try {
			if (mDownloadController != null) {
				Map<Long, DownloadTask> map = mDownloadController.getDownloadConcurrentHashMap();
				for (DownloadTask task : map.values()) {
					if (task != null) {
						long id = task.getId();
						long listenerId = mDownloadController.addDownloadTaskListener(id,
								createListener());
						if (listenerId != -1) {
							mListenerHashMap.put(id, listenerId);
						}
					}
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <br>功能简述:清理本activity添加的任务监听器
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void removeListeners() {
		try {
			if (mDownloadController == null) {
				return;
			}
			Iterator iter = mListenerHashMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Long taskId = (Long) entry.getKey();
				Long listenerId = (Long) entry.getValue();
				DownloadTask dt = null;
				dt = mDownloadController.getDownloadTaskById(taskId);
				if (dt != null) {
					mDownloadController.removeDownloadTaskListener(taskId, listenerId);
				}
				iter.remove();
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 退出activity,
	public static final int MSG_FINISH_ACTIVITY = 1;

	// 显示批量删除界面
	public static final int MSG_SHOW_DELETE_VIEW = 2;

	// 移除批量删除界面
	public static final int MSG_REMOVE_DELETE_VIEW = 3;

	// 更新下载任务状态信息
	public static final int MSG_UPDATE_DOWNLOAD_TASK = 4;

	// 更新下载列表
	public static final int MSG_UPDATE_LIST = 5;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_FINISH_ACTIVITY :
					// 不同进入方式有不同的返回方式
					if (mQuit == QUIT_TO_APPCENTER) {
						// 进入应用中心的统计
						AppManagementStatisticsUtil.getInstance().saveCurrentEnter(sContext,
								AppManagementStatisticsUtil.ENTRY_TYPE_NOTICE);
						AppsManagementActivity.startAppCenter(sContext,
								MainViewGroup.ACCESS_FOR_SHORTCUT, true);
					} 
					finish();
					break;
				case MSG_UPDATE_DOWNLOAD_TASK :
					DownloadTask task = (DownloadTask) msg.obj;
					if (mDownloadControllView != null) {
						mDownloadControllView.notifyTask(task);
					}
					if (mDownloadDeleteView != null) {
						mDownloadDeleteView.notifyTask(task);
					}
					break;
				case MSG_SHOW_DELETE_VIEW :
					showDeleteView();
					break;
				case MSG_REMOVE_DELETE_VIEW :
					showControllerView();
					break;
				case MSG_UPDATE_LIST :
					if (mDownloadControllView != null) {
						mDownloadControllView.updateList();
					}
					if (mDownloadDeleteView != null) {
						mDownloadDeleteView.updateList();
					}
					break;
			}
		};
	};

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
			// 当前正在显示批量删除界面，则切换到下载控制界面
			if (mShowView == mShowDeleteView) {
				mHandler.sendEmptyMessage(AppsDownloadActivity.MSG_REMOVE_DELETE_VIEW);
				return true;
			} else if (mQuit == QUIT_TO_APPCENTER) {
				finish();
				// 进入应用中心的统计
				AppManagementStatisticsUtil.getInstance().saveCurrentEnter(sContext,
						AppManagementStatisticsUtil.ENTRY_TYPE_NOTICE);
				AppsManagementActivity.startAppCenter(this, MainViewGroup.ACCESS_FOR_SHORTCUT, true);
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	private BroadcastReceiver mInstalledReceiver = null;

	private void registerInstalledBroadCast() {
		// 注册安装广播接收器
		// 收到广播，把列表的一项从数据源移除
		mInstalledReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
					String packageName = intent.getData().getSchemeSpecificPart();
					if (packageName != null) {
						mHandler.sendEmptyMessage(MSG_UPDATE_LIST);
					}
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_PACKAGE_ADDED);
		filter.addDataScheme("package");
		registerReceiver(mInstalledReceiver, filter);
	}

	@Override
	public Resources getResources() {
		DeskResourcesConfiguration configuration = DeskResourcesConfiguration.getInstance();
		if (null != configuration) {
			Resources resources = configuration.getDeskResources();
			if (null != resources) {
				return resources;
			}
		}

		return super.getResources();
	}

}
