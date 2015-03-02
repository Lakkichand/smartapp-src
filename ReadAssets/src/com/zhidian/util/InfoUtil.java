package com.zhidian.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InfoUtil {

	/**
	 * @return 返回boolean ,是否为wifi网络
	 * 
	 */
	public static final boolean hasWifiConnection(Context context) {
		final ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo networkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		// 是否有网络并且已经连接
		return (networkInfo != null && networkInfo.isConnected());
	}
	
	/**
	 * 
	 * @return 是否有活动的网络连接
	 */
	public static final boolean hasNetWorkConnection(Context context) {
		// 获取连接活动管理器
		final ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		// 获取链接网络信息
		final NetworkInfo networkInfo = connectivityManager
				.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isAvailable());
	}
	
}
