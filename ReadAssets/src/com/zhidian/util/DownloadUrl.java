package com.zhidian.util;

import com.ta.util.http.AsyncHttpClient;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.ta.util.http.RequestParams;

/**
 * 接口地址
 * @author zhaoyl
 *
 */
public class DownloadUrl {

//	public static final String BASE_URL = "http://sc.yimipingtaitest.cn/";//测试地址
	public static final String BASE_URL = "http://sc.yimipingtai.cn/";//正式地址
	
	/**
	 * 获取全部下载包路径信息接口
	 */
	
	public static final String DOWNLOADAPK = BASE_URL + "apk/checking/links.shtml";
	
	/**
	 * 验证Apk记录接口
	 */
	
	public static final String CheckFail = BASE_URL + "apk/checking/status.shtml";
	
	
	/**
	 * 同步获取网络数据
	 */
	public static void getData(String url, AsyncHttpResponseHandler handler) {
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(url, handler);
	}

	/**
	 * POST方式请求(同步）
	 */
	public static void getPostData(String url, RequestParams params,
			AsyncHttpResponseHandler handler) {
		AsyncHttpClient client = new AsyncHttpClient();
		client.post(url, params, handler);
	}

	/**
	 * POST方式请求(同步）
	 */
	public static void getPostData2(String url, RequestParams params,
			AsyncHttpResponseHandler handler) {
		AsyncHttpClient client = new AsyncHttpClient();
		client.addHeader("Content-Type", "application/x-www-form-urlencoded");
		client.post(url, params, handler);
	}
}
