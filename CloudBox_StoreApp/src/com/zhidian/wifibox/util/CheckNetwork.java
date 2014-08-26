package com.zhidian.wifibox.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * 网络检测
 * 
 * @author Administrator
 * 
 */
public class CheckNetwork {

	/**
	 * 是否有网络连接
	 */
	public static boolean isConnect(Context context) {
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			Log.v("error", e.toString());
		}
		return false;
	}

	/**
	 * @author 获取当前的网络状态
	 * @param context
	 */

	public static String getAPNType(Context context) {
		String netType = "";
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo == null) {
			return netType;
		}

		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			netType = isConnectionFast(networkInfo.getSubtype());

		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = "WIFI";
		}
		return netType;

	}

	private static String isConnectionFast(int subType) {
		switch (subType) {
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			return "2G"; // ~ 50-100 kbps
		case TelephonyManager.NETWORK_TYPE_CDMA:
			return "2G"; // ~ 14-64 kbps
		case TelephonyManager.NETWORK_TYPE_EDGE:
			return "2G"; // ~ 50-100 kbps
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			return "3G"; // ~ 400-1000 kbps
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			return "3G"; // ~ 600-1400 kbps
		case TelephonyManager.NETWORK_TYPE_GPRS:
			return "2G"; // ~ 100 kbps
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			return "3G"; // ~ 2-14 Mbps
		case TelephonyManager.NETWORK_TYPE_HSPA:
			return "3G"; // ~ 700-1700 kbps
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			return "3G"; // ~ 1-23 Mbps
		case TelephonyManager.NETWORK_TYPE_UMTS:
			return "3G"; // ~ 400-7000 kbps
		case TelephonyManager.NETWORK_TYPE_LTE: // 4G
			return "4G";
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			return "unknown";
		default:
			return "unknown";

		}
	}

}
