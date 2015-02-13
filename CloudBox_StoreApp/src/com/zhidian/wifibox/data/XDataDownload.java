package com.zhidian.wifibox.data;

import com.ta.TAApplication;
import com.ta.util.http.AsyncHttpClient;
import com.ta.util.http.AsyncHttpResponseHandler;

/**
 * 急速模式下数据下载类
 * 
 * @author xiedezhi
 * 
 */
public class XDataDownload {

	public static final String BASE_URL = "http://d.zhidian3g.cn/";// 正式地址

	// public static final String BASE_URL =
	// "http://192.168.1.1:8000/mmcblk0p1/";//测试地址

	/**
	 * 极速模式装机必备数据URL
	 */
	public static String getXMustDataUrl() {
		return BASE_URL + "CategoryCacheFile.dat";
	}

	/**
	 * 极速模式新品推荐数据URL
	 */
	public static String getXNewDataUrl() {
		return BASE_URL + "CategoryCacheFile.dat";
	}

	/**
	 * 极速模式获取盒子编号URL
	 */
	public static String getXBoxIdUrl() {
		return BASE_URL + "config.dat";
	}

	/**
	 * 极速模式获取上网时间URL
	 */
	public static String getXTimeOnlineUrl() {
		return BASE_URL + "settings.dat";
	}

	/**
	 * 极速模式门店广告地址
	 */
	public static String getXADUrl() {
		return BASE_URL + "adv.png";
	}

	/**
	 * 异步获取网络数据
	 */
	public static void getData(String url, AsyncHttpResponseHandler handler) {
		AsyncHttpClient client = TAApplication.getApplication()
				.getAsyncHttpClient();
		client.get(url, handler);
	}
}
