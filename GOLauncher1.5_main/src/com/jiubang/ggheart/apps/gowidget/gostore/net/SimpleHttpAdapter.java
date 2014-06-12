package com.jiubang.ggheart.apps.gowidget.gostore.net;

import android.content.Context;

import com.gau.utils.net.HttpAdapter;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.util.NetLog;

public class SimpleHttpAdapter {

	private HttpAdapter mHttpAdapter = null;

	private static SimpleHttpAdapter mSelf = null;

	public static void build(Context context) {
		if (mSelf == null) {
			mSelf = new SimpleHttpAdapter(context);
		}
	}

	/**
	 * 销毁资源
	 * 
	 * @author huyong
	 */
	public static void destory() {
		if (mSelf != null) {
			mSelf.recycle();
			mSelf = null;
		}
	}

	private void recycle() {
		mHttpAdapter = null;
	}

	public static SimpleHttpAdapter getInstance() {
		return mSelf;
	}

	public static HttpAdapter getHttpAdapter(Context context) {
		SimpleHttpAdapter self = getInstance(context);
		return self.mHttpAdapter;
	}

	/**
	 * 通过参数获取HttpAdapter，若不存在，则创建。
	 * 
	 * @author huyong
	 * @param context
	 * @return
	 */
	public static SimpleHttpAdapter getInstance(Context context) {
		build(context);
		return getInstance();
	}

	private SimpleHttpAdapter(Context context) {
		mHttpAdapter = new HttpAdapter(context);
		NetLog.printLog(false);
	}

	/**
	 * 添加网络请求
	 * 
	 * @param request
	 */
	public void addTask(THttpRequest request) {
		if (mHttpAdapter != null) {
			mHttpAdapter.addTask(request);
		}
	}

	/**
	 * 取消网络请求
	 * 
	 * @param request
	 */
	public void cancelTask(THttpRequest request) {
		if (mHttpAdapter != null) {
			mHttpAdapter.cancelTask(request);
		}
	}

	/**
	 * 设置网络请求最大并发数,如果不设置，为1
	 * 
	 * @param num
	 */
	public void setMaxConnectThreadNum(int num) {
		if (mHttpAdapter != null) {
			mHttpAdapter.setMaxConnectThreadNum(num);
		}
	}
}
