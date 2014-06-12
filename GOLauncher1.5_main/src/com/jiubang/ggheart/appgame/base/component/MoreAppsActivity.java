/*
 * 文 件 名:  MoreAppsActivity.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-11-6
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;

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
import android.view.LayoutInflater;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.data.TabDataManager;
import com.jiubang.ggheart.appgame.base.utils.AppDownloadListener;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.components.DeskResourcesConfiguration;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * 
 * 应用中心，展示某个专题列表的activity，外部可以不需要启动应用中心而直接展示专题列表数据。
 * 
 * 取专题列表数据需要一个虚拟的分类id
 * 
 * @author  liuxinyang
 * @date  [2012-11-6]
 */
public class MoreAppsActivity extends Activity {
	/**
	 * 从Intent读取分类id的key
	 */
	public static final String TOPIC_TYPEID_KEY = "TOPIC_TYPEID_KEY";
	/**
	 * 从Intent读取退出专题后是否返回应用中心的key
	 */
	public static final String TOPIC_RETUREACTION_KEY = "TOPIC_RETUREACTION_KEY";
	/**
	 * 读取数据的虚拟分类id
	 */
	private int mTypeId = -1;
	/**
	 * 退出专题后是否返回应用中心
	 */
	private boolean mIsReturnToAppCenter = false;

	/**
	 * 是否已经绑定下载服务的标记
	 */
	private boolean mHasBindService = false;
	/**
	 * 下载控制器
	 */
	private IDownloadService mDownloadController = null;
	/**
	 * 进度条加载页
	 */
	private MoreAppsView mView = null;

	private LayoutInflater mInflater = null;

	private BroadcastReceiver mDownloadReceiver = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		mTypeId = intent.getIntExtra(TOPIC_TYPEID_KEY, -1);
		mIsReturnToAppCenter = intent.getBooleanExtra(TOPIC_RETUREACTION_KEY, false);
		
		mInflater = LayoutInflater.from(this);
		registerDownloadReceiver();
	}

	@Override
	protected void onStart() {
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
		super.onStop();
		//解除绑定下载服务
		if (mHasBindService) {
			GOLauncherApp.getContext().unbindService(mConnenction);
			mHasBindService = false;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mDownloadReceiver);
	}
	
	private ServiceConnection mConnenction = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mDownloadController = IDownloadService.Stub.asInterface(service);
			try {
				// 添加保持DownloadService运行的Activity的className
				// 假如activity在Task的TopProject,那么DownloadService就不会被停止
				mDownloadController.addRunningActivityClassName(MoreAppsActivity.class.getName());
				GOLauncherApp.getApplication().setDownloadController(mDownloadController);
				showView();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mDownloadController = null;
			GOLauncherApp.getApplication().setDownloadController(mDownloadController);
		}
	};

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

	private void showView() {
		if (mView == null) {
			mView = (MoreAppsView) mInflater.inflate(R.layout.more_apps_view, null);
			try {
				if (mDownloadController != null) {
					ArrayList<DownloadTask> list = (ArrayList<DownloadTask>) mDownloadController
							.getDownloadingTaskSortByTime();
					mView.setDownloadTaskList(list);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			mView.setOutsideHandler(mHandler);
			setContentView(mView);
			TabDataManager.getInstance().removeTabData(mTypeId);
			mView.startLoadData(mTypeId);
		}
		try {
			if (mDownloadController != null) {
				ArrayList<DownloadTask> list = (ArrayList<DownloadTask>) mDownloadController
						.getDownloadingTaskSortByTime();
				mView.setDownloadTaskList(list);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static final int sMSG_QUIT = 11;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case sMSG_QUIT :
					finish();
					if (mIsReturnToAppCenter) {
						AppsManagementActivity.startAppCenter(getApplicationContext(),
								MainViewGroup.ACCESS_FOR_SHORTCUT, false);
					}
					break;
			}
		};
	};
	
	public void onBackPressed() {
		super.onBackPressed();
		if (mIsReturnToAppCenter) {
			AppsManagementActivity.startAppCenter(getApplicationContext(),
					MainViewGroup.ACCESS_FOR_SHORTCUT, false);
		}
	};

	private void registerDownloadReceiver() {
		if (mDownloadReceiver == null) {
			mDownloadReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					DownloadTask downloadTask = intent
							.getParcelableExtra(AppDownloadListener.UPDATE_DOWNLOAD_INFO);
					if (mView != null) {
						mView.notifyDownloadState(downloadTask);
					}
				}
			};
		}
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ICustomAction.ACTION_APP_DOWNLOAD);
		this.registerReceiver(mDownloadReceiver, intentFilter);
	}
}
