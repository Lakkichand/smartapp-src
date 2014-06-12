package com.jiubang.ggheart.data;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.snapshot.SnapShotManager;
import com.jiubang.ggheart.data.info.ThemeSettingInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ThreadName;

/**
 *
 */
public class AppService extends Service {
	// 定义个一个Tag标签
	private static final String TAG = "DemoService";
	// 这里定义吧一个Binder类，用在onBind()有方法里，这样Activity那边可以获取到
	private MyBinder mBinder = new MyBinder();

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "start IBinder~~~");
		return mBinder;
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "DemoService onCreate before load~~~");
		super.onCreate();

		startForeground();

		DrawUtils.resetDensity(this);
		if (GoLauncher.getContext() == null) {
			// 桌面尚未启动起来，则表示当前是系统启动的service，可以去加载图标
			Log.d(TAG, "DemoService onCreate to loadicon for golauncher is null~~");
			final AppDataEngine aDE = AppDataEngine.getInstance(getApplicationContext());
			// 使用线程load图标
			// 如果这个时候点击进入桌面，不会出现卡死现象
			new Thread(ThreadName.APP_SERVICE_LOAD_DATA) {
				@Override
				public void run() {
					aDE.loadInitDataInService();
					Log.d(TAG, "DemoService Load done");
				}
			}.start();
		};
		Log.d(TAG, "DemoService onCreate end load~~~");
		PreferencesManager sharedPreferences = new PreferencesManager(this,
				IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
		boolean isScreenShotrunning = sharedPreferences.getBoolean(IPreferencesIds.IS_SCREENSHOT_RUNNING, false);
		// 如果服务非正常退出，而且之前开了截图那么就发消息去通知栏取消截图通知
		if (isScreenShotrunning) {
			if (SnapShotManager.getInstance(GOLauncherApp.getContext()).status == SnapShotManager.STATUS_STOP) {
				boolean isRoot = sharedPreferences.getBoolean(
						IPreferencesIds.IS_SCREENSHOT_RUNNING_IN_ROOT_MODE, false);
				SnapShotManager.getInstance(GOLauncherApp.getContext()).setRootMode(isRoot);
				SnapShotManager.getInstance(GOLauncherApp.getContext()).startCapture();
				if (!isRoot) {
					SnapShotManager.getInstance(GOLauncherApp.getContext()).pauseCapture();
				}
			}
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d(TAG, "DemoService onStart~~~");
		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "start onDestroy~~~");
		super.onDestroy();
	}

	public void startForeground() {
		// SharedPreferences spf =
		// getApplicationContext().getSharedPreferences("Foregound", 0);
		// boolean bNeedForegound = spf.getBoolean("NEEDFOREGOUND", true);
		ThemeSettingInfo mThemeInfo = GOLauncherApp.getSettingControler().getThemeSettingInfo();

		if (!mThemeInfo.mIsPemanentMemory) {
			return;
		}

		Notification localNotification = new Notification(0, "GOLauncher",
				System.currentTimeMillis());
		localNotification.setLatestEventInfo(this, "GOLauncher", "GOLauncher Running",
				PendingIntent.getActivity(this, 0, new Intent(this, ForegroundDialog.class), 0));
		// 0x123456 Notification对应的id
		startForeground(0x123456, localNotification);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "start onUnbind~~~");
		return super.onUnbind(intent);
	}

	/*
	 * public String getSystemTime(){ Time t = new Time(); t.setToNow(); return
	 * t.toString(); }
	 */

	/**
	 *
	 */
	public class MyBinder extends Binder {
		public AppService getService() {
			return AppService.this;
		}
	}

}
