package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.LayoutInflater;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.base.utils.AppDownloadListener;
import com.jiubang.ggheart.appgame.base.utils.AppGameConfigUtils;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * 
 * <br>类描述:更多相关推荐Activity
 * <br>功能详细描述:
 * 
 * @author  zhengxiangcan
 * @date  [2012-12-18]
 */
public class MoreRecommendedAppsActivity extends Activity {
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
	private MoreRecommendedAppsView mView = null;

	private LayoutInflater mInflater = null;

	private BroadcastReceiver mDownloadReceiver = null;
	
	private String mPkgName = null;
	
	public static final String sPACKAGE_NAME = "package_name";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		// 根据SIM卡初始化语言配置信息
		AppGameConfigUtils.updateResourcesLocaleBySim(this, super.getResources());
		
		if (getIntent() != null) {
			mPkgName = getIntent().getStringExtra(sPACKAGE_NAME);
		}
		mInflater = LayoutInflater.from(this);
		registerDownloadReceiver();
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
		//解除绑定下载服务
		if (mHasBindService) {
			GOLauncherApp.getContext().unbindService(mConnenction);
			mHasBindService = false;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
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
				mDownloadController.addRunningActivityClassName(MoreRecommendedAppsActivity.class.getName());
				GOLauncherApp.getApplication().setDownloadController(mDownloadController);
				showView();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mDownloadController = null;
			GOLauncherApp.getApplication().setDownloadController(mDownloadController);
		}
	};

	private void showView() {
		if (mView == null) {
			mView = (MoreRecommendedAppsView) mInflater.inflate(R.layout.more_recommended_apps_view, null);
		}
		mView.setOutsideHandler(mHandler);
		mView.setPkgName(mPkgName);
		setContentView(mView);
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
					break;
			}
		};
	};

	private void registerDownloadReceiver() {
		if (mDownloadReceiver == null) {
			mDownloadReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					// TODO Auto-generated method stub
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
