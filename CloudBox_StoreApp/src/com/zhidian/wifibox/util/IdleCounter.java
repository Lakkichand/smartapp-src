package com.zhidian.wifibox.util;

import java.util.List;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;

import com.ta.TAApplication;

/**
 * 用户连接MI-BOX WIFI时间大于用户【上网时间】+1小时，系统自动把用户自动连接MI-BOX WIFI 改为 不自动连接。
 * 
 * @author xiedezhi
 */
public class IdleCounter extends CountDownTimer {

	private static IdleCounter sIdleCount;

	/**
	 * 监听wifi空闲
	 */
	public synchronized static void listenIdle(String wifi,
			long millisInFuture, long countDownInterval) {
		if (sIdleCount != null) {
			sIdleCount.cancel();
		}
		sIdleCount = new IdleCounter(wifi, millisInFuture, countDownInterval);
		sIdleCount.start();
	}

	private String mWifi;

	private IdleCounter(String wifi, long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
		mWifi = wifi;
	}

	@Override
	public void onTick(long millisUntilFinished) {
		// 获取当前WIFI名称
		if (!InfoUtil.getCurWifiName(TAApplication.getApplication()).equals(
				mWifi)) {
			cancel();
		}
	}

	@Override
	public void onFinish() {
		// 删掉当前网络
		if (InfoUtil.getCurWifiName(TAApplication.getApplication()).equals(
				mWifi)) {
			WifiManager wm = (WifiManager) TAApplication.getApplication()
					.getSystemService(Context.WIFI_SERVICE);
			List<WifiConfiguration> existingConfigs = wm
					.getConfiguredNetworks();
			for (WifiConfiguration con : existingConfigs) {
				if (con.SSID != null && con.SSID.equals(mWifi)) {
					wm.removeNetwork(con.networkId);
				}
			}
		}
	}

}
