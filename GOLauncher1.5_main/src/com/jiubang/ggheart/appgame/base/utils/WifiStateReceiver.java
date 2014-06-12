/*
 * 文 件 名:  WifiStateReceiver.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-12-27
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.utils;

import java.util.Timer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-12-27]
 */
public class WifiStateReceiver extends BroadcastReceiver {

	private Timer mTimer = null;
	/** {@inheritDoc} */

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
			int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
			switch (wifiState) {
				case WifiManager.WIFI_STATE_DISABLED :
					if (mTimer != null) {
						mTimer.cancel();
					}
					break;
				case WifiManager.WIFI_STATE_ENABLED :
					 mTimer = new Timer();
					 WifiTimerTask task = new WifiTimerTask(GOLauncherApp.getContext());
					 mTimer.schedule(task, WifiTimerTask.WATCH_TIME_STEP);
					break;
			}
		}
	}

}
