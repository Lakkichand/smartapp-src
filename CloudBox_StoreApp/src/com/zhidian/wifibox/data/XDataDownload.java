package com.zhidian.wifibox.data;

import com.ta.util.http.AsyncHttpClient;
import com.ta.util.http.AsyncHttpResponseHandler;

/**
 * 急速模式下数据下载类
 * 
 * @author xiedezhi
 * 
 */
public class XDataDownload {

	public static final String BASE_URL = "http://d.zhidian3g.cn/";

	/**
	 * 极速模式装机必备数据URL
	 */
	public static String getXMustDataUrl() {
		return "http://d.zhidian3g.cn/CategoryCacheFile.dat";
	}

	/**
	 * 极速模式装机必备数据URL
	 */
	public static String getXMustIDUrl() {
		return BASE_URL + "caches/CategoryCacheFile.dat_XMUST";
	}

	/**
	 * 极速模式新品推荐数据URL
	 */
	public static String getXNewDataUrl() {
		return "http://d.zhidian3g.cn/CategoryCacheFile.dat";
	}

	/**
	 * 极速模式新品推荐标识URL
	 * 
	 * @return
	 */
	public static String getXNewIDUrl() {
		return BASE_URL + "caches/CategoryCacheFile.dat_XNEW";
	}

	/**
	 * 极速模式全部应用URL
	 */
	public static String getXAllUrl() {
		return "http://d.zhidian3g.cn/AllAppCacheFile.dat";
	}

	/**
	 * 极速模式获取盒子编号URL
	 */
	public static String getXBoxIdUrl() {
		return "http://d.zhidian3g.cn/config.dat";
	}

	/**
	 * 极速模式获取上网时间URL
	 */
	public static String getXTimeOnlineUrl() {
		return "http://d.zhidian3g.cn/settings.dat";
	}

	/**
	 * 极速模式门店广告地址
	 */
	public static String getXADUrl() {
		return "http://d.zhidian3g.cn/adv.png";
	}

	/**
	 * 异步获取网络数据
	 */
	public static void getData(String url, AsyncHttpResponseHandler handler) {
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(url, handler);
	}
}
