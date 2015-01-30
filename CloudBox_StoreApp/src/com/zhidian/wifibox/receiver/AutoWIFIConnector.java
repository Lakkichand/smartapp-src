package com.zhidian.wifibox.receiver;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.ta.TAApplication;
import com.zhidian.wifibox.controller.ModeManager;

/**
 * 检测到盒子WIFI自动连接
 * 
 * @author xiedezhi
 * 
 */
public class AutoWIFIConnector extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {/*
		WifiManager wm = (WifiManager) TAApplication.getApplication()
				.getSystemService(Context.WIFI_SERVICE);
		if (!ModeManager.checkRapidly() && wm.isWifiEnabled()) {
			List<ScanResult> list = wm.getScanResults();
			if (list == null) {
				return;
			}
			ScanResult strong = null;
			for (ScanResult result : list) {
				String wifi = result.SSID;
				if (wifi != null && wifi.indexOf(ModeManager.XMODEPREFIX) != -1) {
					if (strong == null) {
						strong = result;
					} else if (result.level > strong.level) {
						strong = result;
					}
				}
			}
			if (strong != null) {
				Log.e("", "strong = " + strong.SSID);
				// 连接到这个WIFI
				List<WifiConfiguration> existingConfigs = wm
						.getConfiguredNetworks();
				WifiConfiguration find = null;
				for (WifiConfiguration con : existingConfigs) {
					if (con.SSID != null
							&& con.SSID.equals("\"" + strong.SSID + "\"")) {
						find = con;
						break;
					}
				}
				if (find == null) {
					WifiConfiguration config = new WifiConfiguration();
					config.allowedAuthAlgorithms.clear();
					config.allowedGroupCiphers.clear();
					config.allowedKeyManagement.clear();
					config.allowedPairwiseCiphers.clear();
					config.allowedProtocols.clear();
					config.SSID = "\"" + strong.SSID + "\"";
					config.allowedKeyManagement
							.set(WifiConfiguration.KeyMgmt.NONE);
					config.wepTxKeyIndex = 0;
					int netID = wm.addNetwork(config);// 添加
					wm.enableNetwork(netID, true);// 启动
				} else {
					wm.enableNetwork(find.networkId, true);// 启动
				}
			}
		}
	*/}
}
