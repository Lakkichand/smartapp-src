/*
 * 文 件 名:  AppInstallActivity.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-9-24
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.download.DownloadService;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.appgame.download.InstallManager;
import com.jiubang.ggheart.components.DeskResourcesConfiguration;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * <br>类描述:程序的安装activity,用于顺序安装下载的apk包
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-9-24]
 */
public class AppInstallActivity extends Activity {

	public ArrayList<String> mPkgArray = new ArrayList<String>();

	private final int mInstallActivityRequestCode = 1001;

	private InstallManager mInstallMgr = null;

	private static boolean sMIsNeedInstall = true;

	/**
	 * 是否已经绑定下载服务的标记
	 */
	private boolean mHasBindService = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mInstallMgr = InstallManager.getInstance(this);
		mInstallMgr.setState(InstallManager.NO_NEED_NEW_ACTIVITY);
		String path = null;
		if (getIntent() != null) {
			Uri uri = getIntent().getData();
			if (uri != null) {
				path = uri.getPath();
			}
		}
		if (path == null) {
			startInstall();
		} else {
			startInstall(path);
		}
		// 先启动下载服务
		GOLauncherApp.getContext().startService(new Intent(ICustomAction.ACTION_DOWNLOAD_SERVICE));
		// 再bind服务
		if (!mHasBindService) {
			mHasBindService = GOLauncherApp.getContext().bindService(
					new Intent(ICustomAction.ACTION_DOWNLOAD_SERVICE), mConnenction,
					Context.BIND_AUTO_CREATE);
		}
	}

	/**
	 * 下载服务的控制接口Connector
	 */
	private ServiceConnection mConnenction = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			IDownloadService mController = IDownloadService.Stub.asInterface(service);
			try {
				// 添加保持DownloadService运行的Activity的className
				// 假如activity在Task的TopProject,那么DownloadService就不会被停止
				if (mController != null) {
					mController.addRunningActivityClassName(AppInstallActivity.class.getName());
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//解除绑定下载服务
		if (mHasBindService) {
			GOLauncherApp.getContext().unbindService(mConnenction);
			mHasBindService = false;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		String path = null;
		if (getIntent() != null) {
			Uri uri = getIntent().getData();
			if (uri != null) {
				path = uri.getPath();
			}
		}
		if (path == null) {
			startInstall();
		} else {
			startInstall(path);
		}
	}

	private void startInstall() {
		if (!sMIsNeedInstall) {
			return;
		}
		if (mInstallMgr == null) {
			mInstallMgr = InstallManager.getInstance(this);
		}
		String path = mInstallMgr.getPkgFromArray();
		if (path == null) {
			mInstallMgr.setState(InstallManager.NEED_NEW_ACTIVITY);
			this.finish();
			if (!isServiceRunning(DownloadService.class.getName())) {
				Log.i("liuxinyang", "kill download service");
				android.os.Process.killProcess(Process.myPid());
			}
			return;
		}
		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		String[] token = file.getName().split("\\.");
		String pf = token[token.length - 1];
		if (!pf.equals("apk")) {
			return;
		}
		sMIsNeedInstall = false;
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		this.startActivityForResult(intent, mInstallActivityRequestCode);
	}

	/**
	 * <br>功能简述:传入安装路径参数进行安装，用于通知栏pendingIntent的点击安装
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param path
	 */
	private void startInstall(String path) {
		sMIsNeedInstall = true;
		if (!sMIsNeedInstall) {
			finish();
			mInstallMgr.setState(InstallManager.NEED_NEW_ACTIVITY);
			return;
		}
		if (mInstallMgr == null) {
			mInstallMgr = InstallManager.getInstance(this);
		}
		File file = new File(path);
		if (!file.exists()) {
			Toast.makeText(this, this.getString(R.string.download_manager_apk_not_found), 1000).show();
			mInstallMgr.setState(InstallManager.NEED_NEW_ACTIVITY);
			finish();
			return;
		}
		String[] token = file.getName().split("\\.");
		String pf = token[token.length - 1];
		if (!pf.equals("apk")) {
			finish();
			mInstallMgr.setState(InstallManager.NEED_NEW_ACTIVITY);
			return;
		}
		mInstallMgr.removePkgFromArray(path);
		sMIsNeedInstall = false;
		Intent intent = new Intent();
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		this.startActivityForResult(intent, mInstallActivityRequestCode);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == mInstallActivityRequestCode
				&& (resultCode == RESULT_CANCELED || resultCode == RESULT_FIRST_USER)) {
			sMIsNeedInstall = true;
			startInstall();
		}
	}

	private boolean isServiceRunning(String className) {
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) this
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(Integer.MAX_VALUE);
		if (!(serviceList.size() > 0)) {
			return false;
		}
		for (int i = 0; i < serviceList.size(); i++) {
			if (serviceList.get(i).service.getClassName().equals(className) == true) {
				isRunning = true;
				break;
			}
		}
		return isRunning;
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